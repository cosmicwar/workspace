package scripts.factions.features.duel.player

import com.google.common.collect.Sets
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import org.starcade.starlight.helper.utils.Players
import scripts.shared.data.uuid.UUIDDataObject
import scripts.shared.utils.ColorUtil

class DuelParty extends UUIDDataObject {

    UUID partyLeader = null
    Set<UUID> partyMembers = Sets.<UUID> newConcurrentHashSet()

    DuelParty() {}

    DuelParty(UUID id, UUID partyLeader = null) {
        super(id)

        this.partyLeader = partyLeader
    }

    @BsonIgnore
    def msg(String msg, String prefix = "§3§l[P]§r") {
        partyMembers.findAll { Bukkit.getPlayer(it) != null }.each { Players.msg(Bukkit.getPlayer(it), ColorUtil.color("${prefix} ${msg}"), ) }
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }
}
