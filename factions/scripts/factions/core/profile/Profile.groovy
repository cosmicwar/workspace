package scripts.factions.core.profile

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.data.uuid.UUIDDataObject

@CompileStatic(TypeCheckingMode.SKIP)
class Profile extends UUIDDataObject {



    Profile() {}

    Profile(UUID id) {
        super(id)
    }



    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }
}
