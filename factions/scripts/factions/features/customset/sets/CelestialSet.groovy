package scripts.factions.features.customset.sets

import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import scripts.shared.core.cfg.entries.DoubleEntry

import scripts.factions.features.customset.struct.CustomSet
import scripts.factions.features.customset.struct.CustomSetWeapon
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.legacy.utils.BroadcastUtils

class CelestialSet extends CustomSet {

    CelestialSet() {
        super("celestial",
                "Celestial",
                [
                        "",
                        "§<bold>§<primaryColor>CELESTIAL SET BONUS",
                        "§<primaryColor>Deal §<#5FB8C8>15% more §<primaryColor>damage to all enemies.",
                        "§<primaryColor>Take §<#5FB8C8>10% less §<primaryColor>incoming damage.",
                        "",
                        "§<bold>§<rainbow>Celestial Surge Ability Effect",
                ],
                "§<bold>§<rainbow>CELESTIAL SET EQUIPPED",
                "§<bold>§<rainbow>CELESTIAL SET REMOVED",
                "Crown",
                "Robe",
                "Guards",
                "Soles",
                ["#7f98ce", "#0b8cc3"],
                [" §<primaryColor>§l* §<primaryColor>§<#5FB8C8>10% more §<primaryColor>damage to all enemies."]
        )

        this.getConfig().addDefault([
                new DoubleEntry("damageModifier", 0.15D),
                new DoubleEntry("incomingDamageModifier", .10D),

                new DoubleEntry("weaponDamageModifier", .10D),
        ])

        this.getCrystalConfig().addDefault([
                new DoubleEntry("crystalDamageModifier", .05D),
        ])

        this.weapon = new CustomSetWeapon(this, Material.NETHERITE_SWORD, "Blade",                 [
                "",
                "§<bold>§<primaryColor>CELESTIAL SET BONUS",
                "§<primaryColor>Deal §<#5FB8C8>15% more §<primaryColor>damage to all enemies.",
                "§<primaryColor>Take §<#5FB8C8>10% less §<primaryColor>incoming damage.",
                "",
                "§<bold>§<rainbow>Celestial Surge Ability Effect",
        ])
    }

    @Override
    void onEquip(Player player) { sendEquippedMessage(player) }

    @Override
    void onUnequip(Player player) { sendUnequippedMessage(player) }

    @Override
    void onAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event)
    {
        def modifier = damageModifier + 1.0D

        if (this.weapon.isHolding(player))
        {
            EnchantUtils.scaleDamage(event, modifier + weaponDamageModifier)
        }
        else
        {
            EnchantUtils.scaleDamage(event, modifier)
        }
    }

    @Override
    void onDamaged(Player player, Entity attacker, EntityDamageByEntityEvent event)
    {
        def modifier = 1.0D - incomingDamageModifier

        EnchantUtils.scaleDamage(event, modifier)
    }

    @Override
    void onCrystalAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event)
    {
        def modifier = crystalDamageModifier + 1.0D

        EnchantUtils.scaleDamage(event, modifier)
    }

    double getDamageModifier() { return getConfig().getDoubleEntry("damageModifier").value }

    double getIncomingDamageModifier() { return getConfig().getDoubleEntry("incomingDamageModifier").value }

    double getCrystalDamageModifier() { return getCrystalConfig().getDoubleEntry("crystalDamageModifier").value }

    double getWeaponDamageModifier() { return getConfig().getDoubleEntry("weaponDamageModifier").value }
}

