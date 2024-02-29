package scripts

import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import groovy.transform.Field
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import scripts.shared.systems.BungeeCache
import scripts.shared.utils.Configs
import scripts.shared.utils.Temple

import java.text.DecimalFormat

import static scripts.shared.utils.Colors.parse
import static scripts.shared.utils.JsonUtils.joinArray

Events.subscribe(PlayerJoinEvent.class, EventPriority.HIGHEST).handler { e ->
    Schedulers.sync().runLater({
        //e.player.getActivePotionEffects().forEach({effect -> e.player.removePotionEffect(effect.getType())})

        def join = Configs.loadFirst("join.json")
        if (join.get("joinmessage") != null) {
            Players.msg(e.player, parse(joinArray(join.get("joinmessage").asJsonArray, "\n")))
            def title = join.get("title").asJsonObject
            e.player.showTitle(
                    TextComponent.fromLegacyText(parseText(e.player, title.get("main").asString)),
                    TextComponent.fromLegacyText(parseText(e.player, title.get("subtitle").asString)), 10, 40, 10
            )
        }
    }, 5)
}

@Field
DecimalFormat df = new DecimalFormat("#.#");
def getTps(boolean formatted) {
    def tps = Bukkit.getTPS()[0]
    if (!formatted) {
        return df.format(tps)
    }

    if (System.hasProperty("hubMode") && Boolean.valueOf(System.getProperty("hubMode"))) {
        return parse(tps > 8 ? "&a" : tps > 6 ? "&e" : "&c") + df.format(tps)
    }

    return parse(tps > 18 ? "&a" : tps > 16 ? "&e" : "&c") + df.format(tps)
}

def parseText(Player p, String text) {
    return parse(text.replace("%server%", Temple.templeId.substring(0, 1).toUpperCase() + Temple.templeId.substring(1, Temple.templeId.length()))
            .replace("%player%", p.getName())
            .replace("%ping%", p.spigot().ping.toString())
            .replace("%playerslocal%", Bukkit.getOnlinePlayers().size().toString())
            .replace("%playersglobal%", BungeeCache.getGlobalPlayerCount().toString())
            .replace("%tps%", getTps(false))
            .replace("%tpsformatted%", getTps(true))
    )
}