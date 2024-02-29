package scripts

import scripts.shared.legacy.wrappers.Console
import scripts.shared.utils.Temple
import scripts.shared3.Redis

if (Globals.isMcp || Globals.isHub) {
    Redis.getGlobal().subscribe({ channel, message ->
        if (!Globals.isMcp && !Globals.isHub) return
        if (Temple.templeId != "nexus") return
        String[] data = message.split(/\|/)
        Console.dispatchCommand(data.join(" "))
    }, "globalperms")
}
