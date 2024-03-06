package scripts.factions.eco.loottable

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.random.RandomSelector
import org.starcade.starlight.helper.utils.Players
import scripts.shared.core.cfg.utils.SelectionUtils
import scripts.shared.data.uuid.UUIDDataManager
import scripts.factions.eco.loottable.v2.api.LootTable
import scripts.factions.eco.loottable.v2.api.LootTableCategory
import scripts.factions.eco.loottable.v2.api.Reward
import scripts.factions.eco.loottable.v2.impl.CommandReward
import scripts.factions.eco.loottable.v2.impl.ItemReward
import scripts.factions.eco.loottable.v2.api.RewardCategory
import scripts.shared.core.cfg.utils.PromptUtils
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.SignUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MenuDecorator
import scripts.shared3.utils.Callback

class LootTableHandler {

    LootTableHandler() {
        GroovyScript.addUnloadHook {
            UUIDDataManager.getByClass(RewardCategory.class).each { it.saveAll(false) }
            UUIDDataManager.getByClass(LootTableCategory.class).each { it.saveAll(false) }
            UUIDDataManager.getByClass(LootTable.class).each { it.saveAll(false) }
        }

        UUIDDataManager.register("loot_table_rewards", RewardCategory)
        UUIDDataManager.register("loot_table_category", LootTableCategory)
        UUIDDataManager.register("loot_table_tables", LootTable)

        registerCommands()
    }

    static def registerCommands() {
        SubCommandBuilder builder = new SubCommandBuilder("loottable", "lt").defaultAction { player ->
            if (!player.hasPermission("loottable.admin")) {
                Players.msg(player, "&cYou do not have permission to use this command.")
                return
            }

            openLootMenu(player)
        }

        builder.build()
    }

    /*

       DATA

    */

    static LootTableCategory getLootTableCategory(UUID categoryId, boolean create = true) {
        return UUIDDataManager.getData(categoryId, LootTableCategory.class, create)
    }

    static LootTableCategory getTableCategoryByName(String name) {
        return getLootTableCategories().find { it.name.equalsIgnoreCase(name) }
    }

    static Collection<LootTableCategory> getLootTableCategories() {
        return UUIDDataManager.getAllData(LootTableCategory.class)
    }

    static LootTable getLootTable(UUID tableId, boolean create = true) {
        return UUIDDataManager.getData(tableId, LootTable.class, create)
    }

    static LootTable getLootTableByName(String name) {
        return getLootTables().find { it.name.equalsIgnoreCase(name) }
    }

    static Collection<LootTable> getLootTables() {
        return UUIDDataManager.getAllData(LootTable.class)
    }

    static RewardCategory getRewardCategory(UUID categoryId, boolean create = true) {
        return UUIDDataManager.getData(categoryId, RewardCategory.class, create)
    }

    static RewardCategory getRewardCategoryByName(String name) {
        return getRewardCategories().find { it.name.equalsIgnoreCase(name) }
    }

    static Set<RewardCategory> getRewardCategories() {
        return UUIDDataManager.getAllData(RewardCategory.class)
    }

    static Reward getReward(LootTable table) {
        def loot = new HashMap<Reward, Double>()

        table.rewards.each { reward ->
            if (!reward.enabled) return
            if (reward.timesPulled >= reward.maxPulls && reward.maxPulls != 0) return
            loot.put(reward, reward.weight)
        }

        return RandomSelector.weighted(loot.entrySet(), { entry -> entry.getValue() }).pick().getKey()
    }

    static def giveReward(Player player, LootTable table) {
        table.getRandomReward().giveReward(player)
    }

    /*

       GUIS

    */

    static NamespacedKey CATEGORY_KEY = new NamespacedKey(Starlight.plugin, "loot_category")
    static NamespacedKey TABLE_KEY = new NamespacedKey(Starlight.plugin, "loot_table")
    static NamespacedKey REWARD_KEY = new NamespacedKey(Starlight.plugin, "loot_reward")

    static def openLootMenu(Player player) {
        MenuBuilder menu = new MenuBuilder(27, "§3Loot Menu")

        MenuDecorator.decorate(menu, [
                "222222222",
                "222222222",
                "222222222"
        ])

        menu.set(2, 3, FastItemUtils.createItem(Material.CHEST, "§aOpen Loot Tables", [
                "",
                "§a * Click to open loot tables *"
        ]), { p, t, s -> openCategories(p as Player) })

        menu.set(2, 7, FastItemUtils.createItem(Material.EMERALD, "§aOpen Rewards", [
                "",
                "§a * Click to open rewards *"
        ]), { p, t, s -> openRewards(p as Player) })

        menu.openSync(player)
    }

