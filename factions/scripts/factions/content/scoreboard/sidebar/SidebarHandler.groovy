package scripts.factions.content.scoreboard.sidebar

import com.google.common.collect.Maps
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.metadata.Metadata
import org.starcade.starlight.helper.metadata.MetadataKey
import org.starcade.starlight.helper.metadata.MetadataMap
import scripts.factions.content.scoreboard.SCUtils
import scripts.factions.content.scoreboard.sidebar.data.SBoard
import scripts.factions.content.scoreboard.sidebar.data.SBoardEntry
import scripts.shared.utils.BukkitUtils
import scripts.shared.utils.ColorUtil

import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer

@CompileStatic(TypeCheckingMode.SKIP)
class SidebarHandler {

    private static MetadataKey<SBoard> SCOREBOARD_KEY = MetadataKey.create("scoreboard_key", SBoard.class)

    static Map<String, Sidebar> sidebars = Maps.newConcurrentMap()

    SidebarHandler() {

        GroovyScript.addUnloadHook {
            BukkitUtils.getOnlineNonSpoofPlayers().each {
                MetadataMap metadata = Metadata.provideForPlayer(it)
                SBoard board = metadata.getOrNull(SCOREBOARD_KEY)
                if (board != null) {
                    board.getEntries().forEach { it.remove() }
                    board.getEntries().clear()
                    board.getObjective().unregister()
                    board.getScoreboard().clearSlot(DisplaySlot.SIDEBAR)
                }

                metadata.remove(SCOREBOARD_KEY)
            }
        }

        Schedulers.sync().runRepeating({
            for (Player player : BukkitUtils.getOnlineNonSpoofPlayers()) {
                Sidebar sBar = getActiveSidebar(player)

                if (sBar == null || sBar.internalId == null) {
                    continue
                }

                MetadataMap metadata = Metadata.provideForPlayer(player)
                SBoard board = metadata.getOrNull(SCOREBOARD_KEY)
                if (board == null) {
                    board = new SBoard(player)
                    Metadata.provideForPlayer(player).put(SCOREBOARD_KEY, board)
                }
                createSidebar(sBar).accept(player, board)
            }

        }, 5L, TimeUnit.MILLISECONDS, 500L, TimeUnit.MILLISECONDS)

    }

    static boolean registerSidebar(Sidebar sidebar) {
        if (sidebar == null) return false

        if (sidebars.containsKey(sidebar.internalId)) {
            def cached = sidebars.get(sidebar.internalId)
            if (cached == sidebar) return false
        }

        sidebars.put(sidebar.internalId, sidebar)
        return true
    }

    static Sidebar getSidebar(String internalId) {
        return sidebars.get(internalId)
    }

    static boolean unregisterSidebar(String internalId) {
        if (!sidebars.containsKey(internalId)) return false

        def sidebar = sidebars.remove(internalId)
        if (sidebar != null) {
            // handle removing viewer data
        }
        return true
    }


    static def createSidebar(Sidebar sBar) {
        BiConsumer<Player, SBoard> updater = { Player p, SBoard board ->
            if (board == null) return

            def scoreboard = board.getScoreboard()
            def objective = board.getObjective()

            if (scoreboard == null || objective == null) return

            Component title = Component.text(ColorUtil.color(sBar.title(p)))

            if (objective.displayName() != title) {
                objective.displayName(title)
            }

            List<String> newLines = SCUtils.makeLinesUnique(sBar.getLines(p))

            if (newLines == null || newLines.isEmpty()) {
                board.getEntries().forEach { it.remove() }
                board.getEntries().clear()
            } else {
                newLines = newLines.reverse()

                // Remove excessive amount of board entries.
                if (board.getEntries().size() > newLines.size()) {
                    for (int i = newLines.size(); i < board.getEntries().size(); i++) {
                        SBoardEntry entry = board.getEntryAtPosition(i)

                        if (entry != null) {
                            entry.remove()
                        }
                    }
                }

                // Update existing entries.
                for (int i = 0; i < newLines.size(); i++) {
                    String line = newLines.get(i)
                    SBoardEntry entry = board.getEntryAtPosition(i)

                    if (entry == null) {
                        entry = new SBoardEntry(board, line, i)
                        entry.setup()
                    }

                    entry.setText(ColorUtil.color(line))
                    entry.setup()
                    entry.send(i)
                }

                if (p.getScoreboard() != scoreboard) {
                    p.setScoreboard(scoreboard);
                }
            }
        }

        return updater
    }

    static Sidebar getActiveSidebar(Player player) {
        return sidebars.values().findAll { it.shouldDisplayTo(player) }.sort { -it.priority() }.find()
    }

}
