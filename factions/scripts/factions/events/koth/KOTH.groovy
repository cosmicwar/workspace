package scripts.factions.events.koth

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.scheduler.Task
import scripts.factions.content.dbconfig.RegularConfig
import scripts.factions.content.dbconfig.entries.BooleanEntry
import scripts.factions.content.dbconfig.entries.MaterialEntry
import scripts.factions.content.dbconfig.entries.PositionEntry
import scripts.factions.content.dbconfig.entries.SREntry
import scripts.factions.content.dbconfig.entries.StringEntry
import scripts.factions.content.scoreboard.sidebar.SidebarHandler
import scripts.factions.data.DataManager
import scripts.factions.data.obj.Position
import scripts.factions.data.obj.SR
import scripts.factions.events.captureable.CachedEvent
import scripts.factions.events.captureable.CaptureableEvents

@CompileStatic(TypeCheckingMode.SKIP)
class KOTH {

    String internalName
    CachedKoth cachedEvent
    Task currentTask = null
    RegularConfig config

    KOTH(String internalName, String displayName, String inventoryTitle, String hexColor = "§c", Material icon, SR globalRegion = new SR(), SR capRegion = new SR(), Position location = new Position()) {
        this.internalName = internalName
        this.cachedEvent = DataManager.getData(internalName, CachedKoth.class, true)

        config = CaptureableEvents.settingsCategory.getOrCreateConfig(internalName, internalName, icon)
        config.addDefault([
                new BooleanEntry("enabled", true),
                new StringEntry("display-name", displayName),
                new StringEntry("inventory-title", inventoryTitle),
                new StringEntry("hex-color", hexColor),
                new MaterialEntry("icon", icon),
                new SREntry("global-region", globalRegion),
                new SREntry("cap-region", capRegion),
                new PositionEntry("location", location)
        ])
        CaptureableEvents.config.queueSave()

        if (isEnabled()) {
            enableEvent()
        }

        GroovyScript.addUnloadHook {
            currentTask.stop()
            SidebarHandler.unregisterSidebar("koth_${internalName}")

            cachedEvent.queueSave()
        }
    }

    def enableEvent() {
        config.getBooleanEntry("enabled").setValue(true)
        CaptureableEvents.config.queueSave()

        SidebarHandler.registerSidebar(getScoreboard())

        if (currentTask != null) {
            currentTask.stop()
            SidebarHandler.unregisterSidebar("koth_${internalName}")
        }

        currentTask = scheduleTask()
    }
}