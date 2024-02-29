package scripts.factions.features.pack.itemskins.skins.christmas

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scripts.factions.features.pack.itemskins.ItemSkin

class ChristmasChestplate extends ItemSkin {

    ChristmasChestplate() {
        super("christmaschestplate")
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

