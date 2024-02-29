package scripts.factions.features.duel.match

import com.google.common.collect.Maps
import scripts.factions.features.duel.player.DuelPlayer
import scripts.factions.features.duel.snapshot.MatchSnapshot

class ActiveMatch {

    MatchState state = MatchState.NONE
    MatchSnapshot snapshot = new MatchSnapshot()

    Map<UUID, DuelPlayer> matchPlayers = Maps.newConcurrentMap()

    ActiveMatch() {}

}
