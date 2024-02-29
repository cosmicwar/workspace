package scripts.factions.features.enchant.enchants.ultimate


import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.content.dbconfig.entries.LongEntry
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

import java.util.concurrent.ConcurrentHashMap


class Dominate extends CustomEnchantment {

    Map<UUID, ArrayList<Object>> DominatedPlayers = new ConcurrentHashMap<>()

    Dominate() {
        super(
                "dominate",
                EnchantmentTier.ULTIMATE,
                EnchantmentType.PROC,
                "Dominate",
                ["Dominate players for 5 seconds per level."],
                [ItemType.SWORD],
                4
        )

        setProcChance(0.04D)

        getConfig().addDefault([
                new LongEntry("durationperlvl", 1500L)
        ])

        Enchantments.enchantConfig.queueSave()
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (!(target instanceof Player) || !proc(player, enchantLevel)) return
        if (DominatedPlayers.containsKey(target.getUniqueId())) return
        Long expireTime = System.currentTimeMillis() + (getConfig().getLongEntry("durationperlvl").value * enchantLevel)
        DominatedPlayers.put(target.getUniqueId(), new ArrayList<Object>(2))
        ArrayList<Object> data = DominatedPlayers.get(target.getUniqueId())
        data.add(expireTime)
        data.add(enchantLevel)
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (!(attacker instanceof Player)) return
        if (!DominatedPlayers.containsKey(attacker.getUniqueId())) return
        if ((Long) DominatedPlayers.get(attacker.getUniqueId()).get(0) < System.currentTimeMillis()) {
            DominatedPlayers.remove(attacker.getUniqueId())
            return
        }
        Integer level = (Integer) DominatedPlayers.get(attacker.getUniqueId()).get(1)
        EnchantUtils.scaleDamage(event, 1D - (level * 0.05D))
    }

}
