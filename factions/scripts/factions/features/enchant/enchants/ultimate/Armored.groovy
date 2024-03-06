package scripts.factions.features.enchant.enchants.ultimate

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType


class Armored extends CustomEnchantment {

    Armored() {
        super(
                "armored",
                EnchantmentTier.ULTIMATE,
                EnchantmentType.NORMAL,
                "Armored",
                ["Reduces damage taken by 1.85% per level."],
                [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                5,
                true
        )

        getConfig().addDefault([
                new DoubleEntry("dmgReductionPerLvL", 0.0185D)
        ])

        Enchantments.enchantConfig.queueSave()
    }
    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (attacker == null || !EnchantUtils.isSword(itemStack) || event.getFinalDamage() <= 0D) {
            return;
        }

        EnchantUtils.scaleDamage(event, 1D - getConfig().getDoubleEntry("dmgReductionPerLvL").value * enchantLevel);
    }
}
