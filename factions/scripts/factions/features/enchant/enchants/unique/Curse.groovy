package scripts.factions.features.enchant.enchants.unique

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Curse extends CustomEnchantment {
    Curse() {
        super(
                "curse",
                EnchantmentTier.UNIQUE,
                EnchantmentType.NORMAL,
                "Curse",
                ["Buffs and slows at low health"],
                [ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS],
                5,
                true
        )

        setCoolDown(10)
    }

    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (!proc(player, enchantLevel)) return

        if (player.getHealth() - event.getFinalDamage() > 5D) return;

        addPotionWithDuration(player, PotionEffectType.INCREASE_DAMAGE, enchantLevel * 30, 1)
        addPotionWithDuration(player, PotionEffectType.DAMAGE_RESISTANCE, enchantLevel * 30, 1)
        addPotionWithDuration(player, PotionEffectType.SLOW, enchantLevel * 30, 1)
    }
}
