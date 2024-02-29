package scripts.factions.features.pve.entities.boss

import net.minecraft.world.entity.Entity

abstract class Ability {

    Entity entity

    Ability(Entity entity) {
        this.entity = entity
    }

    abstract void start()
    abstract void tick()
    abstract void complete()
    abstract boolean isFinished()

}