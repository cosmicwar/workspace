package scripts.exec.novabeta

import org.bukkit.Material
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.text3.Text
import scripts.shared.utils.ColorUtil

// §~ will return a bold gradient for the set's color
// §` will return a non bold gradient for the set's color
Exports.ptr("customsetconfig", [
        sets: [
                celestial: [
                        color: { String string, String format = "" -> return Text.colorize(ColorUtil.rainbow(string, ["#7f98ce", "#0b8cc3"] as String[], format)) },
                        displayName: "Celestial",
                        setItems: [
                                helmet: [
                                        material: Material.NETHERITE_HELMET,
                                        name: "Crown",
                                ],
                                chestplate: [
                                        material: Material.NETHERITE_CHESTPLATE,
                                        name: "Robe",
                                ],
                                leggings: [
                                        material: Material.NETHERITE_LEGGINGS,
                                        name: "Guards",
                                ],
                                boots: [
                                        material: Material.NETHERITE_BOOTS,
                                        name: "Soles",
                                ],

                        ],
                        description: [
                                "",
                                "&lCELESTIAL SET BONUS",
                                "Deal +15% damage to all enemies",
                                "Negate -10% incoming damage",
                                "",
                                "&lCelestial Surge Ability Effect",
                        ],
                        equippedMessage: [
                                "",
                                "CELESTIAL SET EQUIPPED",
                                ""
                        ],
                        unequippedMessage: [
                                "",
                                "CELESTIAL SET REMOVED",
                                ""
                        ],
                        damageModifier: { 1.15D },
                        incomingDamageModifier: { .10D }
                ],
                cosmic: [
                        color: { String string, String format = "" -> return Text.colorize(ColorUtil.rainbow(string, ["#4f2dd5", "#582036"] as String[], format)) },
                        displayName: "Cosmic",
                        setItems: [
                                helmet: [
                                        material: Material.NETHERITE_HELMET,
                                        name: "Helm",
                                ],
                                chestplate: [
                                        material: Material.NETHERITE_CHESTPLATE,
                                        name: "Plate",
                                ],
                                leggings: [
                                        material: Material.NETHERITE_LEGGINGS,
                                        name: "Trousers",
                                ],
                                boots: [
                                        material: Material.NETHERITE_BOOTS,
                                        name: "Boots",
                                ],

                        ],
                        description: [
                                "",
                                "&lCOSMIC SET BONUS",
                                "Negate -25% incoming damage",
                                "",
                                "&lCosmic Resonance Ability Effect",
                        ],
                        equippedMessage: [
                                "",
                                "COSMIC SET EQUIPPED",
                                ""
                        ],
                        unequippedMessage: [
                                "",
                                "COSMIC SET REMOVED",
                                ""
                        ],
                        incomingDamageModifier: { .25D }
                ],
                interstellar: [
                        color: { String string, String format = "" -> return Text.colorize(ColorUtil.rainbow(string, ["#47ba6a", "#131366"] as String[], format)) },
                        displayName: "Interstellar",
                        setItems: [
                                helmet: [
                                        material: Material.NETHERITE_HELMET,
                                        name: "Headgear",
                                ],
                                chestplate: [
                                        material: Material.NETHERITE_CHESTPLATE,
                                        name: "Chestwrap",
                                ],
                                leggings: [
                                        material: Material.NETHERITE_LEGGINGS,
                                        name: "Legwraps",
                                ],
                                boots: [
                                        material: Material.NETHERITE_BOOTS,
                                        name: "Walkers",
                                ],

                        ],
                        description: [
                                "",
                                "&lINTERSTELLAR SET BONUS",
                                "Deal +25% damage to all enemies",
                                "",
                                "&lGalactic Fury Ability Effect",
                        ],
                        equippedMessage: [
                                "",
                                "INTERSTELLAR SET EQUIPPED",
                                ""
                        ],
                        unequippedMessage: [
                                "",
                                "INTERSTELLAR SET REMOVED",
                                ""
                        ],
                        damageModifier: { 1.25D },
                ],
                lunar: [
                        color: { String string, String format = "" -> return Text.colorize(ColorUtil.rainbow(string, ["#d5a210", "#681147"] as String[], format)) },
                        displayName: "Lunar",
                        setItems: [
                                helmet: [
                                        material: Material.NETHERITE_HELMET,
                                        name: "Cap",
                                ],
                                chestplate: [
                                        material: Material.NETHERITE_CHESTPLATE,
                                        name: "Breastplate",
                                ],
                                leggings: [
                                        material: Material.NETHERITE_LEGGINGS,
                                        name: "Legplates",
                                ],
                                boots: [
                                        material: Material.NETHERITE_BOOTS,
                                        name: "Walkers",
                                ],

                        ],
                        description: [
                                "",
                                "&lLUNAR SET BONUS",
                                "Deal +25% damage to all enemies",
                                "",
                                "&lLunar Eclipse Ability Effect",
                        ],
                        equippedMessage: [
                                "",
                                "LUNAR SET EQUIPPED",
                                ""
                        ],
                        unequippedMessage: [
                                "",
                                "LUNAR SET REMOVED",
                                ""
                        ],
                        damageModifier: { 1.25D },
                ],
                traveller: [
                        color: { String string, String format = "" -> return Text.colorize(ColorUtil.rainbow(string, ["#8925a6", "#a5f8b9"] as String[], format)) },
                        displayName: "Traveller",
                        setItems: [
                                helmet: [
                                        material: Material.NETHERITE_HELMET,
                                        name: "Headpiece",
                                ],
                                chestplate: [
                                        material: Material.NETHERITE_CHESTPLATE,
                                        name: "Vest",
                                ],
                                leggings: [
                                        material: Material.NETHERITE_LEGGINGS,
                                        name: "Greaves",
                                ],
                                boots: [
                                        material: Material.NETHERITE_BOOTS,
                                        name: "Striders",
                                ],

                        ],
                        description: [
                                "",
                                "&lTRAVELLER SET BONUS",
                                "Deal +20% damage to all enemies",
                                "Negate -5% incoming damage",
                                "",
                                "&lTraveller's Warp Ability Effect",
                        ],
                        equippedMessage: [
                                "",
                                "TRAVELLER SET EQUIPPED",
                                ""
                        ],
                        unequippedMessage: [
                                "",
                                "TRAVELLER SET REMOVED",
                                ""
                        ],
                        damageModifier: { 1.20D },
                        incomingDamageModifier: { .05D }
                ],
        ]
])