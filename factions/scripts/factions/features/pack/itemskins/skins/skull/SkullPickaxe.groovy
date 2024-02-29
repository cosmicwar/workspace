package scripts.factions.features.pack.itemskins.skins.skull

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scripts.factions.features.pack.itemskins.ItemSkin

class SkullPickaxe extends ItemSkin {

    SkullPickaxe() {
        super("skullpickaxe")
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