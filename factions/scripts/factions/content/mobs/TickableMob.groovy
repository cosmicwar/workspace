package scripts.factions.content.mobs

import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.monster.Slime

interface TickableMob {

    void tickMob()

    double getMobHealth()

    Mob getMob()

    void setCustomName(String name, boolean hearts)

    default SoundEvent getHurtSound() {
        return null
    }

    default String getMobCustomName() {
        return ""
    }

    default void mobInactiveTick(Mob mob) {
        mob.goalSelector.inactiveTick(1, true)
        if (mob.targetSelector.inactiveTick(1, true)) {
            mob.targetSelector.tick()
        }
    }

    default void killMob() {
        getMob()?.die(getMob()?.damageSources()?.magic())
        ((ServerLevel) getMob()?.level()).getChunkSource().removeEntity(getMob())
    }

    default void mobTick(Mob mob) {
        if (mob.dead) return

        mob.getSensing().tick()
        mob.targetSelector.tick()
        mob.goalSelector.tick()
        mob.getNavigation().tick()
        mob.tick()
        mob.aiStep()
        mob.getMoveControl().tick()
        mob.getLookControl().tick()
        mob.getJumpControl().tick()

        collide(mob)
    }

    default void collide(Mob mob) {
        mob.level().getEntities(mob, mob.getBoundingBox(), EntitySelector.pushableBy(mob)).findAll { !(it instanceof Slime) }.each { it ->
            Entity otherEntity = it
            double d0 = mob.getX() - otherEntity.getX()
            double d1 = mob.getZ() - otherEntity.getZ()
            double d2 = Mth.absMax(d0, d1)

            if (d2 >= 0.009999999776482582D) {
                d2 = Math.sqrt(d2)
                d0 /= d2
                d1 /= d2
                double d3 = 1.0D / d2

                if (d3 > 1.0D) {
                    d3 = 1.0D
                }

                d0 *= d3
                d1 *= d3
                d0 *= 0.05000000074505806D
                d1 *= 0.05000000074505806D

                if (!mob.isVehicle()) {
                    mob.push(d0, 0.0D, d1)
                }
            }
        }
    }

}
