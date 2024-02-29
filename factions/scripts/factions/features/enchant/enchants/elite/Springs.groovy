package scripts.factions.features.enchant.enchants.elite

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Springs extends CustomEnchantment
{

    Springs()
    {
        super(
                "springs",
                EnchantmentTier.ELITE,
                EnchantmentType.NORMAL,
                "Springs",
                ["Receive Jump Boost when equipped."],
                [ItemType.BOOTS],
                3
        )
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel)
    {
        addPotionEffect(player, PotionEffectType.JUMP, enchantLevel - 1)
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel)
    {
        removePotionEffect(player, PotionEffectType.JUMP, enchantLevel - 1)
    }
}

