package scripts.factions.content.essentials.homes


import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material
import org.bukkit.entity.Player
import scripts.factions.data.DataObject
import scripts.factions.data.obj.Position

import java.util.List
import java.util.concurrent.ConcurrentHashMap

@CompileStatic(TypeCheckingMode.SKIP)

public class Home extends DataObject {

    UUID playerId = null

    String displayName = ""

    Material icon = Material.GRASS_BLOCK

    Position position = new Position()
    Double warpTime = 7

    Home() {}

    Home(String id) {
        super(id)
    }

    Home(Player player, String id) {
        super(id)
        this.playerId = player.getUniqueId()
    }

    @Override
    boolean isEmpty() {
        return false
    }
}
