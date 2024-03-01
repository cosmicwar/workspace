package scripts.factions.content.essentials.warp

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import scripts.factions.content.dbconfig.utils.SelectionUtils
import scripts.factions.content.essentials.tp.TeleportHandler
import scripts.factions.core.faction.FCBuilder
import scripts.factions.data.DataManager
import scripts.factions.data.obj.Position
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.DataUtils

class Warps {

    Warps() {
        GroovyScript.addUnloadHook {
            DataManager.getByClass(Warp).saveAll(false)
        }

        DataManager.register("ess_warps", Warp)

        commands()
    }

    static Warp getWarp(String name, boolean create = true) {
        return DataManager.getData(name, Warp, create)
    }

    static Collection<Warp> getWarps() {
        return DataManager.getAllData(Warp)
    }

    def commands() {
        FCBuilder cmd = new FCBuilder("warp", "warps").defaultAction {
            openWarpGui(it)
        }

        cmd.build()
    }

    private static NamespacedKey warpKey = new NamespacedKey(Starlight.plugin, "warp")

    def openWarpGui(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§3Warps", getWarps().toList(), { Warp warp, int index ->
            def lore = warp.description

            if (player.isOp()) {
                lore.add("")
                lore.add("§7§oRight click to edit.")
            }

            def item = FastItemUtils.createItem(warp.icon, "§b" + warp.displayName, lore, false)

            DataUtils.setTag(item, warpKey, PersistentDataType.STRING, warp.id)

            return item
        }, page, false, [
                { Player p, ClickType t, int slot ->
                    def item = menu.get().getItem(slot)

                    if (item == null || item.type.isAir() || !DataUtils.hasTag(item, warpKey, PersistentDataType.STRING)) return

                    def warp = getWarp(DataUtils.getTag(item, warpKey, PersistentDataType.STRING), false)
                    if (warp == null) return

                    if (p.isOp() && (t == ClickType.RIGHT || t == ClickType.SHIFT_RIGHT)) {
                        openWarpEdit(p, warp)
                        return
                    }

                    p.closeInventory()

                    if (warp.position.world == null) {
                        p.sendMessage("§cThis warp is not set up correctly.")
                        return
                    }

                    TeleportHandler.teleportPlayer(p,
                            warp.position.getLocation(null),
                            warp.warpTime,
                            false,
                            "§3Teleporting to §b${warp.displayName}§3..."
                    )
                },
                { Player p, ClickType t, int slot ->
                    openWarpGui(p, page + 1)
                },
                { Player p, ClickType t, int slot ->
                    openWarpGui(p, page - 1)
                },
        ])

        if (player.isOp()) {
            menu.set(menu.get().size - 4, FastItemUtils.createItem(Material.RED_DYE, "§cCreate Warp", [
                "§7Click to create a new warp."
            ]), { p, t, s ->
                SelectionUtils.selectString(p, "§3Enter the new warp name.") {
                    def warp = getWarp(it)
                    warp.displayName = it
                    warp.queueSave()

                    openWarpEdit(p, warp)
                }
            })
        }

        menu.openSync(player)
    }

    def openWarpEdit(Player player, Warp warp) {
        MenuBuilder menu

        menu = new MenuBuilder(18, "§3Editing §b${warp.displayName}")

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.BARRIER, "§cDelete Warp", [
            "§7Click to delete this warp."
        ]), { p, t, s ->
            DataManager.getByClass(Warp).delete(warp.id)

            openWarpGui(p)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.NAME_TAG, "§bEdit Name", [
            "§7Click to edit the warp name."
        ]), { p, t, s ->
            SelectionUtils.selectString(p, "§3Enter the new warp name.") {
                warp.displayName = it
                warp.queueSave()

                openWarpEdit(p, warp)
            }
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.PAPER, "§bEdit Description", [
            "§7Click to edit the warp description."
        ]), { p, t, s ->
            SelectionUtils.selectStringList(p, warp.description) {
                warp.description = it
                warp.queueSave()

                openWarpEdit(p, warp)
            }
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.ITEM_FRAME, "§bEdit Icon", [
            "§7Click to edit the warp icon."
        ]), { p, t, s ->
            SelectionUtils.selectMaterial(p) {
                warp.icon = it
                warp.queueSave()

                openWarpEdit(p, warp)
            }
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.COMPASS, "§bEdit Position", [
            "§7Click to edit the warp position."
        ]), { p, t, s ->
            SelectionUtils.confirmPosition(p, Position.of(p.getLocation())) {
                if (it) {
                    warp.position = Position.of(p.getLocation())
                    warp.queueSave()
                }

                openWarpEdit(p, warp)
            }
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.CLOCK, "§bEdit Warp Time", [
            "§7Click to edit the warp time."
        ]), { p, t, s ->
            SelectionUtils.selectDouble(p, "", [0,1,2,3,4,5,6,7]) {
                warp.warpTime = it
                warp.queueSave()

                openWarpEdit(p, warp)
            }
        })

        menu.set(17, FastItemUtils.createItem(Material.RED_DYE, "§cBack", [
            "§7Click to go back."
        ]), { p, t, s ->
            openWarpGui(p)
        })

        menu.openSync(player)
    }

}
