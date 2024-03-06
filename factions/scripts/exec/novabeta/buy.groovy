package scripts.exec.novabeta

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Schedulers
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.utils.Temple
import scripts.shared3.ArkPerms

//Schedulers.sync().run {
//    def shop = Bukkit.getWorld("shop_world")
//
//    Exports.ptr("buy/warp", new Location(shop, -3.5D, 108.2D, -57.5D, -90, 0))
//    Exports.ptr("buy/jumppad-locations", [
//            [
//                    "areaFrom"  : new Area(23, 105, -60, 21, 110, -58), //Spawn
//                    "locationTo": new Position(31.5, 107, -25.5), //Autominers
//            ],
//            [
//                    "areaFrom"  : new Area(17, 105, -64, 19, 110, -62), //Spawn
//                    "locationTo": new Position(52.5, 108, -76.5), //GKits
//            ],
//            [
//                    "areaFrom"  : new Area(56, 107, -74, 55, 112, -72), //GKits
//                    "locationTo": new Position(79.5, 109, -57.5), //Mid
//            ],
//            [
//                    "areaFrom"  : new Area(81, 107, -51, 79, 112, -49), //Mid
//                    "locationTo": new Position(105.5, 105, -21.5), //crates
//            ],
//            [
//                    "areaFrom"  : new Area(97, 107, -62, 99, 112, -64), //Mid
//                    "locationTo": new Position(184.5, 80, -115.5), //Ranks
//            ]
//    ] as List<HashMap<String, Object>>)
//
////    for (int i = 0; i++; i <= 2) {
////        Bukkit.getWorld("world_${i}").execute {
////            Bukkit.getPlayer("Markoo").sendMessage("Works")
////            spawnNpc("easter_${i}", "§e§lEaster lootboxes", new Location(Bukkit.getWorld("world_${i}"), 11.5D, 155D, -6.5D, 135, 10), "Markoo", { player ->
////                player.chat("/coinshop easter2023")
////            })
////        }
////    }
//
//    spawnNpc("easter_0", "§e§lLootbox", new Location(Bukkit.getWorld("world_0"), 11.5D, 155D, -6.5D, 135, 10), "JDCMC", { player ->
//        player.chat("/coinshop lootbox")
//    })
//    spawnNpc("easter_1", "§e§lLootbox", new Location(Bukkit.getWorld("world_1"), 11.5D, 155D, -6.5D, 135, 10), "JDCMC", { player ->
//        player.chat("/coinshop lootbox")
//    })
//    spawnNpc("easter_2", "§e§lLootbox", new Location(Bukkit.getWorld("world_2"), 11.5D, 155D, -6.5D, 135, 10), "JDCMC", { player ->
//        player.chat("/coinshop lootbox")
//    })
//    spawnNpc("weekly_0", "§b§lSOTW lootboxes", new Location(Bukkit.getWorld("world_0"), 11.5D, 155D, -12.5D, 45, -10), "ukwifi", { player ->
//        player.chat("/coinshop sotw")
//    })
//    spawnNpc("weekly_1", "§b§lSOTW lootboxes", new Location(Bukkit.getWorld("world_1"), 11.5D, 155D, -12.5D, 45, -10), "ukwifi", { player ->
//        player.chat("/coinshop sotw")
//    })
//    spawnNpc("weekly_2", "§b§lSOTW lootboxes", new Location(Bukkit.getWorld("world_2"), 11.5D, 155D, -12.5D, 45, -10), "ukwifi", { player ->
//        player.chat("/coinshop sotw")
//    })
//
//    shop.execute {
//
//        spawnNpc("halloween", "§6§l§6§ki§0§ki§6§ki§0§ki §6§lH§8§la§6§ll§8§ll§6§lo§8§lw§6§le§8§le§6§ln §8§l'§6§l2§8§l3 §6§lL§8§lo§6§lo§8§lt§6§lb§8§lo§6§lx §6§ki§0§ki§6§ki§0§ki", new Location(shop, 13.5D, 106D, -56.5D, 60, 10), "Techmullet", { player ->
//            player.chat("/coinshop halloween")
//        })
//
////        spawnNpc("christmas", "§c§lChristmas Bundle", new Location(shop, 11.5D, 106D, -60.5D, 60, 10), "Markoo", { player ->
////            player.chat("/coinshop christmas")
////        })
//
////        spawnNpc("newyears", "§c§lNew Years Bundle", new Location(shop, 10.5D, 106D, -62.5D, 60, 10), "Markoo", { player ->
////            player.chat("/coinshop newyears")
////        })
////
////        spawnNpc("easter", "§e§lEaster Bundle", new Location(shop, 10.5D, 106D, -62.5D, 60, 10), "Markoo", { player ->
////            player.chat("/coinshop easter")
////        })
//
//        spawnNpc("ranks_shop", "§6§lBrowse Ranks", new Location(shop, 2.5D, 107D, -59.5D, 70, 10), "Serayne92", { player ->
//            player.chat("/coinshop ranks")
//        })
//        spawnNpc("crates_shop", "§6§lBrowse crates", new Location(shop, 2.5D, 107D, -55.5D, 108, 10), "iKoalas", { player ->
//            player.chat("/coinshop crates")
//        })
//
//        spawnNpc("autominer_shop_1", "§6§lView Autominers", new Location(shop, 35.5D, 106D, -19.5D, -90, 45), "1d032333-e6f0-4068-9186-26990bd02e1e", { player ->
//            player.chat("/coinshop autominer")
//        })
//        spawnNpc("autominer_shop_2", "§6§lView Autominers", new Location(shop, 34.5D, 105.5D, -12.5D, -45, 45), "1d032333-e6f0-4068-9186-26990bd02e1e", { player ->
//            player.chat("/coinshop autominer")
//        })
//        spawnNpc("autominer_shop_3", "§6§lView Autominers", new Location(shop, 28.5D, 105.5D, -13.5D, 45, 30), "1d032333-e6f0-4068-9186-26990bd02e1e", { player ->
//            player.chat("/coinshop autominer")
//        })
//        spawnNpc("autominer_shop_4", "§6§lView Autominers", new Location(shop, 31.5D, 105D, -16.5D, 43, 10), "1d032333-e6f0-4068-9186-26990bd02e1e", { player ->
//            player.chat("/coinshop autominer")
//        })
//
//        spawnNpc("battlepass_shop", "§6§lView Premium BattlePass", new Location(shop, 50.5D, 109D, -65.5D, 180, 10), "ukwifi", { player ->
//            player.chat("/coinshop battlepass")
//        })
//
////        spawnNpc("gkit_bundle_shop", "§6§lView GKit Bundle", new Location(shop, 63.5D, 109D, -78.5D, 50, 10), "Markoo", { player ->
////            player.chat("/coinshop gkitbundle")
////        })
////        spawnNpc("gkit_storm", "§b§lView Storm GKit", new Location(shop, 60.5D, 109D, -79.5D, 0, 10), "0ec629fe-bf59-4959-b56e-a12c668d4d35", { player ->
////            player.chat("/coinshop storm")
////        })
////        spawnNpc("gkit_misty", "§9§lView Misty GKit", new Location(shop, 62.5D, 109D, -81.5D, 0, 10), "17ae22f6-ea8c-4587-8acf-33ca54b76043", { player ->
////            player.chat("/coinshop misty")
////        })
////        spawnNpc("gkit_typhoon", "§c§lView Typhoon GKit", new Location(shop, 64.5D, 109D, -81.5D, 0, 10), "850da7d4-0997-4ebd-a069-4d2d923a819f", { player ->
////            player.chat("/coinshop typhoon")
////        })
////        spawnNpc("gkit_lagoon", "§a§lView Lagoon GKit", new Location(shop, 66.5D, 109D, -79.5D, 33, 10), "310895c5-e3f1-472e-b86b-0fad7bc26a94", { player ->
////            player.chat("/coinshop lagoon")
////        })
//
//
//        spawnNpc("surfer_rank_shop", "§b§lView Surfer Rank", new Location(shop, 178.5D, 81D, -117.5D, -140, 10), "b6cce96d-997e-4bde-93a0-35533273ab3b", { player ->
//            player.chat("/coinshop surfer")
//        })
//        spawnNpc("diver_rank_shop", "§9§lView Diver Rank", new Location(shop, 184.5D, 81D, -125.5D, 0, 10), "6c5668cc-39b7-4307-b68c-c9b36952e855", { player ->
//            player.chat("/coinshop diver")
//        })
//        spawnNpc("sailor_rank_shop", "§e§lView Sailor Rank", new Location(shop, 178.5D, 81D, -125.5D, -31, 10), "43803603-c76e-4ae6-951d-0fca1e9390b8", { player ->
//            player.chat("/coinshop sailor")
//        })
//        spawnNpc("captain_rank_shop", "§d§lView Captain Rank", new Location(shop, 194.5D, 84D, -121.5D, 65, 20), "50a932b6-5a58-4c1b-b205-423d04e2d669", { player ->
//            player.chat("/coinshop captain")
//        })
//        spawnNpc("pirate_rank_shop", "§6§lView §6§lP§3§lI§6§lR§3§lA§6§lT§3§lE§r §6§lRank", new Location(shop, 166.5D, 85D, -118.5D, -91, 20), "d8a2d6ba-67c6-43de-9ddd-2ba07fbdb775", { player ->
//            player.chat("/coinshop pirate")
//        })
//        spawnNpc("admiral_rank_shop", "§b§lView §b§lA§c§lD§b§lM§c§lI§b§lR§c§lA§b§lL§r §b§lRank", new Location(shop, 152.5D, 87D, -123.5D, -82, 20), "671cb2ba-38d7-4179-91ca-94b30282c4e9", { player ->
//            player.chat("/coinshop admiral")
//        })
//        spawnNpc("medusa_rank_shop", "§a§lView §a§lM§2§lE§a§lD§2§lU§a§lS§2§lA§r §a§lRank", new Location(shop, 150.5D, 87D, -119.5D, -100, 20), "8e3f6fb0-0092-47e2-8ea1-9f881181a29b", { player ->
//            player.chat("/coinshop medusa")
//        })
//
//        spawnNpc("poseidon_rank_shop", "§b§lLIMITED §b§lP§9§lO§3§lS§b§lE§9§lI§3§lD§b§lO§9§lN§r §b§lRANK", new Location(shop, 11.5D, 106D, -63.5D, 35, 20), "Serayne92", { player ->
//            player.chat("/coinshop poseidon")
//        })
//
//        spawnNpc("lobster_crate_shop", "§b§lView Lobster Key", new Location(shop, 111.5D, 105D, -28.5D, 0, 10), "d6539f55-96c9-4685-b03a-2bbf339018eb", { player ->
//            player.chat("/coinshop lobster")
//        })
//        spawnNpc("dolphin_crate_shop", "§d§lView Dolphin Key", new Location(shop, 115.5D, 105D, -28.5D, 0, 10), "85992f82-61c9-4604-b027-73ac280bf656", { player ->
//            player.chat("/coinshop dolphin")
//        })
//        spawnNpc("shark_crate_shop", "§f§lView Shark Key", new Location(shop, 119.5D, 105D, -28.5D, 0, 10), "e7137878-8088-459a-b657-0f7799506549", { player ->
//            player.chat("/coinshop shark")
//        })
//
//        spawnNpc("whale_crate_shop", "§e§lView Whale Key", new Location(shop, 111.5D, 105D, -14.5D, 180, 10), "d8ea3418-c6e9-47d4-a70b-6ee082545368", { player ->
//            player.chat("/coinshop whale")
//        })
//        spawnNpc("trident_crate_shop", "§6§lView Trident Key", new Location(shop, 114.5D, 105D, -14.5D, 180, 10), "32af616e-7161-45ee-904c-8f6e4a77d79e", { player ->
//            player.chat("/coinshop trident")
//        })
//        spawnNpc("robot_crate_shop", "§9§lView Robot & Drill Key", new Location(shop, 117.5D, 105D, -14.5D, 180, 10), "65c7b5cc-7a50-4c35-b0e8-7be7d72c5388", { player ->
//            player.chat("/coinshop robot")
//        })
//        spawnNpc("pet_crate_shop", "§6§lView Pet Key", new Location(shop, 120.5D, 105D, -14.5D, 180, 10), "f17c083d-952c-47b5-b53f-e8f19dfcf088", { player ->
//            player.chat("/coinshop pet")
//        })
//        spawnNpc("skin_crate_shop", "§5§lView Skin Key", new Location(shop, 123.5D, 105D, -14.5D, 180, 10), "57b5eb2e-4cd4-48e4-9828-eee5a6fc43d5", { player ->
//            player.chat("/coinshop skin")
//        })
//
//        spawnNpc("monthly_crate_shop", "§c§lView Monthly Key", new Location(shop, 129.5D, 103D, -24.5D, 85, 10), "555359ef-4870-452d-957a-c6dcae7486e8", { player ->
//            player.chat("/coinshop monthly")
//        })
//    }
//}

