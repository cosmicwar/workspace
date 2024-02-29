package scripts.factions.features.enchant.enchants.unique

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class DeepWounds extends CustomEnchantment {
    DeepWounds() {
        super(
                "deep_wounds",
                EnchantmentTier.UNIQUE,
                EnchantmentType.NORMAL,
                "Deep Wounds",
                ["Does bleed tick damage."],
                [ItemType.SWORD],
                3,
                true
        )
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        int bleedTicks = 4
        Task bleedTask

        bleedTask = Schedulers.async().runRepeating({
            EnchantUtils.scaleDamage(event, 1D + (bleedTicks * 0.005D) + (enchantLevel * 0.01D))
            if (bleedTicks == 0) bleedTask.stop()
            bleedTicks++
        }, 0, 20)

    }
}
