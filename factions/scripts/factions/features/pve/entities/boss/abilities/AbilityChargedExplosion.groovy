package scripts.factions.features.pve.entities.boss.abilities

import net.minecraft.world.entity.Entity
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Creeper
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import scripts.factions.features.pve.entities.boss.Ability

import java.util.concurrent.ThreadLocalRandom

class AbilityChargedExplosion extends Ability {

    static final double MAX_DAMAGE = 60D
    static final double DAMAGE_RADIUS = 4D

    int delay
    int duration

    AbilityChargedExplosion(Entity entity) {
        super(entity)
    }

    @Override
    void start() {
        delay = ThreadLocalRandom.current().nextInt(60, 80)

        Creeper creeper = entity.bukkitEntity as Creeper
        creeper.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, delay, 1), true)
        creeper.setPowered(true)
        entity.bukkitEntity.world.playSound(entity.bukkitEntity.location, Sound.ENTITY_CREEPER_PRIMED, 1F, 1F)
    }

    @Override
    void tick() {
        if (++duration < delay) return

        Creeper creeper = entity.bukkitEntity as Creeper
        creeper.setPowered(false)

        entity.bukkitEntity.location.getNearbyEntitiesByType(Player.class, DAMAGE_RADIUS, DAMAGE_RADIUS, DAMAGE_RADIUS).each {
            it.setFireTicks(ThreadLocalRandom.current().nextInt(60, 80))
            it.damage(MAX_DAMAGE / Math.max(1, it.location.distance(entity.bukkitEntity.location)), creeper)
        }

        entity.bukkitEntity.world.playSound(entity.bukkitEntity.location, Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F)
        entity.bukkitEntity.world.spawnParticle(Particle.EXPLOSION_HUGE, entity.bukkitEntity.location, 0)
    }

    @Override
    void complete() {}

    @Override
    boolean isFinished() {
        return duration >= delay
    }

}
