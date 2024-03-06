package scripts.factions.features.spawners.collection

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import scripts.shared.data.string.StringDataManager
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.data.obj.Position
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.DataUtils

import java.util.concurrent.ConcurrentHashMap

// TODO: implement logging from sales
@CompileStatic(TypeCheckingMode.SKIP)
class CollectionChests {

    static final NamespacedKey CHEST_KEY = new NamespacedKey(Starlight.plugin, "collection_chest")

    public static Map<World, Map<Long/*chunk hash*/, ChunkCache>> chestChunkCache = new ConcurrentHashMap<>()

    static Set<CollectionChest> chests = Sets.newConcurrentHashSet()

    @CompileStatic
    static long chunkToHash(Chunk c) {
        return (long) c.z << 32 | (long) c.x & 0xffffffffL
    }

    public static final List<EntityType> VALID_ENTITIES = [
            EntityType.CREEPER,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.ZOMBIE,
            EntityType.SLIME,
            EntityType.GHAST,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.ENDERMAN,
            EntityType.CAVE_SPIDER,
            EntityType.SILVERFISH,
            EntityType.BLAZE,
            EntityType.MAGMA_CUBE,
            EntityType.ENDER_DRAGON,
            EntityType.WITHER,
            EntityType.BAT,
            EntityType.WITCH,
            EntityType.ENDERMITE,
            EntityType.GUARDIAN,
            EntityType.SHULKER,
            EntityType.PIG,
            EntityType.SHEEP,
            EntityType.COW,
            EntityType.CHICKEN,
            EntityType.SQUID,
            EntityType.WOLF,
            EntityType.MUSHROOM_COW,
            EntityType.SNOWMAN,
            EntityType.OCELOT,
            EntityType.IRON_GOLEM,
            EntityType.VILLAGER,
    ]

    static Set<Material> acceptedItems = [
            //pve mob drops
            Material.COOKED_COD,
            Material.COOKED_CHICKEN,
            Material.COOKED_PORKCHOP,
            Material.COOKED_BEEF,
            Material.COOKED_MUTTON,
            Material.COOKED_SALMON,
            Material.TURTLE_EGG,
            Material.EGG,

            //pvp mob drops
            Material.NAUTILUS_SHELL,
            Material.STRING,
            Material.ROTTEN_FLESH,
            Material.BONE,
            Material.ENDER_PEARL,
            Material.BLAZE_ROD,
            Material.MAGMA_CREAM,
            Material.GHAST_TEAR,
            Material.BOOK,
            Material.POPPY,
            Material.IRON_INGOT,
            Material.GUNPOWDER,
            Material.HEART_OF_THE_SEA,
            Material.WITHER_ROSE,
            Material.PUFFERFISH,
            Material.GLOWSTONE_DUST,
            Material.SLIME_BALL,

            // farming drops
            Material.WHEAT_SEEDS,
            Material.WHEAT,
            Material.BEETROOT_SEEDS,
            Material.BEETROOT,
            Material.BAMBOO,
            Material.CARROT,
            Material.POTATO,
            Material.CACTUS,
            Material.SUGAR_CANE,
            Material.MELON_SLICE,
            Material.MELON,
            Material.PUMPKIN,
            Material.COCOA_BEANS,
            Material.NETHER_WART,
            Material.BLAZE_POWDER,
            Material.NETHER_STAR,
    ]

    CollectionChests() {
        GroovyScript.addUnloadHook {
            chests.each {
                it.removeHologram(true)
            }
            StringDataManager.getByClass(CollectionChest.class).each { it.saveAll(false) }
        }

        StringDataManager.register("collection_chest", CollectionChest.class)

        StringDataManager.getAllData(CollectionChest.class).each { chest ->
            // ugh so sloppy ik :(
            // update chunk cache :]
            Schedulers.async().runLater({
                def world = Bukkit.getWorld(chest.world)
                if (world == null) return

                def loc = chest.position.getLocation(world)
                if (loc == null) return

                chest.spawnHologram(false)

                def chunkCache = new ChunkCache(loc)
                chunkCache.collectionChest = chest

                chestChunkCache.computeIfAbsent(world, { k -> new ConcurrentHashMap<>() }).put(chunkToHash(loc.getChunk()), chunkCache)
                chests.add(chest)
            }, 5l)
        }



        Schedulers.async().runRepeating({
            chests.each {it.updateHologram() }
        }, 50L, 50l) // 2.5 seconds

        commands()
        events()
    }

