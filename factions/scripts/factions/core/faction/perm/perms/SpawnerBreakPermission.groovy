package scripts.factions.core.faction.perm.perms

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Role
import scripts.factions.core.faction.perm.Permission

@CompileStatic(TypeCheckingMode.SKIP)
class SpawnerBreakPermission extends Permission {

    SpawnerBreakPermission() {
        super("spawner_break_permission", "§cSpawner Break Permission", [
                "",
                "§aAllows you to break spawners",
                "§ain a faction's claim."
        ], Material.SPAWNER, Role.COLEADER)

        Factions.registerPermission(this.internalId, this)

        events()
    }

    def events() {
//        Events.subscribe(BlockBreakEvent.class).handler {
//            def cl = CL.of(it.getBlock().getLocation())
//            def faction = Factions.getFactionAt(cl)
//            if (faction == null) return // wilderness :?
//
//            def member = Factions.getMember(it.player.getUniqueId(), false)
//            if (member == null) return
//
//            if (!hasRole(faction, member)) {
//                if (!hasAccessOverride(faction.factionId, member)) {
//                    Players.msg(it.player, "§cYou do not have permission to break blocks in ${faction.getName()}'s claim")
//                    it.setCancelled(true)
//                }
//            }
//        }
    }

}
