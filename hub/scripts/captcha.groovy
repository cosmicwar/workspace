package scripts

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.scheduler.Task
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import scripts.shared.database.Standard
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.objects.MutableInt
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.legacy.utils.TitleUtils
import scripts.shared.systems.ServerCache
import scripts.shared.utils.Persistent
import scripts.shared.utils.Temple
import scripts.shared3.Redis

import java.util.concurrent.ConcurrentHashMap

Captcha.init()

Redis.get().subscribe({ channel, message ->
    Captcha.CAPTCHA_BYPASS.put(message.split("\\|")[1], System.currentTimeMillis())
}, Standard.PCH_SEND)

Schedulers.async().runRepeating({
    Iterator<Map.Entry<String, Long>> iterator = Captcha.CAPTCHA_BYPASS.entrySet().iterator()

    while (iterator.hasNext()) {
        if (System.currentTimeMillis() - iterator.next().getValue() >= 5000L) {
            iterator.remove()
        }
    }
}, 20, 20)

Set<UUID> disabledInputPlayers = Persistent.of("disabled_input_players", ConcurrentHashMap.newKeySet()).get() as Set<UUID>

Commands.create().assertPermission("commands.showdisabled").handler { command ->
    command.reply(disabledInputPlayers.toString())
}.register("showdisabled")

Commands.create().assertPermission("commands.showcaptchas").handler { command ->
    command.reply(Captcha.CAPTCHAS.entrySet().toString())
}.register("showcaptchas")

Events.subscribe(PlayerLoginEvent.class).handler { event ->
    disabledInputPlayers.add(event.getPlayer().getUniqueId())
}

Events.subscribe(PlayerInteractEvent.class, EventPriority.LOWEST).handler { event ->
    if (disabledInputPlayers.contains(event.getPlayer().getUniqueId())) {
        event.setUseItemInHand(Event.Result.DENY)
    }
}

Exports.ptr("handleJoin", { Player player ->
    Location spawn = Exports.ptr("getSpawn") as Location
    double y = spawn == null ? player.getLocation().getY() : spawn.getY()

    Vector velocity = new Vector(0.0D, 0.075D, 0.0D)
    player.setVelocity(velocity)

    int iterations = 20 * 6
    MutableInt iterated = new MutableInt(0)

    Task task

    task = Schedulers.sync().runRepeating({
        if (iterated.intValue() >= iterations) {
            disabledInputPlayers.remove(player.getUniqueId())
            addToCaptcha(player)

            task.stop()
            return
        }
        if (player.getLocation().getY() > y) {
            disabledInputPlayers.remove(player.getUniqueId())
            task.stop()
        }
        player.setVelocity(velocity)

        iterated.increment()
    }, 1L, 1L)
})

static void addToCaptcha(Player player) {
    UUID uuid = player.getUniqueId()

    if (Captcha.CAPTCHA_BYPASS.containsKey(player.getName()) || player.hasPermission("group.staff") || player.hasPermission("captcha.bypass")) {
        handleSucceess(player)
        return
    }
    println "[CAPTCHA] Giving ${player.getName()} captcha"

    for (Player online : Bukkit.getOnlinePlayers()) {
        online.hidePlayer(Starlight.plugin, player)
        player.hidePlayer(Starlight.plugin, online)
    }
    int[] captcha = getCaptcha()
    int[] offset = getOffset()

    if (RandomUtils.RANDOM.nextBoolean()) {
        offset[0] = -offset[0]
    }
    if (RandomUtils.RANDOM.nextBoolean()) {
        offset[2] = -offset[2]
    }
    Task task = Schedulers.sync().runRepeating({
        renderNumbers(player, captcha, offset, false)
    }, 4L, 200L)

    Captcha.CAPTCHAS.put(uuid, captcha)
    Captcha.OFFSETS.put(uuid, offset)
    Captcha.TASKS.put(uuid, task)

    if ((Exports.ptr("redirects") as Map<UUID, String>)?.get(uuid) != null) {
        Players.msg(player, "\n§] §8» §fPlease complete the captcha to proceed to the server! §7§o(/captcha <captcha>)\n ")
    }
    TitleUtils.show(player, "§4CAPTCHA", "§8Type the 3 numbers you see near you", 0, 5, 1)
}

/*
Events.subscribe(PlayerJoinEvent.class).handler { event ->\
    addToCaptcha(event.getPlayer())
}
*/

Events.subscribe(PlayerQuitEvent.class).handler { event ->
    Player player = event.getPlayer()
    UUID uuid = player.getUniqueId()

    disabledInputPlayers.remove(uuid)
    Captcha.CAPTCHAS.remove(uuid)
    Captcha.OFFSETS.remove(uuid)
    Captcha.ATTEMPTS.remove(uuid)
    Captcha.TASKS.remove(uuid)
    Captcha.WHITELIST_WAITING.remove(uuid)
    Captcha.CAPTCHA_BYPASS.remove(player.getName())
}

