package scripts.factions.features.enchant.data.item

import org.starcade.starlight.Starlight
import groovy.transform.CompileStatic
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.shared.utils.PersistentItemData

@CompileStatic
class SoulGemData extends PersistentItemData {
    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "SoulGemData")

    int soulGemCount

    SoulGemData(int soulGemCount = 0) {
        this.soulGemCount = soulGemCount
    }

    int getSouls() {
        return soulGemCount
    }

    void setSouls(int soulGemCount) {
        this.soulGemCount = soulGemCount
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static SoulGemData read(ItemStack itemStack) {
        return (SoulGemData) read(itemStack, DATA_KEY, SoulGemData.class)
    }
}

