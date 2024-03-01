package scripts.factions.core.faction.cmd

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.GroovyScript
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.FactionUtils
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.random.WarpData
import scripts.factions.data.obj.CL
import scripts.factions.data.obj.Position
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder


@CompileStatic(TypeCheckingMode.SKIP)
class FWarp {

    FWarp() {
        GroovyScript.addUnloadHook {
            Factions?.fCommand?.subCommands?.removeIf { it.aliases.find {
                it.equalsIgnoreCase("warp") || it.equalsIgnoreCase("setwarp")
            } != null }
            Factions?.fCommand?.build()
        }

        commands()
    }

    static def commands() {
        FCBuilder fCommand = Factions.fCommand

        // f warp
        fCommand.create("warp").usage("[target]").description("Teleport to a faction warp.").register { cmd ->
            FCommandUtil.memberFromCommand(cmd) { member ->
                if (cmd.args().size() == 0) {
                    // show that member
                    showFWarps(member, Factions.getFaction(member.getFactionId(), false))
                } else {
                    // show target faction
                    def targetWarp = findWarp(Factions.getFaction(member.getFactionId()), cmd.arg(0).parseOrFail(String))

                    if (targetWarp == null) {
                        cmd.reply("§3A faction warp with the name §e${cmd.arg(0).parseOrFail(String)}§3 does not exist.")
                        return
                    }
                    Player player = Bukkit.getPlayer(member.id)
                    FactionUtils.teleportPlayer(player, targetWarp.position.getLocation(player.world), false, "§3Teleported to warp §e§n" + targetWarp.name + "§3.")
                }
            }
        }

        fCommand.create("setwarp").usage("[target]").description("Set a faction warp.").register { cmd ->
            FCommandUtil.memberFromCommand(cmd) { member ->
                if (cmd.args().size() == 0) {
                    cmd.sender().sendMessage("§cYou must set a warp with a valid name.")
                } else {
                    Faction faction = Factions.getFaction(member.getFactionId())
                    if (faction.warps.size() > faction.maxFactionWarps) {
                        cmd.sender().sendMessage("§cYour faction already has the max amount of warps set. Purchase more through faction upgrades.")
                        return
                    }
                    String name = cmd.arg(0).parseOrFail(String)
                    cmd.sender().sendMessage("§eYou have set a faction warp §l${name}")
                    Location location = cmd.sender().location
                    def warp = new WarpData(name, new Position(location.world.name, location.blockX, location.blockY, location.blockZ, location.yaw, location.pitch), Material.GRASS_BLOCK)
                    faction.warps.add(warp)
                }
            }
        }

        fCommand.create("delwarp").usage("[target]").description("Delete a faction warp.").register { cmd ->
            FCommandUtil.memberFromCommand(cmd) { member ->
                if (cmd.args().size() == 0) {
                    cmd.sender().sendMessage("§cYou must enter a valid warp name.")
                } else {
                    Faction faction = Factions.getFaction(member.getFactionId())
                    String name = cmd.arg(0).parseOrFail(String)
                    if (findWarp(faction, name) == null) {
                        cmd.sender().sendMessage("§cPlease enter the name of a valid faction warp.")
                        return
                    }
                    cmd.sender().sendMessage("§cYou have deleted a faction warp §l§e${name}")
                    removeWarp(name, faction)
                }
            }
        }

        fCommand.create("sethome").description("Set the faction home.").register { cmd ->
            FCommandUtil.memberFromCommand(cmd) { member ->
                Faction faction = Factions.getFaction(member.getFactionId())


                if (Factions.getClaimAt(CL.of(cmd.sender().location)) == null || Factions.getClaimAt(CL.of(cmd.sender().location)).factionId != faction.id) {
                    cmd.sender().sendMessage("§3You must set your faction home within your own faction's territory.")
                    return
                }
                faction.fHome =  Position.of(cmd.sender().location)

                faction.msg("§3${cmd.sender().name} §r§3has set the faction home to ${faction.fHome.x}, ${faction.fHome.z}")
                faction.queueSave()
                //TODO send message to all fac members
            }
        }

        fCommand.create("home").description("Teleport to the faction home.").register { cmd ->
            FCommandUtil.memberFromCommand(cmd) { member ->
                Faction faction = Factions.getFaction(member.getFactionId())
                if (faction.fHome == null || Factions.getClaimAt(CL.of(faction.fHome.getLocation(Bukkit.getWorld(faction.fHome.world)))) == null || Factions.getClaimAt(CL.of(faction.fHome.getLocation(Bukkit.getWorld(faction.fHome.world)))).factionId != faction.id) faction.fHome = null
                if (faction.fHome == null) {
                    cmd.sender().sendMessage("§3Your faction does not their faction home set!")
                } else {
                    FactionUtils.teleportPlayer(cmd.sender(), faction.fHome.getLocation(Bukkit.getWorld(faction.fHome.world)), false)
                }
            }
        }
    }

    static def showFWarps(Member member, Faction faction) {
        MenuBuilder builder
        int size = faction.warps.size().intdiv(9) == 0 ? 9 : (faction.warps.size().intdiv(9) + 1) * 9

        builder = new MenuBuilder(size, "§3Faction Warps")
        int count = 0

        for (WarpData warpData : faction.warps) {
            builder.set(count, FastItemUtils.createItem(Material.GRASS_BLOCK, "§r§l" + warpData.name, ["§r§3" + warpData.position.x.toString() + ", " + warpData.position.z.toString()]), { p, t, s ->
                def world = Bukkit.getWorld(warpData.position.world)
                if (world == null) {
                    p.sendMessage("§cThe world §l" + warpData.position.world + "§c does not exist.")
                } else {
                    if (Factions.getClaimAt(CL.of(warpData.position.getLocation(world))) == null || Factions.getClaimAt(CL.of(warpData.position.getLocation(world))).factionId != faction.id) {
                        p.sendMessage("§cThe warp you attempted to teleport to is no longer within your faction claims and has been deleted.")
                        faction.warps.remove(warpData)
                        return
                    }
                    FactionUtils.teleportPlayer(p, warpData.position.getLocation(world), false, "§3Teleported to warp §e§n" + warpData.name + "§3.")
                }
            })
            count++
        }

        builder.openSync(Bukkit.getPlayer(member.id))
    }

    static WarpData findWarp(Faction faction, String warpName) {
        for (WarpData warp : faction.warps) {
            if (warp.name.trim().toLowerCase() == warpName.trim().toLowerCase()) return warp
        }
        return null
    }

    static def removeWarp(String warpName, Faction faction) {
        faction.warps.remove(findWarp(faction, warpName))
    }
}
