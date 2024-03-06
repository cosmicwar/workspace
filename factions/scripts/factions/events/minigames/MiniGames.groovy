package scripts.factions.events.minigames

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players
import scripts.shared.core.cfg.Config
import scripts.shared.core.cfg.ConfigCategory
import scripts.shared.core.cfg.utils.DBConfigUtil
import scripts.shared.core.cfg.RegularConfig
import scripts.shared.core.cfg.entries.BooleanEntry
import scripts.shared.core.cfg.entries.PositionEntry
import scripts.shared.core.cfg.entries.SREntry
import scripts.shared.core.cfg.entries.StringEntry
import scripts.factions.content.scoreboard.sidebar.Sidebar
import scripts.factions.content.scoreboard.sidebar.SidebarBuilder
import scripts.factions.content.scoreboard.sidebar.SidebarHandler
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.Factions
import scripts.shared.data.obj.Position
import scripts.shared.data.obj.SR
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.gens.VoidWorldGen17

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@CompileStatic(TypeCheckingMode.SKIP)
class MiniGames {

    Config arcadeConfig
    ConfigCategory arcadeCategory
    RegularConfig config

    MiniGames() {
        arcadeConfig = DBConfigUtil.createConfig("minigames", "§3minigames", [], Material.BLUE_STAINED_GLASS)
        arcadeCategory = arcadeConfig.getOrCreateCategory("minigames", "§3minigames", Material.BLUE_STAINED_GLASS)
        config = arcadeCategory.getOrCreateConfig("minigames", "§3minigames", Material.BLUE_STAINED_GLASS)
        config.addDefault([
                new BooleanEntry("enabled", true),
                new SREntry("global-region", new SR()),
                new SREntry("lobby-region", new SR()),
                new PositionEntry("minigames-spawn", new Position()),
                new StringEntry("hex-color", "§3")
        ])

        arcadeConfig.queueSave()

        SidebarHandler.registerSidebar(getScoreboard())

        createWorld("world-arcade")

        events()
        commands()

        GroovyScript.addUnloadHook {
            SidebarHandler.unregisterSidebar("arcade")
        }
    }

    def events() {
        Events.subscribe(BlockPlaceEvent).handler {event ->
            def player = event.getPlayer()

            if (player.gameMode == GameMode.CREATIVE && player.op) return

            if (getGlobalRegion().world != null && getGlobalRegion().contains(event.block)) {
                event.setCancelled(true)
                Players.msg(player, ColorUtil.color("§<${getHexColor()}>§lArcade §> §cYou cannot place blocks here."))
            }
        }

        Events.subscribe(BlockBreakEvent).handler {event ->
            def player = event.getPlayer()

            if (player.gameMode == GameMode.CREATIVE && player.op) return

            if (getGlobalRegion().world != null && getGlobalRegion().contains(event.block)) {
                event.setCancelled(true)
                Players.msg(player, ColorUtil.color("§<${getHexColor()}>§lArcade §> §cYou cannot break blocks here."))
            }
        }
    }

    def commands() {
        FCBuilder cmd = new FCBuilder("arcade").defaultAction {
            Players.msg(it, "§cThis command is not yet implemented.")
        }

        cmd.create("createworld").requirePermission("arcade.createworld").register {ctx ->
            if (getGlobalRegion().world != null) {
                def world = Bukkit.getWorld(getGlobalRegion().world)
                if (world != null) {
                    Players.msg(ctx.sender(), ColorUtil.color("§<${getHexColor()}>§lArcade §> §cThe world already exists."))
                    return
                }

                createWorld(getGlobalRegion().world)
                Players.msg(ctx.sender(), ColorUtil.color("§<${getHexColor()}>§lArcade §> §aThe world has been created."))
            }
        }

        cmd.create("teleport", "tp").requirePermission("arcade.teleport").register {ctx ->
            if (getArcadeSpawn().world != null) {
                def world = Bukkit.getWorld(getArcadeSpawn().world)
                if (world != null) {
                    ctx.sender().teleport(getArcadeSpawn().getLocation(world))
                }
            }
        }

        cmd.build()
    }

