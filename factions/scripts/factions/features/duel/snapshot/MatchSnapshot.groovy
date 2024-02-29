package scripts.factions.features.duel.snapshot

import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.data.uuid.UUIDDataObject
import scripts.factions.features.duel.arena.Arena
import scripts.factions.features.duel.match.MatchState

class MatchSnapshot extends UUIDDataObject {

    MatchState state = null
    MatchResult result = null
    Arena arena = null

    String matchDisplayName = null

    Set<PlayerSnapshot> players = new HashSet<>()

    MatchSnapshot() {}

    MatchSnapshot(UUID duelId) {
        super(duelId)
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }
}
