package scripts.factions.features.enchant.enchants.ultimate

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.TickingEnchant
import scripts.shared.utils.ItemType

import java.util.concurrent.ConcurrentHashMap

class Bleed extends TickingEnchant {

    private Map<UUID, Integer> pendingBleedTicks = new ConcurrentHashMap<>()

    Bleed() {
        super(
                "bleed",
                EnchantmentTier.ULTIMATE,
                "Bleed",
                ["Deals 1.5% of the target's max health per level over 5 seconds."],
                [ItemType.AXE],
                5
        )
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (!(target instanceof Player) || event.getFinalDamage() <= 0D) return
        if (!proc(player, enchantLevel)) return

    }

    @Override
    void start(Player player) {

    }

    @Override
    void onTick(Player player, int tick) {

    }

    @Override
    void cleanup(Player player) {

    }
}
