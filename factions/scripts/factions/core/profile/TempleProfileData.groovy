package scripts.factions.core.profile

import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Location
import org.bukkit.entity.Player

class TempleProfileData {

    String templeId = null

    Boolean godMode = false, vanished = false, staffMode = false
    Boolean staffChat = false, adminChat = false, socialSpy = false

    @BsonIgnore transient Player player = null
    @BsonIgnore transient Location lastLocation = null

    TempleProfileData() {}

    TempleProfileData(String templeId) {
        this.templeId = templeId
    }

}
