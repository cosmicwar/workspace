package scripts.factions.features.customitem.buckets.data

import groovy.transform.CompileStatic
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import scripts.factions.features.customitem.buckets.util.GenDirection
import scripts.shared.utils.PersistentItemData

@CompileStatic
class GenBucketData extends PersistentItemData {
    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "GenBucketData")

    String name
    GenDirection direction
    Material material
    int cost

    GenBucketData(String name, GenDirection direction, Material material, int cost) {
        this.direction = direction
        this.material = material
        this.name = name
        this.cost = cost
    }

    String getName() {
        return name
    }

    GenDirection getDirection() {
        return direction
    }

    Material getMaterial() {
        return material
    }

    int getCost() {
        return cost
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static GenBucketData read(ItemStack itemStack) {
        return (GenBucketData) read(itemStack, DATA_KEY, GenBucketData.class)
    }
}

