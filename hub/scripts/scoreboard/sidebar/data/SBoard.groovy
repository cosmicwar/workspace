package scripts.scoreboard.sidebar.data

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

@CompileStatic(TypeCheckingMode.SKIP)
class SBoard {

    UUID playerId

    List<SBoardEntry> entries = new ArrayList<>()
    List<String> identifiers = new ArrayList<>()

    SBoard(Player player) {
        this.playerId = player.uniqueId

        player.setScoreboard(getScoreboard())
        getObjective()
    }

    Scoreboard getScoreboard() {
        def player = Bukkit.getPlayer(playerId)

        if (player.getScoreboard() != Bukkit.getScoreboardManager().getMainScoreboard()) {
            return player.getScoreboard()
        } else {
            return Bukkit.getScoreboardManager().getNewScoreboard()
        }
    }

    SBoardEntry getEntryAtPosition(int position) {
        return position >= this.entries.size() ? null : entries.get(position)
    }

    String getUniqueIdentifier(int position) {
        String identifier = getRandomChatColor(position) + ChatColor.WHITE

        while (this.identifiers.contains(identifier)) {
            identifier = identifier + getRandomChatColor(position) + ChatColor.WHITE
        }

        // This is rare, but just in case, make the method recursive
        if (identifier.length() > 16) {
            return this.getUniqueIdentifier(position)
        }

        // Add our identifier to the list so there are no duplicates
        this.identifiers.add(identifier)

        return identifier
    }

    Objective getObjective() {
        def objective = getScoreboard().getObjective("sb")
        if (objective == null) {
            objective = getScoreboard().registerNewObjective("sb", "dummy")
            objective.setDisplaySlot(DisplaySlot.SIDEBAR)
            objective.setDisplayName("temp - title")
        }

        return objective
    }

    /**
     * Gets a ChatColor based off the position in the collection.
     *
     * @param position of entry.
     * @return ChatColor adjacent to position.
     */
    private static String getRandomChatColor(int position) {
        return ChatColor.values()[position].toString()
    }
}
