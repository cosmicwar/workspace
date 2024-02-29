package scripts.factions.features.customset

import net.jodah.expiringmap.ExpiringMap
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players
import scripts.factions.content.clickitem.ClickItem
import scripts.factions.content.dbconfig.Config
import scripts.factions.content.dbconfig.ConfigCategory
import scripts.factions.content.dbconfig.DBConfigUtil
import scripts.factions.content.dbconfig.entries.IntEntry
import scripts.factions.content.dbconfig.entries.MaterialEntry
import scripts.factions.content.dbconfig.entries.StringEntry
import scripts.factions.content.dbconfig.entries.list.StringListEntry
import scripts.factions.content.dbconfig.utils.SelectionUtils
import scripts.factions.features.customset.data.CrystalExtractorItemData
import scripts.factions.features.customset.data.CrystalItemData
import scripts.factions.features.customset.data.CustomSetItemData
import scripts.factions.features.customset.struct.CustomSet
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.objects.Executable
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MenuDecorator

import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

class CustomSets {

    Map<String, CustomSet> registeredSets = new ConcurrentHashMap<>()
    Map<UUID, CustomSet> equippedSetsCache = new ConcurrentHashMap<>()

    static final Set<EquipmentSlot> ARMOR_SLOTS = [EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET] as Set<EquipmentSlot>

    Set<String> setScriptPaths = new HashSet<>()
    static Config config
    static ConfigCategory sets
    static ConfigCategory crystals

    CustomSets() {
        GroovyScript.addUnloadHook {
            Starlight.unload("~/CustomSetListener.groovy")

            Bukkit.getOnlinePlayers().each { player ->
                equippedSetsCache.get(player.getUniqueId())?.onUnequip(player)
            }

            registeredSets.clear()
            Starlight.unload(setScriptPaths as String[])
        }

        reloadConfig()
        exports()

        registerSets()
        reloadSets()
        setupClickItems()

        Starlight.watch("~/CustomSetListener.groovy")

        GroovyScript.addScriptHook(GroovyScript.HookType.RECOMPILE, {
            if (!GroovyScript.getCurrentScript().getWatchedScripts().contains(it)) return

            reloadConfig()
            reloadSets()
        })

        registerCommands()
        registerEvents()

        Bukkit.getOnlinePlayers().each { player ->
            hasEquippedSet(player)?.onEquip(player)
        }
    }

    void registerSets() {
        File enchantScriptsFolder = new File("${GroovyScript.getCurrentScript().getScript().getParent()}${File.separator}sets")
        Files.walk(enchantScriptsFolder.toPath(), 3).forEach({
            String path = it.toString()
            if (path.endsWith(".groovy")) {
                registerSet(path)
            }
        })
    }

    void registerSet(String enchantPath) {
        if (!setScriptPaths.add(enchantPath)) return

        Starlight.watch(enchantPath)
    }

    void exports() {
        Exports.ptr("customset:registerSet", { String setPath -> registerSet(setPath) })
        Exports.ptr("customset:getSet", { Player player -> getEquippedSet(player) })
        Exports.ptr("customset:getSetById", { String setId -> getSetById(setId) })
        Exports.ptr("customset:forEquippedSet", { Player player, Closure callback ->
            CustomSet set = getEquippedSet(player)
            if (set != null) {
                callback.call(set)
            }
        })
        Exports.ptr("customset:invalidateEquippedSet", { Player player -> invalidateEquippedSet(player) })
        Exports.ptr("customset:setEquippedSet", { Player player, CustomSet set -> setEquippedSet(player, set) })
    }

