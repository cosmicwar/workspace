package scripts.factions.features.enchant.enchants.legendary

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class DeathGod extends CustomEnchantment {

    DeathGod() {
        super("deathgod",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.PROC,
                "Death God",
                ["Chance to heal yourself", "when you are attacked"],
                [ItemType.HELMET],
                3
        )

        setStackable(false)
        setProcChance(0.07D)
        setCoolDown(5)

        getConfig().addDefault([
                new DoubleEntry("baseActivationHealth", 3),
                new DoubleEntry("baseHealAmt", 2)
        ])

        Enchantments.enchantConfig.queueSave()
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (!(attacker instanceof Player) || player.getHealth() - event.getFinalDamage() > (enchantLevel + getConfig().getDoubleEntry("baseActivationHealth").value)) return
        if (!(proc(player, enchantLevel))) return

        EnchantUtils.heal(player, enchantLevel + getConfig().getDoubleEntry("baseHealAmt").value)
    }
}
