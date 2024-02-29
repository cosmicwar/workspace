package scripts.factions.core.faction.data

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.core.faction.perm.Permission
import scripts.shared.utils.ColorUtil

@CompileStatic
class SystemFaction
{
    String color

    List<Permission> permissions = new ArrayList<>()

    SystemFaction() {
    }

    SystemFaction(String color) {
        this.color = color
    }

    SystemFaction(String color, List<Permission> permissions) {
        this.color = color
        this.permissions = permissions
    }

    @BsonIgnore
    static boolean isEmpty() {
        return false
    }

    String getColor() {
        return ColorUtil.color(color)
    }
}