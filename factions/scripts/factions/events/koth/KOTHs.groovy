package scripts.factions.events.koth

import com.google.common.collect.Sets
import org.bukkit.Material
import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.GroovyScript
import scripts.factions.content.dbconfig.Config
import scripts.factions.content.dbconfig.ConfigCategory
import scripts.factions.content.dbconfig.DBConfigUtil
import scripts.factions.core.faction.FCBuilder
import scripts.factions.data.DataManager
import scripts.factions.data.obj.Position
import scripts.factions.data.obj.SR
import scripts.factions.events.captureable.CachedEvent
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder

class KOTHs {
    static Config config
    static ConfigCategory settingsCategory

    static Set<KOTH> koths = Sets.newConcurrentHashSet()

    KOTHs() {
        GroovyScript.addUnloadHook {
            config.queueSave()

            DataManager.getByClass(CachedEvent.class).saveAll(false)
        }

        config = DBConfigUtil.createConfig("koths", "§eKoth", [], Material.END_PORTAL_FRAME)
        settingsCategory = config.getOrCreateCategory("settings", "§eSettings", Material.BOOK)

        DataManager.register("koth", CachedEvent.class)

        createKoth(15 * 60, "spawn_koth",  "KOTH", "KOTH", "#474fbf", Material.ENDER_EYE)
        commands()
    }

    static def commands() {
        FCBuilder command = new FCBuilder("outpost").defaultAction {
            openGui(it)
        }

        command.create("wipeconfig").requirePermission("starlight.admin").register {ctx ->
            DataManager.wipe(CachedEvent.class)

            settingsCategory.configs.clear()
            config.queueSave()

            ctx.reply("§akoths have been wiped. Creating default koths...")

            createKoth(15 * 60, "spawn_koth",  "KOTH", "KOTH", "#474fbf", Material.ENDER_EYE)


            ctx.reply("§aDefault koths have been created.")
        }

        command.create("resetcache").requirePermission("starlight.admin").register {ctx ->
            DataManager.wipe(CachedEvent.class)
            config.queueSave()

            ctx.reply("§aCaptureable koths cache has been reset. Creating default koths...")

            createKoth(15 * 60, "spawn_koth",  "KOTH", "KOTH", "#474fbf", Material.ENDER_EYE)
            ctx.reply("§aDefault captureable koths have been created.")
        }

        command.build()
    }

    static def openGui(Player player) {
        MenuBuilder menu

        menu = new MenuBuilder(9, "§aOutposts")

        if (koths.size() == 1) {
            def event = koths.first()
            menu.set(4, FastItemUtils.createItem(event.getIcon(), event.getInventoryTitle(), event.getInventoryDescription(player)), { p, t, s ->
                if (p.isOp() && event.getLocation().world != null) {
                    p.teleportAsync(event.getLocation().getLocation(null))
                }
            })

            menu.openSync(player)
        }
    }

    static def createKoth(Integer duration, String internalName, String displayName, String inventoryTitle, String hexColor = "§c", Material icon, SR globalRegion = new SR(), SR capRegion = new SR(), Position location = new Position()) {
        if (koths.any { it.getInternalName() == internalName }) return

        def koth = new KOTH(duration, internalName, displayName, inventoryTitle, hexColor, icon, globalRegion, capRegion, location)
        koths.add(koth)

        return koth
    }
}
