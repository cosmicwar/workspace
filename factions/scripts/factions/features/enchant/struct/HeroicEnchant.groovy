package scripts.factions.features.enchant.struct

import scripts.shared.utils.ItemType

class HeroicEnchant extends CustomEnchantment {

    String overwriteEnchantId

    HeroicEnchant(String id, EnchantmentType enchantmentType, String displayName, String overwriteEnchantId, List<String> description = ["empty"], List<ItemType> applicability = [], int maxLevel = 1, boolean stackable = false, EnchantPriority priority = EnchantPriority.NORMAL) {
        super(
                id,
                EnchantmentTier.HEROIC,
                enchantmentType,
                displayName,
                description,
                applicability,
                maxLevel,
                stackable,
                priority
        )
        this.overwriteEnchantId = overwriteEnchantId
    }
}
