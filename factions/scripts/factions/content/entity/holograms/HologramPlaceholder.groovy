package scripts.factions.content.entity.holograms

import org.bukkit.entity.Player

abstract class HologramPlaceholder {
    abstract String update(Player player, String line)
}
