package scripts.factions.events.outpost

import be.maximvdw.placeholderapi.PlaceholderAPI
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import org.starcade.starlight.helper.utils.Players
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import scripts.exec.Globals
import scripts.shared.legacy.ToggleUtils
import scripts.shared.legacy.command.SubCommand
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.database.mysql.AsyncDatabase
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.ActionableItem
import scripts.shared.utils.MenuDecorator
import scripts.shared.utils.Persistent
import scripts.shared.utils.Temple
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.utils.StringUtils
import scripts.shared.legacy.utils.ThreadUtils

import java.util.logging.Logger

Exports.ptr("getOutpostMultiplierPlayer", { Player player, int type ->
    UUID gangId = (Exports.ptr("getGangId") as Closure<UUID>)?.call(player)
    float multiplier = 0F
    if (gangId != null) {
        multiplier += OutpostUtils.getMultiplier(gangId, OutpostType.values()[type])
    }
    return multiplier
})

Exports.ptr("getOutpostMultiplier", { UUID gangId, int type ->
    return OutpostUtils.getMultiplier(gangId, OutpostType.values()[type])
})

Schedulers.sync().run({
    OutpostUtils.init()
})

SubCommandBuilder builder = SubCommandBuilder.of("outpost", "opost").defaultAction((player) -> outpostMenu(player))

builder.create("register", "r").requirePermission("outpost.register").usage("<outpostregion> <capregion> <outposttype> <multiplier>").register { command ->
    String outpostRegion = command.rawArg(0)
    String capRegion = command.rawArg(1)
    OutpostType type = OutpostType.MONEY
    try {
        type = OutpostType.valueOf(command.rawArg(2).toUpperCase())
    } catch (IllegalArgumentException ignored) {
        command.reply("§cInvalid outpost type, defaulting to MONEY")
    }
    float multiplier = command.arg(3).parse(Float.class).get()
    Outpost outpost = new Outpost()
    outpost.outpostRegionID = outpostRegion
    outpost.capRegionID = capRegion
    outpost.outpostType = type
    outpost.cappingGang = "00000000-0000-0000-0000-000000000000"
    outpost.capped = false
    outpost.neutralizing = true
    outpost.world = Globals.PVP_WORLD.getName()
    outpost.cappingPercentage = 0
    outpost.multiplier = multiplier

    OutpostUtils.registerOutpost(outpost)
    command.reply("§2Successfully registered outpost ${outpostRegion}")
}.create("list", "l").requirePermission("outpost.list").register { command ->
    StringBuilder str = new StringBuilder("§e§lOutposts:\n")
    for (Outpost outpost : OutpostUtils.OUTPOSTS.values()) {
        if (OutpostUtils.capTasks.containsKey(outpost.outpostRegionID)) {
            str.append("§f").append(outpost.outpostRegionID).append(" §7-§6 ").append(outpost.outpostType).append(" §7- ").append("§aRunning (").append(OutpostUtils.getCapTasks().get(outpost.outpostRegionID).bukkitId).append(")").append("\n")
            continue
        }
        str.append("§f").append(outpost.outpostRegionID).append(" §7-§6 ").append(outpost.outpostType).append(" §7- ").append("§cNot running").append("\n")
    }
    command.reply(str.toString())
}.create("unregister", "unr").requirePermission("outpost.unregister").usage("<outpostregion>").register { command ->
    String outpostRegion = command.rawArg(0)

    OutpostUtils.unregisterOutpost(outpostRegion)
    command.reply("§2Successfully unregistered outpost ${outpostRegion}")
}.create("help", "?").requirePermission("outpost.help").register { command ->
    for (SubCommand subCommand : builder.subCommands) {
        if (subCommand.getPermission() == null || command.sender().hasPermission(subCommand.getPermission())) {
            SubCommandBuilder.showUsage(command, subCommand)
        }
    }
}.create("multi", "multis", "multiplier", "multipliers").usage("<username>").requirePermission("outpost.multi").register { command ->
    Player target = command.sender()

    if (command.args().size() == 1) {
        String name = command.args().get(0)
        if (Bukkit.getPlayerExact(name) == null) {
            command.reply("§c${name} is not online!")
            return
        }

        target = Bukkit.getPlayerExact(name)
    }

    Gang gang = GangUtils.getGang(target)
    if (gang == null) {
        command.reply("§cTarget is not in a gang.")
        return
    }

    StringBuilder str = new StringBuilder("§b§l").append(target.getName()).append("'s Multipliers:\n")
    for (OutpostType type : OutpostType.values()) {
        float multiplier = 0
        str.append("§7").append(type.toString().toLowerCase()).append(": §a")

        for (Outpost outpost : OutpostUtils.OUTPOSTS.values()) {
            if (outpost.outpostType == type && outpost.capped && UUID.fromString(outpost.cappingGang) == gang.id) {
                multiplier += outpost.multiplier
            }
        }

        str.append(multiplier).append("\n")
    }

    command.reply(str.toString())
}.build()

