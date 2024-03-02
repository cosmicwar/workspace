package scripts.factions.core.profile.grant

import org.bson.codecs.pojo.annotations.BsonIgnore

class Grant {

    UUID grantId = UUID.randomUUID(), rankId = null
    Long timeAdded = -1, timeExpires = -1

    Grant() {}

    Grant(UUID rankId, Long timeExpires = -1) {
        this.rankId = rankId
        this.timeExpires = timeExpires
    }

    @BsonIgnore
    boolean expired() {
        return timeExpires > 0 && timeExpires < System.currentTimeMillis()
    }

}
