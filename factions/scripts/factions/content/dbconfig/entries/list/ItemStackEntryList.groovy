package scripts.factions.content.dbconfig.entries.list

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType
import scripts.factions.data.obj.SItem
import scripts.shared.utils.ItemType

@CompileStatic(TypeCheckingMode.SKIP)
class ItemStackEntryList extends ConfigEntry<List<SItem>> {
    List<SItem> defaultValue
    List<SItem> value

    ItemStackEntryList() {}

    ItemStackEntryList(String id, List<SItem> defaultValue = [], String... description = []) {
        super(id, ConfigType.LIST_ITEM_STACK)

        this.defaultValue = defaultValue
        this.value = defaultValue

        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }

}
