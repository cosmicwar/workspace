package scripts.factions.features.enchant.enchants.legendary

import net.minecraft.world.entity.Mob
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.helper.Schedulers
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.shared.core.cfg.entries.IntEntry
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantPriority
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.features.actionbar.ActionBarBuilder
import scripts.shared.utils.ItemType

import java.util.concurrent.ThreadLocalRandom

class Inquisitive extends CustomEnchantment {

    Inquisitive() {
        super(
                "inquisitive",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.NORMAL,
                "Inquisitive",
                ["Increases xp given"],
                [ItemType.SWORD, ItemType.AXE],
                4,
                false
        )

        getConfig().addDefault([
                new DoubleEntry("xpMultiplier", 25.0D, "Multiplier for xp given", "", "Per Level")
        ])

        Enchantments.enchantConfig.queueSave()
    }

    @Override
    void onKill(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDeathEvent event) {
        if (target instanceof Player) return

        def multiplier = getConfig().getDoubleEntry("xpMultiplier").value

        def random = ThreadLocalRandom.current()

        multiplier = Math.ceil(Math.max(multiplier + (multiplier / -3 + enchantLevel), random.nextDouble(multiplier, multiplier + (5 * enchantLevel))))

        event.setDroppedExp(event.getDroppedExp() * multiplier as int)
    }
}
