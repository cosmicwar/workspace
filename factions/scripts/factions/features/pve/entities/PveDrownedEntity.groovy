package scripts.factions.features.pve.entities

import net.minecraft.sounds.SoundEvent
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
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
import scripts.factions.features.pve.PveEntityTools
import scripts.factions.content.mobs.TickableMob

import java.util.concurrent.ThreadLocalRandom

class PveDrownedEntity extends Drowned implements TickableMob {

    PveDrownedEntity(Level world, float scale = 1.0f) {
        super(EntityType.DROWNED, world)

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(50.0D* scale)
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue())
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(8.0D* scale)
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25D)
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D* scale)
        this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(1.2D)
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.1D)
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, (Goal)new DrownedTridentAttackGoal(this, 1.0D, 60, 10.0F))
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true))
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

        if (ThreadLocalRandom.current().nextInt(200) == 0) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT))
        }
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
        private final Drowned drowned;

        public DrownedTridentAttackGoal(RangedAttackMob mob, double mobSpeed, int intervalTicks, float maxShootRange) {
            super(mob, mobSpeed, intervalTicks, maxShootRange);
            this.drowned = (Drowned) mob;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && this.drowned.getMainHandItem().is(Items.TRIDENT);
        }

        @Override
        public void start() {
            super.start();
            this.drowned.setAggressive(true);
            this.drowned.startUsingItem(InteractionHand.MAIN_HAND);
        }

        @Override
        public void stop() {
            super.stop();
            this.drowned.stopUsingItem();
            this.drowned.setAggressive(false);
        }
    }
}