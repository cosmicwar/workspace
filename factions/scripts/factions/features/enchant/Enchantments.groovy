package scripts.factions.features.enchant

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Location
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import org.starcade.starlight.helper.text3.Text
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.NotNull
import scripts.factions.content.clickitem.ClickItem
import scripts.factions.content.clickitem.ClickItems
import scripts.factions.content.dbconfig.Config
import scripts.factions.content.dbconfig.ConfigCategory
import scripts.factions.content.dbconfig.DBConfigUtil
import scripts.factions.content.dbconfig.utils.SelectionUtils

import scripts.factions.features.enchant.cfg.EnchantConfigConst

import scripts.factions.features.enchant.data.enchant.StoredEnchantment
import scripts.factions.features.enchant.data.item.BookEnchantmentData
import scripts.factions.features.enchant.data.item.EnchantmentDustData
import scripts.factions.features.enchant.data.item.ItemEnchantmentData
import scripts.factions.features.enchant.data.item.MysteryBookData
import scripts.factions.features.enchant.data.item.MysteryEnchantmentDustData
import scripts.factions.features.enchant.data.item.RandomSoulGenData
import scripts.factions.features.enchant.data.item.SoulPearlData
import scripts.factions.features.enchant.data.item.TimeMachineData
import scripts.factions.features.enchant.data.item.TransmogScrollData
import scripts.factions.features.enchant.data.item.WhiteScrollItemData
import scripts.factions.features.enchant.items.EnchantmentOrbType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.struct.HeroicEnchant
import scripts.factions.features.enchant.utils.RomanNumerals
import scripts.factions.features.enchant.utils.SoulUtils
import scripts.factions.features.enchant.data.item.BlackScrollData
import scripts.factions.features.enchant.data.item.EnchantmentOrbData
import scripts.factions.features.enchant.data.item.SoulGemData
import scripts.shared.legacy.AntiDupeUtils
import scripts.shared.legacy.CooldownUtils
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.utils.BroadcastUtils
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.systems.MenuEvent
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.DataUtils
import scripts.shared.utils.ItemType
import scripts.shared3.utils.Callback

import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

// Enchant Proccing - Enchant Priority: ex priority 1 will execute with all priority 1 enchants before priority 2 enchants or vice versa
@CompileStatic(TypeCheckingMode.SKIP)
class Enchantments {

    static String enchantPrefix = ColorUtil.color("§<#D639E0>§lᴇɴᴄʜᴀɴᴛꜱ §8»")

    Map<String, CustomEnchantment> registeredEnchantments = new ConcurrentHashMap<>()
    static Map<UUID, Map<ItemStack, Map<CustomEnchantment, Integer>>> equippedEnchantsCache = new ConcurrentHashMap<>()

    static final Set<EquipmentSlot> ARMOR_SLOTS = [EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET] as Set<EquipmentSlot>

    static Map<UUID, Stack<Location>> timeMachineUsers = new ConcurrentHashMap<>()
    static Map<UUID, Task> timeMachineTasks = new ConcurrentHashMap<>()

    Set<String> enchantScriptPaths = new HashSet<>()

    static Config enchantConfig
    static ConfigCategory items
    static ConfigCategory values

    Enchantments() {
        GroovyScript.addUnloadHook {
            Bukkit.getOnlinePlayers().each { player ->
                forEquippedEnchants(player, { ItemStack itemStack, CustomEnchantment enchantment, int enchantLevel ->
                    enchantment?.onUnequip(player, itemStack, enchantLevel)
                })
            }

            Starlight.unload("~/EnchantListener.groovy")

            Starlight.unload("~/indicators/Indicators.groovy")
            Starlight.unload("~/indicators/HealthIndicator.groovy")

//            DataManager.getByClass(EnchantConfig).saveAll(false)

            registeredEnchantments.clear()
            Starlight.unload(enchantScriptPaths as String[])

            enchantScriptPaths.clear()
        }

        reloadConfig()

        exports()

        registerEnchantments()
        reloadEnchantments()


        Starlight.watch("~/EnchantListener.groovy")

        Starlight.watch("~/indicators/Indicators.groovy")
        Starlight.watch("~/indicators/HealthIndicator.groovy")

        GroovyScript.addScriptHook(GroovyScript.HookType.RECOMPILE, {
            if (!GroovyScript.getCurrentScript().getWatchedScripts().contains(it)) return

            reloadConfig()
            reloadEnchantments()
        })

        registerCommands()
        registerEvents()
        createClickItems()

        Bukkit.getOnlinePlayers().each { player ->
            forEquippedEnchants(player, { ItemStack itemStack, CustomEnchantment enchantment, int enchantLevel ->
                enchantment.onEquip(player, itemStack, enchantLevel)
            })
        }
    }

    void exports() {
        Exports.ptr("enchantments:registerEnchantment", { String enchantPath -> registerEnchantment(enchantPath) })
        Exports.ptr("enchantments:getEnchantment", { String enchantId -> getEnchantmentById(enchantId) })
        Exports.ptr("enchantments:getEnchantments", { ItemStack itemStack -> getItemEnchantments(itemStack) })
        Exports.ptr("enchantments:getTieredEnchantments", { EnchantmentTier tier -> getTieredEnchantments(tier) })
        Exports.ptr("enchantments:forEquippedEnchants", { Player player, Closure closure -> forEquippedEnchants(player, closure) })
        Exports.ptr("enchantments:invalidateEquippedEnchants", { Player player -> invalidateEquippedEnchants(player) })

        Exports.ptr("enchitems:addEnchantment", { ItemStack item, CustomEnchantment enchantment, int level -> addEnchantToItem(item, enchantment, level) })
        Exports.ptr("enchitems:updateItemStack", { ItemStack item, ItemEnchantmentData data -> updateItemStack(item, data) })

        Exports.ptr("enchantments:getHighestLevel", { Player player, String enchantmentId ->
            CustomEnchantment enchantment = getEnchantmentById(enchantmentId)
            if (enchantment == null) return 0

            Map<ItemStack, Map<CustomEnchantment, Integer>> equippedEnchantments = getEquippedEnchantments(player)
            return equippedEnchantments.values().findResults { it.getOrDefault(enchantment, 0) }?.max() ?: 0
        })
    }

