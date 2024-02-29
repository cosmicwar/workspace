package scripts.factions.features.enchant.enchants.legendary


import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.EnchantListener
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class AntiGank extends CustomEnchantment {

    AntiGank() {
        super(
                "antigank",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.NORMAL,
                "Anti-Gank",
                ["Reduces damage taken from multiple enemies"],
                [ItemType.CHESTPLATE],
                6,
                false
        )
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (!(target instanceof Player)) return

        if (EnchantListener.getRecentDamagerCount(player, 6000L) < 6 - enchantLevel) return

        EnchantUtils.scaleDamage(event, 1D + (0.5D / enchantLevel))
    }
}
