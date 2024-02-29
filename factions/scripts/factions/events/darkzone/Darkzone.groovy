package scripts.factions.events.darkzone

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players
import org.starcade.wazowski.fake.FakeEntityPlayer
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import scripts.factions.content.dbconfig.Config
import scripts.factions.content.dbconfig.ConfigCategory
import scripts.factions.content.dbconfig.RegularConfig
import scripts.factions.content.dbconfig.entries.PositionEntry
import scripts.factions.content.dbconfig.entries.StringEntry
import scripts.factions.content.dbconfig.entries.list.PositionListEntry
import scripts.factions.content.scoreboard.sidebar.Sidebar
import scripts.factions.content.scoreboard.sidebar.SidebarBuilder
import scripts.factions.content.dbconfig.DBConfigUtil
import scripts.factions.content.scoreboard.sidebar.SidebarHandler
import scripts.factions.data.obj.Position
import scripts.factions.data.uuid.UUIDDataManager
import scripts.factions.eco.loottable.LootTableHandler
import scripts.factions.eco.loottable.api.LootTableCategory
import scripts.factions.events.darkzone.user.DZMember
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.DataUtils
import scripts.shared.utils.Formats
import scripts.shared.utils.MenuDecorator

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@CompileStatic(TypeCheckingMode.SKIP)
class Darkzone {

    static Config config
    static ConfigCategory settings
    static RegularConfig values
    static String configId = "darkzone"

    static Sidebar scoreboard

    static Map<UUID, DarkzoneSpawner> spawners = [:]

    static NamespacedKey dzMobKey = new NamespacedKey(Starlight.plugin, "dzMob")
    static NamespacedKey dzSpawnerKey = new NamespacedKey(Starlight.plugin, "dzSpawner")
    static NamespacedKey placingTierKey = new NamespacedKey(Starlight.plugin, "placingTier")

    static LootTableCategory lootTableCategory

    Darkzone() {
        GroovyScript.addUnloadHook {
            SidebarHandler.unregisterSidebar(scoreboard.getInternalId())
            UUIDDataManager.getByClass(DZMember).saveAll(false)

            spawners.values().each {
                it.clearMobs()
                it.removeHologram()
            }
        }

        UUIDDataManager.register("dz_members", DZMember.class)

        config = DBConfigUtil.createConfig(configId, "§5Dark Zone", [
                "§7Welcome to the Dark Zone!",
        ], Material.ENDER_PEARL)

        settings = config.getOrCreateCategory("settings", "§5settings", Material.BOOK, [
                "§5 ~ dark zone settings ~"
        ])

        values = settings.getOrCreateConfig("values", "§5values", Material.BOOK, [
                "§5 ~ dark zone values ~"
        ])

        values.addDefault([
                new StringEntry("worldName", "darkzone"),
                new PositionEntry("spawn", new Position("darkzone", 111, 101, 124, -180, 0)),
        ])

        lootTableCategory = LootTableHandler.getLootTableCategory("darkzone")
        lootTableCategory.icon = Material.END_STONE

        DarkzoneTier.values().each {
            values.addDefault([
                    new PositionListEntry("${it.internalName}Spawners", [])
            ])

            lootTableCategory.getOrCreateTable(it.internalName)
        }

        config.queueSave()
        lootTableCategory.queueSave()

        scoreboard = new SidebarBuilder("darkzone").title { Player player ->
            def now = LocalDate.ofInstant(Instant.now(), TimeZone.getTimeZone(ZoneId.of("America/New_York")).toZoneId())
            def dtf = DateTimeFormatter.ofPattern("MM/dd")

            def title = "${ColorUtil.rainbow("DARKZONE", ["#8E13FB", "#6762C5", "#8E13FB"] as String[], "§l").toString()} §7| ${ColorUtil.color("§<#6762C5>${dtf.format(now)}")}"

            return title
        }.lines { Player player ->
            if (player == null || player instanceof FakeEntityPlayer) return []

            def dzMember = getDzMember(player, true)

            List<String> lines = []

            lines.add("§8§m${StringUtils.repeat('-', 26)}") // spacer
            lines.add("§<#6762C5>Account: §<#09FB29>${player.getName()}")
            lines.add("")
            lines.add("§<#6762C5>Session KDR: §a${dzMember.sessionPlayerKills}§7/§c${dzMember.sessionPlayerDeaths}")
            lines.add("§<#6762C5>Session XP: §<#09FB29>${Formats.formatCommas(dzMember.sessionXpEarned)} xp")
            lines.add("§<#6762C5>Session Money: §<#09FB29>\$${Formats.formatMoneyShort(dzMember.sessionMoneyEarned)}")
            lines.add("")
            lines.add("§<#6762C5>Session Mob Kills: §<#09FB29>${Formats.formatCommas(dzMember.sessionMobKills)}")
            lines.add("")
            lines.add("§<#6762C5>Level: §<#09FB29>§l${dzMember.getLevel()}")
            lines.add("")
            lines.add("§8§m${StringUtils.repeat('-', 26)}") // spacer


            return lines
        }.priority {
            return 2
        }.shouldDisplayTo{player ->
            def world = player.getWorld()
            if (world == null) return false

            return world.getName() == settings?.getOrCreateConfig("values")?.getStringEntry("worldName")?.value ?: false
        }.build()

        Schedulers.sync().runLater({
            scoreboard.registerSidebar()

            def world = Bukkit.getWorld(settings.getOrCreateConfig("values").getStringEntry("worldName").value)
            if (world == null) return

            world.setViewDistance(20)

            world.setGameRule(GameRule.DO_MOB_LOOT, true)
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
            world.setGameRule(GameRule.DISABLE_RAIDS, true)
            world.setGameRule(GameRule.DO_INSOMNIA, false)
            world.setGameRule(GameRule.DO_TILE_DROPS, false)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            world.setGameRule(GameRule.DO_ENTITY_DROPS, false)
            world.setGameRule(GameRule.DO_PATROL_SPAWNING, false)
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)

            loadSpawners()
        }, 5L)

