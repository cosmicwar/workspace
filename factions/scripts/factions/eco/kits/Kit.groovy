package scripts.factions.eco.kits

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material
import scripts.shared.data.uuid.UUIDDataObject

@CompileStatic
class Kit extends UUIDDataObject
{
    String name
    List<String> description

    KitType type = KitType.NORMAL

    Material inventoryMaterial = Material.CHEST
    List<String> items = []
    List<String> armorContents = []
    Integer priority = 0
    Integer cd = -1

    Kit() {}

    Kit(UUID kitId) {
        super(kitId)
    }

    Kit(UUID kitId, String name, List<String> description, int priority, List<String> items = [], List<String> armorContents = []) {
        super(kitId)

        this.name = name
        this.description = description
        this.priority = priority
        this.items = items
        this.armorContents = armorContents
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }
}
