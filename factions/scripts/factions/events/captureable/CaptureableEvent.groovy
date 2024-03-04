package scripts.factions.events.captureable

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import org.starcade.starlight.helper.utils.Players
import scripts.factions.content.dbconfig.RegularConfig
import scripts.factions.content.dbconfig.entries.BooleanEntry
import scripts.factions.content.dbconfig.entries.MaterialEntry
import scripts.factions.content.dbconfig.entries.PositionEntry
import scripts.factions.content.dbconfig.entries.SREntry
import scripts.factions.content.dbconfig.entries.StringEntry
import scripts.factions.content.dbconfig.entries.list.SRListEntry
import scripts.factions.content.scoreboard.sidebar.Sidebar
import scripts.factions.content.scoreboard.sidebar.SidebarBuilder
import scripts.factions.content.scoreboard.sidebar.SidebarHandler
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.data.DataManager
import scripts.factions.data.obj.Position
import scripts.factions.data.obj.SR
import scripts.shared.utils.BukkitUtils
import scripts.shared.utils.ColorUtil

import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom

@CompileStatic(TypeCheckingMode.SKIP)
class CaptureableEvent {

    String internalName
    CachedEvent cachedEvent
    Task currentTask = null
    RegularConfig config

    CaptureableEvent(String internalName, String displayName, String eventType, String inventoryTitle, String hexColor = "§c", Material icon, SR globalRegion = new SR(), SR capRegion = new SR(), Position location = new Position()) {
        this.internalName = internalName
        this.cachedEvent = DataManager.getData(internalName, CachedEvent.class, true)

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
            SidebarHandler.unregisterSidebar("event_${internalName}")

            cachedEvent.queueSave()
        }
    }

    def enableEvent() {
        config.getBooleanEntry("enabled").setValue(true)
        CaptureableEvents.config.queueSave()

        SidebarHandler.registerSidebar(getScoreboard())

        if (currentTask != null) {
            currentTask.stop()
            SidebarHandler.unregisterSidebar("event_${internalName}")
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

                        if (cachedEvent.controllingFactionId != null) {
                            def faction = Factions.getFaction(cachedEvent.controllingFactionId, false)
                            if (faction == null) {
                                cachedEvent.captureState = CaptureState.NEUTRAL
                                cachedEvent.controllingFactionId = null
                                cachedEvent.cappedPercent = 0.0D
                            }
                        }

                        if (cachedEvent.capturingFactionId != null) {
                            def faction = Factions.getFaction(cachedEvent.capturingFactionId, false)
                            if (faction == null) {
                                cachedEvent.capturingFactionId = null
                            }
                        }

                        if (cachedEvent.attackingFactionId != null) {
                            def faction = Factions.getFaction(cachedEvent.attackingFactionId, false)
                            if (faction == null) {
                                cachedEvent.attackingFactionId = null
                            }
                        }

                        Map<Faction, Integer> factionPlayerCount = [:]

                        double changePercent = 0.0D

                        players.each {
                            def member = Factions.getMember(it.getUniqueId())
                            if (member != null) {
                                def faction = Factions.getFaction(member.getFactionId(), false)
                                if (faction != null && faction.id != Factions.wildernessId) {
                                    if (factionPlayerCount.containsKey(faction)) {
                                        factionPlayerCount.put(faction, factionPlayerCount.get(faction) + 1)
                                    } else {
                                        factionPlayerCount.put(faction, 1)
                                    }
                                }
                            }
                        }

                        if (factionPlayerCount.size() > 0) {
                            if (factionPlayerCount.size() == 1) {
                                def entry = factionPlayerCount.entrySet()[0]

                                switch (cachedEvent.captureState) {
                                    case CaptureState.CAPTURING:
                                        if (entry.key.id == cachedEvent.capturingFactionId) {
                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                        } else {
                                            cachedEvent.captureState = CaptureState.ATTACKING
                                            cachedEvent.attackingFactionId = entry.key.id
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * entry.value
                                        }
                                        break
                                    case CaptureState.CONTROLLED:
                                        if (entry.key.id == cachedEvent.controllingFactionId) {
                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                        } else {
                                            cachedEvent.captureState = CaptureState.ATTACKING
                                            cachedEvent.attackingFactionId = entry.key.id
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * entry.value
                                        }
                                        break
                                    case CaptureState.NEUTRAL:
                                        cachedEvent.captureState = CaptureState.CAPTURING
                                        cachedEvent.attackingFactionId = null
                                        cachedEvent.capturingFactionId = entry.key.id
                                        changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                        break
                                    case CaptureState.ATTACKING:
                                        if (entry.key.id == cachedEvent.attackingFactionId) {
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * entry.value
                                        } else {
                                            if (entry.key.id == cachedEvent.controllingFactionId) {
                                                cachedEvent.captureState = CaptureState.CONTROLLED
                                                cachedEvent.attackingFactionId = null
                                                cachedEvent.controllingFactionId = entry.key.id
                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                            } else if (entry.key.id == cachedEvent.capturingFactionId) {
                                                cachedEvent.captureState = CaptureState.CAPTURING
                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                            } else {
                                                if (cachedEvent.attackingFactionId != entry.key.id) cachedEvent.attackingFactionId = entry.key.id
                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * entry.value
                                            }
                                        }
                                        break
                                    case CaptureState.CONTESTED:
                                        if (entry.key.id == cachedEvent.capturingFactionId) {
                                            cachedEvent.attackingFactionId = null
                                            cachedEvent.controllingFactionId = null

                                            cachedEvent.captureState = CaptureState.CAPTURING
                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                        } else if (entry.key.id == cachedEvent.controllingFactionId) {
                                            cachedEvent.attackingFactionId = null
                                            cachedEvent.capturingFactionId = null

                                            cachedEvent.captureState = CaptureState.CONTROLLED
                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                        } else {
                                            cachedEvent.captureState = CaptureState.ATTACKING
                                            cachedEvent.attackingFactionId = entry.key.id
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * entry.value
                                        }
                                        break
                                }

                                if (changePercent != 0.0D) {
                                    cachedEvent.cappedPercent = cachedEvent.cappedPercent + changePercent

                                    if (cachedEvent.cappedPercent > 100.0D) {
                                        cachedEvent.cappedPercent = 100.0D
                                        if (entry.key.id == cachedEvent.capturingFactionId && cachedEvent.controllingFactionId != cachedEvent.capturingFactionId) {
                                            cachedEvent.captureState = CaptureState.CONTROLLED
                                            cachedEvent.controllingFactionId = cachedEvent.capturingFactionId
                                            cachedEvent.capturingFactionId = null
                                            BukkitUtils.broadcast("&e${entry.key.getName()} &7has captured &e${getDisplayName()}&7!")
                                        }
                                    } else if (cachedEvent.cappedPercent < 0.0D) {
                                        cachedEvent.captureState = CaptureState.NEUTRAL
                                        cachedEvent.cappedPercent = 0.0D
                                        cachedEvent.attackingFactionId = null
                                        cachedEvent.controllingFactionId = null
                                    }
                                }

                                cachedEvent.queueSave()
                            } else {
                                factionPlayerCount = factionPlayerCount.sort { a, b -> b.value <=> a.value }

                                def topFaction = factionPlayerCount.entrySet()[0]
                                def secondFaction = factionPlayerCount.entrySet()[1]

                                boolean containsControlling = cachedEvent.controllingFactionId != null && factionPlayerCount.find { it.key.id == cachedEvent.controllingFactionId } != null
                                boolean containsCapturing = cachedEvent.capturingFactionId != null && factionPlayerCount.find { it.key.id == cachedEvent.capturingFactionId } != null

                                switch (cachedEvent.captureState) {
                                    case CaptureState.CAPTURING:
                                        if (topFaction.key.id == cachedEvent.capturingFactionId) {
                                            cachedEvent.attackingFactionId = secondFaction.key.id

                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * topFaction.value
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * secondFaction.value
                                        } else if (secondFaction.key.id == cachedEvent.capturingFactionId) {
                                            cachedEvent.attackingFactionId = topFaction.key.id

                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * secondFaction.value
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value
                                        } else {
                                            cachedEvent.attackingFactionId = topFaction.key.id

                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value
                                        }
                                        break
                                    case CaptureState.CONTROLLED:
                                        if (topFaction.key.id == cachedEvent.controllingFactionId) {
                                            cachedEvent.attackingFactionId = secondFaction.key.id

                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * topFaction.value
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * secondFaction.value
                                        } else if (secondFaction.key.id == cachedEvent.controllingFactionId) {
                                            cachedEvent.attackingFactionId = topFaction.key.id

                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * secondFaction.value
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value
                                        } else {
                                            cachedEvent.attackingFactionId = topFaction.key.id

                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value
                                        }
                                        break
                                    case CaptureState.NEUTRAL:
                                        cachedEvent.capturingFactionId = topFaction.key.id
                                        cachedEvent.attackingFactionId = secondFaction.key.id
                                        changePercent = changePercent + random.nextDouble(0.07, 0.13) * topFaction.value
                                        changePercent = changePercent - random.nextDouble(0.07, 0.13) * secondFaction.value
                                        break
                                    case CaptureState.ATTACKING:
                                        if (topFaction.key.id == cachedEvent.attackingFactionId) {
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value

                                            if (containsControlling) {
                                                def controllingFaction = factionPlayerCount.find { it.key.id == cachedEvent.controllingFactionId }
                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * controllingFaction.value
                                            }
                                        } else if (secondFaction.key.id == cachedEvent.attackingFactionId) {
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * secondFaction.value

                                            if (containsControlling) {
                                                def controllingFaction = factionPlayerCount.find { it.key.id == cachedEvent.controllingFactionId }
                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * controllingFaction.value
                                            }
                                        } else {
                                            cachedEvent.attackingFactionId = topFaction.key.id
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value

                                            if (containsControlling) {
                                                def controllingFaction = factionPlayerCount.find { it.key.id == cachedEvent.controllingFactionId }
                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * controllingFaction.value
                                            }
                                        }
                                        break
                                    case CaptureState.CONTESTED:
                                        if (cachedEvent.controllingFactionId != null) {
                                            if (topFaction.key.id == cachedEvent.controllingFactionId) {
                                                cachedEvent.attackingFactionId = secondFaction.key.id

                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * topFaction.value
                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * secondFaction.value
                                            } else if (secondFaction.key.id == cachedEvent.controllingFactionId) {
                                                cachedEvent.attackingFactionId = topFaction.key.id

                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * secondFaction.value
                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value
                                            } else {
                                                cachedEvent.attackingFactionId = topFaction.key.id

                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value

                                                if (containsControlling) {
                                                    def controllingFaction = factionPlayerCount.find { it.key.id == cachedEvent.controllingFactionId }
                                                    changePercent = changePercent + random.nextDouble(0.07, 0.13) * controllingFaction.value
                                                }
                                            }
                                        } else if (cachedEvent.capturingFactionId != null) {
                                            if (topFaction.key.id == cachedEvent.capturingFactionId) {
                                                cachedEvent.attackingFactionId = secondFaction.key.id

                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * topFaction.value
                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * secondFaction.value
                                            } else if (secondFaction.key.id == cachedEvent.capturingFactionId) {
                                                cachedEvent.attackingFactionId = topFaction.key.id

                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * secondFaction.value
                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value
                                            } else {
                                                cachedEvent.attackingFactionId = topFaction.key.id

                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value

                                                if (containsCapturing) {
                                                    def capturingFaction = factionPlayerCount.find { it.key.id == cachedEvent.capturingFactionId }
                                                    changePercent = changePercent + random.nextDouble(0.07, 0.13) * capturingFaction.value
                                                }
                                            }
                                        }
                                        break
                                }

                                cachedEvent.captureState = CaptureState.CONTESTED

                                if (changePercent != 0.0D) {
                                    cachedEvent.cappedPercent = cachedEvent.cappedPercent + changePercent

                                    if (cachedEvent.cappedPercent > 100.0D) {
                                        cachedEvent.cappedPercent = 100.0D
                                        if (topFaction.key.id == cachedEvent.capturingFactionId && cachedEvent.controllingFactionId != cachedEvent.capturingFactionId) {
                                            cachedEvent.controllingFactionId = cachedEvent.capturingFactionId
                                            cachedEvent.capturingFactionId = null
                                            BukkitUtils.broadcast("&e${topFaction.key.getName()} &7has captured &e${getDisplayName()}&7!")
                                        }
                                    } else if (cachedEvent.cappedPercent < 0.0D) {
                                        cachedEvent.captureState = CaptureState.NEUTRAL
                                        cachedEvent.cappedPercent = 0.0D
                                        cachedEvent.attackingFactionId = null
                                        cachedEvent.controllingFactionId = null
                                    }
                                }

                                cachedEvent.queueSave()
                            }
                        }
                    }
                }
            }
        }, 20L, 20L)
    }

    Sidebar getScoreboard() {
        def board = new SidebarBuilder("event_${internalName}").lines { player ->
            def member = Factions.getMember(player.getUniqueId())
            if (member == null) {
                return []
            }

            def list = []

            list.add("")
            list.add(ColorUtil.color("§<${getHexColor()}>§lAttacking"))
            if (cachedEvent.attackingFactionId != null) {
                def faction = Factions.getFaction(cachedEvent.attackingFactionId, false)
                if (faction != null) {
                    def relation = Factions.getRelationType(member, faction)

                    list.add("§f${relation.color + faction.getName()}")
                }
            } else {
                list.add("§fN/A")
            }

            list.add("")
            list.add(ColorUtil.color("§<${getHexColor()}>§l${cachedEvent.controllingFactionId != null ? "Controlling" : "Capturing"}"))
            if (cachedEvent.controllingFactionId != null) {
                def faction = Factions.getFaction(cachedEvent.controllingFactionId, false)
                if (faction != null) {
                    def relation = Factions.getRelationType(member, faction)

                    list.add("§f${relation.color + faction.getName()}")
                }
            } else {
                if (cachedEvent.capturingFactionId != null) {
                    def faction = Factions.getFaction(cachedEvent.capturingFactionId, false)
                    if (faction != null) {
                        def relation = Factions.getRelationType(member, faction)

                        list.add("§f${relation.color + faction.getName()}")
                    }
                } else {
                    list.add("§fN/A")
                }
            }

            list.add("")
            list.add(ColorUtil.color("§<${getHexColor()}>§lCap %"))
            list.add("§f${format.format(cachedEvent.cappedPercent)}%")

            list.add("")
            list.add(ColorUtil.color("§<${getHexColor()}>§lStatus"))
            list.add("§f${cachedEvent.captureState.displayName}")

            list.add("")
            list.add(ColorUtil.color("§<${getHexColor()}>§lAccount"))
            list.add(player.name)

            return list
        }.title {
            def now = LocalDate.ofInstant(Instant.now(), TimeZone.getTimeZone(ZoneId.of("America/New_York")).toZoneId())
            return ColorUtil.color("§l§<${getHexColor()}>${getDisplayName()} §r§7| §<${getHexColor()}>${dtf.format(now)}")
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

        description.add("§7This is an outpost.")
        description.add("§7You can capture it.")

        description.add("")
        description.add(ColorUtil.color("§<${getHexColor()}>Find the outpost in Darkzone§7:"))
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
        description.add(ColorUtil.color("§<${getHexColor()}>Current Info§7:"))
        switch (cachedEvent.captureState) {
            case CaptureState.NEUTRAL:
                description.add(" §f${cachedEvent.captureState.displayName}")
                break
            case CaptureState.CAPTURING:
                description.add(" §f${cachedEvent.captureState.displayName} §7[§f${format.format(cachedEvent.cappedPercent)}%§7]")
                break
            case CaptureState.ATTACKING:
                description.add(" §f${cachedEvent.captureState.displayName} §7[§f${format.format(cachedEvent.cappedPercent)}%§7]")
                break
            case CaptureState.CONTROLLED:
                if (cachedEvent.controllingFactionId != null) {
                    def faction = Factions.getFaction(cachedEvent.controllingFactionId, false)
                    if (faction != null) {
                        def relation = Factions.getRelationType(member, faction)

                        description.add(" §f${cachedEvent.captureState.displayName} §7(${relation.color + faction.getName()}§7) §7[§f${format.format(cachedEvent.cappedPercent)}%§7]")
                    } else {
                        description.add(" §f${cachedEvent.captureState.displayName}")
                    }
                } else {
                    description.add(" §f${cachedEvent.captureState.displayName}")
                }

                break
            case CaptureState.CONTESTED:
                description.add(" §f${cachedEvent.captureState.displayName} §7[§f${format.format(cachedEvent.cappedPercent)}%§7]")
                break
        }


        description.add("")
        description.add(ColorUtil.color("§<${getHexColor()}>Rewards & Bonuses§7:"))
        description.add(" §f- §7Capture this outpost to gain access to the rewards and bonuses it provides.")
//        description.add(" §f- §7Rewards and bonuses are unique to each outpost.")

        description.add("")
        description.add("§7Control the §nOutpost§7 to gain access to")
        description.add("§7these unique rewards for §nyou§7 and your §nfaction§7.")

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
