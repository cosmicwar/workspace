package scripts.factions.features.spawners.event

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class MobStackerSpawnMobEvent extends Event {

    private static final HandlerList handlers = new HandlerList()

    final Location location
    final EntityType entityType
    int amount

    MobStackerSpawnMobEvent(Location location, EntityType entityType, int amount) {
        this.location = location.clone()
        this.entityType = entityType
        this.amount = amount
    }

    static HandlerList getHandlerList() {
        return handlers
    }

    HandlerList getHandlers() {
        return handlers
    }

}
