package scripts.factions.eco.gambling

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.helper.Commands
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MenuDecorator

static def openCaseBattleMenu(Player player) {
    MenuBuilder builder

    builder = new MenuBuilder(5 * 9, "§3Case Battle")
    MenuDecorator.decorate(builder, [
            "383838383",
            "838383838",
            "383838383",
            "838383838",
            "383838383",
    ])

    builder.set(3, 4, FastItemUtils.createItem(Material.PAPER, "§bCreate a Battle", ["§7Click to create a case battle."]), {p, t, s ->

    })

    builder.set(3, 6, FastItemUtils.createItem(Material.PAPER, "§bJoin a Battle", ["§7Click to join a case battle."]), {p, t, s ->

    })

    builder.set(5, 3, FastItemUtils.createItem(Material.SUNFLOWER, "§aStats", ["§7Click to view your statistics."]), {p, t, s ->

    })

    builder.set(5, 7, FastItemUtils.createItem(Material.DIAMOND, "§aRewards", ["§7Click to view your rewards."]), {p, t, s ->

    })

    builder.set(5, 5, FastItemUtils.createItem(Material.BARRIER, "§cClose Menu", []), {p, t, s ->
        p.closeInventory()
    })

    builder.openSync(player)
}

static def createCaseBattle(Player player) {

}

static def joinCaseBattle(Player player, page = 1) {

}

static def viewCaseBattleStats(Player player) {

}

static def viewCaseBattleRewards(Player player, page = 1) {
    MenuBuilder builder

    builder = MenuUtils.createLargePagedMenu("§3Case Battle Rewards")

    builder.openSync(player)
}

Commands.create().assertPlayer().handler {cmd ->
    openCaseBattleMenu(cmd.sender())
}.register("casebattle", "battle")

@CompileStatic(TypeCheckingMode.SKIP)
class CaseBattleUtils {


}

@CompileStatic
class CaseBattle {

    UUID id
    UUID owner
    String currency
    Long amount
    String color
    UUID opponent
    ItemStack item
    ItemStack opponentItem


    CaseBattle(UUID id, UUID owner = null, String currency = null, Long amount = null, String color = null, UUID opponent = null, ItemStack item = null, ItemStack opponentItem = null) {
        this.id = id
        this.owner = owner
        this.currency = currency
        this.amount = amount
        this.color = color
        this.opponent = opponent
        this.item = item
        this.opponentItem = opponentItem
    }

    CaseBattle(Object coinflip) {
        this.id = coinflip["id"] as UUID ?: null
        this.owner = coinflip["owner"] as UUID ?: null
        this.currency = coinflip["currency"] as String ?: null
        this.amount = coinflip["amount"] as Long ?: null
        this.color = coinflip["color"] as String ?: null
        this.opponent = coinflip["opponent"] as UUID ?: null
        this.item = coinflip["item"] as ItemStack ?: null
        this.opponentItem = coinflip["opponentItem"] as ItemStack ?: null
    }


    boolean isPrivate() {
        return opponent != null && opponent != owner
    }

    boolean isItem() {
        return item != null || currency == "items"
    }

    boolean isValidCoinflip() {
        return id != null && owner != null && currency != null && amount != null && color != null
    }
}