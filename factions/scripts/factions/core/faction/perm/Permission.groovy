package scripts.factions.core.faction.perm

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.Role
import scripts.factions.data.obj.CL
import scripts.factions.core.faction.perm.access.AccessType
import scripts.factions.core.faction.perm.access.TargetType

@CompileStatic
class Permission
{
    String internalId

    String name
    List<String> description = []

    Material material = Material.BOOK

    Role requiredRole = Role.RECRUIT

    Boolean baseFactionOnly = false

    Set<AccessType> accessTypes = Sets.newConcurrentHashSet()

    Permission(String internalId, Role requiredRole = Role.RECRUIT) {
        this.internalId = internalId
        this.name = internalId
        this.requiredRole = requiredRole
    }

    Permission(String internalId, String name, List<String> description = [], Material material = Material.BOOK, Role requiredRole = Role.RECRUIT) {
        this.internalId = internalId
        this.name = name
        this.description = description
        this.material = material
        this.requiredRole = requiredRole
    }

    boolean hasFactionAccess(Faction faction) {
        return faction.getSelfAccess(this.internalId) != null
    }

    Role getFactionRequiredRole(Faction faction) {
        def factionAccess = faction.getSelfAccess(this.internalId)
        if (factionAccess != null)
            return factionAccess.requiredRole

        return this.requiredRole
    }

    static boolean hasRole(UUID factionId, Member member, Role requiredRole) {
        if (member.role == Role.ADMIN) return true // bypass :]

        def faction = Factions.getFaction(factionId, false)
        if (faction == null) return false

        if (member.getFactionId() == null) return false
        if (member.getFactionId() != faction.id) return false

        return member.role.ordinal() >= requiredRole.ordinal()
    }

    static boolean hasRole(Faction faction, Member member, String internalId, Role requiredRole) {
        if (member.role == Role.ADMIN) return true // bypass :]

        if (member.getFactionId() == null) return false
        if (member.getFactionId() != faction.id) return false

        def factionAccess = faction.getSelfAccess(internalId)
        if (factionAccess != null) {
            return member.role.ordinal() >= factionAccess.requiredRole.ordinal()
        }

        return member.role.ordinal() >= requiredRole.ordinal()
    }

    static boolean hasAccessOverride(UUID factionId, Member member, String internalId) {
        if (member.role == Role.ADMIN) return true // bypass :]

        def faction = Factions.getFaction(factionId, false)
        if (faction == null) return false

        def memberAccess = faction.getAccess(member.getId(), TargetType.PLAYER)
        if (memberAccess == null) return false

        def access = memberAccess.access.find { it.internalId == internalId }

        if (access && memberAccess.accessChunk != null) {
            def chunk = memberAccess.accessChunk

            def world = Bukkit.getWorld(chunk.worldName)
            if (world == null) return false

            def player = Bukkit.getPlayer(member.getId())
            if (player == null) return false

            return chunk.x == player.getChunk().x && chunk.z == player.getChunk().z
        }

        return access
    }

    static boolean hasAccessIn(CL cl, Member member, String internalId) {
        if (member.role == Role.ADMIN) return true // bypass :]

        def faction = Factions.getFactionAt(cl)
        if (faction == null) return true // ???

        def memberAccess = faction.getMemberAccess(member)
        if (memberAccess == null) return false

        def access = memberAccess.access.find { it.internalId == internalId }  != null

        if (access && memberAccess.accessChunk != null) {
            def chunk = memberAccess.accessChunk

            def world = Bukkit.getWorld(chunk.worldName)
            if (world == null) return false

            def player = Bukkit.getPlayer(member.getId())
            if (player == null) return false

            return chunk.x == player.getChunk().x && chunk.z == player.getChunk().z
        }

        return access
    }

    static boolean hasAccessAt(Location location, Member member, String internalId) {
        if (member.role == Role.ADMIN) return true // bypass :]

        def faction = Factions.getFactionAt(location)
        if (faction == null) return true // ???

        def memberAccess = faction.getMemberAccess(member)
        if (memberAccess == null) return false

        def access = memberAccess.access.find { it.internalId == internalId } != null

        if (access && memberAccess.accessChunk != null) {
            def chunk = memberAccess.accessChunk

            def world = Bukkit.getWorld(chunk.worldName)
            if (world == null) return false

            return true
        }

        return access
    }

    Permission get() {
        return this
    }

}