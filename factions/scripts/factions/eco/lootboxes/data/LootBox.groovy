package scripts.factions.eco.lootboxes.data

import groovy.transform.CompileStatic
import org.bukkit.inventory.ItemStack

@CompileStatic
class LootBox {

    String displayName
    List<String> displayLore
    String animation
    List<ItemStack> itemStacks
    int minRewards, maxRewards
    List<LootBoxRewardGroup> rewardGroups

}
