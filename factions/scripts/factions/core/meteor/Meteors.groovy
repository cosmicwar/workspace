package scripts.factions.core.meteor

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.starcade.starlight.enviorment.GroovyScript
import scripts.shared.core.cfg.Config
import scripts.shared.core.cfg.ConfigCategory
import scripts.shared.core.cfg.utils.DBConfigUtil
import scripts.shared.core.cfg.RegularConfig
import scripts.shared.core.cfg.entries.IntEntry
import scripts.factions.core.faction.FCBuilder

@CompileStatic(TypeCheckingMode.SKIP)
class Meteors
{

    static FCBuilder command

    static Config config
    static ConfigCategory values
    static RegularConfig tool

    Meteors()
    {
        GroovyScript.addUnloadHook {

        }

        reloadConfig()
        commands()
    }

    static def reloadConfig() {
        config = DBConfigUtil.createConfig("meteors", "Meteors", [], Material.FIRE)
        values = config.getOrCreateCategory("values", "Values")

        // tool settings
        tool = values.getOrCreateConfig("tool", "Tool", Material.NETHERITE_PICKAXE)
        tool.addDefault([
                new IntEntry("maxLevel", 10),

        ])

        config.queueSave()
    }

    static def commands() {
        command = new FCBuilder("meteors")

        command.build()
    }


}
