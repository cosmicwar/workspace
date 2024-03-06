package scripts.scoreboard.sidebar

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import org.bukkit.entity.Player

@CompileStatic
abstract class Sidebar {

    String internalId

    Set<UUID> viewers = Sets.newConcurrentHashSet()

    Sidebar() {
    }

    Sidebar(String internalId) {
        this.internalId = internalId
    }

    abstract String title(Player player)
    abstract List<String> getLines(Player player)
    abstract int priority()
    abstract boolean shouldDisplayTo(Player player)
    abstract boolean registerSidebar()

    boolean registerViewer(Player player) {
        if (this.viewers.contains(player.getUniqueId())) return false

        this.viewers.add(player.getUniqueId())
        return true
    }

    boolean isViewer(Player player) {
        return this.viewers.contains(player.getUniqueId())
    }


}

