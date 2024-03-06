package scripts.factions.eco.rewardbox

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import scripts.shared.core.cfg.Config
import scripts.shared.core.cfg.utils.DBConfigUtil
import scripts.shared.core.cfg.utils.SelectionUtils
import scripts.shared.data.string.StringDataManager
import scripts.factions.eco.loottable.LootTableHandler
import scripts.factions.eco.rewardbox.data.RewardBox
import scripts.shared.core.cfg.utils.PromptUtils
import scripts.shared.legacy.AntiDupeUtils
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.DataUtils

@CompileStatic(TypeCheckingMode.SKIP)
class RewardBoxes {

    static NamespacedKey rewardBoxKey = new NamespacedKey(Starlight.plugin, "rewardbox")
    static NamespacedKey rewardBoxEdit = new NamespacedKey(Starlight.plugin, "rewardboxedit")

//    Map<UUID, RewardBoxOpeningAnimation> openingBoxes = Maps.newConcurrentMap()

    static Config config
    private static UUID configId = UUID.fromString("e0f0f0f0-0000-0000-0000-000000000000")

    RewardBoxes() {
        GroovyScript.addScriptHook(GroovyScript.HookType.RECOMPILE, {
            if (!GroovyScript.getCurrentScript().getWatchedScripts().contains(it)) return

            reloadConfig()
        })

        GroovyScript.addUnloadHook {
            StringDataManager.getByClass(RewardBox.class).saveAll(false)
        }

        StringDataManager.register("rewardboxes", RewardBox)

//        GroovyScript.addUnloadHook {
//            openingBoxes.values().forEach { it.finish() }
//        }

        reloadConfig()
        registerCommands()
        registerEvents()

//        Schedulers.async().runRepeating({
//            openingBoxes.entrySet().removeIf(entry -> {
//                entry.value.tick()
//                return entry.value.finished.get()
//            })
//        }, 50L, TimeUnit.MILLISECONDS, 50L, TimeUnit.MILLISECONDS)
    }

    static def registerCommands() {
        SubCommandBuilder rewardBoxCommand = new SubCommandBuilder("rewardbox", "lootbox", "lb", "rb").defaultAction {
            if (it.isOp()) {
                // open admin gui display
                openRewardBoxes(it)
            } else {
                openDisplayReward(it.getPlayer())
            }
        }

        rewardBoxCommand.create("display").register {
            openDisplayReward(it.sender())
        }

        rewardBoxCommand.create("admin").requirePermission("starlight.admin").register {
            openRewardBoxes(it.sender())
        }

        rewardBoxCommand.build()
    }

    static def registerEvents() {
        Events.subscribe(PlayerInteractEvent.class).handler { event ->
            def item = event.getItem()
            def player = event.getPlayer()

            if (item == null || item.getType() == Material.AIR) return

            if (!FastItemUtils.hasCustomTag(item, rewardBoxKey, ItemTagType.STRING)) return

            def box = getBoxFromItem(item)
            if (box == null) return

            def id = FastItemUtils.getId(item)

            FastInventoryUtils.use(player)

            AntiDupeUtils.useId(id) {
                box.getLootTable().getRandomReward()?.giveReward(player, "§] §> §aYou have received a reward from a reward box.")
            }

//            TODO: add support for opening boxes
//            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
//                // right click open, check if sneaking for instant open? or just make that a toggle
//            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
//                // left click open preview and filter?
//            }
        }

    }

    static def reloadConfig() {
        config = DBConfigUtil.createConfig("reward_boxes", "§3Reward-Boxes", [], Material.BEACON)

        config.queueSave()
    }

    static def openDisplayReward(Player player) {
        MenuBuilder menuBuilder = new MenuBuilder(InventoryType.HOPPER, "§7      §aLATEST §3Reward Box")

        def tempItem = FastItemUtils.createItem(Material.GREEN_STAINED_GLASS_PANE, "", [], false)

        for (int i = 0; i < 5; i++) {
            menuBuilder.set(i, tempItem)
        }

        def activeReward = getRewardBoxes().find { it.isEnabled() && it.isDisplayBox() }
        ItemStack activeRewardItem = activeReward?.createItemStack(true) ?: FastItemUtils.createItem(Material.BARRIER, "§c§lNo Active Reward Box", [], false)

        menuBuilder.set(2, activeRewardItem, { p, t, s ->

        })


        menuBuilder.openSync(player)
    }

