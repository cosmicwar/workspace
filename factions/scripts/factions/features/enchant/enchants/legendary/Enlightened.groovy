package scripts.factions.features.enchant.enchants.legendary

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class Enlightened extends CustomEnchantment {

    Enlightened() {
        super(
                "enlightened",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.PROC,
                "Enlightened",
                ["Chance to heal yourself", "when you are attacked"],
                [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                3
        )

        setStackable(false)
        setProcChance(0.1D)
        setCoolDown(5)

        getConfig().addDefault([
                new DoubleEntry("healAmount", 2, "Heal Amount * level")
        ])
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (!(attacker instanceof Player)) return

        if (proc(player, enchantLevel)) {
            EnchantUtils.heal(player, enchantLevel * getConfig().getDoubleEntry("healAmount").value)
        }

    }
}
