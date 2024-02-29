package scripts.factions.core.faction.cmd

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.enviorment.GroovyScript
import scripts.factions.content.log.v2.api.Log
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder

import java.text.SimpleDateFormat

@CompileStatic(TypeCheckingMode.SKIP)
class FLogs
{

    FLogs()
    {
        GroovyScript.addUnloadHook {
            Factions?.fCommand?.subCommands?.removeIf { it.aliases.find {
                it.equalsIgnoreCase("logs")
            } != null }
            Factions?.fCommand?.build()
        }

        commands()
    }

    static def commands() {
        FCBuilder fCommand = Factions.fCommand

        fCommand.create("logs").register { cmd ->
            FCommandUtil.factionMemberFromCommand(cmd, { faction, member ->
                openLogMenu(cmd.sender(), faction)
            })
        }

        fCommand.build()
    }

    static def openLogMenu(Player player, Faction faction, int page = 1) {
        MenuBuilder menu

        def sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss")

        def history = faction.logData.history.sort { a, b ->
            return b.timestamp <=> a.timestamp
        }

        menu = MenuUtils.createPagedMenu("§3${faction.getName()} §aLogs", history, { Log log, Integer i ->
            def lore = []

            log.getLogMessage().each {
                lore.add("§7$it")
            }

            lore.add("")
            lore.add("§3Timestamp: §e${log.timestamp != null ? sdf.format(log.timestamp) : "Unknown"}")

            if (log.initiatorId != null) {
                lore.add("")
                lore.add("§3Initiator: §e${log.initiatorName == null ? "Unknown" : log.initiatorName}")
            }

            if (log.position.world != null) {
                lore.add("")
                lore.add("§3Position: §ex:${log.position.x1} y:${log.position.y1} z:${log.position.z1} world:${log.position.world}")
            }

            ItemStack item = FastItemUtils.createSkull(log.getInitiatorName(), "§e${log.getTitle()}", lore)

            return item
        }, page, false, [
                { Player p, ClickType t, int s ->

                },
                { Player p, ClickType t, int s ->
                    openLogMenu(player, faction, page + 1)
                },
                { Player p, ClickType t, int s ->
                    openLogMenu(player, faction, page - 1)
                },

        ])

        menu.openSync(player)
    }

}
