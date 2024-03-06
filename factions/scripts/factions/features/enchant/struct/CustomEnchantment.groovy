package scripts.factions.features.enchant.struct

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.jetbrains.annotations.NotNull
import scripts.shared.core.cfg.ConfigCategory
import scripts.shared.core.cfg.RegularConfig
import scripts.shared.core.cfg.entries.BooleanEntry
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.shared.core.cfg.entries.IntEntry
import scripts.shared.data.string.StringDataManager
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.utils.EnchantmentCooldownUtils
import scripts.factions.features.enchant.utils.SoulUtils
import scripts.shared.legacy.ToggleUtils
import scripts.shared.utils.ItemType

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

abstract class CustomEnchantment {

    final String internalName
    final EnchantmentTier enchantmentTier
    final EnchantmentType enchantmentType

    EnchantPriority priority = EnchantPriority.NORMAL
    String displayName
    List<String> description
    List<ItemType> applicability

    boolean stackable

    RegularConfig config

    CustomEnchantment(String internalName, EnchantmentTier enchantmentTier, EnchantmentType enchantmentType, String displayName = "", List<String> description = ["empty"], List<ItemType> applicability = [], int maxLevel = 1, boolean stackable = false, EnchantPriority priority = EnchantPriority.NORMAL) {
        this.internalName = internalName
        this.enchantmentTier = enchantmentTier
        this.enchantmentType = enchantmentType

        this.displayName = displayName
        this.description = description
        this.applicability = applicability

        getConfig().addDefault([
                new IntEntry("maxLevel", maxLevel),
                new BooleanEntry("stackable", stackable),
        ])

        if (enchantmentType.isProc()) {
            getConfig().addDefault([
                    new DoubleEntry("procChanceCalc", 0.0D),
                    new IntEntry("cooldown", -1)
            ])
        }

        if (enchantmentTier == EnchantmentTier.SOUL) {
            getConfig().addDefault([
                    new IntEntry("consumption", 5) //consumption is amount of souls consumed per sec or per proc if a proccable enchant
            ])
        }

        Enchantments.enchantConfig.queueSave()
    }

    boolean isSoul() {
        return enchantmentTier == EnchantmentTier.SOUL
    }

    int getMaxLevel() {
        return getConfig().getIntEntry("maxLevel").value
    }

    def setMaxLevel(int value) {
        getConfig().getIntEntry("maxLevel").value = value

        Enchantments.enchantConfig.queueSave()
    }

    boolean isStackable() {
        return getConfig().getBooleanEntry("stackable").value
    }

    def setStackable(boolean value) {
        getConfig().getBooleanEntry("stackable").value = value

        Enchantments.enchantConfig.queueSave()
    }

    RegularConfig getConfig() {
        if (config == null) {
            config = getCategory().getOrCreateConfig(this.internalName, this.displayName)
        }

        return config
    }

    ConfigCategory getCategory() {
        return Enchantments.enchantConfig.getOrCreateCategory(this.enchantmentTier.tierName, "${this.enchantmentTier.tierColor + this.enchantmentTier.tierName} Enchantments", this.enchantmentTier.glassPane)
    }

    def setProcChance(double value) {
        if (!enchantmentType.isProc()) return // ???
        getConfig().getDoubleEntry("procChanceCalc").value = value

        Enchantments.enchantConfig.queueSave()
    }

    double getProcChance(Player player, int enchantLevel) {
        if (!enchantmentType.isProc()) return -1.0D // ???
        return getConfig().getDoubleEntry("procChanceCalc").value * enchantLevel // add support for better proc chances for players?
    }

    def setCoolDown(int value) {
        if (!enchantmentType.isProc()) return // ???
        getConfig().getIntEntry("cooldown").value = value

        Enchantments.enchantConfig.queueSave()
    }

    int getCoolDown() {
        if (!enchantmentType.isProc()) return -1 // ???
        return getConfig().getIntEntry("cooldown").value
    }

    boolean proc(Player player, int enchantLevel) {
        if (!enchantmentType.isProc()) {
            return false // ???
        }

        boolean proc = ThreadLocalRandom.current().nextDouble() < getProcChance(player, enchantLevel)

        def cooldown = getConfig().getIntEntry("cooldown").value

        if (cooldown > 0 && proc) {
            long cooldownTime = TimeUnit.SECONDS.toMillis(cooldown)
            if (EnchantmentCooldownUtils.get(player.getUniqueId(), getInternalName(), cooldownTime) > 0) {
                if (ThreadLocalRandom.current().nextDouble() > getRandomBypassChance(enchantLevel)) return false //5% cooldown bypass chance
            } else {
                EnchantmentCooldownUtils.set(player, getInternalName())
            }
        }

        return proc
    }

