package scripts.factions.features.enchant.enchants.legendary

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class Deathbringer extends CustomEnchantment {

    Deathbringer() {
        super(
                "deathbringer",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.PROC,
                "Deathbringer",
                ["Chance to deal extra damage"],
                [ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS],
                3
        )

        setStackable(false)
        setProcChance(0.03D)
        setCoolDown(2)

        getConfig().addDefault([
                new DoubleEntry("damageMultiplier", 1.25D, "Damage Multiplier * level")
        ])
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (!proc(player, enchantLevel)) return

        EnchantUtils.scaleDamage(event, getConfig().getDoubleEntry("damageMultiplier").value * enchantLevel)
    }
}
