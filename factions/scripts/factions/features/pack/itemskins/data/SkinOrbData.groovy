package scripts.factions.features.pack.itemskins.data

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.factions.features.pack.itemskins.ItemSkin
import scripts.factions.features.pack.itemskins.utils.ItemSkinType
import scripts.shared.utils.PersistentItemData

@CompileStatic(TypeCheckingMode.SKIP)
class SkinOrbData extends PersistentItemData {
    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "SkinOrbData")

    private static final Closure<ItemSkin> getItemSkin = Exports.ptr("itemskins:getSkinById") as Closure

    final String itemSkin
    final ItemSkinType applicability

    SkinOrbData(String itemSkin, ItemSkinType applicability) {
        this.itemSkin = itemSkin
        this.applicability = applicability
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static SkinOrbData read(ItemStack itemStack) {
        return (SkinOrbData) read(itemStack, DATA_KEY, SkinOrbData.class)
    }

    ItemSkin getItemSkin() {
        return getItemSkin.call(itemSkin) as ItemSkin
    }

    boolean canBeAppliedTo(ItemStack itemStack) {
        return applicability.isType(itemStack)
    }
}
