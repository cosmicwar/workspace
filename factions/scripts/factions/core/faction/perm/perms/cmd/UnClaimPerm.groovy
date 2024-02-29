package scripts.factions.core.faction.perm.perms.cmd

import org.bukkit.Material
import org.bukkit.entity.Player
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.Role
import scripts.factions.core.faction.perm.Permission

class UnClaimPerm extends Permission {

    static String unClaimInternalId = "un_claim_permission"
    static Role requiredRole = Role.OFFICER

    UnClaimPerm(String internalId) {
        super(unClaimInternalId, "§cUn-Claim §7Permission", [
                "",
                "§aAllows you to break blocks",
                "§ain a faction's claim."
        ], Material.NETHERITE_HOE, requiredRole)

        Factions.registerPermission(unClaimInternalId, this.get())

        this.baseFactionOnly = true
    }

    static boolean canAccess(Faction faction, Member member) {
        return hasRole(faction, member, unClaimInternalId, requiredRole)
    }
}
