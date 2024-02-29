package scripts.factions.features.pack.itemskins.data

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.factions.features.pack.itemskins.ItemSkin
import scripts.shared.utils.PersistentItemData

@CompileStatic(TypeCheckingMode.SKIP)
class SkinnedItemData extends PersistentItemData {
    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "ItemSkinData")

    private static final Closure<ItemSkin> getItemSkin = Exports.ptr("itemskins:getSkinById") as Closure

    String itemSkin

    boolean isOverride = false

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static SkinnedItemData read(ItemStack itemStack) {
        return (SkinnedItemData) read(itemStack, DATA_KEY, SkinnedItemData.class)
    }

    Boolean hasItemSkin() {
        if (itemSkin == null) return false
        return getItemSkin.call(itemSkin) != null
    }

    ItemSkin getItemSkin() {
        return getItemSkin.call(itemSkin) as ItemSkin
    }
}