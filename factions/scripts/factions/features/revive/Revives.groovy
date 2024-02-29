package scripts.factions.features.revive

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players
import scripts.factions.content.log.Logs
import scripts.factions.content.log.v2.api.Log
import scripts.factions.content.log.v2.api.LogType
import scripts.factions.data.obj.Position
import scripts.factions.data.uuid.UUIDDataManager
import scripts.factions.features.revive.obj.BukkitInventorySnapshot
import scripts.factions.features.revive.obj.InventorySnapshot
import scripts.factions.features.revive.obj.ReviveSortType
import scripts.factions.features.revive.obj.SlotSnapshot
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.DataUtils
import scripts.shared.utils.MojangAPI

import java.text.SimpleDateFormat

@CompileStatic(TypeCheckingMode.SKIP)
class Revives {

    Revives() {
        GroovyScript.addUnloadHook {
            UUIDDataManager.getByClass(ReviveData).saveAll(false)
        }

        UUIDDataManager.register("revive_data", ReviveData)

        commands()
        events()
    }

    static def events() {
        Events.subscribe(PlayerDeathEvent.class).handler { event ->
            if (event.isCancelled()) return

            def player = event.getEntity()
            def data = UUIDDataManager.getData(player, ReviveData)

            UUID killerId = null
            if (event.getEntity().getKiller() != null) {
                killerId = event.getEntity().getKiller().getUniqueId()
            }

            String deathCause = event.getDeathMessage()
            if (event.getEntity().getLastDamageCause() != null) {
                deathCause = event.getEntity().getLastDamageCause().getCause().name()
            }

            def snapshot = new InventorySnapshot()

            def index = 0
            player.getInventory().contents.each {
                if (it == player.getInventory().getHelmet() || it == player.getInventory().getChestplate() || it == player.getInventory().getLeggings() || it == player.getInventory().getBoots() || it == player.getInventory().getItemInMainHand()) {
                    index++
                    return
                }
                if (it != null && it.type != Material.AIR) {
                    if (index >= 9 && index <= 17) {
                        snapshot.inventorySlots.add(new SlotSnapshot(FastItemUtils.convertItemStackToString(it), index + 18))
                    } else if (index >= 27 && index <= 35) {
                        snapshot.inventorySlots.add(new SlotSnapshot(FastItemUtils.convertItemStackToString(it), index - 18))
                    } else {
                        snapshot.inventorySlots.add(new SlotSnapshot(FastItemUtils.convertItemStackToString(it), index))
                    }
                }

                index++
            }

            def pHelm = player.getInventory().getHelmet()
            if (pHelm != null && pHelm.type != Material.AIR) snapshot.helmet = FastItemUtils.convertItemStackToString(pHelm)

            def pChestPlate = player.getInventory().getChestplate()
            if (pChestPlate != null && pChestPlate.type != Material.AIR) snapshot.chestPlate = FastItemUtils.convertItemStackToString(pChestPlate)

            def pLeggings = player.getInventory().getLeggings()
            if (pLeggings != null && pLeggings.type != Material.AIR) snapshot.leggings = FastItemUtils.convertItemStackToString(pLeggings)

            def pBoots = player.getInventory().getBoots()
            if (pBoots != null && pBoots.type != Material.AIR) snapshot.boots = FastItemUtils.convertItemStackToString(pBoots)

            def pHeldItem = player.getInventory().getItemInMainHand()
            if (pHeldItem != null && pHeldItem.type != Material.AIR) snapshot.heldItem = FastItemUtils.convertItemStackToString(pHeldItem)

            snapshot.position = new Position(player.location.world.name, player.location.x as int, player.location.y as int, player.location.z as int)

            snapshot.deathCause = deathCause
            snapshot.killedBy = killerId

            snapshot.timeStamp = System.currentTimeMillis()

            data.deaths.add(snapshot)
            data.queueSave()

            player.sendMessage("saved data")
        }
    }

