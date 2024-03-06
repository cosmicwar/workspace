package scripts.factions.core.faction.addon.sandbot

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import scripts.shared.content.SCBuilder
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.addon.sandbot.data.SandBot
import scripts.factions.core.faction.addon.upgrade.Upgrades
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.shared.data.obj.CL
import scripts.shared.data.uuid.UUIDDataManager
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MenuDecorator

import java.util.concurrent.TimeUnit

@CompileStatic(TypeCheckingMode.SKIP)
class SandBots {

    SandBots() {
        GroovyScript.addUnloadHook {
            Factions?.fCommand?.subCommands?.removeIf {
                it.aliases.find {
                    it.equalsIgnoreCase("sandbot")
                } != null
            }
            Factions?.fCommand?.build()
        }

        Schedulers.async().runRepeating({
            Factions.factionCache.values().each { faction ->
                if (faction.sandBots.isEmpty()) return

                faction.sandBots.removeAll {
                    if (it.value.location == null || faction != Factions.getFactionAt(CL.of(it.value.location)) || !it.value.spawned) {
                        it.value.despawn()
                        true
                    }

                    false
                }

                faction.sandBots.values().forEach { sandBot ->
                    if (sandBot.spawned) {
                        sandBot.placeSand()
                    }
                }
            }
        }, 500L, TimeUnit.MILLISECONDS, 500L, TimeUnit.MILLISECONDS)


        Upgrades.createUpgrade("sand_bot_radius", "§aSand Bot Radius", [
                "§aIncrease the radius of",
                "§ayour sand bots by 1."
        ], Material.GRASS_BLOCK, 5, "money", 25000D)

        commands()
    }

    static def commands() {
        SCBuilder builder = Factions.fCommand

        builder.create("sandbot", "sandbots", "bot").register { ctx ->
            FCommandUtil.factionMemberFromCommand(ctx) { faction, member ->
                openSandBotMenu(ctx.sender(), faction, member)
            }
        }

        builder.build()
    }

    static def openSandBotMenu(Player player, Faction faction, Member member) {
        MenuBuilder builder

        builder = new MenuBuilder(54, "§3Sand-Bots")

        MenuDecorator.decorate(builder,
                [
                        "33ee7ee33",
                        "3eee7eee3",
                        "3ee777ee3",
                        "3ee777ee3",
                        "3eee7eee3",
                        "33ee7ee33",
                ])

        int startRow = 2
        int startCol = 3

        int row = startRow
        int col = startCol
        for (int i = 1; i < 9; i++) {
            if (i == 5) {
                row = startRow
                col = startCol + 4
            }

            int index = i

            def sandBot = faction.sandBots.get(index)

            ItemStack item
            if (sandBot != null) {
                item = FastItemUtils.createSkull(player, "§3Sand-Bot #${i}", [
                        "§7Status: ${sandBot.spawned ? "§aSpawned" : "§cNot Spawned"}",
                        "§7Position: ${sandBot.location != null ? "§a${sandBot.location.x} ${sandBot.location.y} ${sandBot.location.z}" : "§cNot Spawned"}",
                        "",
                        "§7Click to ${sandBot.spawned ? "De-Spawn" : "Spawn"} this bot."
                ])
            } else {
                item = FastItemUtils.createSkull(player, "§3Sand-Bot #${i}", [
                        "§7Status: §cNot Spawned",
                        "§7Position: §cNot Spawned",
                        "",
                        "§7Click to spawn this bot."
                ])
            }

            builder.set(row, col, item, { p, t, s ->
                if (sandBot) {
                    // despawn
                    sandBot.despawn()
                    faction.sandBots.remove(index)

                    openSandBotMenu(player, faction, member)
                } else {
                    sandBot = new SandBot(faction.id, index)
                    sandBot.spawn(p.location)
                    faction.sandBots.put(index, sandBot)

                    openSandBotMenu(player, faction, member)
                }
            })

            row++
        }

        builder.openSync(player)
    }

}