HashMap<String, Integer> crateId = [
        "lobster": 2,
        "dolphin": 1,
        "shark"  : 6,
        "whale"  : 10,
        "trident": 8,
        "robot"  : 5,
        "pet"    : 4,
        "monthly": 3,
        "skin"   : 7
]

Exports.ptr("buy/crateid", crateId)

String temple = ""
if (Temple.templeId.contains("pacific")) temple = "pacific"
if (Temple.templeId.contains("atlantic")) temple = "atlantic"

Exports.ptr("coins/categories", [
        settings: [
                menuSize  : 3 * 9,
                menuName  : "Starcade Store",
                slots     : [4, 10, 11, 13, 15, 16, 22], //add 4 for lootbox
                decoration: [
                        "fbfbfbfbf",
                        "fbfbfbfbf",
                        "fbfbfbfbf"
                ]
        ],
        items   : [
                "lootbox"   : [
                        displayName: "§c§l5 x Halloween lootboxes",
                        material   : Material.ENDER_CHEST,
                        model      : 106,
                        description: [
                                "",
                                "§6 ▎ Buy the §c§lBRAND NEW §c§lHalloween lootboxes",
                                "§7 ▎ This lootboxes will only be available",
                                "§6 ▎ for a limited time!",
                                "§7 ▎ Get it while its here!",
                                "",
                                ""
                        ]
                ],
                "pass"      : [
                        displayName: "§e§lPasses and Premium",
                        material   : Material.PAPER,
                        description: [
                                ""
                        ]
                ],
                "crates"    : [
                        displayName: "§6§lCrate Keys",
                        material   : Material.TRIPWIRE_HOOK,
                        description: [
                                ""
                        ]
                ],
                "ranks"     : [
                        displayName: "§b§lRanks",
                        material   : Material.NETHER_STAR,
                        description: [
                                ""
                        ]
                ],
                "bundles"   : [
                        displayName: "§d§lBundles",
                        material   : Material.CHEST,
                        description: [
                                ""
                        ]
                ],
                "autominers": [
                        displayName: "§a§lAutominers",
                        material   : Material.GOLDEN_PICKAXE,
                        description: [
                                ""
                        ]
                ],
                "gkits"     : [
                        displayName: "§c§lGKits",
                        material   : Material.ENDER_CHEST,
                        description: [
                                ""
                        ]
                ]
        ]
])
Exports.ptr("coins/menu", [
        "battlepass"   : [
                "coins"      : 700,
                "category"   : "pass",
                "item"       : Material.PAPER,
                "displayName": "§6§lPremium Battle Pass",
                "description": [
                        "",
                        "§b ▎ Access to rewards worth over \$65",
                        "§7 ▎ Unlock a free monthly crate",
                        "§b ▎ 1000 Coins as rewards",
                        "§7 ▎ Double Challenge XP",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepremiumpass ${player.getName()}")
                },
                "onCheck"    : { Player player ->
                    return (Exports.ptr("battlepass:hasPremium") as Closure<Boolean>)?.call(player.getUniqueId()) ?: false
                }
        ],
        "prestige_pack": [
                "hidden"     : true,
                "limit"      : 1,
                "cooldown"   : 12,
                "coins"      : 1100,
                "category"   : "pass",
                "item"       : Material.ENDER_CHEST,
                "displayName": "§d§lPrestige Starter Pack",
                "description": [
                        "",
                        "§d ▎ One Monthly Crate",
                        "§7 ▎ 5x Random Mythical 2h30m Boosters",
                        "§d ▎ 3x Massive E-Token Pouches",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecrate ${player.getName()} monthly 1")

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "giveboosters ${player.getName()} random 4 9000")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "giveboosters ${player.getName()} random 4 9000")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "giveboosters ${player.getName()} random 4 9000")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "giveboosters ${player.getName()} random 4 9000")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "giveboosters ${player.getName()} random 4 9000")

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepouch ${player.getName()} etoken massive")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepouch ${player.getName()} etoken massive")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepouch ${player.getName()} etoken massive")
                },
                "onCheck"    : { Player player ->
                    return false
                }
        ],
