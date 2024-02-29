package scripts.factions.content.mobs.impl

import net.minecraft.sounds.SoundEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Ravager
import net.minecraft.world.level.Level
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage
import scripts.factions.content.mobs.TickableMob
import scripts.shared.utils.ColorUtil

class RavagerMob extends Ravager implements TickableMob {

    String mobCustomName

    RavagerMob(Level world, String customName = "", float scale = 1.0F) {
        super(EntityType.RAVAGER, world)

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0D * scale)
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue())

        this.mobCustomName = customName
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
        return this.getHealth()
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
    SoundEvent getHurtSound() {
        return super.getHurtSound(DamageSource.GENERIC)
    }
}
