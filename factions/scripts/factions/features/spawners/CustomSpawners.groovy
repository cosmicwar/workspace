package scripts.factions.features.spawners

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.apache.commons.lang3.text.WordUtils
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.CreatureSpawner
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.tags.ItemTagType
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import scripts.factions.content.dbconfig.Config
import scripts.factions.content.dbconfig.DBConfigUtil
import scripts.factions.content.dbconfig.RegularConfig
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.addon.ftop.FTEntryType
import scripts.factions.core.faction.addon.ftop.FTopUtils
import scripts.factions.core.faction.claim.Board
import scripts.factions.data.DataManager
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.factions.data.obj.Position
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.StringUtils
import scripts.shared.utils.DataUtils

import java.util.concurrent.ConcurrentHashMap

@CompileStatic(TypeCheckingMode.SKIP)
class CustomSpawners {

    static Map<EntityType, String> friendlyNames = EntityType.values().collectEntries {
        String name = it.getName()
        if (name == null) name = "UNKNOWN"
        else name = name.toLowerCase().replaceAll("_", " ")

        return [it, "${WordUtils.capitalizeFully(name)}".toString().intern()]
    } as Map<EntityType, String>

    static final NamespacedKey SPAWNER_KEY = new NamespacedKey(Starlight.plugin, "custom_spawner")
    static NamespacedKey spawnerTypeKey = new NamespacedKey(Starlight.plugin, "spawnersSpawnerType")

    static Map<World, Map<Long/*chunk hash*/, SpawnerChunkCache>> spawnerChunkCache = new ConcurrentHashMap<>()

    static Map<String, Object> shopData

    static long chunkToHash(Chunk c) {
        return (long) c.z << 32 | (long) c.x & 0xffffffffL
    }

    static long chunkToHash(int x, int z) {
        return (long) z << 32 | (long) x & 0xffffffffL
    }

    static Config config

    static void main(String[] args) {
        GroovyScript.addUnloadHook {
            DataManager.getByClass(CSpawner.class).saveAll(false)

            Starlight.unload("~/mobs/MobStacker.groovy")
            Starlight.unload("~/collection/CollectionChests.groovy")
            Starlight.unload("~/drops/CustomDrops.groovy")
        }

        GroovyScript.addScriptHook(GroovyScript.HookType.RECOMPILE, {
            DataManager.getByClass(CSpawner.class).saveAll(false)
        })

        config = DBConfigUtil.createConfig("spawners", "§cSpawners", [], Material.SPAWNER)

        DataManager.register("custom_spawner", CSpawner.class)

        Starlight.watch("~/drops/CustomDrops.groovy")
        Starlight.watch("~/mobs/MobStacker.groovy")
        Starlight.watch("~/collection/CollectionChests.groovy")

        commands()
        events()
        exports()
        shopData = (Exports.ptr("shops") as Map<String, Map<String, Object>>).get("spawner_shop")

        Schedulers.sync().runLater({
            DataManager.getAllData(CSpawner.class).each { cSpawner ->
                def world = Bukkit.getWorld(cSpawner.world)
                if (world == null) {
                    return
                }

                def loc = cSpawner.position.getLocation(world)
                if (loc == null) {
                    DataManager.removeOne(cSpawner.spawnerId, CSpawner.class)
                    return
                }

                def block = loc.getBlock()
                if (block == null || block.getType() != Material.SPAWNER) {
                    DataManager.removeOne(cSpawner.spawnerId, CSpawner.class)
                    return
                }

                def creatureSpawner = block.getState() as CreatureSpawner
                if (creatureSpawner == null) {
                    DataManager.removeOne(cSpawner.spawnerId, CSpawner.class)
                    return
                }

                creatureSpawner.spawnedType = cSpawner.spawnerType
                creatureSpawner.minSpawnDelay = cSpawner.minSpawnerDelay
                creatureSpawner.maxSpawnDelay = cSpawner.maxSpawnerDelay
                creatureSpawner.update()

                SpawnerChunkCache cSpawnerCache = spawnerChunkCache.get(world)?.get(chunkToHash(loc.chunk))

                if (cSpawnerCache == null) {
                    cSpawnerCache = new SpawnerChunkCache(loc)
                    cSpawnerCache.spawnersInChunk.add(cSpawner)
                } else {
                    cSpawnerCache.spawnersInChunk.add(cSpawner)
                }

                spawnerChunkCache.computeIfAbsent(world, { k -> new ConcurrentHashMap<>() }).put(chunkToHash(loc.getChunk()), cSpawnerCache)
            }

        }, 1L)
    }

