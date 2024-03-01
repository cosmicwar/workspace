package scripts.factions.eco.crates

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.scheduler.Task
import org.starcade.starlight.helper.utils.Players
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import org.bukkit.util.EulerAngle
import scripts.factions.cfg.WorldConfig
import scripts.factions.eco.loottable.v2.api.LootTable
import scripts.factions.eco.loottable.v2.api.Reward
import scripts.factions.eco.loottable.LootTableHandler
import scripts.shared.features.EntityGlow
import scripts.shared.features.holograms.HologramRegistry
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.objects.Pair
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.IntegerUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.PacketUtils
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.legacy.utils.ThreadUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.Persistent
import scripts.shared.utils.Temple

Schedulers.async().runRepeating({
    String prefix = WorldConfig.SPAWN_WORLD_PREFIX

    if (prefix == null) {
        return
    }
    for (Map<String, Object> crate1 : Exports.ptr("crates") as List<Map<String, Object>>) {
        Map<String, Object> crate = crate1

        Location location = crate.get("location") as Location

        if (location == null) {
            continue
        }

        World world = Bukkit.getWorld("${prefix}")

        float radius = 0.4F
        float mode = 0.01F

        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.FIREWORK, true, (float) location.getX(), (float) location.getY() + 0.0F, (float) location.getZ(), radius, 0, radius, mode, 1)
        ClientboundLevelParticlesPacket packet1 = new ClientboundLevelParticlesPacket(ParticleTypes.FIREWORK, true, (float) location.getX(), (float) location.getY() + 0.8F, (float) location.getZ(), radius, 0, radius, mode, 1)
        ClientboundLevelParticlesPacket packet2 = new ClientboundLevelParticlesPacket(ParticleTypes.FIREWORK, true, (float) location.getX(), (float) location.getY() + 0.6F, (float) location.getZ(), radius, 0, radius, mode, 1)
        ClientboundLevelParticlesPacket packet3 = new ClientboundLevelParticlesPacket(ParticleTypes.FIREWORK, true, (float) location.getX(), (float) location.getY() + 0.4F, (float) location.getZ(), radius, 0, radius, mode, 1)
        ClientboundLevelParticlesPacket packet4 = new ClientboundLevelParticlesPacket(ParticleTypes.FIREWORK, true, (float) location.getX(), (float) location.getY() + 0.2F, (float) location.getZ(), radius, 0, radius, mode, 1)
        ClientboundLevelParticlesPacket packet5 = new ClientboundLevelParticlesPacket(ParticleTypes.FIREWORK, true, (float) location.getX(), (float) location.getY() + 0.85F, (float) location.getZ(), radius, 0, radius, mode, 1)
        PacketUtils.send(world, packet, null, 2)
        PacketUtils.send(world, packet1, null, 1)
        PacketUtils.send(world, packet2, null, 2)
        PacketUtils.send(world, packet3, null, 2)
        PacketUtils.send(world, packet4, null, 2)
        PacketUtils.send(world, packet5, null, 1)
    }
}, 0L, 3 * 20L)

