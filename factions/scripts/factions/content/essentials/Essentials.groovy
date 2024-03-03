package scripts.factions.content.essentials

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript

class Essentials {

    Essentials() {
        GroovyScript.addUnloadHook {
            Starlight.unload("~/cmd/EssentialsCmd.groovy")

            Starlight.unload("~/warp/Warps.groovy")
            Starlight.unload("~/homes/Homes.groovy")
            Starlight.unload("~/tp/TeleportHandler.groovy")

            Starlight.unload()
        }

        Starlight.watch("~/tp/TeleportHandler.groovy")
        Starlight.watch("~/warp/Warps.groovy")
        Starlight.watch("~/homes/Homes.groovy")

        // cmds
        Starlight.watch("~/cmd/EssentialsCmd.groovy")
    }

}
