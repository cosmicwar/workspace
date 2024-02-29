package scripts.factions.features.enchant.struct

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

@CompileStatic(TypeCheckingMode.SKIP)
enum EnchantPriority {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4);

    int priority

    EnchantPriority(int priority) {
        this.priority = priority
    }

    static EnchantPriority fromName(String name) {
        for (EnchantPriority priority : values()) {
            if (priority.name().equalsIgnoreCase(name)) return priority
        }
        return null
    }
}