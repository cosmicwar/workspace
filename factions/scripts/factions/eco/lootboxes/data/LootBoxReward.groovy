package scripts.factions.eco.lootboxes.data

import groovy.transform.CompileStatic
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack

@CompileStatic
class LootBoxReward {

    double weight
    String displayName
    ItemStack displayItem
    boolean displayOnLore = true
    ChatColor glowColor
    List<String> commands

}
