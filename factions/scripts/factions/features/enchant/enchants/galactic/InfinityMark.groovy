package scripts.factions.features.enchant.enchants.galactic

import com.google.common.collect.Sets
import org.bukkit.ChatColor
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.features.EntityGlow
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.ItemType

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

class InfinityMark extends CustomEnchantment {

    /*Attacker, MarkedPlayer */
    ConcurrentHashMap<UUID, Mark> activeMarks = new ConcurrentHashMap<>()

    InfinityMark() {
        super(
                "infinitymark",
                EnchantmentTier.GALAXY,
                EnchantmentType.PROC,
                "Infinity Mark",
                ["Chance to mark an enemy for a period of time where you will see their outline and deal extra damage to them."],
                [ItemType.SWORD],
                1,
                false
        )

        setProcChance(0.004D)
        setCoolDown(120)

        Commands.create().assertPlayer().handler {
            def mark = new Mark(it.sender(), it.sender(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(15))

            mark.sendGlow()
        }.register("testmark")

        Events.subscribe(EntityDamageByEntityEvent.class).handler { event ->
            if (event.getDamager() !instanceof Player || event.getEntity() !instanceof LivingEntity) return

            def player = event.getDamager() as Player
            def target = event.getEntity() as LivingEntity
            def mark = activeMarks.get(target.getUniqueId())
            if (mark != null) {
                if (mark.shownPlayers.contains(player.getUniqueId())) {
                    EnchantUtils.scaleDamage(event, 1D + ThreadLocalRandom.current().nextDouble(0D, .1D))
                    player.spawnParticle(Particle.CRIT_MAGIC, target.getLocation().add(0D, 0.75D, 0D), 20)
                }
            }
        }
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (target !instanceof Player) return

        if (activeMarks.containsKey(target.getUniqueId())) return

        if (proc(player, enchantLevel)) {
            def mark = new Mark(target, player, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(15))

            mark.sendGlow()

            Players.msg(player, "")
            Players.msg(player, ColorUtil.color("   §<#ff0000>INFINITY MARK §7- §<#ff0000>MARKED §7" + target.getName() + " §<#ff0000>FOR 15 SECONDS."))
            Players.msg(player, "")

            Players.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL)

            Players.msg(target, "")
            Players.msg(target, ColorUtil.color("§<#ff0000>You have been marked by §7" + player.getName() + " for 15 seconds."))
            Players.msg(target, "")

            Players.playSound(player, Sound.ENTITY_CREEPER_HURT)

            activeMarks.put(target.getUniqueId(), mark)
        }
    }

    class Mark {
        Player targetedPlayer
        Long expiration

        Set<Player> shownPlayers = Sets.newHashSet()

        Mark(Player targetedPlayer, Player attacker, Long expiration) {
            this.targetedPlayer = targetedPlayer
            this.expiration = expiration
            shownPlayers.add(targetedPlayer)
            shownPlayers.add(attacker)
        }

        def sendGlow(ChatColor color = ChatColor.WHITE) {
            EntityGlow.sendGlow(shownPlayers, targetedPlayer, color)

            Schedulers.async().runLater({
                EntityGlow.removeGlow(targetedPlayer)
                activeMarks.remove(targetedPlayer.getUniqueId())
            }, 15, TimeUnit.SECONDS)
        }
    }
}
