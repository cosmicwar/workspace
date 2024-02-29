package scripts.factions.features.enchant.utils

import groovy.transform.CompileStatic
import org.bukkit.util.Vector

@CompileStatic
abstract class MovableEntity {

    double motX, motY, motZ
    float yaw, pitch

    void addMotion(double x, double y, double z) {
        motX += x
        motY += y
        motZ += z
    }

    abstract void move()

    void setDirection(Vector direction) {
        setYaw(Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ())) as float)
        setPitch(Math.toDegrees(Math.atan(-direction.getY() / Math.sqrt(Math.pow(direction.getX(), 2) + Math.pow(direction.getZ(), 2)) as double)) as float)
    }

}

