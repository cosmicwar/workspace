package scripts.factions.content.worldgen

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.*
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.generator.*
import org.bukkit.util.noise.PerlinOctaveGenerator
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.random.RandomSelector
import scripts.factions.content.worldgen.api.WorldMaterial
import scripts.factions.content.worldgen.api.WorldTree
import scripts.factions.content.worldgen.schem.Schematic

import java.util.concurrent.ThreadLocalRandom

@CompileStatic(TypeCheckingMode.SKIP)
class StarcadeWorldGen extends ChunkGenerator {


    def purplePillar0 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/alien_pillar_purple.schem"))
    def purplePillar90 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/alien_pillar_purple_90.schem"))
    def purplePillar180 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/alien_pillar_purple_180.schem"))
    def purplePillar270 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/alien_pillar_purple_270.schem"))

    def blueTree = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/blue_tree1.schem"))
    def blueTree90 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/blue_tree1_90.schem"))
    def blueTree180 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/blue_tree1_180.schem"))
    def blueTree270 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/blue_tree1_270.schem"))

    def purpleTree = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/purple_tree_1.schem"))
    def purpleTree90 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/purple_tree_1_90.schem"))
    def purpleTree180 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/purple_tree_1_180.schem"))
    def purpleTree270 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/purple_tree_1_270.schem"))

    def blueShroomTree = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/blue_shroom_tree.schem"))
    def blueShroomTree90 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/blue_shroom_tree_90.schem"))
    def blueShroomTree180 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/blue_shroom_tree_180.schem"))
    def blueShroomTree270 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/blue_shroom_tree_270.schem"))

    def purpleShroomTree = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/purple_shroom_tree_0.schem"))
    def purpleShroomTree90 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/purple_shroom_tree_90.schem"))
    def purpleShroomTree180 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/purple_shroom_tree_180.schem"))
    def purpleShroomTree270 = Schematic.load(new File(Starlight.plugin.getDataFolder().parentFile, "FastAsyncWorldEdit/schematics/purple_shroom_tree_270.schem"))

    int maxBlockHeight
    int minBlockHeight
    int stoneLayerStart = -20
    boolean generateWater
    boolean generateOres

    double oreChance
    double treeChance
    double grassChance
    double flowerChance


    List<WorldMaterial> topGroundMaterials = [new WorldMaterial(1, Material.GRASS)]
    List<WorldMaterial> underWaterMaterials = [new WorldMaterial(1, Material.SAND)]
    List<WorldMaterial> topLayerMaterials = [new WorldMaterial(1, Material.DIRT)]
    List<WorldMaterial> stoneLayerMaterials = [new WorldMaterial(1, Material.STONE)]
    List<WorldMaterial> bottomLayerMaterials = [new WorldMaterial(1, Material.DEEPSLATE)]

    List<WorldTree> worldTreeTypes = []

    StarcadeWorldGen(int maxBlockHeight = 20, int minBlockHeight = -64, boolean generateWater = false, boolean generateOres = false, double treeChance = 0, double grassChance = 0, double flowerChance = 0, double oreChance = 0) {
        this.maxBlockHeight = maxBlockHeight
        this.minBlockHeight = minBlockHeight
        this.generateWater = generateWater
        this.generateOres = generateOres
        this.treeChance = treeChance
        this.grassChance = grassChance
        this.flowerChance = flowerChance
        this.oreChance = oreChance
    }

    StarcadeWorldGen setTopGroundMaterials(List<WorldMaterial> topGroundMaterials) {
        this.topGroundMaterials = topGroundMaterials
        return this
    }

    StarcadeWorldGen setTopLayerMaterials(List<WorldMaterial> topLayerMaterials) {
        this.topLayerMaterials = topLayerMaterials
        return this
    }

    StarcadeWorldGen setStoneLayerMaterials(List<WorldMaterial> undergroundMaterials) {
        this.stoneLayerMaterials = undergroundMaterials
        return this
    }

    StarcadeWorldGen setTreeTypes(List<WorldTree> worldTreeTypes) {
        this.worldTreeTypes = worldTreeTypes
        return this
    }

    StarcadeWorldGen setBottomLayerMaterials(List<WorldMaterial> bottomLayerMaterials) {
        this.bottomLayerMaterials = bottomLayerMaterials
        return this
    }

