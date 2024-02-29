package scripts.factions.features.spawners.mobs

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity
import org.bukkit.entity.*
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.*
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import scripts.factions.features.spawners.event.MobStackerSpawnMobEvent
import scripts.factions.features.spawners.event.StackedMobKillEvent
import scripts.factions.features.spawners.CSpawner
import scripts.shared.utils.DataUtils

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

@CompileStatic(TypeCheckingMode.SKIP)
class MobStacker {

    public static final NamespacedKey IS_STACKED_MOB = new NamespacedKey(Starlight.plugin, "is_stacked_mob")
    public static final NamespacedKey STACKED_MOB_STACK_SIZE = new NamespacedKey(Starlight.plugin, "stacked_mob_stack_size")
    public static final NamespacedKey OP_SPAWNER_LEVEL = new NamespacedKey(Starlight.plugin, "op_spawner_level")
//    static final NamespacedKey MOB_STACKER_KILL_WHOLE_STACK = new NamespacedKey(Starlight.plugin, "mobstackerKillWholeStack")

    public static Map<World, Map<EntityType, Map<Long, ChunkCache>>> typeCache = new ConcurrentHashMap<>()

    public static final int MAX_STACK_SIZE = 100_000
    public static final float STACK_DISTANCE = 8

    public static final List<EntityDamageEvent.DamageCause> fullStackExcludedCauses = Arrays.asList(
            EntityDamageEvent.DamageCause.LAVA,
            EntityDamageEvent.DamageCause.FALL,
            EntityDamageEvent.DamageCause.SUFFOCATION,
            EntityDamageEvent.DamageCause.CRAMMING,
            EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.VOID,
    )

    public static Map<EntityType, String> readableNames = new HashMap()
    public static Map<String, EntityType> searchableNames = new TreeMap<String, EntityType>(String.CASE_INSENSITIVE_ORDER) {
        {
            for (EntityType type : EntityType.values()) {
                if (type.getName() != null) {
                    put(type.getName(), type)
                    put(type.getName(), type)
                }
                put(type.name(), type)
            }
        }
    }

    static Set<EntityType> blacklistedTypes = new HashSet<EntityType>() {
        {
            add(EntityType.AREA_EFFECT_CLOUD) // blacklisted thingies~
            add(EntityType.FIREBALL)
        }
    }

    static Set<CreatureSpawnEvent.SpawnReason> blacklistedSpawnReasons = new HashSet<CreatureSpawnEvent.SpawnReason>() {
        {
            add(CreatureSpawnEvent.SpawnReason.CUSTOM) // blacklisted thingies~
        }
    }

    static Set<EntityType> blacklistedSpawnTypes = new HashSet<EntityType>() {
        {
            add(EntityType.PHANTOM) // blacklisted thingies~
        }
    }

    @CompileStatic
    static long chunkToHash(Chunk c) {
        return (long) c.z << 32 | (long) c.x & 0xffffffffL
    }

    static EntityType getType(String string) {
        EntityType t = (EntityType) searchableNames.get(string)
        if (t != null) return t
        return (EntityType) searchableNames.computeIfAbsent(string, (Function<String, EntityType>) { String s ->
            for (EntityType type : EntityType.values())
                if (type != EntityType.UNKNOWN && type.getName().is(s))
                    return (EntityType) type
            for (EntityType type : EntityType.values())
                if (type.getName().equalsIgnoreCase(s))
                    return (EntityType) type
            for (EntityType type : EntityType.values())
                if (type.name().equalsIgnoreCase(s))
                    return (EntityType) type
            throw new UnknownFormatConversionException("Unknown entity type: " + s)
        })
    }

    static {
        for (EntityType e : EntityType.values()) {
            String name = e.getName()
            if (name == null) name = "UNKNOWN"
            else name = name.toUpperCase().replaceAll("_", " ")
            readableNames[e] = name.intern()
        }
    }

//    Exports.ptr("spawners:getCSpawner", { Location location ->
//            def spawner = getCSpawner(location, false)
//            if (spawner == null) return null
//
//            return spawner
//        })

    static CSpawner getCSpawner(Location location) {
        return (Exports.ptr("spawners:getCSpawner") as Closure)?.call(location) as CSpawner
    }