Commands.create().assertConsole().handler { c ->
    c.reply("Resetting outposts...")
    if (OutpostUtils.OUTPOSTS.size() < 1) {
        c.reply("No outposts to reset!?")
        return
    }
    OutpostUtils.capTasks.clear()
    OutpostUtils.OUTPOSTS.clear()
    MySQL.getAsyncDatabase().execute("UPDATE outposts SET gang_uuid_least = 0, gang_uuid_most = 0, capping_percentage = 0, capped = 0, neutralizing = 0 WHERE ${DatabaseUtils.getServerIdExpression()};")
    c.reply("outpost have been reset. Please reboot or reload the script.")
}.register("dev/resetoutpost")

static void outpostMenu(Player player) {
    MenuBuilder builder

    builder = new MenuBuilder(5 * 9, "§b§lOutpost")

    List<ActionableItem> actionableItems = new ArrayList<>()
    Outpost etokenOutpost = OutpostUtils.OUTPOSTS.get("outpost_etoken")
    Outpost pathOutpost = OutpostUtils.OUTPOSTS.get("outpost_path")
    Outpost beaconOutpost = OutpostUtils.OUTPOSTS.get("outpost_beacon")
    String etokenGangName = "None"
    String pathGangName = "None"
    String beaconGangName = "None"
    if (etokenOutpost != null) {
        etokenGangName = (Exports.ptr("getGangName") as Closure<String>)?.call(UUID.fromString(etokenOutpost.cappingGang))
    }
    if (pathOutpost != null) {
        pathGangName = (Exports.ptr("getGangName") as Closure<String>)?.call(UUID.fromString(pathOutpost.cappingGang))
    }
    if (beaconOutpost != null) {
        beaconGangName = (Exports.ptr("getGangName") as Closure<String>)?.call(UUID.fromString(beaconOutpost.cappingGang))
    }
    actionableItems.add(new ActionableItem(FastItemUtils.createItem(Material.BOOK, "§a§lOUTPOST INFO", [
            "",
            "§a§lWhat is outpost?",
            "While your gang is holding an outpost",
            "you will get multipliers for the specific",
            "outpost you have captured.",
            "",
            "§a§lHow to claim the outpost?",
            "You can claim the outpost by standing on",
            "the outpost in the pvp zone.",
            "You can go to the pvp zone by typing §e/warp pvp"
    ]), { p, t, s -> }))
    actionableItems.add(new ActionableItem(FastItemUtils.createItem(Material.MAGMA_CREAM, "§b§lCredit Outpost", [
            "",
            "§b§lStatus:",
            "Capped: §b${etokenOutpost == null ? "x" : etokenOutpost.capped ? etokenGangName : "None"}",
            "Status: §b${etokenOutpost == null ? "x" : etokenOutpost.cappingPercentage.toString()}%",
            "Multiplier: §b${etokenOutpost == null ? "x" : etokenOutpost.multiplier.toString()}",
    ])))
    actionableItems.add(new ActionableItem(FastItemUtils.createItem(Material.EXPERIENCE_BOTTLE, "§e§l${Globals.CITIES ? "City" : "Path"} Outpost", [
            "",
            "§e§lStatus:",
            "Capped: §e${pathOutpost == null ? "x" : pathOutpost.capped ? pathGangName : "None"}",
            "Status: §e${pathOutpost == null ? "x" : pathOutpost.cappingPercentage.toString()}%",
            "Multiplier: §e${pathOutpost == null ? "x" : pathOutpost.multiplier.toString()}",
    ])))
    actionableItems.add(new ActionableItem(FastItemUtils.createItem(Material.BEACON, "§6§lBeacon Outpost", [
            "",
            "§6§lStatus:",
            "Capped: §6${beaconOutpost == null ? "x" : beaconOutpost.capped ? beaconGangName : "None"}",
            "Status: §6${beaconOutpost == null ? "x" : beaconOutpost.cappingPercentage.toString()}%",
            "Multiplier: §6${beaconOutpost == null ? "x" : beaconOutpost.multiplier.toString()}",
    ])))
    MenuDecorator.decorate(builder, [
            "888888888",
            "8888-8888",
            "888888888",
            "88-8-8-88",
            "888888888",
    ], actionableItems.toArray() as ActionableItem[])
    builder.open(player)
}

