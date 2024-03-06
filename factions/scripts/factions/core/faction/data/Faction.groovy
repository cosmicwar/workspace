package scripts.factions.core.faction.data

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.starcade.starlight.helper.utils.Players
import scripts.factions.content.log.Logs
import scripts.factions.content.log.v2.LogData
import scripts.factions.content.log.v2.api.LogType
import scripts.factions.core.faction.addon.sandbot.data.SandBot
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.addon.shield.data.ShieldData
import scripts.factions.core.faction.claim.Claim
import scripts.factions.core.faction.claim.CoreChunkData
import scripts.factions.core.faction.data.random.WarpData
import scripts.shared.data.obj.CL
import scripts.shared.data.obj.Position
import scripts.factions.core.faction.data.relation.Relation
import scripts.factions.core.faction.data.relation.RelationType
import scripts.factions.core.faction.perm.access.Access
import scripts.factions.core.faction.perm.access.AccessData
import scripts.factions.core.faction.perm.access.TargetType
import scripts.factions.core.faction.addon.upgrade.data.UpgradeData
import scripts.shared.data.uuid.UUIDDataObject
import scripts.shared.legacy.utils.TimeUtils

import java.util.concurrent.TimeUnit

@CompileStatic(TypeCheckingMode.SKIP)
class Faction extends UUIDDataObject {

    // internals
    String name
    String description = "~ None"

    Integer fTopRank = null
    Long fTopValue = 0
    Long spawnerValue = 0
    Long pointsValue = 0

    UUID leaderId = null

    Set<UUID> members = Sets.newConcurrentHashSet()

    boolean open = false
    boolean disbanded = false

    Long createDate = null

    double factionPower = 0.0D

    Set<Relation> factionRelations = Sets.newConcurrentHashSet()

    int maxFactionWarps = 5

    // Faction Clams & Perms
    Set<Claim> claims = Sets.newConcurrentHashSet()
    AccessData factionAccessData = new AccessData()
    Set<AccessData> accessData = Sets.newConcurrentHashSet()
    CoreChunkData coreChunkData = null

    // Upgrades
    UpgradeData upgradeData = new UpgradeData()

    // Addons
    Position fHome = null
    Position fBanner = null
    Set<WarpData> warps = Sets.newConcurrentHashSet()
    Integer tntBalance = 0

    // Shields
    ShieldData shieldData = new ShieldData()

    // sandbots
    @BsonIgnore
    transient Map<Integer, SandBot> sandBots = Maps.newConcurrentMap()

    // system faction data
    SystemFaction systemFactionData = null

    @BsonIgnore
    transient Map<UUID, Long> pendingInvites = ExpiringMap.builder().expiration(5, TimeUnit.MINUTES).expirationPolicy(ExpirationPolicy.ACCESSED).build()

    @BsonIgnore
    transient Map<UUID, RelationType> pendingRelationChanges = ExpiringMap.builder().expiration(5, TimeUnit.MINUTES).expirationPolicy(ExpirationPolicy.ACCESSED).build()
    
    Faction() {}

    Faction(UUID uuid)
    {
        super(uuid)
    }

    Faction(String name)
    {
        this.name = name
    }

    Faction(String name, String description)
    {
        this.name = name
        this.description = description
    }

    @BsonIgnore
    Collection<UUID> getAllies() {
        return factionRelations.findAll { it.type == RelationType.ALLY }.collect { it.targetFactionId }
    }

    @BsonIgnore
    Collection<UUID> getEnemies() {
        return factionRelations.findAll { it.type == RelationType.ENEMY }.collect { it.targetFactionId }
    }

    @BsonIgnore
    Collection<UUID> getTruces() {
        return factionRelations.findAll { it.type == RelationType.TRUCE }.collect { it.targetFactionId }
    }

    @BsonIgnore
    Access getSelfAccess(String id) {
        return factionAccessData.access.find { it.internalId == id }
    }

