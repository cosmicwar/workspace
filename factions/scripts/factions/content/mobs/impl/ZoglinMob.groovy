package scripts.factions.content.mobs.impl

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.mojang.serialization.Dynamic
import net.minecraft.sounds.SoundEvent
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.behavior.*
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.monster.Zoglin
import net.minecraft.world.entity.schedule.Activity
import net.minecraft.world.level.Level
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage
import scripts.factions.content.mobs.TickableMob
import scripts.shared.utils.ColorUtil

class ZoglinMob extends Zoglin implements TickableMob {

    String mobCustomName


    ZoglinMob(Level world, String customName = "", float scale = 1.0F) {
        super(EntityType.ZOGLIN, world)

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(50.0D* scale)
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue())
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(12.0D* scale)
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D)
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10.0D* scale)
        this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(1D)
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D)

        this.mobCustomName = customName
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
        mobInactiveTick(this)
    }

    @Override
    void tickMob() {
        mobTick(this)
    }

    @Override
    protected boolean damageEntity0(DamageSource damagesource, float f) {
        boolean dmg = super.damageEntity0(damagesource, f)
        if (dmg) {
            setCustomName(mobCustomName, true)
        }

        return dmg
    }

    @Override
    double getMobHealth() {
        return getHealth()
    }

    @Override
    Mob getMob() {
        return this
    }

    @Override
    void setCustomName(String name, boolean hearts) {
        if (name != "") {
            this.mobCustomName = name

            this.setCustomName(CraftChatMessage.fromStringOrNull(ColorUtil.color("${mobCustomName}${hearts ? " §c${Math.round(getHealth()) as int}§l❤" : ""}")))
            this.setCustomNameVisible(true)
        }
    }

    @Override
    String getMobCustomName() {
        return mobCustomName
    }

    @Override
    boolean isSunBurnTick() {
        return false
    }

    @Override
    SoundEvent getHurtSound() {
        return super.getHurtSound(damageSources().generic())
    }

}
