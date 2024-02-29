package scripts.factions.features.enchant.enchants.galactic

import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class Rogue extends CustomEnchantment {

    Rogue() {
        super(
                "rogue",
                EnchantmentTier.GALAXY,
                EnchantmentType.PROC,
                "Rogue",
                ["Do extra damage if hitting your enemy from behind"],
                [ItemType.SWORD],
                3,
                false
        )

        setStackable(false)
        setProcChance(0.15D)
        setCoolDown(10)
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (target !instanceof Player) return

        if (!proc(player, enchantLevel)) return

        def v1 = player.getLocation().getDirection()
        def v2 = target.getLocation().getDirection()
        double relativeAngle = (Math.atan2(v1.getX() * v2.getZ() - v1.getZ() * v2.getX(), v1.getX() * v2.getX() + v1.getZ() * v2.getZ()) * 180) / Math.PI
        if (Math.abs(relativeAngle) > 30D) return

        EnchantUtils.scaleDamage(event, 1D + ((double) enchantLevel / getMaxLevel()))
        player.spawnParticle(Particle.CRIT_MAGIC, target.getLocation().add(0D, 0.75D, 0D), 20)
    }
}
