package scripts.factions.features.enchant.enchants.elite

import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.starcade.starlight.enviorment.Exports
import scripts.shared.core.cfg.entries.IntEntry
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier

import scripts.factions.features.enchant.enchants.legendary.Clarity
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Blind extends CustomEnchantment {

    Closure<Boolean> containsPlayer = Exports.ptr("ench:clarity:containsPlayer") as Closure<Boolean>
    Closure<Integer> getLevel = Exports.ptr("ench:clarity:getLevel") as Closure<Integer>

    Blind() {
        super(
                "blind",
                EnchantmentTier.ELITE,
                EnchantmentType.PROC,
                "Blind",
                ["Chance to blind your opponent", "for a short duration"],
                [ItemType.SWORD],
                3
        )

        setStackable(false)
        setProcChance(0.03D)

        getConfig().addDefault([
                new IntEntry("duration", 2, "Potion Effect Duration In Seconds"),
        ])
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (target !instanceof Player) return

        if (proc(player, enchantLevel)) {
            if (containsPlayer.call(target)) {
                if (getLevel.call(target) >= enchantLevel) {
                    target.playSound(target.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1F, 0.6F)
                    player.playSound(target.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1F, 0.6F)
                    return
                }
            }
            if (containsPlayer.call(target)) {
                if (getLevel.call(target) >= enchantLevel) {
                    target.playSound(target.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1F, 0.6F);
                    player.playSound(target.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1F, 0.6F);
                    return
                }
            }
            addPotionWithDuration(target, PotionEffectType.BLINDNESS, enchantLevel, getConfig().getIntEntry("duration").value * 20 * enchantLevel)
        }
    }
}