    int getConsumption() {
        if (!isSoul()) return -1 // ???
        return getConfig().getIntEntry("consumption").value
    }

    def setConsumption(int value) {
        if (!isSoul()) return // ???
        getConfig().getIntEntry("consumption").value = value

        Enchantments.enchantConfig.queueSave()
    }

    boolean consumeSouls(Player player, boolean force = false) {
//        if (!isSoul()) return false // ???
        return SoulUtils.consumeSouls(player, getConsumption(), force)
    }

    boolean canBeAppliedTo(@NotNull ItemStack itemStack) {
        return getApplicability().stream().anyMatch(applicability -> applicability.isType(itemStack))
    }

    void addPotionEffect(Player player, PotionEffectType potionEffectType, int amplifier) {
        (Exports.ptr("potionEffects:addEquippedEffect") as Closure)?.call(player, potionEffectType, amplifier as Integer)
        Players.msg(player, "§a§l[+] §a${getDisplayName()}: §7${potionEffectType.getName()} ${amplifier + 1}")
    }

    void removePotionEffect(Player player, PotionEffectType potionEffectType, int amplifier) {
        (Exports.ptr("potionEffects:removeEquippedEffect") as Closure)?.call(player, potionEffectType, amplifier as Integer)
        Players.msg(player, "§c§l[-] §c${getDisplayName()}: §7${potionEffectType.getName()} ${amplifier + 1}")
    }

    void addPotionWithDuration(Player player, PotionEffectType potionEffectType, int amplifier, int duration) {
        (Exports.ptr("potionEffects:addPotionWithDuration") as Closure)?.call(player, potionEffectType, amplifier as Integer, duration as Integer)
    }

    void sendParticle(Player player, Location location, Particle particle, int amount) {
        player.spawnParticle(particle, location, amount)
    }

    <T> void sendParticle(Player player, Location location, Particle particle, int amount, T data) {
        player.spawnParticle(particle, location, amount, data)
    }

    void sendParticle(Player origin = null, Set<Player> players, Location location, Particle particle, int amount) {
        Schedulers.async().run {
            players.each { player ->
                if (origin != null)
                {
                    if (origin == player)
                    {
                        if (ToggleUtils.hasToggled(player, "enchant_particles") || ToggleUtils.hasToggled(player, "enchant_particles_self"))
                        {
                            player.spawnParticle(particle, location, amount)
                        }
                    }
                    else
                    {
                        if (ToggleUtils.hasToggled(player, "enchant_particles", origin.getUniqueId()) || ToggleUtils.hasToggled(player, "enchant_particles_others", origin.getUniqueId()))
                        {
                            player.spawnParticle(particle, location, amount)
                        }
                    }
                }
                else
                {
                    if (ToggleUtils.hasToggled(player, "enchant_particles") || ToggleUtils.hasToggled(player, "enchant_particles_others"))
                    {
                        player.spawnParticle(particle, location, amount)
                    }
                }
            }
        }
    }

    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {}

    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {}

    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {}

    void onKill(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDeathEvent event) {}

    void onBossKill(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target) {}

    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {}

    void onKnockback(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityKnockbackByEntityEvent event) {}

    void onKnockedback(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityKnockbackByEntityEvent event) {}

    void onEnvironmentDamaged(Player player, ItemStack itemStack, int enchantLevel, EntityDamageEvent.DamageCause damageCause, EntityDamageEvent event) {}

    void onProjectileLaunch(Player player, ItemStack itemStack, int enchantLevel, Projectile projectile, ProjectileLaunchEvent event) {}

    void onBowShoot(Player player, ItemStack itemStack, int enchantLevel, Projectile projectile, EntityShootBowEvent event) {}

    void onBlockDamage(Player player, ItemStack itemStack, int enchantLevel, Block block, BlockDamageEvent event) {}

    void onBlockBreak(Player player, ItemStack itemStack, int enchantLevel, Block block, BlockBreakEvent event) {}

    void onBlockBreakMonitor(Player player, ItemStack itemStack, int enchantLevel, Block block, BlockBreakEvent event) {}

    double getRandomBypassChance(int enchantLevel) {
        return 0.005 * enchantLevel
    }

}

