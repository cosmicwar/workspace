package scripts.factions.features.duel.match.matches

import org.bukkit.entity.Player
import scripts.factions.content.scoreboard.sidebar.Sidebar
import scripts.factions.content.scoreboard.sidebar.SidebarBuilder
import scripts.factions.features.duel.Duels
import scripts.factions.features.duel.arena.Arena
import scripts.factions.features.duel.arena.ArenaType
import scripts.factions.features.duel.match.Match
import scripts.factions.features.duel.player.DuelPlayer
import scripts.factions.features.duel.snapshot.MatchSnapshot

import java.util.concurrent.CompletableFuture

class NoDebuffMatch extends Match {

    DuelPlayer playerA, playerB
    
    NoDebuffMatch() {
        super("no_debuff", "No-Debuff")
    }

    @Override
    void setupPlayer(Player player) {

    }

    @Override
    def startMatch(MatchSnapshot snapshot) {
        return null
    }

    @Override
    def startRound(MatchSnapshot snapshot) {
        return null
    }

    @Override
    def endRound(MatchSnapshot snapshot) {
        return null
    }

    @Override
    def endMatch(MatchSnapshot snapshot) {
        return null
    }
}
