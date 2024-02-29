package scripts.exec

import groovy.transform.Field
import scripts.shared3.ArkGpt

//gpt {uuid} {transaction} example
@Field String message = "grbc prod §:cstore §> §b{username} §fhas made a purchase on our store! §bstore.starcade.org"
@Field String message2 = "grbc prod §:csale §> §b{username} §fhas saved §b60% §fon our store during our flash sale! §bstore.starcade.org"

//"surfer_rank_prison": [
//                servers: ["atlantic"], //executed on nexus otherwise, if missing require online is ignored, if multiple specified, redeemable on both servers.
//                requireonline: false,
//                cmds: ["padd {uuid} atlantic/group.donator.surfer", message, message2],
//                repeat: 3,
//                repeatdelay: 60,
//                expire: 60,
//                expirecmds: ["msg {username} you have expired!"]
//],

ArkGpt.addPackages([
        "autominer_5_pacific"            : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["padd {uuid} pacific/autominerlimit.5", message, message2],
        ],
        "autominer_5_atlantic"           : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["padd {uuid} atlantic/autominerlimit.5", message, message2],
        ],
        "autominer_4_pacific"            : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["padd {uuid} pacific/autominerlimit.4", message, message2],
        ],
        "autominer_4_atlantic"           : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["padd {uuid} atlantic/autominerlimit.4", message, message2],
        ],
        "autominer_3_pacific"            : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["padd {uuid} pacific/autominerlimit.3", message, message2],
        ],
        "autominer_3_atlantic"           : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["padd {uuid} atlantic/autominerlimit.3", message, message2],
        ],
        "autominer_2_pacific"            : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["padd {uuid} pacific/autominerlimit.2", message, message2],
        ],
        "autominer_2_atlantic"           : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["padd {uuid} atlantic/autominerlimit.2", message, message2],
        ],

        "battlepass_pacific"             : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givepremiumpass {username}", message, message2],
        ],

        "battlepass_atlantic"            : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givepremiumpass {username}", message, message2],
        ],

        "surfer_rank_atlantic"           : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["padd {uuid} atlantic/group.donator.surfer", message, message2],
        ],
        "diver_rank_atlantic"            : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["padd {uuid} atlantic/group.donator.diver", message, message2],
        ],
        "sailor_rank_atlantic"           : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["padd {uuid} atlantic/group.donator.sailor", message, message2],
        ],
        "captain_rank_atlantic"          : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["padd {uuid} atlantic/group.donator.captain", message, message2],
        ],
        "pirate_rank_atlantic"           : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["padd {uuid} atlantic/group.donator.pirate", message, message2],
        ],
        "admiral_rank_atlantic"          : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["padd {uuid} atlantic/group.donator.admiral", message, message2],
        ],
        "medusa_rank_atlantic"           : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["padd {uuid} atlantic/group.donator.medusa", message, message2],
        ],
        "surfer_diver_rankup_atlantic"   : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["cpadd {uuid} atlantic/group.donator.surfer atlantic/group.donator.diver",
                                "cpadd {uuid} atlantic4/group.donator.surfer atlantic/group.donator.diver", message, message2],
        ],
        "diver_sailor_rankup_atlantic"   : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["cpadd {uuid} atlantic/group.donator.diver atlantic/group.donator.sailor",
                                "cpadd {uuid} atlantic4/group.donator.diver atlantic/group.donator.sailor", message, message2],
        ],
        "sailor_captain_rankup_atlantic" : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["cpadd {uuid} atlantic/group.donator.sailor atlantic/group.donator.captain",
                                "cpadd {uuid} atlantic4/group.donator.sailor atlantic/group.donator.captain", message, message2],
        ],
        "captain_pirate_rankup_atlantic" : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["cpadd {uuid} atlantic/group.donator.captain atlantic/group.donator.pirate",
                                "cpadd {uuid} atlantic4/group.donator.captain atlantic/group.donator.pirate", message, message2],
        ],
        "pirate_admiral_rankup_atlantic" : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["cpadd {uuid} atlantic/group.donator.pirate atlantic/group.donator.admiral",
                                "cpadd {uuid} atlantic4/group.donator.pirate atlantic/group.donator.admiral", message, message2],
        ],
        "admiral_medusa_rankup_atlantic" : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["cpadd {uuid} atlantic/group.donator.admiral atlantic/group.donator.medusa",
                                "cpadd {uuid} atlantic4/group.donator.admiral atlantic/group.donator.medusa", message, message2],
        ],
        "storm_gkit_atlantic"            : [
                requireonline: false,
                cmds         : ["padd {uuid} atlantic/kits.storm", message, message2],
        ],
        "misty_gkit_atlantic"            : [
                requireonline: false,
                cmds         : ["padd {uuid} atlantic/kits.misty", message, message2],
        ],
        "typhoon_gkit_atlantic"          : [
                requireonline: false,
                cmds         : ["padd {uuid} atlantic/kits.typhoon", message, message2],
        ],
        "lagoon_gkit_atlantic"           : [
                requireonline: false,
                cmds         : ["padd {uuid} atlantic/kits.lagoon", message, message2],
        ],
        "bundle_gkit_atlantic"           : [
                requireonline: false,
                cmds         : [
                        "padd {uuid} atlantic/kits.lagoon",
                        "padd {uuid} atlantic/kits.storm",
                        "padd {uuid} atlantic/kits.misty",
                        "padd {uuid} atlantic/kits.typhoon",
                        message, message2
                ],
        ],
        "5x_lobster_keys_atlantic"       : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givecratekey {username} lobster 5", message, message2],
        ],
        "5x_dolphin_keys_atlantic"       : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givecratekey {username} dolphin 5", message, message2],
        ],
        "5x_shark_keys_atlantic"         : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givecratekey {username} shark 5", message, message2],
        ],
        "5x_whale_keys_atlantic"         : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givecratekey {username} whale 5", message, message2],
        ],
        "5x_trident_keys_atlantic"       : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givecratekey {username} trident 5", message, message2],
        ],
        "1x_monthly_keys_atlantic"       : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givecratekey {username} monthly 1", message, message2],
        ],
        "5x_skin_keys_atlantic"          : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givecratekey {username} skin 5", message, message2],
        ],
        "1x_robot_keys_atlantic"         : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givecratekey {username} robot 1", message, message2],
        ],
        "1x_pet_keys_atlantic"           : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givecrate {username} pet 1", message, message2],
        ],
        "30_premium_atlantic"            : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givepremium {username} 30d", message, message2],
        ],
        "60_premium_atlantic"            : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givepremium {username} 60d", message, message2],
        ],
        "90_premium_atlantic"            : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givepremium {username} 90d", message, message2],
        ],
        "customtitle_atlantic"           : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitlecredit {username} 1", message, message2],
        ],
        "egirlf1nn_atlantic"             : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &d#EGIRLF1NN", message, message2],
        ],
        "teamcandy_atlantic"             : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &5TeamCandy", message, message2],
        ],
        "god_atlantic"                   : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &aGod", message, message2],
        ],
        "memes_atlantic"                 : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &aMemes", message, message2],
        ],
        "salty_atlantic"                 : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &asalty", message, message2],
        ],
        "moneyman_atlantic"              : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &aMoneyMan", message, message2],
        ],
        "ez_atlantic"                    : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &aEz", message, message2],
        ],
        "pvper_atlantic"                 : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &aPvPer", message, message2],
        ],
        "p2w_atlantic"                   : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &eP2W", message, message2],
        ],
        "hax_atlantic"                   : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &eHax", message, message2],
        ],
        "tryhard_atlantic"               : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &eTryHard", message, message2],
        ],
        "destroyer_atlantic"             : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &eDestroyer", message, message2],
        ],
        "grinder_atlantic"               : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &dGrinder", message, message2],
        ],
        "winner_atlantic"                : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &dWinner", message, message2],
        ],
        "loser_atlantic"                 : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &dLoser", message, message2],
        ],
        "umad_atlantic"                  : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &dUMad?", message, message2],
        ],
        "heart_atlantic"                 : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &6<3", message, message2],
        ],
        "mineman_atlantic"               : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &6Mineman", message, message2],
        ],
        "cheater_atlantic"               : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &6Cheater", message, message2],
        ],
        "exploiter_atlantic"             : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &6Exploiter", message, message2],
        ],
        "yikes_atlantic"                 : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &6Yikes", message, message2],
        ],
        "toxic_atlantic"                 : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &6Toxic", message, message2],
        ],
        "gamer_atlantic"                 : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &6Gamer", message, message2],
        ],
        //new titles
        "i_love_f1nnster_pacific_title"  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &b&lI &e&l<3 &b&lF1nnster", message, message2],
        ],
        "i_love_candyman_pacific_title"  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &5&lI &e&l<3 &5&lCandyman", message, message2]
        ],
        "i_love_ikoalas_pacific_title"   : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &d&lI &e&l<3 &d&liKoalas", message, message2]
        ],
        "i_love_serayne92_pacific_title" : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &4&lI &e&l<3 &4&lSerayne", message, message2]
        ],
        "heart_pacific_title"            : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &&<3", message, message2]
        ],
        "mineman_pacific_title"          : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &b&lMine&3&lMan", message, message2]
        ],
        "robotic_pacific_title"          : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &6&lR&e&lo&6&lb&e&lo&6&lt&e&li&6&lc", message, message2]
        ],
        "gift_card_trader_pacific_title" : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &f&lG&7&li&8&lf&f&lt &7&lC&8&la&f&lr&7&ld &f&lT&7&lr&8&la&f&ld&7&le&8&lr", message, message2]
        ],
        "legend_pacific_title"           : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &6&lLegend", message, message2]
        ],
        "no_lifer_pacific_title"         : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &4&lNo &1&lLifer", message, message2]
        ],
        "treasure_hunter_pacific_title"  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &6&lT&e&lr&f&le&6&la&e&ls&f&lu&6&lr&e&le &f&lH&6&lu&e&ln&f&lt&6&le&e&lr", message, message2]
        ],
        "mcprison_pacific_title"         : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &3&lStar&b&lcade", message, message2]
        ],
        "trader_pacific_title"           : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &2&lT&a&lr&2&la&a&ld&2&le&a&lr", message, message2]
        ],
        "atlantic_on_top_pacific_title"  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &9&lA&e&lt&9&ll&e&la&9&ln&e&lt&9&li&e&lc &9&lO&e&ln &9&lT&e&lo&9&lp", message, message2]
        ],
        "pacific_on_top_pacific_title"   : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &b&lP&d&la&b&lc&d&li&b&lf&d&li&b&lc &d&lO&b&ln &d&lT&b&lo&d&lp", message, message2]
        ],
        "coin_flip_me_pacific_title"     : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &6&lC&f&lo&6&li&f&ln &6&lF&f&ll&6&li&f&lp &6&lM&f&le", message, message2]
        ],
        "pvp_god_pacific_title"          : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &4&lP&c&lV&4&lP &c&lG&4&lo&c&ld", message, message2]
        ],
        "uwu_pacific_title"              : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &&UwU", message, message2]
        ],
        "smiley_pacific_title"           : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &&:D", message, message2]
        ],

        "i_love_f1nnster_atlantic_title" : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &b&lI &e&l<3 &b&lF1nnster", message, message2],
        ],
        "i_love_candyman_atlantic_title" : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &5&lI &e&l<3 &5&lCandyman", message, message2]
        ],
        "i_love_ikoalas_atlantic_title"  : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &d&lI &e&l<3 &d&liKoalas", message, message2]
        ],
        "i_love_serayne92_atlantic_title": [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &4&lI &e&l<3 &4&lSerayne", message, message2]
        ],
        "heart_atlantic_title"           : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &&<3", message, message2]
        ],
        "mineman_atlantic_title"         : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &b&lMine&3&lMan", message, message2]
        ],
        "robotic_atlantic_title"         : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &6&lR&e&lo&6&lb&e&lo&6&lt&e&li&6&lc", message, message2]
        ],
        "gift_card_trader_atlantic_title": [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &f&lG&7&li&8&lf&f&lt &7&lC&8&la&f&lr&7&ld &f&lT&7&lr&8&la&f&ld&7&le&8&lr", message, message2]
        ],
        "legend_atlantic_title"          : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &6&lLegend", message, message2]
        ],
        "no_lifer_atlantic_title"        : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &4&lNo &1&lLifer", message, message2]
        ],
        "treasure_hunter_atlantic_title" : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &6&lT&e&lr&f&le&6&la&e&ls&f&lu&6&lr&e&le &f&lH&6&lu&e&ln&f&lt&6&le&e&lr", message, message2]
        ],
        "mcprison_atlantic_title"        : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &3&lStar&b&lcade", message, message2]
        ],
        "trader_atlantic_title"          : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &2&lT&a&lr&2&la&a&ld&2&le&a&lr", message, message2]
        ],
        "atlantic_on_top_atlantic_title" : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &9&lA&e&lt&9&ll&e&la&9&ln&e&lt&9&li&e&lc &9&lO&e&ln &9&lT&e&lo&9&lp", message, message2]
        ],
        "pacific_on_top_atlantic_title"  : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &b&lP&d&la&b&lc&d&li&b&lf&d&li&b&lc &d&lO&b&ln &d&lT&b&lo&d&lp", message, message2]
        ],
        "coin_flip_me_atlantic_title"    : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &6&lC&f&lo&6&li&f&ln &6&lF&f&ll&6&li&f&lp &6&lM&f&le", message, message2]
        ],
        "pvp_god_atlantic_title"         : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &4&lP&c&lV&4&lP &c&lG&4&lo&c&ld", message, message2]
        ],
        "uwu_atlantic_title"             : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &&UwU", message, message2]
        ],
        "smiley_atlantic_title"          : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : ["givetitle {username} &&:D", message, message2]
        ],


        "surfer_rank_pacific"            : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["padd {uuid} pacific/group.donator.surfer", message, message2],
        ],
        "diver_rank_pacific"             : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["padd {uuid} pacific/group.donator.diver", message, message2],
        ],
        "sailor_rank_pacific"            : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["padd {uuid} pacific/group.donator.sailor", message, message2],
        ],
        "captain_rank_pacific"           : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["padd {uuid} pacific/group.donator.captain", message, message2],
        ],
        "pirate_rank_pacific"            : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["padd {uuid} pacific/group.donator.pirate", message, message2],
        ],
        "admiral_rank_pacific"           : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["padd {uuid} pacific/group.donator.admiral", message, message2],
        ],
        "medusa_rank_pacific"            : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["padd {uuid} pacific/group.donator.medusa", message, message2],
        ],
        "surfer_diver_rankup_pacific"    : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["cpadd {uuid} pacific/group.donator.surfer pacific/group.donator.diver", message, message2],
        ],
        "diver_sailor_rankup_pacific"    : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["cpadd {uuid} pacific/group.donator.diver pacific/group.donator.sailor", message, message2],
        ],
        "sailor_captain_rankup_pacific"  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["cpadd {uuid} pacific/group.donator.sailor pacific/group.donator.captain", message, message2],
        ],
        "captain_pirate_rankup_pacific"  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["cpadd {uuid} pacific/group.donator.captain pacific/group.donator.pirate", message, message2],
        ],
        "pirate_admiral_rankup_pacific"  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["cpadd {uuid} pacific/group.donator.pirate pacific/group.donator.admiral", message, message2],
        ],
        "admiral_medusa_rankup_pacific"  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["cpadd {uuid} pacific/group.donator.admiral pacific/group.donator.medusa", message, message2],
        ],
        "storm_gkit_pacific"             : [
                requireonline: false,
                cmds         : ["padd {uuid} pacific/kits.storm", message, message2],
        ],
        "misty_gkit_pacific"             : [
                requireonline: false,
                cmds         : ["padd {uuid} pacific/kits.misty", message, message2],
        ],
        "typhoon_gkit_pacific"           : [
                requireonline: false,
                cmds         : ["padd {uuid} pacific/kits.typhoon", message, message2],
        ],
        "lagoon_gkit_pacific"            : [
                requireonline: false,
                cmds         : ["padd {uuid} pacific/kits.lagoon", message, message2],
        ],
        "bundle_gkit_pacific"            : [
                requireonline: false,
                cmds         : [
                        "padd {uuid} pacific/kits.lagoon",
                        "padd {uuid} pacific/kits.storm",
                        "padd {uuid} pacific/kits.misty",
                        "padd {uuid} pacific/kits.typhoon",
                        message, message2
                ],
        ],
        "5x_lobster_keys_pacific"        : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givecratekey {username} lobster 5", message, message2],
        ],
        "5x_dolphin_keys_pacific"        : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givecratekey {username} dolphin 5", message, message2],
        ],
        "5x_shark_keys_pacific"          : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givecratekey {username} shark 5", message, message2],
        ],
        "5x_whale_keys_pacific"          : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givecratekey {username} whale 5", message, message2],
        ],
        "5x_trident_keys_pacific"        : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givecratekey {username} trident 5", message, message2],
        ],
        "1x_monthly_keys_pacific"        : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givecratekey {username} monthly 1", message, message2],
        ],
        "private_mine"                   : [
                servers      : ["pacificdev"],
                requireonline: true,
                cmds         : ["givepmine {username} premium", message, message2],
        ],
        "5x_skins_keys_pacific"          : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givecratekey {username} skin 5", message, message2],
        ],
        "1x_robot_keys_pacific"          : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givecratekey {username} robot 1", message, message2],
        ],
        "1x_pet_keys_pacific"            : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givecrate {username} pet 1", message, message2],
        ],
        "30_premium_pacific"             : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givepremium {username} 30d", message, message2],
        ],
        "60_premium_pacific"             : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givepremium {username} 60d", message, message2],
        ],
        "90_premium_pacific"             : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givepremium {username} 90d", message, message2],
        ],
        "customtitle_pacific"            : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitlecredit {username} 1", message, message2],
        ],
        "egirlf1nn_pacific"              : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &d#EGIRLF1NN", message, message2],
        ],
        "teamcandy_pacific"              : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &5TeamCandy", message, message2],
        ],
        "god_pacific"                    : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &aGod", message, message2],
        ],
        "memes_pacific"                  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &aMemes", message, message2],
        ],
        "salty_pacific"                  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &asalty", message, message2],
        ],
        "moneyman_pacific"               : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &aMoneyMan", message, message2],
        ],
        "ez_pacific"                     : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &aEz", message, message2],
        ],
        "pvper_pacific"                  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &aPvPer", message, message2],
        ],
        "p2w_pacific"                    : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &eP2W", message, message2],
        ],
        "hax_pacific"                    : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &eHax", message, message2],
        ],
        "tryhard_pacific"                : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &eTryHard", message, message2],
        ],
        "destroyer_pacific"              : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &eDestroyer", message, message2],
        ],
        "grinder_pacific"                : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &dGrinder", message, message2],
        ],
        "winner_pacific"                 : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &dWinner", message, message2],
        ],
        "loser_pacific"                  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &dLoser", message, message2],
        ],
        "umad_pacific"                   : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &dUMad?", message, message2],
        ],
        "heart_pacific"                  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &6<3", message, message2],
        ],
        "mineman_pacific"                : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &6Mineman", message, message2],
        ],
        "cheater_pacific"                : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &6Cheater", message, message2],
        ],
        "exploiter_pacific"              : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &6Exploiter", message, message2],
        ],
        "yikes_pacific"                  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &6Yikes", message, message2],
        ],
        "toxic_pacific"                  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &6Toxic", message, message2],
        ],
        "gamer_pacific"                  : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : ["givetitle {username} &6Gamer", message, message2],
        ],
        "bundle_christmas_global"        : [
                servers      : ["pacific5", "pacific6", "atlantic6", "atlantic7"],
                requireonline: false,
                cmds         : [
                        "padd {uuid} kits.christmas",
                        "givepresent {username} 1 1",
                        "givepresent {username} 2 1",
                        "givepresent {username} 3 1",
                        message, message2
                ],
        ],
        "bundle_christmas_pacific"       : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} kits.christmas",
                        "givepresent {username} 1 1",
                        "givepresent {username} 2 1",
                        "givepresent {username} 3 1",
                        message, message2
                ],
        ],
        "bundle_christmas_pacificdev"    : [
                servers      : ["pacificdev"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} kits.christmas",
                        "givepresent {username} 1 1",
                        "givepresent {username} 2 1",
                        "givepresent {username} 3 1",
                        message, message2
                ],
        ],
        "bundle_christmas_atlantic"      : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} kits.christmas",
                        "givepresent {username} 1 1",
                        "givepresent {username} 2 1",
                        "givepresent {username} 3 1",
                        message, message2
                ],
        ],
        "bundle_newyears_atlantic"       : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} atlantic/kits.newyears",
                        "givebar {username} champagne 10",
                        "essentials:give {username} cake 1 name:&6&lNew_Years_2021",
                        "givefireworks {username} 64",
                        "padd {uuid} atlantic/fireworkshow.use",
                        message, message2
                ],
        ],
        "bundle_newyears_pacific"        : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} pacific/kits.newyears",
                        "givebar {username} champagne 10",
                        "essentials:give {username} cake 1 name:&6&lNew_Years_2021",
                        "givefireworks {username} 64",
                        "padd {uuid} pacific/fireworkshow.use",
                        message, message2
                ],
        ],
        "bundle_newyears_pacificdev"     : [
                servers      : ["pacificdev"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} pacific/kits.newyears",
                        "givebar {username} champagne 10",
                        "essentials:give {username} cake 1 name:&6&lNew_Years_2021",
                        "givefireworks {username} 64",
                        "padd {uuid} pacific/fireworkshow.use",
                        message, message2
                ],
        ],
        "bundle_valentines_atlantic"     : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} atlantic/kits.valentines",
                        "givetulip {username} 10",
                        message, message2
                ],
        ],
        "bundle_valentines_pacific"      : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} pacific/kits.valentines",
                        "givetulip {username} 5",
                        message, message2
                ],
        ],
        "bundle_easter_pacific"          : [
                servers      : ["pacific5", "pacific6"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} pacific/rabbitcannon.use",
                        "padd {uuid} pacific/kits.easter",
                        "giveeasteregg {username} 5",
                        "givepetvariant {username} rabbit common 1",
                        message, message2
                ],
        ],
        "bundle_easter_atlantic"         : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} atlantic/rabbitcannon.use",
                        "padd {uuid} atlantic/kits.easter",
                        "giveeasteregg {username} 10",
                        "givepetvariant {username} rabbit common 1",
                        message, message2
                ],
        ],
        "bundle_independance_pacific"    : [
                servers      : ["pacific6"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} pacific/kits.independance",
                        "givebar {username} bourbon 10",
                        "padd {uuid} pacific/fireworkevent.use",
                        message, message2
                ],
        ],
        "bundle_independance_atlantic"   : [
                servers      : ["atlantic6", "atlantic7"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} atlantic/kits.independance",
                        "givebar {username} champagne 10",
                        "padd {uuid} atlantic/fireworkevent.use",
                        message, message2
                ],
        ],

        "200_gems"                       : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "gemsgive {username} tradable 200",
                        "keyall dolphin",
                        "keyall shark",
                        "grbc prod §:cstore §> §b{username} §fhas purchased §b200 Gems §fon our store! §bstore.starcade.org"
                ],
        ],
        "500_gems"                       : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "gemsgive {username} tradable 500",
                        "keyall shark",
                        "keyall shark",
                        "grbc prod §:cstore §> §b{username} §fhas purchased §b500 Gems §fon our store! §bstore.starcade.org"
                ],
        ],
        "1100_gems"                      : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "gemsgive {username} tradable 1100",
                        "keyall shark",
                        "keyall whale",
                        "grbc prod §:cstore §> §b{username} §fhas purchased §b1100 Gems §fon our store! §bstore.starcade.org"
                ],
        ],
        "2400_gems"                      : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "gemsgive {username} tradable 2400",
                        "keyall whale",
                        "keyall whale",
                        "grbc prod §:cstore §> §b{username} §fhas purchased §b2400 Gems §fon our store! §bstore.starcade.org"
                ],
        ],
        "5200_gems"                      : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "gemsgive {username} tradable 5200",
                        "keyall whale",
                        "keyall trident",
                        "grbc prod §:cstore §> §b{username} §fhas purchased §b5200 Gems §fon our store! §bstore.starcade.org"
                ],
        ],
        "9500_gems"                      : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "gemsgive {username} tradable 9500",
                        "keyall trident",
                        "keyall trident",
                        "grbc prod §:cstore §> §b{username} §fhas purchased §b9500 Gems §fon our store! §bstore.starcade.org"
                ],
        ],
        "premium"                        : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "givepremium {username} 30d",
                        "grbc prod §:cstore §> §b{username} §fhas purchased §b30 days of premium §fon our store! §bstore.starcade.org"
                ],
        ],
        "lagoon_add"                     : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} atlantic/kits.lagoon_gkit",
                        "grbc prod §:cstore §> §b{username} §fhas purchased §b30 days of Lagoon GKit §fon our store! §bstore.starcade.org"
                ],
        ],
        "lagoon_remove"                  : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "prem {uuid} atlantic/kits.lagoon_gkit",
                ],
        ],
        "misty_add"                      : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} atlantic/kits.misty_gkit",
                        "grbc prod §:cstore §> §b{username} §fhas purchased §b30 days of Misty GKit §fon our store! §bstore.starcade.org"
                ],
        ],
        "misty_remove"                   : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "prem {uuid} atlantic/kits.misty_gkit",
                ],
        ],
        "storm_add"                      : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} atlantic/kits.storm_gkit",
                        "grbc prod §:cstore §> §b{username} §fhas purchased §b30 days of Storm GKit §fon our store! §bstore.starcade.org"
                ],
        ],
        "storm_remove"                   : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "prem {uuid} atlantic/kits.storm_gkit",
                ],
        ],
        "typhoon_add"                    : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} atlantic/kits.typhoon_gkit",
                        "grbc prod §:cstore §> §b{username} §fhas purchased §b30 days of Typhoon GKit §fon our store! §bstore.starcade.org"
                ],
        ],
        "typhoon_remove"                 : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "prem {uuid} atlantic/kits.typhoon_gkit",
                ],
        ],
        "gkit_add"                       : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} atlantic/kits.typhoon",
                        "grbc prod §:cstore §> §b{username} §fhas purchased §b30 days of Typhoon GKit §fon our store! §bstore.starcade.org"
                ],
        ],
        "gkit_remove"                    : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "prem {uuid} atlantic/kits.typhoon",
                ],
        ],
        "supporter_add"                  : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} atlantic/kits.supporter",
                        "givetitle {username} §d§k§l!§r§b§lSupporter§d§k§l!",
                        "keyall whale",
                        "keyall whale",
                        "grbc prod §:cstore §> §b{username} §fhas subscribed as a Supporter §fto our store! §bstore.starcade.org"
                ],
        ],
        "supporter_remove"               : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "prem {uuid} atlantic/kits.supporter",
                ],
        ],
        "supersupporter_add"             : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} atlantic/kits.supersupporter",
                        "givetitle {username} §d§k§l!§r§b§lSupporter+§d§k§l!",
                        "keyall whale",
                        "keyall trident",
                        "grbc prod §:cstore §> §b{username} §fhas subscribed as a Supporter+ §fto our store! §bstore.starcade.org"
                ],
        ],
        "supersupporter_remove"          : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "prem {uuid} atlantic/kits.supersupporter",
                ],
        ],
        "godlysupporter_add"             : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "padd {uuid} atlantic/kits.godlysupporter",
                        "givetitle {username} §d§k§l!§r§b§lGodly§d§k§l!",
                        "keyall trident",
                        "keyall trident",
                        "grbc prod §:cstore §> §b{username} §fhas subscribed as a Godly Supporter §fto our store! §bstore.starcade.org"
                ],
        ],
        "godlysupporter_remove"          : [
                servers      : ["atlantic17", "atlantic18"],
                requireonline: true,
                cmds         : [
                        "prem {uuid} atlantic/kits.godlysupporter",
                ],
        ],
])