package scripts.factions.content.log.v2

import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.content.log.v2.api.LogFilterType

class LogUserData {

    LogFilterType alertType = LogFilterType.NONE
    LogFilterType storedType = LogFilterType.ALL
    LogFilterType inventoryFilterType = LogFilterType.ALL

    LogUserData() {}

    @BsonIgnore
    boolean isEmpty() {
        return alertType == LogFilterType.NONE && inventoryFilterType == LogFilterType.ALL && storedType == LogFilterType.ALL
    }

}
