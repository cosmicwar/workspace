package scripts.factions.events.captureable

import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.shared.data.string.StringDataObject

class CachedEvent extends StringDataObject {
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