//        "easter"       : [
//                "coins"      : 1000,
//                "category"   : "pass",
//                "item"       : Material.EGG,
//                "displayName": "§e§lEaster Bundle",
//                "description": [
//                        "",
//                        "§e ▎ Easter Custom Armour",
//                        "§7 ▎ Easter Title",
//                        "§e ▎ Easter Nickname",
//                        "§7 ▎ Easter Trail",
//                        "§e ▎ Easter Bunny Cannon",
//                        ""
//                ],
//                "onPurchase" : { Player player ->
//                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/rabbitcannon.use")
//                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/kits.easter")
//                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "giveeasteregg ${player.getName()} 5")
//                },
//                "onCheck"    : { Player player ->
//                    return false
//                }
//        ],
        "lootbox"      : [
                "coins"      : 900,
                "category"   : "lootbox",
                "item"       : Material.ENDER_CHEST,
                "model"      : 106,
                "displayName": "§c§l5 x Halloween lootboxes",
                "description": [
                        "",
                        "§6 ▎ Buy the §c§lBRAND NEW §6§lHalloween lootboxes",
                        "§7 ▎ This lootboxes will only be available",
                        "§6 ▎ for a limited time!",
                        "§7 ▎ Get it while its here!",
                        "",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecratekey ${player.getName()} lootbox 5")
                },
                "onCheck"    : { Player player ->
                    return false
                }
        ],
