package scripts.factions.features.itemfilter

import org.bukkit.Material

class FilterCategory {

    final String categoryId
    final Material categoryIcon
    final List<Material> containedMaterials

    FilterCategory(String categoryId, Material categoryIcon, List<Material> materials = new ArrayList<>()) {
        this.categoryId = categoryId
        this.categoryIcon = categoryIcon
        this.containedMaterials = materials
    }
}