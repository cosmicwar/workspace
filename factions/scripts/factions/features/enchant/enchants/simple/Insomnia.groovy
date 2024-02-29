package scripts.factions.features.enchant.enchants.simple

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class Insomnia extends CustomEnchantment {

    Insomnia() {
        super(
                "insomnia",
                EnchantmentTier.SIMPLE,
                EnchantmentType.PROC,
                "Insomnia",
                ["Chance to slow your target", "and deal extra damage"],
                [ItemType.SWORD],
                7
        )

        setProcChance(0.025D)

        getConfig().addDefault([
                new DoubleEntry("damageMultiplierPerLevel", 0.05D)
        ])

        Enchantments.enchantConfig.queueSave()
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (target !instanceof Player) return
        if (!proc(player, enchantLevel)) return

        int durationTicks = enchantLevel * 20

        addPotionWithDuration(target, PotionEffectType.SLOW_DIGGING, Math.min(2, enchantLevel-1), durationTicks)
//        if (enchantLevel > 4) {
//            addPotionWithDuration(target, PotionEffectType.SLOW, 2, 20 * (enchantLevel - 2))
//        }

        double multiplier = 1D + (enchantLevel * getConfig().getDoubleEntry("damageMultiplierPerLevel").value)
        EnchantUtils.scaleDamage(event, multiplier);
    }
}
