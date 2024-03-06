package scripts.factions.eco.rewardbox.data

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import scripts.shared.data.string.StringDataObject
import scripts.factions.eco.loottable.LootTableHandler
import scripts.factions.eco.loottable.v2.api.LootTable
import scripts.factions.eco.rewardbox.RewardBoxes
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.utils.DataUtils

// TODO: this will currently not work right if the internal id is updated, the items will break most likely
// To fix, change this to use an internal id (UUID) and a display id
@CompileStatic(TypeCheckingMode.SKIP)
class RewardBox extends StringDataObject {

    String itemName = ""
    List<String> itemLore = []

    Material itemMaterial = Material.BEACON

    int minRewards = 1
    int maxRewards = 1

    int finalRewardAmount = 1

    boolean enabled = true
    boolean displayBox = false

    UUID lootTableId = null

    RewardBox() {}

    RewardBox(String id) {
        super(id)
    }

    @BsonIgnore
    LootTable getLootTable() {
        return LootTableHandler.getLootTable(lootTableId)
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }

    @BsonIgnore
    ItemStack createItemStack(boolean withTag = false, boolean antiDupe = true) {
        def item = FastItemUtils.createItem(itemMaterial, itemName, itemLore)

        if (withTag) DataUtils.setTag(item, RewardBoxes.rewardBoxKey, PersistentDataType.STRING, id)
        if (antiDupe) FastItemUtils.ensureUnique(item)

        FastItemUtils.addGlow(item)

        return item
    }

}