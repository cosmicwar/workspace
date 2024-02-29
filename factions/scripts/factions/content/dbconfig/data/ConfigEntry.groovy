package scripts.factions.content.dbconfig.data

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore

@CompileStatic(TypeCheckingMode.SKIP)
abstract class ConfigEntry<T> {

    String id
    ConfigType type

    Set<String> description = Sets.newHashSet()

    @BsonIgnore abstract T getValue()
    @BsonIgnore abstract T getDefaultValue()
    @BsonIgnore abstract void resetToDefault()

    ConfigEntry(){}

    ConfigEntry(String id, ConfigType type) {
        this.id = id
        this.type = type
    }

    @BsonIgnore
    ConfigEntry<T> get() {
        return this
    }

    @BsonIgnore
    static boolean isEmpty() {
        return false
    }

}

