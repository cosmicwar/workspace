package scripts.factions.core.profile.cmd

import org.bukkit.entity.Player
import org.starcade.starlight.helper.utils.Players
import scripts.factions.core.faction.FCBuilder

class GrantCmd {

    GrantCmd() {

        commands()
    }

    def commands() {
        FCBuilder grant = new FCBuilder("grant").defaultAction {
            if (it.isOp()) {
                Players.msg(it, "&cUsage: /grant <player>")
            }
        }

        FCBuilder grants = new FCBuilder("grants").defaultAction {
            if (it.isOp()) {
                Players.msg(it, "&cUsage: /grants <player>")
            }
        }

    }

    static def grantMenu(Player player, UUID targetId) {

    }
}
