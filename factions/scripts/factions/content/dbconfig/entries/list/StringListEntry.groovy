package scripts.factions.content.dbconfig.entries.list

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType

@CompileStatic(TypeCheckingMode.SKIP)
class StringListEntry extends ConfigEntry<List<String>>
{
    List<String> defaultValue
    List<String> value

    StringListEntry() {}

    StringListEntry(String id, List<String> defaultValue = [], String... description = []) {
        super(id, ConfigType.LIST_STRING)

        this.defaultValue = defaultValue
        this.value = defaultValue

        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }

}