    static def killeveryone() {
        Schedulers.async().execute {
            StringDataManager.wipe(CollectionChest.class)

            chestChunkCache.each { world, map ->
                map.each { hash, chunkCache ->
                    def chest = chunkCache.collectionChest
                    if (chest == null) return

                    chest.removeHologram(true)

                    // remove all chests from the world by converting them to air
                    def loc = chest.position.getLocation(world)
                    if (loc == null) return

                    def block = loc.getBlock()
                    if (block == null) return

                    Schedulers.sync().execute { block.setType(Material.AIR) }
                }
            }

            chestChunkCache.clear()
        }
    }


    static def commands() {
        SubCommandBuilder command = new SubCommandBuilder("collectionchest", "collection").defaultAction {

        }

        command.create("debugloc").requirePermission("starlight.admin").register { cmd ->
            chestChunkCache.each { world, map ->
                map.each { hash, chunkCache ->
                    cmd.reply("§e${chunkCache.world.name} §7${chunkCache.x} §7${chunkCache.y} §7${chunkCache.z}")
                    cmd.reply("§eCollection Chest: ${chunkCache.collectionChest != null ? "True" : "False"}")
                }
            }
        }.create("give").requirePermission("starlight.admin").register { cmd ->
            cmd.sender().getInventory().addItem(createCollectionChest())
        }.create("killeveryone").requirePermission("starlight.admin").register { cmd ->
            cmd.reply("executing...")
            Schedulers.sync().execute { killeveryone() }
            cmd.reply("it is done master")
        }.build()
    }

    static def events() {
        /**
         * Placing a collection chest
         */
        Events.subscribe(BlockPlaceEvent.class).handler { event ->
            def player = event.getPlayer()
            if (event.getBlockPlaced().getType() != Material.CHEST) return

            if (FastItemUtils.hasCustomTag(event.getItemInHand(), CHEST_KEY, ItemTagType.BYTE)) {
                if (chestChunkCache.get(event.player.world) != null && chestChunkCache.get(event.player.world).containsKey(chunkToHash(event.block.chunk))) {
                    event.player.sendMessage("§cYou may not place more than one collection chest per chunk.")
                    event.setCancelled(true)
                    return
                }
                ChunkCache chunk = new ChunkCache(event.block.location)

                def collectionChest = getChest(event.block.location)
                collectionChest.position = new Position(event.block.getX(), event.block.getY(), event.block.getZ())
                collectionChest.world = event.block.getWorld().getName()

                collectionChest.spawnHologram()

                chunk.collectionChest = collectionChest

                chestChunkCache.computeIfAbsent(event.player.world, { k -> new ConcurrentHashMap<>() }).put(chunkToHash(event.block.chunk), chunk)
                updatePlacedBlock(event.getBlockPlaced(), collectionChest)

                Players.msg(player, "§] §> §aYou have placed a collection chest.")
            }
        }

        /**
         * Breaking a collection chest
         */
        Events.subscribe(BlockBreakEvent.class).handler { event ->
            def player = event.getPlayer()
            def block = event.getBlock()

            if (block == null || block.getType() != Material.CHEST) return

            Chest chest = block.getState() as Chest
            if (!DataUtils.hasTag(chest, CHEST_KEY, PersistentDataType.STRING)) return

            def chestId = chest.getPersistentDataContainer().get(CHEST_KEY, PersistentDataType.STRING)

            def collectionChest = getChest(chestId, false)
            if (collectionChest == null) return

            collectionChest.removeHologram(true)

            StringDataManager.removeOne(chestId, CollectionChest.class)
            chestChunkCache.get(chest.getWorld()).remove(chunkToHash(chest.getChunk()))

            event.dropItems = false

            block.setType(Material.AIR)
            block.getWorld().dropItem(block.getLocation(), createCollectionChest())

            Players.msg(player, "§] §> §cYou have broken a collection chest.")
            // handle dropping | selling contents of chest?
        }

        /**
         *  i dont even know
         */
        Events.subscribe(BlockRedstoneEvent.class).handler { event ->
            def block = event.getBlock()

            if (block == null || block.getType() != Material.CHEST) return

            Chest chest = block.getState() as Chest
            if (!DataUtils.hasTag(chest, CHEST_KEY, PersistentDataType.STRING)) return

            def chestId = chest.getPersistentDataContainer().get(CHEST_KEY, PersistentDataType.STRING)

            def collectionChest = getChest(chestId, false)
            if (collectionChest == null) return

            if (event.getNewCurrent() > 0) {
                chest.collect()
            }
        }

        /**
         * Opening a collection chest
         */
        Events.subscribe(PlayerInteractEvent.class).handler { event ->
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return
            def block = event.getClickedBlock()

            if (block == null || block.getType() != Material.CHEST) return

            Chest chest = block.getState() as Chest
            if (!DataUtils.hasTag(chest, CHEST_KEY, PersistentDataType.STRING)) return

            def chestId = chest.getPersistentDataContainer().get(CHEST_KEY, PersistentDataType.STRING)

            def collectionChest = getChest(chestId, false)
            if (chest == null) return

            event.setCancelled(true)
            openGui(event.getPlayer(), collectionChest)
        }

        /**
         * Mob drops in collection chest chunk
         */
        Events.subscribe(EntityDeathEvent.class).handler { event ->
//            Bukkit.broadcastMessage("collection: ${event.getDrops().toString()}")

            Entity entity = event.getEntity()
            if (entity == null) return

            if (!VALID_ENTITIES.contains(entity.getType())) return

            def chunkCache = chestChunkCache?.get(entity.getWorld())?.find { it.key == chunkToHash(entity.getChunk()) }?.getValue()
            if (chunkCache == null) return

            def collectionChest = chunkCache.collectionChest
            if (collectionChest == null) return

            boolean save = false

            event.getDrops().removeIf {
                if (!acceptedItems.contains(it.getType())) return false
                def entry = collectionChest.entries.find { entry -> entry.material == it.getType() }
                if (entry == null) return false
                if (!entry.enabled) return false

                save = true
                entry.amount += it.getAmount()
                return true
            }

            if (save) collectionChest.queueSave()
        }
    }


