package scripts.factions.content.dbconfig.entries

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType

@CompileStatic(TypeCheckingMode.SKIP)
class StringEntry extends ConfigEntry<String> {

    String defaultValue
    String value

    StringEntry() {}

    StringEntry(String id, String defaultValue = "", String... description = []) {
        super(id, ConfigType.STRING)
        this.defaultValue = defaultValue
        this.value = defaultValue
        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }
}