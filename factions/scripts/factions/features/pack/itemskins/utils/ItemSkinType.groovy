package scripts.factions.features.pack.itemskins.utils

import groovy.transform.CompileStatic
import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

import java.util.concurrent.ConcurrentHashMap

@CompileStatic
enum ItemSkinType {

    HELMET("Helmet", EquipmentSlot.HEAD, Material.NETHERITE_HELMET, [Material.NETHERITE_HELMET] as Set<Material>),
    CHESTPLATE("Chestplate", EquipmentSlot.CHEST, Material.NETHERITE_CHESTPLATE, [Material.NETHERITE_CHESTPLATE] as Set<Material>),
    LEGGINGS("Leggings", EquipmentSlot.LEGS, Material.NETHERITE_LEGGINGS, [Material.NETHERITE_LEGGINGS] as Set<Material>),
    BOOTS("Boots", EquipmentSlot.FEET, Material.NETHERITE_BOOTS, [Material.NETHERITE_BOOTS] as Set<Material>),
    SWORD("Sword", EquipmentSlot.HAND, Material.NETHERITE_SWORD, [Material.NETHERITE_SWORD] as Set<Material>),
    BOW("Bow", EquipmentSlot.HAND, Material.BOW, [Material.BOW, Material.CROSSBOW] as Set<Material>),
    PICKAXE("Pickaxe", EquipmentSlot.HAND, Material.NETHERITE_PICKAXE, [Material.NETHERITE_PICKAXE] as Set<Material>),
    SHOVEL("Shovel", EquipmentSlot.HAND, Material.NETHERITE_SHOVEL, [Material.NETHERITE_SHOVEL] as Set<Material>),
    AXE("Axe", EquipmentSlot.HAND, Material.NETHERITE_AXE, [Material.NETHERITE_AXE] as Set<Material>),
    HOE("Hoe", EquipmentSlot.HAND, Material.NETHERITE_HOE, [Material.NETHERITE_HOE] as Set<Material>),
    ROD("Hoe", EquipmentSlot.HAND, Material.FISHING_ROD, [Material.FISHING_ROD] as Set<Material>)

    static Map<Material, ItemSkinType> materialItemTypes = new ConcurrentHashMap<>()

    final String displayName
    final String displayNamePlural
    final EquipmentSlot equipmentSlot
    final Material icon
    final Set<Material> materials

    ItemSkinType(String displayName, EquipmentSlot equipmentSlot, Material icon, Set<Material> materials) {
        this.displayName = displayName
        this.displayNamePlural = displayName.endsWith("s") ? displayName : displayName + "s"
        this.equipmentSlot = equipmentSlot
        this.icon = icon
        this.materials = materials
    }

    boolean isHoldable() {
        return equipmentSlot == EquipmentSlot.HAND
    }

    boolean isArmor() {
        return equipmentSlot == EquipmentSlot.HEAD || equipmentSlot == EquipmentSlot.CHEST || equipmentSlot == EquipmentSlot.LEGS || equipmentSlot == EquipmentSlot.FEET
    }

    boolean matches(ItemStack itemStack) {
        if (itemStack == null) return false
        return materials.contains(itemStack.getType())
    }

    static ItemSkinType getTypeOf(ItemStack itemStack) {
        if (itemStack == null) return null

        return materialItemTypes.computeIfAbsent(itemStack.getType(), v -> values().find { it.materials.contains(v) })
    }

    boolean isType(final ItemStack item) {
        if (item == null) {
            return false
        }
        final String name = this.getModifiedName()
        return item.getType().name().endsWith(name)
    }

    String getModifiedName() {
        return this.name()
    }

}


