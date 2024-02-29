package scripts.factions.content.scoreboard.health

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.bucket.Bucket
import org.starcade.starlight.helper.bucket.factory.BucketFactory
import org.starcade.starlight.helper.bucket.partitioning.PartitioningStrategies
import scripts.shared.utils.BukkitUtils

@CompileStatic(TypeCheckingMode.SKIP)
class HealthDisplay
{

    // TODO: make this only update players in a certain distance from the player

    static Bucket<Player> bucket = BucketFactory.newConcurrentBucket(100, PartitioningStrategies.nextInCycle())

    HealthDisplay() {
        bucket.addAll(Bukkit.onlinePlayers)

        Schedulers.async().runRepeating({
            def next = bucket.asCycle().next()
            for (def player in next) {
                update(player)
            }
        }, 1, 1)

        events()
    }

    static def events () {
        Events.subscribe(PlayerJoinEvent.class).handler { event ->
            Player player = event.getPlayer()
            Schedulers.sync().runLater({ update(player) }, 2)
        }

        Events.subscribe(EntityDamageByEntityEvent.class).handler { event ->
            if (!(event.getEntity() instanceof Player))  return
            Player player = event.getEntity() as Player
            Schedulers.sync().runLater({ update(player) }, 2)

            def damager = event.getDamager()
            if (damager instanceof Player) {
                Schedulers.sync().runLater({ update(damager) }, 2)
            }
        }

        Events.subscribe(EntityRegainHealthEvent.class).handler { event ->
            if (!(event.getEntity() instanceof Player))  return
            Player player = event.getEntity() as Player
            Schedulers.sync().runLater({ update(player) }, 2)
        }

        Events.subscribe(EntityDamageEvent.class).handler { event ->
            if (!(event.getEntity() instanceof Player))  return
            Player player = event.getEntity() as Player
            Schedulers.sync().runLater({ update(player) }, 2)
        }
    }

    static void update(Player player) {
        def scoreboard = player.getScoreboard()
        if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            Schedulers.sync().execute {
                scoreboard = Bukkit.getScoreboardManager().getNewScoreboard()
            }
        }

        Objective objective = scoreboard.getObjective("health")

        if (objective == null) {
            objective = scoreboard.registerNewObjective("health", Criteria.HEALTH, "§c❤")
            objective.setDisplayName("§c❤")
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME)
        }

        BukkitUtils.onlineNonSpoofPlayers.findAll{it.getWorld() == player.getWorld() }.findAll { it.getLocation().distance(player.getLocation()) < player.getViewDistance() * 16 }.each {
            objective.getScore(it).setScore(it.getHealth().intValue())
        }
    }

}
