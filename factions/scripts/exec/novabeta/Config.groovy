package scripts.exec.novabeta

import org.bukkit.*
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import scripts.factions.cfg.WorldConfig
import scripts.shared.utils.gens.VoidWorldGen17

class Config {

    static void main(String[] args) {
        def mainWorld = new WorldCreator("starcade") // world is pre-genned, so any generated chunks should be void
            .environment(World.Environment.NORMAL)
            .generator(new VoidWorldGen17())
            .createWorld()

        mainWorld.setDifficulty(Difficulty.NORMAL)

        mainWorld.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
        mainWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        mainWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        mainWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        mainWorld.setGameRule(GameRule.MOB_GRIEFING, false)
        mainWorld.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
        mainWorld.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
        mainWorld.setGameRule(GameRule.DO_PATROL_SPAWNING, false)
        mainWorld.setGameRule(GameRule.DO_INSOMNIA, false)
        mainWorld.setGameRule(GameRule.DO_WARDEN_SPAWNING, false)
        mainWorld.setGameRule(GameRule.DO_VINES_SPREAD, false)
        mainWorld.setGameRule(GameRule.FALL_DAMAGE, false)
        mainWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        mainWorld.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0)

        mainWorld.setTime(14000L)

        def nmsWorld = ((CraftWorld) mainWorld).getHandle()

        nmsWorld.paperConfig().tickRates.mobSpawner = 2
        nmsWorld.paperConfig().tickRates.containerUpdate = 4
        nmsWorld.paperConfig().collisions.maxEntityCollisions = 0
        nmsWorld.paperConfig().entities.armorStands.tick = false
        nmsWorld.paperConfig().entities.armorStands.doCollisionEntityLookups = false
        nmsWorld.paperConfig().chunks.preventMovingIntoUnloadedChunks = true

        nmsWorld.spigotConfig.playerTrackingRange = 48
        nmsWorld.spigotConfig.animalTrackingRange = 48
        nmsWorld.spigotConfig.monsterTrackingRange = 64
        nmsWorld.spigotConfig.otherTrackingRange = 48
        nmsWorld.spigotConfig.miscTrackingRange = 48

        nmsWorld.spigotConfig.monsterActivationRange = 16
        nmsWorld.spigotConfig.animalActivationRange = 16
        nmsWorld.spigotConfig.miscActivationRange = 8
        nmsWorld.spigotConfig.itemMerge = 8
        nmsWorld.spigotConfig.expMerge = 8
        nmsWorld.spigotConfig.itemDespawnRate = 20 * 5 * 60
        nmsWorld.spigotConfig.arrowDespawnRate = 20 * 10
        nmsWorld.spigotConfig.hopperTransfer = 10
        nmsWorld.spigotConfig.hopperCheck = 10
        nmsWorld.spigotConfig.hopperAmount = 64

        nmsWorld.spigotConfig.cactusModifier = 10
        nmsWorld.spigotConfig.melonModifier = 10
        nmsWorld.spigotConfig.mushroomModifier = 10
        nmsWorld.spigotConfig.caneModifier = 10
        nmsWorld.spigotConfig.pumpkinModifier = 10
        nmsWorld.spigotConfig.saplingModifier = 10
        nmsWorld.spigotConfig.wartModifier = 10
        nmsWorld.spigotConfig.wheatModifier = 10

        def nebulaOutpost = new WorldCreator("nebula_outpost") // world is pre-genned, so any generated chunks should be void
                .environment(World.Environment.THE_END)
                .generator(new VoidWorldGen17())
                .createWorld()

        nebulaOutpost.setDifficulty(Difficulty.NORMAL)

        nebulaOutpost.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
        nebulaOutpost.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        nebulaOutpost.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        nebulaOutpost.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        nebulaOutpost.setGameRule(GameRule.MOB_GRIEFING, false)
        nebulaOutpost.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
        nebulaOutpost.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
        nebulaOutpost.setGameRule(GameRule.DO_PATROL_SPAWNING, false)
        nebulaOutpost.setGameRule(GameRule.DO_INSOMNIA, false)
        nebulaOutpost.setGameRule(GameRule.DO_WARDEN_SPAWNING, false)
        nebulaOutpost.setGameRule(GameRule.DO_VINES_SPREAD, false)
        nebulaOutpost.setGameRule(GameRule.FALL_DAMAGE, false)
        nebulaOutpost.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        nebulaOutpost.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0)

        nebulaOutpost.setTime(14000L)

        def nmsNebulaOutpost = ((CraftWorld) nebulaOutpost).getHandle()

        nmsNebulaOutpost.paperConfig().tickRates.mobSpawner = 2
        nmsNebulaOutpost.paperConfig().tickRates.containerUpdate = 4
        nmsNebulaOutpost.paperConfig().collisions.maxEntityCollisions = 0
        nmsNebulaOutpost.paperConfig().entities.armorStands.tick = false
        nmsNebulaOutpost.paperConfig().entities.armorStands.doCollisionEntityLookups = false
        nmsNebulaOutpost.paperConfig().chunks.preventMovingIntoUnloadedChunks = true

        nmsNebulaOutpost.spigotConfig.playerTrackingRange = 48
        nmsNebulaOutpost.spigotConfig.animalTrackingRange = 48
        nmsNebulaOutpost.spigotConfig.monsterTrackingRange = 64
        nmsNebulaOutpost.spigotConfig.otherTrackingRange = 48
        nmsNebulaOutpost.spigotConfig.miscTrackingRange = 48

        nmsNebulaOutpost.spigotConfig.monsterActivationRange = 16
        nmsNebulaOutpost.spigotConfig.animalActivationRange = 16
        nmsNebulaOutpost.spigotConfig.miscActivationRange = 8
        nmsNebulaOutpost.spigotConfig.itemMerge = 8
        nmsNebulaOutpost.spigotConfig.expMerge = 8
        nmsNebulaOutpost.spigotConfig.itemDespawnRate = 20 * 5 * 60
        nmsNebulaOutpost.spigotConfig.arrowDespawnRate = 20 * 10
        nmsNebulaOutpost.spigotConfig.hopperTransfer = 10
        nmsNebulaOutpost.spigotConfig.hopperCheck = 10
        nmsNebulaOutpost.spigotConfig.hopperAmount = 64

        nmsNebulaOutpost.spigotConfig.cactusModifier = 10
        nmsNebulaOutpost.spigotConfig.melonModifier = 10
        nmsNebulaOutpost.spigotConfig.mushroomModifier = 10
        nmsNebulaOutpost.spigotConfig.caneModifier = 10
        nmsNebulaOutpost.spigotConfig.pumpkinModifier = 10
        nmsNebulaOutpost.spigotConfig.saplingModifier = 10
        nmsNebulaOutpost.spigotConfig.wartModifier = 10
        nmsNebulaOutpost.spigotConfig.wheatModifier = 10

        WorldConfig.spawn = new Location(mainWorld, -39.5, 179, 0.5, -90, 0)
    }
}

