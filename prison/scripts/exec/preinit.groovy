package scripts.exec

import org.starcade.starlight.Starlight
import scripts.shared.utils.Temple

if (Temple.templeEnv == "dev") {
    Starlight.watch("~/dev.groovy")
}

Starlight.watch("~/${Temple.templeId.replace("_local", "")}/_preinit.groovy")
Starlight.watch("~/init.groovy")