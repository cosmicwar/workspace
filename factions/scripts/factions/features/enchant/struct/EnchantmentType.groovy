package scripts.factions.features.enchant.struct

import org.bukkit.Material

enum EnchantmentType {
    NORMAL("Normal", "§f", Material.WHITE_STAINED_GLASS_PANE),
    PROC("Proc", "§a", Material.LIME_STAINED_GLASS_PANE),
    TICKING("Ticking", "§b", Material.LIGHT_BLUE_STAINED_GLASS_PANE),
    POTION("Potion", "§d", Material.MAGENTA_STAINED_GLASS_PANE);

    String typeName
    String typeColor
    Material glassPane

    EnchantmentType(String typeName, String typeColor, Material glassPane) {
        this.typeName = typeName
        this.typeColor = typeColor
        this.glassPane = glassPane
    }

    static EnchantmentType fromName(String name) {
        for (EnchantmentType type : values()) {
            if (type.typeName.equalsIgnoreCase(name)) return type
        }
        return null
    }

    boolean isProc() {
        return this == PROC
    }
}