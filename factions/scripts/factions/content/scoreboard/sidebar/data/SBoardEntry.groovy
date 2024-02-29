package scripts.factions.content.scoreboard.sidebar.data

import org.bukkit.ChatColor
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team

class SBoardEntry {

    SBoard board

    Team team
    String text, identifier

    SBoardEntry(SBoard board, String text, int position) {
        this.board = board
        this.text = text
        this.identifier = this.board.getUniqueIdentifier(position)

        this.setup()
    }

    /**
     * Setup Board Entry.
     */
    void setup() {
        final Scoreboard scoreboard = this.board.getScoreboard()

        if (scoreboard == null) {
            return
        }

        String teamName = this.identifier

        // This shouldn't happen, but just in case.
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16)
        }

        Team team = scoreboard.getTeam(teamName)

        // Register the team if it does not exist.
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName)
        }

        // Add the entry to the team.
        if (!team.getEntries().contains(this.identifier)) {
            team.addEntry(this.identifier)
        }

        // Add the entry if it does not exist.
        if (!this.board.getEntries().contains(this)) {
            this.board.getEntries().add(this)
        }

        this.team = team
    }

    /**
     * Send Board Entry Update.
     *
     * @param position of entry.
     */
    void send(int position) {
        // Set Prefix & Suffix.
        String[] split = splitTeamText(text)
        this.team.setPrefix(split[0])
        this.team.setSuffix(split[1])

        // Set the score
        this.board.getObjective().getScore(this.identifier).setScore(position)
    }

    /**
     * Remove Board Entry from Board.
     */
    void remove() {
        this.board.getIdentifiers().remove(this.identifier)
        this.board.getScoreboard().resetScores(this.identifier)
    }

    static String[] splitTeamText(String input) {
        final int inputLength = input.length()
        if (inputLength > 64) {
            // Make the prefix the first 16 characters of our text
            String prefix = input.substring(0, 64)

            // Get the last index of the color char in the prefix
            final int lastColorIndex = prefix.lastIndexOf("§")

            String suffix

            if (lastColorIndex >= 63) {
                prefix = prefix.substring(0, lastColorIndex)
                suffix = ChatColor.getLastColors(input.substring(0, 66)) + input.substring(lastColorIndex + 2)
            } else {
                suffix = ChatColor.getLastColors(prefix) + input.substring(16)
            }

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, 16)
            }

            return new String[]{prefix, suffix}
        } else {
            return new String[]{input, ""}
        }
    }

}
