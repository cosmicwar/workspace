package scripts.factions.features.itemfilter

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import scripts.shared.data.uuid.UUIDDataObject

import java.util.concurrent.ConcurrentHashMap

@CompileStatic
class FilterData extends UUIDDataObject {

    boolean enabled = false

    Map<String, FilterOptions> filterOptions = new ConcurrentHashMap<>()
    String lastOptions

    FilterData() {}

    FilterData(UUID id) {
        super(id)
    }

    @BsonIgnore
    boolean enable(String filterId = "null") {
        if (filterOptions.isEmpty()) return false

        if (filterId != "null") {
            if (!filterOptions.containsKey(filterId)) return false
            lastOptions = filterId

            enabled = true
            return true
        }

        if (lastOptions) {
            enabled = true
            return true
        }

        lastOptions = filterOptions.values()[0].id
        enabled = true
        return true
    }

    @BsonIgnore
    def disable() {
        enabled = false
    }

    @BsonIgnore
    boolean toggle() {
        if (!lastOptions) {
            if (filterOptions.isEmpty()) return false

            lastOptions = filterOptions.values()[0].id
        }

        if (enabled) {
            disable()
            return false
        }

        return enable(lastOptions)
    }

    @BsonIgnore
    def addFilter(String filterId) {
        filterOptions.put(filterId, new FilterOptions(filterId))
    }

    @BsonIgnore
    def addFilter(FilterOptions options) {
        filterOptions.put(options.id, options)
    }

    @BsonIgnore
    @Override
    boolean isEmpty() {
        return filterOptions.isEmpty()
    }
}