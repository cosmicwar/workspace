package scripts.factions.content.skylight.api

class BiomeKey {

    public final String key
    public final String value

    BiomeKey(String key, String value) {
        this.key = key
        this.value = value
    }

    BiomeKey(String biomeKeyString) {
        String[] split = biomeKeyString.split(":", 2)
        this.key = split[0]
        this.value = split[1]
    }

    @Override
    String toString() {
        return key + ":" + value
    }

}
