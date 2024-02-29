package scripts.factions.features.pack.itemskins.skins.samurai

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scripts.factions.features.pack.itemskins.ItemSkin

class SamuraiSword extends ItemSkin {

    SamuraiSword() {
        super("samuraisword")
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