package scripts.factions.features.pack.itemskins

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.Pair
import com.google.common.collect.Lists
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.protocol.Protocol
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import scripts.factions.features.pack.itemskins.data.PacketSkinData
import scripts.factions.features.pack.itemskins.data.SkinnedItemData
import scripts.factions.features.pack.itemskins.data.SkinOrbData
import scripts.factions.features.pack.itemskins.utils.ItemSkinType
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.PacketUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.ItemType
import scripts.shared.utils.ItemUtils
import scripts.shared.utils.Temple

import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

// TODO: redo null checks
// TODO: fix pack
// TODO: fix skin preview
// TODO: fix removing custommodeldata
// TODO: maybe redo the way we store the data, maybe in a list :C
class ItemSkins {
    Map<String, ItemSkin> registeredSkins = new ConcurrentHashMap<>()
    Map<UUID, Map<ItemStack, ItemSkin>> equippedSkinsCache = new ConcurrentHashMap<>()

    Set<String> skinScriptPaths = new HashSet<>()

    static final Set<EquipmentSlot> ARMOR_SLOTS = [EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET] as Set<EquipmentSlot>

    Map<String, ?> config

    ItemSkins() {
        GroovyScript.addUnloadHook {
            Starlight.unload("~/ItemSkinListener.groovy")

            Bukkit.getOnlinePlayers().each { player ->
                forEquippedSkins(player, { ItemStack itemStack, ItemSkin itemSkin ->
                    itemSkin?.onUnequip(player, itemStack)
                })
            }

            registeredSkins.clear()
            Starlight.unload(skinScriptPaths as String[])
        }

        exports()
        reloadConfig()

        Starlight.watch("~/ItemSkinListener.groovy")

        registerSkins()
        reloadSkins()

        GroovyScript.addScriptHook(GroovyScript.HookType.RECOMPILE, {
            if (!GroovyScript.getCurrentScript().getWatchedScripts().contains(it)) return

            reloadConfig()
            reloadSkins()
        })

        registerCommands()
        registerEvents()
        addPackets()

        Bukkit.getOnlinePlayers().each { player ->
            forEquippedSkins(player, { ItemStack itemStack, ItemSkin itemSkin ->
                itemSkin?.onEquip(player, itemStack)
            })
        }
    }

    void reloadConfig() {
        Starlight.watch("scripts/exec/$Temple.templeId/skinconfig.groovy")
        config = Exports.ptr("skinconfig") as Map<String, ?>
    }

