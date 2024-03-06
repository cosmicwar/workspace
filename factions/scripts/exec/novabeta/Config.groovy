package scripts.exec.novabeta

import org.bukkit.*
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import scripts.factions.cfg.WorldConfig
import scripts.shared.utils.gens.VoidWorldGen17

class Config {

    static void main(String[] args) {
        def world = WorldCreator.name("beta_map").environment(World.Environment.NORMAL)
                .generator(new VoidWorldGen17())
                .createWorld()

        world.setDifficulty(Difficulty.NORMAL)

        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        world.setGameRule(GameRule.MOB_GRIEFING, false)
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false)
        world.setGameRule(GameRule.DO_INSOMNIA, false)
        world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false)
        world.setGameRule(GameRule.DO_VINES_SPREAD, false)
        world.setGameRule(GameRule.FALL_DAMAGE, false)
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0)
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)

        world.setTime(14000L)

        def nmsworld = ((CraftWorld) world).getHandle()

        nmsworld.paperConfig().tickRates.mobSpawner = 2
        nmsworld.paperConfig().tickRates.containerUpdate = 4
        nmsworld.paperConfig().collisions.maxEntityCollisions = 0
        nmsworld.paperConfig().entities.armorStands.tick = false
        nmsworld.paperConfig().entities.armorStands.doCollisionEntityLookups = false
        nmsworld.paperConfig().chunks.preventMovingIntoUnloadedChunks = true

        nmsworld.spigotConfig.playerTrackingRange = 48
        nmsworld.spigotConfig.animalTrackingRange = 48
        nmsworld.spigotConfig.monsterTrackingRange = 64
        nmsworld.spigotConfig.otherTrackingRange = 48
        nmsworld.spigotConfig.miscTrackingRange = 48

        nmsworld.spigotConfig.monsterActivationRange = 16
        nmsworld.spigotConfig.animalActivationRange = 16
        nmsworld.spigotConfig.miscActivationRange = 8
        nmsworld.spigotConfig.itemMerge = 8
        nmsworld.spigotConfig.expMerge = 8
        nmsworld.spigotConfig.itemDespawnRate = 20 * 5 * 60
        nmsworld.spigotConfig.arrowDespawnRate = 20 * 10
        nmsworld.spigotConfig.hopperTransfer = 10
        nmsworld.spigotConfig.hopperCheck = 10
        nmsworld.spigotConfig.hopperAmount = 64

        nmsworld.spigotConfig.cactusModifier = 10
        nmsworld.spigotConfig.melonModifier = 10
        nmsworld.spigotConfig.mushroomModifier = 10
        nmsworld.spigotConfig.caneModifier = 10
        nmsworld.spigotConfig.pumpkinModifier = 10
        nmsworld.spigotConfig.saplingModifier = 10
        nmsworld.spigotConfig.wartModifier = 10
        nmsworld.spigotConfig.wheatModifier = 10

        WorldConfig.SPAWN_WORLD_PREFIX = "beta_map"
        WorldConfig.spawn = new Location(world, 233.5, 165, 78.5, 90, 0)
    }
}

