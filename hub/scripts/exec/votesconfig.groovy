package scripts.exec

import org.starcade.starlight.enviorment.Exports

Exports.ptr("votes_votegui", [
        "rewards"       : [
                "§2 ▎ §a1x §fVote Key",
                "§2 ▎ §a1x §f/giveaway Ticket",
                "§2 ▎ §a15 §fMinutes of Premium",
                "",
                "§6Milestones",
                "§6 ▎ §e10 §fVotes §8| §aLobster Key",
                "§6 ▎ §e50 §fVotes §8| §aDolphin Key",
                "§6 ▎ §e90 §fVotes §8| §aShark Key",
                "§6 ▎ §e120 §fVotes §8| §aWhale Key",
                "§6 ▎ §e150 §fVotes §8| §a500 §fGold Coins",
                "§6 ▎ §e180 §fVotes §8| §aTrident Key",
                "§6 ▎ §e240 §fVotes §8| §a500 §fGold Coins",
                "",
                "§dLucky Rewards",
                "§d ▎ §fExtra Vote Key §7(4%)",
                "§d ▎ §fDolphin Key §7(2%)",
                "§d ▎ §fRandom Robot §7(1%)",
                "§d ▎ §fWhale Key §7(0.2%)",
                "§d ▎ §a500 §fGold Coins §7(0.04%)"
        ],
        "links"         : [
                "",
                "§7§oWe have §nsix§r §7§ovoting links!",
                "",
                "§7§oYou must be online on the realm you want",
                "§7§oto receive your keys on!",
                "",
                "§7§oIf you're on bedrock edition, include the *",
                "§7§oin front of your username where possible.",
                "",
                "§e * Click to view * "
        ],
        "links_response": [
                " ",
                "              §a§lVoting Links:",
                " §e§l1. §b§ohttps://starcade.org/vote/BMC", //Good
                " §e§l2. §b§ohttps://starcade.org/vote/MCServers", //Good
                " §e§l3. §b§ohttps://starcade.org/vote/TOP", //New
                " §e§l4. §b§ohttps://starcade.org/vote/MCNet", //New
                " §e§l5. §b§ohttps://starcade.org/vote/MCPE", //Good
                " §e§l6. §b§ohttps://starcade.org/vote/MBS", //Good
                " "
        ]
])

Exports.ptr("votes_votereminder", [
        "§] §> §fYou haven't voted today! Go §evote §fat §e/vote §ffor free rewards! Don't forget to enter our §e/giveaways §fafter you vote!",
        "§a",
        "              §a§lVoting Links:",
        " §e§l1. §b§ohttps://starcade.org/vote/BMC", //Good
        " §e§l2. §b§ohttps://starcade.org/vote/MCServers", //Good
        " §e§l3. §b§ohttps://starcade.org/vote/TOP", //New
        " §e§l4. §b§ohttps://starcade.org/vote/MCNet", //New
        " §e§l5. §b§ohttps://starcade.org/vote/MCPE", //Good
        " §e§l6. §b§ohttps://starcade.org/vote/MBS", //Good
])

Exports.ptr("votes_rewards", [
        "voteparty_keys": [
                vote   : 40,
                lobster: 24,
                dolphin: 15,
                shark  : 11,
                whale  : 6,
                trident: 2,
                pet    : 1,
                robot  : 1
        ],
        "always"        : [
                "givecrates %player% vote 1",
                "ticketgive %player% 1",
                "givepremiumnote %player% 15m"
        ],
        "voteMessage"   : "§] §> &a%player% &fhas supported the server using &a/vote&f.",
        "milestones"    : [
                10 : [
                        command: "givecratekey %player% lobster 1",
                        name   : "§ex1 §a§lLobster Key"
                ] as Map<String, String>,
                50 : [
                        command: "givecratekey %player% dolphin 1",
                        name   : "§ex1 §a§lDolphin Key"
                ],
                90 : [
                        command: "givecratekey %player% shark 1",
                        name   : "§ex1 §a§lShark Key"
                ],
                120: [
                        command: "givecratekey %player% whale 1",
                        name   : "§ex1 §a§lWhale Key"
                ],
                150: [
                        command: "coinsgive %player% untradable 500",
                        name   : "§ex1 §e§l500 §6§lGold Coins"
                ],
                180: [
                        command: "givecratekey %player% trident 1",
                        name   : "§ex1 §a§lTrident Key"
                ],
                240: [
                        command: "coinsgive %player% untradable 500",
                        name   : "§ex1 §e§l500 §6§lGold Coins"
                ]
        ],
        "lucky"         : [
                [
                        command: "givecratekey %player% vote 1",
                        name   : "§ex1 §f§lExtra Vote Key",
                        chance : 1.0D / 25.0D
                ],
                [
                        command: "givecratekey %player% dolphin 1",
                        name   : "§ex1 §b§lDolphin Key",
                        chance : 1.0D / 50.0D
                ],
                [
                        command: "giverandomrobot %player% 1",
                        name   : "§ex1 §6§lRandom Robot",
                        chance : 1.0D / 100.0D
                ],
                [
                        command: "givecratekey %player% whale 1",
                        name   : "§ex1 §e§lWhale Key",
                        chance : 1.0D / 500.0D
                ],
                [
                        command: "coinsgive %player% untradable 500",
                        name   : "§ex1 §e§l500 §6§lGold Coins",
                        chance : 1.0D / 2500.0D
                ]
        ]
])