package scripts.factions.features.enchant.enchants.elite

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.relation.RelationType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class Shockwave extends CustomEnchantment {

    Shockwave() {
        super(
                "shockwave",
                EnchantmentTier.ELITE,
                EnchantmentType.PROC,
                "Shockwave",
                ["Chance to push away nearby enemies", "when you are attacked"],
                [ItemType.CHESTPLATE],
                5
        )

        setStackable(false)
        setProcChance(0.02D)
        setCoolDown(8)

        getConfig().addDefault([
                new DoubleEntry("pushAway", 1.8D, "Push Away Distance")
        ])
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (attacker !instanceof Player) return

        if (!proc(player, enchantLevel)) return

        def member = Factions.getMember(player.getUniqueId())

        player.getNearbyEntities(4D, 4D, 4D).forEach(enemy -> {
            if (enemy !instanceof Player) return

            def target = Factions.getMember(enemy.getUniqueId())

            def relation = Factions.getRelationType(member, target)

            if (relation == RelationType.ENEMY || relation == RelationType.NEUTRAL) {
                EnchantUtils.pushAway(player.getLocation(), enemy, getConfig().getDoubleEntry("pushAway").value)
            }
        });
    }
}
