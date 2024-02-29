package scripts.factions.content.combat

import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import scripts.shared.utils.Persistent
import scripts.shared.legacy.objects.Pair
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerQuitEvent

import java.util.concurrent.ConcurrentHashMap

CombatUtils.init()

Events.subscribe(EntityDamageByEntityEvent.class, EventPriority.MONITOR).handler { event ->
    if (event.getEntityType() != EntityType.PLAYER) {
        return
    }
    Entity damager = event.getDamager()

    if (damager.getType() != EntityType.PLAYER) {
        return
    }
    CombatUtils.LAST_HIT.put(event.getEntity().getUniqueId(), Pair.of(damager.getUniqueId(), System.currentTimeMillis()))
}

@CompileStatic(TypeCheckingMode.SKIP)
class CombatUtils {
    public static Map<UUID, Map.Entry<UUID, Long>> LAST_HIT

    static void init() {
        LAST_HIT = Persistent.of("last_hit", new ConcurrentHashMap<UUID, Map.Entry<UUID, Long>>()).get()

        Schedulers.sync().runRepeating({
            Iterator<Map.Entry<UUID, Map.Entry<UUID, Long>>> iterator = LAST_HIT.entrySet().iterator()

            while (iterator.hasNext()) {
                if (System.currentTimeMillis() - iterator.next().getValue().getValue() >= 10000L) {
                    iterator.remove()
                }
            }
        }, 0, 1)

        Events.subscribe(PlayerQuitEvent.class, EventPriority.MONITOR).handler { event ->
            LAST_HIT.remove(event.getPlayer().getUniqueId())
        }
    }

    static UUID getLastHit(UUID uuid) {
        return LAST_HIT.get(uuid)?.getKey()
    }

    static UUID getLastHit(Player player) {
        return getLastHit(player.getUniqueId())
    }
}