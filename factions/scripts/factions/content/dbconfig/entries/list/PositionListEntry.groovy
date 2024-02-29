package scripts.factions.content.dbconfig.entries.list

import com.google.common.collect.Lists
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType
import scripts.factions.data.obj.Position

@CompileStatic(TypeCheckingMode.SKIP)
class PositionListEntry extends ConfigEntry<List<Position>> {

    List<Position> defaultValue
    List<Position> value

    PositionListEntry() {}

    PositionListEntry(String id, List<Position> defaultValue = [], String... description = []) {
        super(id, ConfigType.LIST_POSITION)
        this.defaultValue = defaultValue
        this.value = defaultValue
        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }
}