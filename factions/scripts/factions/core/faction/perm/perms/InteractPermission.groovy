package scripts.factions.core.faction.perm.perms

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Role
import scripts.factions.data.obj.CL
import scripts.factions.core.faction.perm.Permission

@CompileStatic(TypeCheckingMode.SKIP)
class InteractPermission extends Permission {

    InteractPermission() {
        super("interact_permission", "§eInteract Permission", [
                "",
                "§aAllows you to interact with",
                "§ablocks in a faction's claim.",
                "",
                "§7§oex. opening doors, fence gates, pressure plates, etc."
        ], Material.DARK_OAK_DOOR, Role.RECRUIT)

        Factions.registerPermission(this.internalId, this)

        events()
    }

    def events() {
        Events.subscribe(PlayerInteractEvent.class).handler {event ->
            def block = event.getClickedBlock()
            if (block == null) return

            def itemType = block.getType()
            if (itemType == null || itemType == Material.AIR) return

            def cl = CL.of(event.getClickedBlock().getLocation())
            def faction = Factions.getFactionAt(cl)
            if (faction == null) return // wilderness :?

            def member = Factions.getMember(event.getPlayer().getUniqueId(), false)
            if (member == null) return


            if (itemType.toString().endsWith("_DOOR") || itemType.toString().endsWith("_GATE")) {
                if (!hasRole(faction, member, internalId, requiredRole)) {
                    if (!hasAccessOverride(faction.id, member, internalId)) {
                        Players.msg(event.getPlayer(), "§cYou do not have permission to interact with blocks in ${faction.getName()}'s claim")
                        event.setCancelled(true)
                    }
                }
            }
        }

        Events.subscribe(PlayerBucketEmptyEvent.class).handler { event ->
            def block = event.block
            if (block == null) return

            def itemType = block.getType()
            if (itemType == null) return

            def cl = CL.of(block.getLocation())
            def faction = Factions.getFactionAt(cl)
            if (faction == null) return

            def member = Factions.getMember(event.getPlayer().getUniqueId(), false)
            if (member == null) return

            if (!hasRole(faction, member, internalId, requiredRole)) {
                if (!hasAccessOverride(faction.id, member, internalId)) {
                    Players.msg(event.getPlayer(), "§cYou do not have permission to interact with blocks in ${faction.getName()}'s claim")
                    event.setCancelled(true)
                }
            }
        }

        Events.subscribe(PlayerBucketFillEvent.class).handler { event ->
            def block = event.block
            if (block == null) return

            def itemType = block.getType()
            if (itemType == null) return

            def cl = CL.of(block.getLocation())
            def faction = Factions.getFactionAt(cl)
            if (faction == null) return

            def member = Factions.getMember(event.getPlayer().getUniqueId(), false)
            if (member == null) return

            if (!hasRole(faction, member, internalId, requiredRole)) {
                if (!hasAccessOverride(faction.id, member, internalId)) {
                    Players.msg(event.getPlayer(), "§cYou do not have permission to interact with blocks in ${faction.getName()}'s claim")
                    event.setCancelled(true)
                }
            }
        }
    }

}
