package scripts.factions.content.entity.npc.navigable.path

import groovy.transform.CompileStatic
import org.bukkit.Location
import org.bukkit.World

@CompileStatic
class NavigablePath extends Path {

    int pointIndex

    NavigablePath(String name, List<PathPoint> pathPoints) {
        super(name, pathPoints)
    }

    NavigablePath reversed() {
        Collections.reverse(pathPoints)
        pathPoints.each {it.yaw = (it.yaw + 180F) % 360F}
        pointIndex = 0
        return this
    }

    Location getNext(World world) {
        pointIndex++
        if (isComplete()) return null

        PathPoint pathPoint = pathPoints.get(pointIndex)
        return new Location(world, pathPoint.x, pathPoint.y, pathPoint.z, pathPoint.yaw, pathPoint.pitch)
    }

    boolean isComplete() {
        return pointIndex >= pathPoints.size()
    }

}
