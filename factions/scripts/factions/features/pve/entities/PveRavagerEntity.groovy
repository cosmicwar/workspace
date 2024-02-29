package scripts.factions.features.pve.entities

import net.minecraft.sounds.SoundEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Ravager
import net.minecraft.world.level.Level
import scripts.factions.features.pve.PveEntityTools
import scripts.factions.content.mobs.TickableMob

class PveRavagerEntity extends Ravager implements TickableMob {

    PveRavagerEntity(Level world, float scale = 1.0f) {
        super(EntityType.RAVAGER, world)

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0D* scale)
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue())
    }

    @Override
    void inactiveTick() {
        super.inactiveTick()
        PveEntityTools.inactiveTick(this)
    }

    @Override
    void tickMob() {
        PveEntityTools.tick(this)
    }

    @Override
    SoundEvent getHurtSound() {
        return super.getHurtSound(DamageSource.GENERIC)
    }
}
