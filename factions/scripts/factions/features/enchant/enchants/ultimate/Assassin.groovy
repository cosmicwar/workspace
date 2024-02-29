package scripts.factions.features.enchant.enchants.ultimate

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class Assassin extends CustomEnchantment {

    Assassin() {
        super(
                "assassin",
                EnchantmentTier.ULTIMATE,
                EnchantmentType.NORMAL,
                "Assassin",
                ["Deal more damage the closer you are to your enemy, but less if you are further away."],
                [ItemType.SWORD],
                5
        )
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (player.getWorld() != target.getWorld()) return;

        double distance = player.getLocation().distanceSquared(target.getLocation());
        double damageModifier = 0D;
        if (distance > 2D) {
            damageModifier -= (Math.min(3D, distance) - 2D) * 0.025D;
        } else if (distance < 1.5D) {
            double scaledDistance = (1.5D - Math.max(0.25D, distance)) / 1.25D;
            damageModifier += (scaledDistance * 0.01D);
        }

        if (damageModifier == 0D) return;

        damageModifier *= enchantLevel;
        EnchantUtils.scaleDamage(event, 1D + damageModifier);
    }
}