Events.subscribe(PlayerCommandPreprocessEvent.class, EventPriority.HIGHEST).handler { event ->
    String message = event.getMessage().toLowerCase()
    String[] args = message.split(" ")

    if (!["gotoserver", "goto", "server", "skerver", "dev_server"].contains(args[0].substring(1))) {
        return
    }
    Player player = event.getPlayer()

    if (player.hasPermission("group.staff") || player.hasPermission("captcha.bypass")) {
        return
    }
    UUID uuid = player.getUniqueId()

    if (disabledInputPlayers.contains(uuid)) {
        event.setCancelled(true)
        return
    }
    int[] captcha = Captcha.CAPTCHAS.get(uuid)

    if (captcha != null) {
        String stringCaptcha = "${captcha[0]}${captcha[1]}${captcha[2]}"

        if (message.contains(stringCaptcha) && handleAttempt(player, stringCaptcha, true)) {
            return
        }
        /*
        if (args.length == 3 && args[2].equalsIgnoreCase("bypass")) {
            if (ServerCache.servers.get(args[1]) != null) {
                UUID uuid = player.getUniqueId()

                MySQL.getDatabase().async().execute("INSERT INTO captcha_bypassers (uuid_least, uuid_most) VALUES (?, ?)", { statement ->
                    statement.setLong(1, uuid.getLeastSignificantBits())
                    statement.setLong(2, uuid.getMostSignificantBits())
                })
            }
        } else {
            Players.msg(player, "§c§lERROR >> §fYou must complete the captcha before executing this command! The captcha is the giant 3 numbers floating in the air §7§o(/captcha <captcha>)")
            event.setCancelled(true)
        }
         */
        Players.msg(player, "\n§! §> §fYou must complete the captcha before executing this command! The captcha is the giant 3 numbers floating in the air §7§o(/captcha <captcha>)\n ")
        event.setCancelled(true)
    }
}

Events.subscribe(PlayerInteractEvent.class, EventPriority.LOWEST).handler { event ->
    Player player = event.getPlayer()

    if (player.hasPermission("group.staff") || player.hasPermission("captcha.bypass")) {
        return
    }
    UUID uuid = player.getUniqueId()
    int[] captcha = Captcha.CAPTCHAS.get(uuid)
    int[] offset = Captcha.OFFSETS.get(uuid)

    ItemStack item = event.getItem()

    if (item != null && item.getType() == Material.COMPASS && captcha != null) {
        Players.msg(player, "\n§! §> §fYou must complete the captcha before using the server selector! The captcha is 3 red numbers floating in the air §7§o(/captcha <captcha>)\n ")
        event.setCancelled(true)
    }
    Action action = event.getAction()

    if ((action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR) && captcha != null) {
        Schedulers.sync().runLater({
            renderNumbers(player, captcha, offset, false)
        }, 3)
    }
}

Commands.create().assertPlayer().assertUsage("<captcha>").handler { command ->
    handleAttempt(command.sender(), command.rawArg(0), false)
}.register("captcha")

Events.subscribe(AsyncPlayerChatEvent.class, EventPriority.HIGH).filter(EventFilters.ignoreCancelled()).handler { event ->
    Player player = event.getPlayer()
    int[] captcha = Captcha.CAPTCHAS.get(player.getUniqueId())

    if (captcha == null) {
        return
    }
    String stringCaptcha = "${captcha[0]}${captcha[1]}${captcha[2]}"
    String message = event.getMessage()

    if (message.contains(stringCaptcha) && handleAttempt(player, stringCaptcha, true)) {
        event.setCancelled(true)
    }
}

static boolean handleAttempt(Player player, String attempt, boolean silent) {
    UUID uuid = player.getUniqueId()
    int[] captcha = Captcha.CAPTCHAS.get(uuid)

    if (captcha == null) {
        if (!silent) {
            Players.msg(player, "§! §> §fYou do not have an active captcha!")
        }
        return false
    }
    if (attempt.length() != 3) {
        if (!silent) {
            fail(player)
        }
        return false
    }
    boolean matches = true

    for (int i = 0; i < 3; ++i) {
        if (attempt.charAt(i) - 48 != captcha[i]) {
            matches = false
            break
        }
    }
    if (!matches) {
        if (!silent) {
            fail(player)
        }
        return false
    }
    println "[SUCCESS] ${player.getName()}"

    int[] offset = Captcha.OFFSETS.remove(uuid)

    Players.msg(player, "\n§] §8» §fSuccessfuly completed the captcha!\n ")
    Captcha.CAPTCHAS.remove(uuid)
    Captcha.TASKS.remove(uuid)?.stop()

    renderNumbers(player, captcha, offset, true)
    handleSucceess(player)

    return true
}

