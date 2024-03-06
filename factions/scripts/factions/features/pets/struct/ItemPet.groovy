package scripts.factions.features.pets.struct

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.enviorment.GroovyScript
import scripts.shared.core.cfg.RegularConfig
import scripts.shared.core.cfg.entries.BooleanEntry
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.shared.core.cfg.entries.StringEntry
import scripts.shared.core.cfg.entries.list.StringListEntry
import scripts.factions.features.pets.ItemPets

import java.util.concurrent.TimeUnit

abstract class ItemPet {

    String internalName

    RegularConfig config

    ItemPet(String internalName,
            String displayName,
            List<String> description = [],
            String skullTexture,
            double maxLevel = 100.0D,
            double cooldown = 0.0D
    ) {
        this.internalName = internalName

        GroovyScript.addUnloadHook {
            ItemPets.registeredPets.remove(internalName)
        }

        getConfig().addDefault([
                new BooleanEntry("enabled", true),
                new BooleanEntry("debug", false),

                new StringEntry("displayName", displayName),
                new StringEntry("skullTexture", skullTexture),

                new StringListEntry("description", description),

                new DoubleEntry("maxLevel", maxLevel),
                new DoubleEntry("cooldown", cooldown),
                new DoubleEntry("defaultXpIncrease", 0.1D),
                new DoubleEntry("xpDecreasePerLevel", 0.005D),
        ])

        ItemPets.petsConfig.queueSave()

        ItemPets.registeredPets.put(internalName, this)
    }

    boolean isEnabled() {
        return getConfig().getBooleanEntry("enabled").value
    }

    boolean isDebug() {
        return getConfig().getBooleanEntry("debug").value
    }

    String getDisplayName() {
        return getConfig().getStringEntry("displayName").value
    }

    String getSkullTexture() {
        return getConfig().getStringEntry("skullTexture").value
    }

    List<String> getDescription() {
        return getConfig().getStringListEntry("description").value
    }

    double getMaxLevel() {
        return getConfig().getDoubleEntry("maxLevel").value
    }

    double getCooldown() {
        return getConfig().getDoubleEntry("cooldown").value
    }

    double getDefaultXpIncrease() {
        return getConfig().getDoubleEntry("defaultXpIncrease").value
    }

    double getXpDecreasePerLevel() {
        return getConfig().getDoubleEntry("xpDecreasePerLevel").value
    }

    RegularConfig getConfig() {
        return config == null ? config = ItemPets.petsCategory.getOrCreateConfig(internalName, internalName) : config
    }

    def setCooldown(ItemStack stack) {
        if (cooldown >= 0.0D) {
            def data = ItemPetData.read(stack)
            if (data == null) return

            data.cdExpiration = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(getCooldown().toInteger())
            data.write(stack)
        }
    }

    static def setCooldown(ItemStack stack, long time) {
        def data = ItemPetData.read(stack)
        if (data == null) return

        data.cdExpiration = time
        data.write(stack)
    }

    static def resetCooldown(ItemStack stack) {
        def data = ItemPetData.read(stack)
        if (data == null) return

        data.cdExpiration = -1L
        data.write(stack)
    }

    static Long getCooldown(ItemStack stack) {
        def data = ItemPetData.read(stack)
        if (data == null) return 0L

        return data.cdExpiration
    }

    static boolean onCooldown(ItemStack stack) {
        def data = ItemPetData.read(stack)
        if (data == null) return false

        return data.cdExpiration > System.currentTimeMillis()
    }

    static def incrementLevel(ItemStack stack, double amount) {
        def data = ItemPetData.read(stack)
        if (data == null) return

        data.level += amount
        data.write(stack)
        ItemPets.updatePetItem(stack)
    }

    void onHold(Player player, ItemStack stack, double petLevel) {}
    void onUnHold(Player player, ItemStack stack, double petLevel) {}
    void onInteract(Player player, double petLevel, PlayerInteractEvent event) {}
    void onAttack(Player player, LivingEntity target, double petLevel, EntityDamageByEntityEvent event) {}
    void onKill(Player player, LivingEntity target, double petLevel, EntityDeathEvent event) {}
    void onDamaged(Player player, Entity attacker, double petLevel, EntityDamageByEntityEvent event) {}
    void onKnockback(Player player, LivingEntity target, double petLevel, EntityKnockbackByEntityEvent event) {}
    void onKnockedback(Player player, Entity attacker, double petLevel, EntityKnockbackByEntityEvent event) {}
    void onEnvironmentDamaged(Player player, EntityDamageEvent.DamageCause damageCause, double petLevel, EntityDamageEvent event) {}
    void onProjectileLaunch(Player player, Projectile projectile, double petLevel, ProjectileLaunchEvent event) {}
    void onBowShoot(Player player, Projectile projectile, double petLevel, EntityShootBowEvent event) {}
    void onBlockDamage(Player player, Block block, double petLevel, BlockDamageEvent event) {}
    void onBlockBreak(Player player, Block block, double petLevel, BlockBreakEvent event) {}
    void onBlockBreakMonitor(Player player, Block block, double petLevel, BlockBreakEvent event) {}

}
