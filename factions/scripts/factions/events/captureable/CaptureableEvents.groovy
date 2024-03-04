package scripts.factions.events.captureable

import com.google.common.collect.Sets
import org.bukkit.Difficulty
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.GroovyScript
import scripts.factions.content.dbconfig.Config
import scripts.factions.content.dbconfig.ConfigCategory
import scripts.factions.content.dbconfig.DBConfigUtil
import scripts.factions.core.faction.FCBuilder
import scripts.factions.data.DataManager
import scripts.factions.data.obj.Position
import scripts.factions.data.obj.SR
import scripts.factions.events.stronghold.CachedStronghold
import scripts.factions.events.stronghold.Stronghold
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.gens.VoidWorldGen17

class CaptureableEvents {
    static Config config
    static ConfigCategory settingsCategory

    static Set<CaptureableEvent> events = Sets.newConcurrentHashSet()

    CaptureableEvents() {
        GroovyScript.addUnloadHook {
            config.queueSave()

            DataManager.getByClass(CachedEvent.class).saveAll(false)
        }

        config = DBConfigUtil.createConfig("cap_events", "§eCap Events", [], Material.END_PORTAL_FRAME)
        settingsCategory = config.getOrCreateCategory("settings", "§eSettings", Material.BOOK)

        DataManager.register("captureableEvents", CachedEvent.class)

//        createStronghold("arctic", "Arctic Stronghold", "❆ Arctic Stronghold ❆", "#3FDFEC", Material.SNOWBALL)
//        createStronghold("infernal", "Infernal Stronghold", "╓╪╖ Infernal Stronghold ╓╪╖", "#D40B1A", Material.BLAZE_POWDER)

        commands()
    }

    static def commands() {
        FCBuilder command = new FCBuilder("captureableevents").defaultAction {
//            openGui(it)
        }

        command.create("wipeeventsconfig").requirePermission("starlight.admin").register {ctx ->
            DataManager.wipe(CachedEvent.class)

            settingsCategory.configs.clear()
            config.queueSave()

            ctx.reply("§aEvents have been wiped. Creating default events...")

//            createStronghold("arctic", "Arctic Stronghold", "❆ Arctic Stronghold ❆", "#3FDFEC", Material.SNOWBALL)
//            createStronghold("infernal", "Infernal Stronghold", "╓╪╖ Infernal Stronghold ╓╪╖", "#D40B1A", Material.BLAZE_POWDER)

            ctx.reply("§aDefault events have been created.")
        }

        command.create("resetcache").requirePermission("starlight.admin").register {ctx ->
            DataManager.wipe(CachedEvent.class)
            config.queueSave()

            ctx.reply("§aCaptureable events cache has been reset. Creating default events...")

//            createStronghold("arctic", "Arctic Stronghold", "❆ Arctic Stronghold ❆", "#3FDFEC", Material.SNOWBALL)
//            createStronghold("infernal", "Infernal Stronghold", "╓╪╖ Infernal Stronghold ╓╪╖", "#D40B1A", Material.BLAZE_POWDER)

            ctx.reply("§aDefault captureable events have been created.")
        }

        command.build()
    }

//    static def openGui(Player player) {
//        MenuBuilder menu
//
//        menu = new MenuBuilder(9, "§aStrongholds")
//
//        if (strongholds.size() == 1) {
//            def stronghold = strongholds.first()
//            menu.set(4, FastItemUtils.createItem(stronghold.getIcon(), stronghold.getInventoryTitle(), stronghold.getInventoryDescription(player)), { p, t, s ->
//                if (p.isOp() && stronghold.getLocation().world != null) {
//                    p.teleportAsync(stronghold.getLocation().getLocation(null))
//                }
//            })
//        } else if (strongholds.size() == 2) {
//            def stronghold = strongholds.first()
//            def second = strongholds.last()
//
//            menu.set(3, FastItemUtils.createItem(stronghold.getIcon(), stronghold.getInventoryTitle(), stronghold.getInventoryDescription(player)), {p, t, s ->
//                if (p.isOp() && stronghold.getLocation().world != null) {
//                    p.teleportAsync(stronghold.getLocation().getLocation(null))
//                }
//            })
//
//            menu.set(5, FastItemUtils.createItem(second.getIcon(),  second.getInventoryTitle(), second.getInventoryDescription(player)), {p, t, s ->
//                if (p.isOp() && second.getLocation().world != null) {
//                    p.teleportAsync(second.getLocation().getLocation(null))
//                }
//            })
//        } else {
//            strongholds.each { stronghold ->
//                menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(stronghold.getIcon(), stronghold.getInventoryTitle(), stronghold.getInventoryDescription(player)), { p, t, s ->
//
//                })
//            }
//        }
//
//        menu.openSync(player)
//    }
//
//    static def createStronghold(String internalName, String displayName, String inventoryTitle, String hexColor, Material icon = Material.STONE, SR globalRegion = new SR(), SR capRegion = new SR(), List<SR> placeRegions = []) {
//        if (strongholds.any { it.getInternalName() == internalName }) return
//
//        def stronghold = new Stronghold(internalName, displayName, inventoryTitle, hexColor, icon, globalRegion, capRegion, placeRegions)
//        strongholds.add(stronghold)
//
//        return stronghold
//    }

    static def createEvent(String internalName, String displayName, String eventType, String inventoryTitle, String hexColor = "§c", Material icon, SR globalRegion = new SR(), SR capRegion = new SR(), List<SR> placeRegions = [], Position location = new Position()) {
        if (events.any { it.getInternalName() == internalName }) return

        def event = new CaptureableEvent(internalName, displayName, eventType, inventoryTitle, hexColor, icon, globalRegion, capRegion, placeRegions)
        events.add(event)

        return event
    }
}