Schedulers.sync().runLater({

    def category = LootTableHandler.getLootTableCategory("crates", true)
    category.icon = Material.CHEST

    for (Map<String, Object> crate1 : Exports.ptr("crates") as List<Map<String, Object>>) {
        Map<String, Object> crate = crate1

        Location location = crate.get("location") as Location

        if (location == null) {
            continue
        }

        String id = crate.get("id") as String

        def table = category.getOrCreateTable("crate_$id")

        Events.subscribe(PlayerInteractEvent.class).handler { event ->
            if (event.getHand() != EquipmentSlot.HAND) {
                return
            }
            Player player = event.getPlayer()
            Action action = event.getAction()

            if (action == Action.LEFT_CLICK_BLOCK) {
                Location clickedLocation = event.getClickedBlock().getLocation()

                if (clickedLocation.getWorld().getName().startsWith("starcade") && location.getBlockX() == clickedLocation.getBlockX() && location.getBlockY() == clickedLocation.getBlockY() && location.getBlockZ() == clickedLocation.getBlockZ()) {
                    showCrateRewards(player, crate.get("id") as String, table)
                }
            } else if (action == Action.RIGHT_CLICK_BLOCK) {
                Location clickedLocation = event.getClickedBlock().getLocation()

                if (!(clickedLocation.getWorld().getName().startsWith("starcade") && location.getBlockX() == clickedLocation.getBlockX() && location.getBlockY() == clickedLocation.getBlockY() && location.getBlockZ() == clickedLocation.getBlockZ())) {
                    return
                }
                event.setCancelled(true)

                ItemStack item = event.getItem()

                if (item == null || item.getType() != Material.TRIPWIRE_HOOK || !CrateNewUtils.isKey(item)) {
                    Players.msg(player, "§! §> §fYou are not holding any crate key!")
                    return
                }
                if (id != FastItemUtils.getCustomTag(item, CrateNewUtils.CRATE_TYPE, ItemTagType.STRING)) {
                    Players.msg(player, "§! §> §fThe key you are holding is for a different crate!")
                    return
                }
                UUID uuid = FastItemUtils.getId(item)

                if (!CrateNewUtils.USED_KEYS.add(uuid)) {
                    player.setItemInHand(null)
                    Players.msg(player, "§! §> §fThere appears to be something wrong with this item, so it has been deleted!")
                    Starlight.plugin.getLogger().info("${player.getName()} was caught with a duped item! (${uuid})")
                    return
                }
                MySQL.getDatabase().async().execute("INSERT INTO used_keys (uuid_least, uuid_most, server_id) VALUES (?, ?, ?)", { statement ->
                    statement.setLong(1, uuid.getLeastSignificantBits())
                    statement.setLong(2, uuid.getMostSignificantBits())
                    statement.setString(3, Temple.templeId)
                })
                FastInventoryUtils.use(player)

                if (id == "monthly") {
                    openCrate(player, id, table)
                } else {
                    openNormalCrate(player, id, table)
                }
            }
        }
    }

    for (Map<String, Object> crate1 : Exports.ptr("crates") as List<Map<String, Object>>) {
        Map<String, Object> crate = crate1
        Location location = crate.get("location") as Location

        if (location == null) {
            continue
        }

        def world = Bukkit.getWorld(WorldConfig.SPAWN_WORLD_PREFIX)
        def offsetX = crate.get("titleOffsetX") as Double
        def offsetZ = crate.get("titleOffsetZ") as Double
        def newLocation = new Location(world, location.getX() + offsetX, location.getY() + 1.75, location.getZ() + offsetZ)

        HologramRegistry.get().spawn("crate_${crate.get("id") as String}", newLocation, [crate.get("title") as String], false, null)
    }
}, 1L)

Events.subscribe(BlockPlaceEvent.class, EventPriority.HIGHEST).filter(EventFilters.ignoreCancelled()).handler { event ->
    ItemStack item = event.getItemInHand()

    if (item != null && item.getType() == Material.TRIPWIRE_HOOK && CrateNewUtils.isKey(item)) {
        event.setCancelled(true)
    }
}

static ItemStack createRewardItem(Reward reward, boolean isFinal = false) {
    ItemStack stack = reward.getItemStack()

    List<String> lore = FastItemUtils.getLore(stack) ?: []

    lore.addAll([
            "",
            "§aChance: §e${reward.getWeight()}%"
    ])

    if (isFinal) {
        lore.addAll([
                "",
                "§6§l * Final Reward * "
        ])
    }

    FastItemUtils.setLore(stack, lore)
    return stack
}

