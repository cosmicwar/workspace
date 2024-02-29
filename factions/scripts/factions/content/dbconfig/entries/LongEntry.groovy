package scripts.factions.content.dbconfig.entries

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType

@CompileStatic(TypeCheckingMode.SKIP)
class LongEntry extends ConfigEntry<Long> {

    Long defaultValue
    Long value

    LongEntry() {}

    LongEntry(String id, Long defaultValue = 0L, String... description = []) {
        super(id, ConfigType.LONG)
        this.defaultValue = defaultValue
        this.value = defaultValue
        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }
}
