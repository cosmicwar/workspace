package scripts.factions.events.envoy

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import scripts.shared.core.cfg.Config
import scripts.shared.core.cfg.ConfigCategory
import scripts.shared.core.cfg.utils.DBConfigUtil
import scripts.shared.core.cfg.RegularConfig
import scripts.shared.core.cfg.entries.LongEntry
import scripts.shared.core.cfg.entries.SREntry
import scripts.shared.data.obj.SR
import scripts.shared.utils.Persistent

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

@CompileStatic(TypeCheckingMode.SKIP)
class Envoys {

    static Set<Envoy> spawnedEnvoys
    static Set<Envoy> fallingEnvoys
    static AtomicLong activeTimer

    static Task fallingTask = null
    static Task timerTask = null

    static Config config
    static ConfigCategory category
    static RegularConfig regularConfig

    Envoys() {
        config = DBConfigUtil.createConfig("envoys", "§3envoys", [], Material.CHEST)
        category = config.getOrCreateCategory("envoys", "§3envoys", Material.CHEST)
        regularConfig = category.getOrCreateConfig("envoys", "§3envoys", Material.CHEST)
        regularConfig.addDefault([
                new SREntry("spawnableRegion"),
                new LongEntry("delay", TimeUnit.MINUTES.toSeconds(60))
        ])

        config.queueSave()

        activeTimer = Persistent.of("envoys/activeTimer", new AtomicLong(regularConfig.getLongEntry("delay").value)).get()
        spawnedEnvoys = Persistent.of("envoys/spawnedEnvoys", Sets.<Envoy>newConcurrentHashSet()).get()
        fallingEnvoys = Persistent.of("envoys/fallingEnvoys", Sets.<Envoy>newConcurrentHashSet()).get()

        commands()

        timerTask = Schedulers.async().runRepeating({
            if (activeTimer.decrementAndGet() <= 0) {
                activeTimer.set(regularConfig.getLongEntry("delay").value)
                startEnvoy(regularConfig.getSREntry("spawnableRegion").value)
            }
        }, 1L, TimeUnit.SECONDS, 1L, TimeUnit.SECONDS)
    }

    static def startEnvoy(SR region, int count = -1) {
        if (region == null) return

        if (fallingTask != null) {
            fallingTask.stop()
            fallingTask = null
        }

        fallingEnvoys.each {it.killBlock() }
        fallingEnvoys.clear()

        spawnedEnvoys.each {it.killChest() }
        spawnedEnvoys.clear()

        if (count == -1) {
            count = ThreadLocalRandom.current().nextInt(15,35)
        }

        for (int i = 0; i < count; i++) {
            Location location = getRandomLocation(region)
            if (location == null) continue
            if (!location.block.type.isAir()) continue

            Envoy envoy = new Envoy()
            envoy.spawn(location)

            fallingEnvoys.add(envoy)
        }

        if (fallingTask == null) {
            fallingTask = getFallingTask()
        }
    }

    static Location getRandomLocation(SR region) {
        if (region.world == null) return null

        def world = Bukkit.getWorld(region.world)

        def x = ThreadLocalRandom.current().nextInt(region.x1, region.x2)
        def y = 300
        def z = ThreadLocalRandom.current().nextInt(region.z1, region.z2)

        return new Location(world, x, y, z)
    }

    static Task getFallingTask() {
        return Schedulers.async().runRepeating({
            fallingEnvoys.each {
                it.tick()

                if (!it.falling) {
                    spawnedEnvoys.add(it)
                }
            }

            fallingEnvoys.removeAll { !it.falling }
        }, 10L, 10L)
    }

    static def commands() {
        Commands.create().assertOp().assertPlayer().handler {ctx ->

        }.register("dev/envoytest")
    }

}
