package scripts.exec.nova1

import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import org.starcade.starlight.enviorment.Exports

Exports.ptr("genbucketconfig", [
        direction : [
                vertical : [
                        lava    :[
                                name : "Vertical Lava Gen",
                                cost : 10000,
                        ],
                        obsidian :[
                                name : "Vertical Obsidian Gen",
                                cost : 1000,
                        ],
                        cobblestone :[
                                name : "Vertical Cobblestone Gen",
                                cost : 100,
                        ],
                        netherrack :[
                                name : "Vertical Netherrack Gen",
                                cost : 100,
                        ],
                        sand :[
                                name : "Vertical Sand Gen",
                                cost : 400,
                        ],
                ],
                horizontal : [
                        obsidian :[
                                name : "Horizontal Obsidian Gen",
                                cost : 1000,
                        ],
                        cobblestone :[
                                name : "Horizontal Cobblestone Gen",
                                cost : 100,
                        ],
                        netherrack :[
                                name : "Horizontal Netherrack Gen",
                                cost : 100,
                        ],
                        sand :[
                                name : "Horizontal Sand Gen",
                                cost : 400,
                        ]
                ]
        ],
        lore : [
                "§c* Click this item to generate a wall.",
        ],
        blockfacetovector : [
                north : new Vector(0, 0, -1),
                east : new Vector(1, 0, 0),
                south: new Vector(0, 0, 1),
                west : new Vector(-1, 0, 0),
        ],
        spongeradius : 3,
        checkforsponge : true,
        nearbyenemyradius : 50,
        denyGenbucketPlacementInWild : true,
        nearbyEnemyGenbucketMessage : "§cYou may not place genbuckets because there is an enemy player nearby.",
        mustHaveBlockUnderMessage : "§7There must be a block under to use this genbucket.",
        horizontallimit : 16 * 4, //4 Chunks
])