    static def openRewards(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§3Reward Manager", getRewardCategories().toList(), { RewardCategory category, Integer slot ->
            def item = FastItemUtils.createItem(
                    category.icon,
                    "§3Category - §6${category.name}",
                    [
                            "",
                            "§7${category.rewards.size()} Reward${category.rewards.size() == 1 ? "" : "s"}",
                            "",
                            "§b§lLeft-Click §bto view §dRewards",
                            "§b§lMiddle-Click §bto §c§l§nDELETE",
                    ],
                    false)

            FastItemUtils.setCustomTag(item, CATEGORY_KEY, ItemTagType.STRING, category.id.toString())

            return item
        }, page, true, [
                { Player p, ClickType t, int slot ->
                    if (!p.isOp()) return

                    def item = menu.get().getItem(slot)
                    if (item == null || item.type.air) return

                    if (!FastItemUtils.hasCustomTag(item, CATEGORY_KEY, ItemTagType.STRING)) return

                    def categoryId = UUID.fromString(FastItemUtils.getCustomTag(item, CATEGORY_KEY, ItemTagType.STRING))
                    def category = getRewardCategory(categoryId)
                    if (category == null) return

                    if (t == ClickType.LEFT || t == ClickType.SHIFT_LEFT || t == ClickType.RIGHT || t == ClickType.SHIFT_RIGHT) {
                        openRewardCategory(p, category)
                    } else if (t == ClickType.MIDDLE) {
                        MenuUtils.createConfirmMenu(player, "§8Confirm Delete", item, () -> {
                            Players.msg(player, "§] §> §cDeleted this category.")

                            UUIDDataManager.getByClass(RewardCategory.class).delete(categoryId)

                            Schedulers.sync().runLater({
                                openRewards(player, page)
                            }, 1)
                        }, () -> {
                            Players.msg(player, "§] §> §cSuccessfully stopped deleting this category")
                            openRewards(player, page)
                        })
                    }
                },
                { Player p, ClickType t, int slot ->
                    openRewards(p, page + 1)
                },
                { Player p, ClickType t, int slot ->
                    openRewards(p, page - 1)
                },
                { Player p, ClickType t, int slot ->
                    openLootMenu(p)
                }
        ])

        menu.set(menu.get().getSize() - 4, FastItemUtils.createItem(Material.SUNFLOWER, "§aCreate Reward Category", [
                "",
                "§a * Click create a Reward Category * "
        ]), { p, t, s ->
            PromptUtils.prompt(p, "§aEnter Category Name:", { name ->
                if (getRewardCategoryByName(name) != null) {
                    Players.msg(p, "§cA Category with that name already exists.")
                    return
                }

                def category = getRewardCategory(UUID.randomUUID())
                category.name = name
                category.queueSave()

                openRewards(p, page)
            })
        })

        menu.openSync(player)
    }

    static def openRewardCategory(Player player, RewardCategory category, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§3Reward Manager §7(${category.rewards.size()})", category.rewards.toList(), { Reward reward, Integer slot ->
            ItemStack item
            if (reward instanceof ItemReward) {
                def itemReward = reward as ItemReward

                item = itemReward.getItemStack()
            } else if (reward instanceof CommandReward) {
                def commandReward = reward as CommandReward

                item = commandReward.getItemStack()
                List<String> lore = FastItemUtils.getLore(item) ?: []

                lore.add("")
                lore.add("§6Commands: §e${commandReward.commands.size()}")
                commandReward.commands.each {
                    lore.add("§7- §e${it}")
                }

                FastItemUtils.setLore(item, lore)
            } else {
                item = FastItemUtils.createItem(Material.PAPER, "§f§lUnknown", [
                        "",
                        "§7${reward}"
                ], false)
            }


            def lore = FastItemUtils.getLore(item) ?: []

            lore.add("§6Weight: §e${reward.weight}")
            lore.add("§6Status: ${reward.enabled ? "§a§lENABLED" : "§c§lDISABLED"}")
            lore.add("§6Tracking: ${reward.tracking ? "§a§lENABLED" : "§c§lDISABLED"}")

            if (reward.isTracking()) {
                lore.add("")
                lore.add("§6Max Pulls: §e${reward.maxPulls}")
                lore.add("§6Times Pulled: §e${reward.timesPulled}")
                lore.add("§6Anti-Dupe§7(broken)§6: ${reward.antiDupe ? "§a§lENABLED" : "§c§lDISABLED"}")
            }

            lore.add("")
            lore.add("§b§lLeft-Click §bto edit §dReward")

            FastItemUtils.setLore(item, lore)
            FastItemUtils.setCustomTag(item, REWARD_KEY, ItemTagType.STRING, reward.getId().toString())

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    if (!p.isOp()) return

                    ItemStack item = menu.get().getItem(s as Integer)
                    if (item == null || !FastItemUtils.hasCustomTag(item, REWARD_KEY, ItemTagType.STRING)) return

                    String rewardId = FastItemUtils.getCustomTag(item, REWARD_KEY, ItemTagType.STRING)
                    if (rewardId == null || rewardId.isEmpty()) return

                    UUID rewardUuid = UUID.fromString(rewardId)
                    if (rewardUuid == null) return

                    Reward reward = category.rewards.find { it.id == rewardUuid }
                    if (reward == null) return

                    openRewardCategoryEdit(p, category, reward)
                },
                { Player p, ClickType t, int s -> openRewardCategory(p, category, page + 1) },
                { Player p, ClickType t, int s -> openRewardCategory(p, category, page - 1) },
                { Player p, ClickType t, int s ->
                    openRewards(p)
                }
        ])

        menu.set(menu.get().getSize() - 6, FastItemUtils.createItem(Material.SUNFLOWER, "§aCreate Command Reward", [
                "",
                "§a * Click create a Command Reward * ",
                "§a * §b{uuid} §a| §d{player} *"
        ]), { p, t, s ->
            SignUtils.openSign(p, ["", "^ ^ ^", "Enter Command"], { String[] lines, Player p1 ->
                String command = lines[0]

                if (command == null || command.isEmpty()) {
                    Players.msg(p1, "§cInvalid command.")
                    return
                }

                if (category.commandRewards.find { it.commands.contains(command) } != null) {
                    Players.msg(p1, "§cA reward with that command already exists.")
                    return
                }

                category.commandRewards.add(new CommandReward(command, 1.0D))
                category.queueSave()
                openRewardCategory(player, category, page)
            })
        })

        menu.set(menu.get().getSize() - 4, FastItemUtils.createItem(category.icon, "§aChange Table Icon", [
                "",
                "§a * Click choose a new * ",
                "§a *      table icon.   *"
        ]), { p, t, s ->
            SelectionUtils.selectMaterial(p, { Material material ->
                category.icon = material
                category.queueSave()
                openRewardCategory(player, category, page)
            })
        })

        menu.setCloseCallback { p ->
            category.queueSave()
        }

        menu.setExternal { p, t, s ->
            if (!p.isOp()) return

            if (s > 36 || s < 0) return

            ItemStack stack = p.getInventory().getItem(s)
            if (stack == null || stack.type == Material.AIR) return

            MenuUtils.createConfirmMenu(player, "§8Add Reward", stack, () -> {
                Players.msg(player, "§] §> §cAdded this reward to table ${category.name}.")

                category.itemRewards.add(new ItemReward(stack, 1.0D))
                category.queueSave()

                Schedulers.sync().runLater({
                    openRewardCategory(player, category, page)
                }, 1)
            }, () -> {
                Players.msg(player, "§] §> §cSuccessfully stopped adding this reward")
                openRewardCategory(player, category, page)
            })
        }

        menu.openSync(player)
    }

    static def openRewardCategoryEdit(Player player, RewardCategory category, Reward reward) {
        MenuBuilder menu

        menu = new MenuBuilder(18, "§3Reward Editor")

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(reward.isEnabled() ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, "§aToggle Status", [
                "",
                "§a * Click to toggle status *"
        ]), { p, t, s ->
            Players.playSound(p, reward.enabled ? Sound.UI_BUTTON_CLICK : Sound.ENTITY_PLAYER_LEVELUP)

            reward.enabled = !reward.enabled
            category.queueSave()

            openRewardCategoryEdit(p, category, reward)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(reward.isTracking() ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, "§aToggle Tracking", [
                "§7Current: §e${reward.tracking ? "§aEnabled" : "§cDisabled"}",
                "",
                "§a * Click to toggle tracking *"
        ]), { p, t, s ->
            Players.playSound(p, reward.isTracking() ? Sound.UI_BUTTON_CLICK : Sound.ENTITY_PLAYER_LEVELUP)

            reward.tracking = !reward.tracking
            category.queueSave()

            openRewardCategoryEdit(p, category, reward)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.MAGENTA_STAINED_GLASS_PANE, "§aChange Weight", [
                "§7Current: §e${reward.weight}",
                "",
                "§a * Click to change weight *"
        ]), { p, t, s ->
            Players.playSound(p, Sound.UI_BUTTON_CLICK)

            SelectionUtils.selectDouble(p, "Enter Weight", [1D, 2D, 3D, 4D, 5D, 6D, 7D, 8D],{ weight ->
                Players.playSound(p, Sound.ENTITY_PLAYER_LEVELUP)
                reward.weight = weight
                category.queueSave()

                openRewardCategoryEdit(p, category, reward)
            })
        })

        if (reward.isTracking()) {
            menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.YELLOW_STAINED_GLASS_PANE, "§aChange Max Pulls", [
                    "§7Current: §e${reward.maxPulls}",
                    "",
                    "§a * Click to change max pulls *",
                    "§a * 0 = Unlimited *",

            ]), { p, t, s ->
                Players.playSound(p, Sound.UI_BUTTON_CLICK)

                SelectionUtils.selectInteger(p, "Enter Max Pulls", [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],{ int maxPulls ->
                    Players.playSound(p, Sound.ENTITY_PLAYER_LEVELUP)
                    reward.maxPulls = maxPulls
                    category.queueSave()

                    openRewardCategoryEdit(p, category, reward)
                })
            })

            menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.ANVIL, "§aChange Anti-Dupe", [
                    "§7Current: §e${reward.antiDupe}",
                    "",
                    "§a * Click to toggle anti-dupe *",

            ]), { p, t, s ->
                Players.playSound(p, Sound.UI_BUTTON_CLICK)

                reward.antiDupe = !reward.antiDupe
                category.queueSave()

                openRewardCategoryEdit(p, category, reward)
            })
        }

        // delete reward
        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.RED_WOOL, "§cDelete Reward", [
                "",
                "§a * Click to delete this reward *"
        ]), { p, t, s ->
            Players.playSound(p, Sound.UI_BUTTON_CLICK)

            MenuUtils.createConfirmMenu(player, "§8Confirm Delete", FastItemUtils.createItem(Material.BARRIER, "§l", []), () -> {
                Players.msg(player, "§] §> §cDeleted this reward.")
                Players.playSound(p, Sound.ENTITY_PLAYER_LEVELUP)

                category.removeReward(reward)
                category.queueSave()

                Schedulers.sync().runLater({
                    openRewardCategory(p, category)
                }, 1)
            }, () -> {
                Players.playSound(p, Sound.UI_BUTTON_CLICK)
                Players.msg(player, "§] §> §cSuccessfully stopped deleting this reward")
                openRewardCategoryEdit(p, category, reward)
            })
        })

        // add to loottable
        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.NETHERITE_INGOT, "§aAdd to Loot Table", [
                "",
                "§a * Click to add to loot table *"
        ]), { p, t, s ->
            Players.playSound(p, Sound.UI_BUTTON_CLICK)


        })

        menu.set(17, FastItemUtils.createItem(Material.RED_DYE, "§cBack", [
                "",
                "§a * Click to go back *"
        ]), { p, t, s ->
            Players.playSound(p, Sound.UI_BUTTON_CLICK)
            openRewardCategory(p, category)
        })

        menu.setCloseCallback { p ->
            category.queueSave()
        }

        menu.openSync(player)
    }

    static def openCategories(Player player, int page = 1, Callback<LootTable> selectTableCallback = null) {
        MenuBuilder menu

        // make this show either categorized view or all loottables

        menu = MenuUtils.createPagedMenu("§3Loot Manager §7(${getLootTableCategories()?.size() ?: 0})", getLootTableCategories().toList(), { LootTableCategory category, Integer slot ->
            def item = FastItemUtils.createItem(
                    category.icon,
                    "§3Category - §6${category.name}",
                    [
                            "",
                            "§7${category.tables.size()} Table${category.tables.size() == 1 ? "" : "s"}",
                            "",
                            "§b§lLeft-Click §bto view §dTables",
                            "§b§lMiddle-Click §bto §c§l§nDELETE",
                    ],
                    false)

            FastItemUtils.setCustomTag(item, CATEGORY_KEY, ItemTagType.STRING, category.id.toString())
            return item
        }, page, true, [
                { Player p, ClickType t, int slot ->
                    if (!p.isOp()) return

                    ItemStack item = menu.get().getItem(slot as Integer)

                    if (!FastItemUtils.hasCustomTag(item, CATEGORY_KEY, ItemTagType.STRING)) return

                    def categoryId = UUID.fromString(FastItemUtils.getCustomTag(item, CATEGORY_KEY, ItemTagType.STRING))
                    def category = getLootTableCategory(categoryId)
                    if (category == null) return

                    if (t == ClickType.LEFT || t == ClickType.SHIFT_LEFT || t == ClickType.RIGHT || t == ClickType.SHIFT_RIGHT) {
                        openCategory(p, 1, category, selectTableCallback)
                    } else if (t == ClickType.MIDDLE) {
                        MenuUtils.createConfirmMenu(player, "§8Confirm Delete", item, () -> {
                            category.tables.each {
                                UUIDDataManager.getByClass(LootTable.class).delete(it)
                            }

                            UUIDDataManager.getByClass(LootTableCategory.class).delete(categoryId)

                            Players.msg(player, "§] §> §cDeleted this table.")

                            Schedulers.sync().runLater({
                                openCategories(player, page)
                            }, 1)
                        }, () -> {
                            Players.msg(player, "§] §> §cSuccessfully stopped deleting this table")
                            openCategories(player, page)
                        })
                    }
                },
                { Player p, ClickType t, int slot ->
                    openCategories(p as Player, page + 1, selectTableCallback)
                },
                { Player p, ClickType t, int slot ->
                    openCategories(p as Player, page - 1, selectTableCallback)
                },
                { Player p, ClickType t, int slot ->
                    openLootMenu(p)
                }
        ])

        menu.set(menu.get().getSize() - 6, FastItemUtils.createItem(Material.SUNFLOWER, "§aCreate Category", [
                "",
                "§a * Click create a Category * "
        ]), { p, t, s ->
            PromptUtils.prompt(p, "§aEnter Category Name:", { name ->
                if (getTableCategoryByName(name) != null) {
                    Players.msg(p, "§cA Category with that name already exists.")
                    return
                }

                def category = getLootTableCategory(UUID.randomUUID())
                category.name = name
                category.queueSave()

                openCategories(p, page, selectTableCallback)
            })
        })

        menu.openSync(player)
    }

    static def openCategory(Player player, int page = 1, LootTableCategory category, Callback<LootTable> selectTableCallback = null) {
        MenuBuilder builder

        builder = MenuUtils.createPagedMenu("§3Loot Manager §7(${category.tables.size()})", category.tables.toList(), { UUID tableId, Integer i ->
            def table = category.getOrCreateTable(tableId)
            def item = FastItemUtils.createItem(table.icon, "§3Table - §6${table.name}",
                    [
                            "",
                            "§7${table.rewards.size()} rewards",
                            "",
                            "§b§lLeft-Click §bto edit §dRewards",
                            "§b§lRight-Click §bto test §dRewards",
                            "§b§lMiddle-Click §bto §c§lDELETE",
                    ],
                    false)

            FastItemUtils.setCustomTag(item, TABLE_KEY, ItemTagType.STRING, table.id.toString())

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    if (!p.isOp()) return

                    ItemStack item = builder.get().getItem(s as Integer)

                    if (!FastItemUtils.hasCustomTag(item, TABLE_KEY, ItemTagType.STRING)) return

                    def tableId = UUID.fromString(FastItemUtils.getCustomTag(item, TABLE_KEY, ItemTagType.STRING))
                    def table = category.getOrCreateTable(tableId)
                    if (table == null) return

                    if (selectTableCallback != null) {
                        selectTableCallback.exec(table)
                    } else {
                        if (t == ClickType.LEFT || t == ClickType.SHIFT_LEFT) {
                            openTableGui(p as Player, category, table)
                        } else if (t == ClickType.RIGHT || t == ClickType.SHIFT_RIGHT) {
                            giveReward(p as Player, table)
                        } else if (t == ClickType.MIDDLE) {
                            MenuUtils.createConfirmMenu(player, "§8Confirm Delete", item, () -> {
                                Players.msg(player, "§] §> §cDeleted this category.")

                                category.tables.removeIf { it == tableId }
                                category.queueSave()

                                Schedulers.sync().runLater({
                                    openCategory(player, page, category)
                                }, 1)
                            }, () -> {
                                Players.msg(player, "§] §> §cSuccessfully stopped deleting this category")
                                openCategory(player, page, category)
                            })
                        }
                    }
                },
                { p, t, s -> openCategory(p as Player, page + 1, category) },
                { p, t, s -> openCategory(p as Player, page - 1, category) },
                { p, t, s -> openCategories(p as Player, 1, selectTableCallback) }
        ])

        builder.set(builder.get().getSize() - 6, FastItemUtils.createItem(Material.SUNFLOWER, "§aCreate Loot Table", [
                "",
                "§a * Click create a Loot Table * "
        ]), { p, t, s ->
            PromptUtils.prompt(p, "§aEnter Table Name:", { name ->
                if (category.hasTable(name)) {
                    Players.msg(p, "§cA Loot Table with that name already exists.")
                    return
                }

                category.getOrCreateTable(UUID.randomUUID(), name)

                openCategory(p, page, category)
            })
        })

        builder.set(builder.get().getSize() - 3, FastItemUtils.createItem(category.icon, "§aChange Icon", [
                "",
                "§a * Click choose a new icon * ",
        ]), { p, t, s ->
            SelectionUtils.selectMaterial(p, { Material material ->
                category.icon = material
                category.queueSave()
                openCategory(p, page, category)
            })
        })

        builder.openSync(player)
    }

    static def openTableGui(Player player, LootTableCategory category, LootTable table, int page = 1, Callback<Player> backCallback = null, Closure closeCallback = null) {
        MenuBuilder builder

        builder = MenuUtils.createLargePagedMenu("§3Loot Editor §7(§6${table.name}§7)", table.getSortedRewards(), { Reward reward, Integer i ->
            ItemStack item
            if (reward instanceof ItemReward) {
                def itemReward = reward as ItemReward

                item = itemReward.getItemStack()
            } else if (reward instanceof CommandReward) {
                def commandReward = reward as CommandReward

                item = commandReward.getItemStack()
                List<String> lore = FastItemUtils.getLore(item) ?: []

                lore.add("")
                lore.add("§6Commands: §e${commandReward.commands.size()}")
                commandReward.commands.each {
                    lore.add("§7- §e${it}")
                }

                FastItemUtils.setLore(item, lore)
            } else {
                item = FastItemUtils.createItem(Material.PAPER, "§f§lUnknown", [
                        "",
                        "§7${reward}"
                ], false)
            }


            def lore = FastItemUtils.getLore(item) ?: []

            lore.add("§6Weight: §e${reward.weight}")
            lore.add("§6Status: ${reward.enabled ? "§a§lENABLED" : "§c§lDISABLED"}")
            lore.add("§6Tracking: ${reward.tracking ? "§a§lENABLED" : "§c§lDISABLED"}")

            if (reward.isTracking()) {
                lore.add("")
                lore.add("§6Max Pulls: §e${reward.maxPulls}")
                lore.add("§6Times Pulled: §e${reward.timesPulled}")
                lore.add("§6Anti-Dupe§7(broken)§6: ${reward.antiDupe ? "§a§lENABLED" : "§c§lDISABLED"}")
            }

            lore.add("")
            lore.add("§b§lLeft-Click §bto edit §dReward")

            FastItemUtils.setLore(item, lore)
            FastItemUtils.setCustomTag(item, REWARD_KEY, ItemTagType.STRING, reward.getId().toString())

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    if (!p.isOp()) return

                    ItemStack item = builder.get().getItem(s as Integer)
                    if (item == null || !FastItemUtils.hasCustomTag(item, REWARD_KEY, ItemTagType.STRING)) return

                    String rewardId = FastItemUtils.getCustomTag(item, REWARD_KEY, ItemTagType.STRING)
                    if (rewardId == null || rewardId.isEmpty()) return

                    UUID rewardUuid = UUID.fromString(rewardId)
                    if (rewardUuid == null) return

                    Reward reward = table.rewards.find { it.id == rewardUuid }
                    if (reward == null) return

                    openReward(p, category, table, reward, backCallback, closeCallback)
                },
                { Player p, ClickType t, int s -> openTableGui(p, category, table, page + 1, backCallback, closeCallback) },
                { Player p, ClickType t, int s -> openTableGui(p, category, table, page - 1, backCallback, closeCallback) },
                { Player p, ClickType t, int s ->
                    if (backCallback != null) backCallback.exec(p)
                    else openCategory(p, category)
                }
        ])

        builder.set(builder.get().getSize() - 6, FastItemUtils.createItem(Material.SUNFLOWER, "§aCreate Command Reward", [
                "",
                "§a * Click create a Command Reward * ",
                "§a * §b{uuid} §a| §d{player} *"
        ]), { p, t, s ->
            SignUtils.openSign(p, ["", "^ ^ ^", "Enter Command"], { String[] lines, Player p1 ->
                String command = lines[0]

                if (command == null || command.isEmpty()) {
                    Players.msg(p1, "§cInvalid command.")
                    return
                }

                if (table.commandRewards.find { it.commands.contains(command) } != null) {
                    Players.msg(p1, "§cA reward with that command already exists.")
                    return
                }

                table.commandRewards.add(new CommandReward(command, 1.0D))
                table.queueSave()
                category.queueSave()

                openTableGui(player, category, table, page, backCallback, closeCallback)
            })
        })

        builder.set(builder.get().getSize() - 4, FastItemUtils.createItem(table.icon, "§aChange Table Icon", [
                "",
                "§a * Click choose a new * ",
                "§a *      table icon.   *"
        ]), { p, t, s ->
            SelectionUtils.selectMaterial(p, { Material material ->
                table.icon = material

                table.queueSave()
                category.queueSave()

                openTableGui(p, category, table, page, backCallback, closeCallback)
            })
        })

        builder.setCloseCallback { p ->
            table.queueSave()
            category.queueSave()

            if (closeCallback != null) closeCallback.call()
        }

        builder.setExternal { p, t, s ->
            if (!p.isOp()) return

            if (s > 36 || s < 0) return

            ItemStack stack = p.getInventory().getItem(s)
            if (stack == null || stack.type == Material.AIR) return

            MenuUtils.createConfirmMenu(player, "§8Add Reward", stack, () -> {
                Players.msg(player, "§] §> §cAdded this reward to table ${table.name}.")

                table.itemRewards.add(new ItemReward(stack, 1.0D))
                table.queueSave()
                category.queueSave()

                Schedulers.sync().runLater({
                    openTableGui(player, category, table, page, backCallback, closeCallback)
                }, 1)
            }, () -> {
                Players.msg(player, "§] §> §cSuccessfully stopped adding this reward")
                openTableGui(player, category, table, page, backCallback, closeCallback)
            })
        }

        builder.openSync(player)
    }

    static def openReward(Player player, LootTableCategory category, LootTable table, Reward reward, Callback<Player> backCallback = null, Closure closeCallback = null) {
        MenuBuilder menu

        menu = new MenuBuilder(18, "§3Reward Editor")

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(reward.isEnabled() ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, "§aToggle Status", [
                "",
                "§a * Click to toggle status *"
        ]), { p, t, s ->
            Players.playSound(p, reward.enabled ? Sound.UI_BUTTON_CLICK : Sound.ENTITY_PLAYER_LEVELUP)

            reward.enabled = !reward.enabled

            table.queueSave()
            category.queueSave()

            openReward(p, category, table, reward, backCallback, closeCallback)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(reward.isTracking() ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, "§aToggle Tracking", [
                "§7Current: §e${reward.tracking ? "§aEnabled" : "§cDisabled"}",
                "",
                "§a * Click to toggle tracking *"
        ]), { p, t, s ->
            Players.playSound(p, reward.isTracking() ? Sound.UI_BUTTON_CLICK : Sound.ENTITY_PLAYER_LEVELUP)

            reward.tracking = !reward.tracking

            table.queueSave()
            category.queueSave()

            openReward(p, category, table, reward, backCallback, closeCallback)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.MAGENTA_STAINED_GLASS_PANE, "§aChange Weight", [
                "§7Current: §e${reward.weight}",
                "",
                "§a * Click to change weight *"
        ]), { p, t, s ->
            Players.playSound(p, Sound.UI_BUTTON_CLICK)

            SelectionUtils.selectDouble(p, "Enter Weight", [1D, 2D, 3D, 4D, 5D, 6D, 7D, 8D],{ int weight ->
                Players.playSound(p, Sound.ENTITY_PLAYER_LEVELUP)
                reward.weight = weight

                table.queueSave()
                category.queueSave()

                openReward(p, category, table, reward, backCallback, closeCallback)
            })
        })

        if (reward.isTracking()) {
            menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.YELLOW_STAINED_GLASS_PANE, "§aChange Max Pulls", [
                    "§7Current: §e${reward.maxPulls}",
                    "",
                    "§a * Click to change max pulls *",
                    "§a * 0 = Unlimited *",

            ]), { p, t, s ->
                Players.playSound(p, Sound.UI_BUTTON_CLICK)

                SelectionUtils.selectInteger(p, "Enter Max Pulls", [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],{ int maxPulls ->
                    Players.playSound(p, Sound.ENTITY_PLAYER_LEVELUP)
                    reward.maxPulls = maxPulls

                    table.queueSave()
                    category.queueSave()

                    openReward(p, category, table, reward, backCallback, closeCallback)
                })
            })

            menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.CYAN_STAINED_GLASS_PANE, "§aChange Times Pulled", [
                    "§7Current: §e${reward.timesPulled}",
                    "",
                    "§a * Click to reset times pulled *",

            ]), { p, t, s ->
                Players.playSound(p, Sound.UI_BUTTON_CLICK)

                MenuUtils.createConfirmMenu(player, "§8Confirm Reset", FastItemUtils.createItem(Material.BARRIER, "§l", []), () -> {
                    Players.playSound(p, Sound.ENTITY_PLAYER_LEVELUP)
                    reward.timesPulled = 0

                    table.queueSave()
                    category.queueSave()

                    openReward(p, category, table, reward, backCallback, closeCallback)
                }, () -> {
                    Players.playSound(p, Sound.UI_BUTTON_CLICK)
                    Players.msg(player, "§] §> §cSuccessfully stopped resetting times pulled")
                    openReward(p, category, table, reward, backCallback, closeCallback)
                })
            })

            menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.ANVIL, "§aChange Anti-Dupe", [
                    "§7Current: §e${reward.antiDupe}",
                    "",
                    "§a * Click to toggle anti-dupe *",

            ]), { p, t, s ->
                Players.playSound(p, Sound.UI_BUTTON_CLICK)

                reward.antiDupe = !reward.antiDupe

                table.queueSave()
                category.queueSave()

                openReward(p, category, table, reward, backCallback, closeCallback)
            })
        }

        // delete reward
        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.RED_WOOL, "§cDelete Reward", [
                "",
                "§a * Click to delete this reward *"
        ]), { p, t, s ->
            Players.playSound(p, Sound.UI_BUTTON_CLICK)

            MenuUtils.createConfirmMenu(player, "§8Confirm Delete", FastItemUtils.createItem(Material.BARRIER, "§l", []), () -> {
                Players.msg(player, "§] §> §cDeleted this reward.")
                Players.playSound(p, Sound.ENTITY_PLAYER_LEVELUP)

                table.removeReward(reward)

                table.queueSave()
                category.queueSave()

                Schedulers.sync().runLater({
                    openTableGui(p, category, table, 1, backCallback, closeCallback)
                }, 1)
            }, () -> {
                Players.playSound(p, Sound.UI_BUTTON_CLICK)
                Players.msg(player, "§] §> §cSuccessfully stopped deleting this reward")
                openReward(p, category, table, reward, backCallback, closeCallback)
            })
        })

        menu.set(17, FastItemUtils.createItem(Material.RED_DYE, "§cBack", [
                "",
                "§a * Click to go back *"
        ]), { p, t, s ->
            Players.playSound(p, Sound.UI_BUTTON_CLICK)
            openTableGui(p, category, table, 1, backCallback, closeCallback)
        })


        menu.setCloseCallback { p ->
            table.queueSave()
            category.queueSave()
        }

        menu.openSync(player)
    }

}
