package scripts.factions.content.dbconfig.entries.list

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType
import scripts.shared.utils.ItemType

@CompileStatic(TypeCheckingMode.SKIP)
class ItemTypeListEntry extends ConfigEntry<List<ItemType>>
{
    List<ItemType> defaultValue
    List<ItemType> value

    ItemTypeListEntry() {}

    ItemTypeListEntry(String id, List<ItemType> defaultValue = [], String... description = []) {
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
