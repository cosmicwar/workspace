package scripts.factions.features.enchant.enchants.elite

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.content.dbconfig.entries.IntEntry
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class Cactus extends CustomEnchantment {

    Cactus() {
        super(
                "cactus",
                EnchantmentTier.ELITE,
                EnchantmentType.PROC,
                "Cactus",
                ["Chance to damage your opponent", "for a portion of the damage you take"],
                [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                2
        )

        setStackable(true)
        setProcChance(0.04D)
        setCoolDown(3)

        getConfig().addDefault([
                new DoubleEntry("damage", 2.0D, "Return Damage Divisor"),
                new DoubleEntry("scaleFactor", 0.5D, "Damage Scale Factor")
        ])
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (attacker !instanceof Player) return

        if (proc(player, enchantLevel)) {
            double damage = event.getFinalDamage() / getConfig().getDoubleEntry("damage").value
            EnchantUtils.scaleDamage(event, getConfig().getDoubleEntry("scaleFactor").value)

            attacker.damage(damage)
        }
    }
}
