package scripts.factions.features.enchant.enchants.legendary

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class Lifesteal extends CustomEnchantment {

    Lifesteal() {
        super(
                "lifesteal",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.PROC,
                "Lifesteal",
                ["Chance to heal yourself", "when you attack"],
                [ItemType.SWORD],
                5
        )

        setStackable(false)
        setProcChance(0.05D)
        setCoolDown(5)

        getConfig().addDefault([
                new DoubleEntry("weight", 1)
        ])
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (target !instanceof Player) return
        if (!proc(player, enchantLevel)) return

        EnchantUtils.heal(player, (event.getFinalDamage() / 2D) + (enchantLevel * (getConfig().getDoubleEntry("weight").value)))
    }
}
