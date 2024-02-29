package scripts.exec.hub

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.SimpleItem
import groovy.transform.Field
import io.papermc.paper.configuration.GlobalConfiguration
import net.minecraft.network.chat.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.TabCompleteEvent
import org.spigotmc.SpigotConfig
import scripts.shared.legacy.utils.StringUtils
import scripts.shared3.ArkPerms

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

Exports.ptr("hub_selector_slot", 4)
Exports.ptr("hub_selector_item", new SimpleItem(Material.COMPASS).setName("§b§lServer Selector"))

Exports.ptr("hub_selector_title", "§b§lServer Selector")
Exports.ptr("hub_selector_mask_positions", [
        "111111111",
        "111111111",
        "111111111"
])
Exports.ptr("hub_selector_mask_colors", [
        0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0,
])
Exports.ptr("hub_selector_design", [
        "033304440",
        "033304440",
        "033304440"
])
Exports.ptr("hub_selector_suffix", [
        "",
        " §b§lPLAYERS §f%players%/%maxplayers%",
        " §b§lQUEUE §f%currentqueue%/%queue%",
        ""
])
Exports.ptr('hub_selector_server', [
        [
                "slot"     : 11,
                "id"       : "prison1",
                "icon"     : Material.DIAMOND_PICKAXE,
                "name"     : "§9»§b»§9» §b§lPRISON REALM §r§9«§b«§9«",
                "enchanted": true,
                "lore"     : [
                        "",
                        "§b§lRELEASED §7on §b§lOctober§7 ${StringUtils.getOrdinal(13)} §b§l3PM EST§f.",
                        "",
                        "§b█▀▀▀▀",
                        "§b▌ §fREDESIGNED ECO",
                        "§b▌ §fNew Enchants",
                        "§b▌ §fOverhauled Pets",
                        "§b▌ §fMining Unlocks",
                        "§b▌ §fInfinite Enchants",
                        "§b▌ §fDungeons, PvE & Ascendant Mine",
                        "§b▌ §fCity, Mining and PvE Paths",
                        "§b▌ §fGiveaways",
                        "§b█▄▄▄▄",
                        "",
                ]
        ],
        [
                "slot"     : 15,
                "id"       : "nova1",
                "icon"     : Material.TNT,
                "name"     : "§4»§c»§4» §c§lFACTIONS §f§lNOVA §4«§c«§4«",
                "enchanted": true,
                "lore"     : [
                        "",
                        "§c§lBETA MODE",
                        "",
                        "§c█▀▀▀▀",
                        "§c▌ §fNew Custom Enchants",
                        "§c▌ §f1.8 PvP Mechanics",
                        "§c▌ §fNew Economy",
                        "§c▌ §fCustom Bosses",
                        "§c▌",
                        "§c▌ §5§lDARK-ZONE",
                        "§c▌ §6§lSTRONGHOLDS",
                        "§c▌ §b§lKOTH",
                        "§c▌ §a§lOUTPOST",
                        "§c█▄▄▄▄",
                ]
        ],
])

Starlight.watch(
        "~/Config.groovy",
        "scripts/_init.groovy",
)

@Field def spam = new ConcurrentHashMap<UUID, AtomicInteger>()

Schedulers.sync().runRepeating({
    spam.clear()
}, 0, 60)

Events.subscribe(PlayerCommandPreprocessEvent).handler { e ->
    def count = spam.computeIfAbsent(e.player.uniqueId) { new AtomicInteger(0) }

    if (e.message.contains("server")) count.addAndGet(100)

    if (count.addAndGet(e.message.size()) > 1000) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban ${e.player.getName()} [autoban] bot *1")
        ((CraftPlayer) e.player).getHandle().connection.connection.disconnect(Component.literal(""))
    }
}

Events.subscribe(AsyncPlayerChatEvent).handler { e ->
    def count = spam.computeIfAbsent(e.player.uniqueId) { new AtomicInteger(0) }

    if (count.addAndGet(e.message.size()) > 1000) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban ${e.player.getName()} [autoban] bot *1")
        ((CraftPlayer) e.player).getHandle().connection.connection.disconnect(Component.literal(""))
    }
}

Events.subscribe(TabCompleteEvent).handler { e ->
    if (e.sender instanceof Player) {
        def p = e.sender as Player

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban ${p.getName()} [autoban] bot *2")
    }
}

SpigotConfig.tabComplete = -1
GlobalConfiguration.get().spamLimiter.recipeSpamLimit = 1
GlobalConfiguration.get().spamLimiter.tabSpamLimit = 1

/*
Schedulers.sync().runRepeating({
    def pool = Redis.redis.jedisPool
    println "${pool.numActive}/${pool.numIdle}/${pool.numWaiters} ${Identity.address}"
}, 20, 20)*/

ArkPerms.addExpansions([
        "group.tag.media": [
                "ads.bypass"
        ]
])