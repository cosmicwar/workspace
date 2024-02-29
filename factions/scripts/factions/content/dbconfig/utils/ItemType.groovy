package scripts.factions.content.dbconfig.utils

import org.bukkit.inventory.ItemStack

enum ItemType {

    DEFAULT,
    SWORD,
    PICKAXE,
    AXE,
    SHOVEL,
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    NULL

    static ItemType fromItem(ItemStack stack) {
        if (stack == null || stack.type.isAir()) return NULL
        if (stack.getType().name().endsWith("_HELMET")) return HELMET
        if (stack.getType().name().endsWith("_CHESTPLATE")) return CHESTPLATE
        if (stack.getType().name().endsWith("_LEGGINGS")) return LEGGINGS
        if (stack.getType().name().endsWith("_BOOTS")) return BOOTS
        if (stack.getType().name().endsWith("_SWORD")) return SWORD
        if (stack.getType().name().endsWith("_PICKAXE")) return PICKAXE
        if (stack.getType().name().endsWith("_AXE")) return AXE
        if (stack.getType().name().endsWith("_SHOVEL")) return SHOVEL

        return DEFAULT
    }

}