    static def exports() {
        Exports.ptr("spawners:getCSpawner", { Location location -> getCSpawner(location, false) })
        Exports.ptr("spawners:getSpawnerChunkCache", { return spawnerChunkCache })
    }

    static def commands() {
        SubCommandBuilder command = new SubCommandBuilder("cspawner").defaultAction {

        }
        command.create("give").requirePermission("starlight.admin").usage("<spawner> [count]").register { cmd ->
            if (cmd.args().size() == 1) {
                String entityName = cmd.arg(0).parseOrFail(String.class)
                def stack = createSpawner(EntityType.fromName(entityName))
                stack.setAmount(1)
                cmd.sender().getInventory().addItem(stack)
            }

            if (cmd.args().size() == 2) {
                String entityName = cmd.arg(0).parseOrFail(String.class)
                int count = cmd.arg(1).parseOrFail(Integer.class)
                def stack = createSpawner(EntityType.fromName(entityName))
                stack.setAmount(count)
                cmd.sender().getInventory().addItem(stack)
            }
        }.build()
    }

    static def events() {
        Events.subscribe(PlayerInteractEvent.class).handler { event ->
            if (event.action != Action.RIGHT_CLICK_BLOCK) return
            if(event.getHand() == EquipmentSlot.OFF_HAND) return

            def block = event.getClickedBlock()
            if (block == null || block.getType() != Material.SPAWNER) return

            def spawner = getCSpawner(block.location, false)
            if (spawner == null) return

            def player = event.getPlayer()
            Players.msg(player, "§] §> §e${spawner.spawnerStackSize} ${spawner.spawnerType.name} spawners")
        }

        // maybe dupe
        Events.subscribe(BlockPlaceEvent.class).handler { event ->
            if (event.getBlockPlaced().getType() != Material.SPAWNER) return

            Player player = event.getPlayer()
            ItemStack item = event.getItemInHand()
            if (!FastItemUtils.hasCustomTag(event.getItemInHand(), SPAWNER_KEY, ItemTagType.BYTE)) return

            SpawnerChunkCache cSpawnerCache = null
            if (spawnerChunkCache.get(event.player.world) != null)
                cSpawnerCache = spawnerChunkCache.get(event.player.world).get(chunkToHash(event.blockPlaced.chunk))

            int spawnerCount = 1
            if (item.amount > 1 && player.isSneaking()) {
                spawnerCount = item.amount
            }

            CSpawner similarSpawner = similarSpawnerNearby(item, event.block.location)
            if (similarSpawner != null) {
                event.setCancelled(true)

                if (similarSpawner.spawnerStackSize <= 256) {
                    int newStackSize = similarSpawner.spawnerStackSize + spawnerCount

                    if (newStackSize >= 256) {
                        int amtPlaced = 256 - similarSpawner.spawnerStackSize
                        similarSpawner.spawnerStackSize = 256
                        item.amount = newStackSize - 256

                        cSpawnerCache.totalSpawnerValue += getSpawnerTypeValue(similarSpawner.spawnerType) * amtPlaced
                        FTopUtils.addFTopEntry(Factions.getMember(player.getUniqueId()).getFactionId(), 1 * getSpawnerTypeValue(similarSpawner.spawnerType) * amtPlaced, FTEntryType.SPAWNER_VALUE)
                    } else {
                        int amtPlaced = spawnerCount
                        similarSpawner.spawnerStackSize = newStackSize
                        if (item.amount == 1) {
                            if (event.getHand() == EquipmentSlot.HAND) player.getInventory().setItemInMainHand(null)
                            else player.getInventory().setItemInOffHand(null)
                        }
                        else item.amount = item.amount - spawnerCount
                        cSpawnerCache.totalSpawnerValue += getSpawnerTypeValue(similarSpawner.spawnerType) * amtPlaced
                        FTopUtils.addFTopEntry(Factions.getMember(player.getUniqueId()).getFactionId(), 1 * getSpawnerTypeValue(similarSpawner.spawnerType) * amtPlaced, FTEntryType.SPAWNER_VALUE)
                    }

                    def world = event.block.world

                    similarSpawner.update()

                    def block = world.getBlockAt(similarSpawner.position.getLocation(world))
                    if (block == null || block.getType() != Material.SPAWNER) return

                    // ugly asf but it works
                    boolean update = false
                    def creatureSpawner = block.getState() as CreatureSpawner
                    if (creatureSpawner.minSpawnDelay != similarSpawner.minSpawnerDelay) {
                        creatureSpawner.setMinSpawnDelay(similarSpawner.minSpawnerDelay)
                        update = true
                    }
                    if (creatureSpawner.maxSpawnDelay != similarSpawner.maxSpawnerDelay) {
                        if (similarSpawner.maxSpawnerDelay > creatureSpawner.getMinSpawnDelay()) {
                            creatureSpawner.setMaxSpawnDelay(similarSpawner.maxSpawnerDelay)
                            update = true
                        }
                    }

                    if (update) creatureSpawner.update()

                    similarSpawner.queueSave()
                    return
                } else {
                    event.getPlayer().sendMessage("§] §> §cYou may not place spawners that have reached the max stack size so close together!")
                    return
                }
            }

            def spawner = getCSpawner(event.block.location)
            if (spawner == null) return

            spawner.spawnerStackSize = 1
            spawner.spawnerType = getSpawnerType(event.getItemInHand())
            spawner.world = event.blockPlaced.location.world.name
            spawner.position = new Position(event.blockPlaced.location.x, event.blockPlaced.location.y, event.blockPlaced.location.z)

            if (cSpawnerCache == null) {
                cSpawnerCache = new SpawnerChunkCache(event.blockPlaced.location)
                cSpawnerCache.spawnersInChunk.add(spawner)
            } else {
                cSpawnerCache.spawnersInChunk.add(spawner)
            }
            cSpawnerCache.totalSpawnerValue += getSpawnerTypeValue(spawner.spawnerType)
            FTopUtils.addFTopEntry(Factions.getMember(player.getUniqueId()).getFactionId(), 1 * getSpawnerTypeValue(spawner.spawnerType), FTEntryType.SPAWNER_VALUE)

            spawnerChunkCache.computeIfAbsent(event.player.world, { k -> new ConcurrentHashMap<>() }).put(chunkToHash(event.block.chunk), cSpawnerCache)
            updatePlacedBlock(event.getBlockPlaced(), spawner)
        }

        Events.subscribe(EntityExplodeEvent.class).handler({ event ->
            for (Block block : event.blockList()) {
                if (block == null || block.getType() != Material.SPAWNER) continue

                CreatureSpawner spawner = block.getState() as CreatureSpawner
                if (!DataUtils.hasTag(spawner, SPAWNER_KEY, PersistentDataType.STRING)) continue

                def spawnerId = spawner.getPersistentDataContainer().get(SPAWNER_KEY, PersistentDataType.STRING)
                def cSpawner = getCSpawner(spawnerId, false)
                if (cSpawner == null) continue

                SpawnerChunkCache cache = spawnerChunkCache.get(spawner.getWorld()).get(chunkToHash(spawner.getChunk()))
                cache.spawnersInChunk.remove(cSpawner)
                DataManager.removeOne(spawnerId, CSpawner.class)

                Board board = Factions.getBoard(block.world)

                FTopUtils.addFTopEntry(board.getClaimAtPos(block.location).getFactionId(), -1 * getSpawnerTypeValue(cSpawner.spawnerType), FTEntryType.SPAWNER_VALUE)
                cache.totalSpawnerValue -= getSpawnerTypeValue(cSpawner.spawnerType) * cSpawner.spawnerStackSize

                ItemStack spawnerStackToDrop = createSpawner(cSpawner.spawnerType)
                spawnerStackToDrop.setAmount((cSpawner.spawnerStackSize * 0.7).round() as Integer)
                block.getWorld().dropItem(block.getLocation(), spawnerStackToDrop)
            }
        })

        Events.subscribe(BlockBreakEvent.class).handler { event ->
            def player = event.getPlayer()
            def block = event.getBlock()

            if (block == null || block.getType() != Material.SPAWNER) return
            CreatureSpawner spawner = block.getState() as CreatureSpawner

            //datatype.string?? or .byte
            if (!DataUtils.hasTag(spawner, SPAWNER_KEY, PersistentDataType.STRING)) return

            def spawnerId = spawner.getPersistentDataContainer().get(SPAWNER_KEY, PersistentDataType.STRING)
            def cSpawner = getCSpawner(spawnerId, false)
            if (cSpawner == null) return

            event.dropItems = false

            //need to add ammount handling
            if (cSpawner.spawnerStackSize == 1) {
                SpawnerChunkCache cache = spawnerChunkCache.get(spawner.getWorld()).get(chunkToHash(spawner.getChunk()))
                cache.spawnersInChunk.remove(cSpawner)
                DataManager.removeOne(spawnerId, CSpawner.class)

                cache.totalSpawnerValue -= getSpawnerTypeValue(cSpawner.spawnerType)
                FTopUtils.addFTopEntry(Factions.getMember(player.getUniqueId()).getFactionId(), -1 * getSpawnerTypeValue(cSpawner.spawnerType), FTEntryType.SPAWNER_VALUE)

                block.getWorld().dropItem(block.getLocation(), createSpawner(cSpawner.spawnerType))
            } else if (cSpawner.spawnerStackSize > 1) {
                SpawnerChunkCache cache = spawnerChunkCache.get(spawner.getWorld()).get(chunkToHash(spawner.getChunk()))
                if (player.isOp() && player.isSneaking()) {
                    cache.spawnersInChunk.remove(cSpawner)
                    DataManager.removeOne(spawnerId, CSpawner.class)

                    cache.totalSpawnerValue -= getSpawnerTypeValue(cSpawner.spawnerType) * cSpawner.spawnerStackSize
                    FTopUtils.addFTopEntry(Factions.getMember(player.getUniqueId()).getFactionId(), -1 * getSpawnerTypeValue(cSpawner.spawnerType) * cSpawner.spawnerStackSize, FTEntryType.SPAWNER_VALUE)
                    return
                }

                event.setCancelled(true)

                cache.totalSpawnerValue -= getSpawnerTypeValue(cSpawner.spawnerType)
                FTopUtils.addFTopEntry(Factions.getMember(player.getUniqueId()).getFactionId(), -1 * getSpawnerTypeValue(cSpawner.spawnerType), FTEntryType.SPAWNER_VALUE)

                block.getWorld().dropItem(block.getLocation(), createSpawner(cSpawner.spawnerType))
                cSpawner.spawnerStackSize -= 1
                cSpawner.queueSave()
            }
            Players.msg(player, "§] §> §cYou have broken a spawner!")
        }
    }

