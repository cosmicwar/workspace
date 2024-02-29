package scripts.factions.data.obj

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.inventory.ItemStack
import scripts.factions.content.dbconfig.utils.ItemType
import scripts.shared.legacy.utils.FastItemUtils

@CompileStatic(TypeCheckingMode.SKIP)
class SItem {

    String itemStackSerialized = null
    ItemType itemType

    SItem() {}

    SItem(String itemStackSerialized, ItemType itemType = ItemType.DEFAULT) {
        this.itemStackSerialized = itemStackSerialized
        this.itemType = itemType
    }

    @BsonIgnore
    static SItem of(ItemStack stack) {
        if (stack == null || stack.type.isAir()) return null

        return new SItem(FastItemUtils.convertItemStackToString(stack), ItemType.fromItem(stack))
    }
}
