package scripts.factions.events.koth

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import scripts.factions.content.dbconfig.RegularConfig
import scripts.factions.content.dbconfig.entries.BooleanEntry
import scripts.factions.content.dbconfig.entries.MaterialEntry
import scripts.factions.content.dbconfig.entries.PositionEntry
import scripts.factions.content.dbconfig.entries.SREntry
import scripts.factions.content.dbconfig.entries.StringEntry
import scripts.factions.content.scoreboard.sidebar.Sidebar
import scripts.factions.content.scoreboard.sidebar.SidebarBuilder
import scripts.factions.content.scoreboard.sidebar.SidebarHandler
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.data.DataManager
import scripts.factions.data.obj.Position
import scripts.factions.data.obj.SR
import scripts.factions.events.captureable.CachedEvent
import scripts.factions.events.captureable.CaptureState
import scripts.factions.events.captureable.CaptureableEvents
import scripts.shared.utils.BukkitUtils
import scripts.shared.utils.ColorUtil

import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom

@CompileStatic(TypeCheckingMode.SKIP)
class KOTH {

    String internalName
    CachedKoth cachedEvent
    Task currentTask = null
    RegularConfig config

    KOTH(Integer duration, String internalName, String displayName, String inventoryTitle, String hexColor = "§c", Material icon, SR globalRegion = new SR(), SR capRegion = new SR(), Position location = new Position()) {
        this.internalName = internalName
        this.cachedEvent = DataManager.getData(internalName, CachedKoth.class, true)
        cachedEvent.timeRemaining = duration
        cachedEvent.duration = duration

        config = KOTHs.settingsCategory.getOrCreateConfig(internalName, internalName, icon)
        config.addDefault([
                new BooleanEntry("enabled", false),
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

    Task scheduleTask() {
        def random = ThreadLocalRandom.current()

        return Schedulers.async().runRepeating({
            if (isEnabled()) {
                if (getCapRegion().world) {
                    def world = Bukkit.getWorld(getCapRegion().world)
                    if (world != null) {
                        def players = world.getPlayers().findAll { player ->
                            getCapRegion().contains(player.getLocation())
                        }

                        if (cachedEvent.cappingPlayerId == null) {
                            if (players.size() > 0) {
                                cachedEvent.cappingPlayerId = players[random.nextInt(players.size())].getUniqueId()
                                cachedEvent.attackingFactionId = Factions.getMember(cachedEvent.cappingPlayerId).factionId
                            }
                        } else {
                            if (players.contains(Bukkit.getPlayer(cachedEvent.cappingPlayerId))) {
                                cachedEvent.timeRemaining = cachedEvent.timeRemaining - 1
                            } else {
                                cachedEvent.cappingPlayerId = null
                                cachedEvent.attackingFactionId = null
                                cachedEvent.timeRemaining = cachedEvent.duration
                            }
                        }
                    }
                }
            }
        }, 20L, 20L)
    }

    Sidebar getScoreboard() {
        def board = new SidebarBuilder("koth_${internalName}").lines { player ->
            def member = Factions.getMember(player.getUniqueId())
            if (member == null) {
                return []
            }

            def list = []

            list.add("")
            list.add(ColorUtil.color("§<${getHexColor()}>§lCapturing"))
            if (cachedEvent.cappingPlayerId != null) {
                def faction = Factions.getFaction(cachedEvent.attackingFactionId, false)
                if (faction != null) {
                    def relation = Factions.getRelationType(member, faction)

                    list.add("§f${relation.color + faction.getName()} ${Bukkit.getPlayer(cachedEvent.cappingPlayerId).name}")
                }
            } else {
                list.add("§fNone")
            }

            list.add("")
            list.add(ColorUtil.color("§<${getHexColor()}>§lTime remaining:"))
            list.add("§f${cachedEvent.timeRemaining / 60}m ${cachedEvent.timeRemaining % 60}s")

            list.add("")
            list.add(ColorUtil.color("§<${getHexColor()}>§lAccount"))
            list.add(player.name)

            return list
        }.title {
            def now = LocalDate.ofInstant(Instant.now(), TimeZone.getTimeZone(ZoneId.of("America/New_York")).toZoneId())
            return ColorUtil.color("§<${getHexColor()}>§l${getDisplayName()} §r§7| §<${getHexColor()}>${dtf.format(now)}")
        }.priority {
            return 3
        }.shouldDisplayTo { player ->
            return getGlobalRegion().world && getGlobalRegion().world == player.getWorld().name && getGlobalRegion().contains(player.getLocation())
        }.build()

        return board
    }

    List<String> getInventoryDescription(Player player) {
        def member = Factions.getMember(player.getUniqueId())

        List<String> description = []

        description.add("§7Be the King of the Hill.")

        description.add("")
        description.add(ColorUtil.color("§<${getHexColor()}>This KOTH event can be found in the main spawn warzone§7:"))
        if (getLocation().world && getLocation().x && getLocation().y && getLocation().z) {
            if (player.world.name == getLocation().world) {
                description.add(ColorUtil.color(" §f${getLocation().x}§<${getHexColor()}>x §f${getLocation().y}§<${getHexColor()}>y §f${getLocation().z}§<${getHexColor()}>z [§o${distanceFormat.format(getLocation().getLocation(player.world).distance(player.getLocation()))}§<${getHexColor()}>m]"))
            } else {
                description.add(ColorUtil.color(" §f${getLocation().x}§<${getHexColor()}>x §f${getLocation().y}§<${getHexColor()}>y §f${getLocation().z}§<${getHexColor()}>z"))
            }
        } else {
            description.add("§fN/A")
        }



        description.add("")
        description.add(ColorUtil.color("§<${getHexColor()}>Rewards & Bonuses§7:"))
        description.add(" §f- §7Capture this KOTH to obtain to rewards and bonuses.")

        return description
    }

    String getInventoryTitle() {
        return ColorUtil.color("§<${getHexColor()}>${getTitle()}")
    }

    boolean isEnabled() { return config.getBooleanEntry("enabled").getValue() }

    String getDisplayName() { return config.getStringEntry("display-name").getValue() }

    void setDisplayName(String displayName) {
        config.getStringEntry("display-name").setValue(displayName)
        CaptureableEvents.config.queueSave()
    }

    SR getGlobalRegion() { return config.getSREntry("global-region").getValue() }

    void setGlobalRegion(SR globalRegion) {
        config.getSREntry("global-region").setValue(globalRegion)
        CaptureableEvents.config.queueSave()
    }

    SR getCapRegion() { return config.getSREntry("cap-region").getValue() }

    void setCapRegion(SR capRegion) {
        config.getSREntry("cap-region").setValue(capRegion)
        CaptureableEvents.config.queueSave()
    }

    Material getIcon() { return config.getMaterialEntry("icon").getValue() }

    void setIcon(Material icon) {
        config.getMaterialEntry("icon").setValue(icon)
        CaptureableEvents.config.queueSave()
    }

    Position getLocation() { return config.getPositionEntry("location").getValue() }

    void setLocation(Position location) {
        config.getPositionEntry("location").setValue(location)
        CaptureableEvents.config.queueSave()
    }

    String getHexColor() { return config.getStringEntry("hex-color").getValue() }

    void setHexColor(String hexColor) {
        config.getStringEntry("hex-color").setValue(hexColor)
        CaptureableEvents.config.queueSave()
    }

    String getTitle() { return config.getStringEntry("inventory-title").getValue() }

    void setTitle(String title) {
        config.getStringEntry("inventory-title").setValue(title)
        CaptureableEvents.config.queueSave()
    }

    static DecimalFormat format = new DecimalFormat("#,###.##")
    static DecimalFormat distanceFormat = new DecimalFormat("#,###")
    static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd")
}