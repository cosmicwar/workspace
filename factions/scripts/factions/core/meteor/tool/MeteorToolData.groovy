package scripts.factions.core.meteor.tool

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Nullable
import org.starcade.starlight.Starlight
import scripts.factions.core.meteor.MeteorTier
import scripts.shared.utils.PersistentItemData

@CompileStatic(TypeCheckingMode.SKIP)
class MeteorToolData extends PersistentItemData
{
    static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "MeteorToolData")

    UUID lastUsedBy
    long lastUsed

    int level
    long xp

    long blocksBroken

    MeteorTier tier

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    @Nullable
    static MeteorToolData read(ItemStack itemStack) {
        return (MeteorToolData) read(itemStack, DATA_KEY, MeteorToolData.class)
    }
}
