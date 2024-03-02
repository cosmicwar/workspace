package scripts.factions.core.profile

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import scripts.factions.core.faction.FCBuilder
import scripts.factions.data.uuid.UUIDDataManager
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MenuDecorator

@CompileStatic(TypeCheckingMode.SKIP)
class Profiles {

    Profiles() {
        GroovyScript.addUnloadHook {
            UUIDDataManager.getByClass(Profile).saveAll(false)
        }

        UUIDDataManager.register("profiles", Profile)

        UUIDDataManager.getAllData(Profile).each {
            def player = Bukkit.getPlayer(it.id)
            if (player != null) {
                it.player = player
            }
        }

        Events.subscribe(PlayerJoinEvent.class).handler {event ->
            def player = event.player
            def profile = UUIDDataManager.getData(player.uniqueId, Profile)
            profile.player = player

            profile.queueSave()
        }

        Events.subscribe(PlayerQuitEvent.class).handler {event ->
            def player = event.player
            def profile = UUIDDataManager.getData(player.uniqueId, Profile)
            profile.player = null

            profile.queueSave()
        }

        commands()
    }

    def commands() {
        FCBuilder cmd = new FCBuilder("profile").defaultAction {
            openProfile(it)
        }


        cmd.build()
    }

    static def openProfile(Player player, UUID targetId = null) {
        if (targetId == null) targetId = player.getUniqueId()

        def profile = UUIDDataManager.getData(targetId, Profile)

        MenuBuilder menu = new MenuBuilder(54, "§aprofile")

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.NETHERITE_CHESTPLATE, "§eGod Mode", [
                "§7Toggle god mode",
                "",
                "§7Current: ${profile.godMode ? "§a§lENABLED" : "§c§lDISABLED"}"
        ]), {p, t, slot ->
            profile.godMode = !profile.godMode
            profile.queueSave()

            openProfile(player, targetId)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.ENDER_PEARL, "§dVanish", [
                "§7Toggle vanish",
                "",
                "§7Current: ${profile.vanished ? "§a§lENABLED" : "§c§lDISABLED"}"
        ]), {p, t, slot ->
            profile.vanished = !profile.vanished
            profile.queueSave()

            openProfile(player, targetId)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.BLAZE_POWDER, "§cStaff Mode", [
                "§7Toggle staff mode",
                "",
                "§7Current: ${profile.staffMode ? "§a§lENABLED" : "§c§lDISABLED"}"
        ]), {p, t, slot ->
            profile.staffMode = !profile.staffMode
            profile.queueSave()

            openProfile(player, targetId)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.PAPER, "§fStaff Chat", [
                "§7Toggle staff chat",
                "",
                "§7Current: ${profile.staffChat ? "§a§lENABLED" : "§c§lDISABLED"}"
        ]), {p, t, slot ->
            profile.staffChat = !profile.staffChat
            profile.queueSave()

            openProfile(player, targetId)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.BOOK, "§9Admin Chat", [
                "§7Toggle admin chat",
                "",
                "§7Current: ${profile.adminChat ? "§a§lENABLED" : "§c§lDISABLED"}"
        ]), {p, t, slot ->
            profile.adminChat = !profile.adminChat
            profile.queueSave()

            openProfile(player, targetId)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.SPYGLASS, "§eSocial Spy", [
                "§7Toggle social spy",
                "",
                "§7Current: ${profile.socialSpy ? "§a§lENABLED" : "§c§lDISABLED"}"
        ]), {p, t, slot ->
            profile.socialSpy = !profile.socialSpy
            profile.queueSave()

            openProfile(player, targetId)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.RED_DYE, "§c§lPunishments", [
                "§7View your punishments",
                "",
                "§7Total: ${profile.punishments.size()}"
        ]), {p, t, slot ->

        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.GREEN_DYE, "§a§lGrants", [
                "§7View your grants",
                "",
                "§7Total: §a${profile.activeGrants.size()}/§c${profile.grants.size()}"
        ]), {p, t, slot ->

        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.BLUE_DYE, "§b§lPermissions", ["§7View your permissions"]), {p, t, slot ->

        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.GOLDEN_APPLE, "§6§lSettings", ["§7View your settings"]), {p, t, slot ->

        })

        menu.openSync(player)
    }

}
