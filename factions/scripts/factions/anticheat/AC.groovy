package scripts.factions.anticheat

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript

@CompileStatic(TypeCheckingMode.SKIP)
class AC
{

    AC() {
        GroovyScript.addUnloadHook {
            Starlight.unload("~/AutoClickerAlerts.groovy")
        }

        Starlight.watch("~/AutoClickerAlerts.groovy")
    }

}
