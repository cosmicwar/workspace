package scripts.factions.features.enchant.enchants.legendary


import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.EnchantListener
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

/**
 * If you are taking damage from more than {max-attackers} enemies in a short period, the damage from any additional
 * enemies beyond that initial group will be halved
 */

class Aegis extends CustomEnchantment {

    Aegis() {
        super(
                "aegis",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.NORMAL,
                "Aegis",
                ["Reduces damage taken from multiple enemies"],
                [ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS],
                6
        )
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (!(attacker instanceof Player)) return

        if (EnchantListener.getRecentDamagers(player, 2000L, 8 - enchantLevel).contains(attacker.getUniqueId())) return

        EnchantUtils.scaleDamage(event, 0.5D)
    }
}
