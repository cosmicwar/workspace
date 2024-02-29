package scripts.factions.content.combat

import org.bukkit.entity.Entity

class CombatData {

    UUID id

    Entity lastDamager = null
    Long lastDamagerTime = null

    Long combatTagExpiration = null

    boolean bypassCombatTag = false

    CombatData(UUID id) {
        this.id = id
    }

    boolean isTagged() {
        return combatTagExpiration != null && combatTagExpiration > System.currentTimeMillis()
    }

    def setLastDamager(Entity entity) {
        lastDamager = entity
        lastDamagerTime = System.currentTimeMillis()
    }

}
