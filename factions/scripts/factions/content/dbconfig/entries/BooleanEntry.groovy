package scripts.factions.content.dbconfig.entries

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType

@CompileStatic(TypeCheckingMode.SKIP)
class BooleanEntry extends ConfigEntry<Boolean> {

    Boolean defaultValue
    Boolean value

    BooleanEntry() {}

    BooleanEntry(String id, boolean defaultValue = false, String... description = []) {
        super(id, ConfigType.BOOLEAN)

        this.defaultValue = defaultValue
        this.value = defaultValue

        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }
}