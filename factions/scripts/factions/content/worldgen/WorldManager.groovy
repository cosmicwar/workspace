package scripts.factions.content.worldgen

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.TreeType
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.generator.ChunkGenerator
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import scripts.shared.core.cfg.utils.SelectionUtils
import scripts.factions.content.worldgen.api.WorldMaterial
import scripts.factions.content.worldgen.api.WorldTree
import scripts.factions.content.worldgen.schem.Schematic
import scripts.shared.legacy.objects.ChunkLocation
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.Persistent

import java.util.concurrent.atomic.AtomicInteger

@CompileStatic(TypeCheckingMode.SKIP)
class WorldManager {

    List<World> worlds = []

    WorldManager() {
        Commands.create().assertPlayer().assertOp().handler {ctx ->
            openMenu(ctx.sender())
        }.register("sw", "sworlds")

        Commands.create().assertPlayer().assertOp().handler {ctx ->
            if (ctx.args().size() == 0) {
                ctx.reply("§cUsage: /paste <schematicUrl>")
            } else {
                def url = ctx.arg(0).parseOrFail(String)

                Schedulers.sync().execute {
                    def schematic = Schematic.load(url)
                    if (schematic != null) {
                        ctx.reply("§aSchematic loaded, pasting...")
                        schematic.paste(ctx.sender().getLocation()).thenApply {
                            ctx.reply("§aSchematic pasted.")
                        }
                    } else {
                        ctx.reply("§cFailed to load schematic.")
                    }
                }
            }
        }.register("dev/schem/paste")

        Commands.create().assertPlayer().assertOp().handler {ctx ->
            if (ctx.args().size() == 0) {
                ctx.reply("§cUsage: /paste <schem_file_name>")
            } else {
                def fileName = ctx.arg(0).parseOrFail(String)
                if (!fileName.endsWith(".schem")) {
                    fileName += ".schem"
                }

                Schedulers.sync().execute {
                    def file = new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/${fileName}")

                    def schematic = Schematic.load(file)
                    if (schematic != null) {
                        ctx.reply("§aSchematic loaded, pasting...")
                        schematic.paste(ctx.sender().getLocation()).thenApply {
                            ctx.reply("§aSchematic pasted.")
                        }
                    } else {
                        ctx.reply("§cFailed to load schematic.")
                    }
                }
            }
        }.register("dev/schem/pastetest")

        Commands.create().assertPlayer().assertOp().handler {ctx ->
            Schedulers.sync().execute {
                def schem = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/blue_shroom_tree.schem"))
                if (schem != null) {
                    ctx.reply("§aSchematic loaded, pasting...")
                    schem.paste(ctx.sender().getLocation()).thenApply {
                        ctx.reply("§aSchematic pasted.")
                    }
                } else {
                    ctx.reply("§cFailed to load schematic.")
                }
            }
        }.register("dev/schem/paste1test")

        Commands.create().assertPlayer().assertOp().handler {ctx ->
            Schedulers.sync().execute {
                def schem = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/alien_pillar_purple.schem"))
                if (schem != null) {
                    ctx.reply("§aSchematic loaded, pasting...")
                    schem.paste(ctx.sender().getLocation()).thenApply {
                        ctx.reply("§aSchematic pasted.")
                    }
                } else {
                    ctx.reply("§cFailed to load schematic.")
                }
            }
        }.register("dev/schem/paste2test")

        Bukkit.getWorlds().forEach { world ->
            worlds.add(world)
        }

        Events.subscribe(WorldLoadEvent.class).handler {event ->
            worlds.add(event.getWorld())
        }

        Events.subscribe(WorldUnloadEvent.class).handler {
            worlds.remove(it.getWorld())
        }
    }

