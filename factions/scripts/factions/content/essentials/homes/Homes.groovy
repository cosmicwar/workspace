package scripts.factions.content.essentials.homes

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Schedulers
import scripts.factions.content.dbconfig.utils.SelectionUtils
import scripts.factions.content.essentials.tp.TeleportHandler
import scripts.factions.content.essentials.warp.Warp
import scripts.factions.data.DataManager
import scripts.factions.features.spawners.CSpawner
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.DataUtils

import java.util.concurrent.ConcurrentHashMap

class Homes {

    ConcurrentHashMap<UUID, List<Home>> playerHomes = new ConcurrentHashMap<>()

    Homes() {
        GroovyScript.addUnloadHook {
            DataManager.getByClass(Home).saveAll(false)
        }

        GroovyScript.addScriptHook(GroovyScript.HookType.RECOMPILE, {
            DataManager.getByClass(Home.class).saveAll(false)
        })

        DataManager.register("ess_homes", Home)

        Schedulers.sync().runLater({
            getAllHomes().forEach { home ->
                if (home == null) return

                def loc = home.position
                if (loc == null || home.playerId == null) {
                    DataManager.removeOne(home.id, Home)
                }

                if (playerHomes.containsKey(home.playerId)) playerHomes.get(home.playerId).add(home)
                else {
                    def list = new ArrayList<Home>()
                    list.add(home)
                    playerHomes.put(home.playerId, list)
                }
            }
        }, 1L)

        commands()
    }

    static Collection<Home> getAllHomes() {
        DataManager.getAllData(Home)
    }

    void commands() {
        Commands.create().assertUsage("[home]").handler { ctx ->
            if (ctx.sender() instanceof Player) {
                if (ctx.args().size() == 0) {
                    openHomeGui(ctx.sender() as Player)
                    return
                } else if (ctx.args().size() == 1) {
                    def home = getHome(ctx.sender() as Player, ctx.arg(0).parseOrFail(String))
                    if (home == null) {
                        ctx.reply("§cHome not found.")
                        return
                    }

                    if (home.position.world == null) {
                        ctx.reply("§cThis warp is not set up correctly.")
                        return
                    }

                    TeleportHandler.teleportPlayer(ctx.sender() as Player,
                            home.position.getLocation(null),
                            home.warpTime,
                            false,
                            "§3Teleporting to §b${home.displayName}§3..."
                    )
                    return
                }
            }
        }.register("home", "homes")
    }

    Home getHome(Player player, String name) {
        if (name == null) return null
        for (Home home : playerHomes.get(player.getUniqueId())) {
            if (home.displayName.trim().toLowerCase() == name.trim().toLowerCase()) return home
        }
        return null
    }

    List<Home> getHomes(Player player) {
        return playerHomes.get(player.getUniqueId())
    }

    private static NamespacedKey homeKey = new NamespacedKey(Starlight.plugin, "home")

    def openHomeGui(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§3Warps", getHomes(player), { Home home, int index ->
            List<String> lore = warp.description.clone() as List<String>


            lore.add("§7§oRight click to edit.")

            def item = FastItemUtils.createItem(home.icon, "§b" + home.displayName, lore, false)

            DataUtils.setTag(item, homeKey, PersistentDataType.STRING, home.displayName)

            return item
        }, page, false, [
                { Player p, ClickType t, int slot ->
                    def item = menu.get().getItem(slot)

                    if (item == null || item.type.isAir() || !DataUtils.hasTag(item, warpKey, PersistentDataType.STRING)) return

                    def home = getWarp(DataUtils.getTag(item, warpKey, PersistentDataType.STRING), false)
                    if (warp == null) return

                    if (t == ClickType.RIGHT || t == ClickType.SHIFT_RIGHT) {
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
                    openHomeGui(p, page + 1)
                },
                { Player p, ClickType t, int slot ->
                    openHomeGui(p, page - 1)
                },
        ])

        if (player.isOp()) {
            menu.set(menu.get().size - 4, FastItemUtils.createItem(Material.GREEN_DYE, "§cCreate Warp", [
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
}
