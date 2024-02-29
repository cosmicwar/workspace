package scripts.factions.content.dbconfig

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.data.DataObject

@CompileStatic
class Config extends DataObject {

    Set<ConfigCategory> categories = Sets.newConcurrentHashSet()

    String displayName
    Material material = Material.BOOK
    List<String> description = []

    Config() {}

    Config(String id) {
        super(id)
        this.displayName = id
    }

    Config(String id, String displayName, Material material = Material.BOOK, List<String> description = []) {
        super(id)
        this.displayName = displayName
        this.material = material
        this.description = description
    }

    @BsonIgnore
    Config addCategory(ConfigCategory category) {
        this.categories.add(category)
        return this
    }

    @BsonIgnore
    ConfigCategory getCategory(String id) {
        return this.categories.find { it.internalId == id }
    }

    @BsonIgnore
    ConfigCategory getDefaultCategory() {
        def category = this.categories.find { it.internalId == "default" }
        if (category == null) {
            category = new ConfigCategory("default", "Default")
            this.addCategory(category)
            this.queueSave()
        }
        return category
    }

    @BsonIgnore
    ConfigCategory getOrCreateCategory(String id, String displayName = null, Material material = Material.BOOK, List<String> description = []) {
        def category = this.getCategory(id)
        if (category == null) {
            category = new ConfigCategory(id, displayName != null ? displayName : id, description, material)
            this.addCategory(category)
            this.queueSave()
        }
        return category
    }

    @BsonIgnore
    def addEntry(String categoryId, ConfigEntry entry, boolean defaultEntry = false) {
        this.getOrCreateCategory(categoryId, categoryId).addEntry(entry, defaultEntry)
    }

    @BsonIgnore
    def addEntries(String categoryId, String configId, List<ConfigEntry> entry, boolean defaultEntry = false) {
        this.getOrCreateCategory(categoryId, categoryId).addEntries(configId, entry, defaultEntry)
    }

    @BsonIgnore
    def addEntry(ConfigEntry entry, boolean defaultEntry = false) {
        this.getDefaultCategory().addEntry(entry, defaultEntry)
    }

    @BsonIgnore
    def addDefaultEntry(ConfigEntry entry) {
        this.getDefaultCategory().addDefaultEntry(entry)
    }

    @BsonIgnore
    @Override
    boolean isEmpty() {
        return false
    }
}
