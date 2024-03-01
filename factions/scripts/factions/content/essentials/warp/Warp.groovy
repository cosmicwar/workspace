package scripts.factions.content.essentials.warp

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import scripts.factions.data.DataObject
import scripts.factions.data.obj.Position
import scripts.factions.data.uuid.UUIDDataObject

@CompileStatic(TypeCheckingMode.SKIP)
class Warp extends DataObject {

    String displayName = ""
    List<String> description = []

    Material icon = Material.GRAY_STAINED_GLASS

    Position position = new Position()
    Double warpTime = 7

    Warp() {}

    Warp(String id) {
        super(id)
    }

    @Override
    boolean isEmpty() {
        return false
    }
}
