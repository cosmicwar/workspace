package scripts.factions.core.faction.claim

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.core.faction.data.Faction
import scripts.shared.data.obj.CL
import scripts.shared.data.obj.SR

@CompileStatic(TypeCheckingMode.SKIP)
class Claim {

    @BsonIgnore
    transient static UUID wildernessId = UUID.fromString("11111111-1111-1111-1111-111111111111") // shhhh don't tell anyone
    transient static UUID warzoneId = UUID.fromString("22222222-2222-2222-2222-222222222222")

    UUID factionId
    Long claimDate = -1L

    RegionType type = RegionType.CL

    boolean coreChunk = false

    CL location = new CL()
    SR region = new SR()

    Claim() {
    }

    Claim(UUID factionId) {
        this.factionId = factionId
    }

    Claim(UUID factionId, CL location) {
        this.factionId = factionId
        this.location = location
    }

    Claim(UUID factionId, SR region) {
        this.factionId = factionId
        this.region = region

        this.type = RegionType.SR
    }

    @BsonIgnore
    void updateClaimed(Faction faction)
    {
        this.factionId = faction.getId()
        this.claimDate = System.currentTimeMillis()
    }

    @BsonIgnore
    boolean claimedBy(Faction faction)
    {
        if (faction == null) return false

        return this.factionId == faction.getId()
    }

    @BsonIgnore
    boolean claimed()
    {
        if (this.factionId != null)
            return this.factionId != wildernessId

        return false
    }

    @BsonIgnore
    boolean isEmpty()
    {
        return this.factionId == null || this.factionId == wildernessId
    }
}