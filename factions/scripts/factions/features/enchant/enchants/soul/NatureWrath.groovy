package scripts.factions.features.enchant.enchants.soul


import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType


class NatureWrath extends CustomEnchantment {

    Closure<Boolean> hasPMetaphysical = Exports.ptr("ench:pmetaphysical:contains") as Closure<Boolean>
    Closure<Boolean> proc = Exports.ptr("ench:pmetaphysical:proc") as Closure<Boolean>
    Closure<Void> sendPMetaphysicalMessage = Exports.ptr("ench:pmetaphysical:sendMessage") as Closure<Void>

    NatureWrath() {
        super(
                "naturewrath",
                EnchantmentTier.SOUL,
                EnchantmentType.PROC,
                "Nature's Wrath",
                ["Chance to strike your target", "with divine lightning"],
                [ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS],
                4
        )
//        setProcChance(1D)
        setConsumption(150)
        setCoolDown(80)
        setProcChance(0.0075D)
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (!proc(player, enchantLevel)) return
        if (!(attacker instanceof Player) || event.getFinalDamage() <= 0D) {
            return;
        }
        if (!consumeSouls(player, true)) return

        player.sendMessage("§e§l  ** NATURE'S WRATH **  §r§c(-150 souls)")
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.65f);

        int radius = 8 + enchantLevel * 5;
        Set<Player> nearbyEnemyPlayers = EnchantUtils.getNearbyEnemyPlayers(player, radius, false);
        nearbyEnemyPlayers.forEach(p -> {
            p.getWorld().strikeLightningEffect(p.getLocation());

            float ws = p.getWalkSpeed()

            boolean flag1 = false
            if (hasPMetaphysical.call(p) as Boolean) {
                if (proc(p)) {
                    sendPMetaphysicalMessage(p)
                    flag1 = true
                }
            }

            //pMetaphysical proc / bypass
            if (!flag1) {
                p.setWalkSpeed(0.0f)
                addPotionWithDuration(p, PotionEffectType.JUMP, 128, (2 + enchantLevel))
                addPotionWithDuration(p, PotionEffectType.SLOW, 128, (2 + enchantLevel))
            }
            addPotionWithDuration(p, PotionEffectType.WEAKNESS, 2, (2 + enchantLevel))
            Schedulers.async().runLater({p.setWalkSpeed(ws)}, 20 * (2 + enchantLevel))
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 2.0f)

            Task task

            int duration = 0
            task = Schedulers.sync().runRepeating({
                if (duration >= enchantLevel) {
                    task.stop()
                    return
                }

                nearbyEnemyPlayers.forEach(pl -> {
                    pl.getWorld().strikeLightning(pl.getLocation())
                    pl.sendMessage()
                    pl.sendMessage("§c§l  ** NATURE'S WRATH **")
                    pl.damage(2.5)
                })

                duration++
            }, 20L, 20L)
        })
    }
}