    def registerCommands() {
        SubCommandBuilder builder = SubCommandBuilder.of("enchants", "customenchant", "enchantapi", "ce").defaultAction { player -> openEnchantPreview(player) }

        builder.create("devtestuk").register {ctx ->
            ctx.reply("${enchantPrefix} §c§lPHOENIX §c- §eshmeeted")
        }

        builder.create("givebook").requirePermission("enchants.*").usage("<player> <enchant> [level] [success] [destory]").register { c ->
            Player player = c.arg(0).parse(Player.class).get()
            if (player == null) {
                c.reply("§cInvalid player")
                return
            }

            CustomEnchantment enchantment = getEnchantmentById(c.arg(1).parse(String.class).get().toLowerCase())
            if (enchantment == null) {
                c.reply("§cInvalid enchantment")
                return
            }

            int level = 1
            if (c.args().size() > 2 && c.arg(2).parse(Integer.class).isPresent()) {
                level = c.arg(2).parse(Integer.class).get()
            }

            int success = 100
            if (c.args().size() > 3 && c.arg(3).parse(Integer.class).isPresent()) {
                success = c.arg(3).parse(Integer.class).get()
            }

            int destroy = 0
            if (c.args().size() > 4 && c.arg(4).parse(Integer.class).isPresent()) {
                destroy = c.arg(4).parse(Integer.class).get()
            }

            enchantmentBook.giveStack(player, createBook(enchantment, level, success, destroy))
            c.reply("§aGave ${player.getName()} a ${enchantment.displayName} book")
        }.create("givemysterybook").requirePermission("enchants.*").usage("<player> <tier>").register { c ->
            Player player = c.arg(0).parse(Player.class).get()
            if (player == null) {
                c.reply("§cInvalid player")
                return
            }

            EnchantmentTier enchantmentTier = Optional.ofNullable(EnchantmentTier.valueOf(c.arg(1).parse(String.class).get().toUpperCase())).orElse(null)
            if (enchantmentTier == null) {
                c.reply("§cInvalid enchantment tier")
                return
            }

            ItemStack book = createMysteryBook(enchantmentTier)
            player.getInventory().addItem(book)
        }.create("list").requirePermission("enchants.*").register { c ->
            // make this send a message with all the enchants sorted by tier
            for (EnchantmentTier tier : EnchantmentTier.values()) {
                c.sender().sendMessage("§7${tier.tierColor}${tier.tierName} Enchantments:")
                getTieredEnchantments(tier).each { c.sender().sendMessage("§7- ${it.displayName}") }
            }
        }.create("givesouls").requirePermission("enchants.*").usage("<player> <count>").register { c ->
            Player player = c.arg(0).parse(Player.class).get()
            if (player == null) {
                c.reply("§cInvalid player")
                return
            }

            Integer amount = c.arg(1).parse(Integer.class).get()
            if (amount == null || amount < 1) {
                c.reply("§cInvalid amount")
                return
            }

            if (player.getInventory().firstEmpty() == -1) return

            ItemStack item = SoulUtils.createSoulGem(amount)

            String playerMessage = "§] §> §aYou have received souls!"
            Players.msg(player, playerMessage)
            player.getInventory().addItem(item)
        }.create("givescroll").requirePermission("enchants.*").usage("<player> <type> [amount]").register { c ->
            Player player = c.arg(0).parseOrFail(Player.class)
            if (!player.isOnline()) {
                c.reply("§cInvalid player")
                return
            }

            String type = c.arg(1).parseOrFail(String.class)
            if (type.toLowerCase() == "whitescroll") {
                ItemStack whiteScroll = createWhiteScroll()
                if (c.args().size() > 2) {
                    int amount = c.arg(2).parseOrFail(Integer.class)
                    if (amount < 1) {
                        c.reply("§cInvalid amount")
                        return
                    }

                    whiteScroll.setAmount(amount)
                }

                player.getInventory().addItem(whiteScroll)
                c.reply("§aGave ${player.getName()} a white scroll")
            }

            if (type.toLowerCase() == "holywhitescroll") {
                ItemStack whiteScroll = createWhiteScroll(true)
                if (c.args().size() > 2) {
                    int amount = c.arg(2).parseOrFail(Integer.class)
                    if (amount < 1) {
                        c.reply("§cInvalid amount")
                        return
                    }

                    whiteScroll.setAmount(amount)
                }

                player.getInventory().addItem(whiteScroll)
                c.reply("§aGave ${player.getName()} a holy white scroll")
            }
        }.create("debug").requirePermission("enchants.*").register {cmd ->
            def stack = cmd.sender().getInventory().getItemInMainHand()
            if (!stack || stack.getType() == Material.AIR) return

            def data = ItemEnchantmentData.read(stack)
            if (!data) {
                cmd.reply("§c§l(!) §cThis item does not have any enchantments!")
                return
            }

            cmd.reply("§aDATA")
            cmd.reply("§a- ${data.enchantments.size()} enchantments")
            data.enchantments.each {
                CustomEnchantment enchant = getEnchantmentById(it.getEnchantment())
                if (!enchant) return
                cmd.reply("§a- ${enchant.displayName} ${RomanNumerals.numeralOf(it.getLevel())}")
            }
            cmd.reply("")
            cmd.reply("§a- ${data.slotIncrease + getDefSlots()} max slots")
            cmd.reply("§a- ${data.slotIncrease} slots")
            cmd.reply("§a- ${data.whiteScroll} white scroll")
            cmd.reply("§a- ${data.holyWhiteScrollData.hasHoly} holy white scroll")
            cmd.reply("§a- ${data.holyWhiteScrollData.holiesApplied} holies applied")
            cmd.reply("§cEND")
        }.create("admin").requirePermission("enchants.*").register {cmd ->
            if (cmd.sender().isOp()) openAdminEnchantGUI(cmd.sender())
        }

        builder.build()

        Commands.create().assertPlayer().handler { command ->
            showEnchanterMenu(command.sender())
        }.register("enchanter")

        Commands.create().assertPlayer().assertUsage("<amount>").handler { command ->
            Player player = command.sender()
            int amount = -1
            try {
                amount = command.rawArg(0) as int
            } catch (Exception e) {
                player.sendMessage("§c(!) Please provide a valid input!")
                return
            }

            if (amount < 0) {
                player.sendMessage("§c(!) Please provide a valid input!")
                return
            }

            ItemStack inHand = player.getItemInHand()
            if (inHand == null || inHand.type != Material.EMERALD) {
                player.sendMessage("§c(!) You must have souls in your hand!")
                return
            }
            SoulGemData data = SoulGemData.read(inHand)
            if (!data) return
            if (data.getSouls() - amount < 0) {
                player.sendMessage("§c(!) You do not have enough souls for this!")
                return
            }

            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage("§c(!) You do not have enough empty inventory slots for this!")
                return
            }
            data.setSouls(data.getSouls() - amount)
            data.write(inHand)
            SoulUtils.updateSoulCount(inHand, data.getSouls())
            player.updateInventory()
            player.getInventory().addItem(SoulUtils.createSoulGem(amount))
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1)

        }.register("splitsouls")
    }

    static def registerEvents() {
        Events.subscribe(PlayerQuitEvent.class).handler({
            invalidateEquippedEnchants(it.player)
            Exports.ptr("potionEffects:removeAllEffects", it.player)
        })

        Events.subscribe(PlayerDeathEvent.class).handler({
            invalidateEquippedEnchants(it.entity)
            Exports.ptr("potionEffects:removeAllEffects", it.player)
        })
    }

    static def reloadConfig() {
        enchantConfig = DBConfigUtil.createConfig("enchantments", "§dEnchantments", [], Material.ENCHANTING_TABLE)
        items = enchantConfig.getOrCreateCategory("items")
        items.getOrCreateConfig(whiteScrollId).addDefault(EnchantConfigConst.getWhiteScrollEntries())
        items.getOrCreateConfig(holyWhiteScrollId).addDefault(EnchantConfigConst.getHolyWhiteScrollEntries())
        items.getOrCreateConfig(blackScrollId).addDefault(EnchantConfigConst.getBlackScrollEntries())
        items.getOrCreateConfig(enchantmentOrbId).addDefault(EnchantConfigConst.getEnchantOrbEntries())
        items.getOrCreateConfig(mysteryBookId).addDefault(EnchantConfigConst.getMysteryEnchantBookEntries())
        items.getOrCreateConfig(randomSoulGeneratorId).addDefault(EnchantConfigConst.getRandomSoulGenEntries())
        items.getOrCreateConfig(enchantmentDustId).addDefault(EnchantConfigConst.getEnchantDustEntries())
        items.getOrCreateConfig(mysteryEnchantmentDustId).addDefault(EnchantConfigConst.getMysteryEnchantDustEntries())
        items.getOrCreateConfig(transmogScrollId).addDefault(EnchantConfigConst.getTransmogEntries())
        items.getOrCreateConfig(enchantmentBookId).addDefault(EnchantConfigConst.getEnchantBookEntries())
        items.getOrCreateConfig(soulPearlId).addDefault(EnchantConfigConst.getSoulPearlEntries())
        items.getOrCreateConfig(timeMachineId).addDefault(EnchantConfigConst.getTimeMachineEntries())

        values = enchantConfig.getOrCreateCategory("values")
        enchantConfig.queueSave()
    }

    def registerEnchantments() {
        File enchantScriptsFolder = new File("${GroovyScript.getCurrentScript().getScript().getParent()}${File.separator}enchants")
        Files.walk(enchantScriptsFolder.toPath(), 3).forEach({
            String path = it.toString()
            if (path.endsWith(".groovy")) {
                registerEnchantment(path)
            }
        })
    }

    def registerEnchantment(String enchantPath) {
        if (!enchantScriptPaths.add(enchantPath)) return

        Starlight.watch(enchantPath)
    }

    def reloadEnchantments() {
        equippedEnchantsCache.clear()

        GroovyScript.getCurrentScript().getWatchedScripts().findAll { CustomEnchantment.class.isAssignableFrom(it.getScriptClazz()) }.each {
            try {
                CustomEnchantment enchantment = it.getScriptClazz().getDeclaredConstructor().newInstance() as CustomEnchantment

                CustomEnchantment existingEnchant = registeredEnchantments.get(enchantment.getInternalName())
//                existingEnchant?.update()
                if (existingEnchant != null && existingEnchant.getClass().isAssignableFrom(enchantment.getClass())) {
                    return
                }

//                enchantment.update()
                registeredEnchantments.put(enchantment.getInternalName(), enchantment)
                if (existingEnchant) {
                    Starlight.log.info("Reloaded enchantment: $enchantment.displayName")
                } else {
                    Starlight.log.info("Loaded enchantment: $enchantment.displayName")
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    CustomEnchantment getEnchantmentById(String id) {
        return registeredEnchantments.get(id)
    }

    List<CustomEnchantment> getTieredEnchantments(EnchantmentTier tier) {
        return registeredEnchantments.values().stream().filter { it.enchantmentTier == tier }.collect(Collectors.toList())
    }

    static def invalidateEquippedEnchants(Player player) {
        equippedEnchantsCache.remove(player.uniqueId)
        Exports.ptr("potionEffects:removeAllEffects", player)
    }

    Map<ItemStack, Map<CustomEnchantment, Integer>> getEquippedEnchantments(Player player) {
        return equippedEnchantsCache.computeIfAbsent(player.uniqueId, v -> {
            Map<ItemStack, Map<CustomEnchantment, Integer>> equipped = new HashMap<>()

            ItemStack mainHand = player.getEquipment().getItemInMainHand()
            ItemType itemType = ItemType.getTypeOf(mainHand)
            if (itemType?.isHoldable()) {
                equipped.put(mainHand, getItemEnchantments(mainHand))
            }

            ARMOR_SLOTS.each {
                ItemStack itemStack = player.getEquipment().getItem(it)
                if (itemStack != null) {
                    equipped.put(itemStack, getItemEnchantments(itemStack))
                }
            }

            return equipped
        })
    }

    // add something to track equipped effects such as potion effects to make sure they dont double stack
    void forEquippedEnchants(Player player, Closure closure) {
        Set<CustomEnchantment> processedEnchants = new HashSet<>()

        getEquippedEnchantments(player).each { itemStack, enchants ->

            enchants = enchants.findAll { it.key != null }.sort { a, b -> b.getKey().getPriority() <=> a.getKey().getPriority() }

            enchants.each { enchant, level ->
                if (enchant.getEnchantmentType() == EnchantmentType.POTION) {
                    if (equippedEnchantsCache.get(player.uniqueId).values().stream().filter { it.containsKey(enchant) }.count() > 1) {
                        return
                    }
                }

                if (enchant.isStackable()) {
                    closure.call(itemStack, enchant, level)
                    return
                }

                if (!processedEnchants.contains(enchant)) {
                    closure.call(itemStack, enchant, level)
                    processedEnchants.add(enchant)
                }
            }
        }
    }

    void openEnchantPreview(Player player) {
        MenuBuilder builder = new MenuBuilder(9, "§7Select a Category")

        ItemStack rarity = FastItemUtils.createItem(
                Material.NETHER_STAR,
                "§dView by Rarity",
                ["",
                 "§7Click me to view all the enchants",
                 "§7sorted by their §5rarity§7.",
                 ""])

        ItemStack material = FastItemUtils.createItem(
                Material.DIAMOND_HELMET,
                "§bView by Material",
                ["",
                 "§7Click me to view all the enchants",
                 "§7sorted by their respective §3materials§7.",
                 ""])

        FastItemUtils.hideAttributes(material)

        builder.set(3, rarity, { p, t, s ->
            openEnchantRarityPreview(p)
        })

        builder.set(5, material, { p, t, s ->
            openEnchantMaterialPreview(p)
        })

        builder.open(player)
    }

    void openEnchantRarityPreview(Player player) {
        MenuBuilder builder = new MenuBuilder(36, "§7Select a Rarity",)

        def slots = [
                (EnchantmentTier.SIMPLE)   : 1,
                (EnchantmentTier.UNIQUE)   : 3,
                (EnchantmentTier.ELITE)    : 5,
                (EnchantmentTier.ULTIMATE) : 7,
                (EnchantmentTier.LEGENDARY): 11,
                (EnchantmentTier.SOUL)     : 13,
                (EnchantmentTier.HEROIC)   : 15,
                (EnchantmentTier.GALAXY)   : 22
        ]

        slots.each { entry ->
            ItemStack stack = FastItemUtils.createItem(entry.getKey().getGlassPane(),
                    "${entry.getKey().tierColor}${entry.getKey().tierName} §7Enchants",
                    ["",
                     "§fClick me to view all of the",
                     "${entry.getKey().tierColor}${entry.getKey().tierName} §fenchants.",
                     ""],
                    false
            )
            builder.set(entry.getValue(), stack, { p, t, s ->
                showEnchantRarity(p, entry.getKey())
            })
        }

        ItemStack backItem = FastItemUtils.createItem(Material.RED_DYE, "§7§lBack", [])
        builder.set(31, backItem, { p, t, s -> openEnchantPreview(p) })

        builder.open(player)
    }

    void showEnchantRarity(Player player, EnchantmentTier tier, int page = 1) {
        List<CustomEnchantment> enchants = Optional.ofNullable(getRegisteredEnchantments().values().stream().filter { it.enchantmentTier == tier }.collect(Collectors.toList())).orElse(Lists.newArrayList())

        MenuBuilder builder = MenuUtils.createPagedMenu("${tier.tierColor}${tier.tierName} §7Enchants (${enchants.size()})", enchants, { CustomEnchantment enchant, Integer i ->
            if (enchant != null) {
                List<String> lore = [
                        "§7Max Level: §7${enchant.maxLevel}",
                        "§r"
                ]

                enchant.description.forEach { string -> lore.add("§f$string") }

                lore.addAll(
                        [
                                "§r",
                                "§7${enchant.applicability.stream().map { it.displayName }.collect(Collectors.joining(", "))}",
                                "§r",
                        ])

                return FastItemUtils.createItem(Material.PAPER, "${enchant.enchantmentTier.tierColor}§l§n${enchant.displayName}", lore)
            }
            return null
        }, page, true, [
                { Player p, ClickType t, int s -> },
                { Player p, ClickType t, int s -> showEnchantRarity(p, tier, page + 1) },
                { Player p, ClickType t, int s -> showEnchantRarity(p, tier, page - 1) },
                { Player p, ClickType t, int s -> openEnchantRarityPreview(p) }
        ])

        builder.open(player)
    }

    void openEnchantMaterialPreview(Player player) {
        MenuBuilder builder = new MenuBuilder(36, "§7Select a Material")

        def slots = [
                (ItemType.HELMET)    : 1,
                (ItemType.CHESTPLATE): 3,
                (ItemType.LEGGINGS)  : 5,
                (ItemType.BOOTS)     : 7,
                (ItemType.SWORD)     : 11,
                (ItemType.AXE)       : 13,
                (ItemType.PICKAXE)   : 15,
                (ItemType.BOW)       : 21,
                (ItemType.ROD)       : 23
        ]

        slots.each { entry ->
            ItemStack stack = FastItemUtils.createItem(entry.getKey().getIcon(),
                    "§3${entry.getKey().displayName} §7Enchants",
                    ["",
                     "§fClick me to view all of the",
                     "§3${entry.getKey().displayName} §fenchants.",
                     ""],
                    false
            )
            builder.set(entry.getValue(), stack, { p, t, s ->
                showEnchantMaterial(player, entry.getKey())
            })
        }

        ItemStack backItem = FastItemUtils.createItem(Material.RED_DYE, "§7§lBack", [])
        builder.set(31, backItem, { p, t, s -> openEnchantPreview(p) })

        builder.open(player)
    }

    void showEnchantMaterial(Player player, ItemType type, int page = 1) {
        List<CustomEnchantment> enchants = Optional.ofNullable(getRegisteredEnchantments().values().stream().filter { it.applicability.contains(type) }.collect(Collectors.toList())).orElse(Lists.newArrayList())

        MenuBuilder builder = MenuUtils.createPagedMenu("§3${type.displayName} §7Enchants (${enchants.size()})", enchants, { CustomEnchantment enchant, Integer i ->
            if (enchant != null) {
                List<String> lore = [
                        "§7Max Level: §7${enchant.maxLevel}",
                        "§r"
                ]

                enchant.description.forEach { string -> lore.add("§f$string") }

                lore.addAll(
                        [
                                "§r",
                                "§7§l${enchant.applicability.stream().map { it.displayName }.collect(Collectors.joining(", "))} §7Enchantment",
                                "§r",
                        ])

                return FastItemUtils.createItem(Material.PAPER, "${enchant.enchantmentTier.tierColor}§l§n${enchant.displayName}", lore)
            }
            return null
        }, page, true, [
                { p, t, s -> },
                { Player p, ClickType t, int s -> showEnchantMaterial(p, type, page + 1) },
                { Player p, ClickType t, int s -> showEnchantMaterial(p, type, page - 1) },
                { Player p, ClickType t, int s -> openEnchantMaterialPreview(p) }
        ])

        builder.open(player)
    }

    void showEnchanterMenu(Player player) {
        def playerNotEnoughXp = "§cYou do not have enough experience to purchase this item!"
        MenuBuilder menu = new MenuBuilder(9, "Enchanter")

        menu.set(0, FastItemUtils.createItem(Material.WHITE_STAINED_GLASS_PANE, "§7§lMystery Simple Book", ["§71,000xp"]), { p, t, s ->
            if (p.getTotalExperience() < 1000) {
                p.sendMessage(playerNotEnoughXp)
            } else {
                setTotalExperience(p, p.getTotalExperience() - 1000)
                mysteryBook.giveStack(p, createMysteryBook(EnchantmentTier.SIMPLE))
                showEnchanterMenu(p)
            }
        })

        menu.set(1, FastItemUtils.createItem(Material.GREEN_STAINED_GLASS_PANE, "§a§lMystery Unique Book", ["§a2,500xp"]), { p, t, s ->
            if (p.getTotalExperience() < 2500) {
                p.sendMessage(playerNotEnoughXp)
            } else {
                setTotalExperience(p, p.getTotalExperience() - 2500)
                mysteryBook.giveStack(p, createMysteryBook(EnchantmentTier.UNIQUE))
                showEnchanterMenu(p)
            }
        })

        menu.set(2, FastItemUtils.createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "§b§lMystery Elite Book", ["§b10,000xp"]), { p, t, s ->
            if (p.getTotalExperience() < 10000) {
                p.sendMessage(playerNotEnoughXp)
            } else {
                setTotalExperience(p, p.getTotalExperience() - 10000)
                mysteryBook.giveStack(p, createMysteryBook(EnchantmentTier.ELITE))
                showEnchanterMenu(p)
            }
        })

        menu.set(3, FastItemUtils.createItem(Material.YELLOW_STAINED_GLASS_PANE, "§e§lMystery Ultimate Book", ["§e15,000xp"]), { p, t, s ->
            if (p.getTotalExperience() < 15000) {
                p.sendMessage(playerNotEnoughXp)
            } else {
                setTotalExperience(p, p.getTotalExperience() - 15000)
                mysteryBook.giveStack(p, createMysteryBook(EnchantmentTier.ULTIMATE))
                showEnchanterMenu(p)
            }
        })

        menu.set(4, FastItemUtils.createItem(Material.ORANGE_STAINED_GLASS_PANE, "§6§lMystery Legendary Book", ["§625,000xp"]), { p, t, s ->
            if (p.getTotalExperience() < 25000) {
                p.sendMessage(playerNotEnoughXp)
            } else {
                setTotalExperience(p, p.getTotalExperience() - 25000)
                mysteryBook.giveStack(p, createMysteryBook(EnchantmentTier.LEGENDARY))
                showEnchanterMenu(p)
            }
        })

        menu.set(5, FastItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "§c§lRandom Soul Book", ["§c75,000xp"]), { p, t, s ->
            if (p.getTotalExperience() < 75000) {
                p.sendMessage(playerNotEnoughXp)
            } else {
                setTotalExperience(p, p.getTotalExperience() - 75000)
                mysteryBook.giveStack(p, createMysteryBook(EnchantmentTier.SOUL))
                showEnchanterMenu(p)
            }
        })

        menu.openSync(player)
    }

    static void setTotalExperience(final Player player, final int exp) {
        if (exp < 0) {
            throw new IllegalArgumentException("Experience is negative!")
        } else {
            player.setExp(0.0F)
            player.setLevel(0)
            player.setTotalExperience(0)
            int amount = exp

            while (amount > 0) {
                int expToLevel = getExpAtLevel(player)
                amount -= expToLevel
                if (amount >= 0) {
                    player.giveExp(expToLevel)
                } else {
                    amount += expToLevel
                    player.giveExp(amount)
                    amount = 0
                }
            }
        }
    }

    static int getExpAtLevel(final Player player) {
        return getExpAtLevel(player.getLevel())
    }

    static int getExpAtLevel(final int level) {
        if (level <= 15) {
            return 2 * level + 7
        } else {
            return level >= 16 && level <= 30 ? 5 * level - 38 : 9 * level - 158
        }
    }

    /*
    *   Handles Updating Enchanted Item Lore
    */

    static final String ENCHANT_LORE_START = "§b§§§§"
    static final String ENCHANT_LORE_END = "§c§§§§"

    static final String ADDON_SPACER = "§8§§§§"
    static final String WHITE_SCROLL_START = "§9§§§§"
    static final String HOLY_WHITE_SCROLL_START = "§7§§§§"
    static final String ARMOR_ORB_START = "§6§§§§"

    def updateItemStack(ItemStack itemStack, boolean transmog = false) {
        def data = ItemEnchantmentData.read(itemStack)
        if (!data) return
        updateItemStack(itemStack, data, transmog)
    }

    def updateItemStack(ItemStack itemStack, ItemEnchantmentData itemEnchantmentData, boolean transmog = false) {
        Map<CustomEnchantment, Integer> enchantments = getItemEnchantments(itemEnchantmentData)

        List<String> enchantLore = Lists.newArrayList()

        if (transmog) {
            def name = FastItemUtils.getDisplayName(itemStack) ?: itemStack.getI18NDisplayName()
            if (!name.endsWith("]§§§§")) {
                FastItemUtils.setDisplayName(itemStack, "§f$name §d§l[§b§l§n${enchantments.size()}§d§l]§§§§")
            } else {
                FastItemUtils.setDisplayName(itemStack, name.replaceFirst("\\[§b§l§n[0-9]+§d§l§n]", "§d§l§n[§b§l§n${enchantments.size()}§d§l§n]"))
            }

            def sortedEnchants = enchantments.sort { enchantment1, enchantment2 ->
                enchantment2.value <=> enchantment1.value
            }.sort {enchantment1, enchantment2 ->
                enchantment2.key.enchantmentTier.weight <=> enchantment1.key.enchantmentTier.weight
            }

            //replace item data w/ sorted enchants
            itemEnchantmentData.enchantments.clear()
            sortedEnchants.each { enchantment, level ->
                itemEnchantmentData.enchantments.add(new StoredEnchantment(enchantment.getInternalName(), level))
            }

            itemEnchantmentData.write(itemStack)

            enchantLore.addAll(sortedEnchants.findResults {
                it.key.enchantmentTier.tierColor + it.key.displayName + " " + RomanNumerals.numeralOf(it.value)
            } as List<String>)
        } else {
            enchantLore.addAll(enchantments.findResults {
                it.key.enchantmentTier.tierColor + it.key.displayName + " " + RomanNumerals.numeralOf(it.value)
            } as List<String>)
        }

        List<String> lore = FastItemUtils.getLore(itemStack) ?: new ArrayList<String>()
        List<String> addons = []

        def whiteScrollSpacerIndex = lore.findIndexOf { it.contains(ADDON_SPACER) }
        if (whiteScrollSpacerIndex >= 0) {
            lore.remove(whiteScrollSpacerIndex)
        }

        def whiteScrollStartIndex = lore.findIndexOf { it.contains(WHITE_SCROLL_START) }
        if (whiteScrollStartIndex >= 0) {
            lore.remove(whiteScrollStartIndex)
        }

        if (itemEnchantmentData.whiteScroll) {
            addons.add(ADDON_SPACER)
            addons.add("${WHITE_SCROLL_START}§f§lPROTECTED")
        }

        def holyWhiteScrollStartIndex = lore.findIndexOf { it.contains(HOLY_WHITE_SCROLL_START) }
        if (holyWhiteScrollStartIndex >= 0) {
            lore.remove(holyWhiteScrollStartIndex)
        }

        if (itemEnchantmentData.holyWhiteScrollData.hasHoly) {
            if (!addons.contains(ADDON_SPACER)) {
                addons.add(ADDON_SPACER)
            }
            addons.add("${HOLY_WHITE_SCROLL_START}§f§lPROTECTED (§6§lHOLY§f§l)")
        }

        if (itemEnchantmentData.slotIncrease > 0) {
            if (!addons.contains(ADDON_SPACER)) {
                addons.add(ADDON_SPACER)
            }

            def armorOrbStartIndex = lore.findIndexOf { it.contains(ARMOR_ORB_START) }
            if (armorOrbStartIndex >= 0) {
                lore.remove(armorOrbStartIndex)
            }

            addons.add("${ARMOR_ORB_START}§a§l${getDefSlots() + itemEnchantmentData.slotIncrease} Enchantment Slots §7(Orb [§a+${itemEnchantmentData.slotIncrease}§7])")
        }

        int startIndex = lore.findIndexOf { it.endsWith(ENCHANT_LORE_START) }
        int endIndex = lore.findIndexOf { it.endsWith(ENCHANT_LORE_END) }

        if (enchantments.isEmpty()) {
            if (!addons.isEmpty()) lore.addAll(addons)

            if (startIndex >= 0 && endIndex >= 0) lore.subList(startIndex, endIndex + 1).clear()

            FastItemUtils.setLore(itemStack, lore)
            return
        }



        if (startIndex >= 0 && endIndex >= 0) {
            lore.subList(startIndex, endIndex + 1).clear()
        }

        if (enchantLore.size() == 1) {
            enchantLore.set(0, enchantLore[0] + ENCHANT_LORE_START)
            enchantLore.add(ENCHANT_LORE_END)
        } else {
            enchantLore.set(0, enchantLore[0] + ENCHANT_LORE_START)
            enchantLore.set(enchantLore.size() - 1, enchantLore[enchantLore.size() - 1] + ENCHANT_LORE_END)
        }

        // Combine the enchantLore and lore lists
        if (!lore.isEmpty()) {
            enchantLore.addAll(lore)
        }

        if (!addons.isEmpty()) {
            enchantLore.addAll(addons)
        }

        // Set the combined lore to itemStack
        FastItemUtils.setLore(itemStack, enchantLore)
        FastItemUtils.hideAttributes(itemStack)
    }

    /*
    *   Handles Data Structures
    */

    Map<CustomEnchantment, Integer> getItemEnchantments(ItemEnchantmentData itemEnchantmentData) {
        if (itemEnchantmentData == null) return Collections.emptyMap()

        return itemEnchantmentData.getEnchantments().collectEntries { ench ->
            CustomEnchantment enchantment = getEnchantmentById(ench.enchantment)
            if (enchantment == null) return []
            [enchantment, ench.level]
        }
    }

    @NotNull
    Map<CustomEnchantment, Integer> getItemEnchantments(ItemStack itemStack) {
        ItemEnchantmentData itemEnchantmentData = ItemEnchantmentData.read(itemStack)
        if (itemEnchantmentData == null) return Collections.emptyMap()

        return itemEnchantmentData.getEnchantments().collectEntries { ench ->
            CustomEnchantment enchantment = getEnchantmentById(ench.enchantment)
            if (enchantment == null) return []
            [enchantment, ench.level]
        }
    }

    def addEnchantToItem(ItemStack itemStack, CustomEnchantment customEnchantment, int level) {
        ItemEnchantmentData itemEnchantmentData = ItemEnchantmentData.read(itemStack)
        if (itemEnchantmentData == null) {
            itemEnchantmentData = new ItemEnchantmentData(itemStack)
        }

        itemEnchantmentData.addEnchantment(customEnchantment, level)
        itemEnchantmentData.write(itemStack)
        updateItemStack(itemStack, itemEnchantmentData)
    }

    def removeEnchantFromItem(ItemStack itemStack, CustomEnchantment customEnchantment) {
        ItemEnchantmentData itemEnchantmentData = ItemEnchantmentData.read(itemStack)
        if (itemEnchantmentData == null) {
            itemEnchantmentData = new ItemEnchantmentData(itemStack)
        }

        itemEnchantmentData.removeEnchantment(customEnchantment)
        itemEnchantmentData.write(itemStack)
        updateItemStack(itemStack, itemEnchantmentData)
    }

    def removeEnchantFromItem(ItemStack itemStack, String customEnchantmentId) {
        ItemEnchantmentData itemEnchantmentData = ItemEnchantmentData.read(itemStack)
        if (itemEnchantmentData == null) {
            itemEnchantmentData = new ItemEnchantmentData(itemStack)
        }

        itemEnchantmentData.removeEnchantment(customEnchantmentId)
        itemEnchantmentData.write(itemStack)
        updateItemStack(itemStack, itemEnchantmentData)
    }

    /*
    *   Handling Opening GUIs
     */
    def openAdminEnchantGUI(Player player) {
        MenuBuilder builder

        builder = new MenuBuilder(54, "§dAdmin Enchanter")

        builder.set(0, createWhiteScroll(), { p, t, s ->
            whiteScroll.giveStack(p)
        })

        builder.set(1, createHolyWhiteScroll(), { p, t, s ->
            holyWhiteScroll.giveStack(p, createHolyWhiteScroll(true)) // anti dupe
        })

        builder.set(2, createBlackScroll(), { p, t, s ->
            SelectionUtils.selectInteger(p) { int amount ->
                if (amount > 100) amount = 100
                if (amount < 1) amount = 1

                blackScroll.giveStack(p, createBlackScroll(amount))
                openAdminEnchantGUI(p)
            }
        })

        builder.set(3, createMysteryBook(EnchantmentTier.SIMPLE), { p, t, s ->
            selectEnchantTier(player) {
                mysteryBook.giveStack(p, createMysteryBook(it))
                openAdminEnchantGUI(p)
            }
        })

        builder.set(4, createMysteryEnchantmentDust(EnchantmentTier.SIMPLE), { p, t, s ->
            selectEnchantTier(player) {
                mysteryEnchantmentDust.giveStack(p, createMysteryEnchantmentDust(it))
                openAdminEnchantGUI(p)
            }
        })

        builder.set(5, createEnchantmentOrb(100, 1, EnchantmentOrbType.ARMOR), { p, t, s ->
            SelectionUtils.selectInteger(p, "§aChoose Success Rate", [5,10,25,30,40,50,75,100]) { int successRate ->
                if (successRate > 100) successRate = 100
                if (successRate < 1) successRate = 1

                SelectionUtils.selectInteger(p, "§cChoose Slot Increase", [1,2,3,4,5,6,7,8]) { slotIncrease ->
                    SelectionUtils.selectAB(p, "§cChoose Orb Type", "Weapon", "Armour", {
                        enchantmentOrb.giveStack(p, createEnchantmentOrb(successRate, slotIncrease, EnchantmentOrbType.WEAPON))
                        openAdminEnchantGUI(p)
                    }, {
                        enchantmentOrb.giveStack(p, createEnchantmentOrb(successRate, slotIncrease, EnchantmentOrbType.ARMOR))
                        openAdminEnchantGUI(p)
                    })
                }
            }
        })

        builder.set(54 - 5, FastItemUtils.createItem(Material.ENCHANTING_TABLE, "§d~ Admin Enchanter ~", ["§3click me"]), { p, t, s ->
            openAdminEnchanterGUI(p)
        })

        builder.openSync(player)
    }

    def

    static Map<UUID, ItemStack> activeItems = new HashMap<>()
    static Map<UUID, EnchantmentTier> activeTier = new HashMap<>()
    static Map<UUID, ItemEnchantmentData> copiedData = new HashMap<>()

    static ItemStack getActiveItem(Player player) {
        return activeItems.get(player.getUniqueId())
    }

    def openAdminEnchanterGUI(Player player) {
        MenuBuilder builder = new MenuBuilder(54, "§7Select an Item")

        boolean refreshing = false

        if (activeItems.containsKey(player.getUniqueId())) {
            builder.set(1, 5, getActiveItem(player), { p, t, s ->
                Schedulers.sync().execute {
                    def item = builder.get().getItem(s)
                    if (item == null || item.getType() == Material.AIR) return
                    p.getInventory().addItem(item)
                    activeItems.remove(p.getUniqueId())
                    openAdminEnchanterGUI(p)
                }
            })

            builder.set(54 - 8, FastItemUtils.createItem(Material.PAPER, "§f~ whitecroll ~", ["§cfalse"]), { p, t, s ->

            })
            builder.set(54 - 7, FastItemUtils.createItem(Material.MAP, "§f~ holywhitecroll ~", ["§cfalse"]), { p, t, s ->

            })

            builder.set(54 - 6, FastItemUtils.createItem(Material.MAP, "§f~ transmog ~", ["§3click me"]), { p, t, s ->
                refreshing = true
                updateItemStack(getActiveItem(player), true)
                openAdminEnchanterGUI(p)
            })

            def tier = activeTier.get(player.getUniqueId()) ?: EnchantmentTier.SIMPLE
            if (tier) {
                int index = 9
                getTieredEnchantments(activeTier.get(player.getUniqueId())).each { enchant ->
                    if (!enchant.canBeAppliedTo(getActiveItem(player))) return
                    if (index > 45) return
                    builder.set(index, FastItemUtils.createItem(Material.PAPER, "${enchant.displayName} ${RomanNumerals.numeralOf(1)}", ["§3click me"]), { p, t, s ->
                        refreshing = true

                        selectEnchantLevel(p, enchant.maxLevel) {
                            addEnchantToItem(getActiveItem(p), enchant, it)
                            openAdminEnchanterGUI(p)
                        }
                    })
                    index++
                }

                builder.set(54 - 4, FastItemUtils.createItem(Material.MAP, "§f~ tier ~", ["§c${tier.tierColor}${tier.tierName}"]), { p, t, s ->
                    refreshing = true

                    selectEnchantTier(p) {
                        activeTier.put(p.getUniqueId(), it)
                        openAdminEnchanterGUI(p)
                    }
                })
            }

            builder.set(54 - 3, FastItemUtils.createItem(Material.MAP, "§f~ copy ~", ["§3click me"]), { p, t, s ->
                refreshing = true

                copiedData.put(p.getUniqueId(), ItemEnchantmentData.read(getActiveItem(p)))
            })

            builder.set(54 - 2, FastItemUtils.createItem(Material.MAP, "§f~ paste ~", ["§3click me"]), { p, t, s ->
                ItemEnchantmentData data = copiedData.get(p.getUniqueId())
                if (data) {
                    data.write(getActiveItem(p))
                    updateItemStack(getActiveItem(p))

                    p.getInventory().addItem(activeItems.get(p.getUniqueId()))
                    activeItems.remove(p.getUniqueId())

                    openAdminEnchanterGUI(p)
                }
            })
        }

        builder.set(54 - 5, FastItemUtils.createItem(Material.RED_DYE, "§c~ back ~", ["§3click me"]), { p, t, s ->
            openAdminEnchantGUI(p)
        })

        builder.setExternal {p, t, s ->
            if (t != ClickType.LEFT) return

            if (s < 0 || s > 36) return

            def item = p.getInventory().getItem(s)

            if (item == null || item.getType() == Material.AIR) return

            if (item.amount > 1) {
                p.sendMessage("§c§l(!) §cYou can only enchant one item at a time!")
                return
            }

            if (activeItems.get(p.getUniqueId())) {
                p.sendMessage("§c§l(!) §cYou already have an active item!")
                return
            }

            activeItems.put(p.getUniqueId(), item)
            activeTier.put(p.getUniqueId(), EnchantmentTier.SIMPLE)
            p.getInventory().setItem(s, null)
            p.updateInventory()

            refreshing = true

            openAdminEnchanterGUI(p)
        }

        builder.setCloseCallback { p ->
            if (!refreshing)
            {
                if (activeItems.get(p.getUniqueId()))
                {
                    p.getInventory().addItem(activeItems.get(p.getUniqueId()))
                    activeItems.remove(p.getUniqueId())
                }
            }
        }

        builder.openSync(player)
    }

    def selectEnchantTier(Player player, Callback<EnchantmentTier> enchantmentTierCallback) {
        MenuBuilder builder = new MenuBuilder(9, "§7Select a Tier")
        int index = 0
        EnchantmentTier.values().each {
            ItemStack stack = FastItemUtils.createItem(
                    it.getGlassPane(),
                    "${it.tierColor}${it.tierName} §7Enchants",
                    ["",
                     "§fClick me to view all of the",
                     "${it.tierColor}${it.tierName} §fenchants.",
                     ""],
                    false
            )
            builder.set(index, stack, { p, t, s ->
                return enchantmentTierCallback.exec(it)
            })
            index++
        }

        builder.openSync(player)
    }

    def selectEnchantLevel(Player player, int max, Callback<Integer> enchantmentLevelCallback) {
        MenuBuilder builder = new MenuBuilder(max > 9 ? 18 : 9, "§7Select a Tier")
        max.times {
            ItemStack stack = FastItemUtils.createItem(
                    Material.PAPER,
                    "§7Level ${it + 1}",
                    ["",
                     "§fClick me to select level §7${it + 1}",
                     ""],
                    false
            )

            builder.set(it, stack, { p, t, s ->
                return enchantmentLevelCallback.exec(it + 1)
            })
        }

        builder.openSync(player)
    }

    /*
    *   Handling Click Items
    */
    static String whiteScrollId = "enchant_whitescroll"
    static String holyWhiteScrollId = "enchant_holywhitescroll"
    static String transmogScrollId = "enchant_transgmogscroll"
    static String blackScrollId = "enchant_blackscroll"
    static String enchantmentDustId = "enchant_enchantmentdust"
    static String mysteryEnchantmentDustId = "enchant_mysteryenchantmentdust"
    static String enchantmentOrbId = "enchant_enchantmentorb"
    static String mysteryBookId = "enchant_mysterybook"
    static String enchantmentBookId = "enchant_enchantmentbook"
    static String soulPearlId = "enchant_soulpearl"
    static String randomSoulGeneratorId = "enchant_randomsoulgenerator"
    static String timeMachineId = "enchant_timemachine"

    ClickItem whiteScroll
    ClickItem holyWhiteScroll
    ClickItem blackScroll
    ClickItem transmogScroll

    ClickItem enchantmentDust
    ClickItem mysteryEnchantmentDust
    ClickItem enchantmentOrb

    ClickItem mysteryBook
    ClickItem enchantmentBook
    ClickItem soulPearl
    ClickItem randomSoulGenerator
    ClickItem timeMachine

    int getMaxSlots() {
        return items.getOrCreateConfig(enchantmentOrbId).getIntEntry(EnchantConfigConst.enchantOrbMaxSlots.getId()).value
    }

    int getDefSlots() {
        return items.getOrCreateConfig(enchantmentOrbId).getIntEntry(EnchantConfigConst.enchantOrbDefSlots.getId()).value
    }

    def createClickItems() {
        whiteScroll = new ClickItem(whiteScrollId, createWhiteScroll(), { Player player, PlayerInteractEvent event, ClickItem item ->
            event.setCancelled(true)
            Players.msg(player, "§] §> §cYou cannot use this item.")
        }, { Player player, InventoryClickEvent event, ClickItem item ->
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != event.getWhoClicked()) return
            if (event.getWhoClicked() != player) return

            ItemStack currentItem = event.getCurrentItem()
            ItemStack cursor = event.getCursor()

            if (currentItem == null || currentItem.type == Material.AIR || cursor == null || cursor.type == Material.AIR) return

            Inventory inventory = player.getOpenInventory().getTopInventory()
            if (inventory.getType() != InventoryType.CRAFTING) return

            WhiteScrollItemData whiteScrollItemData = WhiteScrollItemData.read(cursor)
            if (whiteScrollItemData == null) return

            if (whiteScrollItemData.holyWhiteScroll) {
                return
            }

            ItemEnchantmentData itemEnchantmentData = ItemEnchantmentData.read(currentItem)
            if (!itemEnchantmentData) {
                itemEnchantmentData = new ItemEnchantmentData(currentItem)
            }

            if (itemEnchantmentData.whiteScroll) {
                Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
                Players.msg(player, "§] §> §cThis item already has a white scroll applied.")
                return
            }

            event.setCancelled(true)

            if (event.getCursor().getAmount() > 1) {
                event.getCursor().setAmount(event.getCursor().getAmount() - 1)
            } else {
                event.getView().setCursor(null)
                player.setItemOnCursor(null)
            }

            itemEnchantmentData.whiteScroll = true
            itemEnchantmentData.write(currentItem)

            updateItemStack(currentItem, itemEnchantmentData)

            Players.playSound(player, Sound.ENTITY_PLAYER_LEVELUP)
            Players.msg(player, "§] §> §aWhite Scroll Applied!")
        })

        holyWhiteScroll = new ClickItem(holyWhiteScrollId, createHolyWhiteScroll(), { Player player, PlayerInteractEvent event, ClickItem item ->
            event.setCancelled(true)
            Players.msg(player, "§] §> §cYou cannot use this item.")
        }, { Player player, InventoryClickEvent event, ClickItem item ->
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != event.getWhoClicked()) return
            if (event.getWhoClicked() != player) return

            ItemStack currentItem = event.getCurrentItem()
            ItemStack cursor = event.getCursor()

            if (currentItem == null || currentItem.type == Material.AIR || cursor == null || cursor.type == Material.AIR) return

            Inventory inventory = player.getOpenInventory().getTopInventory()
            if (inventory.getType() != InventoryType.CRAFTING) return

            WhiteScrollItemData whiteScrollItemData = WhiteScrollItemData.read(cursor)
            if (whiteScrollItemData == null) return

            if (!whiteScrollItemData.holyWhiteScroll) {
                return
            }

            ItemEnchantmentData itemEnchantmentData = ItemEnchantmentData.read(currentItem)
            if (!itemEnchantmentData) {
                itemEnchantmentData = new ItemEnchantmentData(currentItem)
            }

            def holyData = itemEnchantmentData.getHolyWhiteScrollData()

            if (holyData.hasHoly) {
                Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
                Players.msg(player, "§] §> §cThis item already has a holy white scroll applied.")
                return
            }

            if (holyData.holiesApplied >= items.getOrCreateConfig(holyWhiteScrollId).getIntEntry(EnchantConfigConst.holyWhiteScrollMaxUses.getId()).value) {
                Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
                Players.msg(player, "§] §> §cThis item has reached the maximum amount of holy white scrolls applied.")
                return
            }

            if (!itemEnchantmentData.whiteScroll) {
                Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
                Players.msg(player, "§] §> §cThis item does not have a white scroll applied.")
                return
            }

            def id = FastItemUtils.getId(cursor)
            if (id == null ) {
                Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
                Players.msg(player, "§] §> §cError, Contact Staff.")
                return
            }

            event.setCancelled(true)

            if (AntiDupeUtils.isDuped(id)) {
                Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
                Players.msg(player, "§] §> §cError, Contact Staff.")
                return
            }

            AntiDupeUtils.useId(id) {
                if (event.getCursor().getAmount() > 1) { // ???
                    event.getCursor().setAmount(event.getCursor().getAmount() - 1)
                } else {
                    event.getView().setCursor(null)
                    player.setItemOnCursor(null)
                }

                holyData.holiesApplied++
                holyData.hasHoly = true
                itemEnchantmentData.whiteScroll = false

                itemEnchantmentData.write(currentItem)
                updateItemStack(currentItem, itemEnchantmentData)
                Players.playSound(player, Sound.ENTITY_PLAYER_LEVELUP)
                Players.msg(player, "§] §> §aHoly White Scroll Applied!")
            }
        })

        transmogScroll = new ClickItem(transmogScrollId, createTransmogScroll(), { Player player, PlayerInteractEvent event, ClickItem item ->
            event.setCancelled(true)
            Players.msg(player, "§] §> §cYou cannot use this item.")
        }, { Player player, InventoryClickEvent event, ClickItem item ->
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != event.getWhoClicked()) return
            if (event.getWhoClicked() != player) return

            ItemStack currentItem = event.getCurrentItem()
            ItemStack cursor = event.getCursor()

            if (currentItem == null || currentItem.type == Material.AIR || cursor == null || cursor.type == Material.AIR) return

            Inventory inventory = player.getOpenInventory().getTopInventory()
            if (inventory.getType() != InventoryType.CRAFTING) return

            TransmogScrollData transmogScrollData = TransmogScrollData.read(cursor)
            if (transmogScrollData == null) return

            ItemEnchantmentData itemEnchantmentData = ItemEnchantmentData.read(currentItem)
            if (itemEnchantmentData == null) {
                itemEnchantmentData = new ItemEnchantmentData(currentItem)
            }

            event.setCancelled(true)

            if (event.getCursor().getAmount() > 1) {
                event.getCursor().setAmount(event.getCursor().getAmount() - 1)
            } else {
                event.getView().setCursor(null)
                player.setItemOnCursor(null)
            }

            updateItemStack(currentItem, itemEnchantmentData, true)
            Players.playSound(player, Sound.ENTITY_PLAYER_LEVELUP)
            Players.msg(player, "§] §> §aTransmog Scroll Applied!")
        })

        blackScroll = new ClickItem(blackScrollId, createBlackScroll(), { Player player, PlayerInteractEvent event, ClickItem item ->
            event.setCancelled(true)
            Players.msg(player, "§] §> §cYou cannot use this item.")
        }, { Player player, InventoryClickEvent event, ClickItem item ->
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != event.getWhoClicked()) return

            ItemStack currentItem = event.getCurrentItem()
            ItemStack cursor = event.getCursor()

            if (currentItem == null || currentItem.type == Material.AIR || cursor == null || cursor.type == Material.AIR) return
            Inventory inventory = player.getOpenInventory().getTopInventory()
            if (inventory.getType() != InventoryType.CRAFTING) return

            BlackScrollData data = BlackScrollData.read(cursor)
            if (data == null) return

            event.setCancelled(true)

            def itemEnchants = getItemEnchantments(currentItem).entrySet()
            if (itemEnchants.size() < 1) return

            def enchantToRemove = itemEnchants[new Random().nextInt(itemEnchants.size())]

            removeEnchantFromItem(currentItem, enchantToRemove.key)
            enchantmentBook.giveStack(player, createBook(enchantToRemove.key, enchantToRemove.value, data.newSuccessRate, 100))

            cursor.amount == 1 ? (cursor = null) : (cursor.setAmount(cursor.amount - 1))
            event.getView().setCursor(cursor)
            player.setItemOnCursor(cursor)
            player.updateInventory()
        })

        enchantmentDust = new ClickItem(enchantmentDustId, createEnchantmentDust(EnchantmentTier.SIMPLE), { Player player, PlayerInteractEvent event, ClickItem item ->
            event.setCancelled(true)
            Players.msg(player, "§] §> §cYou cannot use this item.") // show dust stats?
            ItemStack
        }, { Player player, InventoryClickEvent event, ClickItem item ->
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != event.getWhoClicked()) return

            ItemStack currentItem = event.getCurrentItem()
            ItemStack cursor = event.getCursor()

            if (currentItem == null || currentItem.type == Material.AIR || cursor == null || cursor.type == Material.AIR) return

            EnchantmentDustData enchantmentDustData = EnchantmentDustData.read(cursor)
            if (enchantmentDustData == null) return

            event.setCancelled(true)

            def bookData = BookEnchantmentData.read(currentItem)
            if (bookData == null) {
                Players.msg(player, "§] §> §cYou must use dust on an Enchantment Book")
                return
            }

            if (bookData.getStoredEnchantments().keySet().size() > 0 && bookData.getStoredEnchantments().keySet()[0].enchantmentTier != enchantmentDustData.enchantmentTier) {
                Players.msg(player, "§] §> §cYou must use dust on an Enchantment Book of the same rarity.")
                return
            }

            cursor.setAmount(cursor.getAmount() - 1)

            def newSuccess = bookData.successChance + enchantmentDustData.successIncrease

            if (newSuccess > 100) {
                newSuccess = 100
            }

            bookData.successChance = newSuccess
            CustomEnchantment ce = bookData.internalEnchantments.collect {
                getEnchantmentById(it.enchantment)
            }.findAll {it != null}[0]

            List<String> lore = items.getOrCreateConfig(enchantmentBookId).getStringListEntry(EnchantConfigConst.enchantBookLore.getId()).value
            lore = lore.collect {
                it.replace("{percent}", bookData.successChance.toInteger().toString())
                        .replace("{destroyPercent}", bookData.destroyChance.toInteger().toString())
                        .replace("{description}", ce.description.toString())
                        .replace("{applicability}", ce.applicability.stream().map { it.displayName }.collect(Collectors.joining(", ")))
            } as List<String>

            bookData.write(currentItem)
            FastItemUtils.setLore(currentItem, lore)

            player.updateInventory()
            Players.playSound(player, Sound.ENTITY_PLAYER_LEVELUP)
        })

        mysteryEnchantmentDust = new ClickItem(mysteryEnchantmentDustId, createMysteryEnchantmentDust(EnchantmentTier.SIMPLE), { Player player, PlayerInteractEvent event, ClickItem item ->
            event.setCancelled(true)
            Players.msg(player, "§] §> §cYou cannot use this item.") // consume dust
        })

        enchantmentOrb = new ClickItem(enchantmentOrbId, createEnchantmentOrb(100, 1, EnchantmentOrbType.ARMOR), { Player player, PlayerInteractEvent event, ClickItem item ->
            event.setCancelled(true)
            Players.msg(player, "§] §> §cYou cannot use this item.")
        }, { Player player, InventoryClickEvent event, ClickItem item ->
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != event.getWhoClicked()) return

            ItemStack currentItem = event.getCurrentItem()
            ItemStack cursor = event.getCursor()

            if (currentItem == null || currentItem.type == Material.AIR || cursor == null || cursor.type == Material.AIR) return

            EnchantmentOrbData enchantmentOrbData = EnchantmentOrbData.read(cursor)
            if (enchantmentOrbData == null) return

            event.setCancelled(true)

            if (!enchantmentOrbData.enchantmentOrbType.isType(currentItem)) {
                Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
                Players.msg(player, "§] §> §cThis item cannot be enchanted with this orb.")
                return
            }

            def itemData = ItemEnchantmentData.read(currentItem)
            if (itemData == null) {
                itemData = new ItemEnchantmentData(currentItem)
                itemData.write(currentItem)
            }

            def oldSlots = itemData.slotIncrease + getDefSlots()
            def newSlots = enchantmentOrbData.slotIncrease + getDefSlots()

            if (oldSlots >= newSlots) {
                Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
                Players.msg(player, "§] §> §cThis item has more slots already.")
                return
            }

            cursor.amount == 1 ? (cursor = null) : (cursor.setAmount(cursor.amount - 1))
            event.getView().setCursor(cursor)
            player.setItemOnCursor(cursor)

            if (newSlots > getMaxSlots()) {
                newSlots = getMaxSlots()
            }

            itemData.slotIncrease = enchantmentOrbData.slotIncrease
            itemData.write(currentItem)

            updateItemStack(currentItem, itemData)
            Players.playSound(player, Sound.ENTITY_PLAYER_LEVELUP)
            Players.msg(player, "§] §> §aEnchantment Orb Applied!")
        })

        randomSoulGenerator = new ClickItem(randomSoulGeneratorId, createRandomSoulGenerator(), { Player player, PlayerInteractEvent event, ClickItem item ->
            ItemStack stack = event.item

            if (stack == null || stack.type == Material.AIR) return

            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (stack.getType() != Material.EMERALD) return
                RandomSoulGenData randomSoulGenData = RandomSoulGenData.read(stack)
                if (randomSoulGenData == null) return

                event.setCancelled(true)

                if (player.getInventory().firstEmpty() == -1) return

                ItemStack randomSouls = SoulUtils.createSoulGem(randomSoulGenData.getRandomSouls())
                player.getInventory().addItem(randomSouls)
                player.getInventory().removeItem(event.item)
            }
        })

        mysteryBook = new ClickItem(mysteryBookId, createMysteryBook(EnchantmentTier.SIMPLE), { Player player, PlayerInteractEvent event, ClickItem item ->
            ItemStack stack = event.item

            if (stack == null || stack.type == Material.AIR) return

            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                // handle mystery books here
                if (stack.getType() == Material.BOOK) {
                    MysteryBookData mysteryBookData = MysteryBookData.read(stack)
                    if (mysteryBookData == null) return

                    event.setCancelled(true)

                    List<CustomEnchantment> enchants = getTieredEnchantments(mysteryBookData.enchantmentTier)
                    if (enchants == null || enchants.size() == 0) {
                        player.sendMessage("§cThere was an error getting the enchantments for this book.")
                        return
                    }

                    if (player.getInventory().firstEmpty() == -1) return

                    CustomEnchantment enchant = enchants.get(new Random().nextInt(enchants.size()))

                    // TODO: add support for ranks getting better enchant %'s
                    int level = new Random().nextInt(enchant.maxLevel) + 1

                    int successChance = (int) Math.round(new Random().nextDouble(100))
                    int destroyChance = (int) Math.round(new Random().nextDouble(100))

                    FastInventoryUtils.use(player)
                    enchantmentBook.giveStack(player, createBook(enchant, level, successChance, destroyChance))
                    player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.7f, 1.4f)
                    Players.msg(player, "§aYou have received a §r" + enchant.enchantmentTier.tierColor + enchant.getDisplayName() + "§a book.")
                }
            }
            event.setCancelled(true)
        })

        enchantmentBook = new ClickItem(enchantmentBookId, createBook(registeredEnchantments.values()[0], 1), { Player player, PlayerInteractEvent event, ClickItem item ->
            event.setCancelled(true)
            Players.msg(player, "§] §> §cYou cannot use this item.") // show book info?
        }, { Player player, InventoryClickEvent event, ClickItem item ->
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != event.getWhoClicked()) return

            ItemStack currentItem = event.getCurrentItem()
            ItemStack cursor = event.getCursor()

            if (currentItem == null || currentItem.type == Material.AIR || cursor == null || cursor.type == Material.AIR) return
            if (cursor.type != Material.BOOK || currentItem.type == Material.BOOK) return

            Inventory inventory = player.getOpenInventory().getTopInventory()
            if (inventory.getType() != InventoryType.CRAFTING) return

            BookEnchantmentData bookEnchantmentData = BookEnchantmentData.read(cursor)
            if (bookEnchantmentData == null) return

            event.setCancelled(true)

            Map<CustomEnchantment, Integer> itemEnchants = getItemEnchantments(currentItem)
            Map<CustomEnchantment, Integer> bookEnchants = bookEnchantmentData.getStoredEnchantments()
            if (bookEnchants.keySet().stream().anyMatch(customEnchantment -> !customEnchantment.canBeAppliedTo(currentItem))) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F)
                Players.msg(player, "§cThis item cannot be enchanted with this book.")
                return
            }


            Map<CustomEnchantment, Integer> enchantsToApply = Maps.newHashMap()
            bookEnchants.entrySet().stream().filter(entry -> itemEnchants.getOrDefault(entry.getKey(), 0) < entry.getValue()).forEach(entry -> enchantsToApply.put(entry.getKey(), entry.getValue()))
            if (enchantsToApply.isEmpty()) {
                Players.msg(player, "§cThis item already has all of the enchantments from this book.")
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F)
                return
            }

            def itemData = ItemEnchantmentData.read(currentItem)
            if (itemData == null) {
                itemData = new ItemEnchantmentData(currentItem)
                itemData.write(currentItem)
            }