    static void main(String[] args) {
        commands()
        events()

        Schedulers.sync().execute {
            Bukkit.getWorlds().each { world ->
                for (Entity entity : world.getEntities()) {
                    if (entity !instanceof LivingEntity) return

                    def living = entity as LivingEntity

                    if (DataUtils.hasTagInteger(living, IS_STACKED_MOB)) {
                        ChunkCache cache = getLocationOrNew(entity.getType(), world, living.getLocation())
                        if (cache.lastSpawnedEntity == null)
                            cache.lastSpawnedEntity = entity

                        setStackName(entity as LivingEntity, getStackSize(living), getOpSpawnerLevel(living))
                    }
                }
            }
        }
    }

    static def commands() {
        Commands.create().assertUsage("<mob> <amount>").assertPlayer().assertOp().handler { c ->

            def mobType = getType(c.arg(0).parseOrFail(String.class))
            def amount = c.arg(1).parseOrFail(Integer.class)
            def player = c.sender() as Player

            def nearby = findNearby(player.getLocation(), mobType, -1)
            if (nearby != null) {
                player.getWorld().spawnEntity(player.getLocation(), mobType, CreatureSpawnEvent.SpawnReason.SPAWNER) {
                    setStackSize(it as LivingEntity, amount)
                    setStackName(it as LivingEntity, amount, -1)
                    sanitize(it as LivingEntity)
                }

                return
            }

            setStackSize(nearby, getStackSize(nearby) + amount)
            setStackName(nearby, getStackSize(nearby), -1)
        }.register("summonstack")

        Commands.create().assertPlayer().assertOp().handler {cmd ->
            int killed = 0
            Bukkit.getWorlds().each {
                it.entities.each {
                    if (DataUtils.hasTag(it, IS_STACKED_MOB, PersistentDataType.INTEGER)) {
                        killed++
                        it.remove()
                    }
                }
            }

            cmd.reply("Killed ${killed} stacked mobs")
        }.register("dev/mobstacker/killall")
    }

    static def events() {
        Events.subscribe(SpawnerSpawnEvent.class).handler { event ->
            if (event.isCancelled()) return

            if (event.entity !instanceof LivingEntity) return

            LivingEntity entity = event.entity as LivingEntity
            EntityType entityType = event.entityType
            def entityLoc = entity.location
            def spawnerLoc = event.spawner.location

            if (blacklistedTypes.contains(entityType)) return

            if (blacklistedSpawnTypes.contains(entityType)) {
                event.setCancelled(true)
            }

            def cSpawner = getCSpawner(spawnerLoc)
            if (cSpawner == null) return

            int spawnCount = cSpawner.spawnCountAlgo()

            event.setCancelled(true)

            def nearby = findNearby(entityLoc, entityType, getOpSpawnerLevel(entity))
            if (nearby == null) {
                ChunkCache cache = getLocationOrNew(entityType, entityLoc.getWorld(), entityLoc)

                def newSpawned = entityLoc.world.spawnEntity(entityLoc, entityType, CreatureSpawnEvent.SpawnReason.SPAWNER) as LivingEntity
                newSpawned.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.0D)
                newSpawned.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(0.0D)
                cache.lastSpawnedEntity = newSpawned
                sanitize(newSpawned)

                setStackSize(newSpawned, spawnCount)
                setStackName(newSpawned, spawnCount, -1)

                MobStackerSpawnMobEvent mobStackerSpawnMobEvent = new MobStackerSpawnMobEvent(entityLoc, newSpawned.type, spawnCount)
                mobStackerSpawnMobEvent.callEvent()

                return
            }

            MobStackerSpawnMobEvent mobStackerSpawnMobEvent = new MobStackerSpawnMobEvent(entityLoc, nearby.type, getStackSize(nearby) + spawnCount)
            mobStackerSpawnMobEvent.callEvent()

            setStackSize(nearby, getStackSize(nearby) + spawnCount)
            setStackName(nearby, getStackSize(nearby), -1)

            return
        }

