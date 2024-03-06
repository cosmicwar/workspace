package scripts.factions.content.log

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import scripts.factions.content.log.v2.LogUserData
import scripts.factions.content.log.v2.api.Log
import scripts.factions.content.log.v2.LogData
import scripts.factions.content.log.v2.api.LogFilterType
import scripts.factions.content.log.v2.api.LogType
import scripts.shared.data.uuid.UUIDDataManager
import scripts.shared.legacy.utils.BroadcastUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.DataUtils
import scripts.shared.utils.MojangAPI

import java.text.SimpleDateFormat

@CompileStatic(TypeCheckingMode.SKIP)
class Logs {

    static Map<UUID, LogData> logs = new HashMap<>()

    Logs() {
        GroovyScript.addUnloadHook {
            UUIDDataManager.getByClass(LogData).saveAll(false)
        }

        UUIDDataManager.register("internal_logs", LogData)

        UUIDDataManager.getAllData(LogData).each { LogData logData ->
            logs.put(logData.getId(), logData)
        }

        commands()
    }

    static def commands() {
        Commands.create().assertOp().assertPlayer().handler { ctx ->
            def player = ctx.sender()

            if (ctx.args().size() == 0 || ctx.args().size() > 2) {
                openAdminLogMenu(player)
            }

            if (ctx.args().size() == 1) {
                def target = ctx.arg(0).parseOrFail(String)
                def targetPlayer = Bukkit.getPlayer(target)
                if (targetPlayer == null) {
                    def targetId = MojangAPI.getUUID(target)
                    if (targetId == null) {
                        player.sendMessage("§c§l(!) §cThat player does not exist.")
                        return
                    }
                    openLogData(player, getLogData(targetId, LogType.PLAYER), 1)
                } else {
                    openLogData(player, getLogData(targetPlayer.getUniqueId(), LogType.PLAYER), 1)
                }
            }
        }.register("logs")
    }

    private static NamespacedKey logTargetId = new NamespacedKey(Starlight.plugin, "logTargetId")
    private static NamespacedKey logId = new NamespacedKey(Starlight.plugin, "logId")

    static def openAdminLogMenu(Player player, int page = 1) {
        MenuBuilder builder

        def sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss")

        def logs = getAllLogs().toList()

        def logUser = getLogData(player.getUniqueId(), LogType.PLAYER)

        def logUserData = logUser.userData
        if (logUserData != null) {
            if (logUserData.storedType == LogFilterType.PLAYER) {
                logs = logs.findAll { LogData log -> log.getType() == LogType.PLAYER }
            } else if (logUserData.storedType == LogFilterType.FACTION) {
                logs = logs.findAll { LogData log -> log.getType() == LogType.FACTION }
            }
        }

        builder = MenuUtils.createPagedMenu("§aInternal Logs", logs, { LogData log, Integer i ->
            def lore = [
                    "§7UUID: §3${log.getId()}",
                    "",
                    "§7Total logs: §3${log.history.size()}",
                    "§7Type: §3${log.getType().name()}",
                    "",
                    "§3Latest log: §e${log.getLatestLog()?.getTitle() ?: "None"}",
                    "§3Latest log date: §e${log.getLatestLog()?.timestamp != null ? sdf.format(log.getLatestLog().timestamp) : "None"}",
                    "",
                    "§7Click to view logs."
            ]

            ItemStack item
            if (log.getName() == log.getId().toString()) {
                item = FastItemUtils.createBase64Skull("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNlOGVjMWI1NDdhZDU5N2I3YWJkZmE3ZjM4ZTQ0OTc3OGNkYWQ3MzY3YzZlMjUxNDMxYmZjZTdlZGIzM2IzYiJ9fX0=", "§e${log.getName()}", lore)
            } else {
                item = FastItemUtils.createSkull(log.getName(), "§e${log.getName()}", lore)
            }

            DataUtils.setTagString(item, logTargetId, log.getId().toString())

            return item
        }, page, false, [
                { Player p, ClickType t, int s ->
                    def item = builder.get().getItem(s)
                    if (item == null || item.type == Material.AIR) {
                        return
                    }

                    def logId = DataUtils.getTagString(item, logTargetId)
                    if (logId == null) {
                        return
                    }

                    UUID targetId = UUID.fromString(logId)
                    def logData = getExistingLogData(targetId)
                    if (logData == null) {
                        return
                    }

                    openLogData(p, logData, 1)
                },
                { Player p, ClickType t, int s ->
                    openAdminLogMenu(p, page + 1)
                },
                { Player p, ClickType t, int s ->
                    openAdminLogMenu(p, page - 1)
                },
        ])

        // Add filter buttons
        def filterTypes = LogFilterType.values().findAll { LogFilterType type -> type.storedType }
        def storedType = logUserData == null ? LogFilterType.ALL : logUserData.storedType
        def lore = filterTypes.collect { LogFilterType type ->
            if (type == storedType) {
                "§a * ${type.prefix}"
            } else {
                "§7${type.prefix}"
            }
        }

        builder.set(builder.get().getSize() - 4, FastItemUtils.createItem(Material.PAPER, "§3Filter §7- $storedType.prefix", lore, false), {p, t, s ->
            if (t != null) {
                if (t.toString().contains("RIGHT")) {
                    if (logUserData == null) {
                        logUserData = new LogUserData()
                        logUser.userData = logUserData
                    }
                    logUserData.storedType = logUserData.storedType.getPreviousType(true)

                    openAdminLogMenu(p, page)
                } else if (t.toString().contains("LEFT")) {
                    if (logUserData == null) {
                        logUserData = new LogUserData()
                        logUser.userData = logUserData
                    }
                    logUserData.storedType = logUserData.storedType.getNext(true)

                    openAdminLogMenu(p, page)
                }
            }
        })

        builder.openSync(player)
    }

