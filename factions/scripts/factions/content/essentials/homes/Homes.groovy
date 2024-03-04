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
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.relation.RelationType
import scripts.factions.data.DataManager
import scripts.factions.data.obj.Position
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
                Player player = ctx.sender() as Player
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
                        ctx.reply("§cThis home is not in a valid world.")
                        return
                    }

                    Faction facAt = Factions.getFactionAt(home.position.getLocation(null))

                    if (facAt.getRelation(Factions.getMember(player.getUniqueId()).factionId).type.isAtMost(RelationType.TRUCE) && facAt.id != Factions.wildernessId && facAt.id != Factions.warZoneId) {
                        player.sendMessage("§cThis home is no longer in friendly claims - removing home.")
                        DataManager.getByClass(Home).delete(home.id)
                        getHomes(player).remove(getHome(player, home.displayName))
                        home.queueSave()
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

        Commands.create().assertUsage("[homeName]").handler { ctx ->
            Player player = ctx.sender() as Player
            if (ctx.args().size() == 0) {
                player.sendMessage("§cPlease set a home with a name.")
                return
            }
            String name = ctx.arg(0).parseOrFail(String)

            Faction facAt = Factions.getFactionAt(player.location)

            if (facAt.getRelation(Factions.getMember(player.getUniqueId()).factionId).type.isAtMost(RelationType.TRUCE) && facAt.id != Factions.wildernessId && facAt.id != Factions.warZoneId) {
                player.sendMessage("§cYou may not set a home in ${facAt.name}'s claims.")
                return
            }

            def home = new Home(player, "${player.getUniqueId()}_${name}_${new Date(System.currentTimeMillis()).toString()}")
            home.displayName = name
            home.position = Position.of(player.location)
            if (playerHomes.containsKey(home.playerId)) {
                playerHomes.get(home.playerId).add(home)
                DataManager.getData(home.displayName, Home, true)
            }
            else {
                def list = new ArrayList<Home>()
                list.add(home)
                playerHomes.put(home.playerId, list)
                DataManager.getData(home.displayName, Home, true)
            }

            home.queueSave()
            player.sendMessage("§7Home '§e${home.displayName}§7' created.")
        }.register("sethome")

        Commands.create().assertUsage("[homeName]").handler { ctx ->
            Player player = ctx.sender() as Player
            if (ctx.args().size() == 0) {
                player.sendMessage("§cPlease set a home with a valid name.")
                return
            }
            String name = ctx.arg(0).parseOrFail(String)
            Home home = getHome(player, name)
            if (home == null) {
                player.sendMessage("§cHome '${name}' not found.")
                return
            }
            DataManager.getByClass(Home).delete(home.id)
            getHomes(player).remove(getHome(player, home.displayName))
            player.sendMessage("§7Home '§e${home.displayName}§7' deleted.")
            home.queueSave()
        }.register("delhome", "deletehome")
    }

    Home getHome(Player player, String name) {
        if (name == null) return null
        for (Home home : playerHomes.get(player.getUniqueId())) {
            if (home.displayName.trim().toLowerCase() == name.trim().toLowerCase()) return home
        }
        return null
    }

    List<Home> getHomes(Player player) {
        return playerHomes.get(player.getUniqueId()) == null ? new ArrayList<Home>() : playerHomes.get(player.getUniqueId())
    }

    private static NamespacedKey homeKey = new NamespacedKey(Starlight.plugin, "home")

    def openHomeGui(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("§3Homes", getHomes(player), { Home home, int index ->
            List<String> lore = ["§r${home.position.x.toInteger()}, ${home.position.z.toInteger()}"]

            lore.add("§7§oRight click to edit.")

            def item = FastItemUtils.createItem(home.icon, "§b" + home.displayName, lore, false)

            DataUtils.setTag(item, homeKey, PersistentDataType.STRING, home.displayName)

            return item
        }, page, false, [
                { Player p, ClickType t, int slot ->
                    def item = menu.get().getItem(slot)

                    if (item == null || item.type.isAir() || !DataUtils.hasTag(item, homeKey, PersistentDataType.STRING)) return

                    def home = getHome(p, DataUtils.getTag(item, homeKey, PersistentDataType.STRING))
                    if (home == null) return

                    if (t == ClickType.RIGHT || t == ClickType.SHIFT_RIGHT) {
                        openHomeEdit(p, home)
                        return
                    }

                    p.closeInventory()

                    if (home.position.world == null) {
                        p.sendMessage("§cThis home is invalid - deleting home.")
                        getHomes(player).remove(home)
                        DataManager.getByClass(Home).delete(home.id)
                        return
                    }

                    TeleportHandler.teleportPlayer(p,
                            home.position.getLocation(null),
                            home.warpTime,
                            false,
                            "§3Teleporting to §b${home.displayName}§3..."
                    )
                },
                { Player p, ClickType t, int slot ->
                    openHomeGui(p, page + 1)
                },
                { Player p, ClickType t, int slot ->
                    openHomeGui(p, page - 1)
                },
        ])

        menu.openSync(player)
    }

    def openHomeEdit(Player player, Home home) {
        MenuBuilder menu

        menu = new MenuBuilder(18, "§3Editing §b${home.displayName}")

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.BARRIER, "§cDelete Home", [
                "§7Click to delete this home."
        ]), { p, t, s ->
            DataManager.getByClass(Home).delete(home.id)
            getHomes(p).remove(home)

            openHomeGui(p)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.NAME_TAG, "§bEdit Name", [
                "§7Click to edit the home name.",
                "",
                "§7Current: §b${home.displayName}"
        ]), { p, t, s ->
            SelectionUtils.selectString(p, "§3Enter the new home name.") {
                home.displayName = it.replaceAll("&", "§")
                home.queueSave()

                openHomeEdit(p, home)
            }
        })


        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.ITEM_FRAME, "§bEdit Icon", [
                "§7Click to edit the home icon.",
                "",
                "§7Current: §b${home.icon.toString()}"
        ]), { p, t, s ->
            SelectionUtils.selectMaterial(p) {
                home.icon = it
                home.queueSave()

                openHomeEdit(p, home)
            }
        })

        menu.set(17, FastItemUtils.createItem(Material.RED_DYE, "§cBack", [
                "§7Click to go back."
        ]), { p, t, s ->
            openHomeGui(p)
        })

        menu.openSync(player)
    }
}