//                "christmas": [
//                "coins"      : 1000,
//                "category"   : "pass",
//                "item"       : Material.COOKIE,
//                "displayName": "§c§lChristmas Bundle",
//                "description": [
//                        "",
//                        "§c ▎ Christmas Custom Armour",
//                        "§7 ▎ Christmas Title",
//                        "§c ▎ Christmas Nickname",
//                        "§7 ▎ Christmas PERMANENT Kit",
//                        "§c ▎ One of Each Present",
//                        ""
//                ],
//                "onPurchase" : { Player player ->
//                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/kits.christmas")
//                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepresent ${player.getName()} 1 1")
//                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepresent ${player.getName()} 2 1")
//                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givepresent ${player.getName()} 3 1")
//                },
//                "onCheck"    : { Player player ->
//                    return false
//                }
//        ],
//        "newyears"     : [
//                "coins"      : 1000,
//                "category"   : "pass",
//                "item"       : Material.FIREWORK_ROCKET,
//                "displayName": "§c§lNew Years Bundle",
//                "description": [
//                        "",
//                        "§c ▎ New Years Custom Armour Kit",
//                        "§a ▎ New Years Title",
//                        "§e ▎ New Years Nickname",
//                        "§b ▎ Firework particle in /particles",
//                        "§d ▎ /fireworkshow command (5 Minute Cooldown)",
//                        ""
//                ],
//                "onPurchase" : { Player player ->
//                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/kits.newyears")
//                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/fireworkshow.use")
//                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givenicknamecolor ${player.getName()} NEW_YEARS")
//                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givetitle ${player.getName()} §6§k§l!§r§bNewYears§f2022§6§k§l!")
//                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "giveparticles ${player.getName()} firework")
//                },
//                "onCheck"    : { Player player ->
//                    return false
//                }
//        ],
        "halloween"    : [
                "coins"      : 1200,
                "category"   : "bundles",
                "item"       : Material.PUMPKIN,
                "displayName": "§6§lHalloween Bundle",
                "description": [
                        "",
                        "§c ▎ Halloween Custom Armour Kit",
                        "§a ▎ Halloween Title",
                        "§b ▎ Flame particle in /particles",
                        "§d ▎ 3x Halloween lootboxes",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/kits.halloween")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givetitle ${player.getName()} §6§k§l!§r§6§lSpooky§6§k§l!")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givelootbox ${player.getName()} halloween 3")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "giveparticles ${player.getName()} flame")
                },
                "onCheck"    : { Player player ->
                    return false
                }
        ],
        "weekly_1"     : [
                "coins"      : 2400,
                "category"   : "bundles",
                "item"       : Material.ENDER_EYE,
                "displayName": "§6§lWeek 1 Bundle",
                "description": [
                        "",
                        "§a ▎ 5x Halloween Lootboxes",
                        "§d ▎ 5x Skin Crate Key",
                        "§b ▎ 1x Monthly Crate",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givelootbox ${player.getName()} halloween 5")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecratekey ${player.getName()} skin 5")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecratekey ${player.getName()} monthly 1")
                },
                "onCheck"    : { Player player ->
                    return false
                }
        ],
        "sotw"         : [
                "coins"      : 2400,
                "category"   : "bundles",
                "item"       : Material.ENDER_EYE,
                "displayName": "§6§lSOTW Bundle",
                "description": [
                        "",
                        "§c ▎ 5x Trident Keys",
                        "§a ▎ 5x Pet Keys",
                        "§d ▎ 5x Robot Keys",
                        "§b ▎ 2x Monthly Crate",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecratekey ${player.getName()} trident 5")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecratekey ${player.getName()} pet 5")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecratekey ${player.getName()} robot 5")
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecratekey ${player.getName()} monthly 2")
                },
                "onCheck"    : { Player player ->
                    return false
                }
        ],
        "autominer"    : [
                "coins"      : 1100,
                "category"   : "autominers",
                "item"       : Material.IRON_PICKAXE,
                "displayName": "§b§lAutominer",
                "description": [
                        "",
                        "§b ▎ Purchase an upgradable companion",
                        "§7 ▎ that will help you mine!",
                        "§b ▎ Up to 4 additional autominers are",
                        "§7 ▎ purchasable",
                        ""
                ],
                "onPurchase" : { Player player ->
                    int autominers = ArkPerms.getPermissionCount(player, "autominerlimit", 0, 6)
                    if (autominers == 0) autominers = 1
                    else autominers += 1
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/autominerlimit.${autominers + 1}")
                },
                "onCheck"    : { Player player ->
                    return false
                }
        ],
        "surfer"       : [
                "coins"      : 200,
                "category"   : "ranks",
                "item"       : Material.COAL,
                "displayName": "§b§lSurfer Rank",
                "description": [
                        "",
                        "§b ▎ 1.1x Sell Multiplier",
                        "§7 ▎ Join the server when full",
                        "§b ▎ Access up to /pv 2",
                        "§7 ▎ Access to /enderchest",
                        "§b ▎ Ability to /sethome 2 times",
                        "§7 ▎ Auction 2 items at once",
                        "§b ▎ Access to surfer kit",
                        "§7 ▎ 2.5% Faster /printer",
                        "§b ▎ 2.5% Faster /recycler",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/group.donator.surfer")
                },
                "onCheck"    : { Player player ->
                    return player.hasPermission("group.donator.surfer")
                }
        ],
        "diver"        : [
                "coins"      : 500,
                "category"   : "ranks",
                "item"       : Material.GOLD_INGOT,
                "displayName": "§9§lDiver Rank",
                "description": [
                        "",
                        "§9 ▎ 1.2x Sell Multiplier",
                        "§7 ▎ Join the server when full",
                        "§9 ▎ Access up to /pv 4",
                        "§7 ▎ Access to /enderchest",
                        "§9 ▎ Ability to /sethome 3 times",
                        "§7 ▎ Auction 3 items at once",
                        "§9 ▎ Access to diver kit",
                        "§7 ▎ 5% Faster /printer",
                        "§9 ▎ 5% Faster /recycler",
                        "§7 ▎ Aqua and Blue chat colour",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/group.donator.diver")
                },
                "onCheck"    : { Player player ->
                    return player.hasPermission("group.donator.diver")
                },
                "onUpgrade"  : { Player player ->
                    return player.hasPermission("group.donator.surfer")
                },
                "upgradable" : "surfer",
                "upgradeCost": 200
        ],
        "sailor"       : [
                "coins"      : 1100,
                "category"   : "ranks",
                "item"       : Material.IRON_INGOT,
                "displayName": "§e§lSailor Rank",
                "description": [
                        "",
                        "§e ▎ 1.3x Sell Multiplier",
                        "§7 ▎ Join the server when full",
                        "§e ▎ 3% Robot Buff",
                        "§7 ▎ Access to /shout (30m cooldown)",
                        "§e ▎ Access up to /pv 6",
                        "§7 ▎ Access to /enderchest",
                        "§e ▎ Ability to /sethome 4 times",
                        "§7 ▎ Auction 4 items at once",
                        "§e ▎ Access to sailor kit",
                        "§7 ▎ 7.5% Faster /printer",
                        "§e ▎ 7.5% Faster /recycler",
                        "§7 ▎ Aqua, Blue, Yellow and Green chat colour",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/group.donator.sailor")
                },
                "onCheck"    : { Player player ->
                    return player.hasPermission("group.donator.sailor")
                },
                "onUpgrade"  : { Player player ->
                    return player.hasPermission("group.donator.diver")
                },
                "upgradable" : "diver",
                "upgradeCost": 450
        ],
        "captain"      : [
                "coins"      : 1800,
                "category"   : "ranks",
                "item"       : Material.DIAMOND,
                "displayName": "§d§lCaptain Rank",
                "description": [
                        "",
                        "§d ▎ 1.4x Sell Multiplier",
                        "§7 ▎ Join the server when full",
                        "§d ▎ 5% Robot Buff",
                        "§7 ▎ Access to /shout (25m cooldown)",
                        "§d ▎ Access up to /pv 8",
                        "§7 ▎ Access to /enderchest",
                        "§d ▎ Ability to /sethome 5 times",
                        "§7 ▎ Ability to /invsee other players inventories",
                        "§d ▎ Auction 5 items at once",
                        "§7 ▎ Access to captain kit",
                        "§d ▎ 10% Faster /printer",
                        "§7 ▎ 10% Faster /recycler",
                        "§d ▎ Aqua, Blue, Yellow, Green, Pink",
                        "§7 ▎ and Purple chat colours",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/group.donator.captain")
                },
                "onCheck"    : { Player player ->
                    return player.hasPermission("group.donator.captain")
                },
                "onUpgrade"  : { Player player ->
                    return player.hasPermission("group.donator.sailor")
                },
                "upgradable" : "sailor",
                "upgradeCost": 600
        ],
        "pirate"       : [
                "coins"      : 2400,
                "category"   : "ranks",
                "item"       : Material.EMERALD,
                "displayName": "§6§lP§3§lI§6§lR§3§lA§6§lT§3§lE§r §6§lRank",
                "description": [
                        "",
                        "§3 ▎ 1.5x Sell Multiplier",
                        "§7 ▎ Join the server when full",
                        "§3 ▎ 7% Robot Buff",
                        "§7 ▎ Access to /shout (20m cooldown)",
                        "§3 ▎ Access to /supershout (25m cooldown)",
                        "§7 ▎ Access up to /pv 10",
                        "§3 ▎ Access to /enderchest",
                        "§7 ▎ Ability to /invsee other players inventories",
                        "§3 ▎ Ability to /fix hand and /fix all",
                        "§7 ▎ Ability to /sethome 6 times",
                        "§3 ▎ Auction 8 items at once",
                        "§7 ▎ Access to pirate kit",
                        "§3 ▎ 12.5% Faster /printer",
                        "§7 ▎ 12.5% Faster /recycler",
                        "§3 ▎ Aqua, Blue, Yellow, Green, Pink,",
                        "§7 ▎ Purple, Orange and Light Green chat colours",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/group.donator.pirate")
                },
                "onCheck"    : { Player player ->
                    return player.hasPermission("group.donator.pirate")
                },
                "onUpgrade"  : { Player player ->
                    return player.hasPermission("group.donator.captain")
                },
                "upgradable" : "captain",
                "upgradeCost": 750
        ],
        "admiral"      : [
                "coins"      : 3500,
                "category"   : "ranks",
                "item"       : Material.END_CRYSTAL,
                "displayName": "§b§lA§c§lD§b§lM§c§lI§b§lR§c§lA§b§lL§r §b§lRank",
                "description": [
                        "",
                        "§c ▎ 1.6x Sell Multiplier",
                        "§7 ▎ Join the server when full",
                        "§c ▎ 10% Robot Buff",
                        "§7 ▎ Access to /shout (15m cooldown)",
                        "§c ▎ Access to /supershout (20m cooldown)",
                        "§7 ▎ Access up to /pv 12",
                        "§c ▎ Access to /enderchest",
                        "§7 ▎ Ability to /invsee other players inventories",
                        "§c ▎ Ability to /fix hand and /fix all",
                        "§7 ▎ Ability to /heal",
                        "§c ▎ Ability to /sethome 7 times",
                        "§7 ▎ Auction 10 items at once",
                        "§c ▎ Access to admiral kit",
                        "§7 ▎ 15% Faster /printer",
                        "§c ▎ 15% Faster /recycler",
                        "§7 ▎ Aqua, Blue, Yellow, Green, Pink,",
                        "§c ▎ Purple, Orange and Light Green chat colours",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/group.donator.admiral")
                },
                "onCheck"    : { Player player ->
                    return player.hasPermission("group.donator.admiral")
                },
                "onUpgrade"  : { Player player ->
                    return player.hasPermission("group.donator.pirate")
                },
                "upgradable" : "pirate",
                "upgradeCost": 1100
        ],
        "medusa"       : [
                "coins"      : 5200,
                "category"   : "ranks",
                "item"       : Material.NETHER_STAR,
                "displayName": "§a§lM§2§lE§a§lD§2§lU§a§lS§2§lA§r §a§lRank",
                "description": [
                        "",
                        "§a ▎ 1.8x Sell Multiplier",
                        "§7 ▎ Join the server when full",
                        "§a ▎ 15% Robot Buff",
                        "§7 ▎ Lifetime Premium",
                        "§a ▎ Access to /shout (10m cooldown)",
                        "§7 ▎ Access to /supershout (15m cooldown)",
                        "§a ▎ Access up to /pv 14",
                        "§7 ▎ Access to /enderchest",
                        "§a ▎ Ability to /invsee other players inventories",
                        "§7 ▎ Ability to /fix hand and /fix all",
                        "§a ▎ Ability to /heal",
                        "§7 ▎ Access to /skull and /hat",
                        "§a ▎ Ability to /sethome 15 times",
                        "§7 ▎ Auction 15 items at once",
                        "§a ▎ Access to medusa kit",
                        "§7 ▎ 15% Faster /printer",
                        "§a ▎ 15% Faster /recycler",
                        "§7 ▎ Access to all nickname colours",
                        "§a ▎ Access to all emotes",
                        "§7 ▎ Aqua, Blue, Yellow, Green, Pink,",
                        "§a ▎ Purple, Orange and Light Green chat colours",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/group.donator.medusa")
                },
                "onCheck"    : { Player player ->
                    return player.hasPermission("group.donator.medusa")
                },
                "onUpgrade"  : { Player player ->
                    return player.hasPermission("group.donator.admiral")
                },
                "upgradable" : "admiral",
                "upgradeCost": 1800
        ],
        "poseidon"     : [
                "coins"      : 9500,
                "category"   : "ranks",
                "item"       : Material.NETHER_STAR,
                "displayName": "§b§lP§9§lO§3§lS§b§lE§9§lI§3§lD§b§lO§9§lN§r §b§lRank",
                "description": [
                        "",
                        "§b ▎ 2x Sell Multiplier",
                        "§7 ▎ Instant Fortune 50",
                        "§b ▎ Join the server when full",
                        "§7 ▎ 15% Robot Buff",
                        "§b ▎ Lifetime Premium",
                        "§7 ▎ Access to /shout (10m cooldown)",
                        "§b ▎ Access to /supershout (15m cooldown)",
                        "§7 ▎ Access up to /pv 16",
                        "§b ▎ Access to /enderchest",
                        "§7 ▎ Ability to /invsee other players inventories",
                        "§b ▎ Ability to /fix hand and /fix all",
                        "§7 ▎ Ability to /heal",
                        "§b ▎ Access to /skull and /hat",
                        "§7 ▎ Ability to /sethome 15 times",
                        "§b ▎ Auction 15 items at once",
                        "§7 ▎ Access to poseidon kit",
                        "§b ▎ 20% Faster /printer",
                        "§7 ▎ 20% Faster /recycler",
                        "§b ▎ Access to all nickname colours",
                        "§7 ▎ Access to all emotes",
                        "§b ▎ Aqua, Blue, Yellow, Green, Pink,",
                        "§7 ▎ Purple, Orange and Light Green chat colours",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getName()} ${temple}/group.donator.poseidon")
                },
                "onCheck"    : { Player player ->
                    return player.hasPermission("group.donator.poseidon")
                },
                "onUpgrade"  : { Player player ->
                    return player.hasPermission("group.donator.medusa")
                },
                "upgradable" : "medusa",
                "upgradeCost": 4000
        ],
        "lobster"      : [
                "coins"      : 200,
                "category"   : "crates",
                "item"       : Material.TRIPWIRE_HOOK,
                "displayName": "§b§l5x Lobster Keys",
                "description": [
                        "§7",
                        "§7§oReceive §b§o5x Lobster Crate Keys",
                        "§7§othat you can open at /spawn.",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecrate ${player.getName()} lobster 5")
                }
        ],
        "dolphin"      : [
                "coins"      : 500,
                "category"   : "crates",
                "item"       : Material.TRIPWIRE_HOOK,
                "displayName": "§d§l5x Dolphin Keys",
                "description": [
                        "§7",
                        "§7§oReceive §d§o5x Dolphin Crate Keys",
                        "§7§othat you can open at /spawn.",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecrate ${player.getName()} dolphin 5")
                }
        ],
        "shark"        : [
                "coins"      : 750,
                "category"   : "crates",
                "item"       : Material.TRIPWIRE_HOOK,
                "displayName": "§f§l5x Shark Keys",
                "description": [
                        "§7",
                        "§7§oReceive §f§o5x Shark Crate Keys",
                        "§7§othat you can open at /spawn.",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecrate ${player.getName()} shark 5")
                }
        ],
        "whale"        : [
                "coins"      : 1100,
                "category"   : "crates",
                "item"       : Material.TRIPWIRE_HOOK,
                "displayName": "§e§l5x Whale Keys",
                "description": [
                        "§7",
                        "§7§oReceive §e§o5x Whale Crate Keys",
                        "§7§othat you can open at /spawn.",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecrate ${player.getName()} whale 5")
                }
        ],
        "trident"      : [
                "coins"      : 1800,
                "category"   : "crates",
                "item"       : Material.TRIPWIRE_HOOK,
                "displayName": "§6§l5x Trident Keys",
                "description": [
                        "§7",
                        "§7§oReceive §6§o5x Trident Crate Keys",
                        "§7§othat you can open at /spawn.",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecrate ${player.getName()} trident 5")
                }
        ],
        "robot"        : [
                "coins"      : 200,
                "category"   : "crates",
                "item"       : Material.TRIPWIRE_HOOK,
                "displayName": "§9§l1x Robot Key",
                "description": [
                        "§7",
                        "§7§oReceive §9§o1x Robot Crate Key",
                        "§7§othat you can open at /spawn.",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecrate ${player.getName()} robot 1")
                }
        ],
        "pet"          : [
                "coins"      : 200,
                "category"   : "crates",
                "item"       : Material.TRIPWIRE_HOOK,
                "displayName": "§6§l1x Pet Key",
                "description": [
                        "§7",
                        "§7§oReceive §6§o1x Pet Crate Key",
                        "§7§othat you can open at /spawn.",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecrate ${player.getName()} pet 1")
                }
        ],
        "skin"         : [
                "coins"      : 700,
                "category"   : "crates",
                "item"       : Material.TRIPWIRE_HOOK,
                "displayName": "§5§l5x Skin Keys",
                "description": [
                        "§7",
                        "§7§oReceive §5§o5x Skin Crate Keys",
                        "§7§othat you can open at /spawn.",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecrate ${player.getName()} skin 5")
                }
        ],
        "monthly"      : [
                "coins"      : 1800,
                "category"   : "crates",
                "item"       : Material.TRIPWIRE_HOOK,
                "displayName": "§c§l1x Monthly Key",
                "description": [
                        "§7",
                        "§7§oReceive §c§o1x Monthly Crate Key",
                        "§7§othat you can open at /spawn.",
                        ""
                ],
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecrate ${player.getName()} monthly 1")
                }
        ],

