package scripts.factions.patches

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import scripts.shared.legacy.utils.TimeUtils
import scripts.shared.utils.Persistent

import java.util.concurrent.TimeUnit

Map<UUID, Long> blessCooldowns = Persistent.of("bless_cooldown", new HashMap<UUID, Long>()).get()

Closure removeMatchingEffect = Exports.ptr("potionEffects:removeMatchingEffect") as Closure

Commands.create()/*.assertPermission("starcade.bless")*/.assertPlayer().handler { c ->
/*    Long cooldown = blessCooldowns.get(c.sender().getUniqueId())
    if (cooldown != null && cooldown > System.currentTimeMillis()) {
        c.sender().sendMessage("§c§l(!) §cYou must wait " + TimeUtils.getTimeAmount(cooldown) + " before using /bless again!")
        return
    }*/
    Player player = c.sender()

//    if (!player.isOp()) blessCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1))

    removeMatchingEffect.call(player, PotionEffectType.SLOW)
    removeMatchingEffect.call(player, PotionEffectType.SLOW_DIGGING)

    player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0F, 1.0F)
    player.sendMessage("§e§lBLESSED!")
}.register("bless")