package scripts.factions.core.faction.perm.perms.cmd

import org.bukkit.Material
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.Role
import scripts.factions.core.faction.perm.Permission

class RelationChangePerm extends Permission {

    static String relationChangeInternalId = "relation_change_permission"
    static Role requiredRole = Role.OFFICER

    RelationChangePerm() {
        super(relationChangeInternalId, "§aRelation Change §7Permission", [
                "",
                "§aAllows you to break blocks",
                "§ain a faction's claim."
        ], Material.NETHERITE_HOE, requiredRole)

        Factions.registerPermission(relationChangeInternalId, this.get())

        this.baseFactionOnly = true
    }

    static boolean canAccess(Faction faction, Member member) {
        return hasRole(faction, member, relationChangeInternalId, requiredRole)
    }
}
