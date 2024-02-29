package scripts.factions.features.customset.data

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.factions.features.customset.struct.CustomSet
import scripts.shared.utils.PersistentItemData

class CustomSetItemData extends PersistentItemData {

    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "CustomSetItemData")
    private static final Closure<CustomSet> getCustomSet = Exports.ptr("customset:getSetById") as Closure

    String setName
    boolean heroic = false

    List<String> crystalSetNames = []
    boolean crystals = false

    CustomSetItemData(String setName = "none", boolean heroic = false) {
        this.setName = setName
        this.heroic = heroic
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static CustomSetItemData read(ItemStack itemStack) {
        return read(itemStack, DATA_KEY, CustomSetItemData.class) as CustomSetItemData
    }

    CustomSet getSet() {
        if (setName == "none") return null

        return getCustomSet.call(this.setName) as CustomSet
    }

    List<CustomSet> getCrystalSets() {
        def sets = []

        for (String setName : this.crystalSetNames) {
            sets.add(getCustomSet.call(setName) as CustomSet)
        }

        return sets
    }
}