static void showCrateRewards(Player player, String crate, LootTable table, int page = 1) {
    Map<String, Object> data = CrateNewUtils.getCrateData(crate)

    if (data == null) {
        return
    }
    Schedulers.async().run {
        List<Reward> rewards = table.getSortedRewards()
//        List<Map<String, Object>> finalRewards = (List<Map<String, Object>>) data.get("final_rewards")

        int originalSize = (int) Math.ceil(rewards.size() / 9.0D) * 9
        int size = originalSize

        if (size == 0) {
            Players.msg(player, "§! §> §fThis crate has no rewards!")
            return
        }

//        if (finalRewards != null) {
//            size += (int) Math.ceil(finalRewards.size() / 9.0D) * 9 + 9
//        }
        MenuBuilder builder = new MenuBuilder(size, "§8${ChatColor.stripColor(data.get("title") as String)}")

        for (int i = 0; i < rewards.size(); ++i) {
            builder.set(i, createRewardItem(rewards.get(i)), { p, t, s -> })
        }
//        if (finalRewards != null) {
//            for (int i = originalSize + 9; i < originalSize + 9 + finalRewards.size(); ++i) {
//                builder.set(i, createRewardItem(finalRewards.get(i - originalSize - 9), true), { p, t, s -> })
//            }
//        }
        MenuUtils.syncOpen(player, builder)
    }
}

static void openCrate(Player player, String crateId, LootTable table) {
    Map<String, Object> data = CrateNewUtils.getCrateData(crateId)

    List<Reward> items = table.getRewards().shuffled()

    issueReward(player, RandomUtils.getRandom(items))
//    if (data.get("final_rewards") == null) {
//        return
//    }
//    Map<Map.Entry<ItemStack, Map<String, Object>>, Integer> finalRewards = new HashMap<>()
//
//    for (Map<String, Object> reward : data.get("final_rewards") as List<Map<String, Object>>) {
//        finalRewards.put(Pair.of(createRewardItem(reward), reward), reward.get("chance") as Integer)
//    }
//    issueReward(player, RandomUtils.getRandom(items))
}

static void openNormalCrate(Player player, String crateId, LootTable table) {
    Map<String, Object> data = CrateNewUtils.getCrateData(crateId)

    int iterations = 25

    List<Reward> items = table.getRewards().shuffled()

    Inventory inventory = Bukkit.createInventory(null, 27, "§8${ChatColor.stripColor(data.get("title") as String)}")
    updateInventory(inventory, items, 0)

    showRollingInventory(player, inventory, items, iterations)
}

static void updateInventory(Inventory inventory, List<Reward> items, int iteration) {
    if (iteration == 0) {
        ItemStack cyan = FastItemUtils.createItem(Material.CYAN_STAINED_GLASS_PANE, "§0", [])

        for (int i = 0; i < 9; ++i) {
            inventory.setItem(i, cyan)
        }
        for (int i = 18; i < 27; ++i) {
            inventory.setItem(i, cyan)
        }
        ItemStack yellow = FastItemUtils.createItem(Material.YELLOW_STAINED_GLASS_PANE, "§0", [])
        inventory.setItem(4, yellow)
        inventory.setItem(22, yellow)
    }
    for (int i = 9; i < 18; ++i) {
        inventory.setItem(i, items.get((i - 9 + iteration) % items.size()).getItemStack())
    }
}

static void showRollingInventory(Player player, Inventory inventory, List<Reward> items, int iterations) {
    player.openInventory(inventory)

    Reward reward = items.get((3 + iterations) % items.size())

    Task task = Schedulers.sync().runRepeating({}, 5, 5)

    for (int i = 0; i < iterations; ++i) {
        int iteration = i + 1

        Schedulers.sync().runLater({
            if (task.isClosed()) {
                return
            }
            if (iteration == iterations) {
                issueReward(player, reward)
                task.stop()
                return
            }
            updateInventory(inventory, items, iteration)
        }, (iteration * (1 + iteration / 10.0D)) as long)
    }
    CrateNewUtils.ROLLING_CRATES.put(player.getUniqueId(), Pair.of(task, reward))
};

