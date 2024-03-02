package scripts.factions.core.profile

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.entity.Player
import scripts.factions.core.profile.grant.Grant
import scripts.factions.core.profile.grant.permission.Permission
import scripts.factions.core.profile.punish.Punishment
import scripts.factions.data.uuid.UUIDDataObject

import javax.xml.stream.Location

@CompileStatic(TypeCheckingMode.SKIP)
class Profile extends UUIDDataObject {

    Set<Grant> grants = new HashSet<Grant>()
    Set<Permission> permissions = new HashSet<Permission>()
    Set<Punishment> punishments = new HashSet<Punishment>()

    Boolean godMode = false, vanished = false, staffMode = false
    Boolean staffChat = false, adminChat = false, socialSpy = false

    @BsonIgnore transient Player player = null
    @BsonIgnore transient Location lastLocation = null

    Profile() {}

    Profile(UUID id) {
        super(id)
    }

    @BsonIgnore
    Collection<Grant> getActiveGrants() {
        return grants.findAll { !it.expired() }
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }
}
