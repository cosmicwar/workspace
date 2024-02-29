package scripts.factions.content.mobs.impl

import groovy.transform.CompileStatic
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Zombie
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage
import scripts.factions.content.mobs.TickableMob
import scripts.shared.utils.ColorUtil

@CompileStatic
class ZombieMob extends Zombie implements TickableMob {

    String mobCustomName

    ZombieMob(Level world, String customName = "", float scale = 1.0F) {
        super(EntityType.ZOMBIE, world)

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(50.0D * scale)
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue())
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(8.0D * scale)
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.4D)
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D * scale)
        this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(1.2D)
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D)

        this.mobCustomName = customName
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true))
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
    boolean isSunBurnTick() {
        return false
    }

    @Override
    SoundEvent getHurtSound() {
        return super.getHurtSound(this.damageSources().generic())
    }
}
