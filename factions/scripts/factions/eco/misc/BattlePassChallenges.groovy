package scripts.factions.eco.misc

import com.google.common.collect.Sets
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import groovy.transform.CompileStatic
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Player
import scripts.exec.Globals
import scripts.shared.features.battlepass.BattlePass
import scripts.shared.features.battlepass.BattlePassChallenge
import scripts.shared.features.battlepass.BattlePassData

import java.time.LocalDate
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

@CompileStatic
class BattlePassChallenges {

    static final Set<UUID> ONLINE_PLAYERS = Sets.newConcurrentHashSet()

    static void main(String[] args) {
        if (!Globals.BATTLEPASS) {
            return
        }

        (Exports.ptr("battlebass_challenges") as Map<String, Map<String, ?>>)?.each {
            BattlePass.CHALLENGE_REGISTRY.put(it.key.toUpperCase(), new BattlePassChallenge(it.value))
        }

        Schedulers.async().runRepeating({
            int currentDay = LocalDate.now().getDayOfYear()
            Bukkit.getOnlinePlayers().each {
                BattlePassData battlePassData = BattlePass.playerData.get(it.uniqueId)
                if (battlePassData == null) return

                if (ONLINE_PLAYERS.contains(it.uniqueId)) {
                    BattlePass.addProgress(it.uniqueId, "ONLINE_MINUTES", 1L)
                }

                if (battlePassData.lastChallengeResetDay != currentDay) {
                    battlePassData.lastChallengeResetDay = currentDay
                    BattlePass.rollChallenges(it, battlePassData)

                    Players.msg(it, "§b§lBattlePass §> §fNew daily challenges are now available on your /battlepass!")
                }
            }

            ONLINE_PLAYERS.clear()
            ONLINE_PLAYERS.addAll(Bukkit.getOnlinePlayers().findResults { it.uniqueId })
        }, 1L, TimeUnit.MINUTES, 1L, TimeUnit.MINUTES)

        Exports.ptr("battlepass:incrementChallengeProgress", { Player player, String challenge, long progress -> BattlePass.addProgress(player.uniqueId, challenge, progress) })

        // legacy
        Exports.ptr("battlepass:onBlockBroken", { Player player, Block block -> onBlockBroken(player, block) })
        Exports.ptr("battlepass:openCommonShinyTreasure", { Player player -> BattlePass.addProgress(player.uniqueId, "OPEN_TREASURES_COMMON", 1L) })
        Exports.ptr("battlepass:openEpicShinyTreasure", { Player player -> BattlePass.addProgress(player.uniqueId, "OPEN_TREASURES_EPIC", 1L) })
        Exports.ptr("battlepass:openRareShinyTreasure", { Player player -> BattlePass.addProgress(player.uniqueId, "OPEN_TREASURES_RARE", 1L) })
        Exports.ptr("battlepass:openLegendaryShinyTreasure", { Player player -> BattlePass.addProgress(player.uniqueId, "OPEN_TREASURES_LEGENDARY", 1L) })
        Exports.ptr("battlepass:pveKill", { Player player -> BattlePass.addProgress(player.uniqueId, "PVE_KILLS", 1L) })
        Exports.ptr("battlepass:bossKill", { Player player -> BattlePass.addProgress(player.uniqueId, "BOSS_KILLS", 1L) })
        Exports.ptr("battlepass:pveBeaconMine", { Player player -> BattlePass.addProgress(player.uniqueId, "PVE_MINE_BEACONS", 1L) })
        Exports.ptr("battlepass:openCrate", { Player player -> BattlePass.addProgress(player.uniqueId, "OPEN_CRATES", 1L) })
        Exports.ptr("battlepass:autominerBlocks", { Player player, int blocks -> BattlePass.addProgress(player.uniqueId, "AUTOMINER_BREAK_BLOCKS", blocks) })
        Exports.ptr("battlepass:enchantLevels", { Player player, long levels -> BattlePass.addProgress(player.uniqueId, "ENCHANT_LEVELS", levels) })
        Exports.ptr("battlepass:wandUses", { Player player -> BattlePass.addProgress(player.uniqueId, "USE_WAND", 1L) })
        Exports.ptr("battlepass:robotMoney", { Player player, long money -> BattlePass.addProgress(player.uniqueId, "ROBOT_MONEY", money) })
        Exports.ptr("battlepass:mergeRobots", { Player player -> BattlePass.addProgress(player.uniqueId, "MERGE_ROBOTS", 1L) })
    }

    static void onBlockBroken(Player player, Block block) {
        BattlePass.addProgress(player.uniqueId, "BREAK_BLOCKS", 1L)

        double xpChance = (Exports.ptr("battlebass_xp") as Map<String, ?>)?["blockBreakXpChance"] as Double ?: 0D
        if (ThreadLocalRandom.current().nextDouble() > xpChance) return

        int xp = (Exports.ptr("battlebass_xp") as Map<String, ?>)?["blockBreakXp"] as Integer ?: 0
        if (xp <= 0) return

        BattlePass.addXp(player.uniqueId, xp)
    }

}

