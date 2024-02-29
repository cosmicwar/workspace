package scripts.factions.cfg

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scripts.Globals
import scripts.shared.legacy.CooldownUtils
import scripts.shared.legacy.database.mysql.AsyncDatabase
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.objects.MutableInt
import scripts.shared.legacy.objects.Pair
import scripts.shared.legacy.wrappers.Console
import scripts.shared.utils.Persistent
import scripts.shared.utils.Temple
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.ThreadUtils
import scripts.shared.legacy.utils.TimeUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.systems.MenuEvent

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

static MenuEvent getMenuEvent(int i) {
    return { Player p, t, s ->
        UUID uuid = p.getUniqueId()

        Map.Entry<Long, Integer> data = CalendarUtils.LAST_CLAIMED.getOrDefault(uuid, Pair.of(0L, 0))
        int claimed = data.getValue()

        if (i > claimed) {
            Players.msg(p, "§! §> §fYou must claim the previous rewards before claiming this one!")
            return
        }
        if (i < claimed) {
            return
        }

        long millisPerDay = TimeUnit.DAYS.toMillis(1L)
        if (System.currentTimeMillis() - data.getKey() < millisPerDay) {
            Players.msg(p, "§! §> §fYou must wait §e${TimeUtils.getTimeAmount(millisPerDay - (System.currentTimeMillis() - data.getKey()))} §fbefore claiming your next reward!")
            p.closeInventory()
            return
        }

        p.closeInventory()
        int multiplier = 1

        Closure<Boolean> hasPremium = Exports.ptr("hasPremium") as Closure<Boolean>

        if (hasPremium != null && hasPremium.call(p)) {
            multiplier = 2
        }
        for (String command : (Exports.ptr("calendar") as Map<Integer, List<String>>).get(i + 1)) {
            for (int j = 0; j < multiplier; ++j) {
                Console.dispatchCommand(command.replace("%player%", p.getName()))
            }
        }
        if (++claimed > 30) {
            claimed = 0
        }

        long lastClaimed
        if (Globals.NEW_DAILY_COOLDOWNS) {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"))
            LocalDateTime nextMidnight = LocalDateTime.of(now.toLocalDate(), LocalTime.MIDNIGHT).plusDays(1L)
            long millsUntilMidnight = now.until(nextMidnight, ChronoUnit.MILLIS)
            long diff = millisPerDay - millsUntilMidnight
            lastClaimed = System.currentTimeMillis() - Math.max(0L, diff)
        } else {
            lastClaimed = System.currentTimeMillis()
        }

        CalendarUtils.LAST_CLAIMED.put(uuid, Pair.of(lastClaimed, claimed))

        MySQL.getAsyncDatabase().execute("INSERT INTO daily_calendar_last_claimed (uuid_least, uuid_most, last_claimed, claimed, server_id) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE last_claimed = VALUES(last_claimed), claimed = VALUES(claimed)", { statement ->
            statement.setLong(1, uuid.getLeastSignificantBits())
            statement.setLong(2, uuid.getMostSignificantBits())
            statement.setLong(3, lastClaimed)
            statement.setInt(4, claimed)
            statement.setString(5, Temple.templeId)
        })
        showCalendar(p)
    }
}

