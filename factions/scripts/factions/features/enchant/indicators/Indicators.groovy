package scripts.factions.features.enchant.indicators

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.bucket.Bucket
import org.starcade.starlight.helper.bucket.factory.BucketFactory
import org.starcade.starlight.helper.bucket.partitioning.PartitioningStrategies
import scripts.shared.features.holograms.HologramRegistry
import scripts.shared.legacy.ToggleUtils

import java.util.concurrent.TimeUnit
import java.util.function.Predicate

class Indicators {

    Bucket<ActiveIndicator> indicatorsCache = BucketFactory.newConcurrentBucket(250, PartitioningStrategies.lowestSize())

    Indicators() {
        GroovyScript.addUnloadHook {
            indicatorsCache.asCycle().next().each {
                HologramRegistry.get().unregister(it.hologram)
            }
        }

        Exports.ptr("indicators:createHologram", { String id, Player attacker, Player target, List<String> lines, Predicate<Player> visibilityPredicate ->
            createHologram(id, attacker, target, lines, visibilityPredicate)
        })

        ToggleUtils.register("damage_indicator", [
                item        : Material.NETHERITE_SWORD,
                display_name: "§c§lDamage Indicators",
                description : [
                        "This setting will toggle damage",
                        "indicator holograms."
                ],
                category    : "other",
                hidden      : false
        ])

        Schedulers.async().runRepeating({
            def bucket = indicatorsCache.asCycle().next()
            for (def indicator in bucket) {
                if (Bukkit.getPlayer(indicator.targetId ?: null) == null) {
                    HologramRegistry.get().unregister(indicator.hologram)
                    bucket.remove(indicator)
                    return
                }

                if (indicator.created + 500 < System.currentTimeMillis()) {
                    HologramRegistry.get().unregister(indicator.hologram)
                    bucket.remove(indicator)
                    return
                }
            }
        }, 0L, TimeUnit.MILLISECONDS, 10L, TimeUnit.MILLISECONDS)
    }

    def createHologram(String id, Player attacker, Player target, List<String> lines, Predicate<Player> visibilityPredicate = null) {
        Schedulers.async().execute {
            if (lines.isEmpty()) return
            if (visibilityPredicate == null) indicatorsCache.add(new ActiveIndicator(id, attacker, target, lines, null))
            else indicatorsCache.add(new ActiveIndicator(id, attacker, target, lines, visibilityPredicate))
        }
    }

}

