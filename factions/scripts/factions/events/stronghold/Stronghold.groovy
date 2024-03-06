package scripts.factions.events.stronghold


import com.google.common.collect.ImmutableList
import com.google.common.collect.Maps
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import org.starcade.starlight.helper.utils.Players
import scripts.shared.core.cfg.RegularConfig
import scripts.shared.core.cfg.entries.*
import scripts.shared.core.cfg.entries.list.SRListEntry
import scripts.factions.content.scoreboard.sidebar.Sidebar
import scripts.factions.content.scoreboard.sidebar.SidebarBuilder
import scripts.factions.content.scoreboard.sidebar.SidebarHandler
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.shared.data.string.StringDataManager
import scripts.shared.data.obj.Position
import scripts.shared.data.obj.SR
import scripts.shared.legacy.utils.PacketUtils
import scripts.shared.utils.BukkitUtils
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.Persistent

import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger

@CompileStatic(TypeCheckingMode.SKIP)
class Stronghold {

    String internalName

    CachedStronghold cachedStronghold

    Task currentTask = null
    RegularConfig config

    Map<Location, CachedBlock> cachedBlocks = Persistent.of("stronghold_blocks", Maps.<Location, CachedBlock> newConcurrentMap()).get()

    Stronghold(String internalName, String displayName, String inventoryTitle, String hexColor = "§c", Material icon, SR globalRegion = new SR(), SR capRegion = new SR(), List<SR> placeRegions = [], Position location = new Position()) {
        this.internalName = internalName

        cachedStronghold = StringDataManager.getData(internalName, CachedStronghold.class, true)

        config = Strongholds.settingsCategory.getOrCreateConfig(internalName, internalName, icon)
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
        Strongholds.config.queueSave()

        if (isEnabled()) {
            enableStronghold()
        }

        events()

        GroovyScript.addUnloadHook {
            currentTask.stop()
            SidebarHandler.unregisterSidebar("sh_${internalName}")

            getCachedStronghold().queueSave()
        }
    }

    def events() {
        Events.subscribe(BlockPlaceEvent.class).handler { event ->
            if (!globalRegion.world) return

            if (!globalRegion.contains(event.block.location)) return
            if (event.player.isOp()) return

            if (event.block.type.isAir() || !event.block.getType().isSolid()) {
                event.setCancelled(true)

                Players.msg(event.getPlayer(), "§cYou cannot place those blocks in this stronghold.")
                return
            }

            if (!placeRegions.isEmpty() && cachedStronghold.controllingFactionId != null) {
                def member = Factions.getMember(event.getPlayer().getUniqueId())
                if (member.factionId == cachedStronghold.controllingFactionId) {
                    def region = placeRegions.find { it.contains(event.block.location) }

                    if (region != null) return
                }
            }

            event.setCancelled(true)
            event.getPlayer().sendMessage("§cYou cannot place blocks in this stronghold.")
        }

        Events.subscribe(BlockBreakEvent.class).handler { event ->
            if (!globalRegion.world) return

            if (!globalRegion.contains(event.block.location)) return
            if (event.player.isOp()) return

            if (!placeRegions.isEmpty() && cachedStronghold.controllingFactionId != null) {
                def member = Factions.getMember(event.getPlayer().getUniqueId())

                def region = placeRegions.find { it.contains(event.block.location) }
                if (region != null) {
                    if (member.factionId != cachedStronghold.controllingFactionId) {
                        event.setCancelled(true)

                        def cachedBlock = cachedBlocks.computeIfAbsent(event.block.location, { new CachedBlock(event.block.location) })
                        cachedBlock.handleBreak(event.getPlayer())

                        if (cachedBlock.isBroken()) cachedBlocks.remove(event.block.location)
                    }

                    return
                }
            }

            event.setCancelled(true)
            event.getPlayer().sendMessage("§cYou cannot break blocks in this stronghold.")
        }
    }

