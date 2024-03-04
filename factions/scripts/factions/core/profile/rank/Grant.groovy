package scripts.factions.core.profile.rank

import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.shared.utils.Temple

class Grant {

    UUID grantId = UUID.randomUUID(), rankId = null
    Long timeAdded = -1, timeExpires = -1, timeRemoved = -1

    Set<String> activeTempleIds = new HashSet<String>()

    Grant() {}
    Grant(UUID rankId, Long timeExpires = -1) {
        this.rankId = rankId
        this.timeExpires = timeExpires
    }

    @BsonIgnore
    boolean temporary() {
        return timeExpires > 0
    }

    @BsonIgnore
    boolean expired() {
        return timeExpires > 0 && timeExpires < System.currentTimeMillis()
    }

    @BsonIgnore
    boolean isGlobal() {
        return activeTempleIds.any {it.equalsIgnoreCase("global") }
    }

    @BsonIgnore
    boolean isActiveTemple() {
        return isGlobal() || activeTempleIds.any {it.equalsIgnoreCase(Temple.templeId.replaceAll("\\d", "").replace("_local", "")) }
    }

}
