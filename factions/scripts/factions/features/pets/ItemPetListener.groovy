package scripts.factions.features.pets

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players
import scripts.factions.features.pets.struct.ItemPetData
import scripts.shared.utils.Formats

@CompileStatic(TypeCheckingMode.SKIP)
class ItemPetListener {

    ItemPetListener() {
        events()
    }

    static def events() {
        Events.subscribe(PlayerInteractEvent.class).handler {event ->
            def player = event.getPlayer()
            def item = event.getItem()

            if (item == null || item.type == Material.AIR) return

            def data = ItemPetData.read(item)
            if (data == null) return

            def pet = data.getPet()
            if (pet == null) return

            if (pet.onCooldown(item)) {
                player.sendMessage("§cThis pet is on cooldown for another §f${Formats.formatTimeMillis(pet.getCooldown(item) - System.currentTimeMillis())} §cseconds.")
                return
            }

            pet.onInteract(player, data.level, event)
        }

        Events.subscribe(BlockPlaceEvent.class).handler {event ->
            def player = event.getPlayer()
            def item = event.getItemInHand()

            if (item == null || item.type == Material.AIR) return

            def data = ItemPetData.read(item)
            if (data == null) return

            event.setCancelled(true)
            Players.msg(player, "§cYou can't place this item.")
        }
    }

    static Map<Integer, ItemStack> getPetItems(Player player) {
        Map<Integer, ItemStack> items = [:]

        int index = 0
        player.getInventory().getContents().each {
            if (it != null && it.type != Material.AIR) {
                ItemPetData data = ItemPetData.read(it)

                if (data != null)
                    items.put(index, it)
            }

            index++
        }

        return items
    }

    static List<ItemPetData> getEquippedPets(Player player) {
        def pets = []

        getPetItems(player).each { index, itemStack ->
            ItemPetData data = ItemPetData.read(itemStack)
            if (data != null) {
                pets.add(data)
            }
        }

        return pets
    }

}
