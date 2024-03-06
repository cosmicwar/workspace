package scripts.factions.cfg

import org.apache.commons.lang3.StringUtils
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.wazowski.fake.FakeEntityPlayer
import scripts.factions.content.scoreboard.sidebar.SidebarBuilder
import scripts.factions.content.scoreboard.sidebar.SidebarHandler
import scripts.factions.core.faction.Factions
import scripts.shared.content.systems.Stardust
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.ExpUtils
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.Temple

import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

CurrencyStorage money = Exports.ptr("money") as CurrencyStorage
DecimalFormat df = new DecimalFormat("#.##")
DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd")

if (Temple.templeId.contains("beta")) {
    def board = new SidebarBuilder("main")
            .title({ Player player ->
                def now = LocalDate.ofInstant(Instant.now(), TimeZone.getTimeZone(ZoneId.of("America/New_York")).toZoneId())

                return "${ColorUtil.rainbow("ꜱᴛᴀʀᴄᴀᴅᴇ ʙᴇᴛᴀ", ["#00e5ff", "#4284ff"] as String[], "§l").toString()} §7| ${ColorUtil.color("§<#3F9DFF>${dtf.format(now)}")}"
            })
            .lines({ Player player ->
                if (player == null || player instanceof FakeEntityPlayer) return []

                List<String> lines = []

                def xp = ExpUtils.getTotalExperience(player)
                def kills = player.getStatistic(Statistic.PLAYER_KILLS)
                def deaths = player.getStatistic(Statistic.DEATHS)

                lines.add("§8§m${StringUtils.repeat('-', 24)}") // spacer
                lines.add("§<#45A0FF>ᴀᴄᴄᴏᴜɴᴛ: §<#09FB29>${player.getName()}")

                def kdr = kills / (deaths == 0 ? 1 : deaths)
                def color = kdr >= 1.0 ? "§a" : "§c"
                lines.add("  §<#45A0FF>ᴋᴅʀ: §a${kills}§7/§c${deaths} ${color}(${df.format(kdr)})")

                lines.add("  §<#45A0FF>ꜱᴛᴀʀᴅᴜꜱᴛ: §<#FBF961>${NumberUtils.format(Stardust.getCachedBalance(player.uniqueId).tradableStardust)}✬")


                def member = Factions.getMember(player.getUniqueId())
                def faction = Factions.getFaction(member.getFactionId(), false)
                if (faction != null && faction.systemFactionData == null) {
                    lines.add("")
                    lines.add("§<#FFA445>ꜰᴀᴄᴛɪᴏɴ: §<#09FB29>${faction.getName()}")
                    lines.add("§<#09FB29>${faction.getOnlineMembers().size()}§7/§<#FFA445>${faction.getMembers().size()}")
                }

                lines.add("§8§m${StringUtils.repeat('-', 22)}") // spacer
                return lines
            })
            .priority({ return 0 })
            .shouldDisplayTo({ Player player -> return true })
            .build()

    SidebarHandler.registerSidebar(board)
} else {
    def board = new SidebarBuilder("main")
            .title({ Player player ->
                def now = LocalDate.ofInstant(Instant.now(), TimeZone.getTimeZone(ZoneId.of("America/New_York")).toZoneId())

                return /*player.hasResourcePack() ? "糷糸" : */ "${ColorUtil.rainbow("ꜱᴛᴀʀᴄᴀᴅᴇ", ["#00e5ff", "#4284ff"] as String[], "§l").toString()} §7| ${ColorUtil.color("§<#3F9DFF>${dtf.format(now)}")}"
            })
            .lines({ Player player ->
                if (player == null || player instanceof FakeEntityPlayer) return []

                List<String> lines = []

                def xp = ExpUtils.getTotalExperience(player)
                def kills = player.getStatistic(Statistic.PLAYER_KILLS)
                def deaths = player.getStatistic(Statistic.DEATHS)

                lines.add("§8§m${StringUtils.repeat('-', 22)}") // spacer
                lines.add("§<#45A0FF>ᴀᴄᴄᴏᴜɴᴛ: §<#09FB29>${player.getName()}")
                lines.add("  §<#45A0FF>ᴍᴏɴᴇʏ: §<#09FB29>${NumberUtils.format(money.get(player.getUniqueId()))}")
                lines.add("  §<#45A0FF>ᴇxᴘᴇʀɪᴇɴᴄᴇ: §<#ADF3FD>${NumberUtils.format(xp)} xp")

                def kdr = kills / (deaths == 0 ? 1 : deaths)
                def color = kdr >= 1.0 ? "§a" : "§c"
                lines.add("  §<#45A0FF>ᴋᴅʀ: §a${kills}§7/§c${deaths} ${color}(${df.format(kdr)})")

                lines.add("  §<#45A0FF>ꜱᴛᴀʀᴅᴜꜱᴛ: §<#FBF961>${NumberUtils.format(Stardust.getCachedBalance(player.uniqueId).tradableStardust)}✬")


                def member = Factions.getMember(player.getUniqueId())
                def faction = Factions.getFaction(member.getFactionId(), false)
                if (faction != null && faction.systemFactionData == null) {
                    lines.add("")
                    lines.add("§<#FFA445>ꜰᴀᴄᴛɪᴏɴ: §<#09FB29>${faction.getName()}")
                    lines.add("§<#09FB29>${faction.getOnlineMembers().size()}§7/§<#FFA445>${faction.getMembers().size()}")
                }

                lines.add("§8§m${StringUtils.repeat('-', 22)}") // spacer
                return lines
            })
            .priority({ return 0 })
            .shouldDisplayTo({ Player player -> return true })
            .build()

    SidebarHandler.registerSidebar(board)
}


def adminBoard = new SidebarBuilder("admin_board")
        .title { player ->

        }.lines {

}.priority({ return 0 })
        .shouldDisplayTo({ Player player -> return true })
        .build()

SidebarHandler.registerSidebar(adminBoard)

GroovyScript.addUnloadHook {
    SidebarHandler.unregisterSidebar(board.getInternalId())
    SidebarHandler.unregisterSidebar(adminBoard.getInternalId())
}
