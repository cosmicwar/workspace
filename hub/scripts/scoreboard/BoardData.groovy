package scripts.scoreboard

import groovy.transform.CompileStatic
import org.bukkit.entity.Player

@CompileStatic
class BoardData {

    UUID playerId

    boolean nameOverride = false
    String name = ""

    String prefix = ""
    String suffix = ""

    String aboveNameText = null
    String teamName = null

    BoardData() {

    }

    BoardData(Player player, String prefix = "", String suffix = "", String nameOverride = null) {
        this.playerId = player.getUniqueId()

        if (nameOverride) {
            this.nameOverride = true
            this.name = nameOverride
        } else this.name = player.getName()

        this.prefix = prefix
        this.suffix = suffix
    }

}

