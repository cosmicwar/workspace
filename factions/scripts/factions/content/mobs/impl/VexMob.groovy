package scripts.factions.content.mobs.impl

import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Vex
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage
import scripts.factions.content.mobs.TickableMob
import scripts.shared.utils.ColorUtil

class VexMob extends Vex implements TickableMob {

    String mobCustomName

    VexMob(Level world, String customName = "", float scale = 1.0F) {
        super(EntityType.VEX, world)

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30.0D)
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue())
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(8.0D)
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.4D)
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D)
        this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(1.2D)
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D)

        this.mobCustomName = customName
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
    void move(MoverType movementType, Vec3 movement) {
        noCulling = false
        super.move(movementType, movement)
    }

    @Override
    boolean isSunBurnTick() {
        return false
    }

    @Override
    SoundEvent getHurtSound() {
        return super.getHurtSound(damageSources().generic())
    }

    private class VexChargeAttackGoal extends Goal {

        VexChargeAttackGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE))
        }

        @Override
        boolean canUse() {
            return VexMob.this.getTarget() != null && !VexMob.this.getMoveControl().hasWanted() && VexMob.this.random.nextInt(reducedTickDelay(7)) == 0 ? VexMob.this.distanceToSqr((Entity) VexMob.this.getTarget()) > 4.0D : false
        }

        @Override
        boolean canContinueToUse() {
            return VexMob.this.getMoveControl().hasWanted() && VexMob.this.isCharging() && VexMob.this.getTarget() != null && VexMob.this.getTarget().isAlive()
        }

        @Override
        void start() {
            LivingEntity entityliving = VexMob.this.getTarget()

            if (entityliving != null) {
                Vec3 vec3d = entityliving.getEyePosition()

                VexMob.this.moveControl.setWantedPosition(vec3d.x, vec3d.y, vec3d.z, 1.0D)
            }

            VexMob.this.setIsCharging(true)
            VexMob.this.playSound(SoundEvents.VEX_CHARGE, 1.0F, 1.0F)
        }

        @Override
        void stop() {
            VexMob.this.setIsCharging(false)
        }

        @Override
        boolean requiresUpdateEveryTick() {
            return true
        }

        @Override
        void tick() {
            LivingEntity entityliving = VexMob.this.getTarget()

            if (entityliving != null) {
                if (VexMob.this.getBoundingBox().intersects(entityliving.getBoundingBox())) {
                    VexMob.this.doHurtTarget(entityliving)
                    VexMob.this.setIsCharging(false)
                } else {
                    double d0 = VexMob.this.distanceToSqr((Entity) entityliving)

                    if (d0 < 9.0D) {
                        Vec3 vec3d = entityliving.getEyePosition()

                        VexMob.this.moveControl.setWantedPosition(vec3d.x, vec3d.y, vec3d.z, 1.0D)
                    }
                }

            }
        }
    }

}