    static def openRewardBoxes(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§a§lReward Boxes", getRewardBoxes().toList(), { RewardBox box, Integer i ->
            def item = box.createItemStack(true)
            def lore = FastItemUtils.getLore(item)

            lore.add("")
            lore.add(box.isEnabled() ? "§a§lENABLED" : "§c§lDISABLED")
            if (box.isDisplayBox()) lore.add("§a§lDISPLAY")
            lore.add("")
            lore.add("§7Left-Click To §b§lEDIT")
            lore.add("§7Right-Click To §c§lDELETE")
            lore.add("§7Shift Left-Click To §a§lGIVE")

            FastItemUtils.setLore(item, lore)

            return item
        }, page, false, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)
                    if (item == null || item.type == Material.AIR) return

                    def box = getBoxFromItem(item)
                    if (box == null) return

                    if (t == ClickType.LEFT) {
                        openBoxEditor(p, box)
                    } else if (t == ClickType.RIGHT) {
                        MenuUtils.createConfirmMenu(p, "§8Confirm", item, {
                            Players.msg(p, "§] §> §cDeleted this reward box.")

                            removeBox(box.getId())
                            Schedulers.sync().runLater({
                                openRewardBoxes(p)
                            }, 1)
                        }, {
                            Players.msg(p, "§] §> §cSuccessfully stopped deleting this reward box")
                            openRewardBoxes(p)
                        })
                    } else if (t == ClickType.SHIFT_LEFT) {
                        p.getInventory().addItem(box.createItemStack(true, true))
                    }
                },
                { Player p, ClickType t, int s -> },
                { Player p, ClickType t, int s -> },
        ])

        menu.set(menu.get().size - 1, FastItemUtils.createItem(Material.BARRIER, "§c§lClose", [], false), { p, t, s ->
            p.closeInventory()
        })

        menu.set(menu.get().size - 7, FastItemUtils.createItem(Material.BEACON, "§a§lCreate", [], false), { p, t, s ->
            p.closeInventory()

            PromptUtils.prompt(p, "§3Enter a Name", { name ->
                def box = getRewardBox(name, false)

                if (box != null) {
                    p.sendMessage("§cA reward box with that name already exists.")
                    return
                }

                box = getRewardBox(name)
                box.setItemName("§a$name")
                box.queueSave()

                openBoxEditor(p, box)
            })
        })

        menu.openSync(player)
    }

    static def openBoxEditor(Player player, RewardBox box) {
        MenuBuilder menu = new MenuBuilder(18, "§a§lEditing §3${box.getId()}")

        menu.set(0, FastItemUtils.createItem(Material.BOOK, "§a§lName", ["§7${box.getItemName()}"], false), { p, t, s ->
            p.closeInventory()

            PromptUtils.prompt(player, "§3Enter a Name", { name ->
                box.setItemName(name.replace('&', '§'))
                box.queueSave()
                openBoxEditor(player, box)
            })
        })

        //REDO
        menu.set(1, FastItemUtils.createItem(Material.PAPER, "§a§lLore", ["§7${box.getItemLore().join("\n")}"], false), { p, t, s ->
            p.closeInventory()

            PromptUtils.prompt(p, "§3Enter a Lore", { lore ->
                box.setItemLore(lore.split("\n").toList())
                box.queueSave()
                openBoxEditor(player, box)
            })
        })

        menu.set(2, FastItemUtils.createItem(box.getItemMaterial(), "§a§lMaterial", ["§7${box.getItemMaterial()}"], false), { p, t, s ->
            SelectionUtils.selectMaterial(player, { material ->
                box.setItemMaterial(material)
                box.queueSave()
                openBoxEditor(player, box)
            })
        })

        menu.set(3, FastItemUtils.createItem(Material.PAPER, "§a§lMin Rewards", ["§7${box.getMinRewards()}"], false), { p, t, s ->
            SelectionUtils.selectInteger(player, { amount ->
                box.setMinRewards(amount)
                box.queueSave()
                openBoxEditor(player, box)
            })
        })

        menu.set(4, FastItemUtils.createItem(Material.PAPER, "§a§lMax Rewards", ["§7${box.getMaxRewards()}"], false), { p, t, s ->
            SelectionUtils.selectInteger(player, { amount ->
                box.setMaxRewards(amount)
                box.queueSave()
                openBoxEditor(player, box)
            })
        })

        menu.set(5, FastItemUtils.createItem(Material.PAPER, "§a§lFinal Reward Amount", ["§7${box.getFinalRewardAmount()}"], false), { p, t, s ->
            SelectionUtils.selectInteger(player, { amount ->
                box.setFinalRewardAmount(amount)
                box.queueSave()
                openBoxEditor(player, box)
            })
        })

        menu.set(6, FastItemUtils.createItem(Material.ENDER_CHEST, "§a§lActive Box", ["§7${box.isEnabled()}"], false), { p, t, s ->
            SelectionUtils.selectBoolean(player, { active ->
                box.setEnabled(active)
                box.queueSave()
                openBoxEditor(player, box)
            })
        })

        menu.set(7, FastItemUtils.createItem(Material.ENDER_CHEST, "§a§lDisplay Box", ["§7${box.isDisplayBox()}"], false), { p, t, s ->
            SelectionUtils.selectBoolean(player, { display ->
                box.setDisplayBox(display)
                box.queueSave()
                openBoxEditor(player, box)
            })
        })

        def lore = [
                "§aThe §bloot table §ais what determines the §brewards",
                "§athat can be obtained from this §breward box§a.",
        ]

        if (box.lootTableId != null) {
            lore.add("")
            lore.add("§aclick to view")
            lore.add("§aright-click to assign")
            lore.add("")
            lore.add("§aCurrent: §b${box.getLootTable().getName()}")
        } else {
            lore.add("")
            lore.add("§7click to assign")
        }

        menu.set(8, FastItemUtils.createItem(Material.NETHERITE_INGOT, "§a§lLoot-Table", lore, false), { p, t, s ->
            if (box.lootTableId != null) {
                if (t == ClickType.RIGHT || t == ClickType.SHIFT_RIGHT) {
                    LootTableHandler.openCategories(p, 1, {
                        if (it == null) {
                            openBoxEditor(p, box)
                        } else {
                            box.lootTableId = it.getId()
                            box.queueSave()
                            openBoxEditor(p, box)
                        }
                    })
                } else {
                    LootTableHandler.openTableGui(p, box.getLootTable().getParentCategory(), box.getLootTable(), 1, {
                        openBoxEditor(p, box)
                    }, {
                        box.queueSave()
                    })
                }
            } else {
                LootTableHandler.openCategories(p, 1, {
                    if (it == null) {
                        openBoxEditor(p, box)
                    } else {
                        box.lootTableId = it.getId()
                        box.queueSave()
                        openBoxEditor(p, box)
                    }
                })
            }
        })

        menu.set(17, FastItemUtils.createItem(Material.RED_DYE, "§cBack", [], false), { p, t, s ->
            box.queueSave()
            openRewardBoxes(p)
        })

        menu.openSync(player)
    }

    static NamespacedKey REWARD_KEY = new NamespacedKey(Starlight.plugin, "rb_reward")

    static Collection<RewardBox> getRewardBoxes() {
        return StringDataManager.getAllData(RewardBox.class)
    }

    static RewardBox getRewardBox(String id, boolean create = true) {
        return StringDataManager.getData(id, RewardBox.class, create)
    }

    static RewardBox removeBox(String id) {
        return StringDataManager.removeOne(id, RewardBox.class)
    }

    static RewardBox getBoxFromItem(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return null

        return getRewardBox(DataUtils.getTag(stack, rewardBoxKey, PersistentDataType.STRING))
    }

}