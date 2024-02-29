package scripts.factions.features.enchant.enchants.elite

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.starcade.starlight.enviorment.Exports
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Frozen extends CustomEnchantment {
    Closure<Boolean> hasMetaphysical = Exports.ptr("ench:metaphysical:contains") as Closure<Boolean>
    Closure<Boolean> proc = Exports.ptr("ench:metaphysical:proc") as Closure<Boolean>
    Closure<Void> sendMetaphysicalMessage = Exports.ptr("ench:metaphysical:sendMessage") as Closure<Void>

    Frozen() {
        super(
                "frozen",
                EnchantmentTier.ELITE,
                EnchantmentType.PROC,
                "Frozen",
                ["Chance to slow your opponent", "for a short duration"],
                [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                3
        )

        setStackable(false)
        setProcChance(0.015D)
        setCoolDown(10)
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event)
    {
        if (attacker !instanceof Player) return

        if (!proc(player, enchantLevel)) return

        if (hasMetaphysical.call(attacker) as Boolean) {
            if (proc(attacker)) {
                sendMetaphysicalMessage(attacker)
                return
            }
        }

        addPotionWithDuration(attacker, PotionEffectType.SLOW, (enchantLevel > 2) ? 1 : 0, enchantLevel * 20)

    }
}
