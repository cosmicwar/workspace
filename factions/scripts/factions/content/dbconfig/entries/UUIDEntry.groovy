package scripts.factions.content.dbconfig.entries

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType

@CompileStatic(TypeCheckingMode.SKIP)
class UUIDEntry extends ConfigEntry<UUID> {

    UUID defaultValue
    UUID value

    UUIDEntry() {}

    UUIDEntry(String id, UUID defaultValue = null, String... description = []) {
        super(id, ConfigType.UUID)
        this.defaultValue = defaultValue
        this.value = defaultValue
        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }
}