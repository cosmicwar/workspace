package scripts.factions.patches

import com.earth2me.essentials.Essentials
import com.earth2me.essentials.IUser
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import groovy.transform.Field
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerTeleportEvent
import scripts.factions.core.faction.FactionUtils

import javax.annotation.Nonnull

@Field Essentials essentials = Bukkit.getPluginManager().getPlugin("Essentials") as Essentials

Events.subscribe(PlayerTeleportEvent.class, EventPriority.MONITOR).filter(EventFilters.<PlayerTeleportEvent> ignoreCancelled()).handler { event ->
    if (Patches.glitchEssentialsHome) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND || event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            Player player = event.getPlayer()
            if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                IUser user = this.essentials.getUser(player)
                Location to = event.getTo()
                boolean isHome = false
                Iterator var7 = user.getHomes().iterator()

                String homeName
                while (var7.hasNext()) {
                    homeName = (String) var7.next()
                    Location homeLocation = user.getHome(homeName)
                    if (homeLocation.getBlockX() == to.getBlockX() && homeLocation.getBlockY() == to.getBlockY() && homeLocation.getBlockZ() == to.getBlockZ()) {
                        isHome = true
                    }
                }

                if (isHome) {
                    if (!FactionUtils.isNonPlayerFactionLandOrOwn(player, to)) {
                        event.setCancelled(true)
                        Players.msg(player, Patches.glitchEssentialsHomeMsgDeny)
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                        var7 = user.getHomes().iterator()

                        while (var7.hasNext()) {
                            homeName = (String) var7.next()
                            if (user.getHome(homeName).getBlock().getLocation().getChunk() == to.getChunk()) {
                                user.delHome(homeName)
                            }
                        }

                    }
                }
            }
        }
    }
}

Events.subscribe(PlayerCommandPreprocessEvent.class, EventPriority.HIGHEST).filter(EventFilters.<PlayerCommandPreprocessEvent> ignoreCancelled()).handler { event ->
    if (Patches.glitchEssentialsBackLimitations) {
        Player player = event.getPlayer()
        if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
            String fullCommand = event.getMessage().substring(1).toLowerCase()
            if (backCommand(fullCommand)) {
                IUser user = this.essentials.getUser(player)
                Location backLocation = user.getLastLocation()
                if (!FactionUtils.isNonPlayerFactionLandOrOwn(player, backLocation)) {
                    if (FactionUtils.isPlayerNotAllowedToBackIntoFactionLand(player, backLocation)) {
                        event.setCancelled(true)
                        Players.msg(player, Patches.glitchEssentialsBackMsgDeny)
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                    }
                }
            }
        }
    }
}

static boolean backCommand(@Nonnull String command) {
    if (Patches.glitchEssentialsBackCommandStartsWith.find(command::startsWith)) {
        return true
    } else {
        String cmdNoArgs = command
        if (command.contains(" ")) {
            if (Patches.glitchEssentialsBackCommandExact.contains(command)) {
                return true
            }

            cmdNoArgs = command.split(" ")[0]
        }

        return Patches.glitchEssentialsBackCommandIgnoreArgs.contains(cmdNoArgs)
    }
}
