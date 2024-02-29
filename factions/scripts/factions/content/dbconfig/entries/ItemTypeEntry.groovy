package scripts.factions.content.dbconfig.entries

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType
import scripts.shared.utils.ItemType

@CompileStatic(TypeCheckingMode.SKIP)
class ItemTypeEntry extends ConfigEntry<ItemType> {

    ItemType defaultValue
    ItemType value

    ItemTypeEntry() {}

    ItemTypeEntry(String id, ItemType defaultValue = null, String... description = []) {
        super(id, ConfigType.ITEM_TYPE)
        this.defaultValue = defaultValue
        this.value = defaultValue
        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }
}