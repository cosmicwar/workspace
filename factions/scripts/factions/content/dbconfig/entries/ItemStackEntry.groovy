package scripts.factions.content.dbconfig.entries

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType
import scripts.factions.data.obj.SItem
import scripts.shared.utils.ItemType

@CompileStatic(TypeCheckingMode.SKIP)
class ItemStackEntry extends ConfigEntry<SItem> {

    SItem defaultValue
    SItem value

    ItemStackEntry() {}

    ItemStackEntry(String id, SItem defaultValue = null, String... description = []) {
        super(id, ConfigType.ITEM_STACK)
        this.defaultValue = defaultValue
        this.value = defaultValue
        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }
}
