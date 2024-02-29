package scripts.factions.features.spawners.drops

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.factions.data.DataObject

@CompileStatic(TypeCheckingMode.SKIP)
class CustomDrop extends DataObject {

    Set<String> customDrops = Sets.newHashSet()

    CustomDrop() {}

    CustomDrop(String mobName) {
        super(mobName)
    }

    @BsonIgnore
    @Override
    boolean isEmpty() {
        return false
    }
}
