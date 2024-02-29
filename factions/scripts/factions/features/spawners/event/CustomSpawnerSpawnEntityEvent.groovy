package scripts.factions.features.spawners.event

import org.bukkit.Location
import org.bukkit.block.CreatureSpawner
import org.bukkit.entity.EntityType
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.jetbrains.annotations.NotNull

class CustomSpawnerSpawnEntityEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList()

    boolean cancelled

    CreatureSpawner spawner
    Location location
    EntityType type
    int spawnMulti

    CustomSpawnerSpawnEntityEvent(CreatureSpawner spawner, Location location, EntityType type, int spawnMulti) {
        this.spawner = spawner
        this.location = location
        this.type = type
        this.spawnMulti = spawnMulti
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
