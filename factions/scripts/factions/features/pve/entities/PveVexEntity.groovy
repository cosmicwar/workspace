package scripts.factions.features.pve.entities

import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Vex
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import scripts.factions.features.pve.PveEntityTools
import scripts.factions.content.mobs.TickableMob

class PveVexEntity extends Vex implements TickableMob {

    PveVexEntity(Level world, float scale = 1.0f) {
        super(EntityType.VEX, world)

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30.0D)
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue())
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(8.0D)
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.4D)
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D)
        this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(1.2D)
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D)
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this))
        this.goalSelector.addGoal(4, new VexChargeAttackGoal())
//        this.goalSelector.addGoal(8, new d())
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player, 0, true, false, null))
    }

    @Override
    void inactiveTick() {
        super.inactiveTick()
        PveEntityTools.inactiveTick(this)
    }

    @Override
    public void move(MoverType movementType, Vec3 movement) {
        noCulling = false
        super.move(movementType, movement);
    }

    @Override
    void tickMob() {
        PveEntityTools.tick(this)
        this.setNoGravity(true)
    }

    @Override
    boolean isSunBurnTick() {
        return false
    }

    @Override
    SoundEvent getHurtSound() {
        return super.getHurtSound(DamageSource.GENERIC)
    }

    private class VexChargeAttackGoal extends Goal {

        public VexChargeAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return PveVexEntity.this.getTarget() != null && !PveVexEntity.this.getMoveControl().hasWanted() && PveVexEntity.this.random.nextInt(reducedTickDelay(7)) == 0 ? PveVexEntity.this.distanceToSqr((Entity) PveVexEntity.this.getTarget()) > 4.0D : false;
        }

        @Override
        public boolean canContinueToUse() {
            return PveVexEntity.this.getMoveControl().hasWanted() && PveVexEntity.this.isCharging() && PveVexEntity.this.getTarget() != null && PveVexEntity.this.getTarget().isAlive();
        }

        @Override
        public void start() {
            LivingEntity entityliving = PveVexEntity.this.getTarget();

            if (entityliving != null) {
                Vec3 vec3d = entityliving.getEyePosition();

                PveVexEntity.this.moveControl.setWantedPosition(vec3d.x, vec3d.y, vec3d.z, 1.0D);
            }

            PveVexEntity.this.setIsCharging(true);
            PveVexEntity.this.playSound(SoundEvents.VEX_CHARGE, 1.0F, 1.0F);
        }

        @Override
        public void stop() {
            PveVexEntity.this.setIsCharging(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity entityliving = PveVexEntity.this.getTarget();

            if (entityliving != null) {
                if (PveVexEntity.this.getBoundingBox().intersects(entityliving.getBoundingBox())) {
                    PveVexEntity.this.doHurtTarget(entityliving);
                    PveVexEntity.this.setIsCharging(false);
                } else {
                    double d0 = PveVexEntity.this.distanceToSqr((Entity) entityliving);

                    if (d0 < 9.0D) {
                        Vec3 vec3d = entityliving.getEyePosition();

                        PveVexEntity.this.moveControl.setWantedPosition(vec3d.x, vec3d.y, vec3d.z, 1.0D);
                    }
                }

            }
        }
    }

}