//        "gkitbundle"   : [
//                "coins"      : 0,
//                "category"   : "gkits",
//                "item"       : Material.ENDER_CHEST,
//                "displayName": "§b§lGKit Information",
//                "description": [
//                        "§7",
//                        "§7In order to gain access",
//                        "§7to any GKits you will need",
//                        "§7to head over to our store!",
//                        "§cstore.starcade.org",
//                        "§7GKits are a subscription",
//                        "§7where every day you'll be able",
//                        "§7to redeem a GKit earning",
//                        "§7Keys, Drills, Coins and much more!",
//                ],
//                "onCheck"    : { Player player ->
//                    return false
//                },
//                "onPurchase" : { Player player ->
//
//                }
//        ],
        "storm"        : [
                "coins"      : 1200,
                "category"   : "gkits",
                "item"       : Material.ENDER_CHEST,
                "displayName": "§b§l1x Storm Gkit",
                "description": [
                        "§7",
                        "§7You will gain access to the §bStorm GKit",
                        "§7",
                        "§7Every §f24 Hours §7you will get the following:",
                        "§71x Random Crate Key",
                        "§71x Random E-Token Pouch",
                        "§71x Random Booster",
                ],
                "onCheck"    : { Player player ->
                    return player.hasPermission("kits.storm")
                },
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getUniqueId()} ${temple}/kits.storm_gkit")
                }
        ],
        "misty"        : [
                "coins"      : 1200,
                "category"   : "gkits",
                "item"       : Material.ENDER_CHEST,
                "displayName": "§9§l1x Misty Gkit",
                "description": [
                        "§7",
                        "§7You will gain access to the §9Misty GKit",
                        "§7",
                        "§7Every §f24 Hours §7you will get the following:",
                        "§71x Random Crate Key",
                        "§71x Random E-Token Pouch",
                        "§71x Random Booster",
                ],
                "onCheck"    : { Player player ->
                    return player.hasPermission("kits.misty_gkit")
                },
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getUniqueId()} ${temple}/kits.misty_gkit")
                }
        ],
        "typhoon"      : [
                "coins"      : 1200,
                "category"   : "gkits",
                "item"       : Material.ENDER_CHEST,
                "displayName": "§c§l1x Typhoon Gkit",
                "description": [
                        "§7",
                        "§7You will gain access to the §cTyphoon GKit",
                        "§7",
                        "§7Every §f24 Hours §7you will get the following:",
                        "§71x Random Crate Key",
                        "§71x Random E-Token Pouch",
                        "§71x Random Booster",
                ],
                "onCheck"    : { Player player ->
                    return player.hasPermission("kits.typhoon")
                },
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getUniqueId()} ${temple}/kits.typhoon_gkit")
                }
        ],
        "lagoon"       : [
                "coins"      : 1200,
                "category"   : "gkits",
                "item"       : Material.ENDER_CHEST,
                "displayName": "§a§l1x Lagoon Gkit",
                "description": [
                        "§7",
                        "§7You will gain access to the §aLagoon GKit",
                        "§7",
                        "§7Every §f24 Hours §7you will get the following:",
                        "§71x Random Crate Key",
                        "§71x Random E-Token Pouch",
                        "§71x Random Booster",
                ],
                "onCheck"    : { Player player ->
                    return player.hasPermission("kits.lagoon")
                },
                "onPurchase" : { Player player ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "padd ${player.getUniqueId()} ${temple}/kits.lagoon_gkit")
                }
        ],
] as Map<String, Map<String, Object>>)


