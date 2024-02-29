package scripts.factions.features.wildpvp

import org.bukkit.entity.Player
import scripts.factions.features.wildpvp.utils.MatchState

import java.util.concurrent.atomic.AtomicBoolean

class WildPvp {
    public AtomicBoolean active = new AtomicBoolean(true)

    public UUID id
    public Player owner
    public String ownerName
    public long creationTime
    public MatchState matchState
    public Player joiningPlayer

    WildPvp(Player player) {
        this.owner = player
        this.ownerName = player.getName()
        this.id = UUID.randomUUID()
        this.creationTime = System.currentTimeMillis()
        this.matchState = MatchState.WAITING
        this.joiningPlayer = null
    }
}
