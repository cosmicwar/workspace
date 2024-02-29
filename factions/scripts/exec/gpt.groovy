package scripts.exec

import groovy.transform.Field
import scripts.shared.utils.Temple
import scripts.shared3.ArkGpt
//gpt {username} {transaction} example

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
        "200_gold": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17", "pacific7", "pacific8", "sun12", "sun13", "sun10", "sun9", "sun8", "sun7", "cloud5", "cloud6", "survival1", "survival2"],
                requireonline: true,
                cmds: [
                        "coinsgive {username} tradable 200",
                        "grbc prod §2§lSTORE >> §a{username} §fhas purchased §a200 Coins §fon our store! §astore.starcade.org"
                ],
        ],
        "500_gold": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17", "pacific7", "pacific8", "sun12", "sun13", "sun10", "sun9", "sun8", "sun7", "cloud5", "cloud6", "survival1", "survival2"],
                requireonline: true,
                cmds: [
                        "coinsgive {username} tradable 500",
                        "grbc prod §2§lSTORE >> §a{username} §fhas purchased §a500 Coins §fon our store! §astore.starcade.org"
                ],
        ],
        "1100_gold": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17", "pacific7", "pacific8", "sun12", "sun13", "sun10", "sun9", "sun8", "sun7", "cloud5", "cloud6", "survival1", "survival2"],
                requireonline: true,
                cmds: [
                        "coinsgive {username} tradable 1100",
                        "grbc prod §2§lSTORE >> §a{username} §fhas purchased §a1100 Coins §fon our store! §astore.starcade.org"
                ],
        ],
        "2400_gold": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17", "pacific7", "pacific8", "sun12", "sun13", "sun10", "sun9", "sun8", "sun7", "cloud5", "cloud6", "survival1", "survival2"],
                requireonline: true,
                cmds: [
                        "coinsgive {username} tradable 2400",
                        "grbc prod §2§lSTORE >> §a{username} §fhas purchased §a2400 Coins §fon our store! §astore.starcade.org"
                ],
        ],
        "5200_gold": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17", "pacific7", "pacific8", "sun12", "sun13", "sun10", "sun9", "sun8", "sun7", "cloud5", "cloud6", "survival1", "survival2"],
                requireonline: true,
                cmds: [
                        "coinsgive {username} tradable 5200",
                        "grbc prod §2§lSTORE >> §a{username} §fhas purchased §a5200 Coins §fon our store! §astore.starcade.org"
                ],
        ],
        "9500_gold": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17", "pacific7", "pacific8", "sun12", "sun13", "sun10", "sun9", "sun8", "sun7", "cloud5", "cloud6", "survival1", "survival2"],
                requireonline: true,
                cmds: [
                        "coinsgive {username} tradable 9500",
                        "grbc prod §2§lSTORE >> §a{username} §fhas purchased §a9500 Coins §fon our store! §astore.starcade.org"
                ],
        ],
        "premium": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17", "pacific7", "pacific8", "sun12", "sun13", "sun10", "sun9", "sun8", "sun7", "cloud5", "cloud6"],
                requireonline: true,
                cmds: [
                        "givepremium {username} 30d",
                        "grbc prod §2§lSTORE >> §a{username} §fhas purchased §a30 days of premium §fon our store! §astore.starcade.org"
                ],
        ],
        "lagoon_add": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17"],
                requireonline: true,
                cmds: [
                        "padd {uuid} atlantic/kits.lagoon_gkit",
                        "grbc prod §:cstore §> §e{username} §fhas purchased §e30 days of Lagoon GKit §fon our store! §estore.starcade.org"
                ],
        ],
        "lagoon_remove": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17"],
                requireonline: true,
                cmds: [
                        "prem {uuid} atlantic/kits.lagoon_gkit",
                ],
        ],
        "misty_add": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17"],
                requireonline: true,
                cmds: [
                        "padd {uuid} atlantic/kits.misty_gkit",
                        "grbc prod §:cstore §> §e{username} §fhas purchased §e30 days of Misty GKit §fon our store! §estore.starcade.org"
                ],
        ],
        "misty_remove": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17"],
                requireonline: true,
                cmds: [
                        "prem {uuid} atlantic/kits.misty_gkit",
                ],
        ],
        "storm_add": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17"],
                requireonline: true,
                cmds: [
                        "padd {uuid} atlantic/kits.storm_gkit",
                        "grbc prod §:cstore §> §e{username} §fhas purchased §e30 days of Storm GKit §fon our store! §estore.starcade.org"
                ],
        ],
        "storm_remove": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17"],
                requireonline: true,
                cmds: [
                        "prem {uuid} atlantic/kits.storm_gkit",
                ],
        ],
        "typhoon_add": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17"],
                requireonline: true,
                cmds: [
                        "padd {uuid} atlantic/kits.typhoon_gkit",
                        "grbc prod §:cstore §> §e{username} §fhas purchased §e30 days of Typhoon GKit §fon our store! §estore.starcade.org"
                ],
        ],
        "typhoon_remove": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17"],
                requireonline: true,
                cmds: [
                        "prem {uuid} atlantic/kits.typhoon_gkit",
                ],
        ],
        "gkit_add": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17"],
                requireonline: true,
                cmds: [
                        "padd {uuid} atlantic/kits.typhoon",
                        "grbc prod §2§lSTORE >> §a{username} §fhas purchased §a30 days of Typhoon GKit §fon our store! §astore.starcade.org"
                ],
        ],
        "gkit_remove": [
                servers: ["atlantic15", "atlantic14", "atlantic13", "atlantic12", "atlantic17"],
                requireonline: true,
                cmds: [
                        "prem {uuid} atlantic/kits.typhoon",
                ],
        ]
])