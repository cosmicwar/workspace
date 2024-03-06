package scripts.exec.novabeta

import org.starcade.starlight.enviorment.Exports

import java.util.concurrent.TimeUnit

Exports.ptr("envoyconfig", [
        envoysPerSpawn: 50,
        worldName     : "starcade",
        timer         : TimeUnit.MINUTES.toSeconds(60),
        types       : [
                simple    : [
                        displayName: "§7§lSimple",
                        weight: 1D,
                        min: 1,
                        max: 3,
                ],
                unique    : [
                        displayName: "§a§lUnique",
                        weight: 1D,
                        min: 1,
                        max: 3,
                ],
                epic    : [
                        displayName: "§b§lEpic",
                        weight: 1D,
                        min: 1,
                        max: 3,
                ],
                ultimate    : [
                        displayName: "§e§lUltimate",
                        weight: 1D,
                        min: 1,
                        max: 3,
                ],
                legendary: [
                        displayName: "§6§lLegendary",
                        weight: 1D,
                        min: 1,
                        max: 3,
                ]
        ]
])