    void registerCommands() {

        SubCommandBuilder builder = SubCommandBuilder.of("sets", "customset", "armorset").defaultAction { player -> openSetsPreview(player) }

        builder.create("list").requirePermission("customset.*").register { command ->
            command.sender().sendMessage("§7Sets: ${registeredSets.keySet().join(", ")}")
        }.create("giveset").requirePermission("customset.*").usage("<player> <set>").register { command ->
            Player player = command.arg(0).parseOrFail(Player.class)
            if (!player.isOnline()) {
                command.reply("§cInvalid player")
                return
            }

            String type = command.arg(1).parseOrFail(String.class)
            CustomSet set = getSetById(type)
            if (set == null) {
                command.reply("§cInvalid set")
                return
            }

            giveCustomSet(player, set)
        }

        builder.create("crystals").requirePermission("customset.*").register {ctx ->
            openCrystalCreator(ctx.sender())
        }

        builder.create("crystalextractor").requirePermission("customset.*").register {ctx ->
            if (ctx.args().size() == 0) {
                def stack = createCrystalExtractor()
                crystalExtractor.giveStack(ctx.sender(), stack, "§a§l+ ${FastItemUtils.getDisplayName(stack)}")
                return
            }

            def success = ctx.arg(0).parseOrFail(Double.class)
            if (success < 0 || success > 100) {
                ctx.reply("§cInvalid success rate")
                return
            }

            def stack = createCrystalExtractor(success)
            crystalExtractor.giveStack(ctx.sender(), stack, "§a§l+ ${FastItemUtils.getDisplayName(stack)}")
        }

        builder.build()
    }

    void registerEvents() {
        Events.subscribe(PlayerQuitEvent.class).handler({
            invalidateEquippedSet(it.player)
        })

        Events.subscribe(PlayerDeathEvent.class).handler({
            invalidateEquippedSet(it.entity)
        })
    }

    static void reloadConfig() {
        config = DBConfigUtil.createConfig("customsets", "§d ~ sets ~", [], Material.NETHERITE_CHESTPLATE)
        sets = config.getOrCreateCategory("sets", "§d ~ sets ~", Material.NETHERITE_CHESTPLATE)

        crystals = config.getOrCreateCategory("crystals", "§d ~ crystals ~", Material.NETHERITE_CHESTPLATE)
        crystals.getOrCreateConfig("values").addDefault([
                new MaterialEntry("crystalMaterial", Material.NETHER_STAR),
                new IntEntry("crystalCustomData", 0),

                new StringListEntry("crystalLoreFormat", [
                        "§a%success%% Success Rate",
                        "§7Can be applied to any non",
                        "§7armor set that is not",
                        "§7already equipped with a",
                        "§7bonus crystal.",
                        "",
                        "§6§lCrystal Bonus:",
                        "%bonuses%",
                        ""
                ]),
                new StringListEntry("crystalExtractorLoreFormat", [
                        "§7Can be used to remove",
                        "§7a crystal from an armor set.",
                        "",
                        "§a%success%% Success Rate",
                        ""
                ]),

                new StringEntry("singleCrystalNameFormat", "§6§lArmor Crystal (%prefixes%§6§l)"),
                new StringEntry("multiCrystalNameFormat", "§6§lMulti Armor Set Crystal (%prefixes%§6§l)"),
                new StringEntry("crystalExtractorNameFormat", "§6§lCrystal Extractor"),
                new StringEntry("itemLoreFormat", "§6§lArmor Crystal (%prefixes%§6§l)"),
        ])
    }

    static Material getCrystalMaterial() {
        return crystals.getOrCreateConfig("values").getMaterialEntry("crystalMaterial").value
    }
    static int getCrystalCustomData() {
        return crystals.getOrCreateConfig("values").getIntEntry("crystalCustomData").value
    }
    static List<String> getCrystalLoreFormat() {
        return crystals.getOrCreateConfig("values").getStringListEntry("crystalLoreFormat").value
    }
    static String getSingleCrystalNameFormat() {
        return crystals.getOrCreateConfig("values").getStringEntry("singleCrystalNameFormat").value
    }
    static String getMultiCrystalNameFormat() {
        return crystals.getOrCreateConfig("values").getStringEntry("multiCrystalNameFormat").value
    }
    static String getCrystalExtractorNameFormat() {
        return crystals.getOrCreateConfig("values").getStringEntry("crystalExtractorNameFormat").value
    }
    static List<String> getCrystalExtractorLoreFormat() {
        return crystals.getOrCreateConfig("values").getStringListEntry("crystalExtractorLoreFormat").value
    }
    static String getCrystalItemLoreFormat() {
        return crystals.getOrCreateConfig("values").getStringEntry("itemLoreFormat").value
    }

    ClickItem armorCrystal
    ClickItem crystalExtractor

