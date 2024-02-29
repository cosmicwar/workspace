package scripts

import org.starcade.starlight.helper.Events
import com.vexsoftware.votifier.model.VotifierEvent
import scripts.shared.utils.Exports2
import scripts.shared3.Redis

import java.util.concurrent.TimeUnit

def voted = new HashMap<String, Long>()

Events.subscribe(VotifierEvent).handler { e ->
    println "Received vote from ${e.vote.serviceName} for ${e.vote.username}"
    if (e.vote.serviceName.equalsIgnoreCase("mcsl")) {
        def lastVote = voted.computeIfAbsent(e.vote.username) { 0L }
        def diff = System.currentTimeMillis() - lastVote
        if (diff < TimeUnit.HOURS.toMillis(12)) {
            println "Prevented vote from mcsl, already voted."
            return
        }
        voted[e.vote.username] = System.currentTimeMillis()
    }

    if (e.vote.serviceName.equalsIgnoreCase("Servers-Minecraft")) {
        def lastVote = voted.computeIfAbsent(e.vote.username) { 0L }
        def diff = System.currentTimeMillis() - lastVote
        if (diff < TimeUnit.HOURS.toMillis(12)) {
            println "Prevented vote from servers minecraft, already voted."
            return
        }
        voted[e.vote.username] = System.currentTimeMillis()
    }

    if (Globals.GLOBAL_VOTES) {
        println("global votes")
    }
    (Globals.GLOBAL_VOTES ? Redis.getGlobal() : Redis.get())?.publish("playervote_event", e.vote.username)
    Exports2.invoke("Interactions", "player_vote_event", e.vote.username);
}