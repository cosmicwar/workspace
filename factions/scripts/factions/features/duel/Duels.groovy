package scripts.factions.features.duel

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.*
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import scripts.factions.content.dbconfig.Config
import scripts.factions.content.dbconfig.ConfigCategory
import scripts.factions.content.dbconfig.DBConfigUtil
import scripts.factions.content.dbconfig.RegularConfig
import scripts.factions.content.dbconfig.entries.BooleanEntry
import scripts.factions.content.dbconfig.entries.PositionEntry
import scripts.factions.content.dbconfig.entries.SREntry
import scripts.factions.content.dbconfig.entries.StringEntry
import scripts.factions.content.dbconfig.utils.SelectionUtils
import scripts.factions.content.worldgen.schem.Schematic
import scripts.factions.core.faction.FCBuilder
import scripts.factions.data.obj.Position
import scripts.factions.data.uuid.UUIDDataManager
import scripts.factions.features.duel.arena.Arena
import scripts.factions.features.duel.arena.ArenaType
import scripts.factions.features.duel.match.Match
import scripts.factions.features.duel.player.DuelPlayer
import scripts.factions.features.duel.snapshot.MatchSnapshot
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.DataUtils
import scripts.shared.utils.MenuDecorator
import scripts.shared.utils.gens.VoidWorldGen17

@CompileStatic(TypeCheckingMode.SKIP)
class Duels {

    static Config config
    static ConfigCategory configCategory
    static RegularConfig duelsConfig

    static Set<Match> activeMatches = Sets.newConcurrentHashSet()
    static Set<Match> matchTypes = Sets.newConcurrentHashSet()

    Duels() {
        GroovyScript.addUnloadHook {
            UUIDDataManager.getByClass(DuelPlayer).saveAll(false)
            UUIDDataManager.getByClass(Arena).saveAll(false)
        }

        config = DBConfigUtil.createConfig("duels", "§cDuels", [], Material.NETHERITE_SWORD)
        configCategory = config.getOrCreateCategory("duels", "§cDuels", Material.NETHERITE_SWORD)
        duelsConfig = configCategory.getOrCreateConfig("duels", "§cDuels", Material.NETHERITE_SWORD)

        ArenaType.values().each {
            def arenaConf = configCategory.getOrCreateConfig(it.internalName, it.displayName, it.material)

            arenaConf.addDefault([
                    new BooleanEntry("enabled", true),
                    new PositionEntry("spawnA"),
                    new PositionEntry("spawnB"),
                    new SREntry("defaultRegion"),
                    new PositionEntry("lastPasteLocation"),
                    new StringEntry("worldName", it.internalName)
            ])

            def worldName = arenaConf.getStringEntry("worldName").getValue()
            def world = new WorldCreator(worldName)
                    .environment(World.Environment.NORMAL)
                    .generator(new VoidWorldGen17())
                    .createWorld()

            world.setDifficulty(Difficulty.NORMAL)

            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false)
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

        config.queueSave()

        UUIDDataManager.register("duel_players", DuelPlayer)
        UUIDDataManager.register("duel_matches", MatchSnapshot)
        UUIDDataManager.register("arenas", Arena)

        commands()
        events()
    }

    static def commands() {
        Commands.create().assertPlayer().handler { ctx ->
            def player = getDuelPlayer(ctx.sender().getUniqueId())


        }.register("duel")

        FCBuilder cmd = new FCBuilder("duels")

        cmd.create("admin").requirePermission("duels.admin").register { ctx ->
            openAdminDuels(ctx.sender())
        }

        cmd.build()
    }

    static def events() {

    }

    static def openAdminDuels(Player player) {
        MenuBuilder menu = new MenuBuilder(45, "§eDuels §7§o(§c§oAdmin§7§o)")

        MenuDecorator.decorate(menu,
                [
                        "bebebebeb",
                        "ebebebebe",
                        "bebebebeb",
                        "ebebebebe",
                        "bebebebeb",
                ])

        menu.set(3, 3, FastItemUtils.createItem(Material.GRASS_BLOCK, "§a§lArena Editor", [
                "§a§lArenas",
                "",
                "§eClick to manage"
        ], false), { p, t, s -> openAdminArenas(p) })

        menu.set(3, 7, FastItemUtils.createItem(Material.BOOK, "§b§lMatch Editor", [
                "§b§lMatch Editor",
                "",
                "§eClick to manage"
        ], false), { p, t, s -> openAdminMatchEditor(p) })


        menu.openSync(player)
    }
    private static NamespacedKey matchKey = new NamespacedKey(Starlight.plugin, "duel_match_key")

