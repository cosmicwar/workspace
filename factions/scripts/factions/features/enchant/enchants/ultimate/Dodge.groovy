package scripts.factions.features.enchant.enchants.ultimate

import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Dodge extends CustomEnchantment {

    Dodge() {
        super(
                "dodge",
                EnchantmentTier.ULTIMATE,
                EnchantmentType.PROC,
                "Dodge",
                ["Chance to completely dodge an attack"],
                [ItemType.BOOTS],
                5,
                false
        )

        setStackable(true)
        setProcChance(0.03D)
        setCoolDown(3)
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (!(attacker instanceof Player)) return
        if (!proc(player, enchantLevel)) return

        event.setDamage(0D);

        player.sendMessage("  §e* DODGE *")
        player.spawnParticle(Particle.CLOUD, player.getLocation().add(0D, 1D, 0D), 10)
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 0.75F);
    }
}
