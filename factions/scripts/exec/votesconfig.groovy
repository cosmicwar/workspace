package scripts.exec

import org.starcade.starlight.enviorment.Exports

Exports.ptr("votes_votegui", [
        "rewards"       : [
                "§2 ▎ §a1x §fVote Key",
                "§2 ▎ §a1x §f/giveaway Ticket",
                "§2 ▎ §a15 §fMinutes of Premium",
                "",
                "§6Milestones",
                "§6 ▎ §e10 §fVotes §8| §a2x Vote Key",
                "§6 ▎ §e50 §fVotes §8| §bCommon Key",
                "§6 ▎ §e90 §fVotes §8| §9Rare Key",
                "§6 ▎ §e120 §fVotes §8| §aLegendary Key",
                "§6 ▎ §e150 §fVotes §8| §a500 §fGold Coins",
                "§6 ▎ §e180 §fVotes §8| §ex1 §a§lLarge Money Pouch",
                "§6 ▎ §e240 §fVotes §8| §6500 Gold Coins",
                "",
                "§dLucky Rewards",
                "§d ▎ §fExtra Vote Key §7(4%)",
                "§d ▎ §fLegendary Key §7(2%)",
                "§d ▎ §6500 Gold Coins §7(0.04%)"
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
                "§eClick to View"
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
        " §e§l1. §b§ohttps://starcade.org/vote/MCNet", //New
        " §e§l2. §b§ohttps://starcade.org/vote/BMC", //Good
        " §e§l3. §b§ohttps://starcade.org/vote/MCServers", //Good
        " §e§l4. §b§ohttps://starcade.org/vote/TOP", //New
        " §e§l5. §b§ohttps://starcade.org/vote/MCPE", //Good
        " §e§l6. §b§ohttps://starcade.org/vote/MBS", //Good
])

Exports.ptr("votes_rewards", [
        "voteparty_keys": [
                vote     : 40,
                jester   : 24,
                merchant : 15,
                noble    : 11,
                duke     : 6,
                monarch  : 2
        ],
        "always"        : [
                "givecrates %player% vote 1",
                "ticketgive %player% 1",
                "givepremium %player% 15m"
        ],
        "voteMessage"   : "§] §> &a%player% &fhas supported the server using &a/vote&f.",
        "milestones"    : [
                10 : [
                        command: "givecratekey %player% vote 2",
                        name   : "§ex2 §a§lVote Key"
                ] as Map<String, String>,
                50 : [
                        command: "givecratekey %player% jester 1",
                        name   : "§ex1 §bJester Key"
                ],
                90 : [
                        command: "givecratekey %player% merchant 1",
                        name   : "§ex1 §9§lMerchant Key"
                ],
                120: [
                        command: "givecratekey %player% noble 1",
                        name   : "§ex1 §6§lNoble Key"
                ],
                150: [
                        command: "coinsgive %player% untradable 500",
                        name   : "§ex1 §e§l500 §6§lGold Coins"
                ],
                180: [
                        command: "givepouch %player% money massive",
                        name   : "§ex1 §c§lMassive Money Pouch"
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
                        command: "givecratekey %player% merchant 1",
                        name   : "§ex1 §b§lMerchant Key",
                        chance : 1.0D / 50.0D
                ],
                [
                        command: "givecratekey %player% jester 1",
                        name   : "§ex1 §6§lJester Key",
                        chance : 1.0D / 100.0D
                ],
                [
                        command: "givecratekey %player% noble 1",
                        name   : "§ex1 §e§lNoble Key",
                        chance : 1.0D / 500.0D
                ],
        ]
])