package scripts.factions.features.enchant.data.item

import com.google.common.collect.ImmutableList
import com.google.common.collect.Sets
import groovy.transform.TypeCheckingMode
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import groovy.transform.CompileStatic
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import scripts.factions.features.enchant.data.enchant.StoredEnchantment
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.HeroicEnchant
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.utils.PersistentItemData

import java.util.concurrent.ConcurrentHashMap

@CompileStatic(TypeCheckingMode.SKIP)
class ItemEnchantmentData extends PersistentItemData {

    static final NamespacedKey DATA_KEY = new NamespacedKey(Starlight.plugin, "ItemEnchantmentData")
    static final Closure<CustomEnchantment> getCustomEnchant = Exports.ptr("enchantments:getEnchantment") as Closure

    Set<StoredEnchantment> enchantments = Sets.newHashSet()

    final List<String> originalLore // not currently used, but may be used in future to make lore handling easier

    boolean whiteScroll = false
    HolyWhiteScrollData holyWhiteScrollData = new HolyWhiteScrollData(false)

    int slotIncrease = 0

    ItemEnchantmentData(ItemStack itemStack) {
        originalLore = FastItemUtils.getLore(itemStack)
    }

    void write(ItemStack itemStack) {
        super.write(itemStack, DATA_KEY)
    }

    @Nullable
    static ItemEnchantmentData read(ItemStack itemStack) {
        return (ItemEnchantmentData) read(itemStack, DATA_KEY, ItemEnchantmentData.class)
    }

    List<StoredEnchantment> getAllEnchantments() {
        return ImmutableList.copyOf(enchantments)
    }

    int getEnchantmentCount() {
        return enchantments.size()
    }

    List<String> getOriginalLore() {
        return ImmutableList.copyOf(originalLore)
    }

    void addEnchantment(@NotNull CustomEnchantment customEnchantment, int enchantLevel) {
        removeEnchantment(customEnchantment)
        enchantments.add(new StoredEnchantment(customEnchantment.getInternalName(), enchantLevel, customEnchantment instanceof HeroicEnchant ? customEnchantment.overwriteEnchantId : null))
    }

    boolean removeEnchantment(@NotNull CustomEnchantment customEnchantment) {
        enchantments.removeIf(storedEnchantment -> storedEnchantment.getEnchantment() == customEnchantment.getInternalName())
        return true
    }

    boolean removeEnchantment(String enchantId) {
        enchantments.removeIf(storedEnchantment -> storedEnchantment.getEnchantment() == enchantId)
        return true
    }

}
