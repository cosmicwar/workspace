package scripts.factions.features.pve.entities.boss.bosses

import com.destroystokyo.paper.event.entity.WitchThrowPotionEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.RangedAttackGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Witch
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ThrownPotion
import net.minecraft.world.entity.raid.Raider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack
import scripts.factions.features.pve.PveEntityTools
import scripts.factions.features.pve.PveMobDifficulty
import scripts.factions.content.mobs.TickableMob
import scripts.factions.features.pve.entities.boss.Ability
import scripts.factions.features.pve.entities.boss.AbilityHandler
import scripts.factions.features.pve.entities.boss.BossType
import scripts.factions.features.pve.entities.boss.PveBoss
import scripts.factions.features.pve.entities.boss.abilities.AbilityLingeringPools
import scripts.factions.features.pve.entities.boss.abilities.AbilityPotionRain
import scripts.factions.features.pve.entities.boss.abilities.AbilityThrowNearby

import java.util.concurrent.TimeUnit

class EstrangedWitchBoss extends Witch implements TickableMob, PveBoss {

    static List<Class<? extends Ability>> ABILITIES = [
            AbilityThrowNearby.class,
            AbilityPotionRain.class,
            AbilityLingeringPools.class
    ]

    final PveMobDifficulty difficulty
    final Map<UUID, Float> damageTracker = new HashMap<>()
    final AbilityHandler abilityHandler

    RangedAttackGoal attackGoal

    EstrangedWitchBoss(Level world, PveMobDifficulty difficulty, float scale = 1.0f) {
        super(EntityType.WITCH, world)

        this.difficulty = difficulty
        abilityHandler = new AbilityHandler(this, ABILITIES, TimeUnit.SECONDS.toMillis(isEnraged() ? 4L : 5L), TimeUnit.SECONDS.toMillis(isEnraged() ? 6L : 8L))

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100000D * scale)
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue())
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(6.0D)
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(isEnraged() ? 0.3D : 0.275D)
        this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(1D)
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.85D)
        setAggressive(true)

        if (isEnraged()) {
            this.goalSelector.removeGoal(attackGoal)
            attackGoal = new RangedAttackGoal(this, 1.0D, 30, 16.0F)
            this.goalSelector.addGoal(1, attackGoal)
        }
    }

    @Override
    BossType getBossType() {
        return BossType.ESTRANGED_WITCH
    }

    @Override
    Color getParticleColor() {
        return Color.MAROON
    }

    @Override
    ChatColor getGlowColor() {
        return ChatColor.DARK_PURPLE
    }

    @Override
    boolean isEnraged() {
        return difficulty == PveMobDifficulty.ENRAGED_BOSS
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, attackGoal = new RangedAttackGoal(this, 1.0D, 40, 10.0F))
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
        PveEntityTools.inactiveTick(this)
    }

    @Override
    SoundEvent getHurtSound() {
        return super.getHurtSound()
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
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        if (!this.isDrinkingPotion()) {
            Vec3 vec3d = target.getDeltaMovement();
            double d0 = target.getX() + vec3d.x - this.getX();
            double d1 = target.getEyeY() - 1.100000023841858D - this.getY();
            double d2 = target.getZ() + vec3d.z - this.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            Potion potionregistry = Potions.HARMING;

            if (target instanceof Raider) {
                if (target.getHealth() <= 4.0F) {
                    potionregistry = Potions.HEALING;
                } else {
                    potionregistry = Potions.REGENERATION;
                }

                this.setTarget((LivingEntity) null);
            } else if (d3 >= 8.0D && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                potionregistry = Potions.SLOWNESS;
            } else if (target.getHealth() >= 8.0F && !target.hasEffect(MobEffects.POISON)) {
                potionregistry = Potions.POISON;
            } else if (d3 <= 3.0D && !target.hasEffect(MobEffects.WEAKNESS) && this.random.nextFloat() < 0.25F) {
                potionregistry = Potions.WEAKNESS;
            }

            // Paper start
            ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potionregistry);
            WitchThrowPotionEvent event = new WitchThrowPotionEvent((org.bukkit.entity.Witch) this.getBukkitEntity(), (org.bukkit.entity.LivingEntity) target.getBukkitEntity(), CraftItemStack.asCraftMirror(potion));
            if (!event.callEvent()) {
                return;
            }
            potion = CraftItemStack.asNMSCopy(event.getPotion());
            ThrownPotion entitypotion = new ThrownPotion(this.level, this);
            entitypotion.setItem(potion);
            // Paper end
            entitypotion.setXRot((entitypotion.getXRot() - -20.0F).toFloat());
            entitypotion.shoot(d0, d1 + d3 * 0.2D, d2, 0.75F, 8.0F);
            if (!this.isSilent()) {
                this.level.playSound((Player) null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, (0.8F + this.random.nextFloat() * 0.4F).toFloat());
            }

            this.level.addFreshEntity(entitypotion);
        }
    }
}
