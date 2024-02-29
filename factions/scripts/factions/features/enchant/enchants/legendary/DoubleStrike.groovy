package scripts.factions.features.enchant.enchants.legendary

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class DoubleStrike extends CustomEnchantment {

    DoubleStrike() {
        super(
                "doublestrike",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.PROC,
                "Double Strike",
                ["Chance to deal double damage"],
                [ItemType.SWORD],
                3
        )

        setProcChance(0.05D)
        setCoolDown(3)
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (target !instanceof Player) return
        if (!proc(player, enchantLevel)) return
        target.damage(2)
        EnchantUtils.scaleDamage(event, 2D)
    }
}