        Events.subscribe(CreatureSpawnEvent.class).handler { event ->
            if (event.isCancelled()) return
            Location loc = event.getLocation()

            EntityType type = event.getEntityType()
            if (blacklistedTypes.contains(type)) return

            if (blacklistedSpawnReasons.contains(event.getSpawnReason())) return

            if (blacklistedSpawnTypes.contains(type)) {
                event.setCancelled(true)
            }

            if (event.entity.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) return

            int stackSize = 1
            if (DataUtils.hasTagInteger(event.entity, IS_STACKED_MOB)) {
                stackSize = getStackSize(event.entity)
            }

            event.setCancelled(true)

            def nearby = findNearby(loc, type, -1)
            if (nearby == null) { // no nearby, spawn a new one
                ChunkCache cache = getLocationOrNew(type, loc.getWorld(), loc)

                def newSpawned = loc.world.spawnEntity(loc, type, CreatureSpawnEvent.SpawnReason.SPAWNER) as LivingEntity
                newSpawned.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.0D)
                newSpawned.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(0.0D)
                cache.lastSpawnedEntity = newSpawned
                sanitize(newSpawned)

                setStackSize(newSpawned, stackSize)
                setStackName(newSpawned, stackSize, -1)

                MobStackerSpawnMobEvent mobStackerSpawnMobEvent = new MobStackerSpawnMobEvent(loc, newSpawned.type, stackSize)
                mobStackerSpawnMobEvent.callEvent()

                return
            }

            MobStackerSpawnMobEvent mobStackerSpawnMobEvent = new MobStackerSpawnMobEvent(loc, nearby.type, stackSize)
            mobStackerSpawnMobEvent.callEvent()

            stackSize += getStackSize(nearby)

