package scripts.factions.features.duel.arena

import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.shared.data.obj.Position
import scripts.shared.data.obj.SR
import scripts.shared.data.uuid.UUIDDataObject

class Arena extends UUIDDataObject {

    ArenaType arenaType

    SR region = new SR()
    Position spawnA = new Position(), spawnB = new Position(), pasteOrigin = new Position()

    boolean isPasted = false, isOccupied = false, changed = false

    Arena() {}

    Arena(UUID arenaId) {
        super(arenaId)
    }

    Arena(UUID arenaId, ArenaType arenaType) {
        super(arenaId)

        this.arenaType = arenaType
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return !isPasted
    }
}
