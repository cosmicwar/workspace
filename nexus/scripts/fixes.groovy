package scripts

import org.starcade.starlight.helper.Schedulers
import scripts.shared.database.Standard
import scripts.shared.systems.ServerCache
import scripts.shared3.Redis

import java.util.concurrent.TimeUnit

Schedulers.async().runRepeating({
    long now = System.currentTimeMillis()
    for (def server in ServerCache.servers.values()) {
        Redis.getGlobal().sync { redis ->
            Set<String> rem = new HashSet<>()
            Set<String> members = redis.smembers(Standard.SRV_SENDBACK + server.name)

            members.each { entry ->
                String[] parts = entry.split(",")
                if (parts.length < 2) {
                    rem.add(entry)
                    return
                }

                long time = Long.valueOf(parts[1])
                if (now - time > TimeUnit.HOURS.toMillis(1L)) {
                    rem.add(entry)
                }
            }

            String removals = rem.findResults { it?.toString() } as String[]
            if (!removals.isEmpty()) {
                redis.srem("${Standard.SRV_SENDBACK}${server.name}".toString(), removals)
            }
        }
    }
}, 30L, TimeUnit.SECONDS, 30L, TimeUnit.SECONDS)