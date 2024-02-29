package scripts.factions.features.customset.data

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import scripts.factions.features.customset.struct.CustomSet
import scripts.shared.utils.PersistentItemData

class CrystalItemData extends PersistentItemData {

    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "CrystalItemData")
    private static final Closure<CustomSet> getCustomSet = Exports.ptr("customset:getSetById") as Closure

    List<String> setNames
    double successRate = 100.0D

    CrystalItemData(List<String> setNames) {
        this.setNames = setNames
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static CrystalItemData read(ItemStack itemStack) {
        return read(itemStack, DATA_KEY, CrystalItemData.class) as CrystalItemData
    }

    List<CustomSet> getSet() {
        def sets = []

        for (String setName : this.setNames) {
            sets.add(getCustomSet.call(setName) as CustomSet)
        }

        return sets
    }
}
