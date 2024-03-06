package scripts.factions.features.itemfilter

import com.google.gson.Gson
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import scripts.shared.data.uuid.UUIDDataManager
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.SignUtils
import scripts.shared.legacy.utils.StringUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.BooleanCallback
import scripts.shared.utils.MenuDecorator

class ItemFilter {

    static List<FilterCategory> FILTER_CATEGORIES = [
            new FilterCategory("equipment", Material.NETHERITE_CHESTPLATE, [
                    Material.NETHERITE_HELMET,
                    Material.NETHERITE_CHESTPLATE,
                    Material.NETHERITE_LEGGINGS,
                    Material.NETHERITE_BOOTS,
                    Material.DIAMOND_HELMET,
                    Material.DIAMOND_CHESTPLATE,
                    Material.DIAMOND_LEGGINGS,
                    Material.DIAMOND_BOOTS,
                    Material.IRON_HELMET,
                    Material.IRON_CHESTPLATE,
                    Material.IRON_LEGGINGS,
                    Material.IRON_BOOTS,
                    Material.GOLDEN_HELMET,
                    Material.GOLDEN_CHESTPLATE,
                    Material.GOLDEN_LEGGINGS,
                    Material.GOLDEN_BOOTS,
                    Material.CHAINMAIL_HELMET,
                    Material.CHAINMAIL_CHESTPLATE,
                    Material.CHAINMAIL_LEGGINGS,
                    Material.CHAINMAIL_BOOTS,
                    Material.LEATHER_HELMET,
                    Material.LEATHER_CHESTPLATE,
                    Material.LEATHER_LEGGINGS,
                    Material.LEATHER_BOOTS,
                    Material.SHIELD,
                    Material.TRIDENT,
                    Material.BOW,
                    Material.CROSSBOW,
                    Material.FISHING_ROD,
                    Material.SHEARS,
                    Material.FLINT_AND_STEEL,
                    Material.ELYTRA,
                    Material.NETHERITE_SWORD,
                    Material.NETHERITE_AXE,
                    Material.NETHERITE_PICKAXE,
                    Material.NETHERITE_SHOVEL,
                    Material.NETHERITE_HOE,
                    Material.DIAMOND_SWORD,
                    Material.DIAMOND_AXE,
                    Material.DIAMOND_PICKAXE,
                    Material.DIAMOND_SHOVEL,
                    Material.DIAMOND_HOE,
                    Material.IRON_SWORD,
                    Material.IRON_AXE,
                    Material.IRON_PICKAXE,
                    Material.IRON_SHOVEL,
                    Material.IRON_HOE,
                    Material.GOLDEN_SWORD,
                    Material.GOLDEN_AXE,
                    Material.GOLDEN_PICKAXE,
                    Material.GOLDEN_SHOVEL,
                    Material.GOLDEN_HOE,
                    Material.STONE_SWORD,
                    Material.STONE_AXE,
                    Material.STONE_PICKAXE,
                    Material.STONE_SHOVEL,
                    Material.STONE_HOE,
                    Material.WOODEN_SWORD,
                    Material.WOODEN_AXE,
                    Material.WOODEN_PICKAXE,
                    Material.WOODEN_SHOVEL,
                    Material.WOODEN_HOE,
            ]),
            new FilterCategory("potion", Material.POTION, [
                    Material.POTION,
                    Material.SPLASH_POTION,
                    Material.LINGERING_POTION,
                    Material.TIPPED_ARROW,
            ]),
            new FilterCategory("redstone", Material.REDSTONE, [
                    Material.REDSTONE,
                    Material.REDSTONE_TORCH,
                    Material.REDSTONE_BLOCK,
                    Material.REDSTONE_LAMP,
                    Material.REPEATER,
                    Material.COMPARATOR,
                    Material.DAYLIGHT_DETECTOR,
                    Material.OBSERVER,
                    Material.PISTON,
                    Material.STICKY_PISTON,
                    Material.DISPENSER,
                    Material.DROPPER,
                    Material.HOPPER,
                    Material.LEVER,
                    Material.STONE_BUTTON,
                    Material.STONE_PRESSURE_PLATE,
                    Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
                    Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
                    Material.TRIPWIRE_HOOK,
                    Material.TNT,
                    Material.WATER_BUCKET,
                    Material.LAVA_BUCKET,
                    Material.BUCKET,
                    Material.FLINT_AND_STEEL,
                    Material.FIRE_CHARGE,
                    Material.POWERED_RAIL,
                    Material.DETECTOR_RAIL,
                    Material.ACTIVATOR_RAIL,
                    Material.RAIL,
                    Material.BELL,
                    Material.JUKEBOX,
                    Material.NOTE_BLOCK,
                    Material.BEACON,
                    Material.ENDER_CHEST,
            ]),
            new FilterCategory("food", Material.COOKED_BEEF, [
                    Material.ENCHANTED_GOLDEN_APPLE,
                    Material.GOLDEN_APPLE,
                    Material.COOKED_BEEF,
                    Material.COOKED_CHICKEN,
                    Material.COOKED_COD,
                    Material.COOKED_MUTTON,
                    Material.COOKED_PORKCHOP,
                    Material.COOKED_RABBIT,
                    Material.COOKED_SALMON,
                    Material.BREAD,
                    Material.CAKE,
                    Material.COOKIE,
                    Material.MUSHROOM_STEW,
                    Material.BEETROOT_SOUP,
                    Material.PUMPKIN_PIE,
                    Material.RABBIT_STEW,
                    Material.SUSPICIOUS_STEW,
                    Material.MELON_SLICE,
                    Material.APPLE,
                    Material.BAKED_POTATO,
                    Material.BEETROOT,
                    Material.CARROT,
                    Material.CHORUS_FRUIT,
                    Material.DRIED_KELP,
                    Material.GOLDEN_CARROT,
                    Material.HONEY_BOTTLE,
                    Material.POTATO,
                    Material.POISONOUS_POTATO,
                    Material.PUFFERFISH,
                    Material.PUMPKIN_PIE,
                    Material.RABBIT,
            ]),
            new FilterCategory("speciality", Material.SPAWNER, [
                    Material.SPAWNER,
                    Material.SADDLE,
                    Material.NAME_TAG,
                    Material.PAPER,
                    Material.BOOK,
                    Material.ELYTRA,
                    Material.TOTEM_OF_UNDYING,
                    Material.ENCHANTED_BOOK,
                    Material.ENCHANTING_TABLE,
                    Material.BOOK,
                    Material.DRAGON_EGG,
                    Material.END_CRYSTAL,
                    Material.END_PORTAL_FRAME,
            ]),
            new FilterCategory("ores", Material.NETHERITE_INGOT, [
                    Material.NETHERITE_INGOT,
                    Material.NETHERITE_SCRAP,
                    Material.DIAMOND,
                    Material.EMERALD,
                    Material.IRON_INGOT,
                    Material.GOLD_INGOT,
                    Material.COAL,
                    Material.CHARCOAL,
                    Material.LAPIS_LAZULI,
                    Material.REDSTONE,
                    Material.QUARTZ,
                    Material.NETHERITE_BLOCK,
                    Material.DIAMOND_BLOCK,
                    Material.EMERALD_BLOCK,
                    Material.IRON_BLOCK,
                    Material.GOLD_BLOCK,
                    Material.COAL_BLOCK,
                    Material.LAPIS_BLOCK,
                    Material.REDSTONE_BLOCK,
                    Material.QUARTZ_BLOCK,
                    Material.DIAMOND_ORE,
                    Material.EMERALD_ORE,
                    Material.IRON_ORE,
                    Material.GOLD_ORE,
                    Material.COAL_ORE,
                    Material.LAPIS_ORE,
                    Material.REDSTONE_ORE,

            ]),
            new FilterCategory("other blocks", Material.SAND, [
                    Material.SAND,
                    Material.RED_SAND,
                    Material.GRAVEL,
                    Material.CLAY,
                    Material.SNOWBALL,
                    Material.SNOW_BLOCK,
                    Material.ICE,
                    Material.PACKED_ICE,
                    Material.BLUE_ICE,
                    Material.SLIME_BLOCK,
                    Material.HONEY_BLOCK,
                    Material.HONEY_BOTTLE,
                    Material.SLIME_BLOCK
            ])
    ]

