package scripts.factions.features.enchant

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import scripts.Globals
import scripts.shared.legacy.AntiDupeUtils
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.legacy.utils.StringUtils

import java.util.concurrent.ThreadLocalRandom

if (Globals.ENCHANTMENTS_PVP_PVE_V3) return

Tinkerer.init()

class Tinkerer {
    static Map<UUID, Inventory> TINKERER_INVENTORIES
    static final NamespacedKey MAGICAL_DUST_KEY = new NamespacedKey(Starlight.plugin, "magical_dust_key")
    static final NamespacedKey DUST_AMPLIFIER_KEY = new NamespacedKey(Starlight.plugin, "dust_amplifier_key")
    static final NamespacedKey MYSTICAL_DUST_KEY = new NamespacedKey(Starlight.plugin, "mystical_dust_key")
    static final NamespacedKey TINKERER_LOCK_KEY = new NamespacedKey(Starlight.plugin, "tinkerer_lock_key")
    static final NamespacedKey ANCIENT_DUST_KEY = new NamespacedKey(Starlight.plugin, "ancient_dust_key")
    static List<Integer> TINKERER_SLOTS = [14]
    static List<Integer> OWNER_SLOTS = [10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39]

    static void init() {
        println "Loading tinkerer..."
        Exports.ptr("tinkerer/magicalDustKey", MAGICAL_DUST_KEY)
        Exports.ptr("tinkerer/mysticalDustKey", MYSTICAL_DUST_KEY)
        TINKERER_INVENTORIES = new HashMap<UUID, Inventory>()
        //0  1  2  3    4   5  6  7  8
        // 9  10 11 12  13  14 15 16 17
        //18 19 20 21   22  23 24 25 26
        //27 28 29 30   31  32 33 34 35
        //36 37 38 39   40  41 42 43 44
        //45 46 47 48   49  50 51 52 53
        Commands.create().assertPlayer().handler { command ->
            Inventory inv = Bukkit.createInventory(null, 6 * 9, "§e§lTinker")
            List<Integer> YELLOW_PAIN_SLOTS = [4, 13, 22, 31, 40, 49]
            List<Integer> BLUE_PAIN_SLOTS = [0, 1, 2, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53]
            BLUE_PAIN_SLOTS.addAll([
                    15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43
            ])
            int OWNER_SLOT = 3
            int TINKER_SLOT = 5

            for (int i : YELLOW_PAIN_SLOTS) {
                ItemStack yellowPain = FastItemUtils.createItem(Material.YELLOW_STAINED_GLASS_PANE, " ", [])
                FastItemUtils.setCustomTag(yellowPain, TINKERER_LOCK_KEY, ItemTagType.BYTE, 1 as byte)
                inv.setItem(i, yellowPain)
            }

            for (int i : BLUE_PAIN_SLOTS) {
                ItemStack bluePain = FastItemUtils.createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ", [])
                FastItemUtils.setCustomTag(bluePain, TINKERER_LOCK_KEY, ItemTagType.BYTE, 1 as byte)
                inv.setItem(i, bluePain)
            }
            ItemStack ownerHead = FastItemUtils.createSkull(command.sender(), "§e§l${command.sender().getName()}", [])
            FastItemUtils.setCustomTag(ownerHead, TINKERER_LOCK_KEY, ItemTagType.BYTE, 1 as byte)
            inv.setItem(OWNER_SLOT, ownerHead)
            ItemStack tinkerHead = FastItemUtils.createSkull("Thiimo", "§9§lTinker", [
                    "§7I will convert custom enchantment books",
                    "§7into Ancient Dust!",
                    "",
                    "§7Place your books on the left side and",
                    "§7Ancient Dust will appear on the right side!"
            ])
            FastItemUtils.setCustomTag(tinkerHead, TINKERER_LOCK_KEY, ItemTagType.BYTE, 1 as byte)
            inv.setItem(TINKER_SLOT, tinkerHead)
            inv.setItem(45, FastItemUtils.createItem(Material.LEVER, "§c§lTinker All", [
                    "",
                    " §e* Click to Tinker All! §e* ",
                    "",
                    "§4§lWARNING:",
                    "§cThis will tinker all books in your",
                    "§cInventory"
            ]))

            TINKERER_INVENTORIES.put(command.sender().getUniqueId(), inv)
            command.sender().openInventory(inv)
        }.register("tinkerer", "tinker")

        Commands.create().assertOp().assertUsage("<player> <type> [amount] [lowestTier] [highestTier]").handler { c ->
            int lowestTier = c.arg(3).parse(Integer).orElseGet({ 1 })
            int highestTier = c.arg(4).parse(Integer).orElseGet({ 5 })
            int amount = c.arg(2).parse(Integer).orElseGet({ 1 })
            String type = c.arg(1).parseOrFail(String).toLowerCase()

            if (lowestTier <= 0) {
                c.reply("§! §> §fLowest tier needs to be higher than 0.")
                return
            }

            if (highestTier < lowestTier) {
                c.reply("§! §> §fHighest tier needs to be higher or the same as the lowest tier.")
                return
            }

            if (amount <= 0) {
                c.reply("§! §> §fTier needs to be higher than 0.")
                return
            }

            switch (type) {
                case "success":
                case "magical":
                    type = "magical"
                    break
                case "destroy":
                case "mystical":
                    type = "mystical"
                    break
                default:
                    c.reply("§! §> §fInvalid type. Valid types are SUCCESS or DESTROY.")
                    return
            }

            DatabaseUtils.getId(c.rawArg(0), { UUID uuid, String username, Player player ->
                if (uuid == null) {
                    c.reply("§! §> §c${c.rawArg(0)} §fhas never joined the server before!")
                    return
                }

                Schedulers.sync().run({
                    for (int i = 0; i < amount; i++) {
                        ItemStack dust = createDust(type, getRandomPercentageDust(lowestTier, highestTier))
                        FastInventoryUtils.addOrBox(player.uniqueId, player, Bukkit.getConsoleSender(), dust, "§] §> §aYou have received ${type == "magical" ? "§5§l" : "§d§l"}${StringUtils.capitalize(type)} Dust§a!")
                    }
                })
            })
        }.register("givedust")


        Events.subscribe(InventoryClickEvent, EventPriority.HIGHEST).handler { event ->
            Inventory inv = event.getView().getTopInventory()
            int slot = event.getRawSlot()

            if (!TINKERER_INVENTORIES.values().contains(inv)) {
                return
            }


            if ([ClickType.NUMBER_KEY, ClickType.DROP, ClickType.CONTROL_DROP, ClickType.MIDDLE, ClickType.UNKNOWN].contains(event.getClick())) {
                return
            }

            Player player = event.getWhoClicked() as Player
            ItemStack placedItem = event.getCursor()
            ItemStack pickUpItem = event.getCurrentItem()

            if (event.click == ClickType.SHIFT_LEFT || event.click == ClickType.SHIFT_RIGHT) {
                event.setCancelled(true)

                //TODO
                handleShiftClick(event, slot, inv, placedItem, pickUpItem, player)
                return
            }

            if (slot == 45) {
                int enchantedBooks = player.inventory.getContents().findAll { isCustomEnchantBook(it) }.size()
                enchantedBooks += inv.getContents().findAll { isCustomEnchantBook(it) }.size()

                ItemStack item = FastItemUtils.createItem(Material.ENCHANTED_BOOK, "§cBooks to Tinker", [
                        "",
                        "§eAmount: §6${enchantedBooks}x"
                ])


                MenuUtils.createConfirmMenu(player, "§cAre you sure?", item, {
                    tinkerAll(player)
                }, {
                    player.closeInventory()
                    Players.msg(player, "§! §> §fCancelled tinkering all Enchanted Books!")
                }, true)

                return
            }

            if (event.isCancelled()) {
                event.setCancelled(false)
            }


            if (pickUpItem != null && FastItemUtils.hasCustomTag(pickUpItem, TINKERER_LOCK_KEY, ItemTagType.BYTE)) {
                event.setCancelled(true)
            }


            if (TINKERER_SLOTS.contains(slot) && placedItem != null && placedItem.type != Material.AIR) {
                event.setCancelled(true)
                return
            }

            if (OWNER_SLOTS.contains(slot) && !isCustomEnchantBook(placedItem) && !isCustomEnchantBook(pickUpItem)) {
                event.setCancelled(true)
                return
            }

            if (pickUpItem != null && !OWNER_SLOTS.contains(slot) && !TINKERER_SLOTS.contains(slot) && !isCustomEnchantBook(pickUpItem)) {
                Players.msg(player, "§! §> §fYou are only able to move §eCustom Enchant Books §finto the menu!")
                event.setCancelled(true)
                return
            }

            /* pick up dust, removing book on other side. */
            if (pickUpItem != null && TINKERER_SLOTS.contains(slot)) {

                pickUpDust(inv, player)
                event.setCancelled(true)
                return
            } else {
                if (pickUpItem != null && TINKERER_SLOTS.contains(slot) && FastItemUtils.hasCustomTag(pickUpItem, ANCIENT_DUST_KEY, ItemTagType.BYTE)) {


                    ItemStack book = inv.getItem(slot - 4)
                    if (book == null || !isCustomEnchantBook(book)) {
                        inv.setItem(slot, null)
                        Schedulers.sync().runLater({ player.updateInventory() }, 1)
                        return
                    }
                    Schedulers.sync().run({
                        inv.setItem(slot - 4, null)
                    })
                    Schedulers.sync().runLater({ player.updateInventory() }, 1)
                    return
                }
            }

            /* removed book, removing dust on other side */
            if (OWNER_SLOTS.contains(slot) && isCustomEnchantBook(pickUpItem)) {
                if (isCustomEnchantBook(placedItem)) {
                    Schedulers.sync().runLater({ player.updateInventory() }, 1)
                    return
                }
                Schedulers.sync().run({
                    updateTinkerSide(inv)
                })
                Schedulers.sync().runLater({ player.updateInventory() }, 1)
                return
            }

            /* placed book, placing dust on other side */
            if (placedItem != null && OWNER_SLOTS.contains(slot) && isCustomEnchantBook(placedItem)) {
                Schedulers.sync().run({
                    updateTinkerSide(inv)
                })
                Schedulers.sync().runLater({ player.updateInventory() }, 1)
                return
            }
        }

        Events.subscribe(InventoryDragEvent).handler { e ->
            Inventory inv = e.getView().getTopInventory()
            Set<Integer> slots = e.rawSlots

            if (!TINKERER_INVENTORIES.values().contains(inv)) {
                return
            }

            if (slots.size() > 1) {
                e.setCancelled(true)
                return
            }

            int slot = slots.first()

            if (e.isCancelled()) {
                e.setCancelled(false)
            }

            ItemStack placedItem = e.getNewItems()[slot]

            Player player = e.getWhoClicked() as Player

            if (TINKERER_SLOTS.contains(slot) && placedItem != null && placedItem.type != Material.AIR) {
                e.setCancelled(true)
                return
            }

            /* placed book, placing dust on other side */
            if (placedItem != null && OWNER_SLOTS.contains(slot) && isCustomEnchantBook(placedItem)) {
                Schedulers.sync().run({
                    updateTinkerSide(inv)
                })
                Schedulers.sync().runLater({ player.updateInventory() }, 1)
                return
            }
        }

        Events.subscribe(InventoryCloseEvent, EventPriority.MONITOR).handler { event ->
            Schedulers.sync().runLater({
                Inventory inv = event.getView().getTopInventory()
                Player player = event.getPlayer() as Player
                if (!TINKERER_INVENTORIES.values().contains(inv)) {
                    return
                }
                for (int ownerSlot : OWNER_SLOTS) {
                    ItemStack item = inv.getItem(ownerSlot)
                    if (item != null && item.type != Material.AIR) {
                        FastInventoryUtils.addOrBox(player.getUniqueId(), player, Bukkit.getConsoleSender(), item, null)
                    }
                }
            }, 2L)
        }

        Events.subscribe(PlayerInteractEvent.class).handler({ event ->
            Player player = event.player

            if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) {
                return
            }

            ItemStack item = player.inventory.itemInMainHand
            if (item == null || item.type == Material.AIR || !FastItemUtils.hasCustomTag(item, ANCIENT_DUST_KEY, ItemTagType.BYTE)) {
                return
            }

            UUID unique = FastItemUtils.getId(item)
            if (unique == null) {
                return
            }
            if (AntiDupeUtils.isDuped(unique)) {
                player.inventory.remove(item)
                Players.msg(player, "§! §> §fThis §eAncient Dust §fhas been removed as it is duped.")
                Starlight.log.info("[Custom Enchants] ${player.name} had a duped Acnient Dust removed from their inventory!")
                for (Player plr in Bukkit.getOnlinePlayers()) {
                    if (plr.isOp()) {
                        plr.sendMessage("§[ §> §f${player.name} had a duped Acient Dust removed from their inventory!")
                    }
                }
                return
            }

            FastInventoryUtils.use(player)

            AntiDupeUtils.useId(unique, {
                String random = RandomUtils.getRandom(["magical", "mystical"])
                player.world.execute({
                    FastInventoryUtils.addOrBox(player.getUniqueId(), player, Bukkit.getConsoleSender(), createDust(random), "§] §> §aYou have received ${random == "magical" ? "§5§l" : "§d§l"}${StringUtils.capitalize(random)} Dust§a!")
                })
            })
        })

