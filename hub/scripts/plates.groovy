package scripts

import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector

Events.subscribe(PlayerMoveEvent.class, EventPriority.HIGHEST).filter(EventFilters.ignoreCancelled()).handler { event ->
    Player player = event.getPlayer()

    if (event.getTo().getBlock().getType() != Material.STONE_PRESSURE_PLATE) {
        Vector velocity = player.getVelocity()

        if (velocity.getY() > -0.07 && event.getFrom().distance(event.getTo()) > 2) {
            velocity = velocity.add(player.getLocation().getDirection().setY(0))
            player.setVelocity(handleModifiers(velocity, false))
        }
    } else {
        player.setVelocity(handleModifiers(player.getLocation().getDirection()))
    }
}

static Vector handleModifiers(Vector velocity, boolean handleY = true) {
    double nerfer = 0.5D
    double minXZ = 2.0D
    double minY = 1.0D

    if (handleY && velocity.getY() < minY) {
       velocity.setY(minY)
    }
    if (Math.abs(velocity.getX()) > Math.abs(velocity.getZ())) {
        if (Math.abs(velocity.getX()) < minXZ) {
            velocity.setX(minXZ * Math.abs(velocity.getX()) / velocity.getX())
        }
        velocity.setZ(velocity.getZ() * nerfer)
    } else {
        if (Math.abs(velocity.getZ()) < minXZ) {
            velocity.setZ(minXZ * Math.abs(velocity.getZ()) / velocity.getZ())
        }
        velocity.setX(velocity.getX() * nerfer)
    }
    double max = 5.0D

    if (Math.abs(velocity.getX()) > max) {
        velocity.setX(max * Math.abs(velocity.getX()) / velocity.getX())
    }
    if (Math.abs(velocity.getY()) > max) {
        velocity.setY(max * Math.abs(velocity.getY()) / velocity.getY())
    }
    if (Math.abs(velocity.getZ()) > max) {
        velocity.setZ(max * Math.abs(velocity.getZ()) / velocity.getZ())
    }
    return velocity
}