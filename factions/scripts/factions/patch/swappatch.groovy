package scripts.factions.patch

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.protocol.Protocol
import org.starcade.starlight.helper.utils.Players

Protocol.subscribe(PacketType.Play.Client.WINDOW_CLICK).handler({
    if (it.playerTemporary) return

    Player player = it.player
    PacketContainer packetContainer = it.getPacket()
    if (packetContainer.getIntegers().read(2) == 40) {
        it.setCancelled(true)
        Players.msg(player, "§c§lItem swapping currently disabled!")
        Schedulers.sync().run {
            player.updateInventory()
        }
    }
})

Events.subscribe(PlayerSwapHandItemsEvent.class).handler { event ->
    event.setCancelled(true)
    event.getPlayer().sendMessage("§c§lItem swapping currently disabled.")
}

Events.subscribe(InventoryClickEvent.class).filter(EventFilters.ignoreCancelled()).handler { event ->
    // handling shield swapping to offhand by shift clicking it.
    ItemStack currentItem = event.getCurrentItem()
    if ((event.click == ClickType.SHIFT_LEFT || event.click == ClickType.SHIFT_RIGHT) && currentItem != null && currentItem.getType() == Material.SHIELD) {
        event.setCancelled(true)
    }

    if (event.click != ClickType.SWAP_OFFHAND) return
    event.setCancelled(true)
    event.getWhoClicked().sendMessage("§c§lItem swapping currently disabled.")
}

Events.subscribe(CraftItemEvent.class).filter(EventFilters.ignoreCancelled()).handler({ event ->
    if (event.click != ClickType.SWAP_OFFHAND) return

    event.setCancelled(true)
    Player player = event.getWhoClicked() as Player
    Players.msg(player, "§c§lItem swapping currently disabled.")
})