    @Override
    void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData cd) {
        PerlinOctaveGenerator wgen = new PerlinOctaveGenerator(worldInfo.getSeed(), 6)
        wgen.setScale(0.01D)

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int realX = x + chunkX * 16
                int realZ = z + chunkZ * 16

                def noise = wgen.noise(realX, realZ, 0.7D, 1D) * 15

//                println("x:${x},z:${z} - ${noise}")

                double normalHeight = noise + maxBlockHeight

//                println("x:${x},z:${z} - ${normalHeight}")

                // stone layer generation
                for (int newY = stoneLayerStart; newY < normalHeight; newY++) {
                    if (stoneLayerMaterials.isEmpty()) {
                        cd.setBlock(x, newY, z, Material.STONE)
                    } else {
                        cd.setBlock(x, newY, z, RandomSelector.weighted(stoneLayerMaterials, { entry -> entry.getChance() }).pick().material)
                    }
                }

                for (int newY = minBlockHeight + 1; newY < stoneLayerStart; newY++) {
                    if (bottomLayerMaterials.isEmpty()) {
                        cd.setBlock(x, newY, z, Material.DEEPSLATE)
                    } else {
                        cd.setBlock(x, newY, z, RandomSelector.weighted(bottomLayerMaterials, { entry -> entry.getChance() }).pick().material)
                    }
                }

                if (generateOres) {
                    for (int i = 0; i < 20 * 10; i++) {
                        setRandomBlock(x, normalHeight as int, z, cd, random, 128, Material.COAL_ORE)
                    }

                    for (int i = 0; i < 20 * 6; i++) {
                        setRandomBlock(x, normalHeight as int, z, cd, random, 64, Material.IRON_ORE)
                    }

                    for (int i = 0; i < 1 * 4; i++) {
                        setRandomBlock(x, normalHeight as int, z, cd, random, 32, Material.GOLD_ORE)
                    }

                    for (int i = 0; i < 4 * 1; i++) {
                        setRandomBlock(x, normalHeight as int, z, cd, random, 32, Material.EMERALD_ORE)
                    }

                    for (int i = 0; i < 8 * 6; i++) {
                        setRandomBlock(x, normalHeight as int, z, cd, random, 16, Material.REDSTONE_ORE)
                    }

                    for (int i = 0; i < 1 * 7; i++) {
                        setRandomBlock(x, normalHeight as int, z, cd, random, 16, Material.DIAMOND_ORE)
                    }

                    for (int i = 0; i < 1 * 5; i++) {
                        setRandomBlock(x, normalHeight as int, z, cd, random, 16, Material.LAPIS_ORE)
                    }
                }

                // bedrock
                cd.setBlock(x, -64, z, Material.BEDROCK)

                // ground generation
                if (generateWater && normalHeight < maxBlockHeight) {
                    double waterH = normalHeight
                    while (waterH <= 14) {
                        cd.setBlock(x, waterH as int, z, Material.WATER)
                        waterH++
                    }

                    for (int newY = (int) normalHeight - 3; newY < normalHeight; newY++) {
                        if (topGroundMaterials.isEmpty()) {
                            cd.setBlock(x, newY, z, Material.GRASS_BLOCK)
                        } else {
                            if (newY < 13) cd.setBlock(x, newY, z, RandomSelector.weighted(underWaterMaterials, { entry -> entry.getChance() }).pick().material)
                            else cd.setBlock(x, newY, z, RandomSelector.weighted(topGroundMaterials, { entry -> entry.getChance() }).pick().material)
                        }
                    }
                } else {
                    for (int newY = (int) normalHeight - 3; newY < normalHeight; newY++) {
                        if (topLayerMaterials.isEmpty()) {
                            cd.setBlock(x, newY, z, Material.DIRT)
                        } else {
                            cd.setBlock(x, newY, z, RandomSelector.weighted(topLayerMaterials, { entry -> entry.getChance() }).pick().material)
                        }
                    }

                    if (topGroundMaterials.isEmpty()) {
                        cd.setBlock(x, normalHeight as int, z, Material.GRASS_BLOCK)
                    } else {
                        cd.setBlock(x, normalHeight as int, z, RandomSelector.weighted(topGroundMaterials, { entry -> entry.getChance() }).pick().material)
                    }
                }

                def structureDepth = ThreadLocalRandom.current().nextInt(0, 5) * -1

                if (normalHeight >= (12 + structureDepth)) {
                    def world = Bukkit.getWorld(worldInfo.getUID())
                    def location = new Location(world, realX, normalHeight, realZ)

                    Double treeChance = 0.0005
                    Schematic schematic = null

                    def biome = cd.getBiome(realX, normalHeight as int, realZ)

                    if (biome != null) {
                        if (biome == Biome.PLAINS || biome == Biome.FOREST || biome == Biome.BIRCH_FOREST || biome == Biome.DARK_FOREST) {
                            switch (ThreadLocalRandom.current().nextInt(0, 3)) {
                                case 0:
                                    schematic = blueTree
                                    break
                                case 1:
                                    schematic = blueTree90
                                    break
                                case 2:
                                    schematic = blueTree180
                                    break
                                case 3:
                                    schematic = blueTree270
                                    break
                            }
                        } else if (biome == Biome.TAIGA || biome == Biome.SNOWY_SLOPES || biome == Biome.SNOWY_BEACH || biome == Biome.SNOWY_PLAINS || biome == Biome.SNOWY_TAIGA) {
                            switch (ThreadLocalRandom.current().nextInt(0, 3)) {
                                case 0:
                                    schematic = purpleTree
                                    break
                                case 1:
                                    schematic = purpleTree90
                                    break
                                case 2:
                                    schematic = purpleTree180
                                    break
                                case 3:
                                    schematic = purpleTree270
                                    break
                            }
                        } else if (biome == Biome.JUNGLE || biome == Biome.OLD_GROWTH_BIRCH_FOREST || biome == Biome.BAMBOO_JUNGLE || biome == Biome.BADLANDS) {
                            switch (ThreadLocalRandom.current().nextInt(0, 3)) {
                                case 0:
                                    schematic = blueShroomTree
                                    break
                                case 1:
                                    schematic = blueShroomTree90
                                    break
                                case 2:
                                    schematic = blueShroomTree180
                                    break
                                case 3:
                                    schematic = blueShroomTree270
                                    break
                            }
                        } else if (biome == Biome.SWAMP || biome == Biome.MANGROVE_SWAMP || biome == Biome.GROVE || biome == Biome.OCEAN || biome == Biome.WARM_OCEAN) {
                            switch (ThreadLocalRandom.current().nextInt(0, 3)) {
                                case 0:
                                    schematic = purpleShroomTree
                                    break
                                case 1:
                                    schematic = purpleShroomTree90
                                    break
                                case 2:
                                    schematic = purpleShroomTree180
                                    break
                                case 3:
                                    schematic = purpleShroomTree270
                                    break
                            }
                        }
                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.00008) {
                        switch (ThreadLocalRandom.current().nextInt(0, 3)) {
                            case 0:
                                purplePillar0.paste(new Location(Bukkit.getWorld(worldInfo.getUID()), realX, normalHeight, realZ))
                                break
                            case 1:
                                purplePillar90.paste(new Location(Bukkit.getWorld(worldInfo.getUID()), realX, normalHeight, realZ))
                                break
                            case 2:
                                purplePillar180.paste(new Location(Bukkit.getWorld(worldInfo.getUID()), realX, normalHeight, realZ))
                                break
                            case 3:
                                purplePillar270.paste(new Location(Bukkit.getWorld(worldInfo.getUID()), realX, normalHeight, realZ))
                                break
                        }
                    } else if (ThreadLocalRandom.current().nextDouble() < treeChance) {
                        if (schematic != null) {
                            schematic.paste(location)
                        }
                    }
                }
            }
        }
    }

    @Override
    List<BlockPopulator> getDefaultPopulators(World world) {
        return [new FlowerAndGrassPopulator()/*, new TreePopulator()*/]
    }

    static int getHighestBlockAt(LimitedRegion limitedRegion, int x, int z) {
        for (int y = 300; y > -64; y--) {
            if (limitedRegion.getBlockData(x, y, z).getMaterial() != Material.AIR) {
                return y
            }
        }
        return -64
    }

    def setRandomBlock(int x, int y, int z, ChunkData cd, Random random, int maxHeight, Material material) {
        int chance = random.nextInt(100) + 1
        if (chance <= oreChance) {

            int rndX = random.nextInt(16)
            int rndZ = random.nextInt(16)
            int rndY = random.nextInt((maxHeight - minBlockHeight) - 4) + 4 + minBlockHeight

            if (rndX == x && rndZ == z) {
                if (rndY <= y) {
                    cd.setBlock(x, rndY, z, material)
                }
            }
        }
    }

    class FlowerAndGrassPopulator extends BlockPopulator {

        @Override
        void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {

                    int realX = x + chunkX * 16
                    int realZ = z + chunkZ * 16

                    int highestY = getHighestBlockAt(limitedRegion, realX, realZ)

                    BlockData bd = limitedRegion.getBlockData(realX, highestY, realZ)
                    BlockData bdAbove = limitedRegion.getBlockData(realX, highestY + 1, realZ)

                    if (bd.getMaterial() == Material.DIRT || bd.getMaterial() == Material.GRASS_BLOCK) {
                        if (bdAbove.getMaterial() == Material.AIR) {

                            int randomBlockGrass = random.nextInt(100) + 1
                            int randomBlockFlower = random.nextInt(100) + 1

                            if (randomBlockGrass <= grassChance) {
                                limitedRegion.setType(realX, highestY + 1, realZ, Material.GRASS)
                            } else if (randomBlockFlower <= flowerChance) {

                                int randomFlowerColor = random.nextInt(4) + 1

                                if (randomFlowerColor <= 3) {
                                    limitedRegion.setType(realX, highestY + 1, realZ, Material.DANDELION)
                                } else {
                                    limitedRegion.setType(realX, highestY + 1, realZ, Material.POPPY)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    class WorldBiomeGenerator extends BiomeProvider {

        @Override
        Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            def biome = Biome.PLAINS


            return biome
        }

        @Override
        List<Biome> getBiomes(WorldInfo worldInfo) {
            return Arrays.asList(Biome.PLAINS)
        }
    }
}