package scripts.factions.features.enchant.struct

import org.bukkit.Material

enum EnchantmentTier {

    SIMPLE(0, "Simple", "§7", Material.WHITE_STAINED_GLASS_PANE),
    UNIQUE(1, "Unique", "§a", Material.LIME_STAINED_GLASS_PANE),
    ELITE(2, "Elite", "§b", Material.LIGHT_BLUE_STAINED_GLASS_PANE),
    ULTIMATE(3, "Ultimate", "§e", Material.YELLOW_STAINED_GLASS_PANE),
    LEGENDARY(4, "Legendary", "§6", Material.ORANGE_STAINED_GLASS_PANE),
    SOUL(5, "Soul", "§c", Material.RED_STAINED_GLASS_PANE),
    HEROIC(6, "Heroic", "§d", Material.MAGENTA_STAINED_GLASS_PANE),
    GALAXY(7, "Galaxy", "§3", Material.CYAN_STAINED_GLASS_PANE)

    int weight
    String tierName
    String tierColor
    Material glassPane

    EnchantmentTier(int weight, String tierName, String tierColor, Material glassPane) {
        this.weight = weight
        this.tierName = tierName
        this.tierColor = tierColor
        this.glassPane = glassPane
    }

    static EnchantmentTier fromName(String name) {
        for (EnchantmentTier tier : values()) {
            if (tier.tierName.equalsIgnoreCase(name)) return tier
        }
        return null
    }

    static EnchantmentTier fromWeight(int weight) {
        for (EnchantmentTier tier : values()) {
            if (tier.weight == weight) return tier
        }
        return null
    }
}

