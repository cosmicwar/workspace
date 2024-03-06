package scripts.factions.core.faction

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.regions.RegionContainer
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.promise.Promise
import org.starcade.starlight.helper.utils.Players
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.claim.Board
import scripts.factions.core.faction.claim.Claim
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.relation.RelationType
import scripts.factions.core.faction.perm.perms.BuildPermission
import scripts.shared.data.obj.CL
import scripts.shared.legacy.ChatUtils
import scripts.shared.utils.ColorUtil

import javax.annotation.Nonnull
import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class FactionUtils {

    static Map<UUID, Promise<Void>> teleportingPlayers = new ConcurrentHashMap<>()

    FactionUtils() {
        events()
    }

    static boolean hasFaction(@Nonnull Player player) {
        Member member = Factions.getMember(player.getUniqueId())
        return member.getFactionId() != Factions.wildernessId
    }

    static boolean isWilderness(@Nonnull Location location) {
        Faction faction = Factions.getFactionAt(location)
        return faction == null ? true : faction.id == Factions.wildernessId
    }

    static boolean isWildernessChunk(@Nonnull Chunk chunk) {
        Board board = Factions.getBoardSync(chunk.getWorld())
        Claim claim = board.getChunkClaim(CL.of(chunk))
        return claim == null ? false : claim.factionId == Factions.wildernessId
    }

    static boolean isWarzone(@Nonnull Location location) {
        Faction faction = Factions.getFactionAt(location)
        return faction == null ? false : faction.id == Factions.warZoneId
    }

    static boolean isSafezone(@Nonnull Location location) {
        Faction faction = Factions.getFactionAt(location)
        return faction == null ? false : faction.id == Factions.safeZoneId
    }

    static boolean isNonPlayerFactionLand(@Nonnull Location location) {
        Faction faction = Factions.getFactionAt(location)
        if (faction == null) {
            return false
        } else {
            return faction.systemFactionData != null || faction.id == Factions.warZoneId || faction.id == Factions.safeZoneId
        }
    }

    static boolean isNonPlayerFactionLandOrOwn(@Nonnull Player player, @Nonnull Location location) {
        Faction faction = Factions.getFactionAt(location)
        if (faction == null) {
            return false
        } else if (faction.systemFactionData != null || faction.id == Factions.warZoneId || faction.id == Factions.safeZoneId) {
            Member member = Factions.getMember(player.getUniqueId())
            if (member == null) {
                return false
            } else {
                return faction.id == member.getFactionId()
            }
        } else {
            return true
        }
    }

    static boolean isPlayerOfFactionLand(@Nonnull Player player, @Nonnull Location location) {
        Faction faction = Factions.getFactionAt(location)
        if (faction == null) {
            return false
        } else {
            Member member = Factions.getMember(player.getUniqueId())
            return member == null ? false : faction.getMembers().contains(member.getId())
        }
    }

    static boolean isPlayerOfFactionLandChunk(@Nonnull Player player, @Nonnull Chunk chunk) {
        Board board = Factions.getBoardSync(chunk.getWorld())
        Claim claim = board.getChunkClaim(CL.of(chunk))


        if (claim == null || claim.getFactionId() == null) {
            return false
        } else {
            Faction faction = Factions.getFaction(claim.factionId, false)

            Member member = Factions.getMember(player.getUniqueId())
            return member == null ? false : faction.getMembers().contains(member.getId())
        }
    }

    static boolean isPlayerNotAllowedToBackIntoFactionLand(@Nonnull Player player, @Nonnull Location location) {
        Faction faction = Factions.getFactionAt(location)
        if (faction == null) {
            return false
        } else {
            Member member = Factions.getMember(player.getUniqueId())
            RelationType relation = Factions.getRelationType(member, faction)
            return relation == RelationType.ENEMY || relation == RelationType.NEUTRAL || relation == RelationType.ALLY || relation == RelationType.TRUCE
        }
    }

    static boolean isPhaseIgnorePlayer(@Nonnull Player player, @Nonnull Location location) {
        Faction faction = Factions.getFactionAt(location)
        if (faction == null) {
            return false
        } else {
            Member member = Factions.getMember(player.getUniqueId())
            return faction.members.contains(member.getId())
        }
    }

    static boolean isRelationToTruce(@Nonnull Player player, @Nonnull Location location) {
        Faction faction = Factions.getFactionAt(location)
        if (faction == null) {
            return false
        } else {
            Member member = Factions.getMember(player.getUniqueId())
            RelationType relation = Factions.getRelationType(member, faction)
            return relation == RelationType.TRUCE
        }
    }

    static boolean isRelationToAlly(@Nonnull Player player, @Nonnull Location location) {
        Faction faction = Factions.getFactionAt(location)
        if (faction == null) {
            return false
        } else {
            Member member = Factions.getMember(player.getUniqueId())
            return Factions.getRelationType(member, faction) == RelationType.ALLY
        }
    }

    static boolean isRelationToEnemy(@Nonnull Player player, @Nonnull Location location) {
        Faction faction = Factions.getFactionAt(location)
        if (faction == null) {
            return false
        } else {
            Member member = Factions.getMember(player.getUniqueId())
            return Factions.getRelationType(member, faction) == RelationType.ENEMY
        }
    }

    static boolean isRelationToNeutral(@Nonnull Player player, @Nonnull Location location) {
        Faction faction = Factions.getFactionAt(location)
        if (faction == null) {
            return false
        } else {
            Member member = Factions.getMember(player.getUniqueId())
            return Factions.getRelationType(member, faction) == RelationType.NEUTRAL
        }
    }

    static boolean isBreakNotAllowed(@Nonnull Player player, @Nonnull Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer()
        return !container.createQuery().testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.BLOCK_BREAK) && !BuildPermission.canBuild(player, location)
    }

    static boolean isPlaceNotAllowed(@Nonnull Player player, @Nonnull Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer()
        return !container.createQuery().testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.BLOCK_PLACE) && !BuildPermission.canBuild(player, location)
    }

    static DecimalFormat format = new DecimalFormat("#,###.##")

    /**
     *
     * @param player - player to teleport
     * @param location - location
     * @param locationMessage - A string which defines the location name and sends it to the player, ie "spawn" will be a message to the
     * player saying: "Teleporting you to spawn"
     * @return
     */
    static def teleportPlayer(Player player,
                              Location location,
                              boolean instant = false,
                              String onTeleportMessage = "",
                              String locationMessage = ""
                              )
    {
        if (player.isOp() || !isInPvpZone(player)) instant = true

        teleportingPlayers.remove(player.getUniqueId())?.cancel()

        if (instant) {
            player.teleportAsync(location).thenAccept {
                if (onTeleportMessage != "") Players.msg(player, ColorUtil.color(onTeleportMessage))
            }
        } else {
            double newTeleportTime = Math.max(0.5, 7.0 - 7.0 * (player.getTotalExperience() / 1_000_000))

            if (locationMessage != "") {
                Players.msg(player, ColorUtil.color(locationMessage))
            } else {
                player.sendMessage("§3§l(!) §3You will be teleported in §e§n${format.format(newTeleportTime)}§3... DON'T MOVE!")
                player.sendMessage("§7Your vanilla XP will decrease this wait time.")
            }

            teleportingPlayers.put(player.getUniqueId(), Schedulers.async().runLater({
                player.teleportAsync(location).thenAccept {
                    if (onTeleportMessage != "") Players.msg(player, ColorUtil.color(onTeleportMessage))

                    teleportingPlayers.remove(player.getUniqueId())
                }
            }, newTeleportTime as long, TimeUnit.SECONDS))
        }
    }

    static void events() {
        Events.subscribe(PlayerMoveEvent.class).handler { event ->
            if (event.from.blockZ != event.to.blockZ || event.from.blockX != event.to.blockX || event.from.blockY != event.to.blockY) {
                if (!teleportingPlayers.containsKey(event.player.getUniqueId())) return

                teleportingPlayers.remove(event.player.getUniqueId()).cancel()
                event.player.sendMessage("§cYou have moved, cancelling pending teleportation.")
            }
        }
    }

    static boolean isInPvpZone(Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer()
        return container.createQuery().testState(BukkitAdapter.adapt(player.location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.PVP)
    }
}

