package scripts.factions.data.uuid

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonIgnore

abstract class UUIDDataObject {

    @BsonId
    UUID id

    transient boolean requiresSaving = false

    UUIDDataObject() {
    }

    UUIDDataObject(UUID id) {
        this.id = id
    }

    @BsonIgnore
    void queueSave() {
        this.requiresSaving = true
    }

    @BsonIgnore
    abstract boolean isEmpty()

}
