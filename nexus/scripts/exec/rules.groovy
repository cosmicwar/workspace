package scripts.exec

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.bukkit.Material


// 0  1  2  3  4  5  6  7  8
// 9 10 11 12 13 14 15 16 17
//18 19 20 21 22 23 24 25 26
//27 28 29 30 31 32 33 34 35
//36 37 38 39 40 41 42 43 44
//45 46 47 48 49 50 51 52 53

Exports.ptr("rulesConfig", [
        menuSize  : 6 * 9, // 6 * 9 is max
        decoration: [ //will be overwritten by an item if you put an item on the same slot
                      "000000000",
                      "000000000",
                      "000000000",
                      "000000000",
                      "000000000",
                      "000000000",
        ] as List<String>, // 0 == black ===> https://i.thimo.dev/624375e765c6e8c6229851e1
        rules     : [
                1: [
                        slot       : 10, // look at the numbers above
                        item       : Material.ENCHANTING_TABLE, //Can be removed if it was a book as it will default to a book.
                        title      : "§b§lIP Advertising",
                        description: [
                                "",
                                "§7Sharing any type of IP address",
                                "§7in any in-game chat. This also",
                                "§7includes any type of inappropriate link."
                        ]
                ],
                2: [ //left out item as it will default to a book anyways.
                     slot       : 11, // look at the numbers above
                     item       : Material.ENCHANTING_TABLE,
                     title      : "§b§lAdvertising Links",
                     description: [
                             "",
                             "§7Posting any sort of YouTube, Twitch, Social media,",
                             "§7malicious, jump scare, adult content",
                             "§7or anything equally inappropriate.",
                             "§7Graphic content will result in an immediate mute."
                     ]
                ],
                3: [
                        slot       : 12, // look at the numbers above
                        item       : Material.ENCHANTING_TABLE, //Can be removed if it was a book as it will default to a book.
                        title      : "§b§lDisclosing Personal Information",
                        description: [
                                "",
                                "§7Threatening or leaking other",
                                "§7players IRL information in ANY form",
                                "§7of chat. This also applies to DDoS or Swat threats."
                        ]
                ],
                4: [ //left out item as it will default to a book anyways.
                     slot       : 13, // look at the numbers above
                     item       : Material.ENCHANTING_TABLE,
                     title      : "§b§lRacist Homophobic Slurs",
                     description: [
                             "§7",
                             "§7Any use of Racial or Homophobic slurs in chat."
                     ]
                ],
                5: [
                        slot       : 14, // look at the numbers above
                        item       : Material.ENCHANTING_TABLE, //Can be removed if it was a book as it will default to a book.
                        title      : "§b§lRace Baiting",
                        description: [
                                "§7",
                                "§7A player trying to incite",
                                "§7racism or spark racial conflict."
                        ]
                ],
                6: [ //left out item as it will default to a book anyways.
                     slot       : 15, // look at the numbers above
                     item       : Material.ENCHANTING_TABLE,
                     title      : "§b§lLight Homophobic Slur",
                     description: [
                             "§7",
                             "§7Use of any lighter homophobic slurs.",
                             "§7Examples include: gay, homo, tranny, etc.."
                     ]
                ],
                7: [
                        slot       : 16, // look at the numbers above
                        item       : Material.ENCHANTING_TABLE, //Can be removed if it was a book as it will default to a book.
                        title      : "§b§lCP Jokes",
                        description: [
                                "§7",
                                "§7A player making jokes in regards to",
                                "§7children engaging in sexual acts,",
                                "§7or comments that are interpreted in a sexual manner."
                        ]
                ],
                8: [ //left out item as it will default to a book anyways.
                     slot       : 19, // look at the numbers above
                     item       : Material.ENCHANTING_TABLE,
                     title      : "§b§lSuicide Encouragement & Death Threats",
                     description: [
                             "§7",
                             "§7Any sort of chat or behavior that encourages",
                             "§7suicidal tendencies in people or threatens",
                             "§7harm to another individual."
                     ]
                ],
                9: [
                        slot       : 20, // look at the numbers above
                        item       : Material.ENCHANTING_TABLE, //Can be removed if it was a book as it will default to a book.
                        title      : "§b§lStaff Impersonation",
                        description: [
                                "§7",
                                "§7Having a /nick which makes you look like a staff",
                                "§7member or making your IGN similar to a staff member."
                        ]
                ],
                10: [ //left out item as it will default to a book anyways.
                      slot       : 21, // look at the numbers above
                      item       : Material.ENCHANTING_TABLE,
                      title      : "§b§lInappropriate IGN / Skin",
                      description: [
                              "§7",
                              "§7Having an IGN or Skin that might be",
                              "§7racist, discriminatory, or insulting the server."
                      ]
                ],
                11: [
                        slot       : 22, // look at the numbers above
                        item       : Material.ENCHANTING_TABLE, //Can be removed if it was a book as it will default to a book.
                        title      : "§b§lExtreme Toxicity",
                        description: [
                                "§7",
                                "§7Using very harsh and overly",
                                "§7rude statements towards another player."
                        ]
                ],
                12: [ //left out item as it will default to a book anyways.
                      slot       : 23, // look at the numbers above
                      item       : Material.ENCHANTING_TABLE,
                      title      : "§b§lMisuse of Report and Helpop",
                      description: [
                              "§7",
                              "§7Falsely using the report, or helpop system."
                      ]
                ],
                13: [
                        slot       : 24, // look at the numbers above
                        item       : Material.ENCHANTING_TABLE, //Can be removed if it was a book as it will default to a book.
                        title      : "§b§lInappropriate Join message / Shout Message",
                        description: [
                                "§7",
                                "§7Anything related to Adult content or",
                                "§7mental illness, fake punishments, trade requests",
                                "§7or impersonation of another staff member or player."
                        ]
                ],
                14: [ //left out item as it will default to a book anyways.
                      slot       : 25, // look at the numbers above
                      item       : Material.ENCHANTING_TABLE,
                      title      : "§b§lAdvertising Spam",
                      description: [
                              "§7",
                              "§7This is when players are spamming chat",
                              "§7more than twice with any kind of buy / sell message",
                              "§7every 1 minute. (2 buy / sell messages per 1 minute)"
                      ]
                ],
                15: [
                        slot       : 28, // look at the numbers above
                        item       : Material.ENCHANTING_TABLE, //Can be removed if it was a book as it will default to a book.
                        title      : "§b§lCharacter Spam / Caps Spam",
                        description: [
                                "§7",
                                "§7Excessive use of capital letters;",
                                "§7Excessive use of random symbols / letters",
                                "§7or 5+ messages that are typed within 15 seconds",
                                "§7or sending 3+ of the same message in 10 seconds."
                        ]
                ],
                16: [ //left out item as it will default to a book anyways.
                      slot       : 29, // look at the numbers above
                      item       : Material.ENCHANTING_TABLE,
                      title      : "§b§lPromoting Spam",
                      description: [
                              "§7",
                              "§7Anything that encourages",
                              "§7private message spam or chat spam."
                      ]
                ],
                17: [
                        slot       : 30, // look at the numbers above
                        item       : Material.ENCHANTING_TABLE, //Can be removed if it was a book as it will default to a book.
                        title      : "§b§lBlock Glitching in PvP",
                        description: [
                                "§7",
                                "§7Block glitching to lose combat",
                                "§7tags, or teleport out of pvp"
                        ]
                ],
                18: [ //left out item as it will default to a book anyways.
                      slot       : 31, // look at the numbers above
                      item       : Material.ENCHANTING_TABLE,
                      title      : "§b§lAbusing Alts to gain an Advantage",
                      description: [
                              "§7",
                              "§7This is when you are using alts to gain an advantage, this",
                              "§7includes voting on alts. Players CAN use alts for Gkits and pvp"
                      ]
                ],
                19: [
                        slot       : 32, // look at the numbers above
                        item       : Material.ENCHANTING_TABLE, //Can be removed if it was a book as it will default to a book.
                        title      : "§b§lUsing Disallowed Modifications (Hacking)",
                        description: [
                                "§7",
                                "§7Using any sort of Modification to gain an advantage over players",
                                "§7It is up to our staff as to whether a hacked client is in use.",
                                "§7Staff must have proof of ban."
                        ]
                ],
                20: [ //left out item as it will default to a book anyways.
                      slot       : 33, // look at the numbers above
                      item       : Material.ENCHANTING_TABLE,
                      title      : "§b§lExploits and Glitches",
                      description: [
                              "§7",
                              "§7Using any bug, dupe, or exploit",
                              "§7for your own personal gain. This includes",
                              "§7sharing information about an exploit to other players",
                              "§7without letting the administration knowing. Intentionally",
                              "§7causing lag falls under this rule. Admins do reserve the right",
                              "§7to adjust the timing of a ban for abusing bugs based on the severity."
                      ]
                ],
                21: [ //left out item as it will default to a book anyways.
                      slot       : 34, // look at the numbers above
                      item       : Material.ENCHANTING_TABLE,
                      title      : "§b§lDuping",
                      description: [
                              "§7",
                              "§7If a player is suspected of duping",
                              "§7please notify a higher staff to confirm."
                      ]
                ],
                22: [
                        slot       : 37, // look at the numbers above
                        item       : Material.ENCHANTING_TABLE, //Can be removed if it was a book as it will default to a book.
                        title      : "§b§lInappropriate Builds",
                        description: [
                                "§7",
                                "§7Any build that might be inappropriate",
                                "§7homophobic, racist, explicit or offensive."
                        ]
                ],
                23: [ //left out item as it will default to a book anyways.
                      slot       : 38, // look at the numbers above
                      item       : Material.ENCHANTING_TABLE,
                      title      : "§b§lBuycraft scamming",
                      description: [
                              "",
                              "§7Scamming via the Starcade store.",
                              "§7Promising someone you will give them",
                              "§7ingame items for them to buy you something on",
                              "§7the store and not handing over your end of the deal.",
                              "§7This also includes scamming ingame items for promise of coins."
                      ]
                ],
                24: [ //left out item as it will default to a book anyways.
                      slot       : 39, // look at the numbers above
                      item       : Material.ENCHANTING_TABLE,
                      title      : "§b§lIRL Trading",
                      description: [
                              "§7",
                              "§7A user who attempts to trade",
                              "§7or sucessfully trades ingame items for",
                              "§7real world currency or for items",
                              "§7that are not related to anything Starcade."
                      ]
                ],
                25: [ //left out item as it will default to a book anyways.
                      slot       : 40, // look at the numbers above
                      item       : Material.ENCHANTING_TABLE,
                      title      : "§b§lInventory Bombing",
                      description: [
                              "§7",
                              "§7This is when a player fills the inventory",
                              "§7with useless items and goes to pvp",
                              "§7with the only intention to die and drop their items."
                      ]
                ],
                26: [ //left out item as it will default to a book anyways.
                      slot       : 41, // look at the numbers above
                      item       : Material.ENCHANTING_TABLE,
                      title      : "§b§lLagging the server",
                      description: [
                              "§7",
                              "§7Intentionally causing lag to the server."
                      ]
                ],
                27: [ //left out item as it will default to a book anyways.
                      slot       : 42, // look at the numbers above
                      item       : Material.ENCHANTING_TABLE,
                      title      : "§b§lBan / Mute Evasion",
                      description: [
                              "§7",
                              "§7A user who logs onto an alt",
                              "§7account or attempts to evade",
                              "§7a mute or ban is guilty of this offense."
                      ]
                ],
                28: [ //left out item as it will default to a book anyways.
                      slot       : 49, // look at the numbers above
                      title      : "§b§lSomeone breaking the rules?",
                      description: [
                              "§7",
                              "§7If you find someone who is",
                              "§7breaking the rules please use",
                              "§7/report or make a ticket on the discord",
                              "§7our staff will handle the situation as soon as they can."
                      ]
                ],
                29: [ //left out item as it will default to a book anyways.
                      slot       : 4, // look at the numbers above
                      title      : "§b§lDisallowed Modifications",
                      description: [
                              "§7Auto clickers (Includes typing down mouse)",
                              "§7Scroll Clicking",
                              "§7Hacked clients of any sort",
                              "§7ANY modification that gives advantage in PvP",
                              "§7NO macros of any type",
                              "§7Bots",
                              "§7Scripts",
                              "§7Chat macros",
                              "§7Auto Farming",
                              "§7Fly",
                              "§7Picture in Picture mod",
                              "§7Printer mod",
                              "§7X-Ray",
                              "§7Phase",
                              "§7Smart Moving",
                              "§7Auto Fishing",
                              "§7Auto Salvaging",
                              "§7If you don't see a modification here, and don't know",
                              "§7if it's allowed, make a ticket on discord.",
                      ]
                ]
        ] as Map<Integer, ?>

] as Map<String, ?>)

Starlight.watch("scripts/shared/legacy/Rules.groovy")