        Events.subscribe(InventoryClickEvent.class, EventPriority.MONITOR).filter(EventFilters.ignoreCancelled()).handler({ event ->
            if (event.getClickedInventory()?.getType() != InventoryType.PLAYER) {
                return
            }

            ItemStack book = event.getCurrentItem()
            if (!isCustomEnchantBook(book)) {
                return
            }

            ItemStack cursor = event.getCursor()
            String type
            int amplifier
            if (FastItemUtils.hasCustomTag(cursor, MYSTICAL_DUST_KEY, ItemTagType.BYTE)) {
                type = "§d§lMystical Dust"
                amplifier = FastItemUtils.getCustomTag(cursor, DUST_AMPLIFIER_KEY, ItemTagType.INTEGER)
            } else if (FastItemUtils.hasCustomTag(cursor, MAGICAL_DUST_KEY, ItemTagType.BYTE)) {
                type = "§5§lMagical Dust"
                amplifier = (FastItemUtils.getCustomTag(cursor, DUST_AMPLIFIER_KEY, ItemTagType.INTEGER) as int)
            } else {
                return
            }

            Player player = event.whoClicked as Player

            UUID unique = FastItemUtils.getId(cursor)
            if (AntiDupeUtils.isDuped(unique)) {
                Players.msg(player, "§! §> §cThat $type appears to be duped. It has been confiscated! If you think this is a mistake, please contact staff.")
                event.getView().setCursor(null)
                event.setCancelled(true)
                for (Player plr : Bukkit.getOnlinePlayers()) {
                    if (plr.hasPermission("staff.notify")) {
                        plr.sendMessage("§[ §> §4${player.name} has tried to use a $type! (item deleted)")
                    }
                }
                return
            }

            Integer id = FastItemUtils.getCustomTag(book, NewEnchantmentsPvPPvEUtils.ENCHANTMENT_BOOK_ENCHANT_ID, ItemTagType.INTEGER)
            int success = FastItemUtils.getCustomTag(book, NewEnchantmentsPvPPvEUtils.ENCHANTMENT_SUCCESS_KEY, ItemTagType.INTEGER) as int
            int fail = FastItemUtils.getCustomTag(book, NewEnchantmentsPvPPvEUtils.ENCHANTMENT_DESTROY_KEY, ItemTagType.INTEGER) as int
            int level = FastItemUtils.getCustomTag(book, NewEnchantmentsPvPPvEUtils.ENCHANTMENT_BOOK_LEVEL, ItemTagType.INTEGER) as int
            Map<String, Object> data = NewEnchantmentsPvPPvEUtils.ID_TO_ENCHANTMENT.get(id)
            ItemStack giveBackItem = null
            if (type == "§5§lMagical Dust") {
                if (success >= 100) {
                    Players.msg(player, "§! §> §fYour enchantment book already has §e100% §fsuccess rate!")
                    return
                }

                int newSuccess = success + amplifier

                if (newSuccess - 100 > 0) {
                    giveBackItem = createDust("magical", newSuccess - 100)
                }

                success = Math.min(100, newSuccess)
            } else if (type == "§d§lMystical Dust") {
                if (fail <= 0) {
                    Players.msg(player, "§1 §> §fYour enchantment book already has §e0% §fdestroy rate!")
                    return
                }

                if (amplifier - fail > 0) {
                    giveBackItem = createDust("mystical", amplifier - fail)
                }

                fail = Math.max(0, fail - amplifier)
            }

            ItemStack newBook = NewEnchantmentsPvPPvEUtils.createBook(success, fail, level, data)
            if (giveBackItem != null) {
                FastInventoryUtils.addOrBox(player.uniqueId, player, Bukkit.getConsoleSender(), giveBackItem, "§] §> §aSuccessfully added back the remaining percentage of your dust!")
            }
            event.setCursor(newBook)
            event.setCurrentItem(null)
            Players.msg(player, "§] §> §aYou have successfully used $type §aon an enchantment book!")

            AntiDupeUtils.useId(unique, {})
        })

