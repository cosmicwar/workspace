package scripts.factions.content.worldgen.schem

import org.bukkit.Chunk
import scripts.shared.legacy.objects.ChunkLocation

import java.util.function.Consumer

class ChunkSchematicTask {
    public ChunkLocation location
    public Consumer<Chunk> consumer
    public Runnable onFinish

    ChunkSchematicTask(ChunkLocation location, Consumer<Chunk> consumer, Runnable onFinish) {
        this.location = location
        this.consumer = consumer
        this.onFinish = onFinish
    }
}
