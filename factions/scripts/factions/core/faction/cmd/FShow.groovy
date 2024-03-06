package scripts.factions.core.faction.cmd

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.md_5.bungee.api.chat.HoverEvent
import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.command.context.PlayerContext
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.relation.RelationType
import scripts.shared.data.uuid.UUIDDataManager
import scripts.factions.util.Clickable
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.legacy.utils.TimeUtils
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.Formats

/*
   ~ f show ~
*/
@CompileStatic(TypeCheckingMode.SKIP)
class FShow {

    FShow() {
        GroovyScript.addUnloadHook {
            Factions?.fCommand?.subCommands?.removeIf { it.aliases.find {
                it.equalsIgnoreCase("show") || it.equalsIgnoreCase("list") ||  it.equalsIgnoreCase("showall")
            } != null }
            Factions?.fCommand?.build()
        }

        commands()
    }

    static def commands() {
        FCBuilder fCommand = Factions.fCommand

        // F Show Logic
        fCommand.create("show", "who", "f").usage("[target]").description("Show a faction.").register { cmd ->
            FCommandUtil.memberFromCommand(cmd) { member ->
                if (cmd.args().size() == 0) {
                    // show that member
                    showFaction(cmd, Factions.getFaction(member.getFactionId(), false))
                    // should theoretically never null w/ system factions ;p
                } else {
                    // show target faction
                    def targetFaction = cmd.arg(0).parseOrFail(Faction)

                    if (targetFaction == null) {
                        showFaction(cmd, Factions.getFaction(member.getFactionId(), false))
                        cmd.reply("")
                        cmd.reply("§3A faction with the name §e${cmd.arg(0).parseOrFail(String)}§3 does not exist.")
                        return
                    }

                    showFaction(cmd, targetFaction)
                }
            }
        }

        fCommand.create("list").description("List factions.").register { ctx ->
            FCommandUtil.memberFromCommand(ctx) { member ->

                def factions = UUIDDataManager.getAllData(Faction).findAll { it.systemFactionData == null }

                if (ctx.args().size() == 0) {
                    factions = factions.toList().subList(0, Math.min(amountPerPage, factions.size()))
                    factions = factions.sort { faction1, faction2 -> faction2.getOnlineMembers().size() <=> faction1.getOnlineMembers().size() }

                    ctx.reply("§8§m${StringUtils.repeat('-', 16)}")
                    factions.each {
                        // show faction
                        ctx.reply(ColorUtil.color("§8[ ${Factions.getRelationType(member, it).color + it.getName()} §8] §7- §d${it.getOnlineMembers().size()}§7/§d${it.getMembers().size()}"))
                    }
                    ctx.reply("§8§m${StringUtils.repeat('-', 16)}")

                    return
                }

                int page = ctx.arg(0).parseOrFail(Integer)
                int maxPages = (int) Math.ceil(factions.size() / amountPerPage)

                if (page > maxPages) {
                    factions = factions.toList().subList(0, Math.min(amountPerPage, factions.size()))
                    factions = factions.sort { faction1, faction2 -> faction2.getOnlineMembers().size() <=> faction1.getOnlineMembers().size() }

                    ctx.reply("§8§m${StringUtils.repeat('-', 16)}")
                    factions.each {
                        // show faction
                        ctx.reply(ColorUtil.color("§8[ ${Factions.getRelationType(member, it).color + it.getName()} §8] §7- §d${it.getMembers().size()}§7/§d${Factions.getMaxFactionSize(it)}"))
                    }
                    ctx.reply("§8§m${StringUtils.repeat('-', 16)}")

                    return
                }

                factions = factions.toList().subList((page - 1) * amountPerPage, Math.min(page * amountPerPage, factions.size()))
                factions = factions.sort { faction1, faction2 -> faction2.getOnlineMembers().size() <=> faction1.getOnlineMembers().size() }

                ctx.reply("§8§m${StringUtils.repeat('-', 16)}")
                factions.each {
                    // show faction
                    ctx.reply(ColorUtil.color("§8[ ${Factions.getRelationType(member, it).color + it.getName()} §8] §7- §d${it.getMembers().size()}§7/§d${Factions.getMaxFactionSize(it)}"))
                }
                ctx.reply("§8§m${StringUtils.repeat('-', 16)}")
            }
        }

        fCommand.create("showall").description("List all factions.").register { ctx ->
            FCommandUtil.memberFromCommand(ctx) { member ->

                def factions = UUIDDataManager.getAllData(Faction).findAll { it.systemFactionData == null }

                if (ctx.args().size() == 0) {
                    factions = factions.toList().subList(0, Math.min(amountPerPage, factions.size()))
                    factions = factions.sort { faction1, faction2 -> faction2.getOnlineMembers().size() <=> faction1.getOnlineMembers().size() }

                    ctx.reply("§8§m${StringUtils.repeat('-', 16)}")
                    factions.each {
                        // show faction
                        ctx.reply(ColorUtil.color("§8[ ${Factions.getRelationType(member, it).color + it.getName()} §8] §7- §d${it.getMembers().size()}§7/§d${Factions.getMaxFactionSize(it)}"))
                    }
                    ctx.reply("§8§m${StringUtils.repeat('-', 16)}")

                    return
                }

                int page = ctx.arg(0).parseOrFail(Integer)
                int maxPages = (int) Math.ceil(factions.size() / amountPerPage)

                if (page > maxPages) {
                    factions = factions.toList().subList(0, Math.min(amountPerPage, factions.size()))
                    factions = factions.sort { faction1, faction2 -> faction2.getOnlineMembers().size() <=> faction1.getOnlineMembers().size() }

                    ctx.reply("§8§m${StringUtils.repeat('-', 16)}")
                    factions.each {
                        // show faction
                        ctx.reply(ColorUtil.color("§8[ ${Factions.getRelationType(member, it).color + it.getName()} §8] §7- §d${it.getMembers().size()}§7/§d${Factions.getMaxFactionSize(it)}"))
                    }
                    ctx.reply("§8§m${StringUtils.repeat('-', 16)}")

                    return
                }

                factions = factions.toList().subList((page - 1) * amountPerPage, Math.min(page * amountPerPage, factions.size()))
                factions = factions.sort { faction1, faction2 -> faction2.getOnlineMembers().size() <=> faction1.getOnlineMembers().size() }

                ctx.reply("§8§m${StringUtils.repeat('-', 16)}")
                factions.each {
                    // show faction
                    ctx.reply(ColorUtil.color("§8[ ${Factions.getRelationType(member, it).color + it.getName()} §8] §7- §d${it.getMembers().size()}§7/§d${Factions.getMaxFactionSize(it)}"))
                }
                ctx.reply("§8§m${StringUtils.repeat('-', 16)}")
            }
        }

        fCommand.build()
    }

