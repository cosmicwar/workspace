package scripts.factions.features.enchant.enchants.simple


import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType


class ThunderingBlow extends CustomEnchantment {

    ThunderingBlow() {
        super(
                "thunderingblow",
                EnchantmentTier.SIMPLE,
                EnchantmentType.PROC,
                "Thundering Blow",
                ["Chance to strike your target", "with lightning"],
                [ItemType.SWORD],
                5
        )

        setProcChance(0.02D)
        setCoolDown(5)

        getConfig().addDefault([
                new DoubleEntry("extraDmg", 1D)
        ])

        Enchantments.enchantConfig.queueSave()
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (target !instanceof Player) return

        if (proc(player, enchantLevel)) {
            target.getLocation().getWorld().strikeLightningEffect(target.getLocation())

            def dmg = getConfig().getDoubleEntry("extraDmg").value * enchantLevel
            if (dmg > 3) dmg = 3

            target.damage(dmg)
        }
    }
}
