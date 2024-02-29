package scripts

import org.apache.commons.lang3.text.WordUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.systems.BungeeCache
import scripts.shared.utils.PAPI
import scripts.shared.utils.Temple
import scripts.shared3.ArkGroups

import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

PAPI.registerPlaceholder("server_cached_players", { event ->
    if (!event.isOnline())
        return "§cError..."
    try {
        return NumberUtils.format(BungeeCache.getGlobalPlayerCount())
    } catch (Exception ignore) {
        return "0"
    }
})

PAPI.registerStaticPlaceholders("server_name_bukkit", Bukkit.getName())

PAPI.registerStaticPlaceholders("server_name", Temple.templeId)

PAPI.registerPlaceholder("server_time", { event ->
    if (!event.isOnline())
        return "§cError..."
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    LocalDateTime now = LocalDateTime.now()

    return dtf.format(now)
})

PAPI.registerPlaceholder("player_first_played", { event ->
    if (!event.isOnline())
        return "§cError..."
    Player player = event.getPlayer()
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    LocalDateTime firstPlayed = LocalDateTime.ofInstant(Instant.ofEpochMilli(player.getFirstPlayed()), TimeZone.getDefault().toZoneId())

    return dtf.format(firstPlayed)
})

PAPI.registerPlaceholder("player", { event ->
    if (!event.isOnline())
        return "§cError..."

    return event.getPlayer().getDisplayName()
})

PAPI.registerPlaceholder("player_rank", { event ->
    if (!event.isOnline())
        return "§cError..."

    return ArkGroups.getScoreboardTag(event.player)
})