Commands.create().assertPlayer().assertOp().handler({ c ->
    def shop = Bukkit.getWorld("shop_world")
    HashMap<String, Location> locations = [
            "lobster": new Location(shop, 112.5D, 105, -29.5D, 0, 69),
            "dolphin": new Location(shop, 116.5D, 105, -29.5D, 0, 69),
            "shark"  : new Location(shop, 120.5D, 105, -29.5D, 0, 69),
            "whale"  : new Location(shop, 110.5D, 105, -13.5D, 180, 69),
            "trident": new Location(shop, 113.5D, 105, -13.5D, 180, 69),
            "robot"  : new Location(shop, 116.5D, 105, -13.5D, 180, 69),
            "pet"    : new Location(shop, 119.5D, 105, -13.5D, 180, 69),
            "skin"   : new Location(shop, 122.5D, 105, -13.5D, 180, 69),
            "monthly": new Location(shop, 130.5D, 103, -23.5D, 90, 69)
    ]

    for (Map.Entry<String, Location> entry : locations) {
        Location loc = entry.value.clone()
        if (loc.getPitch() != 69) continue

        ItemStack item = FastItemUtils.createItem(Material.STONE_BUTTON, "", [])
        FastItemUtils.setCustomModelData(item, crateId.get(entry.key))

        Double angle = loc.getYaw()
        Double angleOffset = 30 + 90
        angle += angleOffset
        double x = Math.cos(Math.toRadians(angle))
        double z = Math.sin(Math.toRadians(angle))

        z *= 0.58
        x *= 0.58

        double offsetX = 0
        double offsetZ = 0

        loc.setX(loc.getX() - x + offsetX)
        loc.setZ(loc.getZ() - z + offsetZ)
        loc.setY(loc.getY() - 0.92)

        ArmorStand eas = loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand
        eas.setGravity(false)
        eas.setMarker(true)
        eas.setArms(true)
        eas.setInvisible(true)
        eas.setInvulnerable(true)
        eas.setRightArmPose(new EulerAngle(Math.toRadians(270), Math.toRadians(0), Math.toRadians(0)))
        eas.setRotation((angle - angleOffset).toFloat(), 0)
        eas.getEquipment().setItem(EquipmentSlot.HAND, item)
        eas.setCustomName("crate_buy_${crateId.get(entry.key)}_${loc.getWorld().getName()}")
    }
}).register("dev/buy/spawnmodels")