    def createWorld(String name) {
        if (Bukkit.getWorld(name) != null) return

        def world = new WorldCreator(name)
                .environment(World.Environment.NORMAL)
                .generator(new VoidWorldGen17())
                .createWorld()

        world.setDifficulty(Difficulty.NORMAL)

        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        world.setGameRule(GameRule.MOB_GRIEFING, false)
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false)
        world.setGameRule(GameRule.DO_INSOMNIA, false)
        world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false)
        world.setGameRule(GameRule.DO_VINES_SPREAD, false)
        world.setGameRule(GameRule.FALL_DAMAGE, true)
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0)

        world.setTime(14000L)

        def nmsWorld = ((CraftWorld) world).getHandle()

        nmsWorld.paperConfig().tickRates.mobSpawner = 2
        nmsWorld.paperConfig().tickRates.containerUpdate = 4
        nmsWorld.paperConfig().collisions.maxEntityCollisions = 0
        nmsWorld.paperConfig().entities.armorStands.tick = false
        nmsWorld.paperConfig().entities.armorStands.doCollisionEntityLookups = false
        nmsWorld.paperConfig().chunks.preventMovingIntoUnloadedChunks = true

        nmsWorld.spigotConfig.playerTrackingRange = 48
        nmsWorld.spigotConfig.animalTrackingRange = 48
        nmsWorld.spigotConfig.monsterTrackingRange = 64
        nmsWorld.spigotConfig.otherTrackingRange = 48
        nmsWorld.spigotConfig.miscTrackingRange = 48

        nmsWorld.spigotConfig.monsterActivationRange = 16
        nmsWorld.spigotConfig.animalActivationRange = 16
        nmsWorld.spigotConfig.miscActivationRange = 8
        nmsWorld.spigotConfig.itemMerge = 8
        nmsWorld.spigotConfig.expMerge = 8
        nmsWorld.spigotConfig.itemDespawnRate = 20 * 5 * 60
        nmsWorld.spigotConfig.arrowDespawnRate = 20 * 10
        nmsWorld.spigotConfig.hopperTransfer = 10
        nmsWorld.spigotConfig.hopperCheck = 10
        nmsWorld.spigotConfig.hopperAmount = 64

        nmsWorld.spigotConfig.cactusModifier = 10
        nmsWorld.spigotConfig.melonModifier = 10
        nmsWorld.spigotConfig.mushroomModifier = 10
        nmsWorld.spigotConfig.caneModifier = 10
        nmsWorld.spigotConfig.pumpkinModifier = 10
        nmsWorld.spigotConfig.saplingModifier = 10
        nmsWorld.spigotConfig.wartModifier = 10
        nmsWorld.spigotConfig.wheatModifier = 10
    }

    boolean isEnabled() { return config.getBooleanEntry("enabled").getValue() }
    SR getGlobalRegion() { return config.getSREntry("global-region").getValue() }
    SR getLobbyRegion() { return config.getSREntry("lobby-region").getValue() }
    Position getArcadeSpawn() { return config.getPositionEntry("arcade-spawn").getValue() }
    String getHexColor() { return config.getStringEntry("hex-color").getValue() }

    static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd")

    Sidebar getScoreboard() {
        def board = new SidebarBuilder("arcade").lines { player ->
            def member = Factions.getMember(player.getUniqueId())
            if (member == null) {
                return []
            }

            def list = []

            list.add("")

            return list
        }.title {
            def now = LocalDate.ofInstant(Instant.now(), TimeZone.getTimeZone(ZoneId.of("America/New_York")).toZoneId())
            return ColorUtil.color("§<${getHexColor()}>Arcade §7| §<${getHexColor()}>${dtf.format(now)}")
        }.priority {
            return 3
        }.shouldDisplayTo { player ->
            return getGlobalRegion().world && getGlobalRegion().world == player.getWorld().name && getGlobalRegion().contains(player.getLocation())
        }.build()

        return board
    }

}
