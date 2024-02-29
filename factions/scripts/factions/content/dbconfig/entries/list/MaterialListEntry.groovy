package scripts.factions.content.dbconfig.entries.list

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.data.ConfigType

@CompileStatic(TypeCheckingMode.SKIP)
class MaterialListEntry extends ConfigEntry<List<Material>>
{
    List<Material> defaultValue
    List<Material> value

    MaterialListEntry() {}

    MaterialListEntry(String id, List<Material> defaultValue = [], String... description = []) {
        super(id, ConfigType.LIST_MATERIAL)

        this.defaultValue = defaultValue
        this.value = defaultValue

        this.description.addAll(description)
    }

    @Override
    void resetToDefault() {
        this.value = this.defaultValue
    }

}
