package scripts.factions.features.enchant.enchants.legendary

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Schedulers
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.shared.core.cfg.entries.IntEntry
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantPriority
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.shared.features.actionbar.ActionBarBuilder
import scripts.shared.utils.ItemType

import java.util.concurrent.ConcurrentHashMap

class Rage extends CustomEnchantment {

    private Map<UUID, Integer> pvp_combos = new ConcurrentHashMap<UUID, Integer>()
    private Map<UUID, Integer> pve_combos = new ConcurrentHashMap<UUID, Integer>()

    private Map<UUID, Long> usersToRemoveActionBar = new ConcurrentHashMap<UUID, Integer>()

    Closure sendActiveActionBar = Exports.ptr("actionbars:sendActiveActionBar") as Closure

    Rage() {
        super(
                "rage",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.NORMAL,
                "Rage",
                ["Increases damage dealt"],
                [ItemType.SWORD, ItemType.AXE],
                6,
                false,
                EnchantPriority.HIGH
        )

        getConfig().addDefault([
                new IntEntry("maxCombo", 10),
                new DoubleEntry("dmgIncPerCombo", 0.1)
        ])

        Enchantments.enchantConfig.queueSave()

        createActionBar()

        Schedulers.async().runRepeating({
            Iterator<Map.Entry<UUID, Long>> iterator = usersToRemoveActionBar.entrySet().iterator()
            while (iterator.hasNext()) {
                Map.Entry<UUID, Long> entry = iterator.next()
                if (System.currentTimeMillis() > entry.getValue()) {
                    pvp_combos.put(entry.getKey(), 0)

                    Schedulers.sync().runLater({
                        pvp_combos.remove(entry.getKey())
                        usersToRemoveActionBar.remove(entry.getKey())
                    }, 20L)
                }
            }
        }, 0L, 20L)
    }

    def createActionBar() {
        new ActionBarBuilder("rage", 0, { Player player ->
            return pvp_combos.containsKey(player.getUniqueId())
        }, { Player player ->
            return "§eRage Stacks §fx${getPvpCombo(player) == 0 ? "§c BROKEN" : getPvpCombo(player)}"
        })
    }

//    @Override
//    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
//        int combo
//        if (!(target instanceof LivingEntity) || event.getFinalDamage() <= 0D) return
//        if (target instanceof Player) {
//            if (new Random().nextDouble() < 0.3) target.damage(4D) //additional crit dmg
//            combo = getPvpCombo(player)
//            if (combo > 0) {
//                EnchantUtils.scaleDamage(event, 1D + combo * getConfig().getDoubleEntry("dmgIncPerCombo").value)
//            }
//            incPvpCombo(player)
//            usersToRemoveActionBar.put(player.getUniqueId(), System.currentTimeMillis() + 3500L)
//            sendActiveActionBar.call(player)
//        } else if (target instanceof Entity) {
//            combo = getPveCombo(player)
//            if (combo > 0) {
//                EnchantUtils.scaleDamage(event, 1D + combo * getConfig().getDoubleEntry("dmgIncPerCombo").value)
//            }
//            incPveCombo(player)
//        }
//    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        resetCombo(player)
    }

    private int getPvpCombo(Player player) {
        return pvp_combos.getOrDefault(player.getUniqueId(), 0);
    }

    private int getPveCombo(Player player) {
        return pve_combos.getOrDefault(player.getUniqueId(), 0);
    }

    private void incPvpCombo(Player player) {
        pvp_combos.merge(player.getUniqueId(), 1, (a, b) -> Math.min(getConfig().getIntEntry("maxCombo").value, a + b));
    }

    private void incPveCombo(Player player) {
        pve_combos.merge(player.getUniqueId(), 1, (a, b) -> Math.min(getConfig().getIntEntry("maxCombo").value, a + b));
    }

    private void resetCombo(Player player) {
        pvp_combos.remove(player.getUniqueId())
        pve_combos.remove(player.getUniqueId())
    }
}