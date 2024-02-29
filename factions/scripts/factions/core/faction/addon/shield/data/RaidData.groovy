package scripts.factions.core.faction.addon.shield.data

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.data.uuid.UUIDDataObject

@CompileStatic(TypeCheckingMode.SKIP)
class RaidData extends UUIDDataObject
{

    UUID raidId = null

    UUID breachedFactionId = null
    UUID attackingFactionId = null

    Long raidStart = null
    Long raidEnd = null

    Long startingFTopValue = 0
    Long startingSpawnerValue = 0
    Long startingPointsValue = 0

    Long endingFTopValue = 0
    Long endingSpawnerValue = 0
    Long endingPointsValue = 0

    int blocksDestroyed = 0
    int spawnersDestroyed = 0

    int tntUsed = 0
    int creepersUsed = 0

    RaidData() {}

    RaidData(UUID raidId, UUID breachedFactionId, UUID attackingFactionId) {
        this.raidId = raidId
        this.breachedFactionId = breachedFactionId
        this.attackingFactionId = attackingFactionId
        this.raidStart = System.currentTimeMillis()
    }

    @BsonIgnore
    boolean hasEnded() {
        return raidEnd != null
    }

    @BsonIgnore
    void endRaid() {
        this.raidEnd = System.currentTimeMillis()
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }

}
