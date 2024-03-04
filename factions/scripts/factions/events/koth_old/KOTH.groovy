package scripts.factions.events.koth_old

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scoreboard.Scoreboard
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import scripts.factions.content.scoreboard.sidebar.Sidebar
import scripts.factions.content.scoreboard.sidebar.SidebarBuilder
import scripts.factions.content.scoreboard.sidebar.SidebarHandler
import scripts.factions.data.obj.SR
import scripts.shared.legacy.command.SubCommandBuilder

@CompileStatic(TypeCheckingMode.SKIP)
class KOTH {

    SR region
    SR broadRegion
    long kothStartTime
    static boolean active = false
    static final int kothDuration = 10

    Player currentPlayerCapping
    long playerCapStartTime
    long timeRemaining

    static Set<Player> scoreboardPlayers = Sets.newConcurrentHashSet()

    static KOTH koth = null

    KOTH() {
        GroovyScript.addUnloadHook {
            SidebarHandler.unregisterSidebar("koth")
        }

        getScoreboard()
        active = false
        listeners()
        commands()
    }

    void startKoth(SR region, SR broadRegion) {
        this.currentPlayerCapping = null
        this.kothStartTime = System.currentTimeSeconds()
        this.region = region
        this.broadRegion = broadRegion
        timeRemaining = kothDuration
        active = true

        Task kothTask
        kothTask = Schedulers.async().runRepeating({
            for (Player p : Bukkit.getOnlinePlayers()) {
                Location l = p.location
                if (broadRegion.contains(l.x, l.y, l.z)) {
                    scoreboardPlayers.add(p)
                    //possibly create scoreboard here too
                }
                else if (scoreboardPlayers.contains(p)) scoreboardPlayers.remove(p)
            }
//            for (Player p : scoreboardPlayers) {
//                p.sendMessage("KOTH: ${currentPlayerCapping != null ? currentPlayerCapping.name : "NONE"} Time remaining: ${timeRemaining.toString()}")//updateScoreboard(p)
//            }

            if (!active) {
                kothTask.stop()
                endKoth()
            }

            timeRemaining--
            if (timeRemaining <= 0) this.active = false
        },0, 20)
    }

    static void listeners() {
        Events.subscribe(PlayerMoveEvent.class).handler { event ->
            Player player = event.player
            if (!active) return
            if (koth.currentPlayerCapping != null) return
            Location playerLoc = event.player.location
            if (!koth.region.contains(playerLoc.x, playerLoc.y, playerLoc.z)) return
            koth.addNewCapper(event.player)
        }

        Events.subscribe(PlayerMoveEvent.class).handler { event ->
            if (!active) return
            if (koth.currentPlayerCapping == null) return
            if (event.player != koth.currentPlayerCapping) return
            Location playerLoc = event.player.location
            if (koth.region.contains(playerLoc.x, playerLoc.y, playerLoc.z)) return
            koth.removeCapper()
        }
    }

    static void commands() {
        SubCommandBuilder builder = new SubCommandBuilder("koth").defaultAction { player ->
        }
        builder.create("create").register { cmd ->
            koth = new KOTH()
            koth.startKoth(new SR(cmd.sender().world.name, 183, 71, 9, 172, 76, -2), new SR(cmd.sender().world.name, 148, 92, -29, 206, 68, 35))
        }
        builder.build()
    }

    void addNewCapper(Player player) {
        this.playerCapStartTime = System.currentTimeSeconds()
        this.timeRemaining = kothDuration
        this.currentPlayerCapping = player
    }

    void removeCapper() {
        this.playerCapStartTime = 0
        this.timeRemaining = kothDuration
        this.currentPlayerCapping = null
    }

    void updateScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard()

        if (timeRemaining <= 0) {
            //display winner scoreboard
        }

    }

    void endKoth() {

    }

    static Sidebar getScoreboard() {
        def board = new SidebarBuilder("koth").lines {
            def list = []
            list.add("§7§l____________________")
//            list.add("")
            list.add("§a§lCapping: §7${koth.currentPlayerCapping ? "§a${koth.currentPlayerCapping.name}" : "§cNone"}")
            list.add("")
            list.add("§a§lTime Remaining: §7${koth.timeRemaining.toString()}")
            list.add("")
            list.add("")
            list.add("§7§l____________________")
            return list
        }.title {
            return "§d§lKOTH §3| §712/25/01"
        }.priority {
            return 3
        }.shouldDisplayTo {player ->
            return active && scoreboardPlayers.contains(player)
        }.build()

        SidebarHandler.registerSidebar(board)

        return board
    }
}
