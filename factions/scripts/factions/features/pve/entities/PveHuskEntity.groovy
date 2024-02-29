package scripts.factions.features.pve.entities

import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Husk
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import scripts.factions.features.pve.PveEntityTools
import scripts.factions.content.mobs.TickableMob

class PveHuskEntity extends Husk implements TickableMob {

    PveHuskEntity(Level world, float scale = 1.0f) {
        super(EntityType.HUSK, world)

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30.0D* scale)
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue())
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(8.0D* scale)
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.4D)
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D* scale)
        this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(1.2D)
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D)
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true))
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player, 0, true, false, null))
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
    boolean isSunBurnTick() {
        return false
    }

    @Override
    SoundEvent getHurtSound() {
        return super.getHurtSound(this.damageSources().generic())
    }

}