    def setupClickItems() {
        armorCrystal = new ClickItem("armorCrystal", null, { Player player, PlayerInteractEvent event, ClickItem item ->
            event.setCancelled(true)
        }, { Player player, InventoryClickEvent event, ClickItem item ->
            def currentItem = event.getCurrentItem()
            def cursor = event.getCursor()

            if (cursor == null || cursor.getType() == Material.AIR) return
            if (currentItem == null || currentItem.getType() == Material.AIR) return

            def cursorData = CrystalItemData.read(cursor)
            if (cursorData == null) return

            def setData = CustomSetItemData.read(currentItem)
            if (setData != null) {
                if (setData.setName != null && setData.setName != "omni") {
                    Players.msg(player, "§] §> §cThis item already has a set applied to it!")
                    return
                }

                if (setData.crystals) {
                    Players.msg(player, "§] §> §cThis item already has a crystal applied to it!")
                    return
                }
            }

            event.setCancelled(true)

            if (setData == null) setData = new CustomSetItemData()

            double success = cursorData.successRate

            if (cursor.amount >= 1) {
                event.setCursor(null)
            } else {
                cursor.setAmount(cursor.amount - 1)
            }

            def random = ThreadLocalRandom.current()
            if (success >= random.nextDouble(0, 100)) {
                setData.crystals = true
                setData.crystalSetNames = cursorData.getSetNames()

                setData.write(currentItem)

                updateCrystalData(currentItem)

                Players.playSound(player, Sound.ENTITY_PLAYER_LEVELUP)
                Players.msg(player, "§] §> §aThe crystal was successfully applied!")
            } else {
                Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
                Players.msg(player, "§] §> §cThe crystal failed to apply!")
            }
        })

        crystalExtractor = new ClickItem("crystalExtractor", null, { Player player, PlayerInteractEvent event, ClickItem item ->
            event.setCancelled(true)
        }, { Player player, InventoryClickEvent event, ClickItem item ->
            def currentItem = event.getCurrentItem()
            def cursor = event.getCursor()

            if (cursor == null || cursor.getType() == Material.AIR) return
            if (currentItem == null || currentItem.getType() == Material.AIR) return

            def extractorData = CrystalExtractorItemData.read(cursor)
            if (extractorData == null) return

            def setData = CustomSetItemData.read(currentItem)

            if (setData == null || !setData.crystals) {
                Players.msg(player, "§] §> §cThis item does not have a crystal applied to it!")
                return
            }

            event.setCancelled(true)
            if (cursor.amount == 1) {
                event.setCursor(null)
            } else {
                cursor.setAmount(cursor.amount - 1)
            }

            def random = ThreadLocalRandom.current()
            if (extractorData.success >= random.nextDouble(0, 100)) {
                def newCrystal = createCrystal(setData.getCrystalSets(), Math.floor(random.nextDouble(0, 100)))

                setData.crystals = false
                setData.crystalSetNames = []

                setData.write(currentItem)

                updateCrystalData(currentItem)
                armorCrystal.giveStack(player, newCrystal)

                Players.playSound(player, Sound.ENTITY_PLAYER_LEVELUP)
                Players.msg(player, "§] §> §aThe crystal was successfully removed!")
            } else {
                Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
                Players.msg(player, "§] §> §cThe crystal failed to remove!")
            }
        })
    }

    static ItemStack createCrystal(List<CustomSet> sets, double successRate = 100D) {
        sets = sets.findAll({it.canHaveCrystal})

        if (sets.size() == 0) {
            return null
        }

        def lore = []

        getCrystalLoreFormat().each {
            if (it.contains("%bonuses%")) {
                sets.each { set ->
                    lore.add(set.colorRemapper.apply("${set.getBoldDisplayName()}"))
                    set.getCrystalLore().each { bonus ->
                        lore.add(set.colorRemapper.apply(bonus))
                    }
                }
            } else {
                lore.add(it.replace("%success%", successRate.toString()))
            }
        }

        String name
        if (sets.size() > 1) {
            name = getMultiCrystalNameFormat().replace("%prefixes%", sets.collect { it.getSetPrefix() }.join("§7, "))
        } else {
            name = getSingleCrystalNameFormat().replace("%prefixes%", sets[0].getDisplayName())
        }

        def item = FastItemUtils.createItem(getCrystalMaterial(), name, lore, false)

        CrystalItemData data = new CrystalItemData(sets.collect { it.getInternalName() })

        if (successRate > 100) {
            successRate = 100.0D
        } else if (successRate < 0) {
            successRate = 0.0D
        }

        data.successRate = successRate
        data.write(item)

        return item
    }

