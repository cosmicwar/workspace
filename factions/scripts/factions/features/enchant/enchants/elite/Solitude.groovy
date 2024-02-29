package scripts.factions.features.enchant.enchants.elite

import com.google.common.collect.Maps
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.enviorment.Exports
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Solitude extends CustomEnchantment {

    Map<UUID, Integer> soulitdeUsers = Maps.newConcurrentMap()

    Solitude() {
        super(
                "solitude",
                EnchantmentTier.ELITE,
                EnchantmentType.NORMAL,
                "Solitude",
                ["todo"],
                [ItemType.SWORD],
                3
        )

        Exports.ptr("ench:solitude:contains", { Player player ->
            return soulitdeUsers.containsKey(player.getUniqueId())
        })

        Exports.ptr("ench:solitude:get", { Player player ->
            return soulitdeUsers.get(player.getUniqueId())
        })
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        soulitdeUsers.put(player.getUniqueId(), enchantLevel)
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        soulitdeUsers.remove(player.getUniqueId())
    }

}
