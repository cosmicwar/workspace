package scripts.factions.features.enchant.enchants.elite

import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.relation.RelationType
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class SmokeBomb extends CustomEnchantment
{

    SmokeBomb()
    {
        super(
                "smokebomb",
                EnchantmentTier.ELITE,
                EnchantmentType.PROC,
                "Smoke Bomb",
                ["Chance to blind and slow nearby enemies", "when you are attacked"],
                [ItemType.HELMET],
                3
        )

        setStackable(false)
        setProcChance(0.03D)
        setCoolDown(15)

        getConfig().addDefault([
                new DoubleEntry("radius", 1.5D, "Radius")
        ])
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event)
    {
        if (attacker !instanceof Player) return

        if (player.getHealth() - event.getFinalDamage() > 5D || !proc(player, enchantLevel)) return

        if (!proc(player, enchantLevel)) return

        def member = Factions.getMember(player.getUniqueId())


        def radius = getRadius(enchantLevel)
        player.getNearbyEntities(radius, radius, radius).forEach {enemy ->
            if (enemy !instanceof Player) return

            enemy = (Player) enemy

            def target = Factions.getMember(enemy.getUniqueId())

            def relation = Factions.getRelationType(member, target)

            if (relation == RelationType.ENEMY || relation == RelationType.NEUTRAL) {
                addPotionWithDuration(enemy, PotionEffectType.BLINDNESS, enchantLevel, 3 * 20)
                addPotionWithDuration(enemy, PotionEffectType.SLOW_DIGGING, enchantLevel, 3 * 20)
                enemy.playSound(enemy.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1F, 1F)
            }
        }
    }

    double getRadius(int enchantLevel)
    {
        return getConfig().getDoubleEntry("radius").value * enchantLevel
    }
}