        Schedulers.async().runRepeating({
            spawners.values().each {
                it.tick()
            }
        }, 20L, 20L)

        commands()
        events()
    }

    static DZMember getDzMember(Player player, boolean create = true) {
        return UUIDDataManager.getData(player, DZMember.class, create)
    }

    static List<Position> getTierPositions(DarkzoneTier tier) {
        return settings.getOrCreateConfig("values").getPositionListEntry("${tier.internalName}Spawners").value
    }

    static def commands() {
        SubCommandBuilder builder = new SubCommandBuilder("darkzone", "dz").defaultAction {player ->
            openDzMenu(player)
        }

        builder.create("dev/killmobs").requirePermission("sc.dev").register {ctx ->
           def count = 0
            spawners.values().each {
                count += it.clearMobs()
            }

            ctx.reply("§akilled §c$count §amobs")
        }

        builder.create("edit").requirePermission("sc.dev").register {ctx ->
            def dzMember = getDzMember(ctx.sender(), true)
            dzMember.editing = !dzMember.editing
            dzMember.queueSave()

            if (dzMember.editing) {
                DarkzoneTier.values().each {
                    def item = FastItemUtils.createItem(it.spawnerMaterial, "§5Place ${it.displayName} Spawner", ["§7Right click to place a spawner for ${it.displayName}"])

                    DataUtils.setTag(item, placingTierKey, PersistentDataType.STRING, it.toString())

                    ctx.sender().getInventory().addItem(item)
                }
            } else {
                ctx.sender().getInventory().contents.each {
                    if (it != null && DataUtils.hasTag(it, placingTierKey, PersistentDataType.STRING)) {
                        ctx.sender().getInventory().remove(it)
                    }
                }
            }

            ctx.reply("§aediting is now ${dzMember.editing ? "§aenabled" : "§cdisabled"}")
        }

        builder.create("dev/setlevel").requirePermission("sc.dev").register {ctx ->
            if (ctx.args().size() == 0) {
                ctx.reply("§c/dz dev/setlevel <level> [player]")
                return
            }

            if (ctx.args().size() == 1) {
                def dzMember = getDzMember(ctx.sender(), true)
                dzMember.level = ctx.arg(0).parseOrFail(Integer)
                dzMember.queueSave()
                ctx.reply("§aset your level to ${dzMember.level}")
                return
            }

            if (ctx.args().size() == 2) {
                def dzMember = getDzMember(ctx.arg(1).parseOrFail(Player), true)
                dzMember.level = ctx.arg(0).parseOrFail(Integer)
                dzMember.queueSave()
                ctx.reply("§aset ${dzMember.getName()}'s level to ${dzMember.level}")
                return
            }

            ctx.reply("§c/dz dev/setlevel <level> [player]")
        }

        builder.build()
    }

    static def openDzMenu(Player player) {
        MenuBuilder menu

        menu = new MenuBuilder(54, "§5Dark Zone")

        def dzMember = getDzMember(player, true)

        MenuDecorator.decorate(menu, [
                "5d5d5d5d5",
                "d5d5d5d5d",
                "5d5d5d5d5",
                "d5d5d5d5d",
                "5d5d5d5d5",
                "d5d5d5d5d",
        ])

        menu.set(2, 5, FastItemUtils.createItem(Material.END_STONE, "§5Click to Teleport", [""]), {p, t, s ->
            def position = settings.getOrCreateConfig("values").getPositionEntry("spawn").value
            if (position == null) return

            def world = Bukkit.getWorld(position.world)
            if (world == null) return

            def location = position.getLocation(world)
            if (location == null) return

            p.teleport(location.add(0.5, 0, 0.5))
        })

        def lore = []

        lore.add("§7Player Kills: §5${dzMember.playerKills}")
        lore.add("§7Player Deaths: §5${dzMember.playerDeaths}")
        lore.add("§7Mob Kills: §5${dzMember.totalMobKills}")
        lore.add("§7Mob Deaths: §5${dzMember.mobDeaths}")
        lore.add("§7Boss Kills: §5${dzMember.bossKills}")
        lore.add("§7Boss Deaths: §5${dzMember.bossDeaths}")
        lore.add("§7XP Earned: §5${dzMember.xpEarned}")
        lore.add("§7Money Earned: §5${dzMember.moneyEarned}")

        menu.set(6, 5, FastItemUtils.createItem(Material.PAPER, "§5Dark Zone Statistics", lore, false))

        menu.openSync(player)
    }

    static def events() {
        Events.subscribe(EntityDeathEvent.class).handler {event ->
            def entity = event.getEntity()
            if (entity == null) return

            if (entity.getPersistentDataContainer().has(dzSpawnerKey, PersistentDataType.STRING))
            {
                def spawnerId = UUID.fromString(entity.getPersistentDataContainer().get(dzSpawnerKey, PersistentDataType.STRING))

                def spawner = spawners.get(spawnerId)
                if (spawner == null) return

                def mob = spawner.removeMob(entity.getUniqueId())
                if (mob == null) return

                def killer = entity.getKiller()
                if (killer == null) return

                def dzMember = getDzMember(killer, true)

                dzMember.addKill(spawner.tier)
                dzMember.sessionMobKills++

                dzMember.queueSave()

                event.drops.clear()

                Players.msg(killer, "§aYou killed a ${spawner.tier.displayName} mob!")
            }
        }

        Events.subscribe(BlockPlaceEvent.class).handler {event ->
            def player = event.getPlayer()
            def dzMember = getDzMember(player, true)

            if (dzMember.isEditing()) {
                def world = event.getBlock().getWorld()

                if (world.getName() != settings.getOrCreateConfig("values").getStringEntry("worldName").value) return

                def item = event.getItemInHand()
                if (DataUtils.hasTag(item, placingTierKey, PersistentDataType.STRING)) {
                    def tier = DarkzoneTier.valueOf(DataUtils.getTagString(item, placingTierKey))

                    if (tier == null) return

                    def position = new Position(world.getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), 0, 0)
                    def positions = getTierPositions(tier)

                    positions.add(position)
                    config.queueSave()

                    def spawner = new DarkzoneSpawner(tier, position)
                    spawners.put(spawner.spawnerId, spawner)

                    Schedulers.sync().runLater({
                        spawner.ensurePlaced()
                    }, 5L)

                    player.sendMessage("§aAdded a spawner for ${tier.displayName}")
                }
            }
        }

        Events.subscribe(BlockBreakEvent.class).handler { event ->
            def player = event.getPlayer()
            def dzMember = getDzMember(player, true)

            if (dzMember.isEditing()) {
                def world = event.getBlock().getWorld()

                if (world.getName() != settings.getOrCreateConfig("values").getStringEntry("worldName").value) return

                def spawner = getSpawnerFromLoc(event.getBlock().getLocation())
                if (spawner == null) return

                spawner.clearMobs()
                spawner.removeHologram()
                spawners.remove(spawner.spawnerId)

                getTierPositions(spawner.tier).remove(spawner.position)

                config.queueSave()

                player.sendMessage("§cRemoved a spawner for ${spawner.tier.displayName}")
            }
        }
    }

    static def loadSpawners() {
        spawners.clear()

        DarkzoneTier.values().each {
            getTierPositions(it).each { position ->
                def spawner = new DarkzoneSpawner(it, position)
                spawners.put(spawner.spawnerId, spawner)

                spawner.ensurePlaced()
            }
        }
    }

    static def getSpawnerFromLoc(Location location) {
        return spawners.values().find {
            it.position.world == location.getWorld().getName() &&
                    it.position.x1 == location.getBlockX() &&
                    it.position.y1 == location.getBlockY() &&
                    it.position.z1 == location.getBlockZ()
        }
    }

}