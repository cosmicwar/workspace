package scripts.factions.features.pack.itemskins.data

import org.starcade.starlight.Starlight
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import scripts.shared.utils.PersistentItemData

@CompileStatic(TypeCheckingMode.SKIP)
class PacketSkinData  extends PersistentItemData {
    private static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "PacketSkinData")

    final String baseStack

    PacketSkinData(String baseStack) {
        this.baseStack = baseStack
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    static PacketSkinData read(ItemStack itemStack) {
        return (PacketSkinData) read(itemStack, DATA_KEY, PacketSkinData.class)
    }
}