    /**
     * Places a spawner in existing stack if there is a valid one nearby.
     * @param spawner
     * @param location
     * @return whether or not a similar spawner is nearby
     */
    static CSpawner similarSpawnerNearby(ItemStack spawner, Location location) {
        Location min = new Location(location.getWorld(), location.getX() - 2, location.getY() - 2, location.getZ() - 2)
        Location max = new Location(location.getWorld(), location.getX() + 2, location.getY() + 2, location.getZ() + 2)
        //loop in a radius of 2 around the center
        for (int x = (int) min.getX(); x <= (int) max.getX(); x++) {
            for (int z = (int) min.getZ(); z <= (int) max.getZ(); z++) {
                for (int y = (int) min.getY(); y <= (int) max.getY(); y++) {
                    Location toCheck = new Location(location.getWorld(), x, y, z)
                    if (toCheck == location) continue
                    if (toCheck.getBlock().getType() != Material.SPAWNER) continue
                    CreatureSpawner cs = (CreatureSpawner) toCheck.getBlock().getState()
                    if (cs.getSpawnedType() != getSpawnerType(spawner)) continue
                    return getCSpawner(toCheck, false)
                }
            }
        }
        return null
    }

    static Set<CSpawner> getCachedSpawners(Chunk chunk) {
        if (chunk == null) return null
        if (spawnerChunkCache.get(chunk.getWorld()) == null) return null

        SpawnerChunkCache sChunkCache = spawnerChunkCache.get(chunk.getWorld()).get(chunkToHash(chunk))
        if (sChunkCache == null) return null

        return sChunkCache.spawnersInChunk
    }

