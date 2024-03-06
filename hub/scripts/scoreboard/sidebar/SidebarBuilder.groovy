package scripts.scoreboard.sidebar

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.entity.Player

import java.util.function.Function

@CompileStatic(TypeCheckingMode.SKIP)
class SidebarBuilder extends Sidebar {

    Function<Player, String> _title = { Player player -> return "~999~"}
    Function<Player, List<String>> _lines = { Player player -> return ["~ovo~"] }
    Function<Player, Boolean> _shouldDisplayTo = { Player player -> return true }

    Closure<Integer> _priority = { return 0 }

    SidebarBuilder(String internalId) {
        super(internalId)
    }

    SidebarBuilder title(Function<Player, String> function) {
        _title = function
        return this
    }

    SidebarBuilder lines(Function<Player, List<String>> function) {
        _lines = function
        return this
    }

    SidebarBuilder priority(Closure<Integer> closure) {
        _priority = closure
        return this
    }

    SidebarBuilder shouldDisplayTo(Function<Player, Boolean> function) {
        _shouldDisplayTo = function
        return this
    }

    Sidebar build() {
        return this
    }

    @Override
    String title(Player player) {
        return _title.apply(player)
    }

    @Override
    List<String> getLines(Player player) {
        return _lines.apply(player)
    }

    @Override
    int priority() {
        return _priority.call()
    }

    @Override
    boolean shouldDisplayTo(Player player) {
        return _shouldDisplayTo.apply(player)
    }

    @Override
    boolean registerSidebar() {
        return SidebarHandler.registerSidebar(this)
    }
}


