package scripts.factions.features.enchant.enchants.ultimate

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class Valor extends CustomEnchantment {

    Valor() {
        super(
                "valor",
                EnchantmentTier.ULTIMATE,
                EnchantmentType.NORMAL,
                "Valor",
                ["Reduces damage taken by 1.25% per level."],
                [ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS],
                5,
                true
        )

        getConfig().addDefault([
                new DoubleEntry("dmgReductionPerLvL", 0.0125D)
        ])

        Enchantments.enchantConfig.queueSave()
    }

    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (attacker !instanceof Player || event.getFinalDamage() <= 0D) return
        Player attackingPlayer = (Player) attacker
        if (!EnchantUtils.isSword(attackingPlayer.getInventory().getItemInMainHand()))
            EnchantUtils.scaleDamage(event, 1D - (getConfig().getDoubleEntry("dmgReductionPerLvL").value * enchantLevel))
    }
}