    /**
     *  GUI Handling
     */
    static def openGui(Player player, CollectionChest chest) {
        List<String> uniqueCategories = chest.entries.collect { it.getCategoryId() }.unique()

        if (uniqueCategories.size() > 45) {
            Players.msg(player, "§cThis collection chest has too many categories to display.")
            return
        }

        int builderSize = ((int) Math.ceil(uniqueCategories.size() / 9.0D) * 9) + 9
        MenuBuilder builder = new MenuBuilder(builderSize, "§eCollection Chest")

        CurrencyStorage money = Exports.ptr("money") as CurrencyStorage

        double totalValue = 0.0D
        int index = 2
        uniqueCategories.each { categoryId ->
            int amount = 0
            double value = 0.0D

            chest.entries.findAll { entry -> entry.categoryId == categoryId }.each {
                amount += it.amount
                value += (it.getSellAmount() <= 0.0D ? 0.0D : it.getSellAmount()) * it.amount
                totalValue += value
            }

            ItemStack stack = null

            if (categoryId == "MOB_DROPS") {
                stack = FastItemUtils.createSkull(EntityType.CREEPER, "§cMob Drops", [
                        "§7Amount: §e${amount}",
                        "§7Value: §e${value}",
                        "",
                        "§7Left-Click to open.",
                        "§7Right-Click to Sell-All.",
                ])
            } else if (categoryId == "VALUABLE") {
                stack = FastItemUtils.createItem(Material.NETHERITE_INGOT, "§dValuables", [
                        "§7Amount: §e${amount}",
                        "§7Value: §e${value}",
                        "",
                        "§7Left-Click to open.",
                        "§7Right-Click to Sell-All.",
                ])
            } else if (categoryId == "FARMING") {
                stack = FastItemUtils.createItem(Material.WHEAT, "§aFarming", [
                        "§7Amount: §e${amount}",
                        "§7Value: §e${value}",
                        "",
                        "§7Left-Click to open.",
                        "§7Right-Click to Sell-All.",
                ])
            }

            FastItemUtils.addGlow(stack)

            builder.set(index, stack, { p, t, s ->
                if (t == ClickType.RIGHT) {
                    if (value <= 0.0D) {
                        Players.msg(player, "§cThis category is empty.")
                        return
                    }

                    money.add(p, BigDecimal.valueOf(value))
                    Players.msg(p, "§] §> §aYou have sold all items in this category for §e\$${value}§a.")
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F)

                    chest.entries.findAll { it.categoryId == categoryId }.each {
                        it.amount = 0
                    }

                    chest.queueSave()
                    openGui(p, chest)
                } else if (t == ClickType.LEFT) {
                    openCategoryGui(p, chest, categoryId)
                }
            })

            index += 2
        }

