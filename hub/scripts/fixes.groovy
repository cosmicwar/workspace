package scripts

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.protocol.Protocol
import groovy.transform.Field
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.spigotmc.event.player.PlayerSpawnLocationEvent

import java.util.regex.Pattern

@Field static Pattern exploitPattern = Pattern.compile('\\$\\{.+\\}')

Protocol.subscribe(PacketType.Play.Client.CHAT).handler({
    if (it.isPlayerTemporary()) return

    PacketContainer packetContainer = it.getPacket()
    String chatMessage = packetContainer.getStrings().read(0)
    if (exploitPattern.matcher(chatMessage).find()) {
        it.setPacket(new PacketContainer(PacketType.Play.Server.CHAT))
        it.setCancelled(true)
    }
})

for (final World world : Bukkit.getWorlds()) {
//    world.execute {
        for (Entity entity : world.getEntities()) {
            if (entity.getType() != EntityType.PLAYER) {
                entity.remove()
            }
        }
//    }
}

Events.subscribe(EntityTeleportEvent.class, EventPriority.HIGH).filter(EventFilters.ignoreCancelled()).handler { event ->
    if (event.getEntityType() != EntityType.PLAYER) {
        event.setCancelled(true)
    }
}

Schedulers.sync().runLater({
    World w = Bukkit.getWorlds().get(0);
    /*w.execute { */w.setSpawnLocation(WorldConfig.getSpawn()) /*}*/
}, 1L)

Exports.ptr("getSpawn", WorldConfig.getSpawn())

Schedulers.sync().runRepeating({
    for (Player player : Bukkit.getOnlinePlayers()) {
        if (player.getLocation().getY() < -25) {
            player.teleportAsync(WorldConfig.getSpawn())
        }
    }
}, 5L, 5L)

Events.subscribe(PlayerJoinEvent.class).handler { event ->
    Player player = event.getPlayer()

    player.teleportAsync(WorldConfig.getSpawn()).thenAccept { result ->
        if (result) {
            (Exports.ptr("handleJoin") as Closure)?.call(player)
        }
    }
}

Events.subscribe(PlayerSpawnLocationEvent.class, EventPriority.LOWEST).handler { event ->
    event.setSpawnLocation(WorldConfig.getSpawn())

    Player player = event.getPlayer()

    if (player.hasPlayedBefore()) {
        (Exports.ptr("handleJoin") as Closure)?.call(player)
    }
}

Events.subscribe(PlayerRespawnEvent.class).handler { event ->
    event.setRespawnLocation(WorldConfig.getSpawn())
}

Commands.create().assertPlayer().handler { command ->
    command.sender().teleportAsync(WorldConfig.getSpawn())
}.register("spawn")