package scripts.factions.content.entity.npc.navigable.path

import groovy.transform.CompileStatic

@CompileStatic
class Path {

    String name
    List<PathPoint> pathPoints

    Path(String name, List<PathPoint> pathPoints) {
        this.name = name
        this.pathPoints = pathPoints
    }

    NavigablePath asNavigablePath(int offsetX, int offsetZ) {
        return new NavigablePath(name, pathPoints.findResults {new PathPoint(it.x + offsetX, it.y, it.z + offsetZ, it.yaw, it.pitch) } as List<PathPoint>)
    }

}

