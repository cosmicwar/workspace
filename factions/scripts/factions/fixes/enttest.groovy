package scripts.factions.fixes

import org.starcade.starlight.helper.Commands
import scripts.shared.visuals.floating.FloatingTNT

class enttest {

    List<FloatingTNT> floatingTNTS = new ArrayList<>()

    enttest() {
        Commands.create().assertOp().assertPlayer().handler {cmd ->
            def player = cmd.sender()
            def location = player.location

            FloatingTNT floatingTNT = new FloatingTNT(cmd.sender().world, location)
            floatingTNTS.add(floatingTNT)
            floatingTNT.track()
        }.register("dev/enttest/tnt")

        Commands.create().assertOp().assertPlayer().handler {cmd ->
            floatingTNTS.each {
                it.untrack()
            }
        }.register("dev/enttest/tntclear")

        Commands.create().assertOp().assertPlayer().handler {cmd ->

        }.register("dev/enttest/entity")
    }

}

