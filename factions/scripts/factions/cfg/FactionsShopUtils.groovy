package scripts.factions.cfg

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.Potion
import org.bukkit.potion.PotionType
//import scripts.factions.spawners.Spawners

class FactionsShopUtils {
    static ItemStack createPotion(PotionType potionType, int level, boolean splash = true) {
        ItemStack potionStack = new ItemStack(Material.POTION);
        Potion potion = new Potion(potionType, level)
        potion.setSplash(splash)
        potionStack = potion.toItemStack(1)

        return potionStack
    }

//    static ItemStack createSpawner(EntityType type) {
//        ItemStack spawnerStack = new ItemStack(Material.SPAWNER)
//    }
}
