package scripts.factions.content.dbconfig.entries

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType

@CompileStatic(TypeCheckingMode.SKIP)
class DoubleEntry extends ConfigEntry<Double> {

    Double defaultValue
    Double value

    DoubleEntry() {}

    DoubleEntry(String id, double defaultValue = 0.0, String... description = []) {
        super(id, ConfigType.DOUBLE)
        this.defaultValue = defaultValue
        this.value = defaultValue
        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }
}