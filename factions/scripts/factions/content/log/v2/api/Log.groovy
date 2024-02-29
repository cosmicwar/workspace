package scripts.factions.content.log.v2.api

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import scripts.factions.data.obj.Position

@CompileStatic(TypeCheckingMode.SKIP)
class Log {

    UUID id = UUID.randomUUID()

    LogType targetType = LogType.UNKNOWN
    UUID targetId = null
    String targetName = null

    LogType initiatorType = LogType.UNKNOWN
    UUID initiatorId = null
    String initiatorName = null

    Position position = new Position()

    Material logMaterial = Material.PAPER

    String title = ""
    List<String> logMessage = []
    Long timestamp = System.currentTimeMillis()

    Log() {
    }

    Log(LogType targetType, UUID targetId, String targetName) {
        this.targetType = targetType
        this.targetId = targetId
        this.targetName = targetName
    }

}
