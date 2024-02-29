package scripts.factions.features.enchant.enchants.elite

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class Execute extends CustomEnchantment {

    Execute() {
        super(
                "execute",
                EnchantmentTier.ELITE,
                EnchantmentType.PROC,
                "Execute",
                ["Chance to deal bonus damage", "to low health targets"],
                [ItemType.SWORD],
                7
        )

        setStackable(false)
        setProcChance(0.15D)
        setCoolDown(3)

        getConfig().addDefault([
                new DoubleEntry("healthThreshold", 8D, "target.getHealth() > healthThreshold = return"),
                new DoubleEntry("additionalMultiplierPerLevel", 2D / 7D)
        ])

        Enchantments.enchantConfig.queueSave()
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event)
    {
        if (target !instanceof Player) return

        if (target.getHealth() > getConfig().getDoubleEntry("healthThreshold").value) return

        if (proc(player, enchantLevel))
        {
            EnchantUtils.scaleDamage(event, 1D + (getConfig().getDoubleEntry("additionalMultiplierPerLevel").value * enchantLevel));
        }

    }

}
