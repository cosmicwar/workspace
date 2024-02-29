package scripts.factions.features.enchant.enchants.simple


import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType


class Obliterate extends CustomEnchantment {

    Obliterate() {
        super(
                "obliterate",
                EnchantmentTier.SIMPLE,
                EnchantmentType.PROC,
                "Obliterate",
                ["Chance to knock your target", "away from you"],
                [ItemType.SWORD],
                5
        )

        setProcChance(0.1D)

        getConfig().addDefault([
                new DoubleEntry("baseKbFactor", 1.8D),
                new DoubleEntry("additionalKbPerLvL", 0.5D)
        ])

        Enchantments.enchantConfig.queueSave()
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (!(target instanceof Player) && !proc(player, enchantLevel)) return
        target = (Player) target
        target.spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation(), 1)

        double factor = getConfig().getDoubleEntry("baseKbFactor").value + (getConfig().getDoubleEntry("additionalKbPerLvL").value * enchantLevel)
        Vector vector = target.getLocation().toVector().subtract(player.getLocation().toVector()) as Vector
        vector.normalize();
        target.setVelocity(vector * factor);
    }
}
