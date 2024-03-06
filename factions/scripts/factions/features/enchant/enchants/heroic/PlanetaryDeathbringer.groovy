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

class PlanetaryDeathbringer extends HeroicEnchant {

    PlanetaryDeathbringer() {
        super(
                "planetary_deathbringer",
                EnchantmentType.PROC,
                "Planetary Deathbringer",
                "deathbringer",
                ["Chance to deal extra damage."],
                [ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS],
                3
        )

        setStackable(false)
        setProcChance(0.05D)
        setCoolDown(5)

        getConfig().addDefault([
                new DoubleEntry("damageMultiplier", 1.25D, "Damage Multiplier * level")
        ])
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (!proc(player, enchantLevel)) return

        EnchantUtils.scaleDamage(event, getConfig().getDoubleEntry("damageMultiplier").value * enchantLevel)
    }
}
