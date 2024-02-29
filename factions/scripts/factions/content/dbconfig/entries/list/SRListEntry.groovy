package scripts.factions.content.dbconfig.entries.list

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType
import scripts.factions.data.obj.SR
import scripts.shared.utils.ItemType

@CompileStatic(TypeCheckingMode.SKIP)
class SRListEntry extends ConfigEntry<List<SR>>
{
    List<SR> defaultValue
    List<SR> value

    SRListEntry() {}

    SRListEntry(String id, List<SR> defaultValue = [], String... description = []) {
        super(id, ConfigType.LIST_SR)

        this.defaultValue = defaultValue
        this.value = defaultValue

        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }

}
