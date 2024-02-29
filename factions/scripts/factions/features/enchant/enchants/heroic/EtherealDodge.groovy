package scripts.factions.features.enchant.enchants.heroic

import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.struct.HeroicEnchant
import scripts.shared.utils.ItemType

class EtherealDodge extends HeroicEnchant {
    EtherealDodge() {
        super(
                "ethereal_dodge",
                EnchantmentType.PROC,
                "Ethereal Dodge",
                "dodge",
                ["Chance to completely dodge an attack."],
                [ItemType.BOOTS],
                1
        )

        setStackable(false)
        setProcChance(0.12D)
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