Events.subscribe(InventoryCloseEvent.class).handler { event ->
    Player player = event.getPlayer() as Player

    Map.Entry<Task, Reward> rolling = CrateNewUtils.ROLLING_CRATES.remove(player.getUniqueId())

    if (rolling == null || rolling.getKey().isClosed()) {
        return
    }
    rolling.getKey().stop()
    issueReward(player, rolling.getValue())
}

Events.subscribe(InventoryClickEvent.class, EventPriority.HIGHEST).filter(EventFilters.ignoreCancelled()).handler { event ->
    Player player = event.getWhoClicked() as Player

    if (CrateNewUtils.ROLLING_CRATES.get(player.getUniqueId()) != null) {
        event.setCancelled(true)
    }
}

static void issueReward(Player player, Reward reward) {
//    String message
    reward.giveReward(player, "§9§lCRATES §> §fYou have won from this crate!")
//    Players.msg(player, "§9§lCRATES §> §fYou have won ${reward.get("title")}§f from this crate!")
}

Commands.create().assertPermission("commands.givecrate").assertUsage("<player> <crate> <amount>").handler { command ->
    DatabaseUtils.getId(command.rawArg(0), { uuid, username, player ->
        if (uuid == null) {
            command.reply("§! §> §e${username} §fhas never joined the server before!")
            return
        }
        String crate = command.rawArg(1).toLowerCase()
        Map<String, Object> data = CrateNewUtils.getCrateData(crate)

        if (data == null) {
            command.reply("§! §> §fNo crate with the name §e${command.rawArg(1)} §fexists!")
            return
        }
        IntegerUtils.IntegerParseResult result = IntegerUtils.parseInt(command.rawArg(2))

        if (!result.isPositive()) {
            command.reply("§! §> §e${command.rawArg(2)} §fis not a valid amount!")
            return
        }
        int amount = result.getValue()

        for (int i = 0; i < amount; ++i) {
            FastInventoryUtils.addOrBox(uuid, player, command.sender(), CrateNewUtils.createKey(crate), "§9§lCRATES §> §fYou have received a ${data.get("title")} §fkey!")
        }
        command.reply("§9§lCRATES §> §fSuccessfully gave §e${username} ${amount} ${data.get("title")} §fkey(s)!")
    })
}.register("givecrates", "givecrate", "givecratekeys", "givecratekey")

Commands.create().assertPermission("commands.keyall").assertUsage("<crate>").handler { command ->
    String crate = command.rawArg(0).toLowerCase()
    Map<String, Object> data = CrateNewUtils.getCrateData(crate)

    if (data == null) {
        command.reply("§! §> §fNo crate with the name §e${command.rawArg(0)} §fexists!")
        return
    }
    Set<String> given = new HashSet<>()

    for (Player player : Bukkit.getOnlinePlayers()) {
        //noinspection GrUnresolvedAccess
        if (given.add(player.getAddress().getHostString())) {
            FastInventoryUtils.addOrBox(player.getUniqueId(), player, command.sender(), CrateNewUtils.createKey(crate), "§9§lCRATES §> §fYou have received a ${data.get("title")} §fkey!")
        }
    }
    command.reply("§9§lCRATES §> §fSuccessfully gave everyone 1x §e${data.get("title")} §fkey(s)!")
}.register("keyall")

Commands.create().handler { command ->
    Bukkit.dispatchCommand(command.sender(), "warp crates")
}.register("crates", "crate")

