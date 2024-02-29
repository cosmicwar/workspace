package scripts.factions.patch

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.starcade.starlight.helper.Commands
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MojangAPI

@CompileStatic(TypeCheckingMode.SKIP)
class InvseePatch {

    InvseePatch() {
        Commands.create().assertPlayer().assertPermission("starcade.commands.invsee").handler { ctx ->
            if (ctx.args().size() == 0) {
                ctx.sender().sendMessage("§cUsage: /invsee <player>")
                return
            }

            def arg = ctx.arg(0).parseOrFail(String)
            if (arg == ctx.sender().name) {
                ctx.sender().sendMessage("§cYou cannot open your own inventory.")
                return
            }

            def target = Bukkit.getPlayer(arg)
            if (target == null) {
                MojangAPI.getUUID(arg).with { uuid ->
                    if (uuid == null) ctx.sender().sendMessage("§cPlayer not found.")
                    else openInventory(ctx.sender(), uuid)
                }
            } else {
                openInventory(ctx.sender(), target.uniqueId)
            }
        }.register("invsee")
    }

    def openInventory(Player player, UUID targetId) {
        def target = Bukkit.getPlayer(targetId)
        if (target == null) {

        } else {
            createBragInventory(target).openSync(player)
        }
    }

    static MenuBuilder createBragInventory(Player player) {
        MenuBuilder builder

        builder = new MenuBuilder(45, "§e§l${player.getName()}'s Inventory")

        PlayerInventory playerInventory = player.getInventory()
        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack item = playerInventory.getItem(i)
            if (item == null || item.getType() == Material.AIR) continue

            ItemStack itemClone = item.clone()
            ItemMeta itemMeta = itemClone.getItemMeta()

            itemClone.setItemMeta(itemMeta)
            if (i >= 9 && i <= 17) {
                builder.get().setItem(i + 18, itemClone, { p, t, s ->
                    if (p.isOp()) {

                    }
                })
            } else if (i >= 27 && i <= 35) {
                builder.set(i - 18, itemClone, { p, t, s ->
                    if (p.isOp()) {

                    }
                })
            } else {
                builder.set(i, itemClone, {p, t, s ->
                    if (p.isOp()) {

                    }
                })
            }
        }

        builder.set(5, 3, player.getInventory().getHelmet() ?: new ItemStack(Material.AIR))
        builder.set(5, 4, player.getInventory().getChestplate() ?: new ItemStack(Material.AIR))
        builder.set(5, 6, player.getInventory().getLeggings() ?: new ItemStack(Material.AIR))
        builder.set(5, 7, player.getInventory().getBoots() ?: new ItemStack(Material.AIR))
        builder.set(5, 1, FastItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", []))
        builder.set(5, 2, FastItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", []))
        builder.set(5, 5, FastItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", []))
        builder.set(5, 8, FastItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", []))
        builder.set(5, 9, FastItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", []))

        return builder
    }

}
