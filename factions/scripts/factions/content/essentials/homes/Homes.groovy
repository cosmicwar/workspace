package scripts.factions.content.essentials.homes

import org.starcade.starlight.enviorment.GroovyScript
import scripts.factions.content.essentials.warp.Warp
import scripts.factions.data.DataManager

class Homes {
    Homes() {
        GroovyScript.addUnloadHook {
            DataManager.getByClass(Home).saveAll(false)
        }

        DataManager.register("ess_homes", Home)

        commands()
    }

    static Home getHome(String name, boolean create = true) {
        return DataManager.getData(name, Home, create)
    }

    static
}
