package scripts.factions.content.worldgen.schem

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.craftbukkit.v1_20_R1.CraftChunk
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.utils.Persistent

import java.util.concurrent.atomic.AtomicInteger

class SchematicManager {
    static boolean LOADED = false

    static int CHUNKS_CONCURRENT_LOADING_MAX = 512
    static int CHUNKS_PROCESSED_PER_TICK = 4

    static AtomicInteger CHUNKS_LOADING

    public static Queue<ChunkSchematicTask> CHUNK_LOAD_QUEUE

    static void init() {
        if (LOADED) {
            return
        }
        CHUNKS_LOADING = Persistent.of("chunks_loading", new AtomicInteger(0)).get()

        CHUNK_LOAD_QUEUE = Persistent.of("chunks_load_queue", new ArrayDeque<ChunkSchematicTask>()).get()

        (Persistent.persistentMap.get("chunk_manager_task") as Task)?.stop()
        (Persistent.persistentMap.get("chunk_manager_reporter_task") as Task)?.stop()

        Task task = Schedulers.sync().runRepeating({
            for (int i = CHUNKS_LOADING.get(); i < CHUNKS_CONCURRENT_LOADING_MAX; ++i) {
                ChunkSchematicTask chunkTask = CHUNK_LOAD_QUEUE.poll()

                if (chunkTask == null) {
                    continue
                }
                CHUNKS_LOADING.incrementAndGet()

                ServerLevel world = (chunkTask.location.getWorld() as CraftWorld).getHandle()
                LevelChunk chunk = new LevelChunk(world, new ChunkPos(chunkTask.location.getX(), chunkTask.location.getZ()))

                ((ServerLevel) chunk.level).getChunkSource().addLoadedChunk(chunk)

                chunk.mustNotSave = false
                chunk.setUnsaved(true)
                chunkTask.consumer.accept(new CraftChunk(chunk))

                ((ServerLevel) chunk.level).getChunkSource().chunkMap.save(chunk)

                CHUNKS_LOADING.decrementAndGet()

                if (chunkTask.onFinish != null) {
                    chunkTask.onFinish.run()
                }
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
