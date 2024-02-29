package scripts.factions.eco.gambling

import org.starcade.starlight.helper.Commands
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MenuDecorator

static void showCasino(Player player) {
    MenuBuilder builder = new MenuBuilder(27, "§8Casino")

    MenuDecorator.decorate(builder, [
            "bfbfbfbfb",
            "fffffffff",
            "bfbfbfbfb"
    ])

    builder.set(10, FastItemUtils.createItem(Material.MAGMA_CREAM, "§8[§eCoinflip§8]", []), { p, t, s -> player.performCommand("cf") })
    builder.set(12, FastItemUtils.createItem(Material.PAINTING, "§8[§bScratch Off§8]", []), { p, t, s -> player.performCommand("scratch") })
    builder.set(14, FastItemUtils.createItem(Material.MAP, "§8[§cLottery§8]", []), { p, t, s -> player.performCommand("lottery") })

    ItemStack bar = FastItemUtils.createItem(Material.POTION, "§8[§dBar§8]", [])
    ItemMeta barMeta = bar.getItemMeta()
    barMeta.addItemFlags(ItemFlag.values())
    bar.setItemMeta(barMeta)

    builder.set(16, bar, { p, t, s -> player.performCommand("bar") })
    //builder.set(15, FastItemUtils.createItem(Material.NETHER_STAR, "§8[§aHigher§cLower§8]", []), { p, t, s -> player.performCommand("higherlower") })

    builder.open(player)
}

Commands.create().assertPlayer().handler { command ->
    showCasino(command.sender())
}.register("casino", "gamble", "gambling")