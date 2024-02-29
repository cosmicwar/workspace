package scripts.factions.features.duel.match

import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.GroovyScript
import scripts.factions.content.scoreboard.sidebar.Sidebar
import scripts.factions.content.scoreboard.sidebar.SidebarBuilder
import scripts.factions.content.scoreboard.sidebar.SidebarHandler
import scripts.factions.features.duel.arena.Arena
import scripts.factions.features.duel.player.DuelPlayer
import scripts.factions.features.duel.snapshot.MatchSnapshot

abstract class Match {

    Arena arena

    String internalName
    String displayName

    Set<MatchSnapshot> activeMatches = new HashSet<>()
    Map<UUID, DuelPlayer> activePlayers = new HashMap<>()

    Match(String internalName, String displayName) {
        this.internalName = internalName
        this.displayName = displayName

        SidebarHandler.registerSidebar(getScoreboard())

        GroovyScript.addUnloadHook {
            SidebarHandler.unregisterSidebar(getScoreboard().internalId)
        }
    }

    abstract void setupPlayer(Player player)
    abstract def startMatch(MatchSnapshot snapshot)
    abstract def startRound(MatchSnapshot snapshot)
    abstract def endRound(MatchSnapshot snapshot)
    abstract def endMatch(MatchSnapshot snapshot)

    Sidebar getScoreboard() {
        return new SidebarBuilder("duel_${internalName}").title {player ->
            "§3§l${displayName}"
        }.lines {player ->
            def list = []

            def match = activeMatches.find {it.players.contains(player.uniqueId) }
            if (match == null) list

            list.add("§8§m----------------")
            list.add("")
            list.add("§fYour Ping: §b${player.ping}")
            list.add("")
            list.add("§8§m----------------")

            return list
        }.priority {
            return 2
        }.shouldDisplayTo {player ->
            return activePlayers.containsKey(player.uniqueId)
        }.build()
    }
}
