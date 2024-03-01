package scripts.factions.content.essentials

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript

class Essentials {

    Essentials() {
        GroovyScript.addUnloadHook {
            Starlight.unload("~/warp/Warps.groovy")
            Starlight.unload("~/tp/TeleportHandler.groovy")
        }

        Starlight.watch("~/tp/TeleportHandler.groovy")
        Starlight.watch("~/warp/Warps.groovy")
    }

}
