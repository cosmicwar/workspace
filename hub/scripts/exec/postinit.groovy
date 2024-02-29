package scripts.exec

import com.google.common.collect.Maps
import scripts.Globals
import scripts.shared.utils.Gson
import scripts.shared.utils.ServerUtils
import scripts.shared.utils.SpoofSettings
import scripts.shared.utils.Temple
import scripts.shared3.Redis

SpoofSettings.spoofMultiplier = 3.0
SpoofSettings.maxCount = 10000
SpoofSettings.banned = []

ServerUtils.queueEnabled = false

/*// don't touch this
Commands.create().assertPlayer().handler({ c ->
    c.reply("§aSupport the server by watching ads!")
    c.reply("§fhttp://rewards.mcprison.com/?server_id=124&player_uuid=${c.sender().uniqueId.toString()}")
}).register("ads")*/

Redis.getGlobal().async { redis ->
    if (Globals.isLocal || Globals.isDev) return
    redis.get("active_temples").thenAccept { String data ->
        if (data == null) {
            Map<String, Long> activeTemples = Maps.<String, Long> newConcurrentMap()
            activeTemples.put(Temple.templeId, System.currentTimeMillis())
            Globals.ACTIVE_TEMPLES.addAll(activeTemples.keySet())
            redis.set("active_temples", Gson.gson.toJson(activeTemples))
            return
        }
        Map<String, Long> activeTemples = Gson.gson.fromJson(data, Map<String, Long>.class)

        activeTemples.removeAll { it.value < System.currentTimeMillis() - 1000 * 60 * 60 * 32 }// 32 hours of inactivity == remove.
        activeTemples.put(Temple.templeId, System.currentTimeMillis())

        Globals.ACTIVE_TEMPLES.addAll(activeTemples.keySet())
        redis.set("active_temples", Gson.gson.toJson(activeTemples, Map<String, Long>.class))
    }
}

Globals.SERVER_REMAPPER = [
        "legacyatlantic": "atlantic9",
        "legacysun": "sun5",
        "atlantic": "atlantic10",
        "sun": "sun6",
        "survival": "survival1",
        "cloud": "cloud5"
]
