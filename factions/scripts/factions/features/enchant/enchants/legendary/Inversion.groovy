package scripts.factions.features.enchant.enchants.legendary

import org.bukkit.Sound
import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

import java.util.concurrent.ThreadLocalRandom

class Inversion extends CustomEnchantment {

    Inversion() {
        super(
                "inversion",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.PROC,
                "Inversion",
                ["Chance to heal yourself", "when you are attacked"],
                [ItemType.SWORD],
                4
        )

        setProcChance(0.04D)
        setCoolDown(3)
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (!(attacker instanceof Player) || !proc(player, enchantLevel)) return;

        EnchantUtils.heal(player, ThreadLocalRandom.current().nextInt(1, 5));
        event.setDamage(0D);
        event.setCancelled(true);
        player.spawnParticle(Particle.SPELL, player.getLocation().add(0D, 1D, 0D), 20)
        player.playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.8F, 1f);
    }
}