static void showCalendar(Player player) {
    int claimed = CalendarUtils.LAST_CLAIMED.getOrDefault(player.getUniqueId(), Pair.of(0L, 0)).getValue()

    boolean toggle = true

    ItemStack red = FastItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "§0", [])
    ItemStack blue = FastItemUtils.createItem(Material.BLUE_STAINED_GLASS_PANE, "§0", [])

    MenuBuilder builder = new MenuBuilder(54, "§8Daily Calendar")

    for (int i : [2, 1, 0, 9, 18, 27, 36, 45, 46, 47, 48, 49, 50, 51, 52, 53, 44, 35, 26, 17, 8, 7, 6]) {
        builder.set(i, toggle ? red : blue, { p, t, s -> })
        toggle = !toggle
    }

    for (int i = 0; i < 31; ++i) {
        ItemStack item
        if (claimed > i) {
            item = FastItemUtils.createItem(Material.EMERALD_BLOCK, "§aReward #${i + 1}", ["", "§fStatus: §aClaimed"])
            FastItemUtils.addGlow(item)
        } else {
            item = FastItemUtils.createItem(Material.REDSTONE_BLOCK, "§cReward #${i + 1}", ["", "§fStatus: §cUnclaimed"])
        }

        if (i < 3) {
            builder.set(3 + i, item, getMenuEvent(i))
        } else {
            int row = ((i - 3).intdiv(7) as int) + 1
            builder.set(row * 9 + 1 + (i - 3) % 7, item, getMenuEvent(i))
        }
    }
    MenuUtils.syncOpen(player, builder)
}

Commands.create().assertPlayer().handler { command ->
    showCalendar(command.sender())
}.register("dailycalendar", "daily", "calendar")

Commands.create().assertPermission("commands.clearcalendar").handler { command ->
    MySQL.getAsyncDatabase().execute("DELETE FROM daily_calendar WHERE ${DatabaseUtils.getServerIdExpression()}")
    CalendarUtils.CLAIMED.clear()
    command.reply("§b§lSERVER §> §fSuccessfully cleared calendar cache!")
}.register("cleardailycalendar", "cleardaily", "clearcalendar")

CalendarUtils.init()

class CalendarUtils {
    static boolean LOADED
    static Map<String, Map<UUID, MutableInt>> CLAIMED
    static Map<UUID, Map.Entry<Long, Integer>> LAST_CLAIMED

    static void init() {
        Map<Integer, List<String>> rewards = Exports.ptr("calendar_config") as Map<Integer, List<String>>
        Exports.ptr("calendar", rewards)

        LOADED = Persistent.persistentMap.containsKey("daily_calendar")
        CLAIMED = Persistent.of("daily_calendar", new HashMap<String, Map<UUID, MutableInt>>()).get()
        LAST_CLAIMED = Persistent.of("calendar_last_claimed", new HashMap<UUID, Map.Entry<Long, Integer>>()).get()

        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS daily_calendar (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, month VARCHAR(8) NOT NULL, claimed INT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, month, server_id))")
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS daily_calendar_last_claimed (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, last_claimed BIGINT NOT NULL, claimed INT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, server_id))")

        if (LOADED) {
            return
        }
        ThreadUtils.runAsync {
            Logger logger = Starlight.plugin.getLogger()
            logger.info("Loading daily calendar...")

            String currentMonth = getMonth()

            AsyncDatabase database = MySQL.getSyncDatabase()

            database.executeQuery("SELECT * FROM daily_calendar WHERE ${DatabaseUtils.getServerIdExpression()}", { statement -> }, { result ->
                while (result.next()) {
                    UUID uuid = new UUID(result.getLong(2), result.getLong(1))
                    String month = result.getString(3)
                    int claimed = result.getInt(4)

                    if (month == currentMonth) {
                        CLAIMED.computeIfAbsent(month, { k -> new HashMap<>() }).put(uuid, new MutableInt(claimed))
                    }
                }
            })
            database.executeQuery("SELECT * FROM daily_calendar_last_claimed WHERE ${DatabaseUtils.getServerIdExpression()}", { statement -> }, { result ->
                while (result.next()) {
                    LAST_CLAIMED.put(new UUID(result.getLong(2), result.getLong(1)), Pair.of(result.getLong(3), result.getInt(4)))
                }
            })
            logger.info("Loaded daily calendar!")
        }
    }

    static String getMonth() {
        SimpleDateFormat format = new SimpleDateFormat("MM-yyyy")
        format.setTimeZone(TimeZone.getTimeZone("EST"))
        return format.format(new Date())
    }
}