    static def openLogData(Player player, LogData data, int page = 1) {
        MenuBuilder builder

        if (data.history.isEmpty()) {
            player.sendMessage("§c§l(!) §cThat player has no logs.")
            return
        }

        def sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss")

        def history = data.history.toList()

        def logUser = getLogData(player.getUniqueId(), LogType.PLAYER)

        def logUserData = logUser.userData
        if (logUserData != null) {
            if (logUserData.inventoryFilterType == LogFilterType.PLAYER) {
                history = history.findAll { Log log -> log.targetType == LogType.PLAYER }
            } else if (logUserData.inventoryFilterType == LogFilterType.FACTION) {
                history = history.findAll { Log log -> log.targetType == LogType.FACTION }
            } else if (logUserData.inventoryFilterType == LogFilterType.ANTI_CHEAT) {
                history = history.findAll { Log log -> log.targetType == LogType.ANTI_CHEAT }
            } else if (logUserData.inventoryFilterType == LogFilterType.NONE) {
                history = []
            }
        }

        builder = MenuUtils.createPagedMenu("§aInternal Logs", history, { Log log, Integer i ->
            def lore = []

            log.getLogMessage().each {
                lore.add("§7$it")
            }

            lore.add("")
            lore.add("§3Timestamp: §e${log.timestamp != null ? sdf.format(log.timestamp) : "Unknown"}")
            lore.add("")

            lore.add("§3Target: §e${log.targetName == null ? "Unknown" : log.targetName}")
            lore.add("§3Target type: §e${log.targetType.name()}")

            if (log.initiatorId != null) {
                lore.add("")
                lore.add("§3Initiator: §e${log.initiatorName == null ? "Unknown" : log.initiatorName}")
                lore.add("§3Initiator type: §e${log.initiatorType.name()}")
            }

            if (log.position.world != null) {
                lore.add("")
                lore.add("§3Position: §ex:${log.position.x} y:${log.position.y} z:${log.position.z} world:${log.position.world}")
            }

            lore.add("")
            lore.add("§3Log ID: §e${log.id}")

            if (log.position.world != null) {
                lore.add("")
                lore.add("§7Click to teleport.")
            }

            def item = FastItemUtils.createItem(Material.PAPER, "§3" + log.getTitle(), lore)

            DataUtils.setTag(item, logId, PersistentDataType.STRING, log.getId().toString())

            return item
        }, page, true, [
                { Player p, ClickType t, int s ->
                    def item = builder.get().getItem(s)
                    if (item == null || item.type == Material.AIR) return

                    def logId = DataUtils.getTag(item, logId, PersistentDataType.STRING)
                    if (logId == null) return

                    data.history.find { Log log -> (log.getId().toString() == logId) }.each { Log log ->
                        if (log.position != null) {
                            player.teleport(log.position.getLocation(Bukkit.getWorld(log.position.world)))
                        }
                    }

                    p.closeInventory()
                },
                { Player p, ClickType t, int s ->
                    openLogData(p, data, page + 1)
                },
                { Player p, ClickType t, int s ->
                    openLogData(p, data, page - 1)
                },
                { Player p, ClickType t, int s ->
                    openAdminLogMenu(p, page)
                }
        ])

        // Add filter buttons
        def filterTypes = LogFilterType.values()
        def storedType = logUserData == null ? LogFilterType.ALL : logUserData.inventoryFilterType
        def lore = filterTypes.collect { LogFilterType type ->
            if (type == storedType) {
                "§a * ${type.prefix}"
            } else {
                "§7${type.prefix}"
            }
        }

        builder.set(builder.get().getSize() - 4, FastItemUtils.createItem(Material.PAPER, "§3Filter §7- $storedType.prefix", lore, false), {p, t, s ->
            if (t != null) {
                if (t.toString().contains("RIGHT")) {
                    if (logUserData == null) {
                        logUserData = new LogUserData()
                        logUser.userData = logUserData
                    }
                    logUserData.inventoryFilterType = logUserData.inventoryFilterType.getPreviousType()

                    openLogData(p, data, page)
                } else if (t.toString().contains("LEFT")) {
                    if (logUserData == null) {
                        logUserData = new LogUserData()
                        logUser.userData = logUserData
                    }
                    logUserData.inventoryFilterType = logUserData.inventoryFilterType.getNext()

                    openLogData(p, data, page)
                }
            }
        })

        builder.openSync(player)
    }

    static LogData getExistingLogData(UUID targetId) {
        return logs.get(targetId)
    }

    static LogData getLogData(UUID targetId, LogType defaultStoredType) {
        LogData logData = getExistingLogData(targetId)
        if (logData == null) {
            logData = UUIDDataManager.getData(targetId, LogData)
            logData.type = defaultStoredType
            logs.put(targetId, logData)
        }

        return logData
    }

    static def insertLog(UUID targetId, LogType type, Log log) {
        def logData = getLogData(targetId, type)
        if (logData == null) return

        logData.history.add(log)
        logData.queueSave()
    }

    static Collection<LogData> getAllLogs() {
        return logs.values()
    }

}
