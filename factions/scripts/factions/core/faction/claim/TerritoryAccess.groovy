package scripts.factions.core.faction.claim

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.core.faction.perm.Permission

@CompileStatic
class TerritoryAccess
{

    UUID claimedFactionId

    UUID targetFactionId = null
    UUID targetPlayerId = null

    Set<Permission> permissions = Sets.newConcurrentHashSet()

    TerritoryAccess() {
    }

    TerritoryAccess(UUID claimedFactionId) {
        this.claimedFactionId = claimedFactionId
    }

    TerritoryAccess(UUID claimedFactionId, UUID targetFactionId) {
        this.claimedFactionId = claimedFactionId
        this.targetFactionId = targetFactionId
    }

    TerritoryAccess(UUID claimedFactionId, UUID targetFactionId, UUID targetPlayerId) {
        this.claimedFactionId = claimedFactionId
        this.targetFactionId = targetFactionId
        this.targetPlayerId = targetPlayerId
    }


    @BsonIgnore
    boolean isEmpty() {
        return this.claimedFactionId == null
    }




}