    static def commands() {
        Commands.create().assertPlayer().assertOp().handler { ctx ->
            def player = ctx.sender()

            if (ctx.args().size() == 0) {
                Players.msg(player, "§c/revive <player>")
                return
            }

            def arg = ctx.arg(0).parseOrFail(String)

            if (arg == "dev/wipedb") {
                UUIDDataManager.getByClass(ReviveData).deleteAll()
                Players.msg(player, "§cDeleted all revive data.")
                return
            }

            def targetPlayer = Bukkit.getPlayer(arg)
            if (targetPlayer == null) {
                Players.msg(player, "§cPlayer not found. Checking db...")
                UUID targetId = MojangAPI.getUUID(arg)
                if (targetId == null) {
                    Players.msg(player, "§cPlayer not found.")
                    return
                }

                def data = UUIDDataManager.getData(targetId, ReviveData)
                if (data == null) {
                    Players.msg(player, "§cPlayer has no revive data.")
                    return
                }

                openReviveMenu(player, data)
            } else {
                def data = UUIDDataManager.getData(targetPlayer, ReviveData, false)
                if (data == null) {
                    Players.msg(player, "§cPlayer has no revive data.")
                    return
                }

                openReviveMenu(player, data)
            }
        }.register("revive")
    }

    private static NamespacedKey reviveKey = new NamespacedKey(Starlight.plugin, "reviveKey")

    static def openReviveMenu(Player player, ReviveData targetData, int page = 1) {
        MenuBuilder menu

        def dateFormatter = new SimpleDateFormat("MM/dd/yy HH:mm:ss")
        def targetName = Bukkit.getPlayer(targetData.getId())?.name ?: Bukkit.getOfflinePlayer(targetData.getId()).getName()

        def playerData = UUIDDataManager.getData(player, ReviveData)

        def snapshots = getSortedData(targetData, playerData)

        menu = MenuUtils.createPagedMenu("§cRevive ${targetName}", snapshots, { InventorySnapshot snapshot, Integer index ->
            def skull = FastItemUtils.createSkull(targetName, "§3snapshot", [
                    "§7Death Cause: §3${snapshot.deathCause}",
                    "§7Killed By: §c${snapshot.killedBy == null ? "Unknown" : Bukkit.getOfflinePlayer(snapshot.killedBy).getName()}",
                    "§7Died: §b${dateFormatter.format(new Date(snapshot.timeStamp))}",
                    "§7Times Revived: §e${snapshot.timesRevived}",
                    "§7Last Revived: §e${snapshot.lastRevive == null ? "Never" : dateFormatter.format(new Date(snapshot.lastRevive))}",
                    "§7Location: §a${snapshot.position.world} ${snapshot.position.x1} ${snapshot.position.y1} ${snapshot.position.z1}"
            ])

            DataUtils.setTag(skull, reviveKey, PersistentDataType.STRING, snapshot.snapshotId.toString())

            return skull
        }, page, false, [
                { Player p, ClickType t, Integer slot ->
                    def item = menu.get().getItem(slot)
                    if (item == null || item.type == Material.AIR) return

                    def snapshotId = DataUtils.getTag(item, reviveKey, PersistentDataType.STRING)
                    if (snapshotId == null) return

                    def snapshot = snapshots.find { it.snapshotId.toString() == snapshotId }

                    if (snapshot == null) {
                        Players.msg(p, "§cSnapshot not found.")
                        return
                    }

                    openSnapshot(p, targetData, snapshot)
                },
                { Player p, ClickType t, Integer slot -> },
                { Player p, ClickType t, Integer slot -> },
        ])

        menu.set(menu.get().size() - 4, FastItemUtils.createItem(Material.PAPER, "Sort Type", [
                "§7Current Sort Type: §e${playerData.sortType.name}",
                "§7Click to change."
        ], false), { p, t, s ->
            playerData.sortType = playerData.sortType.getNext()
            playerData.queueSave()

            openReviveMenu(p, targetData, page)
        })

        menu.openSync(player)
    }

