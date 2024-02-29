package scripts

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import scripts.shared.legacy.objects.Region
import scripts.shared.systems.CachedServer
import scripts.shared.systems.ServerCache
import scripts.shared.utils.Persistent
import scripts.shared.utils.ServerUtils

import java.util.concurrent.ConcurrentHashMap

Map<String, Region> portals = [
        cursed: new Region(1397, 24, 840, 1406, 47, 841),
        medieval: new Region(1487, 24, 922, 1488, 47, 931),
        atlantic: new Region(1406, 24, 1012, 1397, 47, 1013),
        newpacific: new Region(1315, 24, 931, 1316, 47, 922)
]

Set<UUID> sending = Persistent.of("portal_sending", ConcurrentHashMap.newKeySet()).get() as Set<UUID>

Schedulers.async().runRepeating({
    for (Player player : Bukkit.getOnlinePlayers()) {
        for (Map.Entry<String, Region> entry : portals.entrySet()) {
            if (System.currentTimeMillis() - player.getLastLogin() < 200) {
                continue
            }
            if (!entry.getValue().contains(player.getLocation())) {
                continue
            }
            if ((Exports.ptr("hasCaptcha") as Closure<Boolean>)?.call(player)) {
                Players.msg(player, "§] §8» §fPlease complete the captcha to proceed to the server! §7§o(/captcha <captcha>)")
                return
            }
            CachedServer server = ServerCache.servers.get(entry.getKey())

            if (server == null) {
                Players.msg(player, "§cThat server is currently offline!")
                continue
            }
            if (!sending.add(player.getUniqueId())) {
                continue
            }
            ServerUtils.sendToServer(player, server.address)
        }
    }
}, 1, 1)

Events.subscribe(PlayerQuitEvent.class).handler { event ->
    sending.remove(event.getPlayer().getUniqueId())
}