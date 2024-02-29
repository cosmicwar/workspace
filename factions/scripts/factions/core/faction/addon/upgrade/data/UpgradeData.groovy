package scripts.factions.core.faction.addon.upgrade.data

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore

@CompileStatic(TypeCheckingMode.SKIP)
class UpgradeData {

    Set<StoredUpgrade> upgrades = Sets.newConcurrentHashSet()

    UpgradeData() {}

    @BsonIgnore
    StoredUpgrade getUpgrade(String internalId, StoredUpgrade defUpgrade = null) {
        def upgrade = upgrades.stream().filter { (it.internalId == internalId) }.findFirst().orElse(null)
        if (upgrade == null && defUpgrade != null) {
            upgrades.add(defUpgrade)
            upgrade = defUpgrade
        }
        return upgrade
    }

    @BsonIgnore
    boolean isEmpty() {
        return upgrades.isEmpty()
    }

}