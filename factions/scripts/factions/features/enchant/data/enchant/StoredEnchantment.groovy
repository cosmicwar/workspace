package scripts.factions.features.enchant.data.enchant

import groovy.transform.CompileStatic

@CompileStatic
class StoredEnchantment {

    final String enchantment
    final int level
    boolean heroic = false
    String overrideEnchant = null

    StoredEnchantment(String enchantment, int level, overrideEnchant = null) {
        this.enchantment = enchantment
        this.level = level
        this.overrideEnchant = overrideEnchant
    }
}

