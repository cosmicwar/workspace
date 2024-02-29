package scripts.factions.core.faction.addon.tnt

import groovy.transform.CompileStatic
import org.bukkit.Material
import org.bukkit.block.Dispenser
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.command.context.PlayerContext
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.FactionUtils
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.data.obj.CL
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MenuDecorator

@CompileStatic
class FTnt {

    FTnt() {
        GroovyScript.addUnloadHook {
            Factions?.fCommand?.subCommands?.removeIf { it.aliases.find { it.equalsIgnoreCase("tntfill") } != null }
            Factions?.fCommand?.build()
        }

        commands()
    }

    static def commands() {
        FCBuilder builder = Factions.fCommand

        builder.create("tntfill").register {ctx ->
            FCommandUtil.factionMemberFromCommand(ctx) {faction, member ->
                def pos = CL.of(ctx.sender().getLocation())

                def factionAt = Factions.getFactionAt(pos)

                if (factionAt == null) {
                    ctx.reply("§cYou must be in a territory to fill the TNT storage.")
                    return
                }

                if (factionAt.id != faction.id) {
                    ctx.reply("§cYou must be in your own territory to fill the TNT storage.")
                    return
                }

                def chunk = ctx.sender().chunk

                Schedulers.sync().execute { // TODO: add radius & add checks for new chunks filling?
                    List<Dispenser> dispensers = chunk.getTileEntities()
                            .findAll {it != null && it instanceof Dispenser && it.location.distance(ctx.sender().location) <= 10 }
                            .collect { it as Dispenser }

                    if (dispensers.isEmpty()) {
                        ctx.reply("§cNo dispensers found within 10 blocks.")
                        return
                    }

                    int tntUsed = 0
                    int dispensersFilled = 0

                    dispensers.each {dispenser ->
                        dispenser.getInventory().addItem(new ItemStack(Material.TNT, 64))
                    }

                    ctx.reply("§aFilled dispensers with TNT.")
                }
            }
        }.build()

        builder.build()
    }

    static def openTntGui(Player player, Member member, Faction faction) {
        MenuBuilder builder

        builder = new MenuBuilder(27, "§cTNT Storage")

        MenuDecorator.decorate(builder, [
                "cecececec",
                "ecececece",
                "cecececec",
        ])

        builder.set(2, 5, FastItemUtils.createItem(Material.TNT, "${faction.tntBalance}", [], false), {p, t, s ->
            
        })
    }

}
