package scripts.factions.content.scoreboard

import com.google.common.collect.Maps
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events

@CompileStatic(TypeCheckingMode.SKIP)
class SCBoard {

    static Map<UUID, BoardData> boardDataCache = Maps.newConcurrentMap()

    SCBoard() {
        GroovyScript.addUnloadHook {
            Starlight.unload("~/sidebar/SidebarHandler.groovy")
        }

        Events.subscribe(PlayerJoinEvent.class).handler {event ->
            def player = event.getPlayer()

            def boardData = createBoardData(player)

            updateBoardData(player, boardData)
        }

        Events.subscribe(PlayerQuitEvent.class).handler { event ->
            def player = event.getPlayer()

            def boardData = removeBoardData(player)
            if (boardData != null) {
                // handle remove in game data
            }
        }

        Starlight.watch("~/sidebar/SidebarHandler.groovy")
    }

    static def removeBoardData(Player player) {
        return boardDataCache.remove(player.getUniqueId())
    }

    static def createBoardData(Player player) {
        def boardData = boardDataCache.get(player.getUniqueId())
        if (boardData == null) {
            boardData = new BoardData(player)
            boardDataCache.put(player.getUniqueId(), boardData)
        }

        return boardData
    }

    static def updateBoardData(Player player, BoardData boardData) {

    }


}
