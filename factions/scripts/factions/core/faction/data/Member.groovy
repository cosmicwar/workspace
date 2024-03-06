package scripts.factions.core.faction.data

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.EnderPearl
import scripts.factions.content.log.Logs
import scripts.factions.content.log.v2.LogData
import scripts.factions.content.log.v2.api.LogType
import scripts.factions.core.faction.chat.ChatMode
import scripts.factions.core.faction.data.random.FPSData
import scripts.shared.data.uuid.UUIDDataObject

import java.lang.ref.WeakReference

@CompileStatic
class Member extends UUIDDataObject {

    UUID factionId = null
    Role role = Role.SYSTEM

    double power = 25.0D

    ChatMode chatMode = ChatMode.PUBLIC

    long lastOnline = -1L

    FPSData fpsData = new FPSData()

    @BsonIgnore transient Role previousRole = null
    @BsonIgnore transient Long looterMessageCooldown = 0L
    @BsonIgnore transient Long enderpearlCooldown = 0L
    @BsonIgnore transient Long enderpearlDoorCooldown = 0L
    @BsonIgnore transient Long superAppleCooldown = 0L
    @BsonIgnore transient Long regularAppleCooldown = 0L
    @BsonIgnore transient Boolean teleported = false
    @BsonIgnore transient Location validLocation = null
    @BsonIgnore transient WeakReference<EnderPearl> pearl = null

    Member() {
    }

    Member(UUID id) {
        super(id)
    }

    @BsonIgnore
    boolean isRoleAtleast(Role role) {
        return this.role.ordinal() >= role.ordinal()
    }

    @BsonIgnore
    boolean isThrowingPearl() {
        EnderPearl pearl
        return !(this.pearl == null || (pearl = this.pearl.get()) == null || pearl.isDead())
    }

    @BsonIgnore
    String getDisplayName() {
        def online = Bukkit.getPlayer(this.id)
        if (online) {
            return "$role.prefix ${online.getName()}"
        } else {
            return "$role.prefix ${Bukkit.getOfflinePlayer(this.id).getName()}"
        }
    }

    @BsonIgnore
    String getName() {
        def online = Bukkit.getPlayer(this.id)
        if (online) {
            return "${online.getName()}"
        } else {
            return "${Bukkit.getOfflinePlayer(this.id).getName()}"
        }
    }

    @BsonIgnore
    boolean equals(Object object) {
        if (this.is(object)) {
            return true
        } else if (object == null || this.getClass() != object.getClass()) {
            return false
        }
        Member member = (Member) object
        return member.id == this.id
    }

    @BsonIgnore
    Long getLastOnline() {
        if (Bukkit.getPlayer(this.id) != null) return System.currentTimeMillis()
        return lastOnline
    }

    @BsonIgnore
    LogData getLogData() {
        return Logs.getLogData(this.id, LogType.PLAYER)
    }

    @BsonIgnore
    @Override
    boolean isEmpty() {
        return false
    }
}