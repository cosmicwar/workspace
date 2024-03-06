package scripts.factions.content.clickitem

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Events
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils

@CompileStatic(TypeCheckingMode.SKIP)
class ClickItem {

    String _id
    ItemStack _stack

    InteractClickAction _interact = null
    InventoryClickAction _inventoryClick = null
    InteractConsumeAction _consume = null

    boolean showInGui = true
    boolean showInBeta = false

    static NamespacedKey metaKey

    ClickItem(String _id, ItemStack _stack, InteractClickAction _interact = null, InventoryClickAction _inventoryClick = null, InteractConsumeAction _consume = null) {
        this._id = _id
        this._stack = _stack
        this._interact = _interact
        this._inventoryClick = _inventoryClick
        this._consume = _consume

        metaKey = new NamespacedKey(Starlight.plugin, "clickitem_${_id}")

        events()
    }

    def events() {
        Events.subscribe(PlayerInteractEvent.class).handler { event ->
            if (_interact == null) return

            def item = event.getItem()
            if (item == null || item.type == Material.AIR) return

            if (FastItemUtils.getCustomTag(item, metaKey, ItemTagType.STRING) != this._id) return

             _interact.e(event.getPlayer(), event, this)
        }

        Events.subscribe(InventoryClickEvent.class).handler { event ->
            if (_inventoryClick == null) return

            if (event.getWhoClicked() !instanceof Player) return
            def player = event.getWhoClicked() as Player

            def item = event.getCurrentItem()
            if (item == null || item.type == Material.AIR) return

            def cursor = event.getCursor()
            if (cursor == null || cursor.type == Material.AIR) return

            if (FastItemUtils.getCustomTag(cursor, metaKey, ItemTagType.STRING) != this._id) return

            _inventoryClick.e(player, event, this)
        }

        Events.subscribe(PlayerItemConsumeEvent.class).handler { event ->
            if (_consume == null) return

            def player = event.player

            def item = event.item
            if (item == null || item.type == Material.AIR) return

            if (FastItemUtils.getCustomTag(item, metaKey, ItemTagType.STRING) != this._id) return

            _consume.e(player, event, this)
        }
    }

    def giveStack(Player player, String message = null) {
        def item = _stack.clone()

        FastItemUtils.setCustomTag(item, metaKey, ItemTagType.STRING, _id)

        FastInventoryUtils.addOrBox(player.getUniqueId(), player, null, item, message)
    }

    def giveStack(Player player, ItemStack stack, String message = null) {
        FastItemUtils.setCustomTag(stack, metaKey, ItemTagType.STRING, _id)

        FastInventoryUtils.addOrBox(player.getUniqueId(), player, null, stack, message)
    }

    def getStack(ItemStack stack) {
        FastItemUtils.setCustomTag(stack, metaKey, ItemTagType.STRING, _id)
        return stack
    }

    static def consume(Player player, String id, Sound sound = null) {
        def stack = player.getInventory().getItemInMainHand()

        if (stack == null || stack.type == Material.AIR) return

        if (FastItemUtils.getCustomTag(stack, metaKey, ItemTagType.STRING) != id) return

        if (stack.amount == 1) {
            player.getInventory().setItemInMainHand(null)
        } else {
            stack.amount = stack.amount - 1
            player.getInventory().setItemInMainHand(stack)
        }

        if (sound != null) player.playSound(player.getLocation(), sound, 1f, 1f)
        player.updateInventory()
    }

}

@CompileStatic(TypeCheckingMode.SKIP)
interface InteractClickAction {
    void e(Player player, PlayerInteractEvent event, ClickItem clickItem)
}

@CompileStatic(TypeCheckingMode.SKIP)
interface InventoryClickAction {
    void e(Player player, InventoryClickEvent event, ClickItem clickItem)
}

@CompileStatic(TypeCheckingMode.SKIP)
interface InteractConsumeAction {
    void e(Player player, PlayerItemConsumeEvent event, ClickItem clickItem)
}

@CompileStatic(TypeCheckingMode.SKIP)
interface InteractAndConsumeAction {
    void e(Player player, PlayerInteractEvent event1, PlayerItemConsumeEvent event2, ClickItem clickItem)
}

