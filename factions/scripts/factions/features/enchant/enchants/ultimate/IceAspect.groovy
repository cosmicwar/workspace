package scripts.factions.features.enchant.enchants.ultimate


import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.starcade.starlight.enviorment.Exports
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType


class IceAspect extends CustomEnchantment {

    Closure<Boolean> hasMetaphysical = Exports.ptr("ench:metaphysical:contains") as Closure<Boolean>
    Closure<Boolean> proc = Exports.ptr("ench:metaphysical:proc") as Closure<Boolean>
    Closure<Void> sendMetaphysicalMessage = Exports.ptr("ench:metaphysical:sendMessage") as Closure<Void>

    IceAspect() {
        super(
                "iceaspect",
                EnchantmentTier.ULTIMATE,
                EnchantmentType.PROC,
                "Ice Aspect",
                ["Has a 5% chance per level to slow your target for 2 seconds."],
                [ItemType.SWORD],
                3
        )

        setProcChance(0.04D)
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (target !instanceof Player) return;

        if (!proc(player, enchantLevel)) return;

        if (hasMetaphysical.call(target) as Boolean) {
            if (proc(target)) {
                sendMetaphysicalMessage(target)
                return
            }
        }

        addPotionWithDuration(target, PotionEffectType.SLOW, 5, enchantLevel * 2 * 20)
    }

}