    static CSpawner getCSpawner(String id, boolean create = true) {
        return DataManager.getData(id, CSpawner.class, create)
    }

    static CSpawner getCSpawner(Location location, boolean create = true) {
        def cache = getCachedSpawners(location.getChunk())
        if (cache == null && !create) return null

        if (cache) {
            Position locAsPos = new Position(location.getBlockX(), location.getBlockY(), location.getBlockZ())

            for (CSpawner cSpawner : cache) {
                if (cSpawner.position == locAsPos)
                    return cSpawner
            }
        }

        return getCSpawner("$location.x:$location.y:$location.z:$location.world.name", create)
    }

    static Collection<CSpawner> getAllCSpawners() {
        return DataManager.getAllData(CSpawner.class)
    }

    static def updatePlacedBlock(Block block, CSpawner cSpawner) {
        if (block == null || block.getType() != Material.SPAWNER) return

        def newBlock = block.getWorld().getBlockAt(block.getLocation())

        CreatureSpawner spawner = newBlock.getState() as CreatureSpawner
        spawner.getPersistentDataContainer().set(SPAWNER_KEY, PersistentDataType.STRING, cSpawner.spawnerId)
        spawner.setSpawnedType(cSpawner.spawnerType)
        spawner.setMinSpawnDelay(cSpawner.minSpawnerDelay)
        spawner.setMaxSpawnDelay(cSpawner.maxSpawnerDelay)
        spawner.update()

        cSpawner.queueSave()
    }

