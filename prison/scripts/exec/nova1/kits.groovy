package scripts.exec.nova1

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.bukkit.Material

Map<String, Map<String, Object>> kits = [
        member      : [
                name    : "&7&lMember",
                icon    : Material.CHEST,
                cooldown: "30m",
                commands: [
                        "armornote member {name}"
                ],
                armor   : [
                        "givecustomitem {name} diamondhelmet 1 name:&7[Member_Helmet] unbreaking:1",
                        "givecustomitem {name} diamondchestplate 1 name:&7[Member_Chestplate] unbreaking:1",
                        "givecustomitem {name} diamondleggings 1 name:&7[Member_Leggings] unbreaking:1",
                        "givecustomitem {name} diamondboots 1 name:&7[Member_Boots] unbreaking:1",
                        "givecustomitem {name} diamondsword 1 name:&7[Member_Sword] sharpness:1 unbreaking:1"
                ]
        ],
        surfer      : [
                name    : "&b&lSurfer",
                icon    : Material.CHEST,
                cooldown: "24h",
                commands: [
                        "armornote surfer {name}",
                        "givepouch {name} random small"
                ],
                armor   : [
                        "givecustomitem {name} diamondhelmet 1 name:&b[Surfer_Helmet] protection:2 unbreaking:1",
                        "givecustomitem {name} diamondchestplate 1 name:&b[Surfer_Chestplate] protection:2 unbreaking:1",
                        "givecustomitem {name} diamondleggings 1 name:&b[Surfer_Leggings] protection:2 unbreaking:1",
                        "givecustomitem {name} diamondboots 1 name:&b[Surfer_Boots] protection:2 unbreaking:1",
                        "givecustomitem {name} diamondsword 1 name:&b[Surfer_Sword] sharpness:2 unbreaking:1",
                        "give {name} golden_apple 4"
                ]
        ],
        diver       : [
                name    : "&9&lDiver",
                icon    : Material.CHEST,
                cooldown: "24h",
                commands: [
                        "armornote diver {name}",
                        "giverandompouch {name} small medium"
                ],
                armor   : [
                        "givecustomitem {name} diamondhelmet 1 name:&9[Diver_Helmet] protection:4 unbreaking:1",
                        "givecustomitem {name} diamondchestplate 1 name:&9[Diver_Chestplate] protection:4 unbreaking:1",
                        "givecustomitem {name} diamondleggings 1 name:&9[Diver_Leggings] protection:4 unbreaking:1",
                        "givecustomitem {name} diamondboots 1 name:&9[Diver_Boots] protection:4 unbreaking:1",
                        "givecustomitem {name} diamondsword 1 name:&b[Diver_Sword] sharpness:2 unbreaking:1",
                        "give {name} golden_apple 8",
                        "give {name} enchanted_golden_apple 1"
                ]
        ],
        sailor      : [
                name    : "&e&lSailor",
                icon    : Material.CHEST,
                cooldown: "24h",
                commands: [
                        "armornote sailor {name}",
                        "giverandompouch {name} small medium"
                ],
                armor   : [
                        "givecustomitem {name} diamondhelmet 1 name:&e[Sailor_Helmet] protection:7 unbreaking:2",
                        "givecustomitem {name} diamondchestplate 1 name:&e[Sailor_Chestplate] protection:7 unbreaking:2",
                        "givecustomitem {name} diamondleggings 1 name:&e[Sailor_Leggings] protection:7 unbreaking:2",
                        "givecustomitem {name} diamondboots 1 name:&e[Sailor_Boots] protection:7 unbreaking:2",
                        "givecustomitem {name} diamondsword 1 name:&e[Sailor_Sword] sharpness:5 unbreaking:2",
                        "give {name} golden_apple 16",
                        "give {name} enchanted_golden_apple 1"
                ]
        ],
        captain     : [
                name    : "&d&lCaptain",
                icon    : Material.CHEST,
                cooldown: "1d",
                commands: [
                        "armornote captain {name}",
                        "giverandompouch {name} small large"
                ],
                armor   : [
                        "givecustomitem {name} diamondhelmet 1 name:&d[Captain_Helmet] protection:10 unbreaking:3",
                        "givecustomitem {name} diamondchestplate 1 name:&d[Captain_Chestplate] protection:10 unbreaking:3",
                        "givecustomitem {name} diamondleggings 1 name:&d[Captain_Leggings] protection:10 unbreaking:3",
                        "givecustomitem {name} diamondboots 1 name:&d[Captain_Boots] protection:10 unbreaking:3",
                        "givecustomitem {name} diamondsword 1 name:&d[Captain_Sword] sharpness:7 unbreaking:3",
                        "give {name} golden_apple 20",
                        "give {name} enchanted_golden_apple 1"
                ]
        ],
        pirate      : [
                name    : "&3&lP&6&li&3&lr&6&la&3&lt&6&le",
                icon    : Material.CHEST,
                cooldown: "24h",
                commands: [
                        "armornote pirate {name}",
                        "giverandompouch {name} small large"
                ],
                armor   : [
                        "givecustomitem {name} diamondhelmet 1 name:&3[&6&lP&3&lI&6&lR&3&lA&6&lT&3&lE&6&l_&3&lH&6&lE&3&lL&6&lM&3&lE&6&lT&3] protection:12 unbreaking:4",
                        "givecustomitem {name} diamondchestplate 1 name:&3[&6&lP&3&lI&6&lR&3&lA&6&lT&3&lE&6&l_&3&lC&6&lH&3&lE&6&lS&3&lT&6&lP&3&lL&6&lA&3&lT&6&lE&3] protection:12 unbreaking:4",
                        "givecustomitem {name} diamondleggings 1 name:&3[&6&lP&3&lI&6&lR&3&lA&6&lT&3&lE&6&l_&3&lL&6&lE&3&lG&6&lG&3&lI&6&lN&3&lG&6&lS&3] protection:12 unbreaking:4",
                        "givecustomitem {name} diamondboots 1 name:&3[&6&lP&3&lI&6&lR&3&lA&6&lT&3&lE&6&l_&3&lB&6&lO&3&lO&6&lT&3&lS&6] protection:12 unbreaking:4",
                        "givecustomitem {name} diamondsword 1 name:&3&l[&6&lP&3&lI&6&lR&3&lA&6&lT&3&lE&6&l_&3&lS&6&lW&3&lO&6&lR&3&lD&6&l] sharpness:9 unbreaking:4",
                        "give {name} golden_apple 24",
                        "give {name} enchanted_golden_apple 1"
                ]
        ],
        admiral     : [
                name    : "&c&lA&3&ld&c&lm&3&li&c&lr&3&la&c&ll",
                icon    : Material.CHEST,
                cooldown: "24h",
                commands: [
                        "armornote admiral {name}",
                        "giverandompouch {name} small xl"
                ],
                armor   : [
                        "givecustomitem {name} diamondhelmet 1 name:&c&l[&3&lA&c&lD&3&lM&c&lI&3&lR&c&lA&3&lL&c&l_&3&lH&c&lE&3&lL&c&lM&3&lE&c&lT&3&l] protection:14 unbreaking:5",
                        "givecustomitem {name} diamondchestplate 1 name:&c&l[&3&lA&c&lD&3&lM&c&lI&3&lR&c&lA&3&lL&c&l_&3&lC&c&lH&3&lE&c&lS&3&lT&c&lP&3&lL&c&lA&3&lT&c&lE&3&l] protection:14 unbreaking:5",
                        "givecustomitem {name} diamondleggings 1 name:&c&l[&3&lA&c&lD&3&lM&c&lI&3&lR&c&lA&3&lL&c&l_&3&lL&c&lE&3&lG&c&lG&3&lI&c&lN&3&lG&c&lS&3] protection:14 unbreaking:5",
                        "givecustomitem {name} diamondboots 1 name:&c&l[&3&lA&c&lD&3&lM&c&lI&3&lR&c&lA&3&lL&c&l_&3&lB&c&lO&3&lO&c&lT&3&lS&c&l] protection:14 unbreaking:5",
                        "givecustomitem {name} diamondsword 1 name:&c&l[&3&lA&c&lD&3&lM&c&lI&3&lR&c&lA&3&lL&c&l_&3&lS&c&lW&3&lO&c&lR&3&lD&c&l] sharpness:12 unbreaking:5",
                        "give {name} golden_apple 32",
                        "give {name} enchanted_golden_apple 1"
                ]
        ],
        medusa      : [
                name    : "&a&lM&7&le&a&ld&7&lu&a&ls&7&la",
                icon    : Material.CHEST,
                cooldown: "24h",
                commands: [
                        "armornote medusa {name}",
                        "giverandompouch {name} small xl"
                ],
                armor   : [
                        "givecustomitem {name} diamondhelmet 1 name:&7&l[&a&lM&7&lE&a&lD&7&lU&a&lS&7&lA_&a&lH&7&lE&a&lL&7&lM&a&lE&7&lT&a&l] protection:15 unbreaking:5",
                        "givecustomitem {name} diamondchestplate 1 name:&7&l[&a&lM&7&lE&a&lD&7&lU&a&lS&7&lA_&a&lC&7&lH&a&lE&7&lS&a&lT&7&lP&a&lL&7&lA&a&lT&7&lE&a&l] protection:15 unbreaking:5",
                        "givecustomitem {name} diamondleggings 1 name:&7&l[&a&lM&7&lE&a&lD&7&lU&a&lS&7&lA_&a&lL&7&lE&a&lG&7&lG&a&lI&7&lN&a&lG&7&lS&a] protection:15 unbreaking:5",
                        "givecustomitem {name} diamondboots 1 name:&7&l[&a&lM&7&lE&a&lD&7&lU&a&lS&7&lA_&a&lB&7&lO&a&lO&7&lT&a&lS&7&l] protection:15 unbreaking:5",
                        "givecustomitem {name} diamondsword 1 name:&7&l[&a&lM&7&lE&a&lD&7&lU&a&lS&7&lA_&a&lS&7&lW&a&lO&7&lR&a&lD&7&l] sharpness:15 unbreaking:5",
                        "give {name} golden_apple 32",
                        "give {name} enchanted_golden_apple 1"
                ]
        ],
        poseidon1   : [
                name    : "&b&lP&9&lo&3&ls&b&le&9&li&3&ld&b&lo&9&ln",
                icon    : Material.CHEST,
                cooldown: "48h",
                commands: [
                        "armornote poseidon1 {name}",
                        "giverandompouch {name} medium large 2"
                ],
                armor   : [
                        "givecustomitem {name} diamondhelmet 1 name:&d[&b&lP&9&lo&3&ls&b&le&9&li&3&ld&b&lo&9&ln_&b&lHelmet&d] protection:20 unbreaking:5",
                        "givecustomitem {name} diamondchestplate 1 name:&d[&b&lP&9&lo&3&ls&b&le&9&li&3&ld&b&lo&9&ln_&b&lChestplate&d] protection:20 unbreaking:5",
                        "givecustomitem {name} diamondleggings 1 name:&d[&b&lP&9&lo&3&ls&b&le&9&li&3&ld&b&lo&9&ln_&b&lLeggings&d] protection:20 unbreaking:5",
                        "givecustomitem {name} diamondboots 1 name:&d[&b&lP&9&lo&3&ls&b&le&9&li&3&ld&b&lo&9&ln_&b&lBoots&d] protection:20 unbreaking:5",
                        "givecustomitem {name} diamondsword 1 name:&d[&b&lP&9&lo&3&ls&b&le&9&li&3&ld&b&lo&9&ln_&b&lSword&d] sharpness:20 unbreaking:5",
                        "give {name} golden_apple 64",
                        "give {name} enchanted_golden_apple 3"
                ]
        ],
        poseidonpick: [
                name    : "&b&lP&9&lo&3&ls&b&le&9&li&3&ld&b&lo&9&ln &3&lP&b&li&9&lc&3&lk",
                icon    : Material.CHEST,
                cooldown: "7d",
                commands: [
                        "giveposeidonpick {name}"
                ]
        ],
        halloween   : [
                name    : "&6&lH&8&la&7&ll&f&ll&6&lo&7&lw&8&le&6&le&7&ln",
                gkit    : false,
                icon    : Material.JACK_O_LANTERN,
                cooldown: "7d",
                commands: [
                        "givecustomarmour {name} halloween",
                        "givetitle {name} &6&lS&8&lp&7&lo&f&lo&6&lk&8&ly",
                        "givenicknamecolor {name} bold",
                        "givenicknamecolor {name} halloween",
                ]
        ],
        christmas   : [
                name    : "&4&lC&2&lh&f&lr&4&li&2&ls&f&lt&4&lm&2&la&f&ls",
                gkit    : false,
                icon    : Material.COOKIE,
                cooldown: "7d",
                commands: [
                        "givecustomarmour {name} christmas2",
                        "givetitle {name} &4&lC&2&lh&f&lr&4&li&2&ls&f&lt&4&lm&2&la&f&ls",
                        "givenicknamecolor {name} christmas",
                        "givenicknamecolor {name} bold",
                ]
        ],
        newyears    : [
                name    : "&6&k&l!&r&bNewYears&f2021&6&k&l!",
                gkit    : false,
                icon    : Material.FIREWORK_ROCKET,
                cooldown: "7d",
                commands: [
                        "givecustomarmour {name} new_years",
                        "givetitle {name} &6&k&l!&r&bNewYears&f2021&6&k&l!",
                        "givenicknamecolor {name} new_years",
                        "givenicknamecolor {name} bold",
                        "giveparticle {name} firework",
                ]
        ],
        valentines  : [
                name    : "&6&k&l!&r&bValentines&f2021&6&k&l!",
                gkit    : false,
                icon    : Material.RED_TULIP,
                cooldown: "7d",
                commands: [
                        "givecustomarmour {name} valentines",
                        "givetitle {name} &4&lV&f&la&d&ll&4&le&f&ln&d&lt&4&li&f&ln&d&le",
                        "givenicknamecolor {name} valentines",
                        "givenicknamecolor {name} bold",
                        "givenicknamecolor {name} red",
                        "givenicknamecolor {name} light_purple",
                        "giveparticle {name} heart",
                ]
        ],
        easter      : [
                name    : "&6&k&l!&r&bEaster&f2022&6&k&l!",
                gkit    : false,
                icon    : Material.EGG,
                cooldown: "7d",
                commands: [
                        "givecustomarmour {name} easter",
                        "givetitle {name} &b&lE&e&la&a&ls&d&lt&b&le&e&lr",
                        "givenicknamecolor {name} easter",
                        "givenicknamecolor {name} bold",
                        "giveparticle {name} yellow_dust",
                        "giveparticle {name} egg_shells",
                ]
        ],
        independance: [
                name    : "&8[&4&lINDE&f&lPEND&1&lENCE&8]",
                gkit    : false,
                icon    : Material.FIREWORK_ROCKET,
                cooldown: "7d",
                commands: [
                        "givecustomarmour {name} independance",
                        "givetitle {name} &8[&4&lINDE&f&lPEND&9&lENCE&8]",
                        "givetitle {name} &8[&4&l4th&f&lof&9&lJuly &4&l2021&8]",
                        "givenicknamecolor {name} 4TH_OF_JULY",
                        "givenicknamecolor {name} bold",
                        "giveparticle {name} fourth_of_july",
                ]
        ],
        pvp         : [
                name    : "&d&lPvP",
                icon    : Material.DIAMOND_SWORD,
                cooldown: "69d",
                commands: [
                        "givecustomitem {name} diamondhelmet 1 name:&d[PvP_Helmet] protection:3 unbreaking:2",
                        "givecustomitem {name} diamondchestplate 1 name:&dPvP_Chestplate] protection:3 unbreaking:2",
                        "givecustomitem {name} diamondleggings 1 name:&d[PvP_Leggings] protection:3 unbreaking:2",
                        "givecustomitem {name} diamondboots 1 name:&d[PvP_Boots] protection:3 unbreaking:2",
                        "givecustomitem {name} diamondsword 1 name:&d[PvP_Sword] sharpness:3 unbreaking:2, fire_aspect:1"
                ]
        ],
        poseidon    : [
                name    : "&b&lP&9&lo&3&ls&b&le&9&li&3&ld&b&lo&9&ln",
                gkit    : true,
                icon    : Material.ENDER_CHEST,
                cooldown: "1d",
                commands: [
                        "gkitrandomkey {name}",
                        "giverandompouch {name} small large",
                        "giveboosters {name} random 2 600"
                ]
        ],
        lagoon_gkit : [
                name    : "&9&lLagoon",
                gkit    : true,
                icon    : Material.ENDER_CHEST,
                cooldown: "1d",
                commands: [
                        "openinstacrate {name} 5_dollar_gkit",
                        "giverandompouch {name} small large",
                        "giveboosters {name} random 2 600"
                ]
        ],
        misty_gkit  : [
                name    : "&9&lMisty",
                gkit    : true,
                icon    : Material.ENDER_CHEST,
                cooldown: "1d",
                commands: [
                        "openinstacrate {name} 10_dollar_gkit",
                        "giverandompouch {name} small large",
                        "giveboosters {name} random 2 600"
                ]
        ],
        storm_gkit  : [
                name    : "&9&lStorm",
                gkit    : true,
                icon    : Material.ENDER_CHEST,
                cooldown: "1d",
                commands: [
                        "openinstacrate {name} 10_dollar_gkit",
                        "giverandompouch {name} small large",
                        "giveboosters {name} random 2 600"
                ]
        ],
        typhoon_gkit: [
                name    : "&9&lTyphoon",
                gkit    : true,
                icon    : Material.ENDER_CHEST,
                cooldown: "1d",
                commands: [
                        "openinstacrate {name} 20_dollar_gkit",
                        "giverandompouch {name} small large",
                        "giveboosters {name} random 2 600"
                ]
        ],
        advent      : [
                name    : "&9&lAdvent",
                gkit    : true,
                icon    : Material.ENDER_CHEST,
                cooldown: "7d",
                commands: [
                        "givecrate {name} trident 1",
                        "giverandompouch {name} small massive",
                        "givebooster {name} random 2 600"
                ]
        ],
        supporter      : [
                name    : "§b§lSupporter",
                gkit    : true,
                icon    : Material.EMERALD_BLOCK,
                cooldown: "14d",
                commands: [
                        "givelootbox {name} halloween 1",
                        "givecrate {name} dolphin 5",
                        "givecrate {name} whale 5",
                        "giverobot {name} netherite",
                        "givedrill {name} obsidian 1",
                        "givepowerup {name} 2 block 50000",
                        "givepowerup {name} 2 proc 50000",
                        "giverandompouch {name} xl xl",
                        "giverandompouch {name} xl xl",
                        "giverandompouch {name} xl xl",
                        "giverandompouch {name} xl xl",
                        "giverandompouch {name} xl xl",
                ]
        ],
        supersupporter      : [
                name    : "§b§lSuper Supporter",
                gkit    : true,
                icon    : Material.OBSIDIAN,
                cooldown: "14d",
                commands: [
                        "givelootbox {name} halloween 3",
                        "givecrate {name} monthly 1",
                        "givecrate {name} trident 5",
                        "giverobot {name} netherite",
                        "giverobot {name} netherite",
                        "givedrill {name} bedrock 1",
                        "givepowerup {name} 3 block 50000",
                        "givepowerup {name} 3 proc 50000",
                        "giverandompouch {name} massive massive",
                        "giverandompouch {name} massive massive",
                        "giverandompouch {name} massive massive",
                        "givephantompickgem {name} 10 5",
                        "givewandgem {name} 5 5"
                ]
        ],
        godlysupporter      : [
                name    : "§b§lGodly Supporter",
                gkit    : true,
                icon    : Material.BEDROCK,
                cooldown: "14d",
                commands: [
                        "givelootbox {name} halloween 5",
                        "givecrate {name} monthly 3",
                        "givecrate {name} skin 5",
                        "giverobot {name} netherite",
                        "giverobot {name} netherite",
                        "giverobot {name} netherite",
                        "givedrill {name} netherite 1",
                        "givepowerup {name} 4 block 50000",
                        "givepowerup {name} 4 proc 50000",
                        "giverandompouch {name} massive massive",
                        "giverandompouch {name} massive massive",
                        "giverandompouch {name} massive massive",
                        "giverandompouch {name} massive massive",
                        "giverandompouch {name} massive massive",
                        "givephantompickgem {name} 20 5",
                        "givewandgem {name} 10 5",
                        "givecrate {name} monthly 2",
                        "givedrill {name} netherite 2",
                        "giverobot {name} netherite",
                        "giverobot {name} netherite",
                ]
        ],
        premium     : [
                name    : "&a&lPremium",
                gkit    : true,
                icon    : Material.EMERALD,
                cooldown: "1d",
                commands: [
                        "gkitrandomkey {name}",
                        "giveboosters {name} random 2 9000",
                        "giveboosters {name} random 2 9000"
                ]
        ]
]

Exports.ptr("kits", kits)


Starlight.watch("scripts/shared/legacy/kits.groovy")