package scripts.factions.features.pve.entities

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.mojang.serialization.Dynamic
import net.minecraft.sounds.SoundEvent
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.behavior.*
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.monster.Zoglin
import net.minecraft.world.entity.schedule.Activity
import net.minecraft.world.level.Level
import scripts.factions.features.pve.PveEntityTools
import scripts.factions.content.mobs.TickableMob

class PveZoglinEntity extends Zoglin implements TickableMob {

    PveZoglinEntity(Level world, float scale = 1.0f) {
        super(EntityType.ZOGLIN, world)

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(50.0D* scale)
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue())
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(12.0D* scale)
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D)
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10.0D* scale)
        this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(1D)
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D)
    }


    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<Zoglin> brain = this.brainProvider().makeBrain(dynamic);
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(new StartAttacking(Zoglin::findNearestValidAttackTarget), new RunSometimes(new SetEntityLookTarget(8.0F), UniformInt.of(30, 60)), new RunOne(ImmutableList.of(Pair.of(new RandomStroll(0.4F), 2), Pair.of(new SetWalkTargetFromLookTarget(0.4F, 3), 2), Pair.of(new DoNothing(30, 60), 1)))));
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F), new RunIf(Zoglin::isAdult, new MeleeAttack(40)), new RunIf(Zoglin::isBaby, new MeleeAttack(15)), new StopAttackingIfTargetInvalid()), MemoryModuleType.ATTACK_TARGET);

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
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
        return super.getHurtSound(DamageSource.GENERIC)
    }

}
