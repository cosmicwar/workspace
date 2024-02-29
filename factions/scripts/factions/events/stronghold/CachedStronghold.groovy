package scripts.factions.events.stronghold

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.data.DataObject

@CompileStatic
class CachedStronghold extends DataObject {

    UUID attackingFactionId = null
    UUID capturingFactionId = null
    UUID controllingFactionId = null

    CaptureState captureState = CaptureState.NEUTRAL

    double cappedPercent = 0.0D

    CachedStronghold() {}

    CachedStronghold(String internalName) {
        super(internalName)
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }
}
