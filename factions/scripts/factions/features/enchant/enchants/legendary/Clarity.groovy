package scripts.factions.features.enchant.enchants.legendary

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.enviorment.Exports
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

import java.util.concurrent.ConcurrentHashMap

class Clarity extends CustomEnchantment {

    public static Map<UUID, Integer> clarityPlayers = new ConcurrentHashMap<UUID, Integer>()

    Clarity() {
        super(
                "clarity",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.NORMAL,
                "Clarity",
                ["Protects against blindness and nausea."],
                [ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS],
                3
        )

        Exports.ptr("ench:clarity:containsPlayer", { Player player ->
            return clarityPlayers.containsKey(player.getUniqueId())
        })

        Exports.ptr("ench:clarity:getLevel", { Player player ->
            return clarityPlayers.get(player.getUniqueId())
        })
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        clarityPlayers.put(player.getUniqueId(), enchantLevel)
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        clarityPlayers.remove(player.getUniqueId())
    }
}
