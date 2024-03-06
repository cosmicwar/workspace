package scripts.factions.core.faction.perm.perms

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.addon.fbanner.FBanner
import scripts.factions.core.faction.data.Role
import scripts.shared.data.obj.CL
import scripts.factions.core.faction.perm.Permission
import scripts.shared.legacy.utils.BroadcastUtils

@CompileStatic(TypeCheckingMode.SKIP)
class BreakPermission extends Permission {

    static String breakInternalId = "break_permission"
    static Role requiredRole = Role.RECRUIT

    BreakPermission() {
        super("break_permission", "§aBreak Permission", [
                "",
                "§aAllows you to break blocks",
                "§ain a faction's claim."
        ], Material.NETHERITE_PICKAXE, Role.RECRUIT)

        Factions.registerPermission(breakInternalId, this.get())

        events()
    }

    static def events() {
        Events.subscribe(BlockBreakEvent.class).handler {
            def cl = CL.of(it.getBlock().getLocation())
            def faction = Factions.getFactionAt(cl)
            if (faction == null || faction.getId() == Factions.wildernessId) return // wilderness :?

            def member = Factions.getMember(it.player.getUniqueId(), false)
            if (member == null) return

            if (it.block.type == Material.BLACK_BANNER) {
                if (FBanner.activeBanners.containsKey(it.block)) return
            }

            if (!hasRole(faction, member, breakInternalId, requiredRole)) {
                if (!hasAccessOverride(faction.id, member, breakInternalId)) {
                    Players.msg(it.player, "§cYou do not have permission to break blocks in ${faction.getName()}'s claim")
                    it.setCancelled(true)
                }
            }
        }
    }

    static boolean canBreak(Player player, Location location) {
        def faction = Factions.getFactionAt(location)
        if (faction == null || faction.getId() == Factions.wildernessId) return true // wilderness :?

        def member = Factions.getMember(player.getUniqueId(), false)
        if (member == null) return true

        if (!hasRole(faction, member, breakInternalId, requiredRole)) {
            if (!hasAccessOverride(faction.id, member, breakInternalId)) {
                return false
            }
        }

        return true
    }

}
