package scripts.factions.features.itemfilter

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material

@CompileStatic
class FilterOptions {

    String id
    Material optionMaterial
    List<Material> enabledMaterials

    FilterOptions() { }

    FilterOptions(String id, Material material = Material.IRON_BARS, List<Material> data = new ArrayList<>()) {
        this.id = id
        this.optionMaterial = material
        this.enabledMaterials = data
    }

    @BsonIgnore
    boolean isEmpty() {
        return enabledMaterials.isEmpty()
    }
}