package scripts.factions.features.customset.data

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import scripts.factions.features.customset.struct.CustomSet
import scripts.shared.utils.PersistentItemData

class CrystalExtractorItemData extends PersistentItemData {

    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "CrystalExtractorItemData")

    double success

    CrystalExtractorItemData(double success = 100.0D) {
        this.success = success
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static CrystalExtractorItemData read(ItemStack itemStack) {
        return read(itemStack, DATA_KEY, CrystalExtractorItemData.class) as CrystalExtractorItemData
    }

    List<CustomSet> getSet() {
        def sets = []

        for (String setName : this.setNames) {
            sets.add(getCustomSet.call(setName) as CustomSet)
        }

        return sets
    }
}
