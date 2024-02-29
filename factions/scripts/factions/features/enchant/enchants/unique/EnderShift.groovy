package scripts.factions.features.enchant.enchants.unique

import com.google.common.collect.Sets
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class EnderShift extends CustomEnchantment {

    Set<UUID> enderShifting = Sets.newConcurrentHashSet()

    EnderShift() {
        super(
            "ender_shift",
            EnchantmentTier.UNIQUE,
            EnchantmentType.NORMAL,
            "Ender Shift",
            ["Sends you to the Ender dimension if you", "were going to die"],
            [ItemType.HELMET],
            3,
            true
        )

        setCoolDown(180)
        setProcChance(0.10D)
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (player.getHealth() - event.getFinalDamage() > 0D) return;

        event.setCancelled(true)
        event.setDamage(0D)
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1F, 1F);


        player.sendMessage("§d (!) You have endershifted!")


        addPotionWithDuration(player, PotionEffectType.NIGHT_VISION, 20 * (enchantLevel + 7), 0)
        addPotionWithDuration(player, PotionEffectType.SPEED, 20 * (enchantLevel + 7), enchantLevel + 2)
        addPotionWithDuration(player, PotionEffectType.JUMP, 20 * (enchantLevel + 7), enchantLevel - 1)
        addPotionWithDuration(player, PotionEffectType.ABSORPTION, 20 * (enchantLevel + 7), enchantLevel + 2)

        enderShifting.add(player.getUniqueId())
        Schedulers.async().runLater({enderShifting.remove(player.getUniqueId())}, 40)
    }

    void events() {
        Events.subscribe(EntityDamageByEntityEvent.class).handler { event ->
            Entity damager = EnchantUtils.getLiableDamager(event)
            Entity player = event.getEntity()
            if (damager !instanceof Player || player !instanceof Player) return
            player = (Player) player
            damager = (Player) damager
            if (enderShifting.contains(player.getUniqueId())) return

            event.setCancelled(true)
            event.setDamage(0)
            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIFIED_PIGLIN_ANGRY, 0.6F, 0.8F)
            damager.playSound(player.getLocation(), Sound.ENTITY_ZOMBIFIED_PIGLIN_ANGRY, 0.6F, 0.8F)
            damager.sendMessage("§d (!)" + player.getName() + "§d is endershifting!")
        }
    }
}
