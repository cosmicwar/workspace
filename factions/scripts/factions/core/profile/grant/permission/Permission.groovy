package scripts.factions.core.profile.grant.permission

import org.bson.codecs.pojo.annotations.BsonIgnore

class Permission {

    String permission
    Long timeExpires = -1

    Permission() {}

    Permission(String permission, Long timeExpires = -1) {
        this.permission = permission
        this.timeExpires = timeExpires
    }

    @BsonIgnore
    boolean temporary() {
        return timeExpires > 0
    }

}
