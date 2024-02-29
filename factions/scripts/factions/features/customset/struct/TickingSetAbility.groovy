package scripts.factions.features.customset.struct

import groovy.transform.CompileStatic
import org.bukkit.World
import org.bukkit.entity.Player
import org.starcade.starlight.helper.event.Subscription

import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@CompileStatic
abstract class TickingSetAbility {

    final Player player
    final World world

    int durationTicks
    final AtomicInteger currentTick = new AtomicInteger(0)
    final AtomicBoolean finished = new AtomicBoolean(false)

    final private List<Future> pending = new ArrayList<>()
    final List<Subscription> subscriptions = new ArrayList<>()

    TickingSetAbility(Player player, World world) {
        this.player = player
        this.world = world
    }

    abstract void start()

    abstract void onTick(int tick)

    abstract void cleanup()

    boolean canFinish(int currentTick) {
        return currentTick > durationTicks && !isWaiting()
    }

    void tick() {
        int currentTick = currentTick.getAndAdd(1)
        if (currentTick == 0) {
            start()
        }

        if (finished.get()) return

        if (canFinish(currentTick) || player.world != world || currentTick > durationTicks + 100) { // after 100 ticks over, just give up
            finish()
            return
        }

        if (currentTick < durationTicks) {
            onTick(currentTick)
        }
    }

    boolean isFinalTick(int tick) {
        return tick == durationTicks - 1
    }

    void finish() {
        if (finished.getAndSet(true)) return

        cleanup()
        subscriptions.forEach { it.unregister() }
        pending.forEach { it.cancel(true) }
    }

    void subscribe(Subscription subscription) {
        subscriptions.add(subscription)
    }

    void await(Future future) {
        pending.add(future)
    }

    boolean isWaiting() {
        return pending.any { !it.isDone() }
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        TickingSetAbility that = (TickingSetAbility) o
        if (player != that.player) return false
        if (world != that.world) return false

        return true
    }

    @Override
    int hashCode() {
        int result
        result = player.hashCode()
        result = 31 * result + world.hashCode()
        return result
    }

}