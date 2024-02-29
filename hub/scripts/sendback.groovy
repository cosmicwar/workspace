package scripts

import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Schedulers
import org.bukkit.Bukkit
import scripts.shared.database.Standard
import scripts.shared.systems.ServerCache
import scripts.shared.utils.ServerUtils
import scripts.shared3.Redis

import java.util.concurrent.ThreadLocalRandom

//
// todo, get hub count
Schedulers.async().runRepeating({
    try {
        for (def server in ServerCache.servers.values()) {
            if (server.type != "hub") {
                Redis.getGlobal().sync { redis ->
                    Set<String> members = redis.smembers(Standard.SRV_SENDBACK + server.name)
                    int limit = 15
                    for (String next in members) {
                        String[] parts = next.split(",")
                        String name = parts[0]
                        def player = Bukkit.getPlayerExact(name)
                        if (player != null) {
                            if (--limit <= 0) break

                            Starlight.plugin.logger.info("Sending ${player.name} back to " + server.name)
                            ServerUtils.sendToServer(player, server.address)
                            redis.srem(Standard.SRV_SENDBACK + server.name, next)
                            break
                        }
                    }
                }
            }
        }
    } catch (Throwable t) {
        t.printStackTrace()
    }
}, ThreadLocalRandom.current().nextInt(30), 60)