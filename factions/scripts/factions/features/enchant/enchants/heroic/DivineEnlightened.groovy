package scripts.factions.features.enchant.enchants.heroic

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.struct.HeroicEnchant
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class DivineEnlightened extends HeroicEnchant {

    DivineEnlightened() {
        super(
                "divine_enlightened",
                EnchantmentType.PROC,
                "Divine Enlightened",
                "enlightened",
                ["Chance to heal yourself when attacked"],
                [ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS],
                3
        )

        setStackable(false)
        setProcChance(0.15D)
        setCoolDown(10)

        getConfig().addDefault([
                new DoubleEntry("healAmount", 4, "Heal Amount * level")
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
