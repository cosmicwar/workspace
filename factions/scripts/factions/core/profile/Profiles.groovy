package scripts.factions.core.profile

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.profile.rank.Rank
import scripts.factions.data.uuid.UUIDDataManager
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared3.utils.Callback

@CompileStatic(TypeCheckingMode.SKIP)
class Profiles {

    Profiles() {
        GroovyScript.addUnloadHook {
            Starlight.unload("~/cmd/RankCmd.groovy")
            Starlight.unload("~/cmd/GrantCmd.groovy")

            UUIDDataManager.getByClass(Profile).saveAll(false)
        }

        UUIDDataManager.register("ranks", Rank)
        UUIDDataManager.register("profiles", Profile)

        UUIDDataManager.getAllData(Profile).each {
            def player = Bukkit.getPlayer(it.id)
            if (player != null) {
                it.player = player
            }
        }

        Starlight.watch("~/cmd/RankCmd.groovy")
        Starlight.watch("~/cmd/GrantCmd.groovy")

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

    // profile data handling
    static Profile getProfile(UUID uuid, boolean create = true) {
        return UUIDDataManager.getData(uuid, Profile, create)
    }
    static def getProfileAsync(UUID uuid, boolean create = true, Callback<Profile> callback = {}) {
        Schedulers.async().execute {
            callback.exec(getProfile(uuid, create))
        }
    }
    static Collection<Profile> getProfiles() {
        return UUIDDataManager.getAllData(Profile)
    }
    static def getProfilesAsync(Callback<Collection<Profile>> callback = {}) {
        Schedulers.async().execute {
            callback.exec(getProfiles())
        }
    }

    // rank data handling
    static Rank getRank(UUID uuid, boolean create = true) {
        return UUIDDataManager.getData(uuid, Rank, create)
    }
    static def getRankAsync(UUID uuid, boolean create = true, Callback<Rank> callback = {}) {
        Schedulers.async().execute {
            callback.exec(getRank(uuid, create))
        }
    }
    static Collection<Rank> getRanks() {
        return UUIDDataManager.getAllData(Rank)
    }
    static def getRanksAsync(Callback<Collection<Rank>> callback = {}) {
        Schedulers.async().execute {
            callback.exec(getRanks())
        }
    }

    static def broadcastRankUpdate(Rank rank) {}

    static def openProfile(Player player, UUID targetId = null) {
        if (targetId == null) targetId = player.getUniqueId()

        def profile = UUIDDataManager.getData(targetId, Profile)

        def templeData = profile.templeData

        MenuBuilder menu = new MenuBuilder(54, "§aprofile")

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.NETHERITE_CHESTPLATE, "§eGod Mode", [
                "§7Toggle god mode",
                "",
                "§7Current: ${templeData.godMode ? "§a§lENABLED" : "§c§lDISABLED"}"
        ]), {p, t, slot ->
            templeData.godMode = !templeData.godMode
            profile.queueSave()

            openProfile(player, targetId)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.ENDER_PEARL, "§dVanish", [
                "§7Toggle vanish",
                "",
                "§7Current: ${templeData.vanished ? "§a§lENABLED" : "§c§lDISABLED"}"
        ]), {p, t, slot ->
            templeData.vanished = !templeData.vanished
            profile.queueSave()

            openProfile(player, targetId)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.BLAZE_POWDER, "§cStaff Mode", [
                "§7Toggle staff mode",
                "",
                "§7Current: ${templeData.staffMode ? "§a§lENABLED" : "§c§lDISABLED"}"
        ]), {p, t, slot ->
            templeData.staffMode = !templeData.staffMode
            profile.queueSave()

            openProfile(player, targetId)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.PAPER, "§fStaff Chat", [
                "§7Toggle staff chat",
                "",
                "§7Current: ${templeData.staffChat ? "§a§lENABLED" : "§c§lDISABLED"}"
        ]), {p, t, slot ->
            templeData.staffChat = !templeData.staffChat
            profile.queueSave()

            openProfile(player, targetId)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.BOOK, "§9Admin Chat", [
                "§7Toggle admin chat",
                "",
                "§7Current: ${templeData.adminChat ? "§a§lENABLED" : "§c§lDISABLED"}"
        ]), {p, t, slot ->
            templeData.adminChat = !templeData.adminChat
            profile.queueSave()

            openProfile(player, targetId)
        })

        menu.set(menu.get().firstEmpty(), FastItemUtils.createItem(Material.SPYGLASS, "§eSocial Spy", [
                "§7Toggle social spy",
                "",
                "§7Current: ${templeData.socialSpy ? "§a§lENABLED" : "§c§lDISABLED"}"
        ]), {p, t, slot ->
            templeData.socialSpy = !templeData.socialSpy
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
