package scripts.scoreboard

import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.wazowski.fake.FakeEntityPlayer
import scripts.scoreboard.sidebar.SidebarBuilder
import scripts.scoreboard.sidebar.SidebarHandler
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.ExpUtils
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.systems.BungeeCache
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.Temple

import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

CurrencyStorage money = Exports.ptr("money") as CurrencyStorage
DecimalFormat df = new DecimalFormat("#.##")
DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy")


def hubBoard = new SidebarBuilder("hub_board")
        .title { player ->
            def now = LocalDate.ofInstant(Instant.now(), TimeZone.getTimeZone(ZoneId.of("America/New_York")).toZoneId())

            return "${ColorUtil.rainbow("ꜱᴛᴀʀᴄᴀᴅᴇ", ["#00e5ff", "#4284ff"] as String[], "§l").toString()} §8| §7${Temple.templeId}"
        }.lines { player ->

    if (player == null || player instanceof FakeEntityPlayer) return []

    List<String> lines = []

    def xp = ExpUtils.getTotalExperience(player)
    def kills = player.getStatistic(Statistic.PLAYER_KILLS)
    def deaths = player.getStatistic(Statistic.DEATHS)

    lines.add("§8§m${StringUtils.repeat('-', 22)}") // spacer
    lines.add("§<#45A0FF>ᴀᴄᴄᴏᴜɴᴛ: §<#09FB29>${player.getName()}")

    LocalDateTime firstPlayed = LocalDateTime.ofInstant(Instant.ofEpochMilli(player.getFirstPlayed()), TimeZone.getDefault().toZoneId())
    lines.add("§<#45A0FF>ꜰɪʀꜱᴛ ᴊᴏɪɴᴇᴅ: §<#09FB29>${dtf.format(firstPlayed)}")
    lines.add("")

    // global player count
    lines.add("§<#45A0FF>ᴘʟᴀʏᴇʀꜱ: §<#09FB29>${Bukkit.getOnlinePlayers().size()}§7/§<#FFA445>${NumberUtils.format(BungeeCache.getGlobalPlayerCount())}")

    lines.add("§8§m${StringUtils.repeat('-', 22)}") // spacer
    return lines
}.priority({ return 0 })
        .shouldDisplayTo({ Player player -> return true })
        .build()

SidebarHandler.registerSidebar(hubBoard)

GroovyScript.addUnloadHook {
    SidebarHandler.unregisterSidebar(hubBoard.getInternalId())
}
