package scripts.factions.features.enchant.enchants.elite

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class SpiritLink extends CustomEnchantment {

    // TODO
    SpiritLink() {
        super(
                "spiritlink",
                EnchantmentTier.ELITE,
                EnchantmentType.PROC,
                "Spirit Link",
                ["Chance to heal yourself", "when you are attacked"],
                [ItemType.CHESTPLATE],
                5
        )

        setStackable(false)
        setProcChance(0.03D)
        setCoolDown(5)

        getConfig().addDefault([
                new DoubleEntry("healAmount", 4D, "Heal Amount")
        ])
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (attacker !instanceof Player) return
        if (!proc(player, enchantLevel)) return

        double healAmount = event.getFinalDamage() / getConfig().getDoubleEntry("healAmount").value

    }
}
