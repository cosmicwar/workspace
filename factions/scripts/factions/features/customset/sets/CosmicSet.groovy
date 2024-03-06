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

class CosmicSet extends CustomSet {

    CosmicSet() {
        super(
                "cosmic",
                "Cosmic",
                [
                        "",
                        "§<bold>§<primaryColor>COSMIC SET BONUS",
                        "§<primaryColor>Deal §<secondaryColor>5% more §<primaryColor>damage to all enemies.",
                        "§<primaryColor>Take §<#5FB8C8>12.5% less §<primaryColor>incoming damage.",
                        "",
                        "§<bold>§<rainbow>Cosmic Resonance Ability Effect",
                ],
                "§<bold>§<rainbow>COSMIC SET EQUIPPED",
                "§<bold>§<rainbow>COSMIC SET REMOVED",
                "Helm",
                "Plate",
                "Trousers",
                "Boots",
                ["#4f2dd5", "#582036"],
                [" §<primaryColor>§l* §<primaryColor>§<#5FB8C8>25% less §<primaryColor>incoming damage."]
        )

        this.getConfig().addDefault([
                new DoubleEntry("damageModifier", 0.05D),
                new DoubleEntry("incomingDamageModifier", .125D),

                new DoubleEntry("weaponDamageModifier", .05D),
        ])

        this.getCrystalConfig().addDefault([
                new DoubleEntry("crystalIncomingDamageModifier", .05D),
        ])

        this.weapon = new CustomSetWeapon(this, Material.NETHERITE_SWORD, "Blade", [])
    }

    @Override
    void onEquip(Player player) {
        sendEquippedMessage(player)
    }

    @Override
    void onUnequip(Player player) {
        sendUnequippedMessage(player)
    }

    @Override
    void onCrystalEquip(Player player) {

    }

    @Override
    void onCrystalUnequip(Player player) {

    }

    @Override
    void onAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        if (this.weapon != null && this.weapon.isHolding(player))
        {
            EnchantUtils.scaleDamage(event, weaponDamageModifier)
        }
        else
        {
            EnchantUtils.scaleDamage(event, damageModifier)
        }
    }

    @Override
    void onDamaged(Player player, Entity attacker, EntityDamageByEntityEvent event) {
        EnchantUtils.scaleDamage(event, incomingDamageModifier)
    }

    double getDamageModifier() {
        return getConfig().getDoubleEntry("damageModifier").value
    }

    double getIncomingDamageModifier() {
        return getConfig().getDoubleEntry("incomingDamageModifier").value
    }

    double getCrystalIncomingDamageModifier() {
        return getCrystalConfig().getDoubleEntry("crystalIncomingDamageModifier").value
    }

    double getWeaponDamageModifier() {
        return getConfig().getDoubleEntry("weaponDamageModifier").value
    }

    class CosmicResonanceAbility {



        CosmicResonanceAbility () {

        }
    }
}

