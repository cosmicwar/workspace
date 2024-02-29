package scripts.factions.content.entity.npc.navigable.path

import groovy.transform.CompileStatic
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.starcade.starlight.Starlight

@CompileStatic
class NavigationPathRecorder extends BukkitRunnable {

    final Player player
    final String name
    final Closure callback
    final Location initialLocation
    boolean recording = false
    List<PathPoint> pathPoints = new ArrayList<>()

    NavigationPathRecorder(Player player, String name, Closure callback) {
        this.player = player
        this.name = name
        this.callback = callback
        this.initialLocation = player.location

        pathPoints.add(PathPoint.fromLocation(initialLocation))
    }

    void start(long pollingInterval) {
        runTaskTimer(Starlight.plugin, pollingInterval, pollingInterval)
    }

    void end() {
        cancel()
        callback.call(new Path(name, pathPoints))
    }

    @Override
    void run() {
        if (!player.isOnline() || player.isSneaking()) {
            end()
            return
        }

        Location currentLocation = player.getLocation()
        if (!recording) {
            recording = currentLocation.distanceSquared(initialLocation) > 0.1D
        }

        if (!recording) return

        pathPoints.add(PathPoint.fromLocation(currentLocation))
    }

}