    static ItemStack createCrystalExtractor(double successRate = 100D) {
        def lore = []

        getCrystalExtractorLoreFormat().each {
            lore.add(it.replace("%success%", successRate.toString()))
        }

        def item = FastItemUtils.createItem(Material.NETHER_STAR, getCrystalExtractorNameFormat(), lore, false)

        CrystalExtractorItemData data = new CrystalExtractorItemData()

        if (successRate > 100) {
            successRate = 100.0D
        } else if (successRate < 0) {
            successRate = 0.0D
        }

        data.success = successRate
        data.write(item)

        return item
    }

    static final String ARMOR_CRYSTAL_KEY = "§1§§§§"
    static final String ADDON_SPACER = "§8§§§§"

    static def updateCrystalData(ItemStack stack) {
        def data = CustomSetItemData.read(stack)
        if (data == null) return

        List<String> lore = FastItemUtils.getLore(stack) ?: new ArrayList<String>()
        List<String> addons = []

        def sets = data.getCrystalSets()

        def crystalIndex = lore.findIndexOf { it.endsWith(ARMOR_CRYSTAL_KEY) }
        if (crystalIndex >= 0) {
            lore.remove(crystalIndex)
        }

        if (sets.isEmpty() || !data.crystals) {
            FastItemUtils.setLore(stack, lore)
            return
        }

        def addonIndex = lore.findIndexOf { it.endsWith(ADDON_SPACER) }
        if (addonIndex == -1) {
            addons.add(ADDON_SPACER)
        }

        def text = getCrystalItemLoreFormat().replace("%prefixes%", sets.collect { it.getBoldDisplayName() }.join("§7, "))
        addons.add(text + ARMOR_CRYSTAL_KEY)

        if (!addons.isEmpty()) {
            lore.addAll(addons)
        }

        FastItemUtils.setLore(stack, lore)
    }