    @BsonIgnore
    def addSelfAccess(Access access) {
        factionAccessData.access.add(access)
    }

    @BsonIgnore
    def removeSelfAccess(Access access) {
        factionAccessData.access.remove(access)
    }

    @BsonIgnore
    AccessData getAccess(UUID targetId, TargetType accessor) {
        return accessData.find { it.targetId == targetId && it.accessor == accessor }
    }

    @BsonIgnore
    AccessData getChunkAccess(CL chunk) {
        return accessData.find { it.accessChunk == chunk }
    }

    @BsonIgnore
    AccessData getMemberAccess(Member member) {
        def data = getAccess(member.getId(), TargetType.PLAYER)
        if (data != null) return data

        return getAccess(member.getFactionId(), TargetType.PLAYER)
    }

    @BsonIgnore
    def addAccess(AccessData access) {
        accessData.add(access)
    }

    @BsonIgnore
    def removeAccess(AccessData access) {
        accessData.remove(access)
    }

    @BsonIgnore
    Collection<AccessData> getMemberAccess() {
        return accessData.findAll { it.accessor == TargetType.PLAYER }
    }

    @BsonIgnore
    Collection<AccessData> getFactionAccess() {
        return accessData.findAll { it.accessor == TargetType.FACTION }
    }

    @BsonIgnore
    Collection<AccessData> getChunkAccess() {
        return accessData.findAll { it.accessChunk != null }
    }

    @BsonIgnore
    Collection<Claim> getConnectingClaimsCross(CL location) {
        Set<Claim> connectingClaims = Sets.newConcurrentHashSet()
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++)
            {
                if (Math.abs(x) == 1 && Math.abs(z) == 1) continue

                def chunkLocation = new CL(location.worldName, location.getX() + x, location.getZ() + z)
                def claim = claims.find { it.location == chunkLocation }

                if (claim != null) connectingClaims.add(claim)
            }
        }
        return connectingClaims
    }

    @BsonIgnore
    Collection<Member> getOnlineMembers() {
        return getOnlinePlayers().collect { Factions.getMember(it.getUniqueId(), false) }.findAll { it != null }
    }

    @BsonIgnore
    Collection<Player> getOnlinePlayers() {
        return members.collect { Bukkit.getPlayer(it) }.findAll { it != null }
    }

    @BsonIgnore
    def msg(String message, String prefix = "§2§l[§a§lF§2§l] ", Closure<Boolean> filter = null) {
        message = prefix + message
        sendMessage(getOnlinePlayers().findAll {
            //noinspection GrUnresolvedAccess
            return (filter == null || filter.call(it))
        } as List<Player>, message)
    }

    @BsonIgnore
    static def sendMessage(List<Player> players, String message) {
        players.each { Player player ->
            Players.msg(player, message)
        }
    }

    @BsonIgnore
    Relation getRelation(Faction faction) { return factionRelations.find { it.targetFactionId == faction.getId() || it.initiatorId == faction.getId() } }

    @BsonIgnore
    Relation getRelation(UUID factionId) { return factionRelations.find { it.targetFactionId == factionId || it.initiatorId == factionId } }

    @BsonIgnore
    boolean isAllied(Faction faction) { return getRelation(faction)?.type == RelationType.ALLY ?: false }

    @BsonIgnore
    boolean isEnemy(Faction faction) { return getRelation(faction)?.type == RelationType.ENEMY ?: false }

    @BsonIgnore
    boolean isTruce(Faction faction) { return getRelation(faction)?.type == RelationType.TRUCE ?: false }

    @BsonIgnore
    boolean isNeutral(Faction faction) { return getRelation(faction)?.type == RelationType.NEUTRAL ?: false }

    @BsonIgnore
    LogData getLogData() { return Logs.getLogData(getId(), LogType.FACTION) }

    @BsonIgnore
    boolean isShielded() { return shieldData.isShielded(TimeUtils.getTimeHours()) }

    @BsonIgnore @Override
    boolean isEmpty() { return false }
}