    void reloadSkins() {
        equippedSkinsCache.clear()

        Set<String> enabledSkins = (config["skins"] as Map<String, ?>).keySet()
        Starlight.log.info("ENABLED SKINS: " + enabledSkins.toString())
        GroovyScript.getCurrentScript().getWatchedScripts().findAll { ItemSkin.class.isAssignableFrom(it.getScriptClazz()) }.each {
            try {
                ItemSkin itemSkin = it.getScriptClazz().getDeclaredConstructor().newInstance() as ItemSkin
                if (!enabledSkins.contains(itemSkin.getInternalName())) {
                    Starlight.log.info("Unable to load skin \"${itemSkin.getInternalName()}\" due to missing config section")
                    return
                }

                ItemSkin existingSkin = registeredSkins.get(itemSkin.getInternalName())
                existingSkin?.reload()
                if (existingSkin != null && existingSkin.getClass().isAssignableFrom(itemSkin.getClass())) {
                    return
                }

                itemSkin.reload()
                registeredSkins.put(itemSkin.getInternalName(), itemSkin)
                if (existingSkin) {
                    Starlight.log.info("Reloaded skin: $itemSkin.displayName")
                } else {
                    Starlight.log.info("Loaded skin: $itemSkin.displayName")
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    void registerSkins() {
        File skinScriptsFolder = new File("${GroovyScript.getCurrentScript().getScript().getParent()}${File.separator}skins")
        Files.walk(skinScriptsFolder.toPath(), 3).forEach({
            String path = it.toString()
            if (path.endsWith(".groovy")) {
                registerSkin(path)
            }
        })
    }

    void registerSkin(String skinPath) {
        if (!skinScriptPaths.add(skinPath)) return

        Starlight.watch(skinPath)
    }

    void exports() {
        Exports.ptr("itemskins:getSkinById", { String skin -> getSkinById(skin) })
        Exports.ptr("itemskins:getItemSkin", { ItemStack stack -> getItemSkin(stack) })
        Exports.ptr("itemskins:createItemSkin", { ItemSkin skin -> createItemSkin(skin) })
        Exports.ptr("itemskins:addSkinToItem", { ItemStack stack, ItemSkin skin -> addSkinToItem(stack, skin) })
        Exports.ptr("itemskins:removeSkinFromItem", { ItemStack stack -> removeSkinFromItem(stack) })
        Exports.ptr("itemskins:hasItemSkin", { ItemStack stack -> hasItemSkin(stack) })
        Exports.ptr("itemskins:forEquippedSkins", { Player player, Closure<ItemSkin> closure -> forEquippedSkins(player, closure) })
        Exports.ptr("itemskins:invalidateEquippedSkins", { Player player -> invalidateEquippedSkins(player) })
    }

    void registerCommands() {
        SubCommandBuilder builder = SubCommandBuilder.of("skins", "itemskin", "itemskins", "skin").defaultAction { player -> openSkinPreview(player) }

        builder.create("giveskin").requirePermission("skins.*").usage("<player> <skin>").register { c ->
            Player player = c.arg(0).parse(Player.class).get()
            if (player == null) {
                c.reply("§cInvalid player")
                return
            }

            ItemSkin skin = getSkinById(c.arg(1).parse(String.class).get())
            if (skin == null) {
                c.reply("§cInvalid skin")
                return
            }

            ItemStack itemStack = createItemSkin(skin)
            player.getInventory().addItem(itemStack)
        }.build()
    }

    void registerEvents() {
        Events.subscribe(PlayerQuitEvent.class).handler({
            invalidateEquippedSkins(it.player)
        })

        Events.subscribe(PlayerDeathEvent.class).handler({
            invalidateEquippedSkins(it.entity)
        })
    }

    static final List<Integer> INVENTORY_SLOTS_FIRST_EMPTY = [[9..39], [0..8]].flatten() as List<Integer>
    void addPackets() {
        Events.subscribe(InventoryClickEvent.class, EventPriority.LOWEST).filter(EventFilters.ignoreCancelled()).handler({
            if (it.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY || it.getClickedInventory().getType() != InventoryType.PLAYER) return

            ItemStack itemStack = it.getCurrentItem()
            ItemType itemType = ItemType.getTypeOf(itemStack)
            if (itemType == null || !itemType.isArmor()) return

            if (itemStack.toString().endsWith("HELMET")) return

            SkinnedItemData skinnedItemData = SkinnedItemData.read(itemStack)
            if (skinnedItemData == null) return

            Player player = (Player) it.getWhoClicked()
            int slotMovedTo = -1
            if (it.getSlotType() == InventoryType.SlotType.ARMOR) {
                int empty = INVENTORY_SLOTS_FIRST_EMPTY.find {player.getInventory().getItem(it) == null } ?: -1
                if (empty >= 0) {
                    slotMovedTo = empty > 8 ? empty : empty + 36
                }
            } else {
                switch (itemType) {
                    case ItemType.HELMET:
                        slotMovedTo = 5
                        break
                    case ItemType.CHESTPLATE:
                        slotMovedTo = 6
                        break
                    case ItemType.LEGGINGS:
                        slotMovedTo = 7
                        break
                    case ItemType.BOOTS:
                        slotMovedTo = 8
                        break
                }
            }
            if (slotMovedTo < 0) return

            PacketUtils.send(player, new ClientboundContainerSetSlotPacket(0, 0, slotMovedTo, CraftItemStack.asNMSCopy(itemStack)))
        })

        Protocol.subscribe(PacketType.Play.Server.WINDOW_ITEMS, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.ENTITY_EQUIPMENT).handler({ event ->
            if (event.isPlayerTemporary() || !shouldRewriteModels(event.player)) return

            Player player = event.player
            Inventory topInventory = player.getOpenInventory().getTopInventory()
            PacketContainer packet = event.packet

            switch (packet.type) {
                case PacketType.Play.Server.SET_SLOT:
                    if (player.getGameMode() != GameMode.SURVIVAL) break
                    int slot = packet.getIntegers().read(2)

                    ItemStack itemStack = packet.getItemModifier().read(0)
                    if (itemStack == null || !itemStack.type.name().endsWith("HELMET")) break

                    SkinnedItemData data = SkinnedItemData.read(itemStack)
                    if (data == null) break

                    ItemSkin skin = data.getItemSkin()
                    if (skin == null) break

                    if (slot == 5) {
                        packet.getItemModifier().write(0, buildHelmetOverlay(itemStack, data.getItemSkin()))
                    } else {
                        packet.getItemModifier().write(0, itemStack)
                    }

                    break
                case PacketType.Play.Server.ENTITY_EQUIPMENT:
                    Entity entity = packet.getEntityModifier(event).read(0)
                    if (!(entity instanceof Player)) break

                    boolean modified = false
                    List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentPairs = packet.getSlotStackPairLists().read(0)
                    equipmentPairs.each {
                        if (it.getFirst() != EnumWrappers.ItemSlot.HEAD) return

                        SkinnedItemData data = SkinnedItemData.read(it.getSecond())
                        if (data == null) return

                        ItemSkin skin = data.getItemSkin()
                        if (skin == null) return

                        ItemStack modifiedItem = buildHelmetOverlay(it.getSecond(), data.getItemSkin())

                        if (modifiedItem == null) return

                        it.setSecond(modifiedItem)
                        modified = true
                    }

                    if (modified) {
                        packet.getSlotStackPairLists().write(0, equipmentPairs)
                    }
                    break
                case PacketType.Play.Server.WINDOW_ITEMS:
                    if (player.getGameMode() != GameMode.SURVIVAL) break

                    List<ItemStack> windowItems = packet.itemListModifier.read(0)
                    boolean modified = false
                    for (int slot = 0; slot < windowItems.size(); slot++) {
                        if (slot < 0 || slot >= topInventory.getSize()) {
                            if (topInventory.getType() == InventoryType.CRAFTING && slot == 5) {
                                ItemStack itemStack = windowItems.get(slot)
                                if (itemStack == null) continue

                                SkinnedItemData data = SkinnedItemData.read(itemStack)
                                if (data == null) continue

                                ItemSkin skin = data.getItemSkin()
                                if (skin == null) continue

                                ItemStack modifiedItem = buildHelmetOverlay(itemStack, data.getItemSkin())

                                modified = true
                                windowItems.set(slot, modifiedItem)
                            }
                        }
                    }

                    if (modified) packet.getItemListModifier().write(0, windowItems)

                    break
            }
        })
    }

    static ItemStack buildHelmetOverlay(ItemStack originalStack, ItemSkin skin) {
        SkinnedItemData data = SkinnedItemData.read(originalStack)
        if (data == null) return null

        ItemStack stack = FastItemUtils.createItem(Material.NETHERITE_PICKAXE, "§r" + FastItemUtils.getDisplayName(originalStack), FastItemUtils.getLore(originalStack), false)
        FastItemUtils.setCustomModelData(stack, skin.getArmorEquippedId())

        originalStack.enchantments?.each { enchantment, level ->
            stack.addUnsafeEnchantment(enchantment, level)
        }

        new PacketSkinData(ItemUtils.convertItemStackToString(originalStack)).write(stack)

        return stack
    }


    static boolean shouldRewriteModels(Player player) {
        return player.hasResourcePack()
    }

    void forEquippedSkins(Player player, Closure<ItemSkin> closure) {
        getEquippedSkins(player).each {
            closure.call(it.key, it.value)
        }
    }

    ItemSkin getActiveSkinOfType(UUID uuid, ItemStack itemStack) {
        return equippedSkinsCache.get(uuid)?.get(itemStack)
    }

    ItemSkin getItemSkin(ItemStack itemStack) {
        SkinnedItemData itemSkinData = SkinnedItemData.read(itemStack)
        if (itemSkinData == null) return null

        return itemSkinData.getItemSkin()
    }

    Boolean hasItemSkin(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return false

        SkinnedItemData itemSkinData = SkinnedItemData.read(stack)
        if (itemSkinData == null) return false

        return itemSkinData.hasItemSkin()
    }

    ItemSkin getSkinById(String internalName) {
        if (internalName == null) return null
        return registeredSkins.get(internalName) ?: null
    }

    void invalidateEquippedSkins(Player player) {
        equippedSkinsCache.remove(player.uniqueId)
    }

    Map<ItemStack, ItemSkin> getEquippedSkins(Player player) {
        return equippedSkinsCache.computeIfAbsent(player.uniqueId, v -> {
            Map<ItemStack, ItemSkin> equipped = new HashMap<>()
            ItemStack mainHand = player.getEquipment().getItemInMainHand()
            ItemSkinType itemType = ItemSkinType.getTypeOf(mainHand)
            if (itemType?.isHoldable()) {
                if (getItemSkin(mainHand) != null)
                    equipped.put(mainHand, getItemSkin(mainHand))
            }

            ARMOR_SLOTS.each {
                ItemStack itemStack = player.getEquipment().getItem(it)
                if (itemStack != null) {
                    if (getItemSkin(itemStack) != null)
                        equipped.put(itemStack, getItemSkin(itemStack))
                }
            }

            return equipped
        })
    }

    ItemStack createItemSkin(ItemSkin skin) {
        ItemStack orb = new ItemStack(getConfig()["item"]["material"] as Material)

        SkinOrbData skinOrbData = new SkinOrbData(skin.getInternalName(), skin.getApplicability())
        skinOrbData.write(orb)

        FastItemUtils.setDisplayName(orb, (getConfig()["item"]["displayName"] as String).replace("{displayName}", skin.getDisplayName()))

        List<String> lore = getConfig()["item"]["lore"] as List<String>
        lore = lore.collect {
            it.replace("{itemType}", skin.getApplicability().getDisplayName().toUpperCase())
        }

        FastItemUtils.setLore(orb, lore)

        return orb
    }

    void removeSkinFromItem(ItemStack stack) {
        SkinnedItemData itemSkinData = SkinnedItemData.read(stack)
        if (itemSkinData == null) return

        itemSkinData.itemSkin = null
        itemSkinData.write(stack)

        List<String> lore = FastItemUtils.getLore(stack) ?: new ArrayList<String>()
        int loreIndex = lore.findIndexOf { it.contains(getConfig()["loreKey"] as String) }

        if (loreIndex >= 0) {
            lore.remove(loreIndex)
            FastItemUtils.setLore(stack, lore)
        }

        FastItemUtils.setCustomModelData(stack, 1)
    }

    void addSkinToItem(ItemStack itemStack, ItemSkin itemSkin) {
        SkinnedItemData itemSkinData = SkinnedItemData.read(itemStack)
        if (itemSkinData == null) {
            itemSkinData = new SkinnedItemData()
        }

        itemSkinData.itemSkin = itemSkin.getInternalName()
        updateItemStack(itemStack, itemSkinData)
    }

    void updateItemStack(ItemStack stack, SkinnedItemData itemSkinData) {
        itemSkinData.write(stack)

        ItemSkin skin = itemSkinData.getItemSkin()
        if (skin == null) return // shouldn't happen but just in case

        String skinLore = getConfig()["lore"] as String
        skinLore = skinLore.replace("{displayName}", skin.getDisplayName())

        List<String> lore = FastItemUtils.getLore(stack) ?: new ArrayList<String>()
        int loreIndex = lore.findIndexOf { it.contains(getConfig()["loreKey"] as String) }

        if (loreIndex >= 0)
            lore.set(loreIndex, skinLore)
        else
            lore.add(skinLore)

        FastItemUtils.setLore(stack, lore)
        FastItemUtils.setCustomModelData(stack, skin.getModelId())
    }

    void openSkinPreview(Player player) {
        MenuBuilder builder = new MenuBuilder(36, "§7Select a Material")

        (config["gui"]["materialSelector"]["slots"] as Map<String, Integer>).each { entry ->
            ItemSkinType type = ItemSkinType.valueOf(entry.getKey())
            ItemStack stack = FastItemUtils.createItem(type.getIcon(),
                    "§3${type.displayName} §7Enchants",
                    ["",
                     "§fClick me to view all of the",
                     "§3${type.displayName} §fenchants.",
                     ""],
                    false
            )
            builder.set(entry.getValue(), stack, { p, t, s ->
                showSkinMaterial(player, type)
            })
        }

        builder.open(player)
    }

    void showSkinMaterial(Player player, ItemSkinType type, int page = 1) {
        List<ItemSkin> skins = Optional.ofNullable(getRegisteredSkins().values().stream().filter { it.applicability == type }.collect(Collectors.toList())).orElse(Lists.newArrayList())

        MenuBuilder builder
        builder = MenuUtils.createPagedMenu("§3${type.displayName} §7Skins (${skins.size()})", skins, { ItemSkin skin, Integer i ->
            if (skin != null) {
                List<String> lore = [
                        "§r"
                ]

                skin.description.forEach { string -> lore.add("§f$string") }

                lore.addAll(
                        [
                                "§r",
                                "§7§l${type.displayName} §7Skin",
                                "§r",
                        ])

                if (player.isOp()) {
                    lore.addAll(
                            [
                                    "§7Left-Click to give",
                                    "§7Right-Click to give on piece"
                            ]
                    )
                }

                ItemStack stack = FastItemUtils.createItem(type.icon, "${skin.displayName}", lore)

                SkinnedItemData itemSkinData = SkinnedItemData.read(stack)
                if (itemSkinData == null) {
                    itemSkinData = new SkinnedItemData()
                }

                itemSkinData.itemSkin = skin.getInternalName()
                itemSkinData.write(stack)

                FastItemUtils.setCustomModelData(stack, skin.getModelId())
                return stack
            }
            return null
        }, page, true, [
                { Player p, ClickType t, int s ->
                    if (!p.isOp()) return
                    ItemStack stack = builder.get().getItem(s)

                    if (hasItemSkin(stack)) {
                        ItemSkin skin = getItemSkin(stack)
                        if (t == ClickType.LEFT) {
                            p.getInventory().addItem(createItemSkin(skin))
                        } else if (t == ClickType.RIGHT) {
                            ItemStack item = new ItemStack(skin.getApplicability().icon)
                            addSkinToItem(item, skin)
                            p.getInventory().addItem(item)
                        }
                    }
                },
                { p, t, s -> showSkinMaterial(p, type, page + 1) },
                { p, t, s -> showSkinMaterial(p, type, page - 1) },
                { p, t, s -> openSkinPreview(p) }
        ])
        builder.openSync(player)
    }

    static boolean isArmorSlot(EnumWrappers.ItemSlot itemSlot) {
        switch (itemSlot) {
            case EnumWrappers.ItemSlot.FEET:
            case EnumWrappers.ItemSlot.LEGS:
            case EnumWrappers.ItemSlot.CHEST:
            case EnumWrappers.ItemSlot.HEAD:
                return true
            default:
                return false
        }
    }
}

