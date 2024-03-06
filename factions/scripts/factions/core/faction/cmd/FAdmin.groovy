package scripts.factions.core.faction.cmd

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.shared.data.uuid.UUIDDataManager
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.TimeUtils
import scripts.shared.systems.MenuBuilder

@CompileStatic(TypeCheckingMode.SKIP)
class FAdmin {

    FAdmin() {
        GroovyScript.addUnloadHook {
            Factions?.fCommand?.subCommands?.removeIf { it.aliases.find {
                it.equalsIgnoreCase("debug")
            } != null }
            Factions?.fCommand?.build()
        }

        commands()
    }

    static def commands() {
        FCBuilder fCommand = Factions.fCommand

        fCommand.create("debug").requirePermission("faction.dev").register { cmd ->
            Schedulers.async().execute {
                openDebug(cmd.sender())
            }
        }

        fCommand.build()
    }

    /*
   ~ debug admin thingy thing ~
*/

    static def openDebug(Player player) {
        MenuBuilder menu = new MenuBuilder(27, "§3Faction Admin Access")

        menu.set(2, 4, FastItemUtils.createItem(Material.ENDER_CHEST, "§eFactions DB", [
                "",
                "§afaction info",
                "",
                "§asize - §7(${UUIDDataManager.getAllData(Faction).size()})",
        ], false), { p, t, s ->
            openDebugFactions(player, false)
        })

        menu.set(2, 6, FastItemUtils.createItem(Material.ENDER_CHEST, "§eMember DB", [
                "",
                "§aplayer info",
                "",
                "§asize - §7(${UUIDDataManager.getAllData(Member).size()})",
        ], false), { p, t, s ->
            openDebugMembers(player, false)
        })

        menu.openSync(player)
    }

    static def openDebugMembers(Player player, boolean cache, int page = 1) {
        Collection<Member> data = UUIDDataManager.getAllData(Member)

        MenuBuilder menu
        menu = MenuUtils.createPagedMenu("§3Member Cache §7(${data.size()})", data.toList(), { Member member, Integer slot ->
            def memberFaction = Factions.getFaction(member.getFactionId(), false)
            def item = FastItemUtils.createItem(Material.PAPER, "§3${member.getName()}", [
                    "§7uuid: §e${member.getId()}",
                    "",
                    "§7Faction: §e${memberFaction != null ? memberFaction.getName() : "§cnull"}",
                    "§7Role: §e${member.getRole().name}",
                    "§7Last Online: §e${System.currentTimeMillis() == member.getLastOnline() ? "§anow" : TimeUtils.getTimeAmount(System.currentTimeMillis() - member.getLastOnline(), true)}",
            ], false)

            return item
        }, page, true, [
                { Player p, ClickType t, int s -> },
                { Player p, ClickType t, int s -> openDebugMembers(player, cache, page - 1) },
                { Player p, ClickType t, int s -> openDebugMembers(player, cache, page + 1) },
                { Player p, ClickType t, int s -> openDebug(player) }
        ])

        menu.openSync(player)
    }

    static def openDebugFactions(Player player, boolean cache, int page = 1) {
        Collection<Faction> data = UUIDDataManager.getAllData(Faction.class)

        MenuBuilder menu
        menu = MenuUtils.createPagedMenu("§3Faction ${cache ? "§3Cache" : "§eDB"} §7(${data.size()})", data.toList(), { Faction faction, Integer slot ->
            def item = FastItemUtils.createItem(Material.PAPER, "§3${faction.getName()}", [
                    "§7uuid: §e${faction.getId()}",
                    "",
                    "§7name: §e${faction.getName()}",
                    "§7description: §e${faction.getDescription()}",
                    "",
                    "§7Members: §e${faction.getMembers().size()}",
                    "§7Claims: §e${faction.getClaims().size()}",
            ], false)

            return item
        }, page, true, [
                { Player p, ClickType t, int s -> },
                { Player p, ClickType t, int s -> openDebugFactions(player, cache, page - 1) },
                { Player p, ClickType t, int s -> openDebugFactions(player, cache, page + 1) },
                { Player p, ClickType t, int s -> openDebug(player) }
        ])

        menu.openSync(player)
    }

}
