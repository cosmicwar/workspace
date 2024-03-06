package scripts.factions.features.revive

import com.google.common.collect.Sets
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.shared.data.uuid.UUIDDataObject
import scripts.factions.features.revive.obj.InventorySnapshot
import scripts.factions.features.revive.obj.ReviveSortType

class ReviveData extends UUIDDataObject {

    Set<InventorySnapshot> deaths = Sets.newConcurrentHashSet()

    ReviveSortType sortType = ReviveSortType.DEATH_TIME_OLDER_TO_NEW

    ReviveData() {}

    ReviveData(UUID uuid) {
        super(uuid)
    }

    @BsonIgnore
    @Override
    boolean isEmpty() {
        return deaths.isEmpty() && sortType == ReviveSortType.DEATH_TIME_OLDER_TO_NEW
    }
}
