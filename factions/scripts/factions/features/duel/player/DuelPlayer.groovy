package scripts.factions.features.duel.player

import com.google.common.collect.Sets
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.data.obj.SInventory
import scripts.factions.data.uuid.UUIDDataObject
import scripts.factions.features.duel.match.MatchState
import scripts.factions.features.duel.snapshot.MatchSnapshot

class DuelPlayer extends UUIDDataObject {

    MatchState state = MatchState.NONE
    UUID activeMatch = null, partyId = null
    boolean acceptingDuels = true, acceptingPartyInvites = true

    SInventory inventory = new SInventory()
    Set<UUID> matchHistory = Sets.<UUID> newConcurrentHashSet()

    /* tracking */
    @BsonIgnore transient int potsMissed = 0, potsWasted = 0, finalPots = 0, hits = 0, crits = 0, blocked = 0, bestCombo = 0
    @BsonIgnore transient double potAccuracy = 0.0, regenAmount = 0.0, wTaps = 0.0

    DuelPlayer() {}

    DuelPlayer(UUID id) {
        super(id)
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }
}
