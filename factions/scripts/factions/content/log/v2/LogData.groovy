package scripts.factions.content.log.v2

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import scripts.factions.content.log.v2.api.Log
import scripts.factions.content.log.v2.api.LogType
import scripts.factions.data.uuid.UUIDDataObject

@CompileStatic(TypeCheckingMode.SKIP)
class LogData extends UUIDDataObject {

    Set<Log> history = Sets.newConcurrentHashSet()

    LogType type = null

    LogUserData userData = new LogUserData()

    LogData() {}

    LogData(UUID id) {
        super(id)
    }

    @BsonIgnore
    String getName() {
        def name = Bukkit.getPlayer(getId())
        if (name) {
            name = name.getName()
        } else {
            def op = Bukkit.getOfflinePlayer(getId())
            if (op) name = Bukkit.getOfflinePlayer(getId()).getName()

            if (!name) name = getId().toString()
        }

        return name
    }

    @BsonIgnore
    Log getLatestLog() {
        return history.stream().max(Comparator.comparingLong(Log::getTimestamp)).orElse(null)
    }

    @BsonIgnore
    @Override
    boolean isEmpty() {
        return history.isEmpty() && type == null && userData.isEmpty()
    }
}
