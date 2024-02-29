package scripts.factions.features.enchant.indicators

import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.Exports

import java.util.function.Predicate

interface Indicator {

    List<String> build(Object data)

    default spawn(String id, Player attacker, Player target, List<String> lines = build(null), Predicate<Player> visibilityPredicate = null) {
        (Exports.ptr("indicators:createHologram") as Closure).call(id, attacker, target, lines, visibilityPredicate)
    }

}

