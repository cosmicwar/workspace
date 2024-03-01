package scripts.factions.content.essentials.homes


import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import scripts.factions.data.DataObject
import scripts.factions.data.obj.Position

import java.util.List

@CompileStatic(TypeCheckingMode.SKIP)

public class Home extends DataObject {
    String displayName = ""

    Material icon = Material.GRASS_BLOCK

    Position position = new Position()
    Double warpTime = 7

    Home() {}

    Home(String id) {
        super(id)
    }

    @Override
    boolean isEmpty() {
        return false
    }
}