static void fail(Player player) {
    UUID uuid = player.getUniqueId()

    int failed = (Captcha.ATTEMPTS.getOrDefault(uuid, 0) as int) + 1

    if (failed == 3) {
        player.kickPlayer("§cYou have failed to complete the captcha!")
        return
    }
    Players.msg(player, "§c§lERROR §> §fIncorrect captcha, try again! §7§o(§e${failed}§7/§e3 §7§otries used)")
    Captcha.ATTEMPTS.put(uuid, failed)
}

static void handleSucceess(Player player) {
    for (Player online : Bukkit.getOnlinePlayers()) {
        if (Captcha.CAPTCHAS.containsKey(online.getUniqueId())) {
            continue
        }
        online.showPlayer(Starlight.plugin, player)
        player.showPlayer(Starlight.plugin, online)
    }
    String temple = (Exports.ptr("redirects") as Map<UUID, String>)?.get(player.getUniqueId())

    if (temple == null) {
        return
    }
    boolean online = ServerCache.servers.get(temple) != null
    boolean whitelisted = (Exports.ptr("whitelisted_servers") as Map<String, Boolean>)?.getOrDefault(temple, false)

    if (whitelisted) {
        Players.msg(player,"\n§] §8» §fThe server is currently whitelisted, please wait until it is open!\n ")
        Captcha.WHITELIST_WAITING.put(player.getUniqueId(), temple)
        return
    }
    if (online) {
        player.performCommand("goto ${temple}")
    } else {
        Players.msg(player, "\n§] §8» §fThe server is currently rebooting, please wait up to §e1 minute§f!\n ")
        Redis.get().async { redis -> redis.sadd("${Standard.SRV_SENDBACK}${Temple.templeId}".toString(), player.getName()) }
    }
}

Redis.get().subscribe({ channel, message ->
    String[] data = message.split(" ")

    if (Boolean.valueOf(data[1])) {
        return
    }
    String temple = data[0]
    boolean online = ServerCache.servers.get(temple) != null

    Iterator<Map.Entry<UUID, String>> iterator = Captcha.WHITELIST_WAITING.entrySet().iterator()

    while (iterator.hasNext()) {
        Map.Entry<UUID, String> entry = iterator.next()

        if (entry.getValue() != temple) {
            continue
        }
        iterator.remove()

        Player player = Bukkit.getPlayer(entry.getKey())

        if (player == null) {
            continue
        }
        if (online) {
            player.performCommand("goto ${temple}")
        } else {
            Redis.get().sync { redis -> redis.sadd("${Standard.SRV_SENDBACK}${Temple.templeId}".toString(), player.getName()) }
        }
    }
}, "whitelisted_servers")

static void renderNumbers(Player player, int[] captcha, int[] offset, boolean real) {
    Location spawn = player.getWorld().getSpawnLocation()

    Schedulers.async().run {
        renderNumber(player, captcha[0], spawn.clone().add(-8 + offset[0], 1 + offset[1], -10 + offset[2]), BlockFace.EAST, real)
        renderNumber(player, captcha[1], spawn.clone().add(-2 + offset[0], 1 + offset[1], -10 + offset[2]), BlockFace.EAST, real)
        renderNumber(player, captcha[2], spawn.clone().add(4 + offset[0], 1 + offset[1], -10 + offset[2]), BlockFace.EAST, real)
    }
}

static void renderNumber(Player player, int number, Location location, BlockFace direction, boolean real) {
    boolean[][] plane = CaptchaFont.NUMBERS[number]

    switch (direction) {
        case BlockFace.EAST:
            for (int y = 0; y < plane.length; ++y) {
                boolean[] xAxis = plane[y]

                for (int x = 0; x < xAxis.length; ++x) {
                    if (xAxis[x]) {
                        location.add(x, plane.length - y - 1, 0)
                        player.sendBlockChange(location, (real ? Material.AIR : Material.RED_WOOL).createBlockData())
                        location.subtract(x, plane.length - y - 1, 0)
                    }
                }
            }
            break
        default:
            break
    }
}

static int[] getCaptcha() {
    Random random = new Random()

    return [
            random.nextInt(10),
            random.nextInt(10),
            random.nextInt(10)
    ] as int[]
}

static int[] getOffset() {
    Random random = new Random()

    return [
            random.nextInt(5),
            random.nextInt(3),
            random.nextInt(2)
    ] as int[]
}

