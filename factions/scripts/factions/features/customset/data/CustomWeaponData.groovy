package scripts.factions.features.customset.data

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import scripts.factions.features.customset.struct.CustomSet
import scripts.shared.utils.PersistentItemData

class CustomWeaponData extends PersistentItemData {

    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "CustomWeaponData")
    private static final Closure<CustomSet> getCustomSet = Exports.ptr("customset:getSetById") as Closure

    final String setName
    boolean heroic

    CustomWeaponData(String setName, boolean heroic = false) {
        this.setName = setName
        this.heroic = heroic
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static CustomWeaponData read(ItemStack itemStack) {
        return read(itemStack, DATA_KEY, CustomWeaponData.class) as CustomWeaponData
    }

    CustomSet getSet() {
        return getCustomSet.call(this.setName) as CustomSet
    }
}

