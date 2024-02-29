package scripts.factions.core.faction.event

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.jetbrains.annotations.NotNull
import scripts.factions.core.faction.data.Faction

class FactionCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList()

    boolean cancelled

    Faction faction
    long time

    FactionCreateEvent(Faction faction, long time) {
        this.faction = faction
        this.time = time
    }

    @NotNull
    HandlerList getHandlers() {
        return handlers
    }

    @NotNull
    static HandlerList getHandlerList() {
        return handlers
    }


}