    static def getSortedData(ReviveData targetData, ReviveData playerData) {
        def sorted = targetData.deaths.toList()
        switch (playerData.sortType) {
            case ReviveSortType.DEATH_TIME_OLDER_TO_NEW:
                sorted = sorted.sort { it.timeStamp }
                break
            case ReviveSortType.DEATH_TIME_NEWER_TO_OLD:
                sorted = sorted.sort { -it.timeStamp }
                break
            case ReviveSortType.TIMES_REVIVED_ASCENDING:
                sorted = sorted.sort { it.timesRevived }
                break
            case ReviveSortType.TIMES_REVIVED_DESCENDING:
                sorted = sorted.sort { -it.timesRevived }
                break
            case ReviveSortType.DEATH_CAUSE:
                sorted = sorted.sort { it.deathCause }
                break
            case ReviveSortType.KILLED_BY:
                sorted = sorted.sort { it.killedBy == null ? "Unknown" : Bukkit.getOfflinePlayer(it.killedBy).getName() }
                break
        }

        return sorted
    }

    static def openSnapshot(Player player, ReviveData targetData, InventorySnapshot snapshot) {
        MenuBuilder menu

        String targetName = Bukkit.getPlayer(targetData.getId())?.name ?: Bukkit.getOfflinePlayer(targetData.getId()).getName()
        def dateFormatter = new SimpleDateFormat("MM/dd/yy HH:mm:ss")

        menu = new MenuBuilder(54, "§cRevive ${targetName} Snapshot")

        def bukkitSnapshot = new BukkitInventorySnapshot(snapshot)
        bukkitSnapshot.inventorySlots.each { index, item ->
            if (index != null && item != null && item.type != Material.AIR) {
                menu.set(index, item)
            }
        }

        if (bukkitSnapshot.helmet != null) menu.set(5, 2, bukkitSnapshot.helmet)
        if (bukkitSnapshot.chestPlate != null) menu.set(5, 3, bukkitSnapshot.chestPlate)
        if (bukkitSnapshot.leggings != null) menu.set(5, 7, bukkitSnapshot.leggings)
        if (bukkitSnapshot.boots != null) menu.set(5, 8, bukkitSnapshot.boots)

        if (bukkitSnapshot.heldItem != null) menu.set(5, 5, bukkitSnapshot.heldItem)

        def glass = FastItemUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, "", [], false)
        menu.set(5, 1, glass)
        menu.set(5, 4, glass)
        menu.set(5, 6, glass)
        menu.set(5, 9, glass)

        menu.set(6, 1, FastItemUtils.createItem(Material.PAPER, "§7Snapshot Data", [
                "§7Death Cause: §3${snapshot.deathCause}",
                "§7Killed By: §c${snapshot.killedBy == null ? "Unknown" : Bukkit.getOfflinePlayer(snapshot.killedBy).getName()}",
                "§7Died: §b${dateFormatter.format(new Date(snapshot.timeStamp))}",
                "§7Times Revived: §e${snapshot.timesRevived}",
                "§7Last Revived: §e${snapshot.lastRevive == null ? "Never" : dateFormatter.format(new Date(snapshot.lastRevive))}",
                "§7Location: §a${snapshot.position.world} ${snapshot.position.x1} ${snapshot.position.y1} ${snapshot.position.z1}"
        ], false))

