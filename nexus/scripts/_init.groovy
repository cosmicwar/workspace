package scripts

import com.comphenix.protocol.PacketType
import org.bukkit.Bukkit
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.protocol.Protocol
import scripts.shared.legacy.database.mysql.MySQL

import java.util.concurrent.TimeUnit

Globals.STAFF = true


Starlight.watch(
        "scripts/exec/universalconfig.groovy",
        "scripts/exec/config.groovy",
        "scripts/exec/theme.groovy",
        "scripts/exec/databases.groovy",
        "scripts/shared3/data/ArkPlayer.groovy",
        "scripts/shared3/ArkGpt.groovy",
        "scripts/shared3/ArkTheme.groovy",
        "scripts/shared3/ArkPerms.groovy",
        "scripts/shared3/ArkGroups.groovy",
        "scripts/shared3/ArkAlerts.groovy",
        "scripts/shared3/ArkVotes.groovy",
        "scripts/shared3/ArkAds.groovy",

        "scripts/exec/permissions.groovy",
        "scripts/exec/gpt.groovy",
        "scripts/exec/groups.groovy",
        "scripts/shared/systems/heartbeat.groovy",
        "scripts/shared/systems/_servercache.groovy",
        "scripts/shared/systems/_spoofcache.groovy",
        "scripts/shared/systems/bedrock_impl.groovy",
        "scripts/shared/systems/_menubuilder.groovy",
        "scripts/shared/systems/queue.groovy",
        "scripts/shared/legacy/currency.groovy",

        "~/fixes.groovy",
        "~/spoof.groovy",

        "~/interactions.groovy",
        "~/votifier.groovy",
        "~/voteparty.groovy",

        "~/notifications.groovy",
        "~/globalpermissions.groovy",

        "scripts/shared/staff/StaffMode.groovy",
        "scripts/shared/legacy/TrialSystem.groovy",
        "scripts/shared/staff/Staff.groovy",
        "scripts/shared3/ArkExt.groovy"
)

Schedulers.async().runRepeating({
    MySQL.getSyncDatabase().executeUpdate("PURGE BINARY LOGS BEFORE now() - INTERVAL 7 DAY;", {}, {})
}, 0, TimeUnit.DAYS, 1, TimeUnit.DAYS)


Schedulers.sync().runRepeating({
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "buycraft forcecheck")
}, 20 * 15, 20 * 15)


Protocol.subscribe(PacketType.Handshake.Client.SET_PROTOCOL).handler { p ->
    p.setCancelled(true)
    p.player.kickPlayer("")
}


//int triggered = 0
//Schedulers.async().runRepeating({
//    if (Globals.isMcp) {
//        if (!GeyserUtils.isLatestGeyserBuild() && triggered % 288 == 0) {
//            String message = GeyserUtils.getCurrentGeyserBuildMessage()
//            TelegramUtils.sendToAlerts("New Geyser Version Avaliable. Update Message: ${message}")
//            triggered++
//        } else if (!GeyserUtils.isLatestGeyserBuild()) {
//            triggered++
//        }
//        if (GeyserUtils.isLatestGeyserBuild()) {
//            triggered = 0
//        }
//    }
//}, 5, TimeUnit.MINUTES, 5, TimeUnit.MINUTES)
//
//Commands.create().assertOp().handler({ c ->
//    Starlight.log.info("Message: ${GeyserUtils.getCurrentGeyserBuildMessage()}")
//}).register("dev/geyser")
