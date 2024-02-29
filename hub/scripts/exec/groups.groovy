package scripts.exec

import org.starcade.starlight.enviorment.Exports
import net.md_5.bungee.api.ChatColor
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.Temple
import scripts.shared3.ArkGroups

ArkGroups.addGroups([

        //STAFF
        "group.staff.owner"         : [
                priority  : 1000,
                color     : "#fa3e3e",
                scoreboard: "§4§lOwner§r",
                short     : "§4§lOwner§r",
                long      : "§d§k|§8[§4§lOwner§8]§d§k|§r",
                custom    : '丩'
        ],
        "group.staff.developer"     : [
                priority  : 950,
                color     : "#fa3e3e",
                scoreboard: "§4§lDev§r",
                short     : "§4§lDev§r",
                long      : "§4§k|§8[§4§lDev§8]§4§k|§r",
                custom    : '丫'
        ],
        "group.staff.manager"       : [
                priority  : 900,
                color     : "#fa3e3e",
                scoreboard: "§4§lManager§r",
                short     : "§4§lManager§r",
                long      : "§d§k|§8[§4§lManager§8]§d§k|§r",
                custom    : '个'
        ],
        "group.staff.admin"         : [
                priority  : 850,
                color     : "#fa3e3e",
                scoreboard: "§4§lAdmin§r",
                short     : "§4§lAdmin§r",
                long      : "§c§k|§8§l[§c§lAdmin§8§l]§c§k|§r",
                custom    : '丬'
        ],
        "group.staff.srmod"         : [
                priority  : 800,
                color     : "#f26ff2",
                scoreboard: "§5§lSr. Mod§r",
                short     : "§5§lSr. Mod§r",
                long      : "§5§k|§8§l[§5§lSrMod§8§l]§5§k|§r",
                custom    : '中'
        ],
        "group.staff.mod"           : [
                priority  : 750,
                color     : "#3ec73e",
                scoreboard: "§2§lMod§r",
                short     : "§2§lMod§r",
                long      : "§2§k|§8§l[§2§lMod§8§l]§2§k|§r",
                custom    : '丮'
        ],
        "group.staff.helper"        : [
                priority  : 700,
                color     : "#76f576",
                scoreboard: "§a§lHelper§r",
                short     : "§a§lHelper§r",
                long      : "§a§k|§8§l[§a§lHelper§8§l]§a§k|§r",
                custom    : '丯'
        ],
        "group.staff.trial": [
                priority: 600,
                color: "#ffa500",
                scoreboard: "§6§lTrial§r",
                short: "§6§lTrial§r",
                long: "§6§k|§8§l[§6§lTrial§8§l]§6§k|§r",
                custom: "㐰"
        ],

        //MEDIA
        "group.tag.partner"         : [
                priority  : 500,
                color     : "#ff99ff",
                short     : "§d§lPartner",
                scoreboard: "§d§lPartner",
                long      : "§d§k|§r§8[§d§lPARTNER§8]§d§k|§r",
                custom    : '丷'
        ],


        "group.tag.media"           : [
                priority  : 450,
                color     : "#ff99ff",
                short     : "§dMedia",
                scoreboard: "§dMedia",
                long      : "§d§k|§r§8[§d§lMedia§8]§d§k|§r",
                custom    : '丸'
        ],

        //DONATORS
        "group.donator.blizzard"    : [
                priority  : 375,
                color     : "#009AE4",
                short     : "${ColorUtil.gradient("#003de4", "#0090e4", "Blizzard", "§l")}§r",
                scoreboard: "${ColorUtil.gradient("#003de4", "#0090e4", "Blizzard", "§l")}§r",
                long      : "§b§k|§r§8[${ColorUtil.gradient("#003de4", "#0090e4", "Blizzard", "§l")}§8]§b§k|§r",
                custom    : '㐀'
        ],
        "group.donator.frostbite"   : [
                priority  : 325,
                color     : "#ff99ff",
                short     : "${ColorUtil.gradient("#ff3dff", "#ffabff", "Frostbite")}§r",
                scoreboard: "${ColorUtil.gradient("#ff3dff", "#ffabff", "Frostbite")}§r",
                long      : "§b§k|§r§8[${ColorUtil.gradient("#ff3dff", "#ffabff", "Frostbite")}§8]§b§k|§r",
                custom    : '㐁'
        ],
        "group.donator.frosty"      : [
                priority  : 275,
                color     : "#a1ffff",
                short     : "${ColorUtil.gradient("#4affff", "#bdffff", "Frosty")}§r",
                scoreboard: "${ColorUtil.gradient("#4affff", "#bdffff", "Frosty")}§r",
                long      : "§b§k|§r§8[${ColorUtil.gradient("#4affff", "#bdffff", "Frosty")}§8]§b§k|§r",
                custom    : '㐂'
        ],
        "group.donator.frozen"      : [
                priority  : 225,
                color     : "#1cbfff",
                short     : "${ChatColor.of("#1cbfff")}Frozen§r",
                scoreboard: "${ChatColor.of("#1cbfff")}Frozen§r",
                long      : "§b§k|§r§8[${ChatColor.of("#1cbfff")}Frozen§8]§b§k|§r",
                custom    : '㐃'
        ],
        "group.donator.icy"         : [
                priority  : 175,
                color     : "#4561ff",
                short     : "${ChatColor.of("#4561ff")}Icy§r",
                scoreboard: "${ChatColor.of("#4561ff")}Icy§r",
                long      : "§b§k|§r§8[${ChatColor.of("#4561ff")}Icy§8]§b§k|§r",
                custom    : '㐄'
        ],
        "group.donator.snowy"       : [
                priority  : 125,
                color     : "#c0a1f7",
                short     : "${ChatColor.of("#c0a1f7")}Snowy§r",
                scoreboard: "${ChatColor.of("#c0a1f7")}Snowy§r",
                long      : "§b§k|§r§8[${ChatColor.of("#c0a1f7")}Snowy§8]§b§k|§r",
                custom    : '㐅'
        ],
        "group.donator.chilly"      : [
                priority  : 75,
                color     : "#4f80ff",
                short     : "${ChatColor.of("#4f80ff")}Chilly§r",
                scoreboard: "${ChatColor.of("#4f80ff")}Chilly§r",
                long      : "§b§k|§r§8[${ChatColor.of("#4f80ff")}Chilly§8]§b§k|§r",
                custom    : '㐆'
        ],
        "group.donator.crisp"       : [
                priority  : 25,
                color     : "#85c2ff",
                short     : "${ChatColor.of("#85c2ff")}Crisp§r",
                scoreboard: "${ChatColor.of("#85c2ff")}Crisp§r",
                long      : "§b§k|§r§8[${ChatColor.of("#85c2ff")}Crisp§8]§b§k|§r",
                custom    : '㐇'
        ],


        "group.donator.beachbumplus": [
                priority  : 445,
                color     : "#d1d1d1",
                short     : "§7§lBeachBum+§r",
                scoreboard: "§7§lBeachBum+§r",
                long      : "§8[§7BeachBum+§8]§r",
                custom    : '乃'
        ],
        "group.donator.rat": [
                priority  : 444,
                color     : "#d1d1d1",
                short     : "§9§lRat§r",
                scoreboard: "§9§lRat§r",
                long      : "§8[§9Rat§8]§r",
                custom    : '水'
        ],
        "group.donator.wsa": [
                priority  : 443,
                color     : "#d1d1d1",
                short     : "§1§lMother§r",
                scoreboard: "§1§lMother§r",
                long      : "§8[§cMother§8]§r",
                custom    : '字'
        ],
        "group.donator.eagle": [
                priority  : 442,
                color     : "#d1d1d1",
                short     : "§c§lEagle§r",
                scoreboard: "§c§lEagle§r",
                long      : "§8[§cEagle§8]§r",
                custom    : '目'
        ],
        "group.donator.ispy": [
                priority  : 441,
                color     : "#d1d1d1",
                short     : "§d§lISPY§r",
                scoreboard: "§d§lISPY§r",
                long      : "§8[§6ISPY§8]§r",
                custom    : '氿'
        ],
        "group.donator.2kids": [
                priority  : 440,
                color     : "#d1d1d1",
                short     : "§f§l2Kids1Wife§r",
                scoreboard: "§f§l2Kids1Wife§r",
                long      : "§8[§82Kids1Wife§8]§r",
                custom    : '氵'
        ],
        "group.donator.dr": [
                priority  : 439,
                color     : "#d1d1d1",
                short     : "§4§lDr.§r",
                scoreboard: "§4§lDr.§r",
                long      : "§8[§8Dr.§8]§r",
                custom    : '氶'
        ],
        "group.donator.notsr": [
                priority  : 438,
                color     : "#d1d1d1",
                short     : "§5§lNot Sr.Mod§r",
                scoreboard: "§5§lNot Sr.Mod§r",
                long      : "§8[§8Not Sr.Mod§8]§r",
                custom    : '氷'
        ],
        "group.donator.posplusplus": [
                priority  : 437,
                color     : "#d1d1d1",
                short     : "§c§lPoseidon++§r",
                scoreboard: "§c§lPoseidon++§r",
                long      : "§8[§8Poseidon++§8]§r",
                custom    : '永'
        ],
        "group.donator.monarch": [
                priority  : 436,
                color     : "#d1d1d1",
                short     : "§5§lMonarch§r",
                scoreboard: "§5§lMonarch§r",
                long      : "§8[§8Monarch§8]§r",
                custom    : '氹'
        ],
        "group.donator.zeusplusgodly": [
                priority  : 435,
                color     : "#d1d1d1",
                short     : "§6§lZeus+§r",
                scoreboard: "§6§lZeus+§r",
                long      : "§8[§8Zeus+§8]§r",
                custom    : '氺'
        ],
        "group.donator.youtubegodly": [
                priority  : 434,
                color     : "#d1d1d1",
                short     : "§c§lYouTube§r",
                scoreboard: "§c§lYouTube§r",
                long      : "§8[§8YouTube§8]§r",
                custom    : '氻'
        ],
        "group.donator.softanwet": [
                priority  : 433,
                color     : "#d1d1d1",
                short     : "§b§lSoft&Wet§r",
                scoreboard: "§b§lSoft&Wet§r",
                long      : "§8[§8Soft&Wet§8]§r",
                custom    : '氼'
        ],
        "group.donator.olympus": [
                priority  : 432,
                color     : "#d1d1d1",
                short     : "§c§lOlympus§r",
                scoreboard: "§c§lOlympus§r",
                long      : "§8[§8Olympus§8]§r",
                custom    : '氽'
        ],
        "group.donator.devouerer": [
                priority  : 431,
                color     : "#d1d1d1",
                short     : "§f§lDevouerer§r",
                scoreboard: "§f§lDevouerer§r",
                long      : "§8[§8Devouerer§8]§r",
                custom    : '氾'
        ],
        "group.donator.retired": [
                priority  : 430,
                color     : "#d1d1d1",
                short     : "§d§lRetired§r",
                scoreboard: "§d§lRetired§r",
                long      : "§8[§6Retired§8]§r",
                custom    : '汀'
        ],
        "group.donator.rizzl3": [
                priority  : 429,
                color     : "#d1d1d1",
                short     : "§d§lRizzl3§r",
                scoreboard: "§d§lRizzl3§r",
                long      : "§8[§6Rizzl3§8]§r",
                custom    : '汁'
        ],
        "group.donator.surgeon": [
                priority  : 428,
                color     : "#d1d1d1",
                short     : "§7§lSurgeon§r",
                scoreboard: "§7§lSurgeon§r",
                long      : "§8[§6Surgeon§8]§r",
                custom    : '求'
        ],
        "group.donator.peanut": [
                priority  : 427,
                color     : "#d1d1d1",
                short     : "§d§lPeanut§r",
                scoreboard: "§d§lPeanut§r",
                long      : "§8[§6Peanut§8]§r",
                custom    : '汃'
        ],
        "group.donator.youandi": [
                priority  : 426,
                color     : "#d1d1d1",
                short     : "§d§lYou&I§r",
                scoreboard: "§d§lYou&I§r",
                long      : "§8[§6You&I§8]§r",
                custom    : '汄'
        ],
        "group.donator.chigrank": [
                priority  : 425,
                color     : "#d1d1d1",
                short     : "§d§lChig§r",
                scoreboard: "§d§lChig§r",
                long      : "§8[§6Chig§8]§r",
                custom    : '汅'
        ],
        "group.donator.sosgodly": [
                priority  : 425,
                color     : "#d1d1d1",
                short     : "§f§lSOS§r",
                scoreboard: "§f§lSOS§r",
                long      : "§8[§6SOS§8]§r",
                custom    : '汇'
        ],
        "group.donator.failuregodly": [
                priority  : 424,
                color     : "#d1d1d1",
                short     : "§f§lFailure§r",
                scoreboard: "§f§lFailure§r",
                long      : "§8[§6Failure§8]§r",
                custom    : '汈'
        ],
        "group.donator.gtgodly": [
                priority  : 423,
                color     : "#d1d1d1",
                short     : "§f§lGT§r",
                scoreboard: "§f§lGT§r",
                long      : "§8[§6GT§8]§r",
                custom    : '汉'
        ],
        "group.donator.wintergodly": [
                priority  : 422,
                color     : "#d1d1d1",
                short     : "§f§lWinter§r",
                scoreboard: "§f§lWinter§r",
                long      : "§8[§6Winter§8]§r",
                custom    : '汊'
        ],
        "group.donator.egirlgodly": [
                priority  : 420,
                color     : "#d1d1d1",
                short     : "§f§leGirl§r",
                scoreboard: "§f§leGirl§r",
                long      : "§8[§6eGirl§8]§r",
                custom    : '汋'
        ],
        "group.donator.brokegodly": [
                priority  : 419,
                color     : "#d1d1d1",
                short     : "§f§lBroke§r",
                scoreboard: "§f§lBroke§r",
                long      : "§8[§6Broke§8]§r",
                custom    : '汌'
        ],
        "group.donator.roargodly": [
                priority  : 418,
                color     : "#d1d1d1",
                short     : "§f§lRoar§r",
                scoreboard: "§f§lRoar§r",
                long      : "§8[§6Roar§8]§r",
                custom    : '汍'
        ],
        "group.donator.thordadgodly": [
                priority  : 417,
                color     : "#d1d1d1",
                short     : "§f§lThordad§r",
                scoreboard: "§f§lThordad§r",
                long      : "§8[§6Thordad§8]§r",
                custom    : '汎'
        ],
        "group.donator.georgegodly": [
                priority  : 416,
                color     : "#d1d1d1",
                short     : "§f§lGeorge§r",
                scoreboard: "§f§lGeorge§r",
                long      : "§8[§6George§8]§r",
                custom    : '汏'
        ],
        "group.donator.jitgodly": [
                priority  : 415,
                color     : "#d1d1d1",
                short     : "§f§lJustInTime§r",
                scoreboard: "§f§lJustInTime§r",
                long      : "§8[§6JustInTime§8]§r",
                custom    : '汐'
        ],
        "group.donator.weebplusplusgodly": [
                priority  : 414,
                color     : "#d1d1d1",
                short     : "§f§lWeeb++§r",
                scoreboard: "§f§lWeeb++§r",
                long      : "§8[§6Weeb++§8]§r",
                custom    : '汑'
        ],
        "group.donator.memelordgodly": [
                priority  : 413,
                color     : "#d1d1d1",
                short     : "§f§lMemelord§r",
                scoreboard: "§f§lMemelord§r",
                long      : "§8[§6Memelord§8]§r",
                custom    : '汒'
        ],
        "group.donator.poseidonplus": [
                priority  : 401,
                color     : "#a1ffff",
                short     : "§bP§9O§3S§bE§9I§3D§bO§9N§3+§r",
                scoreboard: "§b§lPoseidon§r+",
                long      : "§3§k|§r§8[§b§lP§9§lO§3§lS§b§lE§9§lI§3§lD§b§lO§9§lN§3+§8]§3§k|§r",
                custom    : '丹'
        ],
        "group.donator.poseidon"    : [
                priority  : 400,
                color     : "#a1ffff",
                short     : "§bP§9O§3S§bE§9I§3D§bO§9N§r",
                scoreboard: "§b§lPoseidon",
                long      : "§3§k|§r§8[§b§lP§9§lO§3§lS§b§lE§9§lI§3§lD§b§lO§9§lN§8]§3§k|§r",
                custom    : '为'
        ],
        "group.donator.medusa"      : [
                priority  : 350,
                color     : "#9cff9c",
                short     : "§a§lM§2§le§a§ld§2§lu§a§ls§2§la§r",
                scoreboard: "§aMedusa",
                long      : "§a§k|§r§8[§a§lM§2§lE§a§lD§2§lU§a§lS§2§lA§8]§a§k|§r",
                custom    : '主'
        ],
        "group.donator.admiral"     : [
                priority  : 300,
                color     : "#4debeb",
                short     : "§3A§cD§3M§bI§3R§cA§3L§r",
                scoreboard: "§3Admiral",
                long      : "§3§k|§r§8[§3§lA§c§lD§3§lM§B§lI§3§lR§c§lA§3§lL§8]§3§k|§r",
                custom    : '丼'
        ],
        "group.donator.pirate"      : [
                priority  : 250,
                color     : "#ffc859",
                short     : "§6P§3i§6r§3a§6t§3e§r",
                scoreboard: "§6Pirate",
                long      : "§a§k|§r§8[§6§lP§3§li§6§lr§3§la§6§lt§3§le§8]§a§k|§r",
                custom    : '丽'
        ],
        "group.donator.captain"     : [
                priority  : 200,
                color     : "#ff99ff",
                short     : "§dCaptain§r",
                scoreboard: "§dCaptain§r",
                long      : "§d§k|§r§8[§dCaptain§8]§d§k|§r",
                custom    : '举'
        ],
        "group.donator.sailor"      : [
                priority  : 150,
                color     : "#ffff8f",
                short     : "§eSailor§r",
                scoreboard: "§eSailor§r",
                long      : "§e§k|§r§8[§eSailor§8]§6§k|§r",
                custom    : '丿'
        ],
        "group.donator.diver"       : [
                priority  : 100,
                color     : "#8080ff",
                short     : "§9Diver§r",
                scoreboard: "§9Diver§r",
                long      : "§9§k|§r§8[§9Diver§8]§9§k|§r",
                custom    : '乀'
        ],
        "group.donator.surfer"      : [
                priority  : 50,
                color     : "#a1ffff",
                short     : "§bSurfer§r",
                scoreboard: "§bSurfer§r",
                long      : "§b§k|§r§8[§bSurfer§8]§b§k|§r",
                custom    : '乁'
        ],
])

if (Temple.templeId.startsWith("arctic")) {
        ArkGroups.addGroups([
                "default": [
                        priority  : 0,
                        color     : "#85e9ff",
                        short     : "${ChatColor.of("#85e9ff")}Snowflake§r",
                        scoreboard: "${ChatColor.of("#85e9ff")}Snowflake§r",
                        long      : "§b§k|§r§8[${ChatColor.of("#85e9ff")}Snowflake§8]§b§k|§r",
                        custom    : '㐈'
                ]
        ])
} else {
        ArkGroups.addGroups([
                "default": [
                        priority  : 0,
                        color     : "#d1d1d1",
                        short     : "§7BeachBum§r",
                        scoreboard: "§7BeachBum§r",
                        long      : "§8[§7BeachBum§8]§r",
                        custom    : '乂'
                ]
        ])
}

(Exports.ptr("arktheme/refreshgroups") as Closure).call()
(Exports.ptr("arktags/refreshgroups") as Closure).call()