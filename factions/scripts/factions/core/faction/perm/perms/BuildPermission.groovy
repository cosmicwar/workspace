package scripts.factions.core.faction.perm.perms

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.addon.fbanner.FBanner
import scripts.factions.core.faction.data.Role
import scripts.shared.data.obj.CL
import scripts.factions.core.faction.perm.Permission
import scripts.shared.utils.DataUtils

@CompileStatic(TypeCheckingMode.SKIP)
class BuildPermission extends Permission {

    static String buildInternalId = "build_permission"
    static Role requiredRole = Role.RECRUIT

    BuildPermission() {
        super(buildInternalId, "§aBuild Permission", [
                "",
                "§aAllows you to place blocks",
                "§ain a faction's claim."
        ], Material.GRASS_BLOCK, requiredRole)

        Factions.registerPermission(buildInternalId, this)

        events()
    }

    static def events() {
        Events.subscribe(BlockPlaceEvent.class).handler {event ->
            def cl = CL.of(event.getBlock().getLocation())

            def faction = Factions.getFactionAt(cl)
            if (faction == null || faction.getId() == Factions.wildernessId) return // wilderness :?

            def member = Factions.getMember(event.player.getUniqueId(), false)
            if (member == null) return

            if (event.getItemInHand().type == Material.BLACK_BANNER) {
                if (DataUtils.hasTag(event.itemInHand, FBanner.DATA_KEY, PersistentDataType.BYTE)) return
            }

            if (!hasRole(faction, member, buildInternalId, requiredRole)) {
                if (!hasAccessOverride(faction.id, member, buildInternalId)) {
                    Players.msg(event.player, "§cYou do not have permission to place blocks in ${faction.getName()}'s claim")
                    event.setCancelled(true)
                }
            }
        }

        Events.subscribe(BlockPistonExtendEvent.class).handler { event ->
            def block = event.getBlock()
            def targetBlock = block.getRelative(event.getDirection(), event.getLength() + 1)

            def origin = Factions.getClaimAt(CL.of(block.location))?.factionId ?: Factions.wildernessId
            def destination = Factions.getClaimAt(CL.of(targetBlock.location))?.factionId ?: Factions.wildernessId

            if (origin == destination) return
            if (!targetBlock.isEmpty() && !targetBlock.isLiquid()) return
            if (destination == Factions.wildernessId) return


            if (!hasAccessOverride(origin, destination, targetBlock.location, buildInternalId)) {
                event.setCancelled(true)
            }
        }

        Events.subscribe(BlockPistonRetractEvent.class)
    }

    static boolean canBuild(Player player, Location location) {
        def faction = Factions.getFactionAt(location)
        if (faction == null || faction.getId() == Factions.wildernessId) return true // wilderness :?

        def member = Factions.getMember(player.getUniqueId(), false)
        if (member == null) return true

        if (!hasRole(faction, member, buildInternalId, requiredRole)) {
            if (!hasAccessOverride(faction.id, member, buildInternalId)) {
                return false
            }
        }

        return true
    }

}