    def openMenu(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§3worlds", worlds, {World world, Integer slot ->
            def item = FastItemUtils.createItem(Material.GRASS_BLOCK, world.name, [
                    "§ePlayers: ${world.players.size()}",
                    "§eEntities: ${world.entities.size()}",
                    "§eChunks: ${world.loadedChunks.size()}",
                    "§eAuto-Save: ${world.isAutoSave()}",
                    "",
                    "§3Left-Click to edit.",
                    "§3Right-Click to unload world."
            ])

            return item
        }, page, false, [
                { Player p, ClickType t, Integer s ->
                    def item = menu.get().getItem(s)

                    if (item == null || item.type != Material.GRASS_BLOCK) return

                    def world = worlds[s]
                    if (world == null) return

                    if (t == ClickType.LEFT || t == ClickType.SHIFT_LEFT) {
                        p.closeInventory()

                        if (world.getSpawnLocation() != null) {
                            p.teleport(world.getSpawnLocation())
                        } else {
                            p.teleport(new Location(world, 0, 64, 0))
                        }
                    } else if (t == ClickType.RIGHT || t == ClickType.SHIFT_RIGHT) {
                        worlds.remove(world)

                        if (world.players.size() > 0) {
                            def targetWorld = Bukkit.getWorlds().find { it != world }
                            if (targetWorld == null) {
                                p.sendMessage("§cNo other worlds to send players to.")
                                return
                            }

                            world.players.forEach { target ->
                                if (targetWorld.getSpawnLocation() != null)
                                    target.teleport(targetWorld.getSpawnLocation())
                                else
                                    target.teleport(new Location(targetWorld, 0, 64, 0))
                            }
                        }

                        Bukkit.unloadWorld(world, true)
                        openMenu(p, page)
                    }
                }
        ])

        menu.set(menu.get().size - 4, FastItemUtils.createItem(Material.BARRIER, "§aCreate World", [
                "§aClick to create a new world."
        ]), { Player p, ClickType t, Integer s ->
            SelectionUtils.selectString(p, "§aEnter the name of the world to create.", { name ->
                if (Bukkit.getWorlds().find { it.name == name } != null) {
                    p.sendMessage("§cA world with that name already exists.")
                    return
                }

                def topGroundMaterials = [
                        new WorldMaterial(8, Material.GRASS_BLOCK),
                        new WorldMaterial(1, Material.GRAVEL),
                        new WorldMaterial(1, Material.WARPED_NYLIUM),
                        new WorldMaterial(1, Material.BLUE_TERRACOTTA),
                ]

                def topLayerMaterials  = [
                        new WorldMaterial(8, Material.DIRT),
                        new WorldMaterial(1, Material.GRAVEL),
                        new WorldMaterial(1, Material.PURPLE_TERRACOTTA),
                        new WorldMaterial(1, Material.BLUE_TERRACOTTA),
                ]

                def stoneLayer = [
                        new WorldMaterial(4, Material.STONE),
                        new WorldMaterial(2, Material.COBBLESTONE),
                        new WorldMaterial(1, Material.ANDESITE),
                        new WorldMaterial(1, Material.DIORITE),
                        new WorldMaterial(1, Material.GRANITE),
                ]

                def bottomLayer = [
                        new WorldMaterial(4, Material.DEEPSLATE),
                        new WorldMaterial(2, Material.COBBLED_DEEPSLATE),
                        new WorldMaterial(1, Material.ANDESITE),
                        new WorldMaterial(1, Material.DIORITE),
                ]

                def trees = [
                        new WorldTree(1, TreeType.WARPED_FUNGUS)
                ]

                Schedulers.sync().execute {
                    def world = createWorld(name, new StarcadeWorldGen(32, -64, true, true, 45, 10, 1, 60)
                            .setTopGroundMaterials(topGroundMaterials)
                            .setTopLayerMaterials(topLayerMaterials)
                            .setStoneLayerMaterials(stoneLayer)
                            .setTreeTypes(trees)
                    )

                    if (world != null) {
                        worlds.add(world)
                        p.teleport(new Location(world, 0, 64, 0))
                        p.sendMessage("§aWorld created.")
                    } else {
                        p.sendMessage("§cFailed to create world.")
                    }
                }
            })
        })

        menu.openSync(player)
    }

    static def createWorld(String name, ChunkGenerator gen) {
        def world = new WorldCreator(name)
                .generator(gen)
                .generateStructures(false)
                .createWorld()

        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.DO_INSOMNIA, false)
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)

        world.time = 14000L
        world.setStorm(false)
        world.setThundering(false)

        return world
    }

    static boolean LOADED = false

    static int CHUNKS_CONCURRENT_LOADING_MAX = 512
    static int CHUNKS_PROCESSED_PER_TICK = 4

    static AtomicInteger CHUNKS_LOADING

    public static Queue<ChunkLocation> CHUNK_LOAD_QUEUE

    static void init() {
        if (LOADED) {
            return
        }
        CHUNKS_LOADING = Persistent.of("chunks_loading", new AtomicInteger(0)).get()

        CHUNK_LOAD_QUEUE = Persistent.of("chunks_load_queue", new ArrayDeque<ChunkLocation>()).get()

        (Persistent.persistentMap.get("chunk_manager_task") as Task)?.stop()
        (Persistent.persistentMap.get("chunk_manager_reporter_task") as Task)?.stop()

        Task task = Schedulers.sync().runRepeating({
            for (int i = CHUNKS_LOADING.get(); i < CHUNKS_CONCURRENT_LOADING_MAX; ++i) {
                ChunkLocation cl = CHUNK_LOAD_QUEUE.poll()

                if (cl == null || cl.world == null) {
                    continue
                }
                CHUNKS_LOADING.incrementAndGet()

                ServerLevel world = (cl.world as CraftWorld).getHandle()
                LevelChunk chunk = new LevelChunk(world, new ChunkPos(cl.getX(), cl.getZ()))

                ((ServerLevel) chunk.level).getChunkSource().addLoadedChunk(chunk)
                chunk.mustNotSave = false
                chunk.setUnsaved(true)
//                chunkTask.consumer.accept(chunk.bukkitChunk)
                ((ServerLevel) chunk.level).getChunkSource().chunkMap.save(chunk)

                CHUNKS_LOADING.decrementAndGet()

//                if (chunkTask.onFinish != null) {
//                    chunkTask.onFinish.run()
//                }
            }
        }, 0, 1)

        Task reporterTask = Schedulers.sync().runRepeating({
            if (!CHUNK_LOAD_QUEUE.isEmpty() || CHUNKS_LOADING.get() != 0) {
                println "CHUNK LOAD QUEUE: ${NumberUtils.format(CHUNK_LOAD_QUEUE.size())} Chunks (${NumberUtils.format(CHUNKS_LOADING.get())} loading...)"
            }
        }, 20, 20)

        Persistent.set("chunk_manager_reporter_task", reporterTask)
        Persistent.set("chunk_manager_task", task)

        LOADED = true
    }

}
