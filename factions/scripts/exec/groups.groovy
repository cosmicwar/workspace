package scripts.exec

import org.starcade.starlight.enviorment.Exports
import net.md_5.bungee.api.ChatColor
import scripts.shared.utils.ColorUtil
import scripts.shared3.ArkGroups

ArkGroups.addGroups([

        //STAFF
        "group.staff.owner"      : [
                priority  : 1000,
                color     : "#fa3e3e",
                scoreboard: "§4§lOwner§r",
                short     : "§4§lOwner§r",
                long      : "§d§k|§8[§4§lOwner§8]§d§k|§r",
                custom    : '丩'
        ],
        "group.staff.developer"  : [
                priority  : 950,
                color     : "#fa3e3e",
                scoreboard: "§4§lDev§r",
                short     : "§4§lDev§r",
                long      : "§4§k|§8[§4§lDev§8]§4§k|§r",
                custom    : '丫'
        ],
        "group.staff.manager"    : [
                priority  : 900,
                color     : "#fa3e3e",
                scoreboard: "§4§lManager§r",
                short     : "§4§lManager§r",
                long      : "§d§k|§8[§4§lManager§8]§d§k|§r",
                custom    : '个'
        ],
        "group.staff.admin"      : [
                priority  : 850,
                color     : "#fa3e3e",
                scoreboard: "§4§lAdmin§r",
                short     : "§4§lAdmin§r",
                long      : "§c§k|§8§l[§c§lAdmin§8§l]§c§k|§r",
                custom    : '丬'
        ],
        "group.staff.srmod"      : [
                priority  : 800,
                color     : "#f26ff2",
                scoreboard: "§5§lSr. Mod§r",
                short     : "§5§lSr. Mod§r",
                long      : "§5§k|§8§l[§5§lSrMod§8§l]§5§k|§r",
                custom    : '中'
        ],
        "group.staff.mod"        : [
                priority  : 750,
                color     : "#3ec73e",
                scoreboard: "§2§lMod§r",
                short     : "§2§lMod§r",
                long      : "§2§k|§8§l[§2§lMod§8§l]§2§k|§r",
                custom    : '丮'
        ],
        "group.staff.helper"     : [
                priority  : 700,
                color     : "#76f576",
                scoreboard: "§a§lHelper§r",
                short     : "§a§lHelper§r",
                long      : "§a§k|§8§l[§a§lHelper§8§l]§a§k|§r",
                custom    : '丯'
        ],
        "group.staff.trial"      : [
                priority  : 600,
                color     : "#ffa500",
                scoreboard: "§6§lTrial§r",
                short     : "§6§lTrial§r",
                long      : "§6§k|§8§l[§6§lTrial§8§l]§6§k|§r",
                custom    : "㐰"
        ],

        //MEDIA
        "group.tag.partner"      : [
                priority  : 500,
                color     : "#ff99ff",
                short     : "§d§lPartner",
                scoreboard: "§d§lPartner",
                long      : "§d§k|§r§8[§d§lPARTNER§8]§d§k|§r",
                custom    : '丷'
        ],


        "group.tag.media"        : [
                priority  : 450,
                color     : "#ff99ff",
                short     : "§dMedia",
                scoreboard: "§dMedia",
                long      : "§d§k|§r§8[§d§lMedia§8]§d§k|§r",
                custom    : '丸'
        ],

        //DONATORS
        "group.donator.emperor"  : [
                priority  : 375,
                color     : "#009AE4",
                short     : "${ColorUtil.gradient("#003de4", "#0090e4", "Emperor", "§l")}§r",
                scoreboard: "${ColorUtil.gradient("#003de4", "#0090e4", "Emperor", "§l")}§r",
                long      : "§b§k|§r§8[${ColorUtil.gradient("#003de4", "#0090e4", "Emperor", "§l")}§8]§b§k|§r",
                custom    : "${ColorUtil.gradient("#003de4", "#0090e4", "Emperor", "§l")}§r"
        ],
        "group.donator.vader"    : [
                priority  : 325,
                color     : "#ff99ff",
                short     : "${ColorUtil.gradient("#ff3dff", "#ffabff", "Vader")}§r",
                scoreboard: "${ColorUtil.gradient("#ff3dff", "#ffabff", "Vader")}§r",
                long      : "§b§k|§r§8[${ColorUtil.gradient("#ff3dff", "#ffabff", "Vader")}§8]§b§k|§r",
                custom    : "${ColorUtil.gradient("#ff3dff", "#ffabff", "Vader")}§r"
        ],
        "group.donator.commander": [
                priority  : 275,
                color     : "#a1ffff",
                short     : "${ColorUtil.gradient("#4affff", "#bdffff", "Commander")}§r",
                scoreboard: "${ColorUtil.gradient("#4affff", "#bdffff", "Commander")}§r",
                long      : "§b§k|§r§8[${ColorUtil.gradient("#4affff", "#bdffff", "Commander")}§8]§b§k|§r",
                custom    : "${ColorUtil.gradient("#4affff", "#bdffff", "Commander")}§r"
        ],
        "group.donator.captain"  : [
                priority  : 225,
                color     : "#1cbfff",
                short     : "${ColorUtil.color("§<#1cbfff>Captain")}§r",
                scoreboard: "${ColorUtil.color("§<#1cbfff>Captain")}§r",
                long      : "§b§k|§r§8[${ColorUtil.color("§<#1cbfff>Captain")}§8]§b§k|§r",
                custom    : "${ColorUtil.color("§<#1cbfff>Captain")}§r"
        ],
        "group.donator.voyager"  : [
                priority  : 175,
                color     : "#4561ff",
                short     : "${ColorUtil.color("§<#4561ff>Voyager")}§r",
                scoreboard: "${ColorUtil.color("§<#4561ff>Voyager")}§r",
                long      : "§b§k|§r§8[${ColorUtil.color("§<#4561ff>Voyager")}§8]§b§k|§r",
                custom    : "${ColorUtil.color("§<#4561ff>Voyager")}§r"
        ],
        "group.donator.nomad"    : [
                priority  : 75,
                color     : "#4f80ff",
                short     : "${ColorUtil.color("§<#4f80ff>Nomad")}§r",
                scoreboard: "${ColorUtil.color("§<#4f80ff>Nomad")}§r",
                long      : "§b§k|§r§8[${ColorUtil.color("#4f80ff")}Nomad§8]§b§k|§r",
                custom    : "${ColorUtil.color("§<#4f80ff>Nomad")}§r"
        ]
])

ArkGroups.addGroups([
        "default": [
                priority  : 0,
                color     : "#85e9ff",
                short     : "${ColorUtil.color("§<#85e9ff>Explorer")}§r",
                scoreboard: "${ColorUtil.color("§<#85e9ff>Explorer")}§r",
                long      : "§b§k|§r§8[${ColorUtil.color("§<#85e9ff>Explorer")}§8]§b§k|§r",
                custom    : "${ColorUtil.color("§<#85e9ff>Explorer")}§r"
        ]
])


(Exports.ptr("arktheme/refreshgroups") as Closure).call()
(Exports.ptr("arktags/refreshgroups") as Closure).call()