        Events.subscribe(BlockPlaceEvent).filter(EventFilters.ignoreCancelled()).handler { e ->
            ItemStack item = e.itemInHand
            if (item.type != Material.REDSTONE) return
            if (FastItemUtils.hasCustomTag(item, MYSTICAL_DUST_KEY, ItemTagType.BYTE)) {
                e.setCancelled(true)
            }
        }

        Events.subscribe(CraftItemEvent).filter(EventFilters.ignoreCancelled()).handler { e ->
            boolean hasDust = e.inventory.contents.any(i -> i != null && (FastItemUtils.hasCustomTag(i, MAGICAL_DUST_KEY, ItemTagType.BYTE) || FastItemUtils.hasCustomTag(i, MYSTICAL_DUST_KEY, ItemTagType.BYTE) || FastItemUtils.hasCustomTag(i, ANCIENT_DUST_KEY, ItemTagType.BYTE)))
            if (hasDust) {
                e.viewers.each { Players.msg(it, "§! §> §fYou can't craft with a custom item!") }
                e.setCancelled(true)
            }
        }

        Events.subscribe(InventoryClickEvent).filter(EventFilters.ignoreCancelled()).handler { e ->
            if (e.getClickedInventory()?.getType() != InventoryType.PLAYER) {
                return
            }

            ItemStack cursor = e.cursor
            ItemStack current = e.currentItem
            Player player = e.whoClicked as Player

            if (cursor == null || current == null) {
                return
            }

            int cursorAmplifier = getAmplifier(cursor)
            int currentAmplifier = getAmplifier(current)
            int newAmplifier = Math.min(100, cursorAmplifier + currentAmplifier)
            boolean shouldGiveBack = cursorAmplifier + currentAmplifier > newAmplifier

            if (isMagicalDust(cursor) && isMagicalDust(current)) {

                if (currentAmplifier >= 100) {
                    return
                }
                if (shouldGiveBack) {
                    FastInventoryUtils.addOrBox(player.uniqueId, player, Bukkit.consoleSender, createDust("magical", cursorAmplifier + currentAmplifier - newAmplifier), null)
                }
                e.getView().setCursor(createDust("magical", newAmplifier))
                e.setCurrentItem(null)
            }

            if (isMysticalDust(cursor) && isMysticalDust(current)) {
                if (currentAmplifier >= 100) {
                    return
                }
                if (shouldGiveBack) {
                    FastInventoryUtils.addOrBox(player.uniqueId, player, Bukkit.consoleSender, createDust("mystical", cursorAmplifier + currentAmplifier - newAmplifier), null)
                }
                e.getView().setCursor(createDust("mystical", newAmplifier))
                e.setCurrentItem(null)
            }
        }
        println "Successfully loaded tinkerer!"
    }

    static void updateTinkerSide(Inventory inv) {
        for (int slot in OWNER_SLOTS) {
            ItemStack item = inv.getItem(slot)
            if (isCustomEnchantBook(item)) {
                inv.setItem(14, createMenuDust())
                return
            }
        }

        inv.setItem(14, null)
    }

    static void pickUpDust(Inventory inv, Player player) {
        int books = 0
        for (int slot in OWNER_SLOTS) {
            ItemStack item = inv.getItem(slot)
            if (isCustomEnchantBook(item)) {
                books++
                inv.removeItem(item)
            }
        }

        inv.setItem(14, null)
        player.closeInventory()

        ItemStack dust = createDust("magical", getRandomPercentageDust(1, 5, books))
        FastInventoryUtils.addOrBox(player.uniqueId, player, Bukkit.getConsoleSender(), dust, "§] §> §e${books}x §aEnchanted Book${books > 1 ? "s" : ""} converted into §5§lMagical Dust§a!")
    }

    static void handleShiftClick(InventoryClickEvent event, int slot, Inventory inv, ItemStack placedItem, ItemStack pickUpItem, Player player) {
        if (TINKERER_SLOTS.contains(slot)) {
            if (pickUpItem == null || !FastItemUtils.hasCustomTag(pickUpItem, ANCIENT_DUST_KEY, ItemTagType.BYTE)) {
                return
            }
            ItemStack book = inv.getItem(slot - 4)
            if (!isCustomEnchantBook(book)) {
                inv.setItem(slot, null)
                return
            }
            FastInventoryUtils.addOrBox(player.uniqueId, player, Bukkit.getConsoleSender(), inv.getItem(slot), null)
            inv.setItem(slot, null)
            inv.setItem(slot - 4, null)
            return
        } else if (OWNER_SLOTS.contains(slot)) {
            if (pickUpItem == null || !isCustomEnchantBook(pickUpItem)) {
                return
            }
            FastInventoryUtils.addOrBox(player.uniqueId, player, Bukkit.getConsoleSender(), inv.getItem(slot), null)
            inv.setItem(slot, null)
            updateTinkerSide(inv)
            return
        }

        int firstSlot = -1

        for (int ownerSlot : OWNER_SLOTS) {
            ItemStack item = inv.getItem(ownerSlot)
            if (item == null || item.type == Material.AIR) {
                firstSlot = ownerSlot
                break
            }
        }

        if (firstSlot == -1 || !isCustomEnchantBook(pickUpItem)) {
            return
        }

        inv.setItem(firstSlot, pickUpItem)
        updateTinkerSide(inv)
        event.setCurrentItem(null)
    }

    static ItemStack createAncientDust() {
        ItemStack item = FastItemUtils.createItem(Material.GUNPOWDER, "§e§lAncient Dust", [
                "",
                "§6* §eClick to open §6*"
        ])
        FastItemUtils.setCustomTag(item, ANCIENT_DUST_KEY, ItemTagType.BYTE, 1 as byte)
        FastItemUtils.setShiny(item)
        FastItemUtils.ensureUnique(item)
        return item
    }

    /**
     * Gives a random number between 1 and 5
     * @return integer between 1 and 5
     */
    static int getRandomPercentageDust(int lowest = 1, int highest = 5, int amount = 1) {
        if (amount > 1) {
            int result = 0
            for (int i = 0; i < amount; i++) {
                result += ThreadLocalRandom.current().nextInt(lowest, highest + 1)
            }
            return result
        }
        return ThreadLocalRandom.current().nextInt(lowest, highest + 1)
    }

    static ItemStack createDust(String dust, Integer amplifier = null) {
        if (amplifier == null) amplifier = getRandomPercentageDust()
        switch (dust) {
            case "magical":
                ItemStack item = FastItemUtils.createItem(Material.SUGAR, "§5§lMagical Dust", [
                        "§7Applying this to an enchant will",
                        "§7increase the success chance by §e$amplifier%§7."
                ])
                FastItemUtils.setCustomTag(item, MAGICAL_DUST_KEY, ItemTagType.BYTE, 1 as byte)
                FastItemUtils.setCustomTag(item, DUST_AMPLIFIER_KEY, ItemTagType.INTEGER, amplifier)
                FastItemUtils.ensureUnique(item)
                FastItemUtils.setShiny(item)
                return item
            case "mystical":
                ItemStack item = FastItemUtils.createItem(Material.REDSTONE, "§d§lMystical Dust", [
                        "§7Applying this to an enchant will",
                        "§7decrease the destroy rate by §e$amplifier%§7."
                ])
                FastItemUtils.setCustomTag(item, MYSTICAL_DUST_KEY, ItemTagType.BYTE, 1 as byte)
                FastItemUtils.setCustomTag(item, DUST_AMPLIFIER_KEY, ItemTagType.INTEGER, amplifier)
                FastItemUtils.ensureUnique(item)
                FastItemUtils.setShiny(item)
                return item
        }
        return null
    }

    static void tinkerAll(Player player) {
        player.world.execute {
            player.closeInventory()
            Inventory inv = player.getInventory()

            int books = 0
            for (ItemStack item : inv.getContents()) {
                if (isCustomEnchantBook(item)) {
                    inv.remove(item)
                    books++
                }
            }

            if (books == 0) return

//            for (int i = 0; i < books; i++) {
            ItemStack dust = createDust("magical", getRandomPercentageDust(1, 5, books))
//                FastInventoryUtils.addOrBox(player.uniqueId, player, Bukkit.consoleSender, dust, null)
//            }


            FastInventoryUtils.addOrBox(player.uniqueId, player, Bukkit.getConsoleSender(), dust, "§] §> §e${books}x §aEnchanted Book${books > 1 ? "s" : ""} converted into §5§lMagical Dust§a!")
//            Players.msg(player, "§] §> §e${books}x §aEnchanted Book${books > 1 ? "s" : ""} converted into §5§lMagical Dust§a!")
        }
    }

    static ItemStack createMenuDust() {
        return FastItemUtils.createEnchantedItem(Material.SUGAR, "§5§lMagical Dust", [
                "§7Applying this to an enchant will",
                "§7increase the success chance by §e???%§7."
        ])
    }

    static boolean isMagicalDust(ItemStack item) {
        return FastItemUtils.hasCustomTag(item, MAGICAL_DUST_KEY, ItemTagType.BYTE)
    }

    static boolean isMysticalDust(ItemStack item) {
        return FastItemUtils.hasCustomTag(item, MYSTICAL_DUST_KEY, ItemTagType.BYTE)
    }

    static int getAmplifier(ItemStack item) {
        if (item == null || item.type == Material.AIR) return 0
        return FastItemUtils.getCustomTag(item, DUST_AMPLIFIER_KEY, ItemTagType.INTEGER) ?: 0
    }

    static boolean isCustomEnchantBook(ItemStack item) {
        return item != null && FastItemUtils.hasCustomTag(item, NewEnchantmentsPvPPvEUtils.ENCHANTMENT_BOOK_ENCHANT_ID, ItemTagType.INTEGER)
    }
}