    static def openAdminMatchEditor(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§b§lMatch Editor", getMatchSnapshots().toList(), { MatchSnapshot snapshot, Integer slot ->
            def item = FastItemUtils.createItem(Material.NETHERITE_SWORD, "", [], false)

            DataUtils.setTag(item, matchKey, PersistentDataType.STRING, snapshot.id.toString())

            return item
        }, page, true, [
                { Player p, ClickType t, Integer slot ->
                    def item = menu.get().getItem(slot)
                    if (item == null || item.type.isAir()) return

                    def matchId = UUID.fromString(DataUtils.getTag(item, matchKey, PersistentDataType.STRING))
                    if (matchId == null) return

                    def match = getMatchSnapshot(matchId)
                    if (match == null) return

                    openMatch(p, match)
                },
                { Player p, ClickType t, Integer slot ->
                    openAdminMatchEditor(p, page + 1)
                },
                { Player p, ClickType t, Integer slot ->
                    openAdminMatchEditor(p, page - 1)
                },
                { Player p, ClickType t, Integer slot ->
                    openAdminDuels(p)
                },
        ])

        menu.openSync(player)
    }

    static def openMatch(Player player, MatchSnapshot match) {
        MenuBuilder menu = new MenuBuilder(45, "§b§lMatch §7- §e${match.id}")

        MenuDecorator.decorate(menu, [
                "bebebebeb",
                "ebebebebe",
                "bebebebeb",
                "ebebebebe",
        ])

        menu.set(5, 9, FastItemUtils.createItem(Material.RED_DYE, "§c§lBack", [
                "§c§lBack",
        ], false), { p, t, s -> openAdminDuels(p) })

        menu.openSync(player)
    }

    static def openAdminArenas(Player player) {
        MenuBuilder menu = new MenuBuilder(45, "§a§lArenas §7§o(§c§oAdmin§7§o)")

        ArenaType.values().eachWithIndex { ArenaType type, int index ->

            def arena = type
            def count = getArenas().findAll { it.arenaType == arena }.size()

            menu.set(index, FastItemUtils.createItem(arena.material, "§a§l${arena.displayName}", [
                    "",
                    "§eArena's: §7${count}",
                    "",
                    "§eClick to manage"
            ], false), { p, t, s -> openArena(p, arena) })
        }

        menu.set(5, 9, FastItemUtils.createItem(Material.RED_DYE, "§c§lBack", [
                "§c§lBack",
        ], false), { p, t, s -> openAdminDuels(p) })

        menu.openSync(player)
    }

    static def openArena(Player player, ArenaType type) {
        MenuBuilder menu = new MenuBuilder(36, "§a§l${type.displayName} §c§oEditor")

        MenuDecorator.decorate(menu, [
                "bebebebeb",
                "ebebebebe",
                "bebebebeb",
                "ebebebebe",
        ])

        menu.set(1, 3, FastItemUtils.createItem(Material.GRASS_BLOCK, "§a§lGenerate", [
                "§a§lGenerate",
                "",
                "§eClick to generate"
        ], false), { p, t, s ->
            SelectionUtils.selectInteger(p, "§a§lGenerate Arenas") { amount ->
                openArena(p, type)
                def arenas = createArenas(type, amount)
                p.sendMessage("§a§lGenerated ${arenas.size()} ${type.displayName} arenas")
            }
        })

        menu.set(1, 1, FastItemUtils.createItem(type.material, "§a§lArenas", [
                "§a§lArenas",
                "",
                "§eClick to manage"
        ], false), { p, t, s -> viewArenas(p, type) })

        menu.set(4, 9, FastItemUtils.createItem(Material.RED_DYE, "§c§lBack", [
                "§c§lBack",
        ], false), { p, t, s -> openAdminDuels(p) })

        menu.openSync(player)
    }

    private static NamespacedKey arenaKey = new NamespacedKey(Starlight.plugin, "duel_arena_key")

    static def viewArenas(Player player, ArenaType type, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§a§l${type.displayName} §c§oArenas", getArenas().stream().filter { it.arenaType == type }.toList(), { Arena arena, Integer slot ->
            def item = FastItemUtils.createItem(Material.NETHERITE_SWORD, "§a§lArena §7- §e${arena.id}", [
                    "§a§lArena §7- §e${arena.id}",
                    "",
                    "§eClick to manage"
            ], false)

            DataUtils.setTag(item, arenaKey, PersistentDataType.STRING, arena.id.toString())

            return item
        }, page, true, [
                { Player p, ClickType t, Integer slot ->
                    def item = menu.get().getItem(slot)
                    if (item == null || item.type.isAir()) return

                    def arenaId = UUID.fromString(DataUtils.getTag(item, arenaKey, PersistentDataType.STRING))
                    if (arenaId == null) return

                    def arena = getArena(arenaId)
                    if (arena == null) return

                    if (arena.spawnA.world != null) {
                        p.teleport(arena.spawnA.getLocation(null))
                    } else if (arena.spawnB.world != null) {
                        p.teleport(arena.spawnB.getLocation(null))
                    }
                },
                { Player p, ClickType t, Integer slot ->
                    viewArenas(p, type, page + 1)
                },
                { Player p, ClickType t, Integer slot ->
                    viewArenas(p, type, page - 1)
                },
                { Player p, ClickType t, Integer slot ->
                    openArena(p, type)
                },
        ])

        menu.openSync(player)
    }

    static Collection<Arena> createArenas(ArenaType type, int amount = 1) {
        def arenaConf = getArenaConfig(type)

        def world = Bukkit.getWorld(arenaConf.getStringEntry("worldName").getValue())
        def origin = new Location(world, 0, 0, 0)
        if (world != null) {
            def lastPasteLocation = arenaConf.getPositionEntry("lastPasteLocation").getValue()
            if (lastPasteLocation.world != world.name) {
                lastPasteLocation.world = world.name
                config.queueSave()
            }

            if (!lastPasteLocation.x1 || !lastPasteLocation.y1 || !lastPasteLocation.z1) {
                arenaConf.getPositionEntry("lastPasteLocation").setValue(Position.of(origin))
                config.queueSave()

                lastPasteLocation = arenaConf.getPositionEntry("lastPasteLocation").getValue()
            }

            origin = lastPasteLocation.getLocation(null)
        } else {
            return []
        }


        def arenas = []

        def arenaRegion = arenaConf.getSREntry("defaultRegion").getValue()
        if (arenaRegion.world == null) return []
        arenaRegion.reorder()

        def spawnA = arenaConf.getPositionEntry("spawnA").getValue()
        if (spawnA.world == null) return []

        def spawnAXOffset = spawnA.x1 - arenaRegion.x2
        def spawnAYOffset = spawnA.y1 - arenaRegion.y2
        def spawnAZOffset = spawnA.z1 - arenaRegion.z2

        def spawnB = arenaConf.getPositionEntry("spawnB").getValue()
        if (spawnB.world == null) return []

        def spawnBXOffset = spawnB.x1 - arenaRegion.x2
        def spawnBYOffset = spawnB.y1 - arenaRegion.y2
        def spawnBZOffset = spawnB.z1 - arenaRegion.z2

        amount.times {
            def start = System.currentTimeMillis()

            def arena = UUIDDataManager.getData(UUID.randomUUID(), Arena, true)
            arena.arenaType = type

            def pasteLocation = origin.clone().add(it * 250, 0, it * 250)

            def file = new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/${type.schemName}.schem")
            def schem = Schematic.load(file)
            schem.paste(pasteLocation, false, true, true).thenApply {
                arena.pasteOrigin = Position.of(pasteLocation)
                arena.spawnA = Position.of(pasteLocation.clone().add(spawnAXOffset, spawnAYOffset, spawnAZOffset))
                arena.spawnB = Position.of(pasteLocation.clone().add(spawnBXOffset, spawnBYOffset, spawnBZOffset))

                arena.isPasted = true
                arena.changed = false
                arena.isOccupied = false

                arena.queueSave()

                arenas.add(arena)

                arenaConf.getPositionEntry("lastPasteLocation").setValue(arena.pasteOrigin)
                config.queueSave()

                Starlight.log.info("Pasted arena ${arena.id} in ${System.currentTimeMillis() - start}ms")
            }
        }

        return arenas
    }

    static DuelPlayer getDuelPlayer(UUID playerId, boolean create = true) {
        return UUIDDataManager.getData(playerId, DuelPlayer, create)
    }

    static Collection<DuelPlayer> getDuelPlayers() { return UUIDDataManager.getAllData(DuelPlayer) }

    static Arena getArena(UUID arenaId, boolean create = true) {
        return UUIDDataManager.getData(arenaId, Arena, create)
    }

    static Collection<Arena> getArenas() { return UUIDDataManager.getAllData(Arena) }

    static RegularConfig getArenaConfig(ArenaType type) { return configCategory.getOrCreateConfig(type.internalName) }

    static MatchSnapshot getMatchSnapshot(UUID matchId, boolean create = true) {
        return UUIDDataManager.getData(matchId, MatchSnapshot, create)
    }

    static Collection<MatchSnapshot> getMatchSnapshots() { return UUIDDataManager.getAllData(MatchSnapshot) }

    static synchronized Arena getAvailableArena(ArenaType type) {
        def arenas = getArenas().stream().filter { it.arenaType == type && !it.isOccupied && it.isPasted && !it.changed }.toList()

        if (arenas.isEmpty()) return null

        def arena = arenas.get(0)
        arena.isOccupied = true
        arena.queueSave()

        return arena
    }

}
