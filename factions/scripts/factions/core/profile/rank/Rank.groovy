package scripts.factions.core.profile.rank

import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.core.profile.rank.permission.RankPermission
import scripts.factions.data.uuid.UUIDDataObject

class Rank extends UUIDDataObject {

    String internalName = "Unknown"

    int priority = 0

    String prefix = "", suffix = ""
    String nameColor = "§f", chatColor = "§f"

    Set<RankPermission> permissions = new HashSet<RankPermission>()

    Rank() {}
    Rank(UUID id) { super(id) }

    Rank(String internalName,
         String prefix = "",
         String suffix = "",
         String nameColor = "§f",
         String chatColor = "§f") {
        this.internalName = internalName
        this.prefix = prefix
        this.suffix = suffix
        this.nameColor = nameColor
        this.chatColor = chatColor
    }

    @BsonIgnore
    Set<RankPermission> getTemplePermissions() {
        return permissions.findAll { it.isActiveTemple() }
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }

}
