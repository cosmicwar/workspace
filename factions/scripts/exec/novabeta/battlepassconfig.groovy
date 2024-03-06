package scripts.exec.novabeta

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.time.DurationFormatter
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.utils.NumberUtils

import java.time.Duration
import java.util.concurrent.TimeUnit

Exports.ptr("battlebass_xp", [
        "blockBreakXpChance"   : 0.01D,
        "blockBreakXp"         : 1,
        "challengeCompletionXp": 1500
])

Exports.ptr("battlebass_challenges", [
        "BREAK_BLOCKS"            : [formatter: { long goal -> "§fBreak §d${NumberUtils.format(goal)}§f blocks" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} blocks" }, min: 750L, max: 1500L, tierAdjuster: { int tier, long rolled -> return tier * rolled }],
        "OPEN_TREASURES_COMMON"   : [formatter: { long goal -> "§fOpen §d${NumberUtils.format(goal)}§f common shiny treasures" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} treasures" }, min: 3L, max: 10L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 25) + 1) * rolled }, group: 1],
        "OPEN_TREASURES_RARE"     : [formatter: { long goal -> "§fOpen §d${NumberUtils.format(goal)}§f rare shiny treasures" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} treasures" }, min: 3L, max: 7L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 25) + 1) * rolled }, group: 1],
        "OPEN_TREASURES_EPIC"     : [formatter: { long goal -> "§fOpen §d${NumberUtils.format(goal)}§f epic shiny treasures" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} treasures" }, min: 2L, max: 5L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 25) + 1) * rolled }, group: 1],
        "OPEN_TREASURES_LEGENDARY": [formatter: { long goal -> "§fOpen §d${NumberUtils.format(goal)}§f legendary shiny treasures" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} treasures" }, min: 1L, max: 2L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 25) + 1) * rolled }, group: 1],
        "PVE_KILLS"               : [formatter: { long goal -> "§fKill §d${NumberUtils.format(goal)}§f mobs in the beacon mine" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} mob kills" }, min: 5L, max: 25L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 10) + 1) * rolled }],
        "KILL_DUNGEON_MOBS"       : [formatter: { long goal -> "§fKill §d${NumberUtils.format(goal)}§f dungeon mobs" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} mob kills" }, min: 5L, max: 25L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 10) + 1) * rolled }],
        "OPEN_DUNGEON_CHESTS"     : [formatter: { long goal -> "§fOpen §d${NumberUtils.format(goal)}§f dungeon loot chests" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} chests opened" }, min: 2L, max: 5L, tierAdjuster: { int tier, long rolled -> return rolled }],
        "BOSS_KILLS"              : [formatter: { long goal -> "§fKill §d${NumberUtils.format(goal)}§f bosses in the beacon mine (top 5)" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} boss kills" }, min: 1L, max: 3L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 25) + 1) * rolled }],
        "PVE_MINE_BEACONS"        : [formatter: { long goal -> "§fMine §d${NumberUtils.format(goal)}§f beacons in the beacon mine" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} beacons mined" }, min: 10L, max: 25L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 2) + 1) * rolled }],
        "OPEN_CRATES"             : [formatter: { long goal -> "§fOpen §d${NumberUtils.format(goal)}§f crates" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} crates opened" }, min: 1L, max: 3L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 15) + 1) * rolled }],
        "AUTOMINER_BREAK_BLOCKS"  : [formatter: { long goal -> "§fHave your Autominer mine §d${NumberUtils.format(goal)}§f blocks" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} blocks broken" }, min: 6000L, max: 8000L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 4) + 1) * rolled }],
        "ENCHANT_LEVELS"          : [formatter: { long goal -> "§fEnchant §d${NumberUtils.format(goal)}§f levels on your pickaxe" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} enchant levels" }, min: 50L, max: 100L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 10) + 1) * rolled }],
        "USE_WAND"                : [formatter: { long goal -> "§fUse a wand §d${NumberUtils.format(goal)}§f time(s)" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} wand uses" }, min: 10L, max: 20L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 5) + 1) * rolled }],
        "ROBOT_MONEY"             : [formatter: { long goal -> "§fCollect ${(Exports.ptr("e_tokens") as CurrencyStorage).map(BigDecimal.valueOf(goal))}§f from your robots" }, progressFormatter: { long progress, long goal -> "${(Exports.ptr("e_tokens") as CurrencyStorage).map(BigDecimal.valueOf(progress))} / ${(Exports.ptr("e_tokens") as CurrencyStorage).map(BigDecimal.valueOf(goal))}" }, min: 5000000L, max: 10000000L, tierAdjuster: { int tier, long rolled -> return tier * rolled }],
        "MERGE_ROBOTS"            : [formatter: { long goal -> "§fMerge §d${NumberUtils.format(goal)}§f robots" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} robots merged" }, min: 5L, max: 10L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 20) + 1) * rolled }],
        "CITY_CRAFTING"           : [formatter: { long goal -> "§fCraft §d${NumberUtils.format(goal)}§f items at your /city" }, progressFormatter: { long progress, long goal -> "${NumberUtils.format(progress)} / ${NumberUtils.format(goal)} items crafted" }, min: 2L, max: 4L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 20) + 1) * rolled }],
        "ONLINE_MINUTES"          : [formatter: { long goal -> "§fBe online for §d${DurationFormatter.LONG.format(Duration.ofMillis(TimeUnit.MINUTES.toMillis(goal)))}" }, progressFormatter: { long progress, long goal -> "${DurationFormatter.LONG.format(Duration.ofMillis(TimeUnit.MINUTES.toMillis(progress)))} / ${DurationFormatter.LONG.format(Duration.ofMillis(TimeUnit.MINUTES.toMillis(goal)))}" }, min: 30L, max: 120L, tierAdjuster: { int tier, long rolled -> return (Math.floor(tier / 10) + 1) * rolled }]
])

Exports.ptr("battlepass", [
        1  : [
                free   : [
                        displayName: "§e§lTier 1 Free Reward",
                        description: ["§e1x Vote Key", "§e6 Hours of Premium"],
                        commands   : ["givecrate %player% vote 1", "givepremiumnote %player% 6h"]
                ],
                premium: [
                        displayName: "§6§lTier 1 Premium Reward",
                        description: ["§e2x Whale Keys"],
                        commands   : ["givecrate %player% whale 2"]
                ]
        ],
        2  : [
                free: [
                        displayName: "§e§lTier 2 Free Reward",
                        description: ["§e1x Random Small Pouch"],
                        commands   : ["giverandompouch %player% small small"]
                ]
        ],
        3  : [
                premium: [
                        displayName: "§6§lTier 3 Premium Reward",
                        description: ["§e1x Stone Robot"],
                        commands   : ["giverobot %player% stone"]
                ]
        ],
        4  : [
                free: [
                        displayName: "§e§lTier 4 Free Reward",
                        description: ["§e5x Frag Grenades"],
                        commands   : ["givegrenade %player% frag 5"]
                ]
        ],
        5  : [
                premium: [
                        displayName: "§6§lTier 5 Premium Reward",
                        description: ["§e1x Small Booster Pouch"],
                        commands   : ["givepouch %player% booster small"]
                ]
        ],
        6  : [
                free: [
                        displayName: "§e§lTier 6 Free Reward",
                        description: ["§e1x Random Small Pouch"],
                        commands   : ["giverandompouch %player% small small"]
                ]
        ],
        7  : [
                premium: [
                        displayName: "§6§lTier 7 Premium Reward",
                        description: ["§e1x Small E-Token Pouch"],
                        commands   : ["givepouch %player% etoken small"]
                ]
        ],
        8  : [
                free: [
                        displayName: "§e§lTier 8 Free Reward",
                        description: ["§e50 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 50"]
                ]
        ],
        9  : [
                premium: [
                        displayName: "§6§lTier 9 Premium Reward",
                        description: ["§e1x Small Crystal Pouch"],
                        commands   : ["givepouch %player% crystal small"]
                ]
        ],
        10 : [
                free   : [
                        displayName: "§e§lTier 10 Free Reward",
                        description: ["§eTier 10 Phantom Pickaxe Gemstone"],
                        commands   : ["givephantompickgem %player% 10 1"]
                ],
                premium: [
                        displayName: "§e§lTier 10 Premium Reward",
                        description: ["§e100 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 100"]
                ]
        ],
        11 : [
                premium: [
                        displayName: "§6§lTier 11 Premium Reward",
                        description: ["§e1x Redstone Robot"],
                        commands   : ["giverobot %player% redstone"]
                ]
        ],
        12 : [
                free: [
                        displayName: "§e§lTier 12 Free Reward",
                        description: ["§e2x Holy Hand Grenades"],
                        commands   : ["givegrenade %player% holy 2"]
                ]
        ],
        13 : [
                premium: [
                        displayName: "§6§lTier 13 Premium Reward",
                        description: ["§e5x Vote Keys"],
                        commands   : ["givecrate %player% vote 5"]
                ]
        ],
        14 : [
                free: [
                        displayName: "§e§lTier 14 Free Reward",
                        description: ["§e1x Random Small Pouch"],
                        commands   : ["giverandompouch %player% small small"]
                ]
        ],
        15 : [
                premium: [
                        displayName: "§6§lTier 15 Premium Reward",
                        description: ["§e1x Small Booster Pouch"],
                        commands   : ["givepouch %player% booster small"]
                ]
        ],
        16 : [
                free: [
                        displayName: "§e§lTier 16 Free Reward",
                        description: ["§e3x Holy Hand Grenades"],
                        commands   : ["givegrenade %player% holy 3"]
                ]
        ],
        17 : [
                premium: [
                        displayName: "§6§lTier 17 Premium Reward",
                        description: ["§e1x Small Beacon Pouch"],
                        commands   : ["givepouch %player% beacon small"]
                ]
        ],
        18 : [
                free: [
                        displayName: "§e§lTier 18 Free Reward",
                        description: ["§e3x Glitched Grenades"],
                        commands   : ["givegrenade %player% glitched 3"]
                ]
        ],
        19 : [
                premium: [
                        displayName: "§6§lTier 19 Premium Reward",
                        description: ["§e1x City Level"],
                        commands   : ["givecitylevels %player% 1"]
                ]
        ],
        20 : [
                free   : [
                        displayName: "§e§lTier 20 Free Reward",
                        description: ["§e1x Shark Key"],
                        commands   : ["givecrate %player% shark 1"]
                ],
                premium: [
                        displayName: "§e§lTier 20 Premium Reward",
                        description: ["§e100 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 100"]
                ]
        ],
        21 : [
                premium: [
                        displayName: "§6§lTier 21 Premium Reward",
                        description: ["§e1x Lapis Robot"],
                        commands   : ["giverobot %player% lapis"]
                ]
        ],
        22 : [
                free: [
                        displayName: "§e§lTier 22 Free Reward",
                        description: ["§e1x Random Small-Medium Pouch"],
                        commands   : ["giverandompouch %player% small medium"]
                ]
        ],
        23 : [
                premium: [
                        displayName: "§6§lTier 23 Premium Reward",
                        description: ["§e4x Lobster Keys"],
                        commands   : ["givecrate %player% lobster 4"]
                ]
        ],
        24 : [
                free: [
                        displayName: "§e§lTier 24 Free Reward",
                        description: ["§e50 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 50"]
                ]
        ],
        25 : [
                premium: [
                        displayName: "§6§lTier 25 Premium Reward",
                        description: ["§e1x Medium Booster Pouch"],
                        commands   : ["givepouch %player% booster medium"]
                ]
        ],
        26 : [
                free: [
                        displayName: "§e§lTier 26 Free Reward",
                        description: ["§e5x Glitched Grenades"],
                        commands   : ["givegrenade %player% glitched 5"]
                ]
        ],
        27 : [
                premium: [
                        displayName: "§6§lTier 27 Premium Reward",
                        description: ["§e1x Medium E-Token Pouch"],
                        commands   : ["givepouch %player% etoken medium"]
                ]
        ],
        28 : [
                free: [
                        displayName: "§e§lTier 28 Free Reward",
                        description: ["§e1x Random Small-Medium Pouch"],
                        commands   : ["giverandompouch %player% small medium"]
                ]
        ],
        29 : [
                premium: [
                        displayName: "§6§lTier 29 Premium Reward",
                        description: ["§e1x Medium Crystal Pouch"],
                        commands   : ["givepouch %player% crystal medium"]
                ]
        ],
        30 : [
                free   : [
                        displayName: "§e§lTier 30 Free Reward",
                        description: ["§e1x Whale Key"],
                        commands   : ["givecrate %player% whale 1"]
                ],
                premium: [
                        displayName: "§e§lTier 30 Premium Reward",
                        description: ["§e50 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 50"]
                ]
        ],
        31 : [
                premium: [
                        displayName: "§6§lTier 31 Premium Reward",
                        description: ["§e1x Iron Robot"],
                        commands   : ["giverobot %player% iron"]
                ]
        ],
        32 : [
                free: [
                        displayName: "§e§lTier 32 Free Reward",
                        description: ["§e3x Napalm Grenades"],
                        commands   : ["givegrenade %player% napalm 3"]
                ]
        ],
        33 : [
                premium: [
                        displayName: "§6§lTier 33 Premium Reward",
                        description: ["§e3x Dolphin Keys"],
                        commands   : ["givecrate %player% dolphin 3"]
                ]
        ],
        34 : [
                free: [
                        displayName: "§e§lTier 34 Free Reward",
                        description: ["§e1x Random Small-Large Pouch"],
                        commands   : ["giverandompouch %player% small large"]
                ]
        ],
        35 : [
                premium: [
                        displayName: "§6§lTier 35 Premium Reward",
                        description: ["§e1x Medium Booster Pouch"],
                        commands   : ["givepouch %player% booster medium"]
                ]
        ],
        36 : [
                free: [
                        displayName: "§e§lTier 36 Free Reward",
                        description: ["§e3x Napalm Grenades"],
                        commands   : ["givegrenade %player% napalm 3"]
                ]
        ],
        37 : [
                premium: [
                        displayName: "§6§lTier 37 Premium Reward",
                        description: ["§e1x Medium Beacon Pouch"],
                        commands   : ["givepouch %player% beacon medium"]
                ]
        ],
        38 : [
                free: [
                        displayName: "§e§lTier 38 Free Reward",
                        description: ["§e1x Random Small Pouch"],
                        commands   : ["giverandompouch %player% small small"]
                ]
        ],
        39 : [
                premium: [
                        displayName: "§6§lTier 39 Premium Reward",
                        description: ["§e2x City Levels"],

                        commands   : ["givecitylevels %player% 2"]
                ]
        ],
        40 : [
                free   : [
                        displayName: "§e§lTier 40 Free Reward",
                        description: ["§e1x Gold Robot"],
                        commands   : ["giverobot %player% gold"]
                ],
                premium: [
                        displayName: "§e§lTier 40 Premium Reward",
                        description: ["§e100 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 100"]
                ]
        ],
        41 : [
                premium: [
                        displayName: "§6§lTier 41 Premium Reward",
                        description: ["§e1x Gold Robot"],
                        commands   : ["giverobot %player% gold"]
                ]
        ],
        42 : [
                free: [
                        displayName: "§e§lTier 42 Free Reward",
                        description: ["§e5x Napalm Grenades"],
                        commands   : ["givegrenade %player% napalm 5"]
                ]
        ],
        43 : [
                premium: [
                        displayName: "§6§lTier 43 Premium Reward",
                        description: ["§e3x Shark Keys"],
                        commands   : ["givecrate %player% shark 3"]
                ]
        ],
        44 : [
                free: [
                        displayName: "§e§lTier 44 Free Reward",
                        description: ["§e1x Random Small-Medium Pouch"],
                        commands   : ["giverandompouch %player% small medium"]
                ]
        ],
        45 : [
                premium: [
                        displayName: "§6§lTier 45 Premium Reward",
                        description: ["§e1x Large Booster Pouch"],
                        commands   : ["givepouch %player% booster large"]
                ]
        ],
        46 : [
                free: [
                        displayName: "§e§lTier 46 Free Reward",
                        description: ["§e3x Thermite Grenades"],
                        commands   : ["givegrenade %player% thermite 3"]
                ]
        ],
        47 : [
                premium: [
                        displayName: "§6§lTier 47 Premium Reward",
                        description: ["§e1x Large E-Token Pouch"],
                        commands   : ["givepouch %player% etoken large"]
                ]
        ],
        48 : [
                free: [
                        displayName: "§e§lTier 48 Free Reward",
                        description: ["§e50 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 50"]
                ]
        ],
        49 : [
                premium: [
                        displayName: "§6§lTier 49 Premium Reward",
                        description: ["§e1x Ascendant Gemstone"],
                        commands   : ["giveascendantenchant %player% 5"]
                ]
        ],
        50 : [
                free   : [
                        displayName: "§e§lTier 50 Free Reward",
                        description: ["§e1x XL E-Token Pouch"],
                        commands   : ["givepouch %player% etoken xl"]
                ],
                premium: [
                        displayName: "§e§lTier 50 Premium Reward",
                        description: ["§e100 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 100"]
                ]
        ],
        51 : [
                premium: [
                        displayName: "§6§lTier 51 Premium Reward",
                        description: ["§e1x Diamond Robot"],
                        commands   : ["giverobot %player% diamond"]
                ]
        ],
        52 : [
                free: [
                        displayName: "§e§lTier 52 Free Reward",
                        description: ["§e1x Random Small-XL Pouch"],
                        commands   : ["giverandompouch %player% small xl"]
                ]
        ],
        53 : [
                premium: [
                        displayName: "§6§lTier 53 Premium Reward",
                        description: ["§e3x Whale Keys"],
                        commands   : ["givecrate %player% whale 3"]
                ]
        ],
        54 : [
                free: [
                        displayName: "§e§lTier 54 Free Reward",
                        description: ["§e5x Thermite Grenades"],
                        commands   : ["givegrenade %player% thermite 5"]
                ]
        ],
        55 : [
                premium: [
                        displayName: "§6§lTier 55 Premium Reward",
                        description: ["§e1x Large Booster Pouch"],
                        commands   : ["givepouch %player% booster large"]
                ]
        ],
        56 : [:],
        57 : [
                premium: [
                        displayName: "§6§lTier 57 Premium Reward",
                        description: ["§e1x Large Beacon Pouch"],
                        commands   : ["givepouch %player% beacon large"]
                ]
        ],
        58 : [
                free: [
                        displayName: "§e§lTier 58 Free Reward",
                        description: ["§e1x Random Medium Pouch"],
                        commands   : ["giverandompouch %player% medium medium"]
                ]
        ],
        59 : [
                premium: [
                        displayName: "§6§lTier 59 Premium Reward",
                        description: ["§e2x City Levels"],
                        commands   : ["givecitylevels %player% 2"]
                ]
        ],
        60 : [
                free   : [
                        displayName: "§e§lTier 60 Free Reward",
                        description: ["§e1x Large Beacon Pouch"],
                        commands   : ["givepouch %player% beacon large"]
                ],
                premium: [
                        displayName: "§e§lTier 60 Premium Reward",
                        description: ["§e50 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 50"]
                ]
        ],
        61 : [
                premium: [
                        displayName: "§6§lTier 61 Premium Reward",
                        description: ["§e1x Emerald Robot"],
                        commands   : ["giverobot %player% emerald"]
                ]
        ],
        62 : [
                free: [
                        displayName: "§e§lTier 62 Free Reward",
                        description: ["§e5x Thermite Grenades"],
                        commands   : ["givegrenade %player% thermite 5"]
                ]
        ],
        63 : [
                premium: [
                        displayName: "§6§lTier 63 Premium Reward",
                        description: ["§e3x Trident Keys"],
                        commands   : ["givecrates %player% trident 3"]
                ]
        ],
        64 : [
                free: [
                        displayName: "§e§lTier 64 Free Reward",
                        description: ["§e1x Random Medium-Large Pouch"],
                        commands   : ["giverandompouch %player% medium large"]
                ]
        ],
        65 : [
                premium: [
                        displayName: "§6§lTier 65 Premium Reward",
                        description: ["§e1x XL Booster Pouch"],
                        commands   : ["givepouch %player% booster xl"]
                ]
        ],
        66 : [
                free: [
                        displayName: "§e§lTier 66 Free Reward",
                        description: ["§e1x Nuke"],
                        commands   : ["givegrenade %player% nuke 1"]
                ]
        ],
        67 : [
                premium: [
                        displayName: "§6§lTier 67 Premium Reward",
                        description: ["§e1x XL E-Token Pouch"],
                        commands   : ["givepouch %player% etoken xl"]
                ]
        ],
        68 : [:],
        69 : [
                premium: [
                        displayName: "§6§lTier 69 Premium Reward",
                        description: ["§e1x XL Crystal Pouch"],
                        commands   : ["givepouch %player% crystal xl"]
                ]
        ],
        70 : [
                free   : [
                        displayName: "§e§lTier 70 Free Reward",
                        description: ["§e5x Shark Keys"],
                        commands   : ["givecrate %player% shark 5"]
                ],
                premium: [
                        displayName: "§e§lTier 70 Premium Reward",
                        description: ["§e100 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 100"]
                ]
        ],
        71 : [
                premium: [
                        displayName: "§6§lTier 71 Premium Reward",
                        description: ["§e1x Obsidian Robot"],
                        commands   : ["giverobot %player% obsidian"]
                ]
        ],
        72 : [
                free: [
                        displayName: "§e§lTier 72 Free Reward",
                        description: ["§e1x Random Medium-Large Pouch"],
                        commands   : ["giverandompouch %player% medium large"]
                ]
        ],
        73 : [
                premium: [
                        displayName: "§6§lTier 73 Premium Reward",
                        description: ["§e1x Robot Key"],
                        commands   : ["givecrate %player% robot 1"]
                ]
        ],
        74 : [:],
        75 : [
                premium: [
                        displayName: "§6§lTier 75 Premium Reward",
                        description: ["§e1x XL Booster Pouch"],
                        commands   : ["givepouch %player% booster xl"]
                ]
        ],
        76 : [
                free: [
                        displayName: "§e§lTier 76 Free Reward",
                        description: ["§e50 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 50"]
                ],
        ],
        77 : [
                premium: [
                        displayName: "§6§lTier 77 Premium Reward",
                        description: ["§e1x XL Beacon Pouch"],
                        commands   : ["givepouch %player% beacon xl"]
                ]
        ],
        78 : [
                free: [
                        displayName: "§e§lTier 78 Free Reward",
                        description: ["§e1x Random Large Pouch"],
                        commands   : ["giverandompouch %player% large large"]
                ]
        ],
        79 : [
                premium: [
                        displayName: "§6§lTier 79 Premium Reward",
                        description: ["§e2x City Levels"],
                        commands   : ["givecitylevels %player% 2"]
                ]
        ],
        80 : [
                free   : [
                        displayName: "§e§lTier 80 Free Reward",
                        description: ["§e1x Massive Booster Pouch", "§e1x Tier 15 Wand Gemstone"],
                        commands   : ["givepouch %player% booster massive", "givewandgem %player% 15 1"]
                ],
                premium: [
                        displayName: "§e§lTier 80 Premium Reward",
                        description: ["§e100 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 100"]
                ]
        ],
        81 : [
                premium: [
                        displayName: "§6§lTier 81 Premium Reward",
                        description: ["§e2x Robot Keys"],
                        commands   : ["givecrate %player% robot 2"]
                ]
        ],
        82 : [
                free: [
                        displayName: "§e§lTier 82 Free Reward",
                        description: ["§e1x Nuke"],
                        commands   : ["givegrenade %player% nuke 1"]
                ]
        ],
        83 : [
                premium: [
                        displayName: "§6§lTier 83 Premium Reward",
                        description: ["§e1x Pet Key"],
                        commands   : ["givecrate %player% pet 1"]
                ]
        ],
        84 : [
                free: [
                        displayName: "§e§lTier 84 Free Reward",
                        description: ["§e1x Random Medium-XL Pouch"],
                        commands   : ["giverandompouch %player% medium xl"]
                ]
        ],
        85 : [
                premium: [
                        displayName: "§6§lTier 85 Premium Reward",
                        description: ["§e1x Massive Booster Pouch"],
                        commands   : ["givepouch %player% booster massive"]
                ]
        ],
        86 : [:],
        87 : [
                premium: [
                        displayName: "§6§lTier 87 Premium Reward",
                        description: ["§e1x Massive E-Token Pouch"],
                        commands   : ["givepouch %player% etoken massive"]
                ]
        ],
        88 : [
                free: [
                        displayName: "§e§lTier 88 Free Reward",
                        description: ["§e1x Nuke"],
                        commands   : ["givegrenade %player% nuke 1"]
                ]],
        89 : [
                premium: [
                        displayName: "§6§lTier 89 Premium Reward",
                        description: ["§e1x Massive Booster Pouch"],
                        commands   : ["givepouch %player% booster massive"]
                ]
        ],
        90 : [
                free   : [
                        displayName: "§e§lTier 90 Free Reward",
                        description: ["§e1x Massive E-Token Pouch", "§eTier 15 Phantom Pickaxe Gem"],
                        commands   : ["givepouch %player% etoken massive", "givephantompickgem %player% 15 1"]
                ],
                premium: [
                        displayName: "§e§lTier 90 Premium Reward",
                        description: ["§e100 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 100"]
                ]
        ],
        91 : [
                premium: [
                        displayName: "§6§lTier 91 Premium Reward",
                        description: ["§e1x Bedrock Robot"],
                        commands   : ["giverobot %player% bedrock 1"]
                ]
        ],
        92 : [
                free: [
                        displayName: "§e§lTier 92 Free Reward",
                        description: ["§e1x Random Medium-XL Pouch"],
                        commands   : ["giverandompouch %player% medium xl"]
                ]
        ],
        93 : [
                premium: [
                        displayName: "§6§lTier 93 Premium Reward",
                        description: ["§e1x Skin Key"],
                        commands   : ["givecrate %player% skin 1"]
                ]
        ],
        94 : [:],
        95 : [
                premium: [
                        displayName: "§6§lTier 95 Premium Reward",
                        description: ["§e1x Massive Booster Pouch"],
                        commands   : ["givepouch %player% booster massive"]
                ]
        ],
        96 : [:],
        97 : [
                premium: [
                        displayName: "§6§lTier 97 Premium Reward",
                        description: ["§e1x Massive Beacon Pouch"],
                        commands   : ["givepouch %player% beacon massive"]
                ]
        ],
        98 : [:],
        99 : [
                premium: [
                        displayName: "§e§lTier 99 Premium Reward",
                        description: ["§e100 Gold Coins"],
                        commands   : ["coinsgive %player% untradable 100"]
                ]
        ],
        100: [
                free   : [
                        displayName: "§e§lTier 100 Free Reward",
                        description: ["§e1x Robot Key", "§e1x Pet Key"],
                        commands   : ["givecrate %player% robot 1", "givecrate %player% pet 1", "rbc &c&l%player% &7&lhas achieved &6&lTier 100 &7&lon the &b&lbattlepass&7!"]
                ],
                premium: [
                        displayName: "§6§lTier 100 Premium Reward",
                        description: ["§e1x Monthly Crate"],
                        commands   : ["givecrate %player% monthly 1"]
                ]
        ],
] as TreeMap)