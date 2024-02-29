package scripts.exec

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Events
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerCommandPreprocessEvent

Exports.ptr("secret", "1d17a9f8-4a5d-11e9-8646-d663bd873d93")
//Exports.ptr("secret", "8a1761cd-723a-4bcb-9e76-9e6411cb0f1c")

Events.subscribe(PlayerCommandPreprocessEvent, EventPriority.LOWEST).handler { e ->
    def msg = e.message
    if (msg.equalsIgnoreCase("/server pac")) {
        e.message = "/server pacificdev"
    } else if (msg.equalsIgnoreCase("/server pac2")) {
        e.message = "/server pacific2"
    } else if (msg.startsWith("/server leg")) {
        e.message = "/server pacific"
    }
}

/*
Events.subscribe(PlayerLoginEvent).handler { e ->
    if (!e.player.isOp()) {
        e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Network maintenance...")
    }
}*/

