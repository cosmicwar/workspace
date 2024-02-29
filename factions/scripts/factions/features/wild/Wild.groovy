package scripts.factions.features.wild


import org.starcade.starlight.helper.Commands
import org.bukkit.entity.Player
import org.starcade.starlight.helper.utils.Players

import java.util.concurrent.ConcurrentHashMap


class Wild {
    Wild() {
        Commands.create().assertPlayer().handler { cmd ->
            randomTeleport(cmd.sender())
        }.register("wild", "wilderness", "rtp", "randomteleport")
    }

    private static Map<UUID, Long> wildCooldowns = new ConcurrentHashMap<>()

    static boolean onCooldown(Player player) {
        Long expiry = wildCooldowns.get(player.uniqueId)
        if (expiry == null) return false
        return expiry > System.currentTimeMillis()
    }

    static Long getCooldown(Player player) {
        return wildCooldowns.getOrDefault(player.getUniqueId(), 0)
    }

    static String getCooldownString(Player player) {
        long expiry = getCooldown(player)
        long remaining = expiry - System.currentTimeMillis()
        if (remaining < 0) return "0"
        return remaining.intdiv(1000) as String
    }

    static void setCooldown(Player player) {
        wildCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 300000L)
    }


    static void randomTeleport(Player player) {
        if (onCooldown(player)) {
            Players.msg(player, "§] §> §cThis action is on cooldown for " + getCooldownString(player) + " seconds.")
            return
        }
        TeleportHandler teleportHandler = new TeleportHandler(player, player.getWorld(), 5000, 5000)
        teleportHandler.teleport()
        setCooldown(player)
    }
}
