package scripts.factions.events.darkzone.user

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.core.faction.data.Member
import scripts.factions.data.uuid.UUIDDataObject
import scripts.factions.events.darkzone.DarkzoneTier

@CompileStatic
class DZMember extends Member {

    int level          = 0

    /* statistics */
    int playerKills    = 0
    int playerDeaths   = 0

    int tier1Kills     = 0
    int tier2Kills     = 0
    int tier3Kills     = 0
    int tier4Kills     = 0
    int tier5Kills     = 0
    int mobDeaths      = 0

    int bossKills      = 0
    int bossDeaths     = 0

    double xpEarned    = 0.0D
    double moneyEarned = 0.0D

    boolean editing = false

    @BsonIgnore transient int sessionPlayerKills    = 0
    @BsonIgnore transient int sessionPlayerDeaths   = 0
    @BsonIgnore transient int sessionMobKills       = 0
    @BsonIgnore transient int sessionMobDeaths      = 0
    @BsonIgnore transient int sessionBossKills      = 0
    @BsonIgnore transient int sessionBossDeaths     = 0
    @BsonIgnore transient double sessionXpEarned    = 0.0D
    @BsonIgnore transient double sessionMoneyEarned = 0.0D

    DZMember() {

    }

    DZMember(UUID uuid) {
        super(uuid)
    }

    @BsonIgnore
    int getTotalMobKills() {
        return tier1Kills + tier2Kills + tier3Kills + tier4Kills + tier5Kills
    }

    @BsonIgnore
    def addKill(DarkzoneTier tier, int amount = 1) {
        switch (tier) {
            case DarkzoneTier.TIER_1:
                tier1Kills += amount
                break
            case DarkzoneTier.TIER_2:
                tier2Kills += amount
                break
            case DarkzoneTier.TIER_3:
                tier3Kills += amount
                break
            case DarkzoneTier.TIER_4:
                tier4Kills += amount
                break
            case DarkzoneTier.TIER_5:
                tier5Kills += amount
                break
        }
    }

    @BsonIgnore
    @Override
    boolean isEmpty() {
        return false
    }
}

