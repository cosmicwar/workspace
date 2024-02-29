package scripts.factions.core.skill

import org.bukkit.entity.Player
import org.starcade.starlight.helper.Commands

class Skills
{



    Skills()
    {



    }

    static def commands()
    {
        Commands.create().assertPlayer().handler {ctx ->

        }.register("skill", "stat")
    }

    static def openSkillMenu(Player player) {

    }

}
