package scripts.factions.core.faction.addon.shield

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.entity.Creeper
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.entity.EntityExplodeEvent
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.addon.shield.data.RaidData
import scripts.factions.core.faction.data.Faction
import scripts.factions.data.obj.CL
import scripts.factions.data.uuid.UUIDDataManager
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.TimeUtils
import scripts.shared.systems.MenuBuilder

@CompileStatic(TypeCheckingMode.SKIP)
class Shields {

    Shields() {
        GroovyScript.addUnloadHook {
            Factions?.fCommand?.subCommands?.removeIf {
                it.aliases.find {
                    it.equalsIgnoreCase("shield") || it.equalsIgnoreCase("isshielded")
                } != null
            }
            Factions?.fCommand?.build()
        }

        UUIDDataManager.register("raiddata", RaidData)

        TimeUtils.setTimeZone("EST")

        commands()
        events()
    }

    static def events() {
        Events.subscribe(EntityExplodeEvent.class).handler {event ->
            def entity = event.getEntity()

            if (entity instanceof TNTPrimed) {
                def explosionLoc = event.getLocation()
                CL location = CL.of(explosionLoc)

                def factionBreached = Factions.getFactionAt(location)
                if (factionBreached == null) return

                def connecting = Factions.getConnectingClaims(Factions.getClaimAt(location))
                boolean isCoreChunk = connecting.find {it.isCoreChunk()}

                if (factionBreached.isShielded() && isCoreChunk) {
                    event.setCancelled(true)
                }

                def sourceLoc = entity.getSourceLoc()
                def sourceCl = CL.of(sourceLoc)

                def sourceFaction = Factions.getFactionAt(sourceCl)


            } else if (entity instanceof Creeper) {
                def explosionLoc = event.getLocation()
                CL location = CL.of(explosionLoc)

                def factionBreached = Factions.getFactionAt(location)
                if (factionBreached == null) return

                def connecting = Factions.getConnectingClaims(Factions.getClaimAt(location))

                boolean isCoreChunk = connecting.find {it.isCoreChunk()}

                if (factionBreached.isShielded() && isCoreChunk) {
                    event.setCancelled(true)
                }
            }
        }
    }


    static def commands() {
        FCBuilder builder = Factions.fCommand

        builder.create("shield", "shields").register { ctx ->
            FCommandUtil.factionMemberFromCommand(ctx) { faction, member ->
                openShieldMenu(ctx.sender(), faction)
            }
        }

        builder.create("isshielded").register { ctx ->
            FCommandUtil.factionMemberFromCommand(ctx) { faction, member ->
                ctx.reply(faction.shieldData.isShielded(TimeUtils.getTimeHours()) ? "§aYour faction is currently shielded." : "§cYour faction is not currently shielded.")
            }
        }

        builder.build()
    }

    static def openShieldMenu(Player player, Faction faction) {
        MenuBuilder menu

        def data = faction.shieldData

        menu = new MenuBuilder(45, "§aFaction Shields")

        for (int i = 1; i < 25; i++) {
            def index = i

            def material = Material.RED_STAINED_GLASS_PANE

            boolean pm = i > 12 || i == 12

            if (i == 24) pm = false

            List<String> lore = [
                    "§a${i > 12 ? i - 12 : i} ${pm ? "PM" : "AM"}",
            ]

            if (data.startHours && data.startHours == i) {
                material = Material.LIME_STAINED_GLASS_PANE

                lore.add("")
                lore.add("§aStart Hour")
                lore.add("")
                lore.add("§a§lSHIELDED")
            } else if (data.endHours && data.endHours == i) {
                material = Material.YELLOW_STAINED_GLASS_PANE

                lore.add("")
                lore.add("§cEnd Hour")
                lore.add("")
                lore.add("§a§lSHIELDED")
            } else if (data.startHours && data.endHours && data.startHours < i && data.endHours > i) {
                material = Material.GREEN_STAINED_GLASS_PANE

                lore.add("")
                lore.add("§a§lSHIELDED")
            } else if (data.startHours && data.endHours) {
                if (data.endHours < data.startHours) {
                    if (i > data.startHours || i < data.endHours) {
                        material = Material.GREEN_STAINED_GLASS_PANE

                        lore.add("")
                        lore.add("§a§lSHIELDED")
                    }
                }
            } else {
                lore.add("")
                lore.add("§c§lDISABLED")
            }

            menu.set(i - 1, FastItemUtils.createItem(material, "§a${i > 12 ? i - 12 : i} ${pm ? "PM" : "AM"}", lore, false), { p, t, s ->

                MenuUtils.createConfirmMenu(p, "§aAre you sure you want to set the start hour to ${index > 12 ? index - 12 : index} ${pm ? "PM" : "AM"}?", FastItemUtils.createItem(Material.CLOCK, "", []), {
                    faction.shieldData.startHours = index
                    faction.shieldData.endHours = index + 12

                    if (data.endHours > 24) {
                        faction.shieldData.endHours -= 24
                    }

                    faction.queueSave()
                    openShieldMenu(p, faction)
                }, {
                    openShieldMenu(p, faction)
                })

            })
        }

        menu.openSync(player)
    }

}