class OutpostUtils {
    static boolean LOADED
    static Map<String, Outpost> OUTPOSTS
    static Map<String, Task> capTasks

    static void init() {
        LOADED = Persistent.persistentMap.containsKey("outposts")
        OUTPOSTS = Persistent.of("outposts", new HashMap<String, Outpost>()).get()
        capTasks = Persistent.of("cap_tasks", new HashMap<String, Task>()).get()

        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS outposts (outpost_region VARCHAR(32) NOT NULL, cap_region VARCHAR(32) NOT NULL, gang_uuid_least BIGINT NOT NULL, gang_uuid_most BIGINT NOT NULL, capping_percentage INTEGER NOT NULL, capped BOOLEAN NOT NULL, neutralizing BOOLEAN NOT NULL, world VARCHAR(16) NOT NULL, type VARCHAR(16) NOT NULL, multiplier FLOAT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(outpost_region, server_id))")

        OUTPOSTS.clear()

        ThreadUtils.runAsync {
            Logger logger = Starlight.plugin.getLogger()
            logger.info("Loading outposts...")

            AsyncDatabase database = MySQL.getSyncDatabase()
            database.executeQuery("SELECT * FROM outposts WHERE ${DatabaseUtils.getServerIdExpression()}", { statement ->
            }, { result ->
                while (result.next()) {
                    String outpostRegion = result.getString(1)
                    String capRegion = result.getString(2)
                    UUID cappingGangUUID = new UUID(result.getLong(4), result.getLong(3))
                    int cappingPercentage = result.getInt(5)
                    boolean capped = result.getBoolean(6)
                    boolean neutralizing = result.getBoolean(7)
                    String world = result.getString(8)
                    OutpostType type = OutpostType.valueOf(result.getString(9))
                    float multiplier = result.getFloat(10)

                    Outpost outpost = new Outpost()
                    outpost.outpostRegionID = outpostRegion
                    outpost.capRegionID = capRegion
                    outpost.cappingGang = cappingGangUUID.toString()
                    outpost.cappingPercentage = cappingPercentage
                    outpost.capped = capped
                    outpost.neutralizing = neutralizing
                    outpost.world = world
                    outpost.outpostType = type
                    outpost.multiplier = multiplier

                    OUTPOSTS.put(outpostRegion, outpost)
                }
            })

            try {
                Schedulers.sync().run({
                    for (Task capTask : capTasks.values()) {
                        capTask.stop()
                    }
                    capTasks.clear()

                    for (Outpost outpost : OUTPOSTS.values()) {
                        CapTask capTask = new CapTask(outpost)
                        capTasks.put(outpost.outpostRegionID, Schedulers.async().runRepeating(capTask, 120, 20))
                    }

                    // the placeholders dont take effect if you dont reload holographicdisplays
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "hd reload")
                })
                logger.info("Loaded outposts!")
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }
    }

