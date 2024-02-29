package scripts.factions.features.pve.entities

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Slime
import net.minecraft.world.level.Level

class NametagSpacerSlime extends Slime {

    NametagSpacerSlime(Level world) {
        super(EntityType.SLIME, world)

        setCustomNameVisible(true)
        setInvisible(true)
        persistentInvisibility = true
        setNoGravity(true)
        setInvulnerable(true)
    }

    @Override
    protected void registerGoals() {}

    @Override
    void inactiveTick() {}

    @Override
    void tick() {}

}
