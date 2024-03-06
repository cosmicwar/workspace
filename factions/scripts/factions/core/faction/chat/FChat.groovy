package scripts.factions.core.faction.chat

import groovy.transform.CompileStatic
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players
import scripts.shared.content.SCBuilder
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.Factions

@CompileStatic
class FChat {

    FChat() {
        GroovyScript.addUnloadHook {
            Factions?.fCommand?.subCommands?.removeIf { it.aliases.find {
                it.equalsIgnoreCase("chat")
            } != null }
            Factions?.fCommand?.build()
        }

        commands()
        events()
    }

    static def events() {
        Events.subscribe(AsyncPlayerChatEvent.class).handler {event ->
            def player = event.getPlayer()

            def member = Factions.getMember(player.getUniqueId())

            def faction = Factions.getFaction(member.factionId, false)
            if (faction == null) return

            def chatMode = member.chatMode
            if (chatMode == ChatMode.PUBLIC) return

            event.setCancelled(true)

            def message = event.getMessage()

            switch(chatMode) {
                case ChatMode.ALLY:
                    faction.getAllies().each {
                        def allyFaction = Factions.getFaction(it, false)
                        if (allyFaction == null) return

                        allyFaction.msg("§d${player.getName()}§8» §d$message", "§2§l[§d§lAC§2§l] ")
                    }
                    faction.msg("§a${member.getDisplayName()}§8» §d$message", "§2§l[§d§lAC§2§l] ")
                    break
                case ChatMode.TRUCE:
                    faction.getTruces().each {
                        def truceFaction = Factions.getFaction(it, false)
                        if (truceFaction == null) return

                        truceFaction.msg("§b${player.getName()}§8» §b$message", "§2§l[§b§lTC§2§l] ")
                    }
                    faction.msg("§a${member.getDisplayName()}§8» §b$message", "§2§l[§b§lTC§2§l] ")
                    break
                case ChatMode.FACTION:
                    faction.msg("§a${member.getDisplayName()}§8» §a$message", "§2§l[§a§lFC§2§l] ")
                    break
                case ChatMode.NONE:
                    Players.msg(player, "§] §> §cYou are not allowed to chat.")
            }
        }

        Events.subscribe(AsyncPlayerChatEvent.class).handler { event ->
            if (event.getPlayer().isOp()) return

            List<Player> toRemove = []
            event.recipients.each {
                def member = Factions.getMember(it.getUniqueId())

                if (member.chatMode == ChatMode.NONE) {
                    toRemove.add(it)
                }
            }

            event.recipients.removeAll(toRemove)
        }
    }

    static def commands() {
        SCBuilder fCommand = Factions.fCommand

        fCommand.create("chat", "c").register { ctx ->
            FCommandUtil.factionMemberFromCommand(ctx) { faction, member ->
                if (ctx.args().size() == 0) {
                    // cycle chat mode
                    member.chatMode = member.chatMode.getNext()
                    member.queueSave()
                    ctx.sender().sendMessage("§aYou are now talking in §3${member.chatMode.name()} §achat.")
                    return
                }

                def arg = ctx.arg(0).parseOrFail(String)
                def mode = ChatMode.values().find { it.name().equalsIgnoreCase(arg) || it.aliases.find { it.equalsIgnoreCase(arg) } != null }
                if (mode == null) {
                    ctx.sender().sendMessage("§cInvalid chat mode.")
                    return
                }

                member.chatMode = mode
                member.queueSave()
                ctx.sender().sendMessage("§aYou are now talking in §3${member.chatMode.name()} §achat.")
            }
        }

        fCommand.build()
    }


}
