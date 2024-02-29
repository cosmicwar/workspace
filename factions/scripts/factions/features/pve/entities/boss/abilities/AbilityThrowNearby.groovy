package scripts.factions.features.pve.entities.boss.abilities

import net.minecraft.world.entity.Entity
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import scripts.factions.features.pve.entities.boss.Ability
import scripts.factions.features.pve.entities.boss.PveBoss

class AbilityThrowNearby extends Ability {

    static final int MAX_DURATION = 20
    int duration

    AbilityThrowNearby(Entity entity) {
        super(entity)
    }

    @Override
    void start() {
        entity.bukkitEntity.world.playSound(entity.bukkitEntity.location, Sound.ENTITY_IRON_GOLEM_REPAIR, 1F, 1F)
    }

    @Override
    void tick() {
        if (++duration >= MAX_DURATION) return

        double radius = 2 + (duration / 5)
        double height = 0.2 + (duration / 10)
        for (int i = 0; i < 360; i += 360 / 20) {
            double angle = (i * Math.PI / 180)
            double x = radius * Math.cos(angle)
            double z = radius * Math.sin(angle)

            Location location = entity.bukkitEntity.location.add(x, height, z)
            entity.level().getWorld().spawnParticle(Particle.REDSTONE, location, 1, new Particle.DustOptions(entity instanceof PveBoss ? (entity as PveBoss).getParticleColor() : Color.OLIVE, 3))
        }

        if (duration == (MAX_DURATION / 4) as int) {
            Location entityLoc = entity.bukkitEntity.location
            entity.level().getWorld().getNearbyEntitiesByType(Player.class, entityLoc, 6).each {
                Location loc = it.getLocation()
                Vector v = new Vector(loc.getX() - entityLoc.getX(), 0, loc.getZ() - entityLoc.getZ())
                if (v.length() > 0D) {
                    it.setVelocity(v.normalize().setY(0.8D))
                } else {
                    it.setVelocity(new Vector(0D, 0.8D, 0D))
                }

                it.damage(15, entity.bukkitEntity)
                it.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1))
            }
        }
    }

    @Override
    void complete() {}

    @Override
    boolean isFinished() {
        return duration >= MAX_DURATION
    }

}
