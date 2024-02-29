package scripts.factions.core.faction.addon.upgrade.data

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore

@CompileStatic(TypeCheckingMode.SKIP)
class StoredUpgrade {

    String internalId
    int level = 0

    StoredUpgrade(){}

    StoredUpgrade(String internalId, int level = 0) {
        this.internalId = internalId
        this.level = level
    }

    @BsonIgnore
    boolean isEmpty() {
        return level == 0
    }

}