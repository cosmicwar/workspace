package scripts.factions.content.essentials.cmd

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.utils.Players

class GMCmd {

    GMCmd() {
        Commands.create().assertPermission("essentials.gamemode").assertPlayer().assertUsage("/gmc <player>").handler {ctx ->
            if (ctx.args().size() == 0) {
                def sender = ctx.sender()

                sender.setGameMode(GameMode.CREATIVE)
                ctx.reply("§] §> §aYou are now in creative mode.")
            } else {
                def target = ctx.arg(0).parseOrFail(Player)

                target.setGameMode(GameMode.CREATIVE)

                ctx.reply("§] §> §aYou have set §f${target.getDisplayName()} §ato creative mode.")
                Players.msg(target, "§] §> §aYou are now in creative mode.")
            }
        }.register("creative", "gmc", "gamemodec")

        // survival, spectator, adventure

        Commands.create().assertPermission("essentials.gamemode").assertPlayer().assertUsage("/gms <player>").handler {ctx ->
            if (ctx.args().size() == 0) {
                def sender = ctx.sender()

                sender.setGameMode(GameMode.SURVIVAL)
                ctx.reply("§] §> §aYou are now in survival mode.")
            } else {
                def target = ctx.arg(0).parseOrFail(Player)

                target.setGameMode(GameMode.SURVIVAL)

                ctx.reply("§] §> §aYou have set §f${target.getDisplayName()} §ato survival mode.")
                Players.msg(target, "§] §> §aYou are now in survival mode.")
            }
        }.register("survival", "gms")

        Commands.create().assertPermission("essentials.gamemode").assertPlayer().assertUsage("/gma <player>").handler {ctx ->
            if (ctx.args().size() == 0) {
                def sender = ctx.sender()

                sender.setGameMode(GameMode.ADVENTURE)
                ctx.reply("§] §> §aYou are now in adventure mode.")
            } else {
                def target = ctx.arg(0).parseOrFail(Player)

                target.setGameMode(GameMode.ADVENTURE)

                ctx.reply("§] §> §aYou have set §f${target.getDisplayName()} §ato adventure mode.")
                Players.msg(target, "§] §> §aYou are now in adventure mode.")
            }
        }.register("adventure", "gma")

        Commands.create().assertPermission("essentials.gamemode").assertPlayer().assertUsage("/gmsp <player>").handler {ctx ->
            if (ctx.args().size() == 0) {
                def sender = ctx.sender()

                sender.setGameMode(GameMode.SPECTATOR)
                ctx.reply("§] §> §aYou are now in spectator mode.")
            } else {
                def target = ctx.arg(0).parseOrFail(Player)

                target.setGameMode(GameMode.SPECTATOR)

                ctx.reply("§] §> §aYou have set §f${target.getDisplayName()} §ato spectator mode.")
                Players.msg(target, "§] §> §aYou are now in spectator mode.")
            }
        }.register("spectator", "gmsp")

        // default gamemode cmd
        Commands.create().assertPermission("essentials.gamemode").assertPlayer().assertUsage("/gamemode <player> <mode>").handler {ctx ->
            if (ctx.args().size() == 0) {
                ctx.reply("§] §> §cUsage: /gamemode <mode> [player]")
                return
            } else if (ctx.args().size() == 1) {
                def mode = ctx.arg(0).parseOrFail(String)

                switch(mode) {
                    case "0":
                    case "s":
                    case "survival":
                        ctx.sender().setGameMode(GameMode.SURVIVAL)
                        ctx.reply("§] §> §aYou are now in survival mode.")
                        break
                    case "1":
                    case "c":
                    case "creative":
                        ctx.sender().setGameMode(GameMode.CREATIVE)
                        ctx.reply("§] §> §aYou are now in creative mode.")
                        break
                    case "2":
                    case "a":
                    case "adventure":
                        ctx.sender().setGameMode(GameMode.ADVENTURE)
                        ctx.reply("§] §> §aYou are now in adventure mode.")
                        break
                    case "3":
                    case "sp":
                    case "spectator":
                        ctx.sender().setGameMode(GameMode.SPECTATOR)
                        ctx.reply("§] §> §aYou are now in spectator mode.")
                        break
                    default:
                        ctx.reply("§] §> §cUsage: /gamemode <mode> [player]")
                        break
                }
            } else {
                def mode = ctx.arg(0).parseOrFail(String)
                def target = ctx.arg(1).parseOrFail(Player)

                switch(mode) {
                    case "0":
                    case "s":
                    case "survival":
                        target.setGameMode(GameMode.SURVIVAL)
                        ctx.reply("§] §> §aYou have set §f${target.getDisplayName()} §ato survival mode.")
                        Players.msg(target, "§] §> §aYou are now in survival mode.")
                        break
                    case "1":
                    case "c":
                    case "creative":
                        target.setGameMode(GameMode.CREATIVE)
                        ctx.reply("§] §> §aYou have set §f${target.getDisplayName()} §ato creative mode.")
                        Players.msg(target, "§] §> §aYou are now in creative mode.")
                        break
                    case "2":
                    case "a":
                    case "adventure":
                        target.setGameMode(GameMode.ADVENTURE)
                        ctx.reply("§] §> §aYou have set §f${target.getDisplayName()} §ato adventure mode.")
                        Players.msg(target, "§] §> §aYou are now in adventure mode.")
                        break
                    case "3":
                    case "sp":
                    case "spectator":
                        target.setGameMode(GameMode.SPECTATOR)
                        ctx.reply("§] §> §aYou have set §f${target.getDisplayName()} §ato spectator mode.")
                        Players.msg(target, "§] §> §aYou are now in spectator mode.")
                        break
                    default:
                        ctx.reply("§] §> §cUsage: /gamemode <mode> [player]")
                        break
                }
            }
        }.register("gamemode", "gm")
    }
}
