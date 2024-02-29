package scripts.factions.events.stronghold

import com.google.common.collect.Sets
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Difficulty
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.utils.Players
import scripts.factions.content.dbconfig.Config
import scripts.factions.content.dbconfig.ConfigCategory
import scripts.factions.content.dbconfig.DBConfigUtil
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.FactionUtils
import scripts.factions.data.DataManager
import scripts.factions.data.obj.SR
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.Persistent
import scripts.shared.utils.gens.VoidWorldGen17

@CompileStatic(TypeCheckingMode.SKIP)
class Strongholds {

    static Config config
    static ConfigCategory settingsCategory

    static Set<Stronghold> strongholds = Sets.newConcurrentHashSet()

    Strongholds() {
        GroovyScript.addUnloadHook {
            config.queueSave()

            DataManager.getByClass(CachedStronghold.class).saveAll(false)
        }

        config = DBConfigUtil.createConfig("strongholds", "§estrongholds", [], Material.END_PORTAL_FRAME)
        settingsCategory = config.getOrCreateCategory("settings", "§eSettings", Material.BOOK)

        DataManager.register("strongholds", CachedStronghold.class)

        createStronghold("arctic", "Arctic Stronghold", "❆ Arctic Stronghold ❆", "#3FDFEC", Material.SNOWBALL)
        createStronghold("infernal", "Infernal Stronghold", "╓╪╖ Infernal Stronghold ╓╪╖", "#D40B1A", Material.BLAZE_POWDER)

        commands()
    }

    static def commands() {
        FCBuilder command = new FCBuilder("stronghold").defaultAction {
            openGui(it)
        }

        command.create("createhub").register {ctx ->
            def world  = new WorldCreator("world-hub").generator(new VoidWorldGen17()).createWorld()

            world.setDifficulty(Difficulty.NORMAL)

            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
            world.setGameRule(GameRule.MOB_GRIEFING, false)
            world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
            world.setGameRule(GameRule.DO_PATROL_SPAWNING, false)
            world.setGameRule(GameRule.DO_INSOMNIA, false)
            world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false)
            world.setGameRule(GameRule.DO_VINES_SPREAD, false)
            world.setGameRule(GameRule.FALL_DAMAGE, true)
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0)
        }

        command.create("wipeconfig").requirePermission("starlight.admin").register {ctx ->
            DataManager.wipe(CachedStronghold.class)

            settingsCategory.configs.clear()
            config.queueSave()

            ctx.reply("§aStrongholds have been wiped. Creating default strongholds...")

            createStronghold("arctic", "Arctic Stronghold", "❆ Arctic Stronghold ❆", "#3FDFEC", Material.SNOWBALL)
            createStronghold("infernal", "Infernal Stronghold", "╓╪╖ Infernal Stronghold ╓╪╖", "#D40B1A", Material.BLAZE_POWDER)

            ctx.reply("§aDefault strongholds have been created.")
        }

        command.create("resetcache").requirePermission("starlight.admin").register {ctx ->
            DataManager.wipe(CachedStronghold.class)
            config.queueSave()

            ctx.reply("§aStrongholds cache has been reset. Creating default strongholds...")

            createStronghold("arctic", "Arctic Stronghold", "❆ Arctic Stronghold ❆", "#3FDFEC", Material.SNOWBALL)
            createStronghold("infernal", "Infernal Stronghold", "╓╪╖ Infernal Stronghold ╓╪╖", "#D40B1A", Material.BLAZE_POWDER)

            ctx.reply("§aDefault strongholds have been created.")
        }

        command.build()
    }

    static def openGui(Player player) {
        MenuBuilder menu

        menu = new MenuBuilder(9, "§aStrongholds")

        if (strongholds.size() == 1) {
            def stronghold = strongholds.first()
            menu.set(4, FastItemUtils.createItem(stronghold.getIcon(), stronghold.getInventoryTitle(), stronghold.getInventoryDescription(player)), {p, t, s ->
                if (p.isOp() && stronghold.getLocation().world != null) {
                    p.teleportAsync(stronghold.getLocation().getLocation(null))
                }
            })
        } else if (strongholds.size() == 2) {
            def stronghold = strongholds.first()
            def second = strongholds.last()

            menu.set(3, FastItemUtils.createItem(stronghold.getIcon(), stronghold.getInventoryTitle(), stronghold.getInventoryDescription(player)), {p, t, s ->
                if (p.isOp() && stronghold.getLocation().world != null) {
                    p.teleportAsync(stronghold.getLocation().getLocation(null))
                }
            })

            menu.set(5, FastItemUtils.createItem(second.getIcon(),  second.getInventoryTitle(), second.getInventoryDescription(player)), {p, t, s ->
                if (p.isOp() && second.getLocation().world != null) {
                    p.teleportAsync(second.getLocation().getLocation(null))
                }
            })
        } else {
            strongholds.each { stronghold ->
                menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(stronghold.getIcon(), stronghold.getInventoryTitle(), stronghold.getInventoryDescription(player)), { p, t, s ->

                })
            }
        }

        menu.openSync(player)
    }

    static def createStronghold(String internalName, String displayName, String inventoryTitle, String hexColor, Material icon = Material.STONE, SR globalRegion = new SR(), SR capRegion = new SR(), List<SR> placeRegions = []) {
        if (strongholds.any { it.getInternalName() == internalName }) return

        def stronghold = new Stronghold(internalName, displayName, inventoryTitle, hexColor, icon, globalRegion, capRegion, placeRegions)
        strongholds.add(stronghold)

        return stronghold
    }

}
