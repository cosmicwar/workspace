package scripts.factions.features.enchant.enchants.ultimate

import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class EnderWalker extends CustomEnchantment {

    EnderWalker() {
        super(
                "enderwalker",
                EnchantmentTier.ULTIMATE,
                EnchantmentType.PROC,
                "Ender Walker",
                ["Has a 5% chance per level to heal you for the damage taken."],
                [ItemType.BOOTS],
                5
        )

        setProcChance(0.05D)
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.WITHER && event.getCause() != EntityDamageEvent.DamageCause.POISON) return;
        if (!proc(player, enchantLevel)) return

        EnchantUtils.heal(player, event.getFinalDamage());
        EnchantUtils.scaleDamage(event, 0D);
        player.spawnParticle(Particle.PORTAL, player.getLocation().add(0D, 1D, 0D), 70);
    }
}
