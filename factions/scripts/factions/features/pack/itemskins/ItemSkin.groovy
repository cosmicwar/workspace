package scripts.factions.features.pack.itemskins

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
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
import org.bukkit.inventory.ItemStack
import scripts.factions.features.pack.itemskins.utils.ItemSkinType

abstract class ItemSkin {

    final String internalName

    String displayName
    List<String> description
    ItemSkinType applicability
    int modelId
    int armorEquippedId

    Map<String, ?> config
    
    ItemSkin(String internalName) {
        this.internalName = internalName
    }

    Map<String, ?> getConfig() {
        return config == null ? config = Exports.ptr("skinconfig")["skins"][internalName] as Map<String, ?> : config
    }

    void reload() {
        config = null

        try {
            Map<String, ?> config = getConfig()
            displayName = config["displayName"] as String
            description = config["description"] as List<String>
            applicability = ItemSkinType.valueOf(config["applicability"] as String)
            modelId = config["modelId"] as int
            armorEquippedId = (config["armorEquippedId"] ?: modelId ) as int
        } catch (Exception e) {
            Starlight.log.info("Error reloading ${this.internalName}")
            e.printStackTrace()
        }

        this.onReload()
    }

    void onReload() {}

    void onEquip(Player player, ItemStack itemStack) {}

    void onUnequip(Player player, ItemStack itemStack) {}

    void onAttack(Player player, ItemStack itemStack, LivingEntity target, EntityDamageByEntityEvent event) {}

    void onKill(Player player, ItemStack itemStack, LivingEntity target, EntityDeathEvent event) {}

    void onBossKill(Player player, ItemStack itemStack, LivingEntity target) {}

    void onDamaged(Player player, ItemStack itemStack, Entity attacker, EntityDamageByEntityEvent event) {}

    void onEnvironmentDamaged(Player player, ItemStack itemStack, EntityDamageEvent.DamageCause damageCause, EntityDamageEvent event) {}

    void onProjectileLaunch(Player player, ItemStack itemStack, Projectile projectile, ProjectileLaunchEvent event) {}

    void onBowShoot(Player player, ItemStack itemStack, Projectile projectile, EntityShootBowEvent event) {}

    void onBlockDamage(Player player, ItemStack itemStack, Block block, BlockDamageEvent event) {}

    void onBlockBreak(Player player, ItemStack itemStack, Block block, BlockBreakEvent event) {}

    void onBlockBreakMonitor(Player player, ItemStack itemStack, Block block, BlockBreakEvent event) {}


}

