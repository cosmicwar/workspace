package scripts.factions.core.faction.perm.perms.cmd

import org.bukkit.Material
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.Role
import scripts.factions.core.faction.perm.Permission

class InvitePerm extends Permission {

    static String inviteInternalId = "invite_permission"
    static Role requiredRole = Role.OFFICER

    InvitePerm() {
        super(inviteInternalId, "§aInvite §7Permission", [
                "",
                "§aAllows you to break blocks",
                "§ain a faction's claim."
        ], Material.PAPER, requiredRole)

        Factions.registerPermission(inviteInternalId, this.get())

        this.baseFactionOnly = true
    }

    static boolean canAccess(Faction faction, Member member) {
        return hasRole(faction, member, inviteInternalId, requiredRole)
    }
}
