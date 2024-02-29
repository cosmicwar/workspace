package scripts.factions.content.skylight.api

interface NmsBiome {

    Object getBiomeBase();

    BiomeColors getBiomeColors();

    NmsBiome cloneWithDifferentColors(NmsServer nmsServer, BiomeKey newBiomeKey, BiomeColors newColors);


}