class Captcha {
    static Map<UUID, int[]> CAPTCHAS
    static Map<UUID, Integer> ATTEMPTS
    static Map<UUID, Task> TASKS
    static Map<UUID, int[]> OFFSETS
    static Map<UUID, String> WHITELIST_WAITING
    static Map<String, Long> CAPTCHA_BYPASS

    static void init() {
        CAPTCHAS = Persistent.of("captchas", new ConcurrentHashMap<UUID, int[]>()).get()
        ATTEMPTS = Persistent.of("captcha_attempts", new HashMap<UUID, Integer>()).get()
        TASKS = Persistent.of("captcha_tasks", new HashMap<UUID, Task>()).get()
        OFFSETS = Persistent.of("captcha_offsets", new HashMap<UUID, int[]>()).get()
        WHITELIST_WAITING = Persistent.of("whitelist_waiting", new HashMap<UUID, String>()).get()
        CAPTCHA_BYPASS = Persistent.of("captcha_bypassing", new ConcurrentHashMap<String, Long>()).get()

        MySQL.getDatabase().async().execute("CREATE TABLE IF NOT EXISTS captcha_bypassers (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, PRIMARY KEY(uuid_least, uuid_most))")

        Exports.ptr("hasCaptcha", { Player player -> CAPTCHAS.containsKey(player.getUniqueId()) })
    }
}

class CaptchaFont {
    public static final boolean[][][] NUMBERS = getArray(
            // 0
            getArray(
                    getArray(false, true, true, true, false),
                    getArray(true, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(false, true, true, true, false)
            ),
            // 1
            getArray(
                    getArray(false, false, true, false, false),
                    getArray(false, true, true, false, false),
                    getArray(false, false, true, false, false),
                    getArray(false, false, true, false, false),
                    getArray(false, false, true, false, false),
                    getArray(false, false, true, false, false),
                    getArray(false, false, true, false, false),
                    getArray(true, true, true, true, true)
            ),
            // 2
            getArray(
                    getArray(false, true, true, true, false),
                    getArray(true, false, false, false, true),
                    getArray(false, false, false, false, true),
                    getArray(false, false, false, true, false),
                    getArray(false, false, true, false, false),
                    getArray(false, true, false, false, false),
                    getArray(true, false, false, false, true),
                    getArray(true, true, true, true, true)
            ),
            // 3
            getArray(
                    getArray(false, true, true, true, false),
                    getArray(true, false, false, false, true),
                    getArray(false, false, false, false, true),
                    getArray(false, false, true, true, false),
                    getArray(false, false, false, false, true),
                    getArray(false, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(false, true, true, true, false)
            ),
            // 4
            getArray(
                    getArray(false, false, false, true, false),
                    getArray(false, false, true, true, false),
                    getArray(false, true, false, true, false),
                    getArray(true, false, false, true, false),
                    getArray(true, true, true, true, true),
                    getArray(false, false, false, true, false),
                    getArray(false, false, false, true, false),
                    getArray(false, false, false, true, false)
            ),
            // 5
            getArray(
                    getArray(true, true, true, true, true),
                    getArray(true, false, false, false, false),
                    getArray(true, false, false, false, false),
                    getArray(true, true, true, true, false),
                    getArray(false, false, false, false, true),
                    getArray(false, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(false, true, true, true, false)
            ),
            // 6
            getArray(
                    getArray(false, false, true, true, false),
                    getArray(false, true, false, false, false),
                    getArray(true, false, false, false, false),
                    getArray(true, true, true, true, false),
                    getArray(true, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(false, true, true, true, false)
            ),
            // 7
            getArray(
                    getArray(true, true, true, true, true),
                    getArray(false, false, false, false, true),
                    getArray(false, false, false, true, false),
                    getArray(false, false, false, true, false),
                    getArray(false, false, true, false, false),
                    getArray(false, false, true, false, false),
                    getArray(false, true, false, false, false),
                    getArray(false, true, false, false, false)
            ),
            // 8
            getArray(
                    getArray(false, true, true, true, false),
                    getArray(true, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(false, true, true, true, false),
                    getArray(true, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(false, true, true, true, false)
            ),
            // 9
            getArray(
                    getArray(false, true, true, true, false),
                    getArray(true, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(true, false, false, false, true),
                    getArray(false, true, true, true, true),
                    getArray(false, false, false, false, true),
                    getArray(false, false, false, true, false),
                    getArray(false, true, true, false, false)
            )
    )

    private static boolean[] getArray(boolean ... booleans) {
        return booleans
    }

    private static boolean[][] getArray(boolean[] ... booleans) {
        return booleans
    }

    private static boolean[][][] getArray(boolean[][] ... booleans) {
        return booleans
    }
}