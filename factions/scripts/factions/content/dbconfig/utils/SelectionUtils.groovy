package scripts.factions.content.dbconfig.utils

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.world.World
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.starcade.starlight.helper.utils.Players
import scripts.factions.data.obj.CL
import scripts.factions.data.obj.Position
import scripts.factions.data.obj.SR
import scripts.factions.util.PromptUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.IntegerUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.SignUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.systems.MenuEvent
import scripts.shared.utils.ItemType
import scripts.shared.utils.MenuDecorator
import scripts.shared.utils.PluginUtils
import scripts.shared3.utils.Callback

@CompileStatic(TypeCheckingMode.SKIP)
class SelectionUtils {
    static def selectAB(Player player, String title, String aName = "A", String bName = "B", Closure callbackA, Closure callbackB) {
        MenuBuilder builder = new MenuBuilder(9, "§7Select §aA§7/§cB")

        MenuDecorator.decorate(builder, ["333333333"])

        builder.set(1, 4, FastItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "§c${aName}", [], false), { p, t, s ->
            callbackA.call()
        })

        builder.set(1, 6, FastItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "§a${bName}", [], false), { p, t, s ->
            callbackB.call()
        })

        builder.openSync(player)
    }

    static def selectBoolean(Player player, Callback<Boolean> callback) {
        selectAB(player, "§7Select §aTrue§7/§cFalse", "§cFalse", "§aTrue", { callback.exec(true) }, { callback.exec(false) })
    }

    static def selectInteger(Player player, String title, List<Integer> defaultValues = [1, 5, 25, 50, 100, 250, 500, 1000], Callback<Integer> callback) {
        MenuBuilder builder = new MenuBuilder(9, title)

        for (int i = 0; i < 9; i++) {
            def newValue = defaultValues[i]
            builder.set(i, FastItemUtils.createItem(Material.PAPER, "§3${newValue}", [], false), { p, t, s ->
                callback.exec(newValue)
            })
        }

        builder.set(1, 9, FastItemUtils.createItem(Material.OAK_SIGN, "§aChoose a Value", [], false), { p, t, s ->
            SignUtils.openSign(player, ["", "^ ^ ^", "Enter Amount", ""], { String[] lines, Player p2 ->
                IntegerUtils.IntegerParseResult result = IntegerUtils.parseInt(lines[0])

                // allow for -1?
                if (!result.isPositive()) {
                    Players.msg(player, "§] §> §e${lines[0]} §fis not a valid amount!")
                    return
                }

                int amount = result.getValue()

                callback.exec(amount)
            })
        })

        builder.openSync(player)
    }

    static def selectPositionList(Player player, List<Position> defaultValues = [], Callback<List<Position>> callback) {
        MenuBuilder builder = new MenuBuilder(9, "§7Select a Position")

        for (int i = 0; i < 9; i++) {
            if (i < defaultValues.size()) {
                def position = defaultValues[i]
                builder.set(i, FastItemUtils.createItem(Material.PAPER, "§3${position.world} §7- §3${position.x} §7- §3${position.y} §7- §3${position.z}", [], false), { p, t, s ->
                    callback.exec(defaultValues)
                })
            } else {
                builder.set(i, FastItemUtils.createItem(Material.BARRIER, "§cEmpty", ["Click to set Position"], false), { p, t, s ->
                    callback.exec(defaultValues)
                })
            }
        }

        builder.openSync(player)
    }

    static def selectInteger(Player player, Callback<Integer> callback) {
        selectInteger(player, "§7Select an §bInteger", callback)
    }

    static def selectDouble(Player player, String title, List<Double> defaultValues = [1.0D, 5.0D, 25.0D, 50.0D, 100.0D, 250.0D, 500.0D, 1000.0D], Callback<Double> callback) {
        MenuBuilder builder = new MenuBuilder(9, title)

        for (int i = 0; i < 9; i++) {
            def newValue = defaultValues[i]
            builder.set(i, FastItemUtils.createItem(Material.PAPER, "§3${newValue}", [], false), { p, t, s ->
                callback.exec(newValue)
            })
        }

        builder.set(1, 9, FastItemUtils.createItem(Material.OAK_SIGN, "§aChoose a Value", [], false), { p, t, s ->
            SignUtils.openSign(player, ["", "^ ^ ^", "Enter Amount", ""], { String[] lines, Player p2 ->
                Double result = Double.parseDouble(lines[0])

                if (result == null || result < 0) {
                    Players.msg(player, "§] §> §e${lines[0]} §fis not a valid amount!")
                    return
                }

                callback.exec(result)
            })
        })

        builder.openSync(player)
    }

    static def selectDouble(Player player, Callback<Double> callback) {
        selectDouble(player, "§7Select a §bDouble", callback)
    }

    static def selectString(Player player, String msg = "§3Enter a String", Callback<String> callback) {
        player.closeInventory()
        PromptUtils.prompt(player, msg, callback)
    }

    static SR getSelection(Player player) {
        World adaptedWorld = BukkitAdapter.adapt(player.getWorld())
        def worldEditPlugin = PluginUtils.get("WorldEdit") as WorldEditPlugin
        def selection = worldEditPlugin.getSession(player).getRegionSelector(adaptedWorld).getIncompleteRegion()
        if (selection == null) {
            return null
        }
        if (adaptedWorld == null) return null

        def ret = new SR()
        ret.world = adaptedWorld.getName()

        def min = selection.getMinimumPoint()
        if (min != null) {
            ret.x1 = min.getX()
            ret.y1 = min.getY()
            ret.z1 = min.getZ()
        }

        def max = selection.getMaximumPoint()
        if (max != null) {
            ret.x2 = max.getX()
            ret.y2 = max.getY()
            ret.z2 = max.getZ()
        }

        ret.reorder()

        return ret
    }

    static def confirmRegion(Player player, SR sr, Callback<Boolean> callback) {
        MenuBuilder builder = new MenuBuilder(9, "§7Confirm Region")

        MenuDecorator.decorate(builder, ["333333333"])

        builder.set(1, 4, FastItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "§cCancel", [], false), { p, t, s ->
            callback.exec(false)
        })

        builder.set(1, 5, FastItemUtils.createItem(Material.PAPER, "§3World: §a${sr.world}", [
                "§3X1: §a${sr.x1}",
                "§3Y1: §a${sr.y1}",
                "§3Z1: §a${sr.z1}",
                "",
                "§3X2: §a${sr.x2}",
                "§3Y2: §a${sr.y2}",
                "§3Z2: §a${sr.z2}",
        ], false))

        builder.set(1, 6, FastItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "§aConfirm", [], false), { p, t, s ->
            callback.exec(true)
        })

        builder.openSync(player)
    }

    static def selectRegionList(Player player, List<SR> startValues, int page = 1, Callback<List<SR>> callback) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§3§lSelect a Region", startValues, { SR region, Integer i ->
            def item = FastItemUtils.createItem(Material.PAPER, "§3World: §a${region.world}", [
                    "§3X1: §a${region.x1}",
                    "§3Y1: §a${region.y1}",
                    "§3Z1: §a${region.z1}",
                    "",
                    "§3X2: §a${region.x2}",
                    "§3Y2: §a${region.y2}",
                    "§3Z2: §a${region.z2}",
            ], false)

            FastItemUtils.addGlow(item)

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    def selection = startValues.get(s)
                    def newSelection = getSelection(p)

                    if (selection != null && newSelection != null) {
                        MenuUtils.createConfirmMenu(p, "§3Replace §a${selection.world} §3with §a${newSelection.world}?", FastItemUtils.createItem(Material.BARRIER, "§3Change Region?", [
                                "§3World: §a${newSelection.world}",
                                "§3X1: §a${newSelection.x1}",
                                "§3Y1: §a${newSelection.y1}",
                                "§3Z1: §a${newSelection.z1}",
                                "",
                                "§3X2: §a${newSelection.x2}",
                                "§3Y2: §a${newSelection.y2}",
                                "§3Z2: §a${newSelection.z2}",
                        ], false), {
                            startValues.set(s, newSelection)
                            selectRegionList(p, startValues as List<SR>, page, callback)
                        }, {
                            selectRegionList(p, startValues, page, callback)
                        })
                    }
                },
                { Player p, ClickType t, int s -> selectRegionList(p, startValues, page + 1, callback) },
                { Player p, ClickType t, int s -> selectRegionList(p, startValues, page - 1, callback) },
        ])

        menu.set(menu.get().size() - 6, FastItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "§aAdd", [], false), { Player p, ClickType t, int s ->
            def selection = getSelection(p)
            if (selection == null) {
                Players.msg(p, "§cYou must make a selection first!")
                return
            }

            startValues.add(selection)
            selectRegionList(p, startValues as List<SR>, page, callback)
        })

        menu.set(menu.get().size() - 4, FastItemUtils.createItem(Material.GREEN_DYE, "§aConfirm", [], false), { Player p, ClickType t, int s ->
            callback.exec(startValues as List<SR>)
        })

        menu.openSync(player)
    }

    static def confirmChunk(Player player, CL cl, Callback<Boolean> callback) {
        MenuBuilder builder = new MenuBuilder(9, "§7Confirm Chunk")

        MenuDecorator.decorate(builder, ["333333333"])

        builder.set(1, 4, FastItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "§cCancel", [], false), { p, t, s ->
            callback.exec(false)
        })

        builder.set(1, 5, FastItemUtils.createItem(Material.PAPER, "§3World: §a${cl.worldName}", [
                "§3Chunk X: §a${cl.x}",
                "§3Chunk Z: §a${cl.z}",
                "",
                "§3X CORNER: §a${cl.x * 16}",
                "§3Z CORNER: §a${cl.z * 16}",
        ], false))

        builder.set(1, 6, FastItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "§aConfirm", [], false), { p, t, s ->
            callback.exec(true)
        })

        builder.openSync(player)
    }

    static def confirmPosition(Player player, Position position, Callback<Boolean> callback) {
        MenuBuilder builder = new MenuBuilder(9, "§7Confirm Position")

        MenuDecorator.decorate(builder, ["333333333"])

        builder.set(1, 4, FastItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "§cCancel", [], false), { p, t, s ->
            callback.exec(false)
        })

        builder.set(1, 5, FastItemUtils.createItem(Material.PAPER, "§3World: §a${position.world}", [
                "§3X: §a${position.x}",
                "§3Y: §a${position.y}",
                "§3Z: §a${position.z}",
        ] as List<String>, false))

        builder.set(1, 6, FastItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "§aConfirm", [], false), { p, t, s ->
            callback.exec(true)
        })

        builder.openSync(player)
    }

    static def selectMaterial(Player player, List<Material> materials = null, int page = 1, Callback<Material> callback) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§a§lSelect a Material", materials == null ? Material.values().findAll { it != Material.AIR } : materials, { Material material, Integer i ->
            return FastItemUtils.createItem(material, "§3${material?.name() ?: "error"}", [], false)
        }, page, false, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)

                    if (item == null || item.type == Material.AIR) return

                    callback.exec(item.type)
                },
                { Player p, ClickType t, int s ->
                    selectMaterial(p, materials, page + 1, callback)
                },
                { Player p, ClickType t, int s ->
                    selectMaterial(p, materials, page - 1, callback)
                },
        ] as List<MenuEvent>)

        menu.open(player)
    }

    static def selectItemType(Player player, int page = 0, Callback<ItemType> callback) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§a§lSelect an Item-Type", ItemType.values().toList(), { ItemType itemType, Integer i ->
            return FastItemUtils.createItem(itemType.icon, "§3${itemType.displayName ?: "error"}", [], false)
        }, page, false, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)

                    if (item == null || item.type == Material.AIR) return

                    callback.exec(ItemType.getTypeOf(item))
                },
                { Player p, ClickType t, int s ->
                    selectItemType(p, page + 1, callback)
                },
                { Player p, ClickType t, int s ->
                    selectItemType(p, page - 1, callback)
                },
        ] as List<MenuEvent>)

        menu.open(player)
    }

    static def selectItemTypes(Player player, int page = 1, Callback<List<ItemType>> callback) {
        MenuBuilder menu

        List<ItemType> selections = []

        menu = MenuUtils.createPagedMenu("§a§lSelect a Material", ItemType.values().toList(), { ItemType itemType, Integer i ->
            return FastItemUtils.createItem(itemType.icon, "§3${itemType.displayName ?: "error"}", [], false)
        }, page, false, [
                { Player p, ClickType t, int s ->
                    def item = menu.get().getItem(s)

                    if (item == null || item.type == Material.AIR) return

                    def itemType = ItemType.getTypeOf(item)
                    if (itemType == null) return

                    if (selections.contains(itemType)) {
                        selections.remove(itemType)
                    } else {
                        selections.add(itemType)
                    }

                    menu.refresh(p)
                },
                { Player p, ClickType t, int s ->
                    selectItemTypes(p, page + 1, callback)
                },
                { Player p, ClickType t, int s ->
                    selectItemTypes(p, page - 1, callback)
                },
        ] as List<MenuEvent>)

        menu.set(menu.get().size() - 1, FastItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "§aConfirm", [], false), { Player p, ClickType t, int s ->
            callback.exec(selections)
        })

        menu.open(player)
    }

    static def selectStringList(Player player, List<String> startValues, int page = 1, Callback<List<String>> callback) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§a§lSelect a String", startValues, { String string, Integer i ->
            def item = FastItemUtils.createItem(Material.PAPER, "§3${string}", ["§3click to change"], false)

            FastItemUtils.addGlow(item)

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    PromptUtils.prompt(p, "§3Enter a String", { String string ->
                        startValues.set(s, string.replace('&', '§'))
                        selectStringList(p, startValues as List<String>, page, callback)
                    })
                },
                { Player p, ClickType t, int s -> selectStringList(p, startValues, page + 1, callback)},
                { Player p, ClickType t, int s -> selectStringList(p, startValues, page - 1, callback)},
        ])

        menu.set(menu.get().size() - 6, FastItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "§aAdd", [], false), { Player p, ClickType t, int s ->
            PromptUtils.prompt(p, "§3Enter a String", { String string ->
                startValues.add(string.replace('&', '§'))
                selectStringList(p, startValues as List<String>, page, callback)
            })
        })

        menu.set(menu.get().size() - 4, FastItemUtils.createItem(Material.GREEN_DYE, "§aConfirm", [], false), { Player p, ClickType t, int s ->
            callback.exec(startValues as List<String>)
        })

        menu.openSync(player)
    }
}