    static void registerOutpost(Outpost outpost) {
        capTasks.remove(outpost.outpostRegionID)?.stop()
        OUTPOSTS.put(outpost.outpostRegionID, outpost)
        MySQL.getAsyncDatabase().execute("INSERT INTO outposts (outpost_region, cap_region, gang_uuid_least, gang_uuid_most, capping_percentage, capped, neutralizing, world, type, multiplier, server_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE cap_region = VALUES(cap_region), gang_uuid_least = VALUES(gang_uuid_least), gang_uuid_most = VALUES(gang_uuid_most), capping_percentage = VALUES(capping_percentage), capped = VALUES(capped), neutralizing = VALUES(neutralizing), world = VALUES(world), type = VALUES(type), multiplier = VALUES(multiplier)", { statement ->
            statement.setString(1, outpost.outpostRegionID)
            statement.setString(2, outpost.capRegionID)
            UUID capUUID = UUID.fromString(outpost.cappingGang)
            statement.setLong(3, capUUID.leastSignificantBits)
            statement.setLong(4, capUUID.mostSignificantBits)
            statement.setInt(5, outpost.cappingPercentage)
            statement.setBoolean(6, outpost.capped)
            statement.setBoolean(7, outpost.neutralizing)
            statement.setString(8, outpost.world)
            statement.setString(9, outpost.outpostType.toString())
            statement.setFloat(10, outpost.multiplier)
            statement.setString(11, Temple.templeId)
        })
        CapTask capTask = new CapTask(outpost)
        capTasks.put(outpost.outpostRegionID, Schedulers.async().runRepeating(capTask, 120, 20))
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "hd reload")
    }

    static void unregisterOutpost(String name) {
        if (!OUTPOSTS.containsKey(name)) return
        OUTPOSTS.remove(name)
        MySQL.getAsyncDatabase().execute("DELETE FROM outposts WHERE ${DatabaseUtils.getServerIdExpression()} AND outpost_region = \"${name}\"")
        capTasks.remove(name)?.stop()
    }

    static float getMultiplier(UUID gangId, OutpostType type) {
        float multiplier = 0
        for (Outpost outpost : OUTPOSTS.values()) {
            if (outpost.outpostType == type && outpost.capped && UUID.fromString(outpost.cappingGang) == gangId) {
                multiplier += outpost.multiplier
            }
        }
        return multiplier
    }

}

enum OutpostType {
    MONEY,
    PATH,
    CREDIT,
    BEACON
}

class Outpost {
    public String outpostRegionID

    public String capRegionID
    public String cappingGang
    public int cappingPercentage
    public boolean capped
    public boolean neutralizing
    public String world
    public OutpostType outpostType
    public float multiplier
}

class CapTask implements Runnable {
    private String cappingString = "§9§l<§c\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588§9§l>"
    private ProtectedRegion capRegion
    private ProtectedRegion outpostRegion

    private Outpost outpost
    private String outpostName

