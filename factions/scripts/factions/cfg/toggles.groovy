package scripts.factions.cfg

import org.bukkit.Material
import scripts.shared.legacy.ToggleUtils

ToggleUtils.register("enchant_particles", [
        item        : Material.REDSTONE,
        display_name: "§cCustom Enchant Particles",
        description : [
                "This setting will show all custom enchant particles.",
        ],
        category    : "other"
])

ToggleUtils.register("enchant_particles_others", [
        item        : Material.REDSTONE,
        display_name: "§cCustom Enchant Particles §e(Other Players)",
        description : [
                "This setting will show only custom ",
                "enchant particles from other players."
        ],
        category    : "other"
])

ToggleUtils.register("enchant_particles_self", [
        item        : Material.REDSTONE,
        display_name: "§cCustom Enchant Particles §a(Self)",
        description : [
                "This setting will show only custom ",
                "enchant particles from yourself."
        ],
        category    : "other"
])