            setStackSize(nearby, stackSize)
            setStackName(nearby, stackSize, -1)
        }

        Events.subscribe(ChunkLoadEvent.class).handler { ChunkLoadEvent e ->
            for (Entity entity : e.chunk.getEntities()) {
                if (!DataUtils.hasTagInteger(entity, IS_STACKED_MOB)) {
                    continue
                }
                Location location = entity.getLocation()
                ChunkCache cache = getLocationOrNew(entity.getType(), e.chunk.getWorld(), location)

                if (cache.lastSpawnedEntity == null) {
                    cache.lastSpawnedEntity = entity
                }
            }
        }

        Events.subscribe(ChunkUnloadEvent.class).handler { ChunkUnloadEvent e ->
//            if (e.isCancelled()) return
            int cx = e.getChunk().getX()
            int cz = e.getChunk().getZ()
            long chunkHash = chunkToHash(e.getChunk())
            Map<EntityType, Map<Long, ChunkCache>> worldCache = typeCache.computeIfAbsent(e.getWorld(), { t -> new ConcurrentHashMap<>() })
            for (Map<Long, ChunkCache> map : worldCache.values()) {
                map.remove(chunkHash)
            }
        }

        Events.subscribe(EntityDeathEvent.class, EventPriority.MONITOR).handler { event ->
            if (event.drops.size() > 50)
                event.drops.subList(50, event.drops.size() - 1).clear()
        }

        Events.subscribe(EntityDeathEvent.class).handler { e ->
            LivingEntity entity = e.getEntity()
            EntityType entityType = e.getEntityType()

            if (entity instanceof Player || !(entity instanceof LivingEntity)) {
                return
            }
            int stackSize = getStackSize(entity)
            boolean runDamageLogic = false
            boolean doDrops = true
            int amountKilled = 1

            if (e.getEntity().getLastDamageCause() instanceof EntityDamageByBlockEvent) {
                return
            }

            EntityDamageEvent.DamageCause ldc = entity.getLastDamageCause()?.getCause()
            if (ldc == null) {
                return
            }
            if (ldc == EntityDamageEvent.DamageCause.DROWNING) {
                e.setCancelled(true)
                return
            }
            switch (ldc) {
                case null:
                    break
                case EntityDamageEvent.DamageCause.LAVA:
                case EntityDamageEvent.DamageCause.FIRE:
                    if (entityType == EntityType.IRON_GOLEM) amountKilled = stackSize - 1
                    else amountKilled = stackSize
                    break
                case EntityDamageEvent.DamageCause.FALL:
                case EntityDamageEvent.DamageCause.CRAMMING:
                case EntityDamageEvent.DamageCause.VOID:
                case EntityDamageEvent.DamageCause.SUFFOCATION:
                    if (entityType == EntityType.BLAZE) doDrops = false
                    amountKilled = stackSize
                    break
                default:
                    runDamageLogic = true
            }
            boolean countEntireStack = !fullStackExcludedCauses.contains(ldc)

            double dropMultiplier = 1D
            Player killer = entity.getKiller()
            if (killer != null) { // handle all player data updates
                if (killer.isOp() && killer.getGameMode() == GameMode.CREATIVE && killer.isSneaking())
                    amountKilled = stackSize
            }

            if (stackSize <= 1) {
                return // challenges?
            }

            if (amountKilled > stackSize) amountKilled = stackSize
            if (amountKilled < 1) amountKilled = 1

            if (killer instanceof Player && amountKilled > 1) {
                StackedMobKillEvent stackedMobKillEvent = new StackedMobKillEvent(e, stackSize, amountKilled - 1)
                // this event is purely for the additional amount killed
                stackedMobKillEvent.callEvent()
            }

            int opSpawnerLevel = getOpSpawnerLevel(entity)
//            if (opSpawnerLevel > 0) {
//                Closure<Integer> getOpSpawnerLootRate = Exports.ptr("opSpawnersGetLootMultiplierByLevel") as Closure<Integer>
//                if (getOpSpawnerLootRate != null) {
//                    int rate = getOpSpawnerLootRate.call(opSpawnerLevel)
//                    if (rate > 0) {
//                        e.drops.each { it.setAmount(it.amount * rate) }
//                    }
//                }
//            }

            if (amountKilled > 1) {
                List<ItemStack> drops = new ArrayList(e.drops)
                e.drops.clear()
                if (doDrops) {
                    int droppedItemsInChunk = 0

                    for (def queryEntity : e.getEntity().getLocation().getChunk().getEntities()) {
                        if (queryEntity.getType() == EntityType.DROPPED_ITEM) {
                            droppedItemsInChunk++
                        }
                    }

                    if (droppedItemsInChunk > 50) {
                        doDrops = false
                    }
                }

                if (doDrops) {
                    for (ItemStack item : (drops as List<ItemStack>)) {
                        if (entity.getFireTicks() != 1)
                            item.setType(cook(item.getType()))
                        int dropCounter = (item.getAmount() * amountKilled) * dropMultiplier as int
                        if (!runDamageLogic) dropCounter /= 5
                        for (/*yes*/ ; dropCounter > 64; dropCounter -= 64) {
                            ItemStack clone = item.clone()
                            clone.setAmount(64)
                            e.getDrops().add(clone)
                        }
                        item.setAmount(dropCounter)
                        e.getDrops().add(item)
                    }
                }


                if (countEntireStack) {
                    int xpDropped = (e.getDroppedExp() * amountKilled) / 3 as int
                    e.setDroppedExp(Math.floor(xpDropped) as int)
                } else {
                    int xpDropped = Math.max(e.getDroppedExp() / 5 as int, 1)
                    e.setDroppedExp(xpDropped)
                }
            }

            if (amountKilled < stackSize) {
                entity.getWorld().spawnEntity(entity.getLocation(), entityType, CreatureSpawnEvent.SpawnReason.SPAWNER) {
                    def living = it as LivingEntity
                    setStackSize(living, stackSize - amountKilled)
                    setStackName(living, stackSize - amountKilled, opSpawnerLevel)
                    sanitize(living)

                    living.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.0D)
                    living.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(0.0D)
                    living.setHealth(living.getHealth() / 2D)

                    living.setFireTicks(400)

                    Location loc = entity.getLocation()
                    for (int xo = -1; xo <= 1; xo++) {
                        for (int y = 0; y <= 15; y++) {
                            for (int zo = -1; zo <= 1; zo++) {
                                ChunkCache cc = getChunk(e.getEntityType(), loc.getWorld(), loc.getChunk())
                                if (cc != null) cc.lastSpawnedEntity = living
                            }
                        }
                    }
                }

                entity.remove()
            }
        }

        Events.subscribe(SlimeSplitEvent.class).handler { event ->
            if (event.isCancelled()) return
            if (event.getEntity().hasMetadata("mobstacker")) event.setCancelled(true)
        }
    }

    static List<String> names = [
            "§a§lCommon",
            "§9§lRare",
            "§5§lEpic",
            "§6§lLegendary",
            "§d§lMythical"
    ]

    static void setStackName(LivingEntity livingEntity, int stackSize, int opSpawnerLevel) {
        if (opSpawnerLevel > 0) {
            livingEntity.setCustomName("§c§l${stackSize}x ${names[opSpawnerLevel - 1]} ${readableNames.get(livingEntity.getType())}")
            // TODO
        } else {
            livingEntity.setCustomName(entityName(livingEntity.getType(), stackSize))
        }

        livingEntity.setCustomNameVisible(true)
    }

    static double distance2d(Location location1, Location location2) {
        return Math.sqrt(Math.pow(location1.getX() - location2.getX(), 2) + Math.pow(location1.getZ() - location2.getZ(), 2))
    }

    static LivingEntity findNearby(Location loc, EntityType type, int opSpawnerLevel) { //
        if (!type.isAlive()) return null
        ChunkCache cache = getLocationOrNew(type, loc.getWorld(), loc)
        if (cache.lastSpawnedEntity != null) {
            if (cache.lastSpawnedEntity.isValid()) {
                // update if new chunk
                Location eloc = cache.lastSpawnedEntity.location
                if (eloc.world == loc.world && distance2d(loc, eloc) < STACK_DISTANCE && getStackSize(cache.lastSpawnedEntity as LivingEntity) < MAX_STACK_SIZE) {
                    return cache.lastSpawnedEntity as LivingEntity
                }
            } else cache.lastSpawnedEntity = null
        }

        World world = loc.getWorld()
        org.bukkit.Chunk center = loc.getChunk()

        ServerLevel worldServer = ((CraftWorld) world).getHandle()
        List<LevelChunk> chunks = new ArrayList<>()
        for (int x = -1; x <= 1; x++) { // 3x3 chunks
            for (int z = -1; z <= 1; z++) {
                LevelChunk chunk = worldServer.getChunkIfLoaded(center.getX() + x, center.getZ() + z)
                if (chunk != null) {
                    chunks.add(chunk)
                }
            }
        }
        def stacks = 0

        LivingEntity nearby = null
        chunks.each {
            it.getChunkHolder().getEntityChunk().getChunkEntities().each { entity ->
                if (!(entity instanceof LivingEntity) || distance2d(loc, entity.getLocation()) > STACK_DISTANCE)
                    return
                def livingEntity = entity as LivingEntity
                if (livingEntity.getType() == type && isStackable(livingEntity) && opSpawnerLevel == getOpSpawnerLevel(livingEntity)) {
                    if (getStackSize(livingEntity) >= MAX_STACK_SIZE) {
                        if (++stacks == 5) {
                            cache.lastSpawnedEntity = livingEntity
                            nearby = cache.lastSpawnedEntity as LivingEntity
                            return
                        }

                        return
                    }

                    cache.lastSpawnedEntity = livingEntity
                    nearby = cache.lastSpawnedEntity as LivingEntity
                    return
                }
            }
        }

        return nearby
    }

