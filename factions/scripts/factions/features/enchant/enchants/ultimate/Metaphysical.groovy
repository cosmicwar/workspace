package scripts.factions.features.enchant.enchants.ultimate

import com.google.common.collect.Sets
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.enviorment.Exports
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

import java.util.concurrent.ConcurrentHashMap

class Metaphysical extends CustomEnchantment {
    static Map<UUID, Integer> equipedMetaphysical = new ConcurrentHashMap<>()

    Metaphysical() {
        super(
                "metaphysical",
                EnchantmentTier.ULTIMATE,
                EnchantmentType.PROC,
                "Metaphysical",
                ["Negates most enchants that cause slowness with high probability."],
                [ItemType.BOOTS],
                4
        )

        setProcChance(0.24D)

        Exports.ptr("ench:metaphysical:contains", { Player player ->
            return equipedMetaphysical.containsKey(player.getUniqueId())
        })

        Exports.ptr("ench:metaphysical:get", { Player player ->
            return equipedMetaphysical.get(player.getUniqueId())
        })

        Exports.ptr("ench:metaphysical:proc", { Player player ->
            return proc(player, equipedMetaphysical.get(player.getUniqueId()))
        })

        Exports.ptr("ench:metaphysical:sendMessage", { Player player ->
            player.sendMessage("§e  ** Metaphysical (Slow negated) **")
        })
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        equipedMetaphysical.put(player.getUniqueId(), enchantLevel)
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        equipedMetaphysical.remove(player.getUniqueId())
    }
}
