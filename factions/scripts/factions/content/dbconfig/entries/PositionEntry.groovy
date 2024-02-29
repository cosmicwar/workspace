package scripts.factions.content.dbconfig.entries

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType
import scripts.factions.data.obj.Position

@CompileStatic(TypeCheckingMode.SKIP)
class PositionEntry extends ConfigEntry<Position> {

    Position defaultValue
    Position value

    PositionEntry() {}

    PositionEntry(String id, Position defaultValue = new Position(), String... description = []) {
        super(id, ConfigType.POSITION)
        this.defaultValue = defaultValue
        this.value = defaultValue
        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }
}