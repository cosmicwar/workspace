package scripts.factions.features.enchant.enchants.elite

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.starcade.starlight.enviorment.Exports
import scripts.factions.features.enchant.indicators.Indicator
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Paralyze extends CustomEnchantment {

    Closure<Boolean> hasMetaphysical = Exports.ptr("ench:metaphysical:contains") as Closure<Boolean>
    Closure<Boolean> proc = Exports.ptr("ench:metaphysical:proc") as Closure<Boolean>
    Closure<Void> sendMetaphysicalMessage = Exports.ptr("ench:metaphysical:sendMessage") as Closure<Void>

    ParalyzeIndicator indicator = new ParalyzeIndicator()
    Paralyze() {
        super(
                "paralyze",
                EnchantmentTier.ELITE,
                EnchantmentType.PROC,
                "Paralyze",
                ["Chance to slow your opponent", "for a short duration"],
                [ItemType.SWORD],
                4
        )

        setStackable(false)
        setProcChance(0.05D)
        setCoolDown(3)
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (target !instanceof Player) return

        if (proc(player, enchantLevel))
        {
            indicator.spawn("paralyze_${player.getUniqueId().toString()}_${target.getUniqueId()}", player, target)
            target.getLocation().getWorld().strikeLightningEffect(target.getLocation())
            target.damage(6)
            if (new Random().nextDouble() < 0.3) target.damage(6)

            if (enchantLevel == getMaxLevel()) addPotionWithDuration(target, PotionEffectType.SLOW_DIGGING, 1, 5 * 20)

            if (hasMetaphysical.call(target) as Boolean) {
                if (proc(target)) {
                    sendMetaphysicalMessage(target)
                    return
                }
            }

            addPotionWithDuration(target, PotionEffectType.SLOW, (enchantLevel > 2) ? 1 : 0, 5 * 20)
        }
    }

    class ParalyzeIndicator implements Indicator {

        @Override
        List<String> build(Object data) {
            return [" §5§l* PARALYZE * "]
        }
    }
}
