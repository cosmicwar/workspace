package scripts.factions.features.spawners.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDeathEvent
import org.jetbrains.annotations.NotNull

class StackedMobKillEvent extends Event {

    private static final HandlerList handlers = new HandlerList()

    final EntityDeathEvent originalEvent
    int stackSize
    int amountKilled

    StackedMobKillEvent(@NotNull EntityDeathEvent originalEvent, int stackSize, int amountKilled) {
        this.originalEvent = originalEvent
        this.stackSize = stackSize
        this.amountKilled = amountKilled
    }

    static HandlerList getHandlerList() {
        return handlers
    }

    HandlerList getHandlers() {
        return handlers
    }

}