//    static LivingEntity mergeLocation(Location location, EntityType) {
//
//    }

    static ChunkCache getChunk(EntityType type, World world, Chunk chunk) {
        return typeCache.get(world)?.get(type)?.get(chunkToHash(chunk))
    }

    static ChunkCache getBlockOrNew(EntityType type, World world, Block block) {
        int cx = block.location.blockX >> 4, cy = block.location.blockY >> 4, cz = block.location.blockZ >> 4
        return getChunkOrNew(type, world, cx, cy, cz, block.chunk)
    }

    static ChunkCache getLocationOrNew(EntityType type, World world, Location location) {
        int cx = location.blockX >> 4, cy = location.blockY >> 4, cz = location.blockZ >> 4
        return getChunkOrNew(type, world, cx, cy, cz, location.chunk)
    }

    static ChunkCache getChunkOrNew(EntityType type, World world, int x, int y, int z, Chunk chunk) {
        ChunkCache cache = getChunk(type, world, chunk)
        if (cache == null) {
            cache = new ChunkCache(world, x, y, z)
            Map<EntityType, Map<Long, ChunkCache>> cache1 = typeCache.computeIfAbsent(world, { t -> new ConcurrentHashMap<>() })
            Map<Long, ChunkCache> cache2 = cache1.computeIfAbsent(type, { t -> new ConcurrentHashMap<>() })
            cache2.put(chunkToHash(chunk), cache)
        }
        return cache
    }

    static int getStackSize(LivingEntity entity) {
        return DataUtils.getTagInteger(entity, STACKED_MOB_STACK_SIZE) ?: 1
    }

    static void setStackSize(LivingEntity entity, int size) {
        DataUtils.setTag(entity, STACKED_MOB_STACK_SIZE, PersistentDataType.INTEGER, size)
    }

    static int incrementStackSize(LivingEntity entity, int size) {
        def stackSize = getStackSize(entity)

        if (stackSize >= MAX_STACK_SIZE) return stackSize

        stackSize += size

        if (stackSize > MAX_STACK_SIZE) stackSize = MAX_STACK_SIZE

        setStackSize(entity, stackSize)
        return stackSize
    }

    static boolean isStackable(Entity e) {
        if (!(e instanceof LivingEntity))
            return false
        if (e.isDead()) return false
        int stackSize = getStackSize(e as LivingEntity)
        if (stackSize >= 1)
            return stackSize <= MAX_STACK_SIZE
        if (MobStacker.blacklistedTypes.contains(e.type))
            return false
        if (!(e as CraftLivingEntity).getHandle().spawnedViaMobSpawner)
            return false
        return true
    }

    static void sanitize(Entity entity) {
        if (entity instanceof LivingEntity) {
            entity.getEquipment().clear()
            entity.setCanPickupItems(false)
        }
        if (entity instanceof Ageable) {
            entity.setAdult()
            entity.setBreed(false)
        }
        if (entity instanceof Zombie)
            entity.setBaby(false)
        if (entity.getVehicle() != null)
            entity.getVehicle().remove()
        DataUtils.setTagInteger(entity, IS_STACKED_MOB, 1)
    }

    static String entityName(EntityType type, int stack) {
        return "§c§l${readableNames.get(type)} ${stack}x" // red for debug
    }

