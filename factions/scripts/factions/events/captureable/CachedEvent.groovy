package scripts.factions.events.captureable

import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.data.DataObject

class CachedEvent extends DataObject {
    UUID attackingFactionId = null
    UUID capturingFactionId = null
    UUID controllingFactionId = null

    CaptureState captureState = CaptureState.NEUTRAL

    double cappedPercent = 0.0D

    CachedEvent() {}

    CachedEvent(String internalName) {
        super(internalName)
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }
}
