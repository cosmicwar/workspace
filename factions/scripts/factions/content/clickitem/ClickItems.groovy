package scripts.factions.content.clickitem

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Commands
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.systems.MenuEvent

import java.util.concurrent.ConcurrentHashMap

@CompileStatic(TypeCheckingMode.SKIP)
class ClickItems {

    static Map<String, ClickItem> items = new ConcurrentHashMap<>()

    ClickItems() {
        register("test", new ClickItem("test", new ItemStack(Material.DIAMOND), null, { Player player, PlayerInteractEvent event, ClickItem item ->
            player.sendMessage("test")
        }))

        Commands.create().assertOp().assertPlayer().handler {cmd ->
            Player player = cmd.sender()
            openGui(player)
        }.register("customitem", "items", "clickitems")
    }

    static List<ClickItem> getClickItems() {
        return items.values().toList()
    }

    static NamespacedKey metaKey = new NamespacedKey(Starlight.plugin, "clickitemgui")

    def openGui(Player player, int page = 1) {
        MenuBuilder builder

        builder = MenuUtils.createPagedMenu("§8Items",  getClickItems().findAll { it.showInGui && it._stack != null }, { ClickItem item, Integer i ->
            def stack = item._stack.clone()

            List<String> lore = FastItemUtils.getLore(stack) ?: []

            lore.add("")
            lore.add("§dInternal ID: ${item._id}")
            lore.add("§dInventoryClick: ${item._inventoryClick != null ? "§aEnabled" : "§cDisabled"}")
            lore.add("§dInteractClick: ${item._interact != null ? "§aEnabled" : "§cDisabled"}")
            lore.add("")
            lore.add("§7§oClick to give")

            FastItemUtils.setLore(stack, lore)
            FastItemUtils.setCustomTag(stack, metaKey, ItemTagType.STRING, item._id)

            return stack
        }, page, false, [
                { Player p, ClickType t, int s ->
                    def item = builder.get().getItem(s)

                    if (item == null || item.getType() == Material.AIR) return

                    if (!FastItemUtils.hasCustomTag(item, metaKey, ItemTagType.STRING)) return

                    def string = FastItemUtils.getCustomTag(item, metaKey, ItemTagType.STRING)

                    if (!items.containsKey(string)) return

                    items.get(string).giveStack(p)
                },
                { Player p, ClickType t, int s -> openGui(p, page + 1) },
                { Player p, ClickType t, int s -> openGui(p, page - 1) }
        ] as List<MenuEvent>)

        builder.openSync(player)
    }

    static ClickItem register(String id, ClickItem item) {
        return items.put(id, item)
    }

    static ClickItem register(ClickItem item) {
        return items.put(item._id, item)
    }

    static def giveItem(String id, Player player) {
        if (!items.containsKey(id)) return
        items.get(id).giveStack(player)
    }

}

