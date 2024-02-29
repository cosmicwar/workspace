package scripts.factions.content.entity.holograms

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

class HologramRegistry {
    private static HologramRegistry registry = new HologramRegistry()

    static HologramRegistry get() {
        return registry
    }

    public boolean initialized = false

    public Map<String, HologramTracker> registered = new ConcurrentHashMap<>()
    public Map<String, HologramPlaceholder> placeholders = new ConcurrentHashMap<>()

    static void main(String[] args) {
        get().init()
    }

    void init() {
        if (initialized) {
            return
        }
        initialized = true

        Schedulers.async().runRepeating({
            for (HologramTracker tracker : registered.values()) {
                tracker.updateNearby()
            }
        }, 1L, TimeUnit.SECONDS, 1L, TimeUnit.SECONDS)

        Events.subscribe(PlayerChangedWorldEvent.class).handler({
            Player player = it.player
            World world = it.getFrom()

            registered.values().findAll { it.location.world == world && it.viewerHolograms.containsKey(player) }.each { it.removeForPlayer(player) }
        })
    }

    HologramTracker spawn(String id, Location location, List<String> lines, boolean dynamic, Set<String> placeholders = null, Predicate<Player> visibilityPredicate = null) {
        HologramTracker previous = registered.get(id)
        if (previous != null) {
            previous.removeForAll()
        }

        HologramTracker tracker = new HologramTracker()
        tracker.init(id, location, lines, dynamic, placeholders, visibilityPredicate)
        registered.put(id, tracker)

        return tracker
    }

    void unregister(HologramTracker tracker) {
        tracker?.removeForAll()
        registered.entrySet().removeIf { it.value.id == tracker?.id }
    }

    def unload() {
        for (HologramTracker tracker : registered.values()) {
            tracker.removeForAll()
        }
        registered.clear()
    }
}