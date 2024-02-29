package scripts.factions.content.worldgen.schem

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.level.chunk.LevelChunk
import org.apache.commons.io.FileUtils
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Schedulers

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@CompileStatic(TypeCheckingMode.SKIP)
class Schematic {

    String fileName
    BlockState[] blocks
    int[] dimensions
    int[] offset

    static Schematic load(String url) {
        def file = new File(Starlight.plugin.getDataFolder(), "schematics/${url}")

        if (!file.exists()) {
            FileUtils.copyURLToFile(new URL(url), file)
        }

        if (file != null) {
            return load(file)
        } else {
            return null
        }
    }

    static Schematic load(File file) {
        try {
            FileInputStream fis = new FileInputStream(file)

            CompoundTag compound = NbtIo.readCompressed(fis)
            byte[] blockData = compound.getByteArray("BlockData")
            BlockState[] blocks = new BlockState[blockData.length]

            CompoundTag palette = compound.getCompound("Palette")
            Map<Integer, String> types = new HashMap<>()

            boolean isReserved = false
            int reserved = -1

            for (String key : palette.getAllKeys()) {
                if (key == "minecraft:__reserved__") {
                    isReserved = true
                    reserved = palette.getInt(key)
                    continue
                }
                types.put(palette.getInt(key), key)
            }
            int index = 0
            int i = 0

            while (i < blockData.length) {
                int value = 0
                int varintLength = 0

                while (true) {
                    value |= (blockData[i] & 127) << (varintLength++ * 7)

                    if (varintLength > 5) {
                        throw new IOException("VarInt too big (probably corrupted data)")
                    }
                    if ((blockData[i] & 128) != 128) {
                        i++
                        break
                    }
                    i++
                }
                if (value != reserved || !isReserved) {
                    blocks[index] = CraftBlockData.newData(null, types.get(value)).getState()
                }
                ++index
            }
            CompoundTag metaData = compound.getCompound("Metadata")

            Schematic schematic = new Schematic()
            schematic.blocks = blocks
            schematic.dimensions = [compound.getShort("Width"), compound.getShort("Height"), compound.getShort("Length")] as int[]
            schematic.offset = [metaData.getInt("WEOffsetX"), metaData.getInt("WEOffsetY"), metaData.getInt("WEOffsetZ")] as int[]
            schematic.fileName = file.getName()

            return schematic
        } catch (Exception e) {
            println "Schematic load error: ${e.message} ${file.absolutePath}"
            //e.printStackTrace()
            return null
        }
    }

    static void loadAsync(File file, Consumer<Schematic> consumer) {
        Schedulers.async().run {
            Schematic schematic = load(file)
            Schedulers.sync().run { consumer.accept(schematic) }
        }
    }

    CompletableFuture<Void> paste(Location location, boolean pasteAir = false, boolean markDirty = true, boolean forceLoad = false) {
        return paste(location, pasteAir, markDirty, forceLoad, Collections.<Material, Material>emptyMap())
    }

    CompletableFuture<Void> paste(Location location, boolean pasteAir = false, boolean markDirty = true, boolean forceLoad = false, Map<Material, Material> blockRemappings) {
        List<CompletableFuture> chunkFutures = new ArrayList<>()

        World world = location.getWorld()
        ServerLevel level =  ((CraftWorld) world).getHandle()

        int width = this.dimensions[0]
        int height = this.dimensions[1]
        int length = this.dimensions[2]

        int xBase = location.getBlockX() + this.offset[0]
        int yBase = location.getBlockY() + this.offset[1]
        int zBase = location.getBlockZ() + this.offset[2]

        int clx = xBase >> 4
        int cgx = (xBase + width - 1) >> 4
        int clz = zBase >> 4
        int cgz = (zBase + length - 1) >> 4

        int widthChunks = cgx - clx + 1
        int lengthChunks = cgz - clz + 1

        for (int cix = 0; cix < widthChunks; ++cix) {
            for (int ciz = 0; ciz < lengthChunks; ++ciz) {
                int cx = clx + cix
                int cz = clz + ciz

                int xStart = Math.max(xBase, cx << 4)
                int xEnd = Math.min(xBase + width, ((xStart + 16) >> 4) << 4)

                int yStart = yBase
                int yEnd = yStart + height

                int zStart = Math.max(zBase, cz << 4)
                int zEnd = Math.min(zBase + length, ((zStart + 16) >> 4) << 4)

                CompletableFuture<Void> chunkFuture = new CompletableFuture<Void>()
                chunkFutures.add(chunkFuture)

                Consumer<Chunk> consumer = { Chunk bukkitChunk ->
                    LevelChunk nmsChunk = level.getChunk(bukkitChunk.getX(), bukkitChunk.getZ())

                    for (int x = xStart; x < xEnd; ++x) {
                        for (int y = yStart; y < yEnd; ++y) {
                            for (int z = zStart; z < zEnd; ++z) {
                                BlockState blockState = this.blocks[((y - yBase) * length + (z - zBase)) * width + (x - xBase)]
                                if (blockState.bukkitMaterial == Material.AIR && !pasteAir) continue

                                Material remapped = blockRemappings.get(blockState.bukkitMaterial)
                                if (remapped == Material.AIR) {
                                    continue
                                } else if (remapped != null) {
                                    BlockState remappedBlockState = CraftMagicNumbers.getBlock(remapped).defaultBlockState()
                                    blockState.getValues().each {
                                        remappedBlockState = remappedBlockState.setValue(it.getKey() as Property, it.getValue())
                                    }

                                    blockState = remappedBlockState
                                }

                                nmsChunk.setBlockState(new BlockPos(x, y, z), blockState, false, false)
                                if (nmsChunk.playerChunk != null) {
                                    nmsChunk.playerChunk.blockChanged(new BlockPos(x & 15, y, z & 15))
                                    nmsChunk.unsaved = true
                                }
                            }
                        }
                    }

                    chunkFuture.complete(null)
                }
                Schedulers.async().runLater({
                    world.getChunkAtAsync(cx, cz, true, consumer)
                }, (cix * widthChunks + ciz).intdiv(3) as long)
            }
        }

        return CompletableFuture.allOf(chunkFutures.toArray() as CompletableFuture[])
    }
}