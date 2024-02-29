package scripts.factions.features.pve.entities.boss.abilities

import net.minecraft.world.entity.Entity
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.TNTPrimed
import org.bukkit.util.Vector
import scripts.factions.features.pve.entities.boss.Ability

import java.util.concurrent.ThreadLocalRandom

class AbilityTNTRing extends Ability {

    AbilityTNTRing(Entity entity) {
        super(entity)
    }

    @Override
    void start() {
        Location entityLoc = entity.bukkitEntity.location
        entityLoc.world.playSound(entityLoc, Sound.ENTITY_CREEPER_DEATH, 1F, 1F)
        for (int i = 0; i < 360; i += 360 / 10) {
            double angle = (i * Math.PI / 180)
            double x = 2 * Math.cos(angle)
            double z = 2 * Math.sin(angle)

            Location location = entityLoc.clone().add(x, 0.5, z)
            TNTPrimed tntPrimed = location.world.spawn(location, TNTPrimed.class) as TNTPrimed

            Vector v = new Vector(location.getX() - entityLoc.getX(), 0, location.getZ() - entityLoc.getZ()).normalize().multiply(0.25)
            v.setY(ThreadLocalRandom.current().nextDouble(0.5))
            tntPrimed.setVelocity(v)
        }
    }

    @Override
    void tick() {}

    @Override
    void complete() {}

    @Override
    boolean isFinished() {
        return true
    }

}
