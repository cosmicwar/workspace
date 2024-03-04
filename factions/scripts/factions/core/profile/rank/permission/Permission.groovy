package scripts.factions.core.profile.rank.permission

import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.shared.utils.Temple

class Permission {

    String permission
    Long timeExpires = -1

    Boolean global = true
    Set<String> activeTempleIds = new HashSet<String>()

    Permission() {}

    Permission(String permission, Long timeExpires = -1) {
        this.permission = permission
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
        return global || activeTempleIds.any {it.equalsIgnoreCase(Temple.templeId.replaceAll("\\d", "").replace("_local", "")) }
    }

}