Commands.create().assertPlayer().assertOp().handler({ c ->
    List<Map<String, Object>> crates = Exports.ptr("crates") as List<Map<String, Object>>

    for (Map<String, Object> crate1 : crates) {
        final crate = crate1
        Location location = crate.get("location") as Location

        if (location == null) {
            continue
        }

        def world = Bukkit.getWorld(WorldConfig.SPAWN_WORLD_PREFIX)
        def newLocation = new Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch())


        Starlight.log.info("On ${crate.get("id")}")

        if (newLocation.getPitch() != 69) continue
        if (newLocation.getWorld() != c.sender().getWorld()) continue

        Starlight.log.info("Spawning Model for ${crate.get("id")} in ${newLocation.getWorld().getName()}")
        ItemStack item = FastItemUtils.createItem(Material.STONE_BUTTON, "", [])
        FastItemUtils.setCustomModelData(item, crate.get("model") as int)

        Double angle = newLocation.getYaw()
        Double angleOffset = 30 + 90
        angle += angleOffset
        double x = Math.cos(Math.toRadians(angle))
        double z = Math.sin(Math.toRadians(angle))

        z *= 0.58
        x *= 0.58

        double offsetX = crate.get("titleOffsetX") as Double
        double offsetZ = crate.get("titleOffsetZ") as Double

        newLocation.setX(newLocation.getX() - x + offsetX)
        newLocation.setZ(newLocation.getZ() - z + offsetZ)
        newLocation.setY(newLocation.getY() - 0.92)

        ArmorStand eas = newLocation.getWorld().spawnEntity(newLocation, EntityType.ARMOR_STAND) as ArmorStand
        eas.setGravity(false)
        eas.setMarker(true)
        eas.setArms(true)
        eas.setInvisible(true)
        eas.setInvulnerable(true)
        eas.setRightArmPose(new EulerAngle(Math.toRadians(270), Math.toRadians(0), Math.toRadians(0)))
        eas.setRotation((angle - angleOffset).toFloat(), 0)
        eas.getEquipment().setItem(EquipmentSlot.HAND, item)
        eas.setCustomName("crate_model_${crate.get("id")}_${newLocation.getWorld().getName()}")
        EntityGlow.addGlow(eas, ChatColor.getByChar((crate.get("title") as String).charAt(1)))
    }
}).register("dev/crates/spawnmodels")

CrateNewUtils.init()

class CrateNewUtils {
    static NamespacedKey CRATE_TYPE = new NamespacedKey(Starlight.plugin, "crate_type")

    static boolean LOADED
    static Set<UUID> USED_KEYS
    static Map<UUID, Map.Entry<Task, Reward>> ROLLING_CRATES

    static void init() {
        LOADED = Persistent.persistentMap.contains("used_keys")
        USED_KEYS = Persistent.of("used_keys", new HashSet<UUID>()).get()
        ROLLING_CRATES = Persistent.of("rolling_crates", new HashMap<UUID, Map.Entry<Task, Reward>>()).get()

        MySQL.getDatabase().async().execute("CREATE TABLE IF NOT EXISTS used_keys (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, server_id))")

        if (LOADED) {
            return
        }
        ThreadUtils.runAsync {
            MySQL.getDatabase().sync().executeQuery("SELECT * FROM used_keys WHERE ${DatabaseUtils.getServerIdExpression()}", { statement -> }, { result ->
                while (result.next()) {
                    USED_KEYS.add(new UUID(result.getLong(2), result.getLong(1)))
                }
            })
        }
    }

    static boolean isKey(ItemStack item) {
        return FastItemUtils.hasCustomTag(item, CRATE_TYPE, ItemTagType.STRING)
    }

    static ItemStack createKey(String crate) {
        Map<String, Object> data = getCrateData(crate)

        ItemStack item = FastItemUtils.createItem(Material.TRIPWIRE_HOOK, "${data.get("title")} §f§lKey", [])
        FastItemUtils.setCustomTag(item, CRATE_TYPE, ItemTagType.STRING, crate)
        FastItemUtils.ensureUnique(item)

        return item
    }

    static Map<String, Object> getCrateData(String crateId) {
        for (Map<String, Object> crate : Exports.ptr("crates") as List<Map<String, Object>>) {
            if (crate.get("id") == crateId) {
                return crate
            }
        }
        return null
    }
}

