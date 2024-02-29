package scripts.factions.content.staff

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.starcade.starlight.enviorment.GroovyScript
import scripts.factions.content.dbconfig.Config
import scripts.factions.content.dbconfig.ConfigCategory
import scripts.factions.content.dbconfig.DBConfigUtil
import scripts.factions.content.dbconfig.RegularConfig

@CompileStatic(TypeCheckingMode.SKIP)
class Staff
{

    static Config staffConfig
    static ConfigCategory staffCategory
    static RegularConfig config

    Staff() {
        GroovyScript.addUnloadHook {

        }

        staffConfig = DBConfigUtil.createConfig("staff", "§astaff", [], Material.WOODEN_AXE)
        staffCategory = staffConfig.getOrCreateCategory("staff", "§astaff")
        config = staffCategory.getOrCreateConfig("staff", "§astaff")

        config.addDefault([

        ])

        commands()
        events()
    }

    static def commands() {

    }

    static def events() {

    }

}
