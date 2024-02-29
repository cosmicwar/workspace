package scripts.factions.features.enchant.enchants.ultimate

import org.starcade.starlight.helper.Schedulers
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

import java.util.concurrent.ConcurrentHashMap

class Implants extends CustomEnchantment{

    Map<Player, Integer> equipped = new ConcurrentHashMap<>()

    Implants() {
        super(
                "implants",
                EnchantmentTier.ULTIMATE,
                EnchantmentType.NORMAL,
                "Implants",
                ["Feeds you every 5 seconds."],
                [ItemType.HELMET],
                3
        )

        Schedulers.async().runRepeating(() -> {
            equipped.forEach((player, enchantLevel) -> {
                if (player.isDead()) return
                for (i in enchantLevel) {
                    EnchantUtils.heal(player, 1D)
                    if (player.getFoodLevel() < 20) player.setFoodLevel(player.getFoodLevel() + 1)
                }
            })
        }, 20L, 100L)
    }

    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        equipped.put(player, enchantLevel)
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        equipped.remove(player)
    }

}
