package scripts.factions.content.combat

import com.comphenix.protocol.events.PacketAdapter
import com.google.common.collect.Maps
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.jodah.expiringmap.ExpiringMap
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@CompileStatic(TypeCheckingMode.SKIP)
class CombatTag
{



    static Map<UUID, CombatData> combatData = Maps.newConcurrentMap()
    static Map<UUID, UUID> taggedPlayers = ExpiringMap.builder().expiration(20, TimeUnit.SECONDS).expirationListener { UUID uuid, UUID npc ->
        def player = Bukkit.getPlayer(uuid)
        if (player != null) Players.msg(player, "§cYou are no longer in combat.")

        def data = combatData.get(uuid)
        if (data != null) data.combatTagExpiration = null
    }.build()

    static final long COMBAT_NPC_DESPAWN_TIMER = TimeUnit.SECONDS.toMillis(30L)
    static final double COMBAT_NPC_DAMAGE_PER_HIT = 1D

    static Map<UUID, CombatLogNPC> combatLoggers = new ConcurrentHashMap<>()
    static PacketAdapter packetAdapter

    CombatTag() {
        Bukkit.getOnlinePlayers().each {
            combatData.computeIfAbsent(it.uniqueId, { new CombatData(it) })
        }
    }

    static def events() {
        Events.subscribe(PlayerJoinEvent.class).handler {event ->
            if (!combatData.containsKey(event.player.uniqueId)) {
                combatData.put(event.player.uniqueId, new CombatData(event.player.uniqueId))
            } else {
                def data = combatData.get(event.player.uniqueId)
            }
        }

        Events.subscribe(PlayerQuitEvent.class, EventPriority.LOW).handler {event ->
            def data = combatData.get(event.player.uniqueId)
            if (data != null) {
                if (data.isTagged()) {

                }
            }
        }

        Events.subscribe(EntityDamageByEntityEvent.class).handler { event ->
            if (event.isCancelled()) return

            def entity = event.entity
            def damager = event.damager

            if (entity !instanceof Player) return
            def player = (Player) entity

            if (damager instanceof Player) {
                def data = combatData.computeIfAbsent(player.uniqueId, { new CombatData(it) })
                if (!data.combatTagExpiration) {
                    tag(player, damager, 20_000L)
                }

                def damagerData = combatData.computeIfAbsent(damager.uniqueId, { new CombatData(it) })
                if (!damagerData.combatTagExpiration) {
                    tag(damager, 20_000L)
                }
            }
        }
    }

    static def isTagged(Player player) {
        return taggedPlayers.containsKey(player.uniqueId)
    }

    static def tag(Player player, Long duration) {
        def data = combatData.computeIfAbsent(player.uniqueId, { new CombatData(it) })

        if (!data.isTagged()) {
            Players.msg(player, "§cYou are now in combat for §l${duration / 1000}§c seconds.")
        }

        data.combatTagExpiration = System.currentTimeMillis() + duration
        taggedPlayers.put(player.uniqueId, player.uniqueId, 20, TimeUnit.SECONDS)
    }

    static def tag(Player player, Entity damager, Long duration) {
        def data = combatData.computeIfAbsent(player.uniqueId, { new CombatData(it) })

        if (!data.isTagged()) {
            Players.msg(player, "§cYou are now in combat for §l${duration / 1000}§c seconds.")
        }

        data.combatTagExpiration = System.currentTimeMillis() + duration
        data.setLastDamager(damager)
    }
}