        menu.set(6, 5, FastItemUtils.createItem(Material.GREEN_STAINED_GLASS_PANE, "§aRevive", [], false), { p, t, s ->
            MenuUtils.createConfirmMenu(p, "Confirm", FastItemUtils.createItem(Material.PAPER, "Are you sure?", [
                    "§7You are about to revive ${targetName}.",
            ]), {
                snapshot.lastRevive = System.currentTimeMillis()
                snapshot.timesRevived++
                targetData.queueSave()

                def target = Bukkit.getPlayer(targetData.getId())
                if (target != null) {
                    if (bukkitSnapshot.helmet != null && (target.getInventory().getHelmet() == null || target.getInventory().getHelmet().type == Material.AIR)) {
                        target.getInventory().setHelmet(bukkitSnapshot.helmet)
                    } else if (bukkitSnapshot.helmet != null) {
                        FastInventoryUtils.addOrBox(targetData.getId(), null, p, bukkitSnapshot.helmet, "§cRevived by ${p.getName()}")
                    }

                    if (bukkitSnapshot.chestPlate != null && (target.getInventory().getChestplate() == null || target.getInventory().getChestplate().type == Material.AIR)) {
                        target.getInventory().setChestplate(bukkitSnapshot.chestPlate)
                    } else if (bukkitSnapshot.chestPlate != null) {
                        FastInventoryUtils.addOrBox(targetData.getId(), null, p, bukkitSnapshot.chestPlate, "§cRevived by ${p.getName()}")
                    }

                    if (bukkitSnapshot.leggings != null && (target.getInventory().getLeggings() == null || target.getInventory().getLeggings().type == Material.AIR)) {
                        target.getInventory().setLeggings(bukkitSnapshot.leggings)
                    } else if (bukkitSnapshot.leggings != null) {
                        FastInventoryUtils.addOrBox(targetData.getId(), null, p, bukkitSnapshot.leggings, "§cRevived by ${p.getName()}")
                    }

                    if (bukkitSnapshot.boots != null && (target.getInventory().getBoots() == null || target.getInventory().getBoots().type == Material.AIR)) {
                        target.getInventory().setBoots(bukkitSnapshot.boots)
                    } else if (bukkitSnapshot.boots != null) {
                        FastInventoryUtils.addOrBox(targetData.getId(), null, p, bukkitSnapshot.boots, "§cRevived by ${p.getName()}")
                    }

//                    if (bukkitSnapshot.heldItem != null && (target.getInventory().getItemInMainHand() == null || target.getInventory().getItemInMainHand().type == Material.AIR)) {
//                        target.getInventory().setItemInMainHand(bukkitSnapshot.heldItem)
//                    } else if (bukkitSnapshot.heldItem != null) {
//                        FastInventoryUtils.addOrBox(targetData.getId(), null, p, bukkitSnapshot.heldItem, "§cRevived by ${p.getName()}")
//                    }

                    bukkitSnapshot.inventorySlots.each {
                        FastInventoryUtils.addOrBox(targetData.getId(), null, p, it.value, "§cRevived by ${p.getName()}")
                    }
                } else {
                    if (bukkitSnapshot.helmet != null) FastInventoryUtils.addOrBox(targetData.getId(), null, p, bukkitSnapshot.helmet, "§cRevived by ${p.getName()}")
                    if (bukkitSnapshot.chestPlate != null) FastInventoryUtils.addOrBox(targetData.getId(), null, p, bukkitSnapshot.chestPlate, "§cRevived by ${p.getName()}")
                    if (bukkitSnapshot.leggings != null) FastInventoryUtils.addOrBox(targetData.getId(), null, p, bukkitSnapshot.leggings, "§cRevived by ${p.getName()}")
                    if (bukkitSnapshot.boots != null) FastInventoryUtils.addOrBox(targetData.getId(), null, p, bukkitSnapshot.boots, "§cRevived by ${p.getName()}")

                    bukkitSnapshot.inventorySlots.each {
                        FastInventoryUtils.addOrBox(targetData.getId(), null, p, it.value, "§cRevived by ${p.getName()}")
                    }
                }

                def playerLog = new Log(LogType.PLAYER, targetData.getId(), targetName)
                def targetLog = new Log(LogType.PLAYER, p.getUniqueId(), p.getName())

                playerLog.title = "§7Revived " + targetName
                playerLog.logMessage = ["§7Revived " + targetName]
                targetLog.title = "§7Revived by " + p.getName()
                targetLog.logMessage = ["§7Revived by " + p.getName()]

                Logs.insertLog(p.getUniqueId(), LogType.PLAYER, playerLog)
                Logs.insertLog(targetData.getId(), LogType.PLAYER, targetLog)

                openSnapshot(player, targetData, snapshot)
            }, {
                openSnapshot(player, targetData, snapshot)
            })

        })

        menu.set(6, 9, FastItemUtils.createItem(Material.RED_DYE, "§cBack", [
                "§cClick to go Back."
        ], false), { p, t, s ->
            openReviveMenu(p, targetData)
        })

        menu.openSync(player)
    }

}
