package scripts.factions.content.dbconfig.entries

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType
import scripts.factions.data.obj.SR

@CompileStatic(TypeCheckingMode.SKIP)
class SREntry extends ConfigEntry<SR> {

    SR defaultValue
    SR value

    SREntry() {}

    SREntry(String id, SR defaultValue = new SR(), String... description = []) {
        super(id, ConfigType.SR)
        this.defaultValue = defaultValue
        this.value = defaultValue
        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }
}