package scripts.factions.core.profile.rank.permission

import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.shared.utils.Temple

class RankPermission {

    UUID permissionId = UUID.randomUUID()

    String permission
    Set<String> activeTempleIds = new HashSet<String>()

    RankPermission() {}

    RankPermission(String permission, Long timeExpires = -1) {
        this.permission = permission
    }

    @BsonIgnore
    boolean isGlobal() {
        return activeTempleIds.any {it.equalsIgnoreCase("global") }
    }

    @BsonIgnore
    boolean isActiveTemple() {
        return isGlobal() || activeTempleIds.any {it.equalsIgnoreCase("global") || it.equalsIgnoreCase(Temple.templeId.replaceAll("\\d", "").replace("_local", "")) }
    }

}
