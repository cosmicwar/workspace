package scripts.factions.features.enchant.indicators

import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.starcade.starlight.helper.Events

class HealthIndicator implements Indicator {

    HealthIndicator() {
        Events.subscribe(EntityDamageByEntityEvent.class, EventPriority.HIGH).handler { event ->
            if (event.isCancelled()) return

            if (!(event.getEntity() instanceof Player)) return
            if (!(event.getDamager() instanceof Player)) return

            Player attacker = event.getDamager() as Player
            Player target = event.getEntity() as Player

            double dmg = event.getFinalDamage()
            if (dmg >= 1) spawn("healthdisplay_${attacker.getUniqueId()}_${target.getUniqueId()}", attacker, target, build(dmg))
        }
    }

    @Override
    List<String> build(Object data) {
        if (!(data instanceof Double)) return []
        double dmg = (Double) data

        int finalDmg = (Integer) Math.round(dmg)

        return ["§c§l- $finalDmg♥"]
    }
}

