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
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder

class KOTHs {
    static Config config
    static ConfigCategory settingsCategory

    static Set<KOTH> koths = Sets.newConcurrentHashSet()

    KOTHs() {
        GroovyScript.addUnloadHook {
            config.queueSave()

            DataManager.getByClass(CachedKoth.class).saveAll(false)
        }

        config = DBConfigUtil.createConfig("koths", "§eKoth", [], Material.END_PORTAL_FRAME)
        settingsCategory = config.getOrCreateCategory("settings", "§eSettings", Material.BOOK)

        DataManager.register("koth", CachedKoth.class)

        int dur = 15 * 60
        createKoth(15, "spawn_koth",  "Spawn KOTH", "Spawn KOTH", "#32a89e", Material.ENCHANTING_TABLE)
        commands()
    }

    static def commands() {
        FCBuilder command = new FCBuilder("koth").defaultAction {
            openGui(it)
        }

        command.create("start").requirePermission("starlight.admin").register {ctx ->
            koths[0].enableEvent()
        }

        command.create("wipeconfig").requirePermission("starlight.admin").register {ctx ->
            DataManager.wipe(CachedKoth.class)

            settingsCategory.configs.clear()
            config.queueSave()

            ctx.reply("§akoths have been wiped. Creating default koths...")

            createKoth(15 , "spawn_koth",  "KOTH", "KOTH", "#474fbf", Material.ENDER_EYE)


            ctx.reply("§aDefault koths have been created.")
        }

        command.create("resetcache").requirePermission("starlight.admin").register {ctx ->
            DataManager.wipe(CachedKoth.class)
            koths.clear()
            config.queueSave()

            ctx.reply("§aCaptureable koths cache has been reset. Creating default koths...")

            createKoth(15 * 60, "spawn_koth",  "KOTH", "KOTH", "#474fbf", Material.ENDER_EYE)
            ctx.reply("§aDefault captureable koths have been created.")
        }

        command.build()
    }

    static def openGui(Player player) {
        MenuBuilder menu

        menu = new MenuBuilder(9, "§aKOTH")

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

    static def createKoth(Integer duration, String internalName, String displayName, String inventoryTitle, String hexColor, Material icon, SR globalRegion = new SR(), SR capRegion = new SR(), Position location = new Position()) {
        if (koths.any { it.getInternalName() == internalName }) return

        def koth = new KOTH(duration, internalName, displayName, inventoryTitle, hexColor, icon, globalRegion, capRegion, location)
        koths.add(koth)

        return koth
    }
}