        builder.set(builderSize - 3, FastItemUtils.createItem(Material.DIAMOND, "§bToggle Hologram", ["§7Toggle Chest Hologram", "", "§3State: ${chest.hologramEnabled ? "§a§lENABLED" : "§c§lDISABLED"}",], false), { p, t, s ->
            if (chest.hologramEnabled) chest.removeHologram()
            else chest.spawnHologram()

            chest.queueSave()
            openGui(p, chest)
        })
        builder.set(builderSize - 5, FastItemUtils.createItem(Material.DIAMOND, "§bSell All", ["§7Sell All items in this chest.", "", "§7Value: §a\$${totalValue}",], false), { p, t, s ->
            def value = 0.0D
            chest.entries.each {
                value += (it.getSellAmount() <= 0.0D ? 0.0D : it.getSellAmount()) * it.amount
            }

            if (value <= 0.0D) {
                Players.msg(player, "§cThis collection chest is empty.")
                return
            }

            money.add(p, BigDecimal.valueOf(value))
            Players.msg(p, "§] §> §aYou have sold all items in this chest for §e\$${value}§a.")
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F)

            chest.entries.each {
                it.amount = 0
            }

            chest.queueSave()
            openGui(p, chest)
        })
        builder.set(builderSize - 7, FastItemUtils.createItem(Material.PAPER, "§aAdd a Category", []), { p, t, s ->

            chest.queueSave()
            openGui(p, chest)
        })

        builder.openSync(player)
    }

    static NamespacedKey CATEGORY_KEY = new NamespacedKey(Starlight.plugin, "collection_chest_category")

    static def openCategoryGui(Player player, CollectionChest chest, String category) {
        MenuBuilder builder

        def categoryEntries = chest.getEntriesList().findAll { it.categoryId == category }

        if (categoryEntries.isEmpty()) {
            Players.msg(player, "§cThis category is empty.")
            return
        }

        // redo this to update data?
        def value = 0.0D
        categoryEntries.each {
            value += (it.getSellAmount() <= 0.0D ? 0.0D : it.getSellAmount()) * it.amount
        }

        CurrencyStorage money = Exports.ptr("money") as CurrencyStorage

        builder = MenuUtils.createPagedMenu("§eCollection Chest §7- §a\$$value", categoryEntries, { CollectionChestEntry chestEntry, Integer slot ->
            def item = FastItemUtils.createItem(chestEntry.material, "§e${chestEntry.material.name()}", [
                    "§7Amount: §e${chestEntry.amount}",
                    "§7Value: §e${chestEntry.amount * chestEntry.getSellAmount()}",
                    "§7Enabled: §e${chestEntry.enabled}",
                    "",
                    "§7Left-Click to Sell-All.",
                    "§7Middle-Click to Extract All.",
                    "§7Right-Click to toggle."
            ], false)

            return item
        }, 1, true, [
                { Player p, ClickType t, int s ->
                    // TODO: check for perms here
                    def entry = categoryEntries.find { it.material == builder.get().getItem(s).getType() }
                    if (!entry) return
                    if (t == ClickType.RIGHT) {
                        entry.enabled = !entry.enabled
                        chest.queueSave()
                        openCategoryGui(p, chest, category)
                    } else if (t == ClickType.LEFT) {
                        def amount = entry.amount
                        if (amount <= 0) return

                        def entryValue = entry.getSellAmount() * amount

                        money.add(p, BigDecimal.valueOf(entryValue))
                        Players.msg(p, "§] §> §aYou have sold §e${amount}x ${entry.material.name()} §afor §e\$${entryValue}§a.")
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F)

                        entry.amount = 0
                        chest.queueSave()
                        openCategoryGui(p, chest, category)
                    } else if (t == ClickType.MIDDLE) {
                        def amount = entry.amount
                        if (amount <= 0) return

                        entry.amount = 0
                        chest.queueSave()

                        def stack = new ItemStack(entry.material)

                        amount.times {
                            FastInventoryUtils.addOrBox(p.getUniqueId(), p, null, stack, null)
                        }

                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F)
                        openCategoryGui(p, chest, category)
                    }
                },
                { Player p, ClickType t, int s -> },
                { Player p, ClickType t, int s -> },
                { Player p, ClickType t, int s -> openGui(p, chest) },
        ])

        builder.openSync(player)
    }

    // TODO: pre generate collection chest ids, therefore we just keep stored data in a collection chest
    // TODO: dont have to handle anything on block break, will have to re do chest id system
    static ItemStack createCollectionChest() {
        def stack = FastItemUtils.createItem(Material.CHEST, "§eCollection Chest", [
                "§7Place this chest in your faction",
                "§7to allow players to deposit",
                "§7items into your collection."
        ], false)

        FastItemUtils.addGlow(stack)
        FastItemUtils.setCustomTag(stack, CHEST_KEY, ItemTagType.BYTE, 1 as byte)
        return stack
    }

    static def updatePlacedBlock(Block block, CollectionChest collectionChest) {
        if (block == null || block.getType() != Material.CHEST) return

        def newBlock = block.getWorld().getBlockAt(block.getLocation())

        Chest chest = newBlock.getState() as Chest
        chest.getPersistentDataContainer().set(CHEST_KEY, PersistentDataType.STRING, collectionChest.chestId)
        chest.update()

        collectionChest.entries = getDefaultEntries()
        collectionChest.queueSave()
    }

    static List<CollectionChestEntry> getDefaultEntries() {
        List<CollectionChestEntry> entries = []

        entries.add(new CollectionChestEntry("MOB_DROPS", Material.BLAZE_ROD, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.BLAZE_POWDER, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.GHAST_TEAR, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.MAGMA_CREAM, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.SLIME_BALL, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.STRING, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.SPIDER_EYE, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.COOKED_COD, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.COOKED_CHICKEN, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.COOKED_PORKCHOP, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.COOKED_BEEF, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.COOKED_MUTTON, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.COOKED_SALMON, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.TURTLE_EGG, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.ROTTEN_FLESH, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.BONE, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.ENDER_PEARL, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.GUNPOWDER, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.WITHER_ROSE, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.PUFFERFISH, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.GLOWSTONE_DUST, 0))
        entries.add(new CollectionChestEntry("MOB_DROPS", Material.NAUTILUS_SHELL, 0))

        entries.add(new CollectionChestEntry("VALUABLE", Material.NETHER_STAR, 0))
        entries.add(new CollectionChestEntry("VALUABLE", Material.HEART_OF_THE_SEA, 0))
        entries.add(new CollectionChestEntry("VALUABLE", Material.DIAMOND, 0))
        entries.add(new CollectionChestEntry("VALUABLE", Material.EMERALD, 0))
        entries.add(new CollectionChestEntry("VALUABLE", Material.GOLD_INGOT, 0))
        entries.add(new CollectionChestEntry("VALUABLE", Material.IRON_INGOT, 0))

        entries.add(new CollectionChestEntry("FARMING", Material.WHEAT_SEEDS, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.WHEAT, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.BEETROOT_SEEDS, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.BEETROOT, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.BAMBOO, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.CARROT, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.POTATO, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.CACTUS, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.SUGAR_CANE, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.MELON_SLICE, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.MELON, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.PUMPKIN, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.COCOA_BEANS, 0))
        entries.add(new CollectionChestEntry("FARMING", Material.NETHER_WART, 0))

        return entries
    }

    static CollectionChest getCachedChest(Chunk chunk) {
        if (chunk == null) return null
        if (chestChunkCache.get(chunk.getWorld()) == null) return null

        ChunkCache chunkCache = chestChunkCache.get(chunk.getWorld()).get(chunkToHash(chunk))
        if (chunkCache == null) return null

        return chunkCache.collectionChest
    }

    static CollectionChest getChest(String id, boolean create = true) {
        return StringDataManager.getData(id, CollectionChest.class, create)
    }

    static CollectionChest getChest(Location location, boolean create = true) {
        def cache = getCachedChest(location.getChunk())
        if (cache != null) return cache

        return getChest("$location.x:$location.y:$location.z:$location.world.name", create)
    }

    static Collection<CollectionChest> getAllChests() {
        return StringDataManager.getAllData(CollectionChest.class)
    }

}

@CompileStatic(TypeCheckingMode.SKIP)
class ChunkCache {
    World world
    int x, y, z
    transient CollectionChest collectionChest = null

    ChunkCache(World world, int x, int y, int z) {
        this.world = world
        this.x = x
        this.y = y
        this.z = z
    }

    ChunkCache(Location location) {
        this.world = location.world
        this.x = location.getBlockX()
        this.y = location.getBlockY()
        this.z = location.getBlockZ()
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
}




