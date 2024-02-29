package scripts.factions.features.duel.snapshot

import scripts.factions.data.obj.SInventory

class PlayerSnapshot {

    UUID playerId

    SInventory inventory = null
    MatchResult result = null

    int potsMissed = 0, potsWasted = 0, finalPots = 0, hits = 0, crits = 0, blocked = 0, bestCombo = 0
    double potAccuracy = 0.0, regenAmount = 0.0, wTaps = 0.0

    PlayerSnapshot() {}

    PlayerSnapshot(UUID playerId) {
        this.playerId = playerId
    }

}
