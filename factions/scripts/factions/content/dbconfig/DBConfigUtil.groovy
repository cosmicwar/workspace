package scripts.factions.content.dbconfig

import org.bukkit.Material
import org.starcade.starlight.enviorment.Exports

class DBConfigUtil {
    static Config createConfig(String configId, String displayName, List<String> description = [], Material material = Material.BOOK) {
        return (Exports.ptr("dbcfg:create") as Closure<Config>).call(configId, displayName, description, material)
    }

    static Config getConfig(String configId) {
        return (Exports.ptr("dbcfg:get") as Closure<Config>).call(configId)
    }
}