//    static int getAmount(Location loc, int vanilla) {
//        try {
//            Block block = loc.getBlock()
//            if (block.getType() != Material.SPAWNER) {
//                return 0
//            }
//
//            CreatureSpawner creatureSpawner = block.getState() as CreatureSpawner
//            Integer upgradableLevel = (Exports.ptr("upgradableSpawners:getSpawnAmount") as Closure<Integer>)?.call(creatureSpawner)
//            if (upgradableLevel != null) return upgradableLevel
//
//            return (Exports.ptr("stackedSpawners:getSpawnAmount") as Closure<Integer>)?.call(block.getState() as CreatureSpawner) ?: vanilla
//        } catch (Exception ignored) {
//            return vanilla
//        }
//    }

    static Material cook(Material type) {
        switch (type) {
            case Material.BEEF: return Material.COOKED_BEEF
            case Material.CHICKEN: return Material.COOKED_CHICKEN
            case Material.PORKCHOP: return Material.COOKED_PORKCHOP
//            case Material.TROPICAL_FISH: return Material.tropi
            case Material.MUTTON: return Material.COOKED_MUTTON
            case Material.RABBIT: return Material.COOKED_RABBIT
            default: return type
        }
    }

    static int getOpSpawnerLevel(LivingEntity livingEntity) {
        return DataUtils.getTagInteger(livingEntity, OP_SPAWNER_LEVEL) ?: -1
    }

    static void setOpSpawnerLevel(LivingEntity livingEntity, int level) {
        if (level > 0) {
            DataUtils.setTagInteger(livingEntity, OP_SPAWNER_LEVEL, level)
        }
    }

}

@CompileStatic(TypeCheckingMode.SKIP)
class ChunkCache {
    World world
    int x, y, z
    transient Entity lastSpawnedEntity = null

    ChunkCache(World world, int x, int y, int z) {
        this.world = world
        this.x = x
        this.y = y
        this.z = z
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ChunkCache that = (ChunkCache) o

        if (x != that.x) return false
        if (y != that.y) return false
        if (z != that.z) return false
        if (world != that.world) return false

        return true
    }

    int hashCode() {
        int result
        result = (world != null ? world.hashCode() : 0)
        result = 31 * result + x
        result = 31 * result + y
        result = 31 * result + z
        return result
    }
}


