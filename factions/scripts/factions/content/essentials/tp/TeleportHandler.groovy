package scripts.factions.content.essentials.tp

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.regions.RegionContainer
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.promise.Promise
import org.starcade.starlight.helper.utils.Players
import scripts.shared.utils.ColorUtil

import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class TeleportHandler {

    static DecimalFormat format = new DecimalFormat("#,###.##")
    static Map<UUID, Promise<Void>> teleportingPlayers = new ConcurrentHashMap<>()

    TeleportHandler() {
        events()
    }

    static def teleportPlayer(Player player,
                              Location location,
                              Double duration = 7.0,
                              boolean instant = false,
                              String onTeleportMessage = "",
                              String locationMessage = ""
    )
    {
        if (player.isOp() || !isInPvpZone(player)) instant = true
        if (duration <= 0) instant = true

        teleportingPlayers.remove(player.getUniqueId())?.cancel()

        if (instant) {
            player.teleportAsync(location).thenAccept {
                if (onTeleportMessage != "") Players.msg(player, ColorUtil.color(onTeleportMessage))
            }
        } else {
            double newTeleportTime = Math.max(0.5, duration - duration * (player.getTotalExperience() / 1_000_000))

            if (locationMessage != "") {
                Players.msg(player, ColorUtil.color(locationMessage))
            } else {
                player.sendMessage("§3§l(!) §3You will be teleported in §e§n${format.format(newTeleportTime)}§3... DON'T MOVE!")
                player.sendMessage("§7Your vanilla XP will decrease this wait time.")
            }

            teleportingPlayers.put(player.getUniqueId(), Schedulers.async().runLater({
                player.teleportAsync(location).thenAccept {
                    if (onTeleportMessage != "") Players.msg(player, ColorUtil.color(onTeleportMessage))

                    teleportingPlayers.remove(player.getUniqueId())
                }
            }, newTeleportTime as long, TimeUnit.SECONDS))
        }
    }

    static void events() {
        Events.subscribe(PlayerMoveEvent.class).handler { event ->
            if (event.from.blockZ != event.to.blockZ || event.from.blockX != event.to.blockX || event.from.blockY != event.to.blockY) {
                if (!teleportingPlayers.containsKey(event.player.getUniqueId())) return

                teleportingPlayers.remove(event.player.getUniqueId()).cancel()
                event.player.sendMessage("§cYou have moved, cancelling pending teleportation.")
            }
        }
    }

    static boolean isInPvpZone(Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer()
        return container.createQuery().testState(BukkitAdapter.adapt(player.location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.PVP)
    }

}
