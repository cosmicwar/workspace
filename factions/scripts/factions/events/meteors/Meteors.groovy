package scripts.factions.events.meteors


import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.random.RandomSelector
import org.starcade.starlight.helper.utils.Players
import scripts.factions.eco.loottable.v2.api.LootTable
import scripts.factions.eco.loottable.LootTableHandler
import scripts.factions.eco.loottable.v2.api.LootTableCategory
import scripts.factions.eco.loottable.v2.api.Reward
import scripts.shared.features.holograms.HologramRegistry
import scripts.shared.features.holograms.HologramTracker
import scripts.shared.legacy.ToggleUtils
import scripts.shared.legacy.utils.BroadcastUtils
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.PAPI
import scripts.shared.utils.Persistent
import scripts.shared.utils.Temple
import scripts.shared.visuals.floating.FloatingBlock
import scripts.shared.visuals.floating.FloatingEntityTracker

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors

@CompileStatic(TypeCheckingMode.SKIP)
class Meteors {

    static Map<String, ?> config
    static Map<Location, Envoy> activeEnvoys
    static AtomicLong activeTimer
    static NamespacedKey envoyChestKey = new NamespacedKey(Starlight.plugin, "envoys/envoychest")

    static LootTableCategory category

    Meteors() {
        reloadConfig()
        if (config == null) return

        category = LootTableHandler.getLootTableCategory("meteors", true)

        Map<String, ?> envoyTypes = config["types"] as Map<String, ?>
        envoyTypes.each { key, value ->
            category.getOrCreateTable("meteor_$key")
        }

        activeTimer = Persistent.of("envoys/activeTimer", new AtomicLong(config["timer"] as long)).get()
        activeEnvoys = Persistent.of("envoys/activeEnvoys", new ConcurrentHashMap<Location, Envoy>()).get()
        Commands.create().handler({ command ->
            if (command.sender().isOp() && !command.args().isEmpty()) {
                def label = command.arg(0).value().get()
                if (label == "start") {
                    envoyStart()
                    command.reply("§3§lMETEORS §f§l> §fYou have started an envoy§f!")
                } else if (label == "stop") {
                    envoyEnd(false)
                    command.reply("§3§lMETEORS §f§l> §fYou have stopped an envoy§f!")
                }
                return
            }

            command.reply("§3§lMETEORS §f§l> §cMeteors§f will be spawning in the warzone in §c" + (activeTimer.get() / 60).round(0) + " minutes§f!")
        }).register("envoy")

        Schedulers.async().runRepeating({
            def nextTime = activeTimer.addAndGet(-1)
            switch (nextTime) {
                case TimeUnit.MINUTES.toSeconds(40):
                case TimeUnit.MINUTES.toSeconds(30):
                case TimeUnit.MINUTES.toSeconds(20):
                case TimeUnit.MINUTES.toSeconds(10):
                case TimeUnit.MINUTES.toSeconds(5):
                case TimeUnit.MINUTES.toSeconds(1):
                    BroadcastUtils.broadcast("§3§lMETEORS §f§l> §cMeteors§f will be spawning in the warzone in §c${(nextTime / 60).round(0)} minutes§f!", { Player player -> return !ToggleUtils.hasToggled(player, "envoy_messages") })
                    break
                case TimeUnit.SECONDS.toSeconds(30):
                    BroadcastUtils.broadcast("§3§lMETEORS §f§l> §cMeteors§f will be spawning in the warzone in §c$nextTime seconds§f!", { Player player -> return !ToggleUtils.hasToggled(player, "envoy_messages") })
            }

            if (nextTime <= 0) {
                activeTimer.set(config["timer"] as long)
                envoyStart()
            }
        }, 1L, TimeUnit.SECONDS, 1L, TimeUnit.SECONDS)

        Events.subscribe(PlayerInteractEvent).handler { e ->
            if (!e.hasBlock()) {
                return
            }
            def blockLoc = e.clickedBlock.getLocation()
            def envoy = activeEnvoys.values().stream().filter(envoy -> envoy.location == blockLoc).findFirst().orElse(null)
            if (!envoy) {
                return
            }
            e.setCancelled(true)

            def player = e.player
            // No menu. generate a menu
            if (!envoy.menu) {
                List<Integer> slots = new ArrayList<>(0..27)
                Collections.shuffle(slots)

                Map<String, ?> typeConfig = config["types"][envoy.type] as Map<String, ?>
                MenuBuilder menuBuilder = new MenuBuilder(9 * 4, "${typeConfig["displayName"]} Crate")

                LootTable lootTable = category.getOrCreateTable("meteor_${envoy.type}")
                if (lootTable == null) {
                    Players.msg(player, "§3§lMETEORS §f§l> §cAn error occurred while opening this envoy§f!")
                    return
                }

                int min = typeConfig["min"] as int
                int max = typeConfig["max"] as int
                // change here
                int rewardCount = max > min ? ThreadLocalRandom.current().nextInt(min, max + 1) : min

                Map<Integer, Reward> rewards = new HashMap<>()

                rewardCount.times {
                    int slot = slots.remove(0)
                    def reward = lootTable.getRandomReward()
                    rewards.put(slot, reward)

                    menuBuilder.set(slot, reward.getItemStack(), { p, t, s ->
                        Reward reward2 = rewards.remove(s)
                        if (reward2 == null) return

                        menuBuilder.get().setItem(s, null)
                        Starlight.log.info("[Envoys] ${p.getName()} opened an envoy and got a ${reward2}")
                        Schedulers.async().execute {
                            reward2.giveReward(p)
                        }
                    })
                }

                menuBuilder.setCloseCallback { p ->
                    if (rewards.isEmpty()) {
                        activeEnvoys.remove(envoy.location)
                        envoy.location.getBlock().setType(Material.AIR)
                        envoy.location.world.spawnParticle(Particle.EXPLOSION_LARGE, envoy.location.getBlockX() + 0.5D, envoy.location.getBlockY(), envoy.location.getBlockZ() + 0.5D, 300, 0.01D, 0.01D, 0.01D, 0.05D)
                        if (envoy.hologram != null) {
                            HologramRegistry.get().unregister(envoy.hologram)
                        }
                    }
                }

                envoy.menu = menuBuilder.get()
            }

            if (!envoy.menu.getViewers().isEmpty()) {
                Players.msg(player, "§3§lMETEORS §f§l> §fThis envoy has already been opened!")
                return
            }

            player.openInventory(envoy.menu)
        }

        AtomicInteger tick = new AtomicInteger(0)
        Schedulers.async().runRepeating({
            boolean particles = tick.incrementAndGet() % 2 == 0
            for (def envoy : activeEnvoys.values()) {
                Location currentLocation = envoy.envoyEntity.floatingBlock.currentLocation.clone()
                if (FloatingEntityTracker.isTracked(envoy.envoyEntity.floatingBlock)) {
                    if (envoy.envoyEntity.yMot > -0.8D) {
                        envoy.envoyEntity.yMot -= 0.04D
                    }

                    envoy.envoyEntity.floatingBlock.move(0D, envoy.envoyEntity.yMot, 0D)
                    if (currentLocation.getY() <= envoy.envoyEntity.target.getY()) {
                        envoy.envoyEntity?.remove()
                        BroadcastUtils.broadcast("placing chest ${envoy.location.x}, ${envoy.location.y}, ${envoy.location.z}")
                        envoyPlaceChest(envoy)
                    }
                }
                if (particles) {
                    Location particleLocation = envoy.spawnedBlock == null ? envoy.envoyEntity.floatingBlock.currentLocation : envoy.spawnedBlock.location.add(0.5D, 0D, 0.5D)
                    if (envoy.type == "legendary") {
                        currentLocation.world.spawnParticle(Particle.FLAME, particleLocation.x, particleLocation.y, particleLocation.z, 20, 0.01D, 0.01D, 0.01D, 0.05D)
                    } else {
                        currentLocation.world.spawnParticle(Particle.SPELL_INSTANT, particleLocation.x, particleLocation.y, particleLocation.z, 10, 0.01D, 0.01D, 0.01D, 0.05D)
                    }
                }

            }
        }, 20L, 20L)

//        Events.subscribe(WorldTickEvent).handler { e ->
//            def world = e.world
//            if (world.getName() != config["worldName"]) {
//                return
//            }
//            boolean particles = tick++ % 2 == 0
//            for (def envoy : activeEnvoys.values()) {
//                Location currentLocation = envoy.envoyEntity.floatingBlock.currentLocation.clone()
//                if (FloatingEntityTracker.isTracked(envoy.envoyEntity.floatingBlock)) {
//                    if (envoy.envoyEntity.yMot > -0.8D) {
//                        envoy.envoyEntity.yMot -= 0.04D
//                    }
//
//                    envoy.envoyEntity.floatingBlock.move(0D, envoy.envoyEntity.yMot, 0D)
//                    if (currentLocation.getY() <= envoy.envoyEntity.target.getY()) {
//                        envoy.envoyEntity?.remove()
//                        envoyPlaceChest(envoy)
//                    }
//                }
//
//                if (particles) {
//                    Location particleLocation = envoy.spawnedBlock == null ? envoy.envoyEntity.floatingBlock.currentLocation : envoy.spawnedBlock.location.add(0.5D, 0D, 0.5D)
//                    if (envoy.type == "legendary") {
//                        world.spawnParticle(Particle.FLAME, particleLocation.x, particleLocation.y, particleLocation.z, 20, 0.01D, 0.01D, 0.01D, 0.05D)
//                    } else {
//                        world.spawnParticle(Particle.SPELL_INSTANT, particleLocation.x, particleLocation.y, particleLocation.z, 10, 0.01D, 0.01D, 0.01D, 0.05D)
//                    }
//                }
//            }
//        }

        Events.subscribe(ChunkLoadEvent.class).handler {
            if (it.world.name != config["worldName"]) return

            ServerLevel level = ((CraftWorld) it.world).getHandle()

            LevelChunk nmsChunk = level.getChunk(it.chunk.getX(), it.chunk.getZ())
            Map<BlockPos, BlockEntity> tileEntities = nmsChunk.getBlockEntities()
            tileEntities.each {entry ->
                if (!(entry.value instanceof ChestBlockEntity)) return

                BlockPos blockPos = entry.key

                Block block = it.world.getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ())
                Chest chest = block.getState() as Chest
                String envoyType = chest.getPersistentDataContainer().get(envoyChestKey, PersistentDataType.STRING)
                if (envoyType == null) return

                if (!activeEnvoys.containsKey(block.location)) {
                    block.setType(Material.AIR, false)
                }
            }
        }