    static int amountPerPage = 15


    static String primaryHexColor = "#0891DB"
    static String magenta = "#C039EB"
    static String secondaryHexColor = "#24DBE7"
    static String redHex = "#ff0000"
    static String neutralHexColor = "#ffffff"

    static String redHexColor = "#A41313"
    static String greenHexColor = "#22D851"
    static String yellowHexColor = "#E3DD1C"
    static String aquaHexColor = "#24DBE7"
    static String blueHexColor = "#0891DB"
    static String purpleHexColor = "#AF24E7"
    static String symbol = '⋆'

    static def showFaction(PlayerContext cmd, Faction faction) {
        if (faction == null) {
            cmd.reply("§] §> §cAn error occurred while loading the faction data.")
            return
        }

        FCommandUtil.memberFromCommand(cmd) { member ->
            def relationType = Factions.getRelationType(member, faction)
            if (relationType == null) relationType = RelationType.NEUTRAL

            if (faction.systemFactionData != null) {
                def startLine = "§8§m${StringUtils.repeat('-', 16)}§8 [ ${ColorUtil.color(faction.systemFactionData.color + faction.getName())} §8] §8§m${StringUtils.repeat('-', 16)}"
                def startLineStripped = "${StringUtils.repeat('-', 16)} [ ${faction.getName()} ] ${StringUtils.repeat('-', 16)}"
                cmd.reply(startLine)
                cmd.reply("")
                cmd.reply(ColorUtil.color("§c${faction.systemFactionData.color} ${faction.getDescription()}"))
                cmd.reply("")
                cmd.reply("§8§m${StringUtils.repeat('-', startLineStripped.length() - 3 /*smaller chars?*/)}")
            } else {
                def startLine = "§8§m${StringUtils.repeat('-', 16)}§8 [ ${relationType.color + faction.getName()} §8] §8§m${StringUtils.repeat('-', 16)}"
                def startLineStripped = "${StringUtils.repeat('-', 16)} [ ${faction.getName()} ] ${StringUtils.repeat('-', 16)}"
                cmd.reply(startLine)

                cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Name: §<$secondaryHexColor>${faction.getName()}"))
                cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Description: §<$secondaryHexColor>${faction.getDescription()}"))
                if (faction.fTopRank != null) cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Faction Rank: §<$secondaryHexColor>${NumberUtils.format(faction.fTopRank)}"))

                cmd.reply("")

                cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>FTop Value: §<$secondaryHexColor>${NumberUtils.format(faction.fTopValue)}"))
                cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Spawner Value: §<$secondaryHexColor>${NumberUtils.format(faction.spawnerValue as Integer)}"))
                cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Points: §<$secondaryHexColor>${NumberUtils.format(faction.pointsValue as Integer)}"))



                def leader = Factions.getMember(faction.leaderId)
                def leaderName = leader != null ? leader.getName() : "§cUnknown"
                cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Leader: §<$secondaryHexColor>${leaderName}"))

                cmd.reply("")

                cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Claims: §<$secondaryHexColor>${faction.claims.size()}/§d1000"))

                cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Power: §<$secondaryHexColor>${faction.getFactionPower()}§7/§d${Factions.getMaxFactionPower(faction)}"))

                cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Size: §<$secondaryHexColor>${faction.getMembers().size()}§7/§d${Factions.getMaxFactionSize(faction)}"))

                cmd.reply("")
                if (relationType == RelationType.MEMBER) {
                    cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Balance: §<$secondaryHexColor>\$0"))
                    cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>TnT: §<$secondaryHexColor>${faction.getTntBalance()}"))

                    if (faction.coreChunkData != null) {
                        cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Core: §<$secondaryHexColor>${faction.coreChunkData.blockPosition.x},${faction.coreChunkData.blockPosition.y},${faction.coreChunkData.blockPosition.z}"))
                    }

                    cmd.reply("")
                }

                def space = false
                def allies = faction.getAllies()
                if (allies.size() > 0) {
                    allies = allies.collect {
                        def af = Factions.getFaction(it, false)
                        if (af == null) return "§cUnknown"

                        def relation = Factions.getRelationType(member, af)
                        return relation.color + af.getName()
                    }
                    space = true
                    cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Allies: §<$secondaryHexColor>${allies.size()} §7- §d${allies.join("§7, §d")}"))
                }

                def truces = faction.getTruces()
                if (truces.size() > 0) {
                    truces = truces.collect {
                        def tf = Factions.getFaction(it, false)
                        if (tf == null) return "§cUnknown"

                        def relation = Factions.getRelationType(member, tf)
                        return relation.color + tf.getName()
                    }
                    space = true
                    cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Truces: §<$secondaryHexColor>${truces.size()} §7- §d${truces.join("§7, §b")}"))
                }

                if (relationType == RelationType.MEMBER) {
                    def enemies = faction.getEnemies()
                    if (enemies.size() > 0) {
                        enemies = enemies.collect {
                            def ef = Factions.getFaction(it, false)
                            if (ef == null) return "§cUnknown"

                            def relation = Factions.getRelationType(member, ef)
                            return relation.color + ef.getName()
                        }
                        space = true
                        cmd.reply(ColorUtil.color("§<$neutralHexColor>$symbol §<$primaryHexColor>Enemies: §<$secondaryHexColor>${enemies.size()} §7- §d${enemies.join("§7, §c")}"))
                    }
                }
                if (space) cmd.reply("")

                cmd.reply(ColorUtil.color("§f${symbol} §<$primaryHexColor>Open: ${faction.isOpen() ? "§aYes" : "§cNo"}"))
                cmd.reply(ColorUtil.color("§f${symbol} §<$primaryHexColor>Created: §<$secondaryHexColor>${TimeUtils.getTimeAmount(System.currentTimeMillis() - faction.getCreateDate())} ago"))
                cmd.reply("")

                def members = new ArrayList<>(Factions.getFactionMembers(faction))
                members = members.sort { member1, member2 -> member1.getRole().priority <=> member2.getRole().priority }

                def onlineMembers = members.findAll { Bukkit.getPlayer(it.getId()) != null }.sort { member1, member2 -> member2.getRole().priority <=> member1.getRole().priority }
                def offlineMembers = members.findAll { Bukkit.getPlayer(it.getId()) == null }.sort { member1, member2 -> member2.getRole().priority <=> member1.getRole().priority }

                def membersClickable = new Clickable("§aOnline Members §7§o(${onlineMembers.size()}) §7- ")

                onlineMembers.each { onlineMember ->
                    membersClickable.addHoverEvent("${relationType.color + onlineMember.getDisplayName()}§7", HoverEvent.Action.SHOW_TEXT, "§7Role: §a${onlineMember.getRole().name()}\n§7Last Online: §a${Formats.formatTimeMillis(System.currentTimeMillis() - onlineMember.getLastOnline())} ago").addText(", ")
                }
                cmd.sender().spigot().sendMessage(membersClickable.build())

                def offlineMembersClickable = new Clickable("§cOffline Members §7§o(${offlineMembers.size()}) §7- ")

                offlineMembers.each { offlineMember ->
                    offlineMembersClickable.addHoverEvent("§7${offlineMember.getDisplayName()}§r", HoverEvent.Action.SHOW_TEXT, "§7Role: §c${offlineMember.getRole().name()}\n§7Last Online: §c${Formats.formatTimeMillis(System.currentTimeMillis() - offlineMember.getLastOnline())} ago").addText(", ")
                }

                cmd.sender().spigot().sendMessage(offlineMembersClickable.build())

//                cmd.reply("§aOnline Members §7§o(${onlineMembers.size()}) §7- [§a${onlineMembers.collect { it.getDisplayName() }.join(", ")}§7]")
//                cmd.reply("§cOffline Members §7§o(${offlineMembers.size()}) §7- [§c${offlineMembers.collect { it.getDisplayName() }.join(", ")}§7]")
                cmd.reply("§8§m${StringUtils.repeat('-', startLineStripped.length() - 2 /*smaller chars?*/)}")
            }
        }
    }
}
