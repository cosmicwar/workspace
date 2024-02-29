package scripts

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.utils.Persistent
import scripts.shared3.Redis


List<String> messages = Exports.ptr("announcement_global") as List<String>

long time = 15 * 60 * 20L

(Persistent.persistentMap.get("network_announcements_task") as Task)?.stop()

Task task = Schedulers.async().runRepeating({
    Redis.get().publish("network_announcements", "\n${RandomUtils.getRandom(messages)}\n ")
}, 0, time)

Persistent.persistentMap.put("network_announcements_task", new Persistent<Task>(task))
