package scripts.factions.features.enchant.data.item

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import scripts.shared.utils.PersistentItemData

class RandomSoulGenData extends PersistentItemData {

    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "RandomSoulGenData")

    int maxGems

    RandomSoulGenData(int maxGems = 10000) {
        this.maxGems = maxGems
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static RandomSoulGenData read(ItemStack itemStack) {
        return (RandomSoulGenData) read(itemStack, DATA_KEY, RandomSoulGenData.class)
    }

    /*
    default
    500 - 10
    1000 - 20
    1500 - 20
    2500 - 20
    5000 - 20
    10000 - 10
     */
    int getRandomSouls() {
        int random = new Random().nextInt(100)
        if (random < 10) return maxGems * 0.05 as int
        else if (random < 30) return maxGems * 0.1 as int
        else if (random < 50) return maxGems * 0.15 as int
        else if (random < 70) return maxGems * 0.25 as int
        else if (random < 90) return maxGems * 0.5 as int
        else return maxGems
    }
}
