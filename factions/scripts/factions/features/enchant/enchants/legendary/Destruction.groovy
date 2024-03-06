package scripts.factions.features.enchant.enchants.legendary

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.helper.Schedulers
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.relation.RelationType
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

import java.util.concurrent.ConcurrentHashMap

class Destruction extends CustomEnchantment {

    protected static Map<Player, Integer> equipped = new ConcurrentHashMap<>()

    Destruction() {
        super(
                "destruction",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.NORMAL,
                "Destruction",
                ["Damages nearby enemies"],
                [ItemType.HELMET],
                5
        )

        getConfig().addDefault([
                new DoubleEntry("radiusPerLevel", 1.5)
        ])

        Enchantments.enchantConfig.queueSave()

        Schedulers.async().runRepeating(() -> {
            equipped.forEach((player, enchantLevel) -> {

                def member = Factions.getMember(player.getUniqueId())

                double radius = getConfig().getDoubleEntry("radiusPerLevel").value * enchantLevel


                Schedulers.sync().run {
                    for (Player enemy : EnchantUtils.getNearbyEnemyPlayers(player, radius, false)) {
                        if (!enemy.isDead()) enemy.damage(enchantLevel == 5 ? 2D : 1D)
                    }
                }
            })
        }, 20L, 200L)
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        equipped.put(player, enchantLevel)
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        equipped.remove(player)
    }

}
