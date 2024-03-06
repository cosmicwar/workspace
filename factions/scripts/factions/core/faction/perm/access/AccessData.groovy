package scripts.factions.core.faction.perm.access

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.relation.RelationType
import scripts.shared.data.obj.CL

@CompileStatic
class AccessData {

    UUID targetId // Faction or Player
    TargetType accessor

    Set<Access> access = Sets.newConcurrentHashSet()

    CL accessChunk = null

    Long accessStart = null
    Long accessEnd = -1L // -1 = forever

    AccessData() {
    }

    AccessData(UUID targetId, TargetType accessType, Long accessStart = System.currentTimeMillis(), Long accessEnd = -1L) {
        this.targetId = targetId
        this.accessor = accessType
        this.accessStart = accessStart
        this.accessEnd = accessEnd
    }

    @BsonIgnore
    static AccessData of(UUID targetId, TargetType accessType, String internalId, Long accessStart = System.currentTimeMillis(), Long accessEnd = -1L) {
        return new AccessData(targetId, accessType, accessStart, accessEnd)
    }

    @BsonIgnore
    boolean isExpired() {
        if (accessEnd == -1) return false
        return System.currentTimeMillis() > accessEnd
    }

    @BsonIgnore
    boolean isPermanent() {
        return accessEnd == -1
    }

    @BsonIgnore
    boolean isAccessing() {
        if (accessStart == null) return false
        if (accessEnd == -1) return true
        return System.currentTimeMillis() < accessEnd
    }

    @BsonIgnore
    def endAccess() {
        this.accessEnd = System.currentTimeMillis()
    }

    @BsonIgnore
    boolean isEmpty() {
        return isExpired()
    }

    @BsonIgnore
    String getTargetName() {
        if (accessor == TargetType.FACTION) return Factions.getFaction(targetId, false).name
        if (accessor == TargetType.PLAYER) return Bukkit.getOfflinePlayer(targetId).getName()

        return "Unknown"
    }

    @BsonIgnore
    RelationType getRelation(Faction targetFaction) {
        if (accessor == TargetType.FACTION) return Factions.getRelationType(Factions.getFaction(targetId, false), targetFaction)
        if (accessor == TargetType.PLAYER) return Factions.getRelationType(Factions.getMember(targetId, false), targetFaction)

        return RelationType.NEUTRAL
    }

}

