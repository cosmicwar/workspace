package scripts.exec

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.text3.Text
import org.starcade.starlight.helper.utils.Players
import groovy.transform.Field
import scripts.shared.utils.ColorUtil

@Field String HEX_MAIN = "#0059ff"
@Field String HEX_SECONDARY = "#00e5ff"

@Field Map<String, String> replacements = new HashMap() {
    {
        put("§>", "§8»")
        put("§<", "§8«")
        put("§]", Text.colorize(ColorUtil.rainbow("STARCADE", ["#00e5ff", "#4284ff"] as String[], "§l")))
        put("§[", "§4§lSTAR§4§lCADE")
        put("§^", Text.colorize(ColorUtil.rainbow("Starcade", ["#00e5ff", "#4284ff"] as String[], "§l").toString()))
        put("§p", "§7")
        put("§s", "§b")
        put("§z", "§3")
        put("§!", "§4§lERROR")
        put("§:store", "store.starcade.org")
        put("§:forums", "starcade.org")
        put("§:discord", "discord.gg/starcade")
        put("§:twitter", "https://twitter.com/Starcade")
        put"§:cstore", Text.colorize(ColorUtil.rainbow("STORE", ["#00e5ff", "#4284ff"] as String[], "§l").toString())
        put"§:csale", Text.colorize(ColorUtil.rainbow("SALE", ["#00e5ff", "#4284ff"] as String[], "§l").toString())
        Exports.ptr("theme", this)
    }
}

Players.messageRemapper = { String remap ->
    for (Map.Entry<String, String> entry in replacements.entrySet()) {
        remap = remap.replace(entry.key, entry.value)
    }
    return remap
}

Exports.ptr("tablist_colors", ["§a", "§b"])

Exports.ptr("tablist_header",
        "${ColorUtil.rainbow("-----------------------------------------------------------" as String, ["#0059ff", "#00e5ff", "#0059ff" ] as String[], "§m")}\n" +
                "\n" +
                "§7§m----§3§l» §f§lᴡᴇʟᴄᴏᴍᴇ §b§l%s§f§l! §3§l«§7§m----\n" +
                "§7§o(ʏᴏᴜ ᴀʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ ᴏɴ %s)\n" +
                "\n" +
                "${ColorUtil.rainbow("ʟᴀᴛᴇꜱᴛ ᴀɴɴᴏᴜɴᴄᴇᴍᴇɴᴛ", ["#00e5ff", "#4284ff"] as String[], "§l")}\n" +
                "§f%s" +
                "\n"
)

Exports.ptr("tablist_footer",
        "\n" +
                "${ColorUtil.rainbow("ᴘʟᴀʏᴇʀꜱ", ["#00e5ff", "#4284ff"] as String[], "§l")}\n" +
                "${ColorUtil.rainbow("ᴏɴʟɪɴᴇ", ["#00e5ff", "#4284ff"] as String[], "§l")}       ${ColorUtil.rainbow("ɢʟᴏʙᴀʟ", ["#00e5ff", "#4284ff"] as String[], "§l")}\n" +
                "§f%s§7/§f%s         §f%s§7/§f%s\n" +
                "\n" +
                "${ColorUtil.rainbow("ꜱᴛᴏʀᴇ", ["#00e5ff", "#4284ff"] as String[], "§l")} §fꜱᴛᴏʀᴇ.ꜱᴛᴀʀᴄᴀᴅᴇ.ᴏʀɢ ${ColorUtil.rainbow("ᴅɪꜱᴄᴏʀᴅ", ["#00e5ff", "#4284ff"] as String[], "§l")} §fᴅɪꜱᴄᴏʀᴅ.ɢɢ/ꜱᴛᴀʀᴄᴀᴅᴇ\n" +

                "${ColorUtil.rainbow("-----------------------------------------------------------" as String, [ "#0059ff", "#00e5ff", "#0059ff" ] as String[], "§m")}"
)