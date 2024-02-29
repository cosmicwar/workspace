package scripts.factions.features.pve.entities.boss

import net.minecraft.world.entity.Entity
import scripts.shared.legacy.utils.RandomUtils

import java.util.concurrent.ThreadLocalRandom

class AbilityHandler {

    final List<Class<? extends Ability>> abilities
    Entity entity
    final long minIntervalMillis
    final long maxIntervalMillis

    long nextAbility
    Ability activeAbility

    AbilityHandler(Entity entity, List<Class<? extends Ability>> abilities, long minIntervalMillis, long maxIntervalMillis) {
        this.abilities = abilities
        this.entity = entity
        this.minIntervalMillis = minIntervalMillis
        this.maxIntervalMillis = maxIntervalMillis

        this.nextAbility = System.currentTimeMillis() + minIntervalMillis
    }

    void tick() {
        long now = System.currentTimeMillis()
        if (activeAbility) {
            if (activeAbility.isFinished()) {
                activeAbility.complete()
                activeAbility = null
                nextAbility = now + ThreadLocalRandom.current().nextLong(minIntervalMillis, maxIntervalMillis)
                return
            }

            activeAbility.tick()
            return
        }

        if (now < nextAbility) return

        activeAbility = RandomUtils.getRandom(getAbilities()).getDeclaredConstructor(Entity.class)?.newInstance(entity) as Ability
        activeAbility.start()
    }

    void stop() {
        activeAbility?.complete()
        activeAbility = null
        entity = null
    }

}
