package scripts.factions.features.enchant.indicators


import org.bukkit.Location
import org.bukkit.entity.Player
import scripts.shared.features.holograms.HologramRegistry
import scripts.shared.features.holograms.HologramTracker
import scripts.shared.legacy.ToggleUtils

import java.util.function.Predicate

class ActiveIndicator {

    UUID targetId

    HologramTracker hologram
    Long created

    ActiveIndicator(UUID targetId, HologramTracker hologram) {
        this.targetId = targetId
        this.hologram = hologram
        this.created = System.currentTimeMillis()
    }

    ActiveIndicator(String id, Player attacker, Player target, List<String> lines, Predicate<Player> visibilityPredicate) {
        if (target != null) {
            this.targetId = target.getUniqueId()

            if (attacker != null) if (visibilityPredicate == null) visibilityPredicate = normalPredicate(attacker.getUniqueId(), this.targetId)


            this.hologram = HologramRegistry.get().spawn(id, getRandomLocation(target.getLocation()), lines ?: ["empty"], true, null, visibilityPredicate)
        }

        this.created = System.currentTimeMillis()
    }

    // Shift it a bit so it doesn't spawn inside the player
    static Location getRandomLocation(Location location) {
        double randomX = Math.min(.75, Math.random() * 2)
        double randomY = Math.min(.7, Math.random() * 2)
        double randomZ = Math.min(.75, Math.random() * 2)

        return location.add(randomX, randomY, randomZ)
    }

    // Only show the indicator if the player is not the target
    // Or the player is the attacker and has it toggled
    static Predicate<Player> normalPredicate(UUID attackerId, UUID targetId) {
        return { Player player ->
            if (player.getUniqueId() == targetId) return false

            if (player.getUniqueId() == attackerId) {
                return !ToggleUtils.hasToggled(player, "damage_indicator")
            }

            return false
        }
    }

}

