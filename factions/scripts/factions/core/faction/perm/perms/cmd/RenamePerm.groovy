package scripts.factions.core.faction.perm.perms.cmd

import org.bukkit.Material
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.Role
import scripts.factions.core.faction.perm.Permission

class RenamePerm extends Permission {

    static String renameInternalId = "rename_permission"
    static Role requiredRole = Role.COLEADER

    RenamePerm() {
        super(renameInternalId, "§aRename §7Permission", [
                "",
                "§aAllows you to break blocks",
                "§ain a faction's claim."
        ], Material.NETHERITE_HOE, requiredRole)

        Factions.registerPermission(renameInternalId, this.get())

        this.baseFactionOnly = true
    }

    static boolean canAccess(Faction faction, Member member) {
        return hasRole(faction, member, renameInternalId, requiredRole)
    }
}
