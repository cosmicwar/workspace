package scripts.factions.features.enchant.enchants.ultimate

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType


class Enrage extends CustomEnchantment {

    Enrage() {
        super(
                "enrage",
                EnchantmentTier.ULTIMATE,
                EnchantmentType.NORMAL,
                "Enrage",
                ["Deal more damage the more health you are missing."],
                [ItemType.SWORD],
                3
        )
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (!(target instanceof Player) || event.getFinalDamage() <= 0D) return;

        double hpLostPercent = 1D - (player.getHealth() / player.getMaxHealth());
        hpLostPercent *= ((double) enchantLevel / (double) getMaxLevel());
        hpLostPercent /= 2D;

        EnchantUtils.scaleDamage(event, 1D + hpLostPercent);
    }
}
