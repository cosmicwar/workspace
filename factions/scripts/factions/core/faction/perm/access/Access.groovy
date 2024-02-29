package scripts.factions.core.faction.perm.access

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.core.faction.data.Role

@CompileStatic
class Access {

    String internalId
    Role requiredRole = Role.RECRUIT

    Access() { }

    Access(String internalId, Role requiredRole = Role.RECRUIT) {
        this.internalId = internalId
        this.requiredRole = requiredRole
    }

    @BsonIgnore
    boolean isEmpty() {
        return internalId == null
    }

}