package scripts.factions.eco.loottable.v2.api

import com.google.common.collect.Maps
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material
import scripts.factions.data.uuid.UUIDDataObject

@CompileStatic(TypeCheckingMode.SKIP)
class LootTableCategory extends UUIDDataObject
{

    String name
    Material icon

    Set<UUID> tables

    @BsonIgnore transient LinkedHashMap<UUID, LootTable> tableCache = Maps.<UUID, LootTable> newLinkedHashMap()

    LootTableCategory() {}
    LootTableCategory(UUID id) {
        super(id)
    }

    LootTableCategory(String name, Material icon = Material.STONE) {
        this.name = name
        this.icon = icon
    }

    @BsonIgnore
    LootTable getTable(UUID uuid) {
        return tableCache.get(uuid) ?: null
    }

    @BsonIgnore
    boolean hasTable(String name) {
        return tableCache.values().any { it.name == name }
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }
}