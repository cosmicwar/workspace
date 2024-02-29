package scripts.factions.features.pack.itemskins.skins.halloween

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scripts.factions.features.pack.itemskins.ItemSkin

class HalloweenHoe extends ItemSkin {

    HalloweenHoe() {
        super("halloweenhoe")
    }

    @Override
    void onEquip(Player player, ItemStack itemStack) {
        player.sendMessage("${getDisplayName()} equipped!")
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack) {
        player.sendMessage("${getDisplayName()} unequipped!")
    }
}

