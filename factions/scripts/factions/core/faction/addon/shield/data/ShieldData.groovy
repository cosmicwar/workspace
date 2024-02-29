package scripts.factions.core.faction.addon.shield.data

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore

@CompileStatic
class ShieldData {

    Integer startHours = null
    Integer endHours = null

    Long changeTime = null
    Integer requestedStartHours = null
    Integer requestedEndHours = null

    ShieldData() {}

    ShieldData(Integer startHours, Integer endHours, Long changeTime, Integer requestedStartHours, Integer requestedEndHours) {
        this.startHours = startHours
        this.endHours = endHours
        this.changeTime = changeTime
        this.requestedStartHours = requestedStartHours
        this.requestedEndHours = requestedEndHours
    }

    @BsonIgnore
    boolean isShielded(int currentHour) {
        if (startHours == null || endHours == null) return false

        if (endHours < startHours) {
            return currentHour >= startHours || currentHour <= endHours
        } else {
            return currentHour >= startHours && currentHour <= endHours
        }
    }

}
