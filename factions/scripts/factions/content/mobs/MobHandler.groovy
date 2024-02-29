package scripts.factions.content.mobs

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.minecraft.world.entity.Mob
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent
import org.starcade.starlight.helper.Commands
import scripts.factions.content.mobs.impl.CaveSpiderMob
import scripts.factions.content.mobs.impl.EndermanMob

@CompileStatic(TypeCheckingMode.SKIP)
class MobHandler {

    static Map<UUID, TickableMob> aliveMobs = [:]

    MobHandler() {
        commands()
    }

    static def commands() {
        Commands.create().assertOp().assertPlayer().handler { ctx ->
            if (ctx.args().size() == 0) {
                ctx.reply("Usage: /mob/spawn <mob>")
                return
            }

            def arg = ctx.arg(0).parseOrFail(String)
            if (arg.equalsIgnoreCase("cavespider")) {
                def level = ((CraftWorld) ctx.sender().world).getHandle()
                spawn(ctx.sender().location, new CaveSpiderMob(level))
            } else if (arg.equalsIgnoreCase("enderman")) {
                def level = ((CraftWorld) ctx.sender().world).getHandle()
                spawn(ctx.sender().location, new EndermanMob(level, "§c§lENDERMAN §7(Tier 2)"))
            } else {
                def level = ((CraftWorld) ctx.sender().world).getHandle()
                spawn(ctx.sender().location, new CaveSpiderMob(level, "§d§lCAVE-SPIDER §7(Tier 1)"))
            }
        }.register("mob/spawn")

        Commands.create().assertOp().assertPlayer().handler { ctx ->
            aliveMobs.values().each { mob ->
                ctx.reply("${mob.getMobHealth()}")
            }
        }.register("mob/debug")

        Commands.create().assertOp().assertPlayer().handler { ctx ->
            aliveMobs.eachWithIndex { entry, index ->
                entry.value?.killMob()
                ctx.reply("${index}: Killed ${entry.value?.getClass()?.getSimpleName()}")
            }

            aliveMobs.clear()
        }.register("mob/killall")
    }

    static def spawn(Location location, Mob mob) {
        if (mob instanceof TickableMob) {
            def tickableMob = (TickableMob) mob
            aliveMobs.put(mob.getUUID(), tickableMob)

            def nmsWorld = (location.getWorld() as CraftWorld).getHandle()
            mob.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch())
            nmsWorld.addEntity(mob, CreatureSpawnEvent.SpawnReason.CUSTOM)

            tickableMob.setCustomName(tickableMob.getMobCustomName(), true)
        }
    }

}
