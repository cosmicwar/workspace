package scripts.factions.features.enchant.struct

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.Subscription
import org.bukkit.World
import org.bukkit.entity.Player
import scripts.factions.content.dbconfig.entries.IntEntry
import scripts.factions.features.enchant.Enchantments
import scripts.shared.utils.ItemType

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

abstract class TickingEnchant extends CustomEnchantment {

    Closure startEnchant = Exports.ptr("enchantments:startTickingEnchant") as Closure

    Player player = null
    World world = null

    final AtomicInteger currentTick = new AtomicInteger(0)
    final AtomicBoolean finished = new AtomicBoolean(false)

    final private List<Future> pendingFutures = new ArrayList<>()
    final List<Subscription> subscriptions = new ArrayList<>()

    TickingEnchant(String internalName, EnchantmentTier enchantmentTier, String displayName = "", List<String> description = ["empty"], List<ItemType> applicability = [], int maxLevel = 1, boolean stackable = false) {
        super(internalName, enchantmentTier, EnchantmentType.TICKING, displayName, description, applicability, maxLevel, stackable)

        getConfig().addDefault([
            new IntEntry("durationTicks", 20)
        ])

        Enchantments.enchantConfig.queueSave()
    }

    abstract void start(Player player)

    abstract void onTick(Player player, int tick)

    abstract void cleanup(Player player)

    boolean canFinish(int currentTick) {
        return currentTick > getConfig().getIntEntry("durationTicks").value && !isWaiting()
    }

    void tick() {
        int currentTick = currentTick.getAndAdd(1)
        if (currentTick == 0) {
            start(player)
        }

        if (finished.get()) return

        if (!player.isOnline() || player.getWorld() != world || canFinish(currentTick) || currentTick > getConfig().getIntEntry("durationTicks").value + 100) { // after 100 ticks over, just give up
            finish()
            return
        }

        if (currentTick < getConfig().getIntEntry("durationTicks").value) {
            onTick(player, currentTick)
        }
    }

    boolean isFinalTick(int tick) {
        return tick == getConfig().getIntEntry("durationTicks").value - 1
    }

    void subscribe(Subscription subscription) {
        subscriptions.add(subscription)
    }

    void await(Future future) {
        pendingFutures.add(future)
    }

    void runAsync(Runnable runnable) {
        await(CompletableFuture.runAsync(runnable, Schedulers.async()))
    }

    boolean isWaiting() {
        return pendingFutures.any { !it.isDone() }
    }

    void finish() {
        if (finished.getAndSet(true)) return // already finished

        subscriptions.each { it.unregister() }
        pendingFutures.each { it.cancel(false) }
        cleanup(player)

        player = null
        world = null
        currentTick.set(0)
    }

    void startEnchantment(Player player) {
        player.sendMessage("finished: ${finished.get()}")
        player.sendMessage("currentTick: ${currentTick.get()}")
        if (!finished.get() && currentTick.get() == 0) {
            this.player = player
            this.world = player.getWorld()
            startEnchant.call(player, this);
        }
    }
}