    void reloadSets() {
        equippedSetsCache.clear()

        GroovyScript.getCurrentScript().getWatchedScripts().findAll { CustomSet.class.isAssignableFrom(it.getScriptClazz()) }.each {
            try {
                CustomSet customSet = it.getScriptClazz().getDeclaredConstructor().newInstance() as CustomSet

                CustomSet existingSet = registeredSets.get(customSet.getInternalName())
                if (existingSet != null && existingSet.getClass().isAssignableFrom(customSet.getClass())) {
                    return
                }

                registeredSets.put(customSet.getInternalName(), customSet)
                if (existingSet) {
                    Starlight.log.info("Reloaded set: $customSet.displayName")
                } else {
                    Starlight.log.info("Loaded set: $customSet.displayName")
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    static void giveCustomSet(Player player, CustomSet set) {
        if (set == null) return

        getArmour(set).each { item ->
            FastInventoryUtils.addOrBox(player.getUniqueId(), player, Bukkit.getConsoleSender(), item, null)
        }

        if (set.weapon != null) {
            def weapon = set.weapon.getWeapon()
            if (weapon != null) {
                FastInventoryUtils.addOrBox(player.getUniqueId(), player, Bukkit.getConsoleSender(), weapon, null)
            }
        }
    }

    static List<ItemStack> getArmour(CustomSet set) {
        if (set == null) return null

        List<ItemStack> armor = new ArrayList<>()
        armor.add(set.getHelmet())
        armor.add(set.getChestPlate())
        armor.add(set.getLeggings())
        armor.add(set.getBoots())

        return armor
    }

    CustomSet getSetById(String id) {
        return registeredSets.get(id)
    }

    void invalidateEquippedSet(Player player) {
        equippedSetsCache.remove(player.uniqueId)
    }

    void setEquippedSet(Player player, CustomSet set) {
        equippedSetsCache.remove(player.getUniqueId())
        if (set != null) equippedSetsCache.put(player.uniqueId, set)
    }

    CustomSet getEquippedSet(Player player) {
        return equippedSetsCache.get(player.uniqueId)
    }

    CustomSet hasEquippedSet(Player player) {
        def nonOmni = Arrays.stream(player.getEquipment().getArmorContents()).map(CustomSetItemData::read).filter(Objects::nonNull).filter(data -> data.getSetName() != "omni" && getSetById(data.getSetName()) != null).findFirst().orElse(null)
        if (nonOmni == null) return null

        CustomSet set = getSetById(nonOmni.getSetName())

        List<String> equippedPieces = Arrays.stream(player.getEquipment().getArmorContents()).map(CustomSetItemData::read).filter(Objects::nonNull).map(CustomSetItemData::getSetName).collect(Collectors.toList())
        // list of all setnames
        if (equippedPieces.size() != 4 || !equippedPieces.stream().allMatch(equippedPiece -> (equippedPiece == nonOmni.getSetName() || equippedPiece == "omni") && equippedPiece != "none")) {
            return null
        }

        setEquippedSet(player, set)

        return set
    }

    void openSetsPreview(Player player) {
        MenuBuilder builder = new MenuBuilder(54, "§3§lCustom Sets")
        MenuDecorator.decorate(builder, [
                "3f3f3f3f3",
                "f3f3f3f3f",
                "3f3f3f3f3",
                "f3f3f3f3f",
                "3f3f3f3f3",
                "f3f3f3f3f",
        ])

        int startIndex = 11
        List<CustomSet> sets = registeredSets.values().stream().collect(Collectors.toList())
        for (int i = 0; i < sets.size(); i++) {
            CustomSet set = sets.get(i)
            int slotIndex = startIndex
            for (ItemStack item : getArmour(set)) {
                builder.set(slotIndex, item, { p, t, s ->
                    {
                        if (!p.isOp()) return
                        p.getInventory().addItem(builder.get().getItem(s))
                    }
                })
                slotIndex += 9
            }
            if (set.weapon != null) {
                def weapon = set.weapon.getWeapon()
                if (weapon != null) {
                    builder.set(slotIndex, weapon, { p, t, s ->
                        {
                            if (!p.isOp()) return
                            p.getInventory().addItem(weapon)
                        }
                    })
                }
            }
            startIndex += 1
        }

        MenuUtils.syncOpen(player, builder)
    }

    Map<UUID, Tuple2<Double, List<CustomSet>>> selectedCrystalSets = ExpiringMap.builder().expiration(5, TimeUnit.MINUTES).build()

    def openCrystalCreator(Player player) {
        MenuBuilder menu = new MenuBuilder(36, "§3§lCrystal Creator")

        MenuDecorator.decorate(menu, [
                "3f3f3f3f3",
                "f3f3f3f3f",
                "3f3f3f3f3",
                "f3f3f3f3f",
        ])

        def data = selectedCrystalSets.computeIfAbsent(player.uniqueId, { new Tuple2<Double, List<CustomSet>>(100D, []) })
        def sets = data.getV2()
        def success = data.getV1()

        menu.set(3, 4, createCrystal(sets, success) ?: FastItemUtils.createItem(Material.NETHER_STAR, "§cEmpty", []), { p, t, s ->
            {
                def item = menu.get().getItem(s)
                if (item == null || item.type.isAir()) return
                if (!p.isOp()) return

                armorCrystal.giveStack(p, createCrystal(sets, success))
            }
        })

        menu.set(3, 6, FastItemUtils.createItem(Material.SUGAR, "§a$success%", ["§7Click to Change"]), { p, t, s ->
            {
                if (!p.isOp()) return

                SelectionUtils.selectDouble(p, "§3§lCrystal Success Rate", [5,10,15,20,25,50,75,100],{ newValue ->
                    {
                        selectedCrystalSets.remove(p.uniqueId)
                        selectedCrystalSets.put(p.uniqueId, new Tuple2<Double, List<CustomSet>>(newValue, sets))
                        openCrystalCreator(p)
                    }
                })
            }
        })

        registeredSets.values().findAll{it.canHaveCrystal }.withIndex().each {
            def set = it.getV1()
            def index = it.getV2()

            menu.set(index, createCrystal([set]), { p, t, s ->
                {
                    if (!p.isOp()) return

                    if (sets.contains(set)) {
                        selectedCrystalSets.remove(p.uniqueId)
                        selectedCrystalSets.put(p.uniqueId, new Tuple2<Double, List<CustomSet>>(success, sets - [set]))
                    } else {
                        selectedCrystalSets.remove(p.uniqueId)
                        selectedCrystalSets.put(p.uniqueId, new Tuple2<Double, List<CustomSet>>(success, sets + [set]))
                    }

                    openCrystalCreator(p)
                }
            })
        }

        menu.openSync(player)
    }

}

