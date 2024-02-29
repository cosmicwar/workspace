package scripts.factions.features.tutorial

import org.starcade.starlight.helper.Commands
import org.bukkit.Material
import org.bukkit.entity.Player
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.MenuDecorator

static void showTutorialMenu(Player player) {
    MenuBuilder builder = new MenuBuilder(5 * 9, "§e§lTutorial §7(§aGetting Started§7)")

    MenuDecorator.decorate(builder, [
            "000000000",
            "013131310",
            "034444430",
            "013131310",
            "000000000",
    ])



    builder.set(1, 5, FastItemUtils.createItem(Material.PAPER, ColorUtil.rainbow("Nova Factions", ["#d62b09", "#d7ff00"] as String[], "§l"), [
            "§r",
            "§fWelcome to §c§lFactions§f!",
            "§6❙ §7§oThis map was started on §6§l12/16/23",
            "§6❙ §eFactions §fis focused around providing the best ",
            "§6❙ §epossible §eVanilla §fexperience with a ton of new features.",
            "§r",
            "§6⬇ §fFor more information visit below §6⬇",
            "§r"
    ]), { p, t, s ->  })

    builder.set(3, 3, FastItemUtils.createItem(Material.GRASS_BLOCK, "§f⭑ §b§lHome World §f⭑", [
            "§r",
            "§3❙ §bHome World's §fare your private world where you can start building your creations",
            "§3❙ §fHome World's are §b1000x1000 §fVanilla Generated worlds.",
            "§3❙ §fYou can also invite friends to join you at your Private World",
            "§r",
            "§b§lRIGHT§f-§b§lCLICK §fto visit your §b§lHome World"
    ]), { p, t, s -> player.chat("/territory") })

    builder.set(3, 5, FastItemUtils.createItem(Material.WOODEN_AXE, "§e✪ §6§lJobs §e✪", [
            "§r",
            "§e❙ §fJobs offer several classes that you can progress in order to increase your skill set.",
            "§e❙ §fTo equip a class you must be holding the specified tool the class requires.",
            "§e❙ §fFor more information on all skills visit §6/jobs§f.",
            "§r",
            "§e§lRIGHT§f-§e§lCLICK §fto view all §6§lClasses"
    ]), { p, t, s -> player.chat("/jobs") })
    builder.set(3, 7, FastItemUtils.createItem(Material.PAPER, "§e⚠ §4§lDimensions §e⚠", [
            "",
            "§f§oWhat are §4Dimensions§o?",
            "§f▶ §4Dimensions §fcontain your much needed resources in order to progress.",
            "§f▶ §4Dimensions §falso carry §5Mobs §fand §cBosses§f. §aHealth §fand §cDamage §fvary per stage.",
            "§f▶ There are five stages of §cDifficulty§f. Each stage gradually gets harder to complete.",
            "§f▶ However, each stage also has different §eResource Rates §fand §eSpawns§f.",
            "§f▶ Every §cDimension §fyou enter provides a unique experience, as they're fully Vanilla.",
            "",
            "§f§l⬇ §c§lDimension Difficulties §f§l⬇",
            "§f➥ §aEasy§f: §7§oSome would describe as somewhat \"§aPeaceful\".",
            "§f➥ §eMedium§f: §7§oLets turn the difficulty up a tad bit, shall we?",
            "§f➥ §cHard§f: §7§oAlright, so here's where things might start to get tricky... (§aGrab some friends!§7)",
            "§f➥ §4§lBr§c§lu§4§ltal§f: §7§oHah, Good luck getting through this one on the first try.",
            "§f➥ §6§l§nI§e§l§nM§6§l§nP§e§l§nO§6§l§nS§e§l§nS§6§l§nI§e§l§nB§6§l§nL§e§l§nE§f: §7§oWell, I wish you luck - will you beat the odds? §e§lTIP§f: You'll need help... a lot.",
            "",
            "§c§lRIGHT§f-§c§lCLICK §fto view §c§lDimension Shop"
    ]), { p, t, s -> player.chat("/dshop") })

    builder.open(player)
}

Commands.create().assertPlayer().handler { command ->
    showTutorialMenu(command.sender())
}.register("tutorial", "help", "helpmenu")

