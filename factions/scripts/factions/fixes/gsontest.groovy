package scripts.factions.fixes

import groovy.transform.Field
import org.bukkit.Material
import org.starcade.starlight.helper.Commands
import scripts.shared.utils.Gson

@Field String data = ""

Commands.create().assertOp().assertPlayer().handler {cmd ->
    def matTest = new FilterOptions("lol", [Material.ACACIA_LOG, Material.ACACIA_PLANKS, Material.ACACIA_SAPLING])
    def matTest2 = new FilterOptions("lol2", [Material.DAMAGED_ANVIL, Material.DIAMOND, Material.DIAMOND_BOOTS])
    def test = new FilterData()
    test.filterOptions.put(matTest.id, matTest)
    test.filterOptions.put(matTest2.id, matTest2)
    data = Gson.gson.toJson(test)
    cmd.reply(data)
}.register("dev/gsontest")

Commands.create().assertOp().assertPlayer().handler {cmd ->
    if (data) {
        def obj = Gson.gson.fromJson(data, FilterData.class)
        obj.filterOptions.values().each {
            cmd.reply(it.id)
            cmd.reply(it.enabledMaterials.toString())
        }
    }
}.register("dev/gsontest2")

class FilterData {

    boolean enabled = false

    Map<String, FilterOptions> filterOptions = new HashMap<>()
    String lastOptions

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

    def disable() {
        enabled = false
    }

    boolean toggle() {
        if (!lastOptions) return false

        if (enabled) {
            disable()
            return false
        }

        return enable(lastOptions)
    }

    def addFilter(String filterId) {
        filterOptions.put(filterId, new FilterOptions(filterId))
    }

    def addFilter(FilterOptions options) {
        filterOptions.put(options.id, options)
    }
}

class FilterOptions {
    String id
    List<Material> enabledMaterials

    FilterOptions(String id, List<Material> data = new ArrayList<>()) {
        this.id = id
        enabledMaterials = data
    }
}