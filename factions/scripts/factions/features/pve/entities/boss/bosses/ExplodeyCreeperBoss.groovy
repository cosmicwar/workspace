package scripts.factions.features.pve.entities.boss.bosses

import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import org.bukkit.ChatColor
import org.bukkit.Color
import scripts.factions.features.pve.PveEntityTools
import scripts.factions.features.pve.PveMobDifficulty
import scripts.factions.content.mobs.TickableMob
import scripts.factions.features.pve.entities.boss.Ability
import scripts.factions.features.pve.entities.boss.AbilityHandler
import scripts.factions.features.pve.entities.boss.BossType
import scripts.factions.features.pve.entities.boss.PveBoss
import scripts.factions.features.pve.entities.boss.abilities.*

import java.util.concurrent.TimeUnit

class ExplodeyCreeperBoss extends Creeper implements TickableMob, PveBoss {

    static List<Class<? extends Ability>> ABILITIES = [
            AbilityTNTRing.class,
            AbilityExplosionShuffle.class,
            AbilityProjectileTNT.class,
            AbilityChargedExplosion.class,
            AbilityThrowNearby.class
    ]

    final PveMobDifficulty difficulty
    final Map<UUID, Float> damageTracker = new HashMap<>()
    final AbilityHandler abilityHandler

    ExplodeyCreeperBoss(Level world, PveMobDifficulty difficulty, float scale = 1.0f) {
        super(EntityType.CREEPER, world)

        this.difficulty = difficulty
        abilityHandler = new AbilityHandler(this, ABILITIES, TimeUnit.SECONDS.toMillis(isEnraged() ? 2L : 3L), TimeUnit.SECONDS.toMillis(isEnraged() ? 4L : 5L))

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100000D* scale)
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue())
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(6.0D)
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(isEnraged() ? 0.35D : 0.275D)
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(12.0D* scale)
        this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(1D)
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.85D)
        setAggressive(true)
    }

    @Override
    BossType getBossType() {
        return BossType.EXPLODEY_CREEPER
    }

    @Override
    ChatColor getGlowColor() {
        return ChatColor.GREEN
    }

    @Override
    Color getParticleColor() {
        return Color.LIME
    }

    @Override
    boolean isEnraged() {
        return difficulty == PveMobDifficulty.ENRAGED_BOSS
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true))
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player, 0, true, false, null))
    }

    @Override
    protected boolean damageEntity0(DamageSource damagesource, float f) {
        def startingHealth = health
        boolean damaged = super.damageEntity0(damagesource, f)
        if (damaged) {
            float damage = Math.max(0F, startingHealth - this.health) as float
            if (damage > 0F) {
                if (damagesource instanceof DamageSource) {
                    DamageSource attackSource = damagesource as DamageSource
                    def attacker = attackSource.getEntity()
                    if (!(attacker instanceof ServerPlayer)) return damaged
                    damageTracker.merge(attacker.getUUID(), damage, Float::sum)
                }
            }
        }
        return damaged
    }

    @Override
    void inactiveTick() {
        super.inactiveTick()
        PveEntityTools.inactiveTick(this)
    }

    @Override
    void die(DamageSource source) {
        super.die(source)
        abilityHandler?.stop()
    }

    @Override
    void tickMob() {
        PveEntityTools.tick(this)
        abilityHandler?.tick()
    }

    @Override
    SoundEvent getHurtSound() {
        return super.getHurtSound()
    }

}
