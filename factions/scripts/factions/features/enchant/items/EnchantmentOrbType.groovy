package scripts.factions.features.enchant.items

import groovy.transform.CompileStatic
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@CompileStatic()
enum EnchantmentOrbType {
    ARMOR("Armor"),
    WEAPON("Weapon");

    public String label

    EnchantmentOrbType(String label) {
        this.label = label
    }

    boolean isType(ItemStack stack) {
        if (this == ARMOR) {
            return stack.getType().name().endsWith("_HELMET") || stack.getType().name().endsWith("_CHESTPLATE") || stack.getType().name().endsWith("_LEGGINGS") || stack.getType().name().endsWith("_BOOTS")
        } else if (this == WEAPON) {
            return stack.getType().name().endsWith("_SWORD") || stack.getType().name().endsWith("_AXE") || stack.getType().name().endsWith("_BOW")
        }
        return false
    }
}

