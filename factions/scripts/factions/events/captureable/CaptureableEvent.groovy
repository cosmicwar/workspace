package scripts.factions.events.captureable

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import scripts.factions.content.dbconfig.RegularConfig
import scripts.factions.content.dbconfig.entries.BooleanEntry
import scripts.factions.content.dbconfig.entries.MaterialEntry
import scripts.factions.content.dbconfig.entries.PositionEntry
import scripts.factions.content.dbconfig.entries.SREntry
import scripts.factions.content.dbconfig.entries.StringEntry
import scripts.factions.content.dbconfig.entries.list.SRListEntry
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.data.DataManager
import scripts.factions.data.obj.Position
import scripts.factions.data.obj.SR
import scripts.shared.utils.BukkitUtils

import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom

@CompileStatic(TypeCheckingMode.SKIP)
class CaptureableEvent {

    String internalName
    CachedEvent cachedEvent
    Task currentTask = null
    RegularConfig config

    CaptureableEvent(String internalName, String displayName, String eventType, String inventoryTitle, String hexColor = "§c", Material icon, SR globalRegion = new SR(), SR capRegion = new SR(), List<SR> placeRegions = [], Position location = new Position()) {
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
                new SRListEntry("place-regions", placeRegions),
                new PositionEntry("location", location)
        ])
        CaptureableEvents.config.queueSave()

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

    List<SR> getPlaceRegions() { return config.getSRListEntry("place-regions").getValue() }

    void setPlaceRegions(List<SR> placeRegions) {
        config.getSRListEntry("place-regions").setValue(placeRegions)
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
