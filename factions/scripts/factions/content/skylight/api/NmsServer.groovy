package scripts.factions.content.skylight.api

import org.bukkit.block.Block

interface NmsServer {

    NmsBiome getBiomeFromBiomeKey(BiomeKey biomeKey);

    NmsBiome getBiomeFromBiomeBase(Object biomeBase);

    boolean doesBiomeExist(BiomeKey biomeKey);

    void loadBiome(BiomeKey biomeKey, BiomeColors biomeColors);

    void setBlocksBiome(Block block, NmsBiome nmsBiome);

    Object getBlocksBiomeBase(Block block);

    void registerBiome(Object biomeBase, Object biomeMinecraftKey);

    String getBiomeString(NmsBiome nmsBiome);

}
