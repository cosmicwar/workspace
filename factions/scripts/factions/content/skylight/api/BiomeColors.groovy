package scripts.factions.content.skylight.api

class BiomeColors {

    int grassColor
    int foliageColor
    int waterColor
    int waterFogColor
    int skyColor
    int fogColor

    void setColor(BiomeColorType colorType, int color) {
        if (colorType == BiomeColorType.GRASS) {
            this.setGrassColor(color)
        } else if (colorType == BiomeColorType.FOLIAGE) {
            this.setFoliageColor(color)
        } else if (colorType == BiomeColorType.WATER) {
            this.setWaterColor(color)
        } else if (colorType == BiomeColorType.WATER_FOG) {
            this.setWaterFogColor(color)
        } else if (colorType == BiomeColorType.SKY) {
            this.setSkyColor(color)
        } else if (colorType == BiomeColorType.FOG) {
            this.setFogColor(color)
        }
    }

    BiomeColors setGrassColor(int grassColor) {
        this.grassColor = grassColor
        return this
    }

    BiomeColors setFoliageColor(int foliageColor) {
        this.foliageColor = foliageColor
        return this
    }

    BiomeColors setWaterColor(int waterColor) {
        this.waterColor = waterColor
        return this
    }

    BiomeColors setWaterFogColor(int waterFogColor) {
        this.waterFogColor = waterFogColor
        return this
    }

    BiomeColors setSkyColor(int skyColor) {
        this.skyColor = skyColor
        return this
    }

    BiomeColors setFogColor(int fogColor) {
        this.fogColor = fogColor
        return this
    }

}
