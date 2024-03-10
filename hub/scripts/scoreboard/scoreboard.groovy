package scripts.scoreboard

import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.wazowski.fake.FakeEntityPlayer
import scripts.scoreboard.sidebar.SidebarBuilder
import scripts.scoreboard.sidebar.SidebarHandler
import scripts.shared.core.profile.Profiles
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.systems.BungeeCache
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.Temple

import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy")

def hubBoard = new SidebarBuilder("hub_board")
        .title { player -> return "${ColorUtil.rainbow("ꜱᴛᴀʀᴄᴀᴅᴇ", ["#00e5ff", "#4284ff"] as String[], "§l").toString()} §8| §<#FFA445>${Temple.templeId}" }
        .lines { player ->
            if (player == null || player instanceof FakeEntityPlayer) return []

            List<String> lines = []

            lines.add("§8§m${StringUtils.repeat('-', 24)}") // spacer
            lines.add("§<#45A0FF>ᴀᴄᴄᴏᴜɴᴛ: §<#09FB29>${player.name}")

            LocalDateTime firstPlayed = LocalDateTime.ofInstant(Instant.ofEpochMilli(player.firstPlayed), TimeZone.getDefault().toZoneId())
            lines.add("§<#45A0FF>ꜰɪʀꜱᴛ ᴊᴏɪɴᴇᴅ: §<#FFA445>${dtf.format(firstPlayed)}")

            def profile = Profiles.getProfile(player.uniqueId)
            lines.add("§<#45A0FF>ʀᴀɴᴋ: §<#09FB29>${profile.getRank().displayName}")

            lines.add("")

            // global player count
            lines.add("§<#45A0FF>ɢʟᴏʙᴀʟ ᴘʟᴀʏᴇʀꜱ: §<#09FB29>${Bukkit.getOnlinePlayers().size()}§7/§<#FFA445>${NumberUtils.format(BungeeCache.getGlobalPlayerCount())}")
            lines.add("§8§m${StringUtils.repeat('-', 24)}") // spacer
            return lines
        }.priority { return 0 }
        .shouldDisplayTo { Player player -> return true }
        .build()

SidebarHandler.registerSidebar(hubBoard)

GroovyScript.addUnloadHook {
    SidebarHandler.unregisterSidebar(hubBoard.getInternalId())
}
