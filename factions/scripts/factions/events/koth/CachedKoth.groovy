package scripts.factions.events.koth

import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.data.DataObject
import scripts.factions.events.captureable.CaptureState

class CachedKoth extends DataObject {
    UUID cappingPlayerId = null
    UUID attackingFactionId = null

    CaptureState captureState = CaptureState.NEUTRAL

    Integer timeRemaining = 0
    Integer duration = 0

    CachedKoth() {}

    CachedKoth(String internalName) {
        super(internalName)
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }
}
