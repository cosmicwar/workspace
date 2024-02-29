package scripts.factions.content.mobs.impl

import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.InteractionHand
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RangedAttackGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Drowned
import net.minecraft.world.entity.monster.RangedAttackMob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage
import scripts.factions.content.mobs.TickableMob
import scripts.shared.utils.ColorUtil

import java.util.concurrent.ThreadLocalRandom

class DrownedMob extends Drowned implements TickableMob {

    String mobCustomName

    DrownedMob(Level world, String customName = "", float scale = 1.0F) {
        super(EntityType.DROWNED, world)

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(50.0D * scale)
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue())
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(8.0D * scale)
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25D)
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D * scale)
        this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(1.2D)
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.1D)

        this.mobCustomName = customName
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT))
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, (Goal) new DrownedTridentAttackGoal(this, 1.0D, 60, 10.0F))
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true))
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player, 0, true, false, null))
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
    void inactiveTick() {
        super.inactiveTick()
        mobInactiveTick(this)
    }

    @Override
    void tickMob() {
        mobTick(this)

        if (ThreadLocalRandom.current().nextInt(200) == 0) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT))
        }
    }

    @Override
    void killMob() {
        die(damageSources().magic())
        ((ServerLevel) level()).getChunkSource().removeEntity(this)
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
        return super.getHurtSound(this.damageSources().generic())
    }

    static class DrownedTridentAttackGoal extends RangedAttackGoal {
        private final Drowned drowned

        DrownedTridentAttackGoal(RangedAttackMob mob, double mobSpeed, int intervalTicks, float maxShootRange) {
            super(mob, mobSpeed, intervalTicks, maxShootRange)
            this.drowned = (Drowned) mob
        }

        @Override
        boolean canUse() {
            return super.canUse() && this.drowned.getMainHandItem().is(Items.TRIDENT)
        }

        @Override
        void start() {
            super.start()
            this.drowned.setAggressive(true)
            this.drowned.startUsingItem(InteractionHand.MAIN_HAND)
        }

        @Override
        void stop() {
            super.stop()
            this.drowned.stopUsingItem()
            this.drowned.setAggressive(false)
        }
    }
}