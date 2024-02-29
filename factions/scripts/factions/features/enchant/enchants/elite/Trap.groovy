package scripts.factions.features.enchant.enchants.elite

import org.bukkit.event.entity.PlayerDeathEvent
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

import java.util.concurrent.ConcurrentHashMap

class Trap extends CustomEnchantment
{
    Closure<Boolean> hasMetaphysical = Exports.ptr("ench:metaphysical:contains") as Closure<Boolean>
    Closure<Boolean> proc = Exports.ptr("ench:metaphysical:proc") as Closure<Boolean>
    Closure<Void> sendMetaphysicalMessage = Exports.ptr("ench:metaphysical:sendMessage") as Closure<Void>

    ConcurrentHashMap<UUID, Float> trappedPlayers = new ConcurrentHashMap<>() //pUUID, Original walk speed

    Trap()
    {
        super(
                "trap",
                EnchantmentTier.ELITE,
                EnchantmentType.PROC,
                "Trap",
                ["Chance to trap your opponent", "for a short duration"],
                [ItemType.SWORD],
                3
        )

        setStackable(false)
        setProcChance(0.03D)
        setCoolDown(5)

        Events.subscribe(PlayerDeathEvent.class).handler { event ->
            restoreWalkSpeed(event.getPlayer())
        }
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event)
    {
        if (target !instanceof Player) return

        if (!proc(player, enchantLevel)) return

        if (hasMetaphysical.call(target) as Boolean) {
            if (proc(target)) {
                sendMetaphysicalMessage(target)
                return
            }
        }

        Player targetPlayer = (Player) target
        if (trappedPlayers.containsKey(targetPlayer.getUniqueId())) return

        trappedPlayers.put(targetPlayer.getUniqueId(), targetPlayer.getWalkSpeed())
        targetPlayer.setWalkSpeed(0F)

        targetPlayer.sendMessage("§c§l(!) §cYou've been trapped by {player}!".replace("{player}", player.getName()))

        Schedulers.async().runLater({restoreWalkSpeed(targetPlayer)}, 20*enchantLevel)
    }


    void restoreWalkSpeed(Player player) {
        if (!player.isOnline()) return

        Float originalWalkSpeed = trappedPlayers.get(player.getUniqueId())
        if (originalWalkSpeed == null) return

        trappedPlayers.remove(player.getUniqueId())
        player.setWalkSpeed(originalWalkSpeed);
        player.sendMessage("§a§l(!) §aYou are no longer trapped!")
    }
}