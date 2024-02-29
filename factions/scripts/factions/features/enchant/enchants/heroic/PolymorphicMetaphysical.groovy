package scripts.factions.features.enchant.enchants.heroic

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.enviorment.Exports
import scripts.factions.features.enchant.enchants.ultimate.Metaphysical
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.struct.HeroicEnchant
import scripts.shared.utils.ItemType

import java.util.concurrent.ConcurrentHashMap

class PolymorphicMetaphysical extends HeroicEnchant {

    static Map<UUID, Integer> equipedPMetaphysical = new ConcurrentHashMap<>()


    PolymorphicMetaphysical() {
        super(
                "polymorphic_metaphysical",
                EnchantmentType.NORMAL,
                "Polymorphic Metaphysical",
                "metaphysical",
                ["Negates all slows from enchants with a very high success rate."],
                [ItemType.BOOTS],
                4
        )

        setProcChance(0.24D)

        Exports.ptr("ench:pmetaphysical:contains", { Player player ->
            return equipedPMetaphysical.containsKey(player.getUniqueId())
        })

        Exports.ptr("ench:pmetaphysical:get", { Player player ->
            return equipedPMetaphysical.get(player.getUniqueId())
        })

        Exports.ptr("ench:pmetaphysical:proc", { Player player ->
            return proc(player, equipedPMetaphysical.get(player.getUniqueId()))
        })

        Exports.ptr("ench:pmetaphysical:sendMessage", { Player player ->
            player.sendMessage("§e  ** Metaphysical (Slow negated) **")
        })
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        equipedPMetaphysical.put(player.getUniqueId(), enchantLevel)
        Metaphysical.equipedMetaphysical.put(player.getUniqueId(), enchantLevel)
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        equipedPMetaphysical.remove(player.getUniqueId())
        Metaphysical.equipedMetaphysical.remove(player.getUniqueId())
    }
}
