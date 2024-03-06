package scripts.factions.core.faction.addon.ftop

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.item.ItemStackBuilder
import scripts.factions.core.faction.FCBuilder
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Faction
import scripts.shared.data.uuid.UUIDDataManager
//import scripts.shared.legacy.utils.BroadcastUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.FancyMenuDecorator

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

@CompileStatic(TypeCheckingMode.SKIP)
class FTop {

    static Map<UUID, Long> fTopCache
    static Map<UUID, Long> sortedFTop
    static Map<UUID, Long> spawnerVal
    static Map<UUID, Long> pointsVal
    static LinkedBlockingQueue<FTopEntry> queuedEntries

    FTop() {
        fTopCache = new ConcurrentHashMap<>()
        sortedFTop = new ConcurrentHashMap<>()
        spawnerVal = new ConcurrentHashMap<>()
        pointsVal = new ConcurrentHashMap<>()
        queuedEntries = new LinkedBlockingQueue<>()

        GroovyScript.addUnloadHook {
            Factions.fCommand.subCommands.removeIf { it.aliases.find {
                it.equalsIgnoreCase("top")
            } != null }
            Factions.fCommand.build()
        }

        commands()
        exports()
        loadFTop()

        Schedulers.async().runRepeating({
            updateFTop()
        },20, 20*15)
    }

    static def loadFTop() {
        UUIDDataManager.getAllData(Faction).each { faction ->
            fTopCache.put(faction.id, faction.fTopValue)
            spawnerVal.put(faction.id, faction.spawnerValue)
            pointsVal.put(faction.id, faction.pointsValue)
        }
    }

    static def exports() {
        Exports.ptr("ftop/addEntry", { UUID factionId, int changeAmount, FTEntryType entryType ->
            addFTopEntry(factionId, changeAmount, entryType)
        })
    }

    static def commands() {
        FCBuilder fCommand = Factions.fCommand

        fCommand.create("top").description("Faction leaderboards.").register { cmd ->
            Player player = cmd.sender()
            createFTopMenu(player).openSync(player)
//            FCommandUtil.factionMemberFromCommand(cmd) { faction, member ->
////                GenericLeaderboardUI.create("Faction Top", sortedFTop).openSync(player)
//
//            }
        }
    }

    static def updateFTop() {
        while (!queuedEntries.isEmpty()) {
            FTopEntry entry = queuedEntries.poll()

            fTopCache.put(entry.factionId, fTopCache.getOrDefault(entry.getFactionId(), 0) + entry.amount)

            if (entry.entryType.type == ValueType.MONEY) {
                spawnerVal.put(entry.factionId, spawnerVal.getOrDefault(entry.getFactionId(), 0) + entry.amount)
            } else {
                pointsVal.put(entry.factionId, pointsVal.getOrDefault(entry.getFactionId(), 0) + entry.amount)
            }
        }
        sortedFTop = fTopCache.sort{ a,b -> b.value <=> a.value }

        int rank = 1
        for (def ftEntry : sortedFTop.entrySet()) {
            def faction = Factions.getFaction(ftEntry.key, false)
            if (faction == null) continue

            faction.fTopRank = rank
            faction.fTopValue = ftEntry.value
            faction.pointsValue = pointsVal.getOrDefault(faction.id, 0)
            faction.spawnerValue = spawnerVal.getOrDefault(faction.id, 0)

            if (faction.id == Factions.wildernessId || faction.id == Factions.warZoneId || faction.id == Factions.safeZoneId) continue
            faction.queueSave()
            rank++
        }
    }

    static def addFTopEntry(UUID factionId, int changeAmount, FTEntryType entryType) {
        if (factionId == null || factionId == Factions.wildernessId) return

        queuedEntries.add(new FTopEntry(factionId, changeAmount, entryType))
    }

    static def createFTopMenu(Player player) {
        MenuBuilder builder = new MenuBuilder(6 * 9, "F Top")

        FancyMenuDecorator.decorate(builder,
                [
                        "x": ItemStackBuilder.of(Material.DIAMOND_BLOCK).name("§b§l1st Place").build(),
                        "y": ItemStackBuilder.of(Material.GOLD_BLOCK).name("§6§l2nd Place").build(),
                        "z": ItemStackBuilder.of(Material.IRON_BLOCK).name("§f§l3rd Place").build(),
                        "|": ItemStackBuilder.of(Material.QUARTZ_PILLAR).name("§1").build(),
                        "q": ItemStackBuilder.of(Material.CHISELED_QUARTZ_BLOCK).name("§1").build(),
                ],
                [
                        "|8888888|",
                        "|888=888|",
                        "|88=x=88|",
                        "|88y|z88|",
                        "|=======|",
                        "|8888888|",
                ])

        int counter = 0
        for (def entry : sortedFTop.entrySet()) {
            if (counter > 10) break
            Faction faction = Factions.getFaction(entry.key, false)
            if (faction == null || faction.id == Factions.wildernessId || faction.id == Factions.warZoneId) continue
            String facName = faction.getName()

            if (faction.leaderId == null) continue
//            BroadcastUtils.broadcast(faction.leaderId.toString())
            def leader = Bukkit.getPlayer(faction.leaderId)
            if (leader == null) {
                leader = Bukkit.getOfflinePlayer(faction.leaderId).getName()
            } else {
                leader = leader.getName()
            }

            builder.appendEmpty(FastItemUtils.createSkull(leader, Factions.getRelationType(Factions.getMember(player.getUniqueId()), faction).color + facName, [
                    "§7----------------------------------",
                    "§9§lSpawner Value: §r§f" + spawnerVal.getOrDefault(faction.id, 0).toString(),
                    "§9§lPoints: §r§f" + pointsVal.getOrDefault(faction.id, 0).toString(),
                    "§7----------------------------------"
            ]))
            counter++
        }
        return builder
    }
}
