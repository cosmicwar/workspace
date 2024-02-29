package scripts.exec.hub

import org.bukkit.GameRule
import org.starcade.starlight.enviorment.Exports
import com.viaversion.viaversion.ViaVersionPlugin
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.starcade.starlight.helper.Schedulers
import scripts.WorldConfig
import scripts.shared.legacy.utils.npc.NPCRegistry
import scripts.shared.legacy.utils.npc.NPCTracker
import scripts.shared.utils.gens.VoidWorldGen15

import java.util.function.Consumer

class Config {
    static ViaVersionPlugin via = Bukkit.getPluginManager().getPlugin("ViaVersion") as ViaVersionPlugin

    // TODO: make it check client version logging in and spawn in different spawn if 1.8? build was corrupting and making blocks invisible

    static void main(String[] args) {
        Exports.ptr("spawn_worlds", WorldConfig.SPAWN_WORLDS)
        Exports.ptr("spawn_world_prefix", WorldConfig.SPAWN_WORLD_PREFIX)
        Schedulers.sync().runLater({
            World world = new WorldCreator("world").environment(World.Environment.NORMAL).generator(new VoidWorldGen15()).createWorld()

            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)

            world.setTime(14000L)

            WorldConfig.spawn = new Location(world, 0.5, 20, 0.5, 180, 0)

            WorldConfig.spawn.getWorld().setDifficulty(Difficulty.PEACEFUL)

//            WorldConfig.spawn.getWorld().execute({
                spawnNpc("factions", "§c§lFactions §e§lNova §7§o(Beta)", new Location(world, 0.5, 18, -17.5, 0, 0), "bubbleboyhero", { Player player ->
                    player.performCommand("server nova1")
                })
//            })
        }, 1L)

//        via.getApi().getPlayerVersion()
    }

    static void spawnNpc(String id, String name, Location location, Object skinHolder = null, Consumer<Player> onClick = null) {
        NPCTracker npcTracker = NPCRegistry.get().spawn(id, name, location, skinHolder, onClick)
        npcTracker.turnTowardPlayers = true
    }
}

