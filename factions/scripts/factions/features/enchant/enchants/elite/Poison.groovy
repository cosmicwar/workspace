package scripts.factions.features.enchant.enchants.elite

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Poison extends CustomEnchantment {

    Poison() {
        super(
                "poison",
                EnchantmentTier.ELITE,
                EnchantmentType.PROC,
                "Poison",
                ["Chance to poison your opponent", "for a short duration"],
                [ItemType.SWORD],
                3
        )

        setStackable(false)
        setProcChance(0.03D)
        setCoolDown(3)
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event)
    {
        if (target !instanceof Player) return

        if (proc(player, enchantLevel))
        {
            addPotionWithDuration(target, PotionEffectType.POISON, (enchantLevel > 2) ? 1 : 0, 2 * enchantLevel * 20)
        }
    }
}
