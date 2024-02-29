package scripts

import org.starcade.starlight.helper.Schedulers
import io.papermc.paper.configuration.GlobalConfiguration
import net.minecraft.server.MinecraftServer
import org.bukkit.Bukkit
import org.spigotmc.SpigotConfig
import scripts.shared.utils.Temple

//MinecraftServer.FAST_STOP = true

SpigotConfig.movedTooQuicklyMultiplier = 30.0d
SpigotConfig.movedWronglyThreshold = Double.MAX_VALUE

GlobalConfiguration.get().collisions.enablePlayerCollisions = false

//Schedulers.sync().runLater({
//    Bukkit.getWorlds().each {
//        it.setHubMode(Temple.templeId != "techmullet")
//    }
//}, 1)