    static EntityType getSpawnerType(ItemStack itemStack) {
        if (itemStack == null || itemStack.type != Material.SPAWNER) return null

        ItemMeta itemMeta = itemStack.getItemMeta()
        if (itemMeta == null) return null

        EntityType entityType = null
        String typeString = itemMeta.getPersistentDataContainer().get(spawnerTypeKey, PersistentDataType.STRING)
        if (typeString != null) {
            entityType = EntityType.fromName(typeString)
        } else if (itemMeta instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = itemMeta as BlockStateMeta
            BlockState blockState = blockStateMeta.getBlockState()
            if (blockState instanceof CreatureSpawner) {
                CreatureSpawner creatureSpawner = blockState as CreatureSpawner
                entityType = creatureSpawner?.getSpawnedType()
            }
        }

        return entityType
    }

    static ItemStack createSpawner(EntityType entityType) {
        def stack = constructSpawner(entityType)
        FastItemUtils.setCustomTag(stack, SPAWNER_KEY, ItemTagType.BYTE, 1 as byte)

        return stack
    }

    static ItemStack constructSpawner(EntityType entityType) {
        ItemStack itemStack = FastItemUtils.createItem(Material.SPAWNER, "§e${StringUtils.capitalize(friendlyNames.get(entityType))} Spawner", [])
        BlockStateMeta meta = (BlockStateMeta) itemStack.getItemMeta()
        if (meta != null) {
            CreatureSpawner spawner = (CreatureSpawner) meta.getBlockState()
            spawner.setSpawnedType(entityType)
            meta.setBlockState(spawner)
            meta.getPersistentDataContainer().set(spawnerTypeKey, PersistentDataType.STRING, entityType.name())
            itemStack.setItemMeta(meta)
        }

        return itemStack
    }

    static int getSpawnerTypeValue(EntityType entityType) {
        if (shopData == null) throw new Exception("shop not loaded successfully in CustomSpawners")

        def items = shopData.get("items")
        def spawnerConfig = null

        for (item in items) {
            def itemType = item['type']

            if (itemType == entityType.toString()) {
                spawnerConfig = item
                break
            }
        }

        if (spawnerConfig == null) return 0
        return spawnerConfig["buy"]
    }
}


@CompileStatic(TypeCheckingMode.SKIP)
class SpawnerChunkCache {
    World world
    int x, y, z
    transient Set<CSpawner> spawnersInChunk = Sets.newConcurrentHashSet()
    int totalSpawnerValue = 0
    int chunkX, chunkZ

    SpawnerChunkCache(Location location) {
        this.world = location.world
        this.x = location.getBlockX()
        this.y = location.getBlockY()
        this.z = location.getBlockZ()
        this.chunkX = location.getChunk().x
        this.chunkZ = location.getChunk().z
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        SpawnerChunkCache that = (SpawnerChunkCache) o

        if (x != that.x) return false
        if (y != that.y) return false
        if (z != that.z) return false
        if (world != that.world) return false

        return true
    }

    long chunkHashCode() {
        Chunk chunk = new Location(world, x as double, y as double, z as double).getChunk()
        return (long) chunk.z << 32 | (long) chunk.x & 0xffffffffL
    }

    int hashCode() {
        int result
        result = (world != null ? world.hashCode() : 0)
        result = 31 * result + x
        result = 31 * result + y
        result = 31 * result + z
        return result
    }

    /**
     * Copmutes the total spawner value in the chunk based off of spawners in spawnersInChunk
     *
     * This method is used immediately after the cache is reloaded from the db so values from db
     * reflect the spawner value in the chunk.
     */
    void computeTotalSpawnerValue() {
        for (CSpawner spawner : spawnersInChunk) {
            totalSpawnerValue += CustomSpawners.getSpawnerTypeValue(spawner.spawnerType) * spawner.spawnerStackSize
        }
    }


}