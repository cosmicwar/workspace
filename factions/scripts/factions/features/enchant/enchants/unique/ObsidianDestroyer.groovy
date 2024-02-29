package scripts.factions.features.enchant.enchants.unique

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class ObsidianDestroyer extends CustomEnchantment {
    ObsidianDestroyer() {
        super(
                "obsidian_destroyer",
                EnchantmentTier.UNIQUE,
                EnchantmentType.PROC,
                "Obsidian Destroyer",
                ["Instantly destroys obsidian"],
                [ItemType.PICKAXE],
                5,
                false
        )
        setProcChance(0.2D)
    }

    @Override
    void onBlockDamage(Player player, ItemStack itemStack, int enchantLevel, Block block, BlockDamageEvent event) {
        if (block.getType() != Material.OBSIDIAN) return
        if (!proc(player, enchantLevel)) return

        event.setInstaBreak(true)
    }
}
