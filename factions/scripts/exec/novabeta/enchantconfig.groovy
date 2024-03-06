package scripts.exec.novabeta

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.starcade.starlight.enviorment.Exports
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.shared.utils.ItemType

Exports.ptr("enchantconfig", [
        gui        : [
                materialSelector: [
                        title: "§7Select a Material",
                        slots: [
                                (ItemType.HELMET)    : 1,
                                (ItemType.CHESTPLATE): 3,
                                (ItemType.LEGGINGS)  : 5,
                                (ItemType.BOOTS)     : 7,
                                (ItemType.SWORD)     : 11,
                                (ItemType.AXE)       : 13,
                                (ItemType.PICKAXE)   : 15,
                                (ItemType.BOW)       : 21,
                                (ItemType.ROD)       : 23
                        ]
                ],
                raritySelector  : [
                        title: "§7Select a Rarity",
                        slots: [
                                (EnchantmentTier.SIMPLE)   : 1,
                                (EnchantmentTier.UNIQUE)   : 3,
                                (EnchantmentTier.ELITE)    : 5,
                                (EnchantmentTier.ULTIMATE) : 7,
                                (EnchantmentTier.LEGENDARY): 11,
                                (EnchantmentTier.SOUL)     : 13,
                                (EnchantmentTier.HEROIC)   : 15,
                                (EnchantmentTier.GALAXY)   : 22
                        ]
                ],
                enchanterMenu   : [
                        title: "§7Starcade Enchanter",
                        size : 27,
                        items: [
                                [
                                        slot       : 2,
                                        material   : Material.WHITE_STAINED_GLASS_PANE,
                                        displayName: "§f§lSimple Enchantment Book §7(Right Click)",
                                        lore       : [
                                                "§7Examine to receive a random",
                                                "§fSimple §7enchantment book.",
                                                "§r",
                                                "§7Use §f/ce §7to view a list of potential",
                                                "§7enchantments you can unlock.",
                                                "§r",
                                                "§fShift Left-Click §7to buy §f16 Books",
                                        ],
                                        onClick    : { Player player, ClickType type, int slot ->
                                            player.sendMessage("simple")
                                        },
                                        currency   : "EXP",
                                        price      : 1000
                                ],
                                [
                                        slot       : 3,
                                        material   : Material.LIME_STAINED_GLASS_PANE,
                                        displayName: "§a§lUnique Enchantment Book §7(Right Click)",
                                        lore       : [
                                                "§7Examine to receive a random",
                                                "§aUnique §7enchantment book.",
                                                "§r",
                                                "§7Use §a/ce §7to view a list of potential",
                                                "§7enchantments you can unlock.",
                                                "§r",
                                                "§aShift Left-Click §7to buy §a16 Books",
                                        ],
                                        onClick    : { Player player, ClickType type, int slot ->
                                            player.sendMessage("unique")
                                        },
                                        currency   : "EXP",
                                        price      : 5000
                                ],
                                [
                                        slot       : 4,
                                        material   : Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                                        displayName: "§b§lElite Enchantment Book §7(Right Click)",
                                        lore       : [
                                                "§7Examine to receive a random",
                                                "§bUnique §7enchantment book.",
                                                "§r",
                                                "§7Use §b/ce §7to view a list of potential",
                                                "§7enchantments you can unlock.",
                                                "§r",
                                                "§bShift Left-Click §7to buy §b16 Books",
                                        ],
                                        onClick    : { Player player, ClickType type, int slot ->
                                            player.sendMessage("elite")
                                        },
                                        currency   : "EXP",
                                        price      : 10000
                                ],
                                [
                                        slot       : 5,
                                        material   : Material.YELLOW_STAINED_GLASS_PANE,
                                        displayName: "§e§lUltimate Enchantment Book §7(Right Click)",
                                        lore       : [
                                                "§7Examine to receive a random",
                                                "§eUltimate §7enchantment book.",
                                                "§r",
                                                "§7Use §e/ce §7to view a list of potential",
                                                "§7enchantments you can unlock.",
                                                "§r",
                                                "§eShift Left-Click §7to buy §e16 Books",
                                        ],
                                        onClick    : { Player player, ClickType type, int slot ->
                                            player.sendMessage("ultimate")
                                        },
                                        currency   : "EXP",
                                        price      : 25000
                                ],
                                [
                                        slot       : 6,
                                        material   : Material.ORANGE_STAINED_GLASS_PANE,
                                        displayName: "§6§lLegendary Enchantment Book §7(Right Click)",
                                        lore       : [
                                                "§7Examine to receive a random",
                                                "§6Legendary §7enchantment book.",
                                                "§r",
                                                "§7Use §6/ce §7to view a list of potential potential",
                                                "§7enchantments you can unlock.",
                                                "§r",
                                                "§6Shift Left-Click §7to buy §616 Books",
                                        ],
                                        onClick    : { Player player, ClickType type, int slot ->
                                            player.sendMessage("legendary")
                                        },
                                        currency   : "EXP",
                                        price      : 50000
                                ],
                        ]
                ]
        ],
        book       : [
                lore                             : [
                        "§r",
                        "§8┃ §8{description}",
                        "§r",
                        "§a➥ {chance-success}% Success Rate",
                        "§c➥ {chance-destroy}% Destroy Rate",
                        "§r",
                        "§7{applicability}",
                        "§7Drag 'n drop onto an item to enchant"
                ],
                messageEnchantFailed             : "§c§l(!) §cThe enchantment book failed to apply!",
                messageEnchantFailedItemDestroyed: "§c§l(!) §cThe enchantment failed and your {item} has been destroyed!",
                messageEnchantSuccess            : "§a§l(!) §aSuccessfully applied {enchant} §ato your {item}§a!"
        ],
        customItems: [
                whiteScroll           : [
                        material       : Material.PAPER,
                        displayName    : "§eWhite Scroll",
                        lore           : [
                                "§7Prevents an item from being destroyed",
                                "§7due to a failed Enchantment Book.",
                                "§r",
                                "§ePlace scroll onto an item to apply."
                        ],
                        messageApplied : "§a§l(!) §aSuccessfully applied a §eWhite Scroll §ato your {item}§a!",
                        messageFailed  : "§c§l(!) §cYou have already applied a §eWhite Scroll §cto this item!",
                        customModelData: 0
                ],
                holyWhiteScroll       : [
                        material       : Material.PAPER,
                        displayName    : "§6§lHoly White Scroll",
                        lore           : [
                                "§r",
                                "§eA legendary scroll item that",
                                "§ecan be applied to armor/weapons,",
                                "§egives a 100% chance of not losing",
                                "§ethe blessed item on death.",
                                "§r",
                                "§6§nREQ:§e White Scroll"
                        ],
                        messageApplied : "§a§l(!) §aSuccessfully applied a §6Holy White Scroll §ato your {item}§a!",
                        messageMaxUses : "§c§l(!) §cYou have already applied the max §6Holy White Scroll's §cto this item!",
                        maxUses        : 2,
                        customModelData: 0
                ],
                blackScroll           : [
                        material       : Material.PAPER,
                        displayName    : "§f§lBlack Scroll",
                        lore           : [
                                "§7Removes a random enchantment",
                                "§7from an item and converts",
                                "§7it into an Enchantment Book.",
                                "§r",
                                "§7Book Success Rate: §f{percent}%",
                                "§fPlace scroll onto an item to extract."
                        ],
                        // make the message say something about what enchant is given to the player and from what item
                        messageUsed    : "§a§l(!) §aSuccessfully applied a §fBlack Scroll §ato your {item}§a!",
                        customModelData: 0
                ],
                enchantmentDust       : [
                        material      : Material.PAPER,
                        displayName   : "{tierColor}{tierName} Magic Dust",
                        lore          : [
                                "§a+{percent}% Success Rate",
                                "§7Apply to any {tierColor}{tierName} enchantment book",
                                "§7to increase the success rate by §a{percent}%§7.",
                        ],
                        messageApplied: "§a§l(!) §aSuccessfully applied §a{tierColor}{tierName} Magic Dust §ato your {item}§a!",
                        customModelData: 0
                ],
                mysteryEnchantmentDust: [
                        material   : Material.PAPER,
                        displayName: "§6Mystery Enchantment Dust §7(Right Click)",
                        lore       : [""],
                        messageUsed: "§a§l(!) §aYou have received a §6{tierName} dust§a!",
                        customModelData: 0
                ],
                enchantmentOrb        : [
                        material       : Material.PAPER,
                        displayName    : "§6§l{type} Enchantment Orb [§a§n{maxSlots}§6§l]",
                        lore           : [
                                "§a{percent}% Success Rate",
                                "§r",
                                "§6+{increaseSlots}§7 Enchantment Slots",
                                "§6{maxSlots} Max Enchantment Slots",
                                "§r",
                                "§eIncreases the # of enchantment",
                                "§eslots on an item by §6{increaseSlots}§e,",
                                "§eup to a maximum of {maxSlots}.",
                                "§r",
                                "§7Drag n' Drop onto {type} to apply."
                        ],
                        defaultMaxSlots: 8,
                        maxSlots       : 11,
                        messageApplied: "§a§l(!) §aSuccessfully applied a §6{type} Enchantment Orb §ato your {item}§a!",
                        messageFailed: "§c§l(!) §cFailed to apply a §6{type} Enchantment Orb §cto your {item}§c!",
                        customModelData: 0
                ],
                transmogScroll        : [
                        material       : Material.PAPER,
                        displayName    : "§e&lTransmog Scroll",
                        lore           : [
                                "§7Organizes enchants by &e&nrarity &7on item",
                                "§7and adds the §dlore §bcount §7to name.",
                                "§r",
                                "§e§oPlace scroll on item to apply."
                        ],
                        messageApplied : "§a§l(!) §aSuccessfully applied a §eTransmog Scroll §ato your {item}§a!",
                        customModelData: 0
                ]
        ],
        soul       : [
                item            : [
                        material   : Material.EMERALD,
                        displayName: "§c§lSoul Gem [{color}§l{souls}§c§l]",
                        lore       : [
                                "§r",
                                "§c* Click this item to toggle §nSoul Mode",
                                "§7While in \"Soul Mode\" your ACTIVE god tier",
                                "§7enchantments will activate and drain souls",
                                "§7for as long as this mode is enabled.",
                                "§r",
                                "§c* §7Use §c§n/splitsouls§7 with this item",
                                "§7to split souls out of it.",
                                "§r",
                                "§c* §7Stack other §cSoul Gems §7on top of this",
                                "§7one to combine their soul counts."
                        ]
                ],
                tickRemoveAmount: 4,
        ],
        enchants   : [ // cooldown is in seconds
                       // GALAXY
                       // HEROIC
                       // SOUL
                       divineimmolation: [
                               displayName         : "Divine Immolation",
                               description         : [""],
                               applicability       : [ItemType.SWORD],
                               maxLevel            : 4,
                               procChanceCalc      : { int level ->
                                   return 20D
                               },
                               radiusPerLevel      : 1,
                               dmgperlvl           : 1.35D,
                               durationperlvl      : 4000L,
                               proccableSoulEnchant: false,
                               consumption         : 20,
                       ],
                       natureswrath :   [
                               displayName         : "Nature's Wrath",
                               description         : [""],
                               applicability       : [ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS],
                               maxLevel            : 4,
                               procChanceCalc      : { int level ->
                                   return 18D
                               },
                               radiusPerLevel      : 1,
                               proccableSoulEnchant: true,
                               consumption         : 150,
                       ],
//                       soultest        : [
//                               displayName         : "Test Soul",
//                               description         : [""],
//                               applicability       : [ItemType.SWORD],
//                               maxLevel            : 4,
//                               procChanceCalc      : { int level ->
//                                   return 100D
//                               },
//                               proccableSoulEnchant: false,
//                               consumption         : 20,
//                               cooldown            : 10L
//                       ],
                       phoenix         : [
                               displayName         : "Phoenix",
                               description         : [""],
                               applicability       : [ItemType.BOOTS],
                               maxLevel            : 3,
                               procChanceCalc      : { int level ->
                                   return 30D
                               },
                               proccableSoulEnchant: true,
                               consumption         : 500,
                               cooldown            : 120L
                       ],
                       // LEGENDARY
                       silence         : [
                               displayName   : "Silence",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 4,
                               procChanceCalc: { int level ->
                                   return level * 0.03D
                               },
                       ],
                       rage            : [
                               displayName   : "Rage",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 6,
                               dmgincpercombo: 0.1D,
                               maxcombo      : 10,
                       ],
                       gears           : [
                               displayName  : "Gears",
                               description  : [""],
                               applicability: [ItemType.BOOTS],
                               maxLevel     : 3,
                       ],
                       deathbringer    : [
                               displayName   : "Deathbringer",
                               description   : [""],
                               applicability : [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                               maxLevel      : 3,
                               procChanceCalc: { int level ->
                                   return level * 0.03D
                               },
                       ],
                       enlightened     : [
                               displayName   : "Enlightened",
                               description   : [""],
                               applicability : [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                               maxLevel      : 3,
                               procChanceCalc: { int level ->
                                   return 10D/*level * 0.05D*/
                               },
                       ],
                       overload        : [
                               displayName  : "Overload",
                               description  : [""],
                               applicability: [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                               maxLevel     : 3,

                       ],
                       diminish        : [
                               displayName   : "Diminish",
                               description   : [""],
                               applicability : [ItemType.CHESTPLATE],
                               maxLevel      : 5,
                               procChanceCalc: { int level ->
                                   return level * 0.03D
                               },
                               message       : "§e§l* DIMINISH [§eDMG:{damage}§l] *"
                       ],
                       lifesteal       : [
                               displayName   : "Life Steal",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 5,
                               procChanceCalc: { int level ->
                                   return level * 0.05D
                               },
                               weight        : 1,
                       ],
                       antigank        : [
                               displayName  : "Anti Gank",
                               description  : [""],
                               applicability: [ItemType.AXE],
                               maxLevel     : 4,
                       ],
                       aegis           : [
                               displayName  : "Aegis",
                               description  : [""],
                               applicability: [ItemType.AXE],
                               maxLevel     : 6,
                       ],
                       doublestrike    : [
                               displayName   : "Double Strike",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 3,
                               procChanceCalc: { int level ->
                                   return level * 0.03D
                               },
                       ],
                       clarity         : [
                               displayName  : "Clarity",
                               description  : [""],
                               applicability: [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                               maxLevel     : 3,
                       ],
                       destruction     : [
                               displayName   : "Destruction",
                               description   : [""],
                               applicability : [ItemType.HELMET],
                               maxLevel      : 5,
                               radiusPerLevel: 1.5D,
                       ],
                       drunk           : [
                               displayName  : "Drunk",
                               description  : [""],
                               applicability: [ItemType.HELMET],
                               maxLevel     : 4,
                       ],
                       deathgod        : [
                               displayName         : "Death God",
                               description         : [""],
                               applicability       : [ItemType.HELMET],
                               maxLevel            : 3,
                               baseActivationHealth: 4D,
                               baseHealAmt         : 5D,
                               procChanceCalc      : { int level ->
                                   return level * 0.03D
                               },
                       ],
                       inversion       : [
                               displayName   : "Inversion",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 4,
                               procChanceCalc: { int level ->
                                   return level * 0.05D
                               },
                       ],

                       // ULTIMATE
                       dominate        : [
                               displayName   : "Dominate",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 4,
                               procChanceCalc: { int level ->
                                   return level * 0.04D
                               },
                               durationperlvl: 1500L
                       ],
                       iceaspect       : [
                               displayName   : "Ice Aspect",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 3,
                               procChanceCalc: { int level ->
                                   return level * 0.04D
                               },
                       ],
                       armored         : [
                               displayName       : "Armored",
                               description       : [""],
                               applicability     : [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                               maxLevel          : 4,
                               dmgreductionperlvl: 0.0185D,
                       ],
                       obsidianshield  : [
                               displayName  : "Obsidian Shield",
                               description  : [""],
                               applicability: [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                               maxLevel     : 1,
                               stackable: false
                       ],
                       dodge           : [
                               displayName   : "Dodge",
                               description   : [""],
                               applicability : [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                               maxLevel      : 5,
                               procChanceCalc: { int level, boolean isSneaking ->
                                   if (isSneaking) return level * 1.15D * 0.025D
                                   return level * 0.025D
                               },
                       ],
                       angelic         : [
                               displayName   : "Angelic",
                               description   : [""],
                               applicability : [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                               maxLevel      : 5,
                               procChanceCalc: { int level ->
                                   return level * 0.03D
                               },
                               durationTicks : 25
                       ],
                       assassin        : [
                               displayName  : "Assassin",
                               description  : [""],
                               applicability: [ItemType.SWORD],
                               maxLevel     : 5,
                       ],
                       bleed           : [
                               displayName   : "Bleed",
                               description   : [""],
                               applicability : [ItemType.AXE],
                               maxLevel      : 6,
                               procChanceCalc: { int level ->
                                   return 0.4D + level * 0.08D
                               },
                               durationTicks : 20
                       ],
                       enderwalker     : [
                               displayName   : "Ender Walker",
                               description   : [""],
                               applicability : [ItemType.BOOTS],
                               maxLevel      : 5,
                               procChanceCalc: { int level ->
                                   return level * 0.15D
                               },
                       ],
                       enrage          : [
                               displayName  : "Enrage",
                               description  : [""],
                               applicability: [ItemType.SWORD],
                               maxLevel     : 3,
                       ],
                       valor           : [
                               displayName       : "Valor",
                               description       : [""],
                               applicability     : [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                               maxLevel          : 5,
                               dmgReductionPerLvl: 0.0125D
                       ],
                       implants        : [
                               displayName  : "Implants",
                               description  : [""],
                               applicability: [ItemType.HELMET],
                               maxLevel     : 3,
                       ],
                       // ELITE
                       springs         : [
                               displayName  : "Springs",
                               description  : [""],
                               applicability: [ItemType.BOOTS],
                               maxLevel     : 3,
                       ],
                       cactus          : [
                               displayName   : "Cactus",
                               description   : ["Chance to redirect a portion of damage back at your attacker."],
                               applicability : [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                               maxLevel      : 2,
                               procChanceCalc: { int level ->
                                   return level * 0.08D
                               },
                       ],
                       execute         : [
                               displayName               : "Execute",
                               description               : [""],
                               applicability             : [ItemType.SWORD],
                               maxLevel                  : 7,
                               procChanceCalc            : { int level ->
                                   return level * 0.08D
                               },
                               healththreshold           : 8D,
                               additionalmultiplierperlvl: 2D / 7,
                       ],
                       frozen          : [
                               displayName   : "Frozen",
                               description   : [""],
                               applicability : [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                               maxLevel      : 3,
                               procChanceCalc: { int level ->
                                   return level * 0.015D
                               },
                       ],
                       paralyze        : [
                               displayName   : "Paralyze",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 4,
                               procChanceCalc: { int level ->
                                   return level * 0.05D
                               },
                       ],
                       poison          : [
                               displayName   : "Poison",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 3,
                               procChanceCalc: { int level ->
                                   return level * 0.03D
                               },
                       ],
                       poisoned        : [
                               displayName   : "Poisoned",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 4,
                               procChanceCalc: { int level ->
                                   return level * 0.03D
                               },
                       ],
                       rocketescape    : [
                               displayName   : "Rocket Escape",
                               description   : [""],
                               applicability : [ItemType.BOOTS],
                               maxLevel      : 4,
                               procChanceCalc: { int level ->
                                   return level * 0.3D
                               },
                               cooldown      : 30L
                       ],
                       shockwave       : [
                               displayName   : "Shockwave",
                               description   : [""],
                               applicability : [ItemType.CHESTPLATE],
                               maxLevel      : 5,
                               procChanceCalc: { int level ->
                                   return level * 0.02D
                               },
                       ],
                       smokebomb       : [
                               displayName   : "Smoke Bomb",
                               description   : [""],
                               applicability : [ItemType.HELMET],
                               maxLevel      : 3,
                               procChanceCalc: { int level ->
                                   return level * 0.2D
                               },
                               cooldown      : 30L
                       ],
                       blind           : [
                               displayName   : "Blind",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 3,
                               procChanceCalc: { int level ->
                                   return level * 0.03D
                               },
                       ],
                       solitude        : [
                               displayName  : "Solitude",
                               description  : [""],
                               applicability: [ItemType.SWORD],
                               maxLevel     : 3,
                       ],
                       trap            : [
                               displayName   : "Trap",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 3,
                               procChanceCalc: { int level ->
                                   return level * 0.02D
                               },
                       ],
                       // UNIQUE
                       // SIMPLE
                       aquatic         : [
                               displayName  : "Aquatic",
                               description  : [""],
                               applicability: [ItemType.HELMET],
                               maxLevel     : 1,
                       ],
                       haste           : [
                               displayName  : "Haste",
                               description  : [""],
                               applicability: [ItemType.PICKAXE, ItemType.AXE, ItemType.SHOVEL],
                               maxLevel     : 1,
                       ],
                       obliterate      : [
                               displayName                      : "Obliterate",
                               description                      : [""],
                               applicability                    : [ItemType.SWORD, ItemType.HOE],
                               maxLevel                         : 5,
                               procChanceCalc                   : { int level ->
                                   return level * 0.1D
                               },
                               baseknockbackfactor              : 1.8D,
                               additionalknockbackfactorperlevel: 0.5D,
                       ],
                       thunderingblow  : [
                               displayName   : "Thundering Blow",
                               description   : [""],
                               applicability : [ItemType.SWORD],
                               maxLevel      : 3,
                               procChanceCalc: { int level ->
                                   return level * 0.0175D
                               },
                               extradamage   : 5D,
                       ],
                       insomnia        : [
                               displayName             : "Insomnia",
                               description             : [""],
                               applicability           : [ItemType.SWORD],
                               maxLevel                : 7,
                               procChanceCalc          : { int level ->
                                   return level * 0.025D
                               },
                               damagemultiplierperlevel: 0.05D,
                       ],

        ]
])