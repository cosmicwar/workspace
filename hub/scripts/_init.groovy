package scripts

import com.earth2me.essentials.Essentials
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import org.bukkit.Bukkit
import org.bukkit.Difficulty

Starlight.watch(
        "scripts/shared/systems/_spoofcache.groovy",
        "scripts/shared/content/systems/spoof.groovy",
        "scripts/shared/legacy/executables.groovy",
        "scripts/shared/legacy/nicknames.groovy",
        "~/sendback.groovy",
        "~/tracking.groovy",
        "~/tracker.groovy",
        "~/selector.groovy",
        "~/items.groovy",
        "~/modifiers.groovy",
        "~/scoreboard.groovy",
        "~/join.groovy",
        "~/portals.groovy",
//        "~/plates.groovy",
        "~/fixes.groovy",
        "~/toggles.groovy",
        "~/chat.groovy",
        "~/version_fixes.groovy",
        "~/hotfix.groovy"
)

/*Schedulers.sync().run {
    def spawn = Bukkit.getWorld("world")

    for (def x in -8..8) {
        for (def y in -8..8) {
            spawn.setChunkForceLoaded(x, y, true)
            spawn.loadChunk(x, y, false)
        }
    }
}*/

Schedulers.sync().runLater({
    Bukkit.getWorlds().each {
        it.setTime(18000)

        it.setDifficulty(Difficulty.PEACEFUL)
        try {
            it.setViewDistance(16)
            it.setNoTickViewDistance(2)
        } catch (UnsupportedOperationException e) {

        }
        it.setSpawnFlags(false, false)
    }
}, 1)

GroovyScript.addUnloadHook {
    Essentials essentials = Essentials.getPlugin(Essentials.class)
    essentials.getUserMap().getAllUniqueUsers().each {
        essentials.getUserMap().getUser(it)?.reset()
    }
}