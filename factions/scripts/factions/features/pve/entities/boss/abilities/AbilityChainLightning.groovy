package scripts.factions.features.pve.entities.boss.abilities

import com.google.common.collect.Sets
import net.minecraft.world.entity.Entity
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Creature
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import scripts.factions.features.pve.entities.boss.Ability

class AbilityChainLightning extends Ability {

    static final int MAX_DURATION = 200

    Player target
    Set<UUID> hoppedPlayers = Sets.newHashSet()
    int duration

    AbilityChainLightning(Entity entity) {
        super(entity)
    }

    @Override
    void start() {
        LivingEntity target = (entity.bukkitEntity as Creature).getTarget()
        if (target == null || !(target instanceof Player)) {
            target = entity.bukkitEntity.location.getNearbyEntitiesByType(Player.class, 32, 4 ,32).find()
        }

        this.target = target as Player
    }

    @Override
    void tick() {
        if (++duration >= MAX_DURATION || duration % 10 != 0) return

        if (target == null || target.world != entity.level().getWorld()) return

        hoppedPlayers.add(target.uniqueId)
        lightning(target.location)
        hop()
    }

    @Override
    void complete() {}

    @Override
    boolean isFinished() {
        return target == null || duration >= MAX_DURATION
    }

    void hop() {
        target = target.location.getNearbyEntitiesByType(Player.class, 6, 4 ,6).find{it != target && !hoppedPlayers.contains(it.uniqueId)}
    }

    void lightning(Location location) {
        entity.level().getWorld().spigot().strikeLightningEffect(location, true)
        entity.level().getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1F, 1F)

        location.getNearbyEntitiesByType(Player.class, 4, 4 ,4).each {
            it.damage(15D, entity.bukkitEntity)
            it.setFireTicks(60)
            entity.level().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, it.location.add(0, 0.5, 0), 1)
        }
    }

}
