package scripts.factions.core.faction

import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players
import scripts.factions.core.faction.claim.Claim
import scripts.factions.core.faction.data.relation.RelationType
import scripts.shared.data.obj.CL

import java.util.concurrent.ConcurrentHashMap

class FactionEvents {

    FactionEvents() {
        events()
    }

    static def events() {
        Events.subscribe(EntityDamageByEntityEvent.class).handler {event ->
            def damager = event.damager
            def entity = event.entity

            if (damager !instanceof Player || entity !instanceof Player) return

            def player = damager as Player
            def target = entity as Player

            def playerMember = Factions.getMember(player.getUniqueId())
            def targetMember = Factions.getMember(target.getUniqueId())

            if (playerMember == null || targetMember == null) return

            def relation = Factions.getRelationType(playerMember, targetMember)
            switch (relation) {
                case RelationType.ALLY:
                    event.setCancelled(true)
                    Players.msg(player, "§2§l[§a§lF§2§l] §cYou cannot damage §dAllies§c.")
                    break
                case RelationType.TRUCE:
                    event.setCancelled(true)
                    Players.msg(player, "§2§l[§a§lF§2§l] §cYou cannot damage §bTruces§c.")
                    break
                case RelationType.MEMBER:
                    event.setCancelled(true)
                    Players.msg(player, "§2§l[§a§lF§2§l] §cYou cannot damage §aMembers§c.")
                    break
            }
        }

        Events.subscribe(ProjectileHitEvent.class).handler {event ->
            def projectile = event.getEntity()
            def shooter = projectile.getShooter()
            def hitEntity = event.getHitEntity()

            if (shooter !instanceof Player || hitEntity !instanceof Player) return

            if (projectile instanceof ThrownPotion) return // TODO: add future support for potion effects

            def player = shooter as Player
            def target = hitEntity as Player

            def playerMember = Factions.getMember(player.getUniqueId())
            def targetMember = Factions.getMember(target.getUniqueId())

            if (playerMember == null || targetMember == null) return

            def relation = Factions.getRelationType(playerMember, targetMember)

            switch (relation) {
                case RelationType.ALLY:
                    event.setCancelled(true)
                    Players.msg(player, "§2§l[§a§lF§2§l] §cYou cannot damage §dAllies§c.")
                    break
                case RelationType.TRUCE:
                    event.setCancelled(true)
                    Players.msg(player, "§2§l[§a§lF§2§l] §cYou cannot damage §bTruces§c.")
                    break
                case RelationType.MEMBER:
                    event.setCancelled(true)
                    Players.msg(player, "§2§l[§a§lF§2§l] §cYou cannot damage §aMembers§c.")
                    break
            }
        }
    }

}