    static final Gson GSON = new Gson()

    ItemFilter() {

        GroovyScript.addUnloadHook {
            UUIDDataManager.getByClass(FilterData.class).each { it.saveAll(false) }
        }

        UUIDDataManager.register("item_filter", FilterData.class)

        events()
        commands()
    }

    static def events() {
        Events.subscribe(PlayerPickupItemEvent.class).handler { event ->
            if (event.isCancelled()) return

            def item = event.getItem().getItemStack()
            if (item == null) return

            Player player = event.getPlayer()
            FilterData data = getFilterData(player)

            if (data == null) return

            if (!data.enabled || !data.lastOptions) return

            def filterOptions = data.filterOptions.get(data.lastOptions)

            if (filterOptions == null) return

            if (!filterOptions.enabledMaterials.contains(item.getType())) {
                event.setCancelled(true)
            }
        }
    }

    def commands() {
        SubCommandBuilder command = new SubCommandBuilder("itemfilter", "filter").defaultAction { player ->
            openFilterSelection(player)
        }

        command.create("list").register { cmd ->
            def player = cmd.sender()
            def data = getFilterData(player)
            if (data == null) {
                cmd.reply("§3§lFilter §> §cError.")
                return
            }

            if (data.filterOptions.isEmpty()) {
                cmd.reply("§3§lFilter §> §cNo filters.")
                return
            }

            cmd.reply("§3§lFilter §> §eFilters: §a${data.filterOptions.keySet().join(", ")}")
        }.create("toggle").register { cmd ->
            def player = cmd.sender()

            updateFilterData(player) { data ->
                if (cmd.args().size() == 0) {
                    def state = data.toggle()
                    if (state) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                        cmd.reply("§3§lFilter §> §eFilter §a§lENABLED")
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
                        cmd.reply("§3§lFilter §> §eFilter §c§lDISABLED")
                    }
                    return state
                }

                String filterId = cmd.arg(0).parseOrFail(String.class)
                if (data.filterOptions.get(filterId) == null) {
                    def state = data.toggle()
                    if (state) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                        cmd.reply("§3§lFilter §> §e${filterId} §a§lENABLED")
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
                        cmd.reply("§3§lFilter §> §e${filterId} §c§lDISABLED")
                    }

                    return state
                }

                if (data.enable(filterId)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                    cmd.reply("§3§lFilter §> §e${filterId} §a§lENABLED")
                    return true
                }

                cmd.reply("§3§lFilter §> §cError.")

                return false
            }
        }.create("create").register { cmd ->
            def player = cmd.sender()

            updateFilterData(player) { data ->
                if (cmd.args().size() == 0) {
                    cmd.reply("§3§lFilter §> §cPlease specify a filter id.")
                    return false
                }

                String filterId = cmd.arg(0).parseOrFail(String.class)
                if (data.filterOptions.get(filterId) != null) {
                    cmd.reply("§3§lFilter §> §cFilter already exists.")
                    return false
                }

                data.addFilter(filterId)
                cmd.reply("§3§lFilter §> §aFilter ${filterId} created.")
                return true
            }
        }.create("delete").register { cmd ->
            def player = cmd.sender()

            updateFilterData(player) { data ->
                if (cmd.args().size() == 0) {
                    cmd.reply("§3§lFilter §> §cPlease specify a filter id.")
                    return false
                }

                String filterId = cmd.arg(0).parseOrFail(String.class)
                if (data.filterOptions.get(filterId) == null) {
                    cmd.reply("§3§lFilter §> §cFilter does not exist.")
                    return false
                }

                if (data.enabled && data.lastOptions?.equals(filterId)) {
                    data.disable()
                }
                data.filterOptions.remove(filterId)
                cmd.reply("§3§lFilter §> §cFilter ${filterId} deleted.")
                return true
            }
        }.build()

        Commands.create().assertPlayer().handler { cmd ->
            def player = cmd.sender()

            updateFilterData(player) { data ->
                if (cmd.args().size() == 0) {
                    def state = data.toggle()
                    if (state) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                        cmd.reply("§3§lFilter §> §eFilter §a§lENABLED")
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
                        cmd.reply("§3§lFilter §> §eFilter §c§lDISABLED")
                    }
                    return state
                }

                String filterId = cmd.arg(0).parseOrFail(String.class)
                if (data.filterOptions.get(filterId) == null) {
                    def state = data.toggle()
                    if (state) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                        cmd.reply("§3§lFilter §> §e${filterId} §a§lENABLED")
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
                        cmd.reply("§3§lFilter §> §e${filterId} §c§lDISABLED")
                    }

                    return state
                }

                if (data.enable(filterId)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                    cmd.reply("§3§lFilter §> §e${filterId} §a§lENABLED")
                    return true
                }

                cmd.reply("§3§lFilter §> §cError.")

                return false
            }
        }.register("ft", "filtertoggle", "togglefilter")
    }

    static final NamespacedKey FILTER_DATA_KEY = new NamespacedKey(Starlight.plugin, "filter_data")

    def openFilterSelection(Player player, int page = 1) {
        def data = getFilterData(player)

        if (data == null) return

        MenuBuilder builder

        builder = MenuUtils.createPagedMenu("§3Filter Preview", data.filterOptions.values().toList(), { FilterOptions filterOptions, Integer i ->
            ItemStack stack = FastItemUtils.createItem(filterOptions.optionMaterial, "§3$filterOptions.id", [
                    "§eLeft-Click to edit.",
                    "§eRight-Click to Toggle.",
                    "",
                    "§3Material Size: §a" + filterOptions.enabledMaterials.size(),
                    "§3Status: " + (data.enabled && data.lastOptions?.equals(filterOptions.id) ? "§a§lENABLED" : "§c§lDISABLED"),
            ])

            FastItemUtils.setCustomTag(stack, FILTER_DATA_KEY, ItemTagType.STRING, filterOptions.id)

            return stack
        }, page, false, [
                { Player p, ClickType t, int s ->
                    def item = builder.get().getItem(s as int)

                    if (item == null) return

                    if (!FastItemUtils.hasCustomTag(item, FILTER_DATA_KEY, ItemTagType.STRING)) return

                    String filterId = FastItemUtils.getCustomTag(item, FILTER_DATA_KEY, ItemTagType.STRING)

                    if (t == ClickType.RIGHT) {
                        updateFilterData(p) { filterData ->
                            if (filterData.enabled && filterData.lastOptions?.equals(filterId)) {
                                filterData.disable()
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
                                openFilterSelection(player)
                                return true
                            }

                            if (filterData.enable(filterId)) {
                                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                                openFilterSelection(player)
                                return true
                            }

                            return false
                        }
                    } else if (t == ClickType.LEFT) {
                        openFilterEdit(player, filterId)
                    }
                },
                { p, t, s -> openFilterSelection(player, page + 1) },
                { p, t, s -> openFilterSelection(player, page - 1) },
        ])

        builder.set(builder.get().getSize() - 7, FastItemUtils.createItem(Material.SUNFLOWER, "§8[§3Create§8]", [
                "",
                "§e * Click to create a Filter * "
        ]), { p, t, s ->
            SignUtils.openSign(player, ["", "^ ^ ^", "Enter Name"], { lines, p1 ->
                if (lines == null) return

                String filterId = lines[0]

                if (filterId == null || filterId.isEmpty()) {
                    player.sendMessage("§cPlease enter a valid filter id.")
                    openFilterSelection(player)
                    return
                }

                if (data.filterOptions.get(filterId) != null) {
                    player.sendMessage("§cFilter already exists.")
                    openFilterSelection(player)
                    return
                }

                data.addFilter(filterId)
                player.sendMessage("§aFilter ${filterId} created.")
                openFilterSelection(player)
            })
        })

        builder.openSync(player)
    }

    static ItemStack backItem = FastItemUtils.createItem(Material.PAPER, "§cBack", ["", "§a ▎ §fClick to go back"])

    void openFilterEdit(Player player, String filterId) {
        def data = getFilterData(player)
        if (data == null) return

        FilterOptions options = data.filterOptions.get(filterId)
        if (options == null) return

        MenuBuilder builder

        builder = new MenuBuilder(18, "§3Edit Filter §7- §e$filterId")
        MenuDecorator.decorate(builder, [
                "888888888",
                "888888888"
        ])

        int startIndex = 1

        for (int i = 0; i < FILTER_CATEGORIES.size(); i++) {
            FilterCategory category = FILTER_CATEGORIES.get(i)
            if (category == null) continue

            builder.set(startIndex, FastItemUtils.createItem(category.categoryIcon, "§b§l" + StringUtils.capitalize(category.getCategoryId()), ["§7Click to edit."], false), { p, t, s ->
                openFilterEditCategory(p, filterId, category)
            })
            startIndex++
        }

        builder.set(2, 1, backItem, { p, t, s -> openFilterSelection(p) })

        builder.openSync(player)
    }

    void openFilterEditCategory(Player player, String filterId, FilterCategory category, int page = 1) {
        def data = getFilterData(player)

        if (data == null) return

        FilterOptions options = data.filterOptions.get(filterId)

        if (options == null) return

        MenuBuilder builder

        builder = MenuUtils.createPagedMenu("§3Edit Filter §7- §e$filterId §7§o(${StringUtils.capitalize(category.categoryId)})", category.containedMaterials, { Material material, Integer i ->
            ItemStack stack = FastItemUtils.createItem(material, "§3" + StringUtils.capitalize(material.name()), [
                    "§aClick to toggle.",
                    "",
                    "§7Currently: " + (options.enabledMaterials.contains(material) ? "§a§lALLOWED" : "§c§lDENIED")
            ], false)

            return stack
        }, page, true, [
                { Player p, ClickType t, int s ->

                    def item = builder.get().getItem(s as int)

                    if (item == null || item.getType() == Material.AIR) return

                    updateFilterData(p) { filterData ->
                        def material = item.getType()

                        if (options.enabledMaterials.contains(material)) {
                            options.enabledMaterials.remove(material)
                            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f)
                        } else {
                            options.enabledMaterials.add(material)
                            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f)
                        }

                        openFilterEditCategory(player, filterId, category, page)
                        return true
                    }
                },
                { p, t, s -> openFilterEditCategory(p as Player, filterId, category, page + 1) },
                { p, t, s -> openFilterEditCategory(p as Player, filterId, category, page - 1) },
                { p, t, s -> openFilterEdit(p as Player, options.id) }
        ])

        builder.openSync(player)
    }

    static boolean updateFilterData(Player player, BooleanCallback<FilterData> update) {
        FilterData data = getFilterData(player)

        if (data != null && update.exec(data)) {
            data.queueSave()
            return true
        }

        return false
    }

    static FilterData getFilterData(Player player) {
        return getFilterData(player.getUniqueId())
    }

    static FilterData getFilterData(UUID playerId) {
        return UUIDDataManager.getData(playerId, FilterData.class)
    }


}