//            // TODO add support for enchant orbs to not use the book if slots are full
            def maxSlots = itemData.slotIncrease + getDefSlots() + (enchantsToApply.entrySet().key.get(0) instanceof HeroicEnchant ? 0 : 1)
            if (itemEnchants.size() + enchantsToApply.size() > maxSlots) {
                Players.msg(player, "§cThis item does not have enough slots to apply these enchantments.")
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F)
                return
            }
            boolean flag1 = false
            def enchants = bookEnchants.entrySet()
            Set<String> enchantsToRemove = Sets.newHashSet()
            for (def entry : enchants) { //loop probably only runs once but here just to be safe
                CustomEnchantment customEnchantment = entry.key
                if (customEnchantment instanceof HeroicEnchant) {
                    boolean found = false
                    itemData.getAllEnchantments().forEach {
                        if (it.enchantment == customEnchantment.overwriteEnchantId) {
                            enchantsToRemove.add(it.enchantment)
                            enchantsToApply.put(customEnchantment, entry.value)
                            found = true
                        }
                    }
                    if (!found) {
                        player.sendMessage("§cThis item does not have the required prerequisite enchant.")
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F)
                        return
                    }
                } else {
                    itemData.enchantments.forEach {storedEnchantment ->
                        if (storedEnchantment.overrideEnchant == customEnchantment.getInternalName()) {
                            Players.msg(player, "§cThis item already has all of the enchantments from this book.")
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F)
                            flag1 = true
                        }
                    }
                }
            }
            if (flag1) return

            cursor.amount == 1 ? (cursor = null) : (cursor.setAmount(cursor.amount - 1))
            event.getView().setCursor(cursor)
            player.setItemOnCursor(cursor)

            double successChance = bookEnchantmentData.successChance
            if (ThreadLocalRandom.current().nextDouble(100) > successChance) { // not successful
                double destroyChance = bookEnchantmentData.getDestroyChance()
                if (destroyChance > ThreadLocalRandom.current().nextDouble(100)) {
                    // TODO add white scrolls ? maybe done
                    ItemEnchantmentData itemEnchantData = ItemEnchantmentData.read(currentItem)

                    if (itemEnchantData != null) {
                        if (itemEnchantData.whiteScroll) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)

                            Players.msg(player, "§] §> §aA §f§l§nWhite Scroll§a saved your §r${FastItemUtils.getDisplayName(currentItem)}§a!")

                            itemEnchantData.whiteScroll = false
                            itemEnchantData.write(currentItem)
                            updateItemStack(currentItem, itemEnchantData)
                            player.updateInventory()
                            return
                        }
                    }

                    event.setCurrentItem(null)
                    player.getInventory().setItem(event.getSlot(), null)
                    player.updateInventory()

                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F)

                    String message = items.getOrCreateConfig(enchantmentBookId).getStringEntry(EnchantConfigConst.messageEnchantFailedDestroyed.getId()).value
                    message = message.replace("{item}", "§r${FastItemUtils.getDisplayName(currentItem)}")

                    Players.msg(player, message)
                    return
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 1F)

                    String message = (items.getOrCreateConfig(enchantmentBookId).getStringEntry(EnchantConfigConst.messageEnchantFailed.getId()).value).replace("{item}", "§r${FastItemUtils.getDisplayName(currentItem)}")
                    Players.msg(player, message)
                }
            }

            enchantsToRemove.forEach((customEnchantmentId) -> removeEnchantFromItem(currentItem, customEnchantmentId))
            enchantsToApply.forEach((customEnchantment, level) -> addEnchantToItem(currentItem, customEnchantment, level))
            player.updateInventory()

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1F, 1F)
            String message = items.getOrCreateConfig(enchantmentBookId).getStringEntry(EnchantConfigConst.messageEnchantSuccess.getId()).value
            message = message.replace("{item}", "§r${FastItemUtils.getDisplayName(currentItem)}")
            message = message.replace("{enchant}", "${enchantsToApply.keySet()[0].enchantmentTier.tierColor}§l${enchantsToApply.keySet()[0].getDisplayName()}") // no support for multiple enchants yet
            Players.msg(player, message)
        })

        soulPearl = new ClickItem(soulPearlId, createSoulPearl(), { Player player, PlayerInteractEvent event, ClickItem item ->
            if (event.getAction().isLeftClick()) return
            if (CooldownUtils.get(player, "soulPearl", 10 * 1000L) > 0) {
                player.sendMessage("§cThis action is on cooldown.")
                event.setCancelled(true)
                return
            }
            if (!SoulUtils.consumeSouls(player, 10, true)) {
                player.sendMessage("§cNot enough souls.")
                event.setCancelled(true)
                return
            }
            event.getItem().setAmount(event.getItem().amount + 1)
            CooldownUtils.set(player, "soulPearl")
        })

        timeMachine = new ClickItem(timeMachineId, createTimeMachine(), { Player player, PlayerInteractEvent event, ClickItem item ->
            if (event.getAction().isLeftClick()) return
            def timeMachineData = TimeMachineData.read(event.getItem())
            if (timeMachineData == null || timeMachineData.used) return

            if (timeMachineTasks.containsKey(player.getUniqueId())) {
                timeMachineTasks.get(player.getUniqueId()).stop()
                timeMachineTasks.remove(player.getUniqueId())

                timeMachineData.used = true
                timeMachineData.write(event.getItem())
                FastItemUtils.setDisplayName(event.getItem(), "§e§l§mTime Machine§r§c [BROKEN]")

                Task teleportTask

                teleportTask = Schedulers.sync().runRepeating({
                    if (timeMachineUsers.get(player.getUniqueId()).isEmpty()) {
                        timeMachineUsers.remove(player.getUniqueId())
                        teleportTask.stop()
                        return
                    }

                    player.teleport(timeMachineUsers.get(player.getUniqueId()).pop())
                }, 1, 2)
            } else {
                timeMachineUsers.put(player.getUniqueId(), new Stack<Location>())
                player.sendMessage("§e§lYou have activated your time machine!§r§e Click it again within 30 seconds to teleport back to this location.")

                int ticks = 0
                Task timeMachineTask

                timeMachineTasks.put(player.getUniqueId(), Schedulers.sync().runRepeating({
                    if (ticks > 30) {
                        timeMachineData.used = true
                        timeMachineData.write(event.getItem())
                        FastItemUtils.setDisplayName(event.getItem(), "§e§l§mTime Machine§r§c [BROKEN]")
                        timeMachineTasks.remove(player.getUniqueId())
                        timeMachineTask.stop()
                        return
                    }
                    /*if (ticks % 2 == 0) */timeMachineUsers.get(player.getUniqueId()).add(player.location)
                    if (ticks > 20) player.sendMessage("§eYour time machine will break in ${30-ticks} seconds!")

                    ticks++
                }, 0, 20))
            }
        })

        ClickItems.register(whiteScroll)
        ClickItems.register(holyWhiteScroll)
        ClickItems.register(transmogScroll)
        ClickItems.register(blackScroll)
        ClickItems.register(enchantmentDust)
        ClickItems.register(mysteryEnchantmentDust)
        ClickItems.register(enchantmentOrb)
        ClickItems.register(mysteryBook)
        ClickItems.register(enchantmentBook)
        ClickItems.register(randomSoulGenerator)
        ClickItems.register(soulPearl)
        ClickItems.register(timeMachine)
    }

    ItemStack createBook(CustomEnchantment enchantment, int enchantLevel, int successChance = 100, int destroyChance = 0) {
        ItemStack enchantedBook = new ItemStack(Material.BOOK)

        BookEnchantmentData bookEnchantmentData = new BookEnchantmentData(Collections.singletonList(new StoredEnchantment(enchantment.getInternalName(), enchantLevel)), successChance, destroyChance)
        bookEnchantmentData.write(enchantedBook)

        FastItemUtils.setDisplayName(enchantedBook, items.getOrCreateConfig(enchantmentBookId).getStringEntry(EnchantConfigConst.enchantBookName.getId()).value
                .replace("{tierColor}", enchantment.enchantmentTier.tierColor)
                .replace("{enchantName}", enchantment.displayName)
                .replace("{romanNumeral}", RomanNumerals.numeralOf(enchantLevel)))

        List<String> lore = items.getOrCreateConfig(enchantmentBookId).getStringListEntry(EnchantConfigConst.enchantBookLore.getId()).value
        lore = lore.collect {
            it.replace("{percent}", successChance.toString())
                    .replace("{destroyPercent}", destroyChance.toString())
                    .replace("{description}", enchantment.description.toString())
                    .replace("{applicability}", enchantment.applicability.stream().map { it.displayName }.collect(Collectors.joining(", ")))
        } as List<String>

        FastItemUtils.setLore(enchantedBook, lore)

        return enchantedBook
    }

    ItemStack createMysteryBook(EnchantmentTier enchantmentTier) {
        ItemStack enchantedBook = new ItemStack(Material.BOOK)

        MysteryBookData mysteryBookData = new MysteryBookData(enchantmentTier)
        mysteryBookData.write(enchantedBook)

        FastItemUtils.setDisplayName(enchantedBook, enchantmentTier.tierColor + "§l" + enchantmentTier.tierName + " Enchantment Book §7(Right Click)")

        List<String> lore = [
                "§7Examine to receive a random",
                "§7$enchantmentTier.tierColor$enchantmentTier.tierName §7enchantment book."
        ]

        FastItemUtils.setLore(enchantedBook, lore)

        return enchantedBook
    }

    ItemStack createEnchantmentDust(EnchantmentTier enchantmentTier, int successIncrease = 1) {
        if (successIncrease < 1) successIncrease = 1
        if (successIncrease > 100) successIncrease = 100

        String name = items.getOrCreateConfig(enchantmentDustId).getStringEntry(EnchantConfigConst.enchantDustName.getId()).value

        List<String> lore = items.getOrCreateConfig(enchantmentDustId).getStringListEntry(EnchantConfigConst.enchantDustLore.getId()).value
        lore = lore.collect {
            it.replace("{percent}", successIncrease.toString())
                    .replace("{tierColor}", enchantmentTier.tierColor)
                    .replace("{tierName}", enchantmentTier.tierName)
        } as List<String>

        ItemStack enchantmentDust = FastItemUtils.createItem(
                items.getOrCreateConfig(enchantmentDustId).getMaterialEntry(EnchantConfigConst.enchantDustMaterial.getId()).value,
                name.replace("{tierColor}", enchantmentTier.tierColor).replace("{tierName}", enchantmentTier.tierName),
                lore
        )

        new EnchantmentDustData(enchantmentTier, successIncrease).write(enchantmentDust)

        return enchantmentDust
    }

    ItemStack createMysteryEnchantmentDust(EnchantmentTier enchantmentTier) {
        ItemStack mysteryDust = FastItemUtils.createItem(
                items.getOrCreateConfig(mysteryEnchantmentDustId).getMaterialEntry(EnchantConfigConst.mysteryEnchantDustMaterial.getId()).value,
                items.getOrCreateConfig(mysteryEnchantmentDustId).getStringEntry(EnchantConfigConst.mysteryEnchantDustName.getId()).value,
                items.getOrCreateConfig(mysteryEnchantmentDustId).getStringListEntry(EnchantConfigConst.mysteryEnchantDustLore.getId()).value
        )

        new MysteryEnchantmentDustData(enchantmentTier).write(mysteryDust)

        return mysteryDust
    }

    ItemStack createEnchantmentOrb(int successRate, int slotIncrease, EnchantmentOrbType enchantmentOrbType) {
        if (slotIncrease > (maxSlots - defSlots)) slotIncrease = maxSlots - defSlots

        def name = items.getOrCreateConfig(enchantmentOrbId).getStringEntry(EnchantConfigConst.enchantOrbName.getId()).value

        def lore = items.getOrCreateConfig(enchantmentOrbId).getStringListEntry(EnchantConfigConst.enchantOrbLore.getId()).value
        lore = lore.collect() {
            it.replace("{type}", enchantmentOrbType.label)
                    .replace("{maxSlots}", (defSlots + slotIncrease).toString())
                    .replace("{increaseSlots}", slotIncrease.toString())
                    .replace("{percent}", successRate.toString())
        } as List<String>

        ItemStack enchantmentOrb = FastItemUtils.createItem(
                items.getOrCreateConfig(enchantmentOrbId).getMaterialEntry(EnchantConfigConst.enchantOrbMaterial.getId()).value,
                name.replace("{type}", enchantmentOrbType.label).replace("{maxSlots}", (defSlots + slotIncrease).toString()),
                lore
        )

        new EnchantmentOrbData(successRate, slotIncrease, enchantmentOrbType).write(enchantmentOrb)
        return enchantmentOrb
    }

    ItemStack createWhiteScroll() {
        ItemStack whiteScroll = FastItemUtils.createItem(
                items.getOrCreateConfig(whiteScrollId).getMaterialEntry(EnchantConfigConst.whiteScrollMaterial.getId()).value,
                items.getOrCreateConfig(whiteScrollId).getStringEntry(EnchantConfigConst.whiteScrollName.getId()).value,
                items.getOrCreateConfig(whiteScrollId).getStringListEntry(EnchantConfigConst.whiteScrollLore.getId()).value
        )

        new WhiteScrollItemData(false).write(whiteScroll)
        return whiteScroll
    }

    ItemStack createHolyWhiteScroll(boolean antiDupe = false) {
        ItemStack whiteScroll = FastItemUtils.createItem(
                items.getOrCreateConfig(holyWhiteScrollId).getMaterialEntry(EnchantConfigConst.holyWhiteScrollMaterial.getId()).value,
                items.getOrCreateConfig(holyWhiteScrollId).getStringEntry(EnchantConfigConst.holyWhiteScrollName.getId()).value,
                items.getOrCreateConfig(holyWhiteScrollId).getStringListEntry(EnchantConfigConst.holyWhiteScrollLore.getId()).value
        )

        if (antiDupe) FastItemUtils.ensureUnique(whiteScroll)

        new WhiteScrollItemData(true).write(whiteScroll)
        return whiteScroll
    }

    ItemStack createBlackScroll(int newSuccessRate = 5) {
        if (newSuccessRate < 1) newSuccessRate = 1
        if (newSuccessRate > 100) newSuccessRate = 100

        List<String> lore = items.getOrCreateConfig(blackScrollId).getStringListEntry(EnchantConfigConst.blackScrollLore.getId()).value
        lore = lore.collect { it.replace("{percent}", newSuccessRate.toString()) } as List<String>

        ItemStack blackScroll = FastItemUtils.createItem(
                items.getOrCreateConfig(blackScrollId).getMaterialEntry(EnchantConfigConst.blackScrollMaterial.getId()).value,
                items.getOrCreateConfig(blackScrollId).getStringEntry(EnchantConfigConst.blackScrollName.getId()).value,
                lore
        )

        new BlackScrollData(newSuccessRate).write(blackScroll)

        return blackScroll
    }

    ItemStack createTransmogScroll() {
        def scroll = FastItemUtils.createItem(
                items.getOrCreateConfig(transmogScrollId).getMaterialEntry(EnchantConfigConst.transmogMaterial.getId()).value,
                items.getOrCreateConfig(transmogScrollId).getStringEntry(EnchantConfigConst.transmogName.getId()).value,
                items.getOrCreateConfig(transmogScrollId).getStringListEntry(EnchantConfigConst.transmogLore.getId()).value
        )

        new TransmogScrollData().write(scroll)

        return scroll
    }

    ItemStack createRandomSoulGenerator() {
        def soulGem = FastItemUtils.createItem(
                items.getOrCreateConfig(randomSoulGeneratorId).getMaterialEntry(EnchantConfigConst.randomSoulGenMaterial.getId()).value,
                items.getOrCreateConfig(randomSoulGeneratorId).getStringEntry(EnchantConfigConst.randomSoulGenName.getId()).value,
                items.getOrCreateConfig(randomSoulGeneratorId).getStringListEntry(EnchantConfigConst.randomSoulGenLore.getId()).value
        )

        new RandomSoulGenData().write(soulGem)

        return soulGem
    }

    ItemStack createSoulPearl() {
        def soulPearl = FastItemUtils.createItem(
            items.getOrCreateConfig(soulPearlId).getMaterialEntry(EnchantConfigConst.soulPearlMaterial.getId()).value,
            items.getOrCreateConfig(soulPearlId).getStringEntry(EnchantConfigConst.soulPearlName.getId()).value,
            items.getOrCreateConfig(soulPearlId).getStringListEntry(EnchantConfigConst.soulPearlLore.getId()).value
        )

        new SoulPearlData().write(soulPearl)

        return soulPearl
    }

    ItemStack createTimeMachine() {
        def timeMachine = FastItemUtils.createItem(
                items.getOrCreateConfig(timeMachineId).getMaterialEntry(EnchantConfigConst.timeMachineMaterial.getId()).value,
                items.getOrCreateConfig(timeMachineId).getStringEntry(EnchantConfigConst.timeMachineName.getId()).value,
                items.getOrCreateConfig(timeMachineId).getStringListEntry(EnchantConfigConst.timeMachineLore.getId()).value
        )

        new TimeMachineData().write(timeMachine)

        return timeMachine
    }
}