    CapTask(Outpost outpost) {
        this.outpost = outpost
        updateCapText()
        this.capRegion = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld(outpost.world))).getRegion(outpost.capRegionID)
        this.outpostRegion = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld(outpost.world))).getRegion(outpost.outpostRegionID)

        this.outpostName = outpost.outpostRegionID.replace("outpost_", "")
        if (outpostName == "path" && Globals.CITIES) {
            this.outpostName = "city"
        }

        Plugin plugin = Starlight.plugin

        PlaceholderAPI.registerPlaceholder(plugin, "capture_progress_${outpostName}", { event ->
            if (!event.isOnline()) {
                return "§cError..."
            }
            return cappingString
        })
        PlaceholderAPI.registerPlaceholder(plugin, "gang_hold_${outpostName}", { event ->
            if (!event.isOnline()) {
                return "§cError..."
            }
            if (!outpost.capped || outpost.cappingGang == "00000000-0000-0000-0000-000000000000") {
                return "None"
            } else {
                String symbol = (Exports.ptr("getGangSymbol") as Closure<String>).call(UUID.fromString(outpost.cappingGang))
                String name = (Exports.ptr("getGangName") as Closure<String>).call(UUID.fromString(outpost.cappingGang))
                return symbol == null ? (name == null ? "None" : name) : symbol
            }
        })

        try {
            Logger logger = plugin.getLogger()

            Schedulers.sync().run({
                HolographicDisplaysAPI api = HolographicDisplaysAPI.get(plugin)
                api.unregisterPlaceholder("capture_progress_" + outpostName)
                api.unregisterPlaceholder("gang_hold_" + outpostName)

                api.registerGlobalPlaceholder("capture_progress_" + outpostName, 1, { String s ->
                    return cappingString
                })
                api.registerGlobalPlaceholder("gang_hold_" + outpostName, 1, { String s ->
                    if (!outpost.capped || outpost.cappingGang == "00000000-0000-0000-0000-000000000000") {
                        return "None"
                    } else {
                        String symbol = (Exports.ptr("getGangSymbol") as Closure<String>).call(UUID.fromString(outpost.cappingGang))
                        String name = (Exports.ptr("getGangName") as Closure<String>).call(UUID.fromString(outpost.cappingGang))
                        return symbol == null ? (name == null ? "None" : name) : symbol
                    }
                })

                logger.info("Registered hologram placeholder: capture_progress_${outpostName}")
                logger.info("Registered hologram placeholder: gang_hold_${outpostName}")
            })
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    void run() {
        // There's no gang disband event, doing this as a workaround.
        if (outpost.cappingGang != "00000000-0000-0000-0000-000000000000" && (Exports.ptr("getGangName") as Closure<String>).call(UUID.fromString(outpost.cappingGang)) == null) {
            uncap(outpost)
        }

        Set<UUID> gangsAtOutpost = new HashSet<>()

        for (Player player : Bukkit.getWorld(outpost.world).getPlayers()) {
            if ((Exports.ptr("vanish:isVanished") as Closure<Boolean>)?.call(player)) continue
            UUID gangId = (Exports.ptr("getGangId") as Closure<UUID>).call(player)
            if (!isIn(player.getLocation()) || gangId == null) {
                continue
            }
            gangsAtOutpost.add(gangId)
        }

        if (gangsAtOutpost.size() != 1) {
            return
        }

        if (outpost.capped) {
            if (gangsAtOutpost.contains(UUID.fromString(outpost.cappingGang))) {
                if (outpost.cappingPercentage >= 100) {
                    return
                }

                setCappingPercentage(outpost.cappingPercentage + 1)
                changeColor("GREEN")
                return
            }

            // members of capped gang arent there, begin uncapping
            setCappingPercentage(outpost.cappingPercentage - 1)
            changeColor("RED")

            if (outpost.cappingPercentage <= 0) {
                String gangName = (Exports.ptr("getGangName") as Closure<String>).call(UUID.fromString(outpost.cappingGang))
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (!ToggleUtils.hasToggled(target, "alert_outposts")) {
                        Players.msg(target,"§> §b§l${gangName}§6 has lost control of the §b§l${StringUtils.capitalize(outpostName)} Outpost§6! Go to §f/warp pvp §6to take it! §<")
                    }
                }
                uncap(outpost)
                return
            }

            if (outpost.cappingPercentage % 20 == 0) {
                UUID gangId = gangsAtOutpost.first()
                String gangName = (Exports.ptr("getGangName") as Closure<String>).call(gangId)

                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerGangId = (Exports.ptr("getGangId") as Closure<UUID>).call(player)
                    if (playerGangId == null) continue
                    if (playerGangId == UUID.fromString(outpost.cappingGang)) {
                        if (!ToggleUtils.hasToggled(player, "alert_outposts")) {
                            Players.msg(player, "§6[GC] §b§l${gangName} §6is trying to capture the §b§l${StringUtils.capitalize(outpostName)} Outpost§6. You better protect it!")
                        }
                    } else if (playerGangId == gangId) {

                        if (!ToggleUtils.hasToggled(player, "alert_outposts")) {
                            Players.msg(player, "§6[GC] §6Your gang §6is capturing the §b§l${StringUtils.capitalize(outpostName)} Outpost§6, be careful!")
                        }
                    }
                }
            }
        } else {
            if (!outpost.neutralizing && gangsAtOutpost.contains(UUID.fromString(outpost.cappingGang))) {
                setCappingPercentage(outpost.cappingPercentage + 1)

                if (outpost.cappingPercentage >= 100) {
                    String gangName = (Exports.ptr("getGangName") as Closure<String>).call(UUID.fromString(outpost.cappingGang))
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (!ToggleUtils.hasToggled(target, "alert_outposts")) {
                            Players.msg(target, "§> §b§l${gangName}§6 has captured the §b§l${StringUtils.capitalize(outpostName)} Outpost! §<")
                        }
                    }
                    outpost.capped = true
                    changeColor("GREEN")
                    updateDatabase()
                } else {
                    changeColor("ORANGE")
                }

                if (outpost.cappingPercentage % 20 == 0) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        UUID playerGangId = (Exports.ptr("getGangId") as Closure<UUID>).call(player)
                        if (playerGangId == null) continue
                        if (playerGangId == UUID.fromString(outpost.cappingGang)) {
                            if (!ToggleUtils.hasToggled(player, "alert_outposts")) {
                                Players.msg(player, "§6[GC] §6Your gang §6is capturing the §b§l${StringUtils.capitalize(outpostName)} Outpost§6, be careful!")
                            }
                        }
                    }
                }
            } else {
                uncap(outpost)
                UUID newCapper = gangsAtOutpost.first()
                String gangName = (Exports.ptr("getGangName") as Closure<String>).call(newCapper)
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (!ToggleUtils.hasToggled(target, "alert_outposts")) {
                        Players.msg(target,"§> §b§l${StringUtils.capitalize(outpostName)} Outpost§6 is being captured by §b§l${gangName} Gang! §6Go to §b§l/warp pvp §6to stop them! §<")
                    }
                }
                outpost.cappingGang = newCapper.toString()
                outpost.neutralizing = false
            }
        }
    }

    void uncap(Outpost outpost) {
        outpost.cappingGang = "00000000-0000-0000-0000-000000000000"
        outpost.neutralizing = true
        outpost.capped = false
        setCappingPercentage(0)
        changeColor("WHITE")
    }

    void changeColor(String color) {
        BlockVector3 max = outpostRegion.getMaximumPoint()
        BlockVector3 min = outpostRegion.getMinimumPoint()
        World world = Bukkit.getWorld(outpost.world)
        world.execute {
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        Block block = world.getBlockAt(x, y, z)
                        if (block.getType().toString().endsWith("CARPET")) {
                            block.setType(Material.valueOf(color + "_CARPET"))
                        } else if (block.getType().toString().endsWith("STAINED_GLASS")) {
                            block.setType(Material.valueOf(color + "_STAINED_GLASS"))
                        }
                    }
                }
            }
        }
    }

    void updateDatabase() {
        MySQL.getAsyncDatabase().execute("INSERT INTO outposts (outpost_region, cap_region, gang_uuid_least, gang_uuid_most, capping_percentage, capped, neutralizing, world, type, multiplier, server_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE cap_region = VALUES(cap_region), gang_uuid_least = VALUES(gang_uuid_least), gang_uuid_most = VALUES(gang_uuid_most), capping_percentage = VALUES(capping_percentage), capped = VALUES(capped), neutralizing = VALUES(neutralizing), world = VALUES(world), type = VALUES(type), multiplier = VALUES(multiplier)", { statement ->
            statement.setString(1, outpost.outpostRegionID)
            statement.setString(2, outpost.capRegionID)
            UUID capUUID = UUID.fromString(outpost.cappingGang)
            statement.setLong(3, capUUID.leastSignificantBits)
            statement.setLong(4, capUUID.mostSignificantBits)
            statement.setInt(5, outpost.cappingPercentage)
            statement.setBoolean(6, outpost.capped)
            statement.setBoolean(7, outpost.neutralizing)
            statement.setString(8, outpost.world)
            statement.setString(9, outpost.outpostType.toString())
            statement.setFloat(10, outpost.multiplier)
            statement.setString(11, Temple.templeId)
        })
    }

    void setCappingPercentage(int percentage) {
        outpost.cappingPercentage = percentage
        updateCapText()
    }


    boolean isIn(Location location) {
        if (capRegion == null) return false
        BlockVector3 max = capRegion.getMaximumPoint()
        BlockVector3 min = capRegion.getMinimumPoint()
        return location.getX() <= max.getX() && location.getX() >= min.getX() && location.getY() <= max.getY() && location.getY() >= min.getY() && location.getZ() <= max.getZ() && location.getZ() >= min.getZ()
    }

    void updateCapText() {
        StringBuilder cappingStringBuilder = new StringBuilder("§9§l<")
        int substring = (int) (outpost.cappingPercentage / 10)
        cappingStringBuilder.append("§a")
        for (int i = 0; i < substring; i++) {
            cappingStringBuilder.append("\u2588")
        }
        cappingStringBuilder.append("§c")
        for (int i = 0; i < 10 - substring; i++) {
            cappingStringBuilder.append("\u2588")
        }
        cappingStringBuilder.append("§9§l>")
        cappingString = cappingStringBuilder.toString()
    }

    String getCappingString() {
        return cappingString
    }
}