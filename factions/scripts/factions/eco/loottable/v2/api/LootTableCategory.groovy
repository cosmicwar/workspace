package scripts.factions.eco.loottable.v2.api

import com.google.common.collect.Maps
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material
import scripts.shared.data.uuid.UUIDDataManager
import scripts.shared.data.uuid.UUIDDataObject

@CompileStatic(TypeCheckingMode.SKIP)
class LootTableCategory extends UUIDDataObject {

    String name
    Material icon = Material.STONE

    Set<UUID> tables = []

    @BsonIgnore
    transient LinkedHashMap<UUID, LootTable> tableCache = Maps.<UUID, LootTable> newLinkedHashMap()

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

    @BsonIgnore
    LootTable getOrCreateTable(UUID uuid, String defaultName = "default") {
        if (!tables.contains(uuid)) tables.add(uuid)
        if (tableCache.containsKey(uuid)) return tableCache.get(uuid)

        def table = UUIDDataManager.getData(uuid, LootTable.class)
        if (table.name == "default") {
            table.name = defaultName
            table.queueSave()
        }

        tableCache.put(uuid, table)
        queueSave()

        return table
    }

    @BsonIgnore
    @Override
    boolean isEmpty() {
        return false
    }
}