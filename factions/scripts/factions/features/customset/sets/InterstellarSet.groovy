package scripts.factions.features.customset.sets

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.potion.PotionEffectType
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.factions.features.customset.struct.CustomSet

import java.util.concurrent.ThreadLocalRandom

class InterstellarSet extends CustomSet {

    Closure<Double> damageModifier

    InterstellarSet() {
        super(
                "interstellar",
                "Interstellar",
                [
                        "",
                        "§<bold>§<primaryColor>INTERSTELLAR SET BONUS",
                        "§<primaryColor>Receive §<#5FB8C8>20% more xp §<primaryColor>from all killed mobs.",
                        "§<primaryColor>Receive §<#5FB8C8>Unlimited Hunger§<primaryColor>.",
                        "",
                        "§<bold>§<rainbow>Galactic Fury Ability Effect",
                ],
                "§<bold>§<rainbow>INTERSTELLAR SET EQUIPPED",
                "§<bold>§<rainbow>INTERSTELLAR SET REMOVED",
                "Headgear",
                "Chestwrap",
                "Legwraps",
                "Footwraps",
                ["#47ba6a", "#131366"],
                [" §<primaryColor>§l* §<primaryColor>§<#5FB8C8>20% more xp §<primaryColor>from all killed mobs."]
        )

        this.getConfig().addDefault([
                new DoubleEntry("damageModifier", 1.00D),
                new DoubleEntry("incomingDamageModifier", 1.00D),
                new DoubleEntry("xpMultiplier", 1.20D)
        ])
    }

    @Override
    void onEquip(Player player) {
        addPotionEffect(player, PotionEffectType.ABSORPTION, 0)
        sendEquippedMessage(player)
    }

    @Override
    void onUnequip(Player player) {
        removePotionEffect(player, PotionEffectType.ABSORPTION, 0)
    }

    @Override
    void onKill(Player player, LivingEntity target, EntityDeathEvent event) {
        double multiplier = getXpMultiplier()

        ThreadLocalRandom random = ThreadLocalRandom.current()
        // min = .6-1.2
        // max = 1.2-2.4
        def randomFactor = 1 + (random.nextDouble(0, 100) / 100)
        multiplier = random.nextDouble(multiplier / randomFactor, multiplier * randomFactor)

        def xp = Math.ceil(event.getDroppedExp() * multiplier)

        event.setDroppedExp(xp as int)
    }

    double getDamageModifier() {
        return getConfig().getDoubleEntry("damageModifier").value
    }

    double getIncomingDamageModifier() {
        return getConfig().getDoubleEntry("incomingDamageModifier").value
    }

    double getXpMultiplier() {
        return getConfig().getDoubleEntry("xpMultiplier").value
    }
}

