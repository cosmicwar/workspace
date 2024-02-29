package scripts.factions.features.enchant.data.item

import groovy.transform.CompileStatic
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import scripts.shared.utils.PersistentItemData


@CompileStatic
class SoulPearlData extends PersistentItemData {
    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "SoulPearlData")

    Boolean soulPearl

    SoulPearlData(Boolean soulPearl = true) {
        this.soulPearl = soulPearl
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static SoulPearlData read(ItemStack itemStack) {
        return (SoulPearlData) read(itemStack, DATA_KEY, SoulPearlData.class)
    }
}
