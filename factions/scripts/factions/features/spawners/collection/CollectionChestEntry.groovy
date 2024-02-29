package scripts.factions.features.spawners.collection

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material
import scripts.shared.legacy.ShopUtils

@CompileStatic
class CollectionChestEntry {

    String categoryId
    Material material

    int amount = 0

    boolean enabled = false

    CollectionChestEntry(){}

    CollectionChestEntry(String categoryId, Material material, int amount = 0, boolean enabled = false){
        this.categoryId = categoryId
        this.material = material
        this.amount = amount
        this.enabled = enabled
    }

    @BsonIgnore
    double getSellAmount() {
        return ShopUtils.getSellPrice(this.material)
    }

    @BsonIgnore
    double getTotalSellAmount() {
        return ShopUtils.getSellPrice(this.material) * (amount as double)
    }

    @BsonIgnore
    static boolean isEmpty() {
        return false
    }
}

