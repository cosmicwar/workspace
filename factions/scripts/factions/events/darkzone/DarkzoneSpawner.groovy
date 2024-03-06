package scripts.factions.events.darkzone

import com.google.common.collect.Maps
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.helper.Schedulers
import scripts.factions.content.mobs.TickableMob
import scripts.shared.data.obj.Position
import scripts.factions.eco.loottable.v2.api.LootTable
import scripts.shared.features.holograms.HologramRegistry
import scripts.shared.features.holograms.HologramTracker
import scripts.shared.legacy.utils.RandomUtils

import java.util.concurrent.ThreadLocalRandom

@CompileStatic(TypeCheckingMode.SKIP)
class DarkzoneSpawner {

    UUID spawnerId

    DarkzoneTier tier
    Position position

    Map<UUID, TickableMob> spawnedMobs = Maps.newConcurrentMap()
    HologramTracker hologram = null

    LootTable lootTable

    int tick

    DarkzoneSpawner(DarkzoneTier tier, Position position) {
        this.spawnerId = UUID.randomUUID()
        this.tier = tier
        this.position = position

        this.tick = 0
    }


    def tick() {
        updateHologram()

        def world = Bukkit.getWorld(position.world)
        if (world == null) return

        def loc = position.getLocation(world)
        if (loc == null) return

        Schedulers.sync().execute {
            def nearbyPlayers = loc.getNearbyPlayers(20)

            if (nearbyPlayers.isEmpty()) {
                clearMobs()

                tick = 0
            } else {
                if (tick % 30 == 0) { // Every 10 ticks
                    if (tick == 30) tick = 0 // dont allow tick to overflow

                    if (spawnedMobs.size() < tier.maxSpawnCount) {
                        def random = ThreadLocalRandom.current()

                        def amountToSpawn = tier.maxSpawnCount - spawnedMobs.size()

                        if (amountToSpawn > 1) {
                            amountToSpawn = random.nextInt(1, amountToSpawn)
                        }

                        for (int i = 0; i < amountToSpawn; i++) { // TODO: add random spawn chances?
                            spawnTierMob(position.getLocation(Bukkit.getWorld(position.world)))
                        }

                        updateHologram()
                    }
                }

                tick++
            }
        }
    }

    def removeMob(UUID mobId) {
        def mob = spawnedMobs.remove(mobId)

        if (mob != null) {
            mob.killMob()
            updateHologram()
        }

        return mob
    }

    def spawnHologram() {
        if (hologram != null) {
            updateHologram()
            return
        }

        hologram = HologramRegistry.get().spawn("dz_spawner__${getSpawnerId().toString()}", position.getLocation(Bukkit.getWorld(position.world)).clone().add(0.5D, 2D, 0.5D), [
                "§d§lSpawner §8| ${tier.getDisplayName()}",
                "§5Mobs: §c§l§n${spawnedMobs.size()}",
                "§3",
                "§aRespawn: §7${30 - tick % 30}"
        ] as List<String>, false)
    }

    def removeHologram() {
        HologramRegistry.get().unregister(hologram)
        hologram = null
    }

    def updateHologram() {
        if (hologram == null) spawnHologram()
        else hologram?.updateLines([
                "§d§lSpawner §8| ${tier.getDisplayName()}",
                "§5Mobs: §c§l§n${spawnedMobs.size()}",
                "§3",
                "§aRespawn: §7${30 - tick % 30}"
        ] as List<String>)
    }

    def spawnTierMob(Location location, float scale = 1.0F) {
        def level = ((CraftWorld) location.getWorld()).getHandle()

        if (tier.spawnableTypes.isEmpty()) {
            return
        }

        try {
            TickableMob toSpawn = RandomUtils.getRandom(tier.spawnableTypes).newInstance(level, "§5DZ MOB ${tier.displayName}", scale)
            if (toSpawn == null) return

            def mob = toSpawn.getMob()

            def randomLoc = getRandomSpawnLoc()

            mob.moveTo(randomLoc.getX(), randomLoc.getY(), randomLoc.getZ(), randomLoc.getYaw(), randomLoc.getPitch())
            level.addEntity(mob, CreatureSpawnEvent.SpawnReason.CUSTOM)

            toSpawn.setCustomName(toSpawn.getMobCustomName(), true)
            mob.getBukkitEntity().getPersistentDataContainer().set(Darkzone.dzMobKey, PersistentDataType.STRING, tier.toString())
            mob.getBukkitEntity().getPersistentDataContainer().set(Darkzone.dzSpawnerKey, PersistentDataType.STRING, spawnerId.toString())

            spawnedMobs.put(mob.getUUID(), toSpawn)
        } catch (Exception exception) {
            exception.printStackTrace()
        }
    }

    def ensurePlaced() {
        def world = Bukkit.getWorld(position.world)
        if (world == null) return

        def loc = position.getLocation(world)
        if (loc == null) return

        def block = loc.getBlock()
        if (block.getType() != tier.getSpawnerMaterial()) {
            block.setType(tier.getSpawnerMaterial())
        }

        updateHologram()
    }

    def clearMobs() {
        int count = spawnedMobs.size()
        spawnedMobs.values().each { mob ->
            mob.killMob()
        }
        spawnedMobs.clear()

        if (count > 0) {
            updateHologram()
        }

        return count
    }

    Location getRandomSpawnLoc() {
        def world = Bukkit.getWorld(position.world)
        if (world == null) return null

        def loc = position.getLocation(world)
        if (loc == null) return null

        def random = ThreadLocalRandom.current()
        loc = loc.clone().add(random.nextDouble(-5, 5), random.nextDouble(2, 4), random.nextDouble(-5, 5))

        if (loc.getBlock().getType().isSolid()) {
            return getRandomSpawnLoc()
        }

        return loc
    }
}