    def enableStronghold() {
        config.getBooleanEntry("enabled").setValue(true)
        Strongholds.config.queueSave()

        SidebarHandler.registerSidebar(getScoreboard())

        if (currentTask != null) {
            currentTask.stop()
            SidebarHandler.unregisterSidebar("sh_${internalName}")
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

                        if (cachedStronghold.controllingFactionId != null) {
                            def faction = Factions.getFaction(cachedStronghold.controllingFactionId, false)
                            if (faction == null) {
                                cachedStronghold.captureState = CaptureState.NEUTRAL
                                cachedStronghold.controllingFactionId = null
                                cachedStronghold.cappedPercent = 0.0D
                            }
                        }

                        if (cachedStronghold.capturingFactionId != null) {
                            def faction = Factions.getFaction(cachedStronghold.capturingFactionId, false)
                            if (faction == null) {
                                cachedStronghold.capturingFactionId = null
                            }
                        }

                        if (cachedStronghold.attackingFactionId != null) {
                            def faction = Factions.getFaction(cachedStronghold.attackingFactionId, false)
                            if (faction == null) {
                                cachedStronghold.attackingFactionId = null
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

                                switch (cachedStronghold.captureState) {
                                    case CaptureState.CAPTURING:
                                        if (entry.key.id == cachedStronghold.capturingFactionId) {
                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                        } else {
                                            cachedStronghold.captureState = CaptureState.ATTACKING
                                            cachedStronghold.attackingFactionId = entry.key.id
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * entry.value
                                        }
                                        break
                                    case CaptureState.CONTROLLED:
                                        if (entry.key.id == cachedStronghold.controllingFactionId) {
                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                        } else {
                                            cachedStronghold.captureState = CaptureState.ATTACKING
                                            cachedStronghold.attackingFactionId = entry.key.id
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * entry.value
                                        }
                                        break
                                    case CaptureState.NEUTRAL:
                                        cachedStronghold.captureState = CaptureState.CAPTURING
                                        cachedStronghold.attackingFactionId = null
                                        cachedStronghold.capturingFactionId = entry.key.id
                                        changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                        break
                                    case CaptureState.ATTACKING:
                                        if (entry.key.id == cachedStronghold.attackingFactionId) {
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * entry.value
                                        } else {
                                            if (entry.key.id == cachedStronghold.controllingFactionId) {
                                                cachedStronghold.captureState = CaptureState.CONTROLLED
                                                cachedStronghold.attackingFactionId = null
                                                cachedStronghold.controllingFactionId = entry.key.id
                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                            } else if (entry.key.id == cachedStronghold.capturingFactionId) {
                                                cachedStronghold.captureState = CaptureState.CAPTURING
                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                            } else {
                                                if (cachedStronghold.attackingFactionId != entry.key.id) cachedStronghold.attackingFactionId = entry.key.id
                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * entry.value
                                            }
                                        }
                                        break
                                    case CaptureState.CONTESTED:
                                        if (entry.key.id == cachedStronghold.capturingFactionId) {
                                            cachedStronghold.attackingFactionId = null
                                            cachedStronghold.controllingFactionId = null

                                            cachedStronghold.captureState = CaptureState.CAPTURING
                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                        } else if (entry.key.id == cachedStronghold.controllingFactionId) {
                                            cachedStronghold.attackingFactionId = null
                                            cachedStronghold.capturingFactionId = null

                                            cachedStronghold.captureState = CaptureState.CONTROLLED
                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * entry.value
                                        } else {
                                            cachedStronghold.captureState = CaptureState.ATTACKING
                                            cachedStronghold.attackingFactionId = entry.key.id
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * entry.value
                                        }
                                        break
                                }

                                if (changePercent != 0.0D) {
                                    cachedStronghold.cappedPercent = cachedStronghold.cappedPercent + changePercent

                                    if (cachedStronghold.cappedPercent > 100.0D) {
                                        cachedStronghold.cappedPercent = 100.0D
                                        if (entry.key.id == cachedStronghold.capturingFactionId && cachedStronghold.controllingFactionId != cachedStronghold.capturingFactionId) {
                                            cachedStronghold.captureState = CaptureState.CONTROLLED
                                            cachedStronghold.controllingFactionId = cachedStronghold.capturingFactionId
                                            cachedStronghold.capturingFactionId = null
                                            BukkitUtils.broadcast("&e${entry.key.getName()} &7has captured &e${getDisplayName()}&7!")
                                        }
                                    } else if (cachedStronghold.cappedPercent < 0.0D) {
                                        cachedStronghold.captureState = CaptureState.NEUTRAL
                                        cachedStronghold.cappedPercent = 0.0D
                                        cachedStronghold.attackingFactionId = null
                                        cachedStronghold.controllingFactionId = null
                                    }
                                }

                                cachedStronghold.queueSave()
                            } else {
                                factionPlayerCount = factionPlayerCount.sort { a, b -> b.value <=> a.value }

                                def topFaction = factionPlayerCount.entrySet()[0]
                                def secondFaction = factionPlayerCount.entrySet()[1]

                                boolean containsControlling = cachedStronghold.controllingFactionId != null && factionPlayerCount.find { it.key.id == cachedStronghold.controllingFactionId } != null
                                boolean containsCapturing = cachedStronghold.capturingFactionId != null && factionPlayerCount.find { it.key.id == cachedStronghold.capturingFactionId } != null

                                switch (cachedStronghold.captureState) {
                                    case CaptureState.CAPTURING:
                                        if (topFaction.key.id == cachedStronghold.capturingFactionId) {
                                            cachedStronghold.attackingFactionId = secondFaction.key.id

                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * topFaction.value
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * secondFaction.value
                                        } else if (secondFaction.key.id == cachedStronghold.capturingFactionId) {
                                            cachedStronghold.attackingFactionId = topFaction.key.id

                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * secondFaction.value
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value
                                        } else {
                                            cachedStronghold.attackingFactionId = topFaction.key.id

                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value
                                        }
                                        break
                                    case CaptureState.CONTROLLED:
                                        if (topFaction.key.id == cachedStronghold.controllingFactionId) {
                                            cachedStronghold.attackingFactionId = secondFaction.key.id

                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * topFaction.value
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * secondFaction.value
                                        } else if (secondFaction.key.id == cachedStronghold.controllingFactionId) {
                                            cachedStronghold.attackingFactionId = topFaction.key.id

                                            changePercent = changePercent + random.nextDouble(0.07, 0.13) * secondFaction.value
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value
                                        } else {
                                            cachedStronghold.attackingFactionId = topFaction.key.id

                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value
                                        }
                                        break
                                    case CaptureState.NEUTRAL:
                                        cachedStronghold.capturingFactionId = topFaction.key.id
                                        cachedStronghold.attackingFactionId = secondFaction.key.id
                                        changePercent = changePercent + random.nextDouble(0.07, 0.13) * topFaction.value
                                        changePercent = changePercent - random.nextDouble(0.07, 0.13) * secondFaction.value
                                        break
                                    case CaptureState.ATTACKING:
                                        if (topFaction.key.id == cachedStronghold.attackingFactionId) {
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value

                                            if (containsControlling) {
                                                def controllingFaction = factionPlayerCount.find { it.key.id == cachedStronghold.controllingFactionId }
                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * controllingFaction.value
                                            }
                                        } else if (secondFaction.key.id == cachedStronghold.attackingFactionId) {
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * secondFaction.value

                                            if (containsControlling) {
                                                def controllingFaction = factionPlayerCount.find { it.key.id == cachedStronghold.controllingFactionId }
                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * controllingFaction.value
                                            }
                                        } else {
                                            cachedStronghold.attackingFactionId = topFaction.key.id
                                            changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value

                                            if (containsControlling) {
                                                def controllingFaction = factionPlayerCount.find { it.key.id == cachedStronghold.controllingFactionId }
                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * controllingFaction.value
                                            }
                                        }
                                        break
                                    case CaptureState.CONTESTED:
                                        if (cachedStronghold.controllingFactionId != null) {
                                            if (topFaction.key.id == cachedStronghold.controllingFactionId) {
                                                cachedStronghold.attackingFactionId = secondFaction.key.id

                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * topFaction.value
                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * secondFaction.value
                                            } else if (secondFaction.key.id == cachedStronghold.controllingFactionId) {
                                                cachedStronghold.attackingFactionId = topFaction.key.id

                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * secondFaction.value
                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value
                                            } else {
                                                cachedStronghold.attackingFactionId = topFaction.key.id

                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value

                                                if (containsControlling) {
                                                    def controllingFaction = factionPlayerCount.find { it.key.id == cachedStronghold.controllingFactionId }
                                                    changePercent = changePercent + random.nextDouble(0.07, 0.13) * controllingFaction.value
                                                }
                                            }
                                        } else if (cachedStronghold.capturingFactionId != null) {
                                            if (topFaction.key.id == cachedStronghold.capturingFactionId) {
                                                cachedStronghold.attackingFactionId = secondFaction.key.id

                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * topFaction.value
                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * secondFaction.value
                                            } else if (secondFaction.key.id == cachedStronghold.capturingFactionId) {
                                                cachedStronghold.attackingFactionId = topFaction.key.id

                                                changePercent = changePercent + random.nextDouble(0.07, 0.13) * secondFaction.value
                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value
                                            } else {
                                                cachedStronghold.attackingFactionId = topFaction.key.id

                                                changePercent = changePercent - random.nextDouble(0.07, 0.13) * topFaction.value

                                                if (containsCapturing) {
                                                    def capturingFaction = factionPlayerCount.find { it.key.id == cachedStronghold.capturingFactionId }
                                                    changePercent = changePercent + random.nextDouble(0.07, 0.13) * capturingFaction.value
                                                }
                                            }
                                        }
                                        break
                                }

                                cachedStronghold.captureState = CaptureState.CONTESTED

                                if (changePercent != 0.0D) {
                                    cachedStronghold.cappedPercent = cachedStronghold.cappedPercent + changePercent

                                    if (cachedStronghold.cappedPercent > 100.0D) {
                                        cachedStronghold.cappedPercent = 100.0D
                                        if (topFaction.key.id == cachedStronghold.capturingFactionId && cachedStronghold.controllingFactionId != cachedStronghold.capturingFactionId) {
                                            cachedStronghold.controllingFactionId = cachedStronghold.capturingFactionId
                                            cachedStronghold.capturingFactionId = null
                                            BukkitUtils.broadcast("&e${topFaction.key.getName()} &7has captured &e${getDisplayName()}&7!")
                                        }
                                    } else if (cachedStronghold.cappedPercent < 0.0D) {
                                        cachedStronghold.captureState = CaptureState.NEUTRAL
                                        cachedStronghold.cappedPercent = 0.0D
                                        cachedStronghold.attackingFactionId = null
                                        cachedStronghold.controllingFactionId = null
                                    }
                                }

                                cachedStronghold.queueSave()
                            }
                        }
                    }
                }
            }
        }, 20L, 20L)
    }

    def disableStronghold() {
        config.getBooleanEntry("enabled").setValue(false)
        Strongholds.config.queueSave()

        SidebarHandler.unregisterSidebar("sh_${internalName}")

        if (currentTask != null) {
            currentTask.stop()
        }
    }

    boolean isEnabled() { return config.getBooleanEntry("enabled").getValue() }
    String getDisplayName() { return config.getStringEntry("display-name").getValue() }

    void setDisplayName(String displayName) {
        config.getStringEntry("display-name").setValue(displayName)
        Strongholds.config.queueSave()
    }

    SR getGlobalRegion() { return config.getSREntry("global-region").getValue() }

    void setGlobalRegion(SR globalRegion) {
        config.getSREntry("global-region").setValue(globalRegion)
        Strongholds.config.queueSave()
    }

    SR getCapRegion() { return config.getSREntry("cap-region").getValue() }

    void setCapRegion(SR capRegion) {
        config.getSREntry("cap-region").setValue(capRegion)
        Strongholds.config.queueSave()
    }

    List<SR> getPlaceRegions() { return config.getSRListEntry("place-regions").getValue() }

    void setPlaceRegions(List<SR> placeRegions) {
        config.getSRListEntry("place-regions").setValue(placeRegions)
        Strongholds.config.queueSave()
    }

    Material getIcon() { return config.getMaterialEntry("icon").getValue() }

    void setIcon(Material icon) {
        config.getMaterialEntry("icon").setValue(icon)
        Strongholds.config.queueSave()
    }

    Position getLocation() { return config.getPositionEntry("location").getValue() }

    void setLocation(Position location) {
        config.getPositionEntry("location").setValue(location)
        Strongholds.config.queueSave()
    }

    String getHexColor() { return config.getStringEntry("hex-color").getValue() }

    void setHexColor(String hexColor) {
        config.getStringEntry("hex-color").setValue(hexColor)
        Strongholds.config.queueSave()
    }

    String getTitle() { return config.getStringEntry("inventory-title").getValue() }

    void setTitle(String title) {
        config.getStringEntry("inventory-title").setValue(title)
        Strongholds.config.queueSave()
    }

    static DecimalFormat format = new DecimalFormat("#,###.##")
    static DecimalFormat distanceFormat = new DecimalFormat("#,###")
    static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd")

    Sidebar getScoreboard() {
        def board = new SidebarBuilder("sh_${internalName}").lines { player ->
            def member = Factions.getMember(player.getUniqueId())
            if (member == null) {
                return []
            }

            def list = []

            list.add("")
            list.add(ColorUtil.color("§<${getHexColor()}>§lAttacking"))
            if (cachedStronghold.attackingFactionId != null) {
                def faction = Factions.getFaction(cachedStronghold.attackingFactionId, false)
                if (faction != null) {
                    def relation = Factions.getRelationType(member, faction)

                    list.add("§f${relation.color + faction.getName()}")
                }
            } else {
                list.add("§fN/A")
            }

            list.add("")
            list.add(ColorUtil.color("§<${getHexColor()}>§l${cachedStronghold.controllingFactionId != null ? "Controlling" : "Capturing"}"))
            if (cachedStronghold.controllingFactionId != null) {
                def faction = Factions.getFaction(cachedStronghold.controllingFactionId, false)
                if (faction != null) {
                    def relation = Factions.getRelationType(member, faction)

                    list.add("§f${relation.color + faction.getName()}")
                }
            } else {
                if (cachedStronghold.capturingFactionId != null) {
                    def faction = Factions.getFaction(cachedStronghold.capturingFactionId, false)
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
            list.add("§f${format.format(cachedStronghold.cappedPercent)}%")

            list.add("")
            list.add(ColorUtil.color("§<${getHexColor()}>§lStatus"))
            list.add("§f${cachedStronghold.captureState.displayName}")

            list.add("")
            list.add(ColorUtil.color("§<${getHexColor()}>§lAccount"))
            list.add(player.name)

            return list
        }.title {
            def now = LocalDate.ofInstant(Instant.now(), TimeZone.getTimeZone(ZoneId.of("America/New_York")).toZoneId())
            return ColorUtil.color("§<${getHexColor()}>${getDisplayName()} §7| §<${getHexColor()}>${dtf.format(now)}")
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

        description.add("§7This is a stronghold.")
        description.add("§7You can capture it.")

        description.add("")
        description.add(ColorUtil.color("§<${getHexColor()}>Stronghold Coords§7:"))
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
        switch (cachedStronghold.captureState) {
            case CaptureState.NEUTRAL:
                description.add(" §f${cachedStronghold.captureState.displayName}")
                break
            case CaptureState.CAPTURING:
                description.add(" §f${cachedStronghold.captureState.displayName} §7[§f${format.format(cachedStronghold.cappedPercent)}%§7]")
                break
            case CaptureState.ATTACKING:
                description.add(" §f${cachedStronghold.captureState.displayName} §7[§f${format.format(cachedStronghold.cappedPercent)}%§7]")
                break
            case CaptureState.CONTROLLED:
                if (cachedStronghold.controllingFactionId != null) {
                    def faction = Factions.getFaction(cachedStronghold.controllingFactionId, false)
                    if (faction != null) {
                        def relation = Factions.getRelationType(member, faction)

                        description.add(" §f${cachedStronghold.captureState.displayName} §7(${relation.color + faction.getName()}§7) §7[§f${format.format(cachedStronghold.cappedPercent)}%§7]")
                    } else {
                        description.add(" §f${cachedStronghold.captureState.displayName}")
                    }
                } else {
                    description.add(" §f${cachedStronghold.captureState.displayName}")
                }

                break
            case CaptureState.CONTESTED:
                description.add(" §f${cachedStronghold.captureState.displayName} §7[§f${format.format(cachedStronghold.cappedPercent)}%§7]")
                break
        }


        description.add("")
        description.add(ColorUtil.color("§<${getHexColor()}>Rewards & Bonuses§7:"))
        description.add(" §f- §7Capture this stronghold to gain access to the rewards and bonuses it provides.")
        description.add(" §f- §7Rewards and bonuses are unique to each stronghold.")

        description.add("")
        description.add("§7Control the §nStronghold§7 to gain access to")
        description.add("§7these unique rewards for §nyou§7 and your §nfaction§7.")

        return description
    }

    String getInventoryTitle() {
        return ColorUtil.color("§<${getHexColor()}>${getTitle()}")
    }

    class CachedBlock {

        int durability
        Block block
        List<Integer> destroyPacketData = ImmutableList.of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1)
        Material m

        boolean broken = false

        CachedBlock(Location location) {
            this.durability = 11
            this.block = location.getBlock()
            this.m = block.getType()
        }

        void handleBreak(Player player) {
            durability--

            if (durability <= 0) {
                block.setType(Material.AIR)
                broken = true
                player.sendMessage("§cThe block has been §l§ndestroyed§c.")
            } else {
                updateBlockPacket(durability)
                player.sendMessage("§cThe current block durability is §e${durability}§c.")
            }
        }

        static boolean isValid(Block block) {
            return block != null && block.getType() != Material.AIR && !block.isLiquid()
        }

        void resetDurability() {
            durability = 15
        }

        void updateBlockPacket(int durability) {
            int data = destroyPacketData.indexOf(durability)
            if (data == -1) {
                return
            }
            AtomicInteger count = new AtomicInteger()
            ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(count.getAndIncrement(), new BlockPos(block.getX(), block.getY(), block.getZ()), data)

            block.location.getNearbyPlayers(30).each {
                PacketUtils.send(it, packet)
            }
        }
    }
}
