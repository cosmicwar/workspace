package scripts.factions.features.pve.entities.boss.abilities

import net.minecraft.world.entity.Entity
import org.bukkit.Particle
import scripts.factions.features.pve.entities.boss.Ability

class AbilityFireColumns extends Ability {

    static final int MAX_DURATION = 200

    int duration

    AbilityFireColumns(Entity entity) {
        super(entity)
    }

    @Override
    void start() {

    }

    @Override
    void tick() {
        if (++duration >= MAX_DURATION || duration % 10 != 0) return

        entity.bukkitEntity.world.spawnParticle(Particle.SOUL_FIRE_FLAME, entity.bukkitEntity.location, 0)
    }

    @Override
    void complete() {}

    @Override
    boolean isFinished() {
        return duration >= MAX_DURATION
    }

}
