package scripts.factions.content.dbconfig.entries

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType

@CompileStatic(TypeCheckingMode.SKIP)
class IntEntry extends ConfigEntry<Integer> {

    Integer defaultValue
    Integer value

    IntEntry() {}

    IntEntry(String id, Integer defaultValue = 0, String... description = []) {
        super(id, ConfigType.INT)
        this.defaultValue = defaultValue
        this.value = defaultValue
        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }
}