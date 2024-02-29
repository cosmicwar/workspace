package scripts.factions.data

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonIgnore

abstract class DataObject {

    @BsonId
    String id

    transient boolean requiresSaving = false

    DataObject() {
    }

    DataObject(String id) {
        this.id = id
    }

    @BsonIgnore
    void queueSave() {
        this.requiresSaving = true
    }

    @BsonIgnore
    abstract boolean isEmpty()

}


