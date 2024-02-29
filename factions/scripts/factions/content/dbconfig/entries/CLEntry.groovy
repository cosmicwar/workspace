package scripts.factions.content.dbconfig.entries

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType
import scripts.factions.data.obj.CL

@CompileStatic(TypeCheckingMode.SKIP)
class CLEntry extends ConfigEntry<CL> {

    CL defaultValue
    CL value

    CLEntry() {}

    CLEntry(String id, CL defaultValue = new CL(), String... description = []) {
        super(id, ConfigType.CL)
        this.defaultValue = defaultValue
        this.value = defaultValue
        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }
}