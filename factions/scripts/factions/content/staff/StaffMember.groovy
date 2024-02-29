package scripts.factions.content.staff

import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import scripts.factions.data.uuid.UUIDDataObject

class StaffMember extends UUIDDataObject {

    boolean staffMode = false

    @BsonIgnore transient ItemStack[] inventoryContents
    @BsonIgnore transient ItemStack[] armorContents

    StaffMember() {}

    @BsonIgnore
    def setStaffMode() {
        this.staffMode = !this.staffMode

        Bukkit.getPlayer(id).getInventory().content
        queueSave()
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }

}
