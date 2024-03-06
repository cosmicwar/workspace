package scripts.factions.features.enchant.enchants.heroic

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.struct.HeroicEnchant
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class ShadowAssassin extends HeroicEnchant {
    ShadowAssassin() {
        super(
                "shadow_assassin",
                EnchantmentType.NORMAL,
                "Shadow Assassin",
                "assassin",
                ["Deal more damage the closer you are to your oponent."],
                [ItemType.SWORD],
                5
        )
    }


    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (player.getWorld() != target.getWorld()) return;

        double distance = player.getLocation().distanceSquared(target.getLocation());
        double damageModifier = 0D;
        if (distance < 1.5D) {
            double scaledDistance = (1.5D - Math.max(0.25D, distance)) / 1.25D;
            damageModifier += (scaledDistance * 0.01D);
        }

        if (damageModifier == 0D) return;

        damageModifier *= enchantLevel;
        EnchantUtils.scaleDamage(event, 1D + damageModifier);
    }
}
