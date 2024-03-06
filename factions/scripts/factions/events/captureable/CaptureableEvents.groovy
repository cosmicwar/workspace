package scripts.factions.events.captureable

import com.google.common.collect.Sets
import org.bukkit.Material
import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.GroovyScript
import scripts.shared.core.cfg.Config
import scripts.shared.core.cfg.ConfigCategory
import scripts.shared.core.cfg.utils.DBConfigUtil
import scripts.shared.content.SCBuilder
import scripts.shared.data.string.StringDataManager
import scripts.shared.data.obj.Position
import scripts.shared.data.obj.SR
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder


class CaptureableEvents {
    static Config config
    static ConfigCategory settingsCategory

    static Set<CaptureableEvent> events = Sets.newConcurrentHashSet()

    CaptureableEvents() {
        GroovyScript.addUnloadHook {
            config.queueSave()

            StringDataManager.getByClass(CachedEvent.class).saveAll(false)
        }

        config = DBConfigUtil.createConfig("cap_events", "§eCap Events", [], Material.END_PORTAL_FRAME)
        settingsCategory = config.getOrCreateCategory("settings", "§eSettings", Material.BOOK)

        StringDataManager.register("captureableEvents", CachedEvent.class)

//        createStronghold("arctic", "Arctic Stronghold", "❆ Arctic Stronghold ❆", "#3FDFEC", Material.SNOWBALL)
//        createStronghold("infernal", "Infernal Stronghold", "╓╪╖ Infernal Stronghold ╓╪╖", "#D40B1A", Material.BLAZE_POWDER)
        createEvent("nebula_outpost", "Nebula Outpost", "outpost", "Nebula Outpost", "#474fbf", Material.ENDER_EYE)
        commands()
    }

    static def commands() {
        SCBuilder command = new SCBuilder("outpost").defaultAction {
            openGui(it)
        }

        command.create("wipeconfig").requirePermission("starlight.admin").register {ctx ->
            StringDataManager.wipe(CachedEvent.class)

            settingsCategory.configs.clear()
            config.queueSave()

            ctx.reply("§aEvents have been wiped. Creating default events...")

            createEvent("nebula_outpost", "Nebula Outpost", "outpost", "Nebula Outpost", "#474fbf", Material.ENDER_EYE)

//            createStronghold("arctic", "Arctic Stronghold", "❆ Arctic Stronghold ❆", "#3FDFEC", Material.SNOWBALL)
//            createStronghold("infernal", "Infernal Stronghold", "╓╪╖ Infernal Stronghold ╓╪╖", "#D40B1A", Material.BLAZE_POWDER)

            ctx.reply("§aDefault events have been created.")
        }

        command.create("resetcache").requirePermission("starlight.admin").register {ctx ->
            StringDataManager.wipe(CachedEvent.class)
            config.queueSave()

            ctx.reply("§aCaptureable events cache has been reset. Creating default events...")

//            createStronghold("arctic", "Arctic Stronghold", "❆ Arctic Stronghold ❆", "#3FDFEC", Material.SNOWBALL)
//            createStronghold("infernal", "Infernal Stronghold", "╓╪╖ Infernal Stronghold ╓╪╖", "#D40B1A", Material.BLAZE_POWDER)
            createEvent("nebula_outpost", "Nebula Outpost", "outpost", "Nebula Outpost", "#474fbf", Material.ENDER_EYE)
            ctx.reply("§aDefault captureable events have been created.")
        }

        command.build()
    }

    static def openGui(Player player) {
        MenuBuilder menu

        menu = new MenuBuilder(9, "§aOutposts")

        if (events.size() == 1) {
            def event = events.first()
            menu.set(4, FastItemUtils.createItem(event.getIcon(), event.getInventoryTitle(), event.getInventoryDescription(player)), { p, t, s ->
                if (p.isOp() && event.getLocation().world != null) {
                    p.teleportAsync(event.getLocation().getLocation(null))
                }
            })

            menu.openSync(player)
        }
    }

    static def createEvent(String internalName, String displayName, String eventType, String inventoryTitle, String hexColor = "§c", Material icon, SR globalRegion = new SR(), SR capRegion = new SR(), Position location = new Position()) {
        if (events.any { it.getInternalName() == internalName }) return

        def event = new CaptureableEvent(internalName, displayName, eventType, inventoryTitle, hexColor, icon, globalRegion, capRegion, location)
        events.add(event)

        return event
    }
}