Commands.create().assertOp().assertPlayer().handler({ c ->
    World world = c.sender().getWorld()
    Schedulers.sync().execute {
        world.getEntitiesByClass(ArmorStand.class).each {
            if (it.name.startsWith("crate_buy_")) {
                it.remove()
            }
        }
    }
}).register("dev/buy/clear")


//static void spawnNpc(String id, String name, Location location, Object skinHolder = null, Consumer<Player> onClick = null) {
//    NPCTracker npcTracker = NPCRegistry.get().spawn(id, name, location, skinHolder, onClick)
//
//    if (id.contains("autominer")) {
//        npcTracker.addEquipment(EnumItemSlot.HEAD, FastItemUtils.createItem(Material.CHAINMAIL_HELMET, "", []))
//        npcTracker.addEquipment(EnumItemSlot.CHEST, FastItemUtils.createItem(Material.CHAINMAIL_CHESTPLATE, "", []))
//        npcTracker.addEquipment(EnumItemSlot.MAINHAND, FastItemUtils.createItem(Material.IRON_PICKAXE, "", []))
//        npcTracker.turnTowardPlayers = false
//        Schedulers.async().runRepeating({
//            npcTracker.swing()
//        }, 3L, 3L)
//    } else {
//        npcTracker.turnTowardPlayers = true
//    }
//
//    if (id.contains("crate_")) {
//        HashMap<String, Integer> crateId = [
//                "lobster": 2,
//                "dolphin": 1,
//                "shark"  : 6,
//                "whale"  : 10,
//                "trident": 8,
//                "robot"  : 5,
//                "pet"    : 4,
//                "monthly": 3,
//                "skin"   : 7
//        ]
//
//        ItemStack item = FastItemUtils.createItem(Material.TRIPWIRE_HOOK, "", [])
//        Integer crateKeyId = crateId.get(id.split("_")[0])
//
//        FastItemUtils.setCustomModelData(item, crateKeyId)
//        FastItemUtils.addGlow(item)
//        npcTracker.setGlow(EnumChatFormat.AQUA)
//
//        npcTracker.addEquipment(EnumItemSlot.MAINHAND, item)
//    }
//}

Starlight.watch("scripts/shared/features/Store.groovy")