        PAPI.registerPlaceholder("envoy-timer", { event ->
            if (!event.isOnline()) {
                return "§cError..."
            }
            return (activeTimer.get() / 60).round(0) + " minutes"
        })

        GroovyScript.addUnloadHook {
            unload()
        }
    }

    static void reloadConfig() {
        Starlight.watch("scripts/exec/$Temple.templeId/envoyconfig.groovy")

        config = Exports.get("envoyconfig", null)
    }

    static def envoyPlaceChest(Envoy envoy) {
        Schedulers.sync().execute {
            Block block = envoy.location.getBlock()
            envoy.spawnedBlock = block

            block.setType(Material.CHEST)
            Chest chestState = envoy.location.getBlock().getState() as Chest
            BlockData blockData = chestState.getBlockData()
            (blockData as Directional).setFacing(RandomUtils.getRandom([BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST]))
            chestState.setBlockData(blockData)
            chestState.getPersistentDataContainer().set(envoyChestKey, PersistentDataType.STRING, envoy.type)
            chestState.update()

            Map<String, ?> typeConfig = config["types"][envoy.type] as Map<String, ?>
            envoy.hologram = HologramRegistry.get().spawn("envoy_${ThreadLocalRandom.current().nextInt()}", envoy.location.clone().add(0.5D, 1.5D, 0.5D), ["${typeConfig["displayName"]} Crate"] as List<String>, false)
            chestState.setCustomName("${typeConfig["displayName"]} Crate")

            // debug location of envoy
            BroadcastUtils.broadcast("${envoy.location.x}, ${envoy.location.y}, ${envoy.location.z} - spawned envoy chest")

//        ThreadLocalRandom.current().nextInt(3, 6).times {
//            Location spawnLocation = envoy.location.clone().add(ThreadLocalRandom.current().nextDouble(-5D, 5D), 3D, ThreadLocalRandom.current().nextDouble(-5D, 5D))
//            (Exports.ptr("pve:spawnMob") as Closure)?.call(spawnLocation, RandomUtils.getRandom(["MEDIUM", "HARD"]))
//        }
        }

    }

    static def envoyStart() {
        envoyEnd(false)
        BroadcastUtils.broadcast("§3§lMETEORS §f§l> §cMeteors§f have spawned in the §cwarzone§f!", { Player player -> return !ToggleUtils.hasToggled(player, "envoy_messages") })
        for (def i = 0; i < (config["envoysPerSpawn"] as int); i++) {
            getRandomSpawnLocation().thenAccept((Location origin) -> {
                Schedulers.sync().execute {
                    if (origin == null) {
                        return
                    }
                    Location spawnLocation = origin.clone().add(0D, 100D, 0D)
                    FloatingBlock floatingBlock = new FloatingBlock(origin.world, spawnLocation.add(0.5D, 0D, 0.5D), Material.BEACON)
                    floatingBlock.track()
                    EnvoyEntity envoyEntity = new EnvoyEntity(origin, floatingBlock)

                    String envoyType = RandomSelector.weighted((config["types"] as Map<String, ?>).entrySet(), entry -> entry.value["weight"] as Double ?: Double.MIN_VALUE).pick().key
                    Envoy newEnvoy = new Envoy(origin, envoyType, envoyEntity)
                    activeEnvoys.put(origin, newEnvoy)
                    origin.chunk.addPluginChunkTicket(Starlight.plugin)
                }
            })
        }
    }

    static def unload() {
        envoyEnd(true)
    }

    static def envoyEnd(boolean unload) {
        for (def entry : activeEnvoys.entrySet()) {
            Envoy envoy = entry.value
            envoy.envoyEntity?.remove()
            if (envoy.hologram != null) {
                HologramRegistry.get().unregister(envoy.hologram)
            }
            def key = entry.getKey()
            def runnable = {
                key.getBlock().setType(Material.AIR)
            }
            if (unload) { // We aren't allowed to schedule on reboots, so do it anyway
                runnable()
            } else {
                Schedulers.sync().execute(runnable)
            }
        }
        activeEnvoys.clear()
    }

    static CompletableFuture<Location> getRandomSpawnLocation() {
        def world = Bukkit.getWorld(config["worldName"] as String)
        CompletableFuture retr = new CompletableFuture()
        Schedulers.sync().execute {
            try {
                def localWorld = BukkitAdapter.adapt(world)
                def regionMap = WorldGuard.getInstance().getPlatform().getRegionContainer().get(localWorld).getRegions()
                def collectedRegions = regionMap.keySet().stream().filter(e -> e.startsWith("envoy_")).map(e -> regionMap.get(e)).collect(Collectors.toList())

                ProtectedRegion region = collectedRegions.get(ThreadLocalRandom.current().nextInt(collectedRegions.size()))
                def x = ThreadLocalRandom.current().nextInt(region.minimumPoint.x, region.maximumPoint.x)
                def z = ThreadLocalRandom.current().nextInt(region.minimumPoint.z, region.maximumPoint.z)
                def y = region.maximumPoint.y

                while (y > region.minimumPoint.y - 1) {
                    if (world.getBlockAt(x, y - 1, z).getType().isOccluding() && world.getBlockAt(x, y, z).getType().isAir() && world.getBlockAt(x, y + 1, z).getType().isAir()) {
                        def loc = new Location(world, x, y, z)
                        retr.complete(loc)
                        return retr
                    } else {
                        y--
                    }
                }

                retr.complete(null)
                return retr
            } catch (Throwable e) {
                retr.completeExceptionally(e)
            }
        }
        return retr
    }

    static class Envoy {

        Location location
        String type
        transient EnvoyEntity envoyEntity = null
        transient HologramTracker hologram
        transient Block spawnedBlock
        Inventory menu = null

        Envoy(Location location, String type, EnvoyEntity envoyEntity) {
            this.location = location
            this.type = type
            this.envoyEntity = envoyEntity
        }
    }

    static class EnvoyEntity {

        Location target
        FloatingBlock floatingBlock
        double yMot

        EnvoyEntity(Location target, FloatingBlock floatingBlock) {
            this.target = target
            this.floatingBlock = floatingBlock
        }

        void remove() {
            floatingBlock?.untrack()
        }

    }
}



