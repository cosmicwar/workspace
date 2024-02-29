package scripts.factions.features.enchant.enchants.elite

import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class RocketEscape extends CustomEnchantment {

    RocketEscape() {
        super(
                "rocketescape",
                EnchantmentTier.ELITE,
                EnchantmentType.PROC,
                "Rocket Escape",
                ["Chance to launch you into the air", "and give you regeneration"],
                [ItemType.BOOTS],
                3
        )

        setStackable(false)
        setProcChance(0.17D)
        setCoolDown(180)
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (player.getHealth() - event.getFinalDamage() > 0D) return

        if (attacker !instanceof Player) return

        if (proc(player, enchantLevel))
        {
            event.setCancelled(true);
            event.setDamage(0D);

            player.sendMessage("§a§l(!) §aYour Rocket Escape boots have activated, recover while they last!")

            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 0.54F);
            addPotionWithDuration(player, PotionEffectType.REGENERATION, enchantLevel, 2 * enchantLevel * 20)
            player.setVelocity(new Vector(0, 4 + (enchantLevel * 2), 0))
            player.spawnParticle(Particle.EXPLOSION_LARGE, player.getEyeLocation(), 3)
        }
    }
}
