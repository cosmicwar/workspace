package scripts.factions.core.profile

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.entity.Player
import scripts.factions.core.profile.rank.Grant
import scripts.factions.core.profile.rank.permission.Permission
import scripts.factions.core.profile.punish.Punishment
import scripts.factions.data.uuid.UUIDDataObject
import scripts.shared.utils.Temple

import javax.xml.stream.Location

@CompileStatic(TypeCheckingMode.SKIP)
class Profile extends UUIDDataObject {

    String playerName = "Unknown"

    Set<Grant> grants = new HashSet<Grant>()
    Set<Permission> permissions = new HashSet<Permission>()
    Set<Punishment> punishments = new HashSet<Punishment>()
    Set<TempleProfileData> templeData = new HashSet<TempleProfileData>()

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

    @BsonIgnore
    TempleProfileData getTempleData() {
        def data = templeData.find { it.templeId.equalsIgnoreCase(Temple.templeId) }
        if (data == null) {
            data = new TempleProfileData(Temple.templeId.replaceAll("\\d", "").replace("_local", ""))
            templeData.add(data)
        }

        return data
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }
}
