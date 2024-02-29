package scripts.factions.content.dbconfig.entries

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType

@CompileStatic(TypeCheckingMode.SKIP)
class MaterialEntry extends ConfigEntry<Material> {

    Material defaultValue
    Material value

    MaterialEntry() {}

    MaterialEntry(String id, Material defaultValue = Material.AIR, String... description = []) {
        super(id, ConfigType.MATERIAL)

        this.defaultValue = defaultValue
        this.value = defaultValue

        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }

}
