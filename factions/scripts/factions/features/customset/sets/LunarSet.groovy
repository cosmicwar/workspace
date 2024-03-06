package scripts.factions.features.customset.sets

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.factions.features.customset.struct.CustomSet
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.legacy.utils.BroadcastUtils

class LunarSet extends CustomSet {

    LunarSet() {
        super(
                "lunar",
                "Lunar",
                [
                        "",
                        "§<bold>§<primaryColor>LUNAR SET BONUS",
                        "§<primaryColor>Deal §<#5FB8C8>25% more §<primaryColor>damage to all enemies.",
                        "",
                        "§<bold>§<rainbow>Lunar Eclipse Ability Effect",
                ],
                "§<bold>§<rainbow>LUNAR SET EQUIPPED",
                "§<bold>§<rainbow>LUNAR SET REMOVED",
                "Cap",
                "Breast-Plate",
                "Leg-Plates",
                "Walkers",
                ["#d5a210", "#681147"],
                [" §<primaryColor>§l* §<primaryColor>§<#5FB8C8>25% more §<primaryColor>damage to all enemies."]
        )

        getConfig().addDefault([
                new DoubleEntry("damageModifier", 1.25D),
                new DoubleEntry("incomingDamageModifier", 1.00D),
        ])
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
    void onAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        BroadcastUtils.broadcast(event.getDamage() + " - damage")
        EnchantUtils.scaleDamage(event, damageModifier)
        BroadcastUtils.broadcast(event.getFinalDamage() + " - final damage")
    }

    @Override
    void onDamaged(Player player, Entity attacker, EntityDamageByEntityEvent event) {
        BroadcastUtils.broadcast(event.getDamage() + " - damage")
        BroadcastUtils.broadcast(event.getFinalDamage() + " - final damage")
    }

    double getDamageModifier() {
        return getConfig().getDoubleEntry("damageModifier").value
    }

    double getIncomingDamageModifier() {
        return getConfig().getDoubleEntry("incomingDamageModifier").value
    }
}



