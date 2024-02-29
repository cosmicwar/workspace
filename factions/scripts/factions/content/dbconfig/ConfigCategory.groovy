package scripts.factions.content.dbconfig

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material
import scripts.factions.content.dbconfig.data.ConfigEntry

@CompileStatic
class ConfigCategory {

    String internalId

    String displayName

    List<String> description = []
    Material material = Material.BOOK

    Set<RegularConfig> configs = Sets.newConcurrentHashSet()

    ConfigCategory() {}

    ConfigCategory(String internalId, String displayName, List<String> description = [], Material material = Material.BOOK) {
        this.internalId = internalId
        this.displayName = displayName
        this.description = description
        this.material = material
    }

    @BsonIgnore
    ConfigCategory addConfig(RegularConfig config) {
        this.configs.add(config)
        return this
    }

    @BsonIgnore
    def removeConfig(RegularConfig config) {
        this.configs.remove(config)
    }

    @BsonIgnore
    def clearConfigs() {
        this.configs.clear()
    }

    @BsonIgnore
    RegularConfig getConfig(String configId) {
        return this.configs.find { it.internalId == configId }
    }

    @BsonIgnore
    RegularConfig getOrCreateConfig(String configId, String displayName = null, Material material = Material.BOOK, List<String> description = []) {
        def config = this.getConfig(configId)
        if (config == null) {
            config = new RegularConfig(configId, displayName != null ? displayName : configId, description, material)
            this.addConfig(config)
        }
        return config
    }

    @BsonIgnore
    def addEntry(String configId, ConfigEntry entry, boolean defaultEntry = false) {
        def config = this.getOrCreateConfig(configId, configId)
        config.addEntry(entry, defaultEntry)
    }

    @BsonIgnore
    def addEntries(String configId, List<ConfigEntry> entry, boolean defaultEntry = false) {
        def config = this.getOrCreateConfig(configId, configId)
        config.addEntries(entry, defaultEntry)
    }

    @BsonIgnore
    def addEntry(ConfigEntry entry, boolean defaultEntry = false) {
        this.getDefaultConfig().addEntry(entry, defaultEntry)
    }

    @BsonIgnore
    def addDefaultEntry(ConfigEntry entry) {
        addEntry(entry, true)
    }

    @BsonIgnore
    RegularConfig getDefaultConfig() {
        def config = this.configs.find { it.internalId == "default" }
        if (config == null) {
            config = new RegularConfig("default", "Default")
            this.addConfig(config)
        }
        return config
    }

    @BsonIgnore
    boolean isEmpty() {
        return this.configs.isEmpty()
    }

}
