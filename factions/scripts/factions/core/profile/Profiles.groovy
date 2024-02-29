package scripts.factions.core.profile

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.GroovyScript
import scripts.factions.core.faction.FCBuilder
import scripts.factions.data.uuid.UUIDDataManager
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MenuDecorator

@CompileStatic(TypeCheckingMode.SKIP)
class Profiles {

    Profiles() {
        GroovyScript.addUnloadHook {
            UUIDDataManager.getByClass(Profile).saveAll(false)
        }

        UUIDDataManager.register("profiles", Profile)
    }

    def commands() {
        FCBuilder cmd = new FCBuilder("profile").defaultAction {
            openProfile(it)
        }


        cmd.build()
    }

    static def openProfile(Player player, UUID targetId = null) {
        if (targetId == null) targetId = player.getUniqueId()

        MenuBuilder menu = new MenuBuilder(54, "§aprofile")

        MenuDecorator.decorate(menu, [
                "555555555",
                "555555555",
                "555555555",
                "555555555",
                "555555555",
                "555555555",
        ])


        menu.openSync(player)
    }

}
