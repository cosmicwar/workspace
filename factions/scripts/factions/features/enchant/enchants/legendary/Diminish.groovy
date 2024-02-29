package scripts.factions.features.enchant.enchants.legendary

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.content.dbconfig.entries.StringEntry
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

import java.util.concurrent.ConcurrentHashMap

class Diminish extends CustomEnchantment {
    private Map<UUID, Double> diminishedPlayers = new ConcurrentHashMap<UUID, Double>()

    Diminish() {
        super(
                "diminish",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.PROC,
                "Diminish",
                ["Reduces damage taken"],
                [ItemType.CHESTPLATE],
                5
        )

        setStackable(false)
        setProcChance(0.03D)
        setCoolDown(3)

        getConfig().addDefault([
                new StringEntry("message", "§6§lDiminish §> §e§l{damage} Damage")
        ])
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (event.getFinalDamage() < 0D) return

        Double damageCap = diminishedPlayers.remove(player.getUniqueId())
        if (damageCap != null) {
            event.setDamage(Math.min(event.getFinalDamage(), damageCap))
        }

        if (!proc(player, enchantLevel)) return
        diminishedPlayers.put(player.getUniqueId(), event.getFinalDamage() / 2D)
        String msg = getConfig().getStringEntry("message").value
        msg = msg.replace("{damage}", (event.getFinalDamage() / 2D).toString())
        player.sendMessage(msg)
    }
}
