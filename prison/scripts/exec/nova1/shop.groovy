package scripts.exec.nova1

import org.bukkit.entity.EntityType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.bukkit.Material
import org.bukkit.potion.PotionType
import scripts.factions.cfg.FactionsShopUtils
import scripts.factions.features.spawners.CustomSpawners


Map<String, Map<String, Object>> shops = [
        potion: [
                icon: Material.POTION,

                title: "§8§lPotion Shop",
                items: [
                    [
                            item: FactionsShopUtils.createPotion(PotionType.INSTANT_HEAL, 2, false),
                            buy: 10,
                            sell: -1
                    ],
                    [
                            item: FactionsShopUtils.createPotion(PotionType.INSTANT_HEAL, 2),
                            buy: 10,
                            sell: -1
                    ],
                    [
                            item: FactionsShopUtils.createPotion(PotionType.REGEN, 2, false),
                            buy: 10,
                            sell: -1
                    ],
                    [
                            item: FactionsShopUtils.createPotion(PotionType.REGEN, 2),
                            buy: 10,
                            sell: -1
                    ],
                    [
                            item: FactionsShopUtils.createPotion(PotionType.STRENGTH, 2, false),
                            buy: 10,
                            sell: -1
                    ],
                    [
                            item: FactionsShopUtils.createPotion(PotionType.STRENGTH, 2),
                            buy: 10,
                            sell: -1
                    ],
                    [
                            item: FactionsShopUtils.createPotion(PotionType.SPEED, 2, false),
                            buy: 10,
                            sell: -1
                    ],
                    [
                            item: FactionsShopUtils.createPotion(PotionType.SPEED, 2),
                            buy: 10,
                            sell: -1
                    ],
                    [
                            item: FactionsShopUtils.createPotion(PotionType.FIRE_RESISTANCE, 1, false),
                            buy: 10,
                            sell: -1
                    ],

                ]
        ],
        raid_shop: [
                icon: Material.REDSTONE,
                title: "§e§lRaid Shop",
                items: [
                        [
                                item: Material.REDSTONE,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.REDSTONE_BLOCK,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.REDSTONE_TORCH,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.COMPARATOR,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.REPEATER,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.OAK_BUTTON,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.STONE_BUTTON,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.TNT,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.DISPENSER,
                                buy: 500,
                                sell: -1
                        ],
                        [
                                item: Material.OBSERVER,
                                buy: 500,
                                sell: -1
                        ],
                        [
                                item: Material.STICKY_PISTON,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.PISTON,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.GLOWSTONE,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.GLASS,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.LADDER,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.COBWEB,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.SPONGE,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.LEVER,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.OAK_TRAPDOOR,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.STONE,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.OAK_PLANKS,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.SAND,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.RED_SAND,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.GRAVEL,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.ICE,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.WATER_BUCKET,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.LAVA_BUCKET,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.STONE_SLAB,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.CREEPER_SPAWN_EGG,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.FLINT_AND_STEEL,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.FISHING_ROD,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.OBSIDIAN,
                                buy: 15,
                                sell: -1
                        ],
                    ]
                ],
        spawner_shop: [
                icon: Material.SPAWNER,
                title: "§e§lSpawners",
                items: [
                        [
                                item: CustomSpawners.createSpawner(EntityType.WITCH),
                                buy: 4000000,
                                sell: -1,
                                type: EntityType.WITCH.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.IRON_GOLEM),
                                buy: 2000000,
                                sell: -1,
                                type: EntityType.IRON_GOLEM.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.SLIME),
                                buy: 1300000,
                                sell: -1,
                                type: EntityType.SLIME.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.BLAZE),
                                buy: 390000,
                                sell: -1,
                                type: EntityType.BLAZE.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.CREEPER),
                                buy: 390000,
                                sell: -1,
                                type: EntityType.CREEPER.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.ENDERMAN),
                                buy: 390000,
                                sell: -1,
                                type: EntityType.ENDERMAN.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.ZOMBIFIED_PIGLIN),
                                buy: 390000,
                                sell: -1,
                                type: EntityType.ZOMBIFIED_PIGLIN.toString()
                        ],

                        [
                                item: CustomSpawners.createSpawner(EntityType.SKELETON),
                                buy: 115000,
                                sell: -1,
                                type: EntityType.SKELETON.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.ZOMBIE),
                                buy: 115000,
                                sell: -1,
                                type: EntityType.ZOMBIE.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.COW),
                                buy: 165000,
                                sell: -1,
                                type: EntityType.COW.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.WOLF),
                                buy: 100000,
                                sell: -1,
                                type: EntityType.WOLF.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.SPIDER),
                                buy: 95000,
                                sell: -1,
                                type: EntityType.SPIDER.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.CAVE_SPIDER),
                                buy: 90000,
                                sell: -1,
                                type: EntityType.CAVE_SPIDER.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.SHEEP),
                                buy: 95000,
                                sell: -1,
                                type: EntityType.SHEEP.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.CHICKEN),
                                buy: 65000,
                                sell: -1,
                                type: EntityType.CHICKEN.toString()
                        ],
                        [
                                item: CustomSpawners.createSpawner(EntityType.PIG),
                                buy: 65000,
                                sell: -1,
                                type: EntityType.PIG.toString()
                        ],
                    ]
                ],
        building_blocks: [
                icon: Material.STONE_BRICKS,
                title: "§8§lBuilding Blocks",
                items: [
                        [
                                item: Material.STONE,
                                buy: 5,
                                sell: -1
                        ],
                        [
                                item: Material.COBBLESTONE,
                                buy: 5,
                                sell: -1
                        ],
                        [
                                item: Material.OBSIDIAN,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.NETHERRACK,
                                buy: 5,
                                sell: -1
                        ],
                        [
                                item: Material.GRANITE,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.POLISHED_GRANITE,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.DIORITE,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.POLISHED_DIORITE,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.ANDESITE,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.POLISHED_ANDESITE,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.GRASS_BLOCK,
                                buy: 15,
                                sell: -1
                        ],
                        [
                                item: Material.DIRT,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.COARSE_DIRT,
                                buy: 20,
                                sell: -1
                        ],
                        [
                                item: Material.PODZOL,
                                buy: 20,
                                sell: -1
                        ],
                        [
                                item: Material.MYCELIUM,
                                buy: 20,
                                sell: -1
                        ],
                        [
                                item: Material.GRAVEL,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.BOOKSHELF,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.BRICKS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.SMOOTH_STONE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.MOSSY_COBBLESTONE,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.STONE_BRICKS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.MOSSY_STONE_BRICKS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.CRACKED_STONE_BRICKS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.CHISELED_STONE_BRICKS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.CLAY,
                                buy: 20,
                                sell: -1
                        ],
                        [
                                item: Material.SNOW_BLOCK,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.OAK_PLANKS,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.SPRUCE_PLANKS,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.BIRCH_PLANKS,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.JUNGLE_PLANKS,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.ACACIA_PLANKS,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.DARK_OAK_PLANKS,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.OAK_LOG,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.SPRUCE_LOG,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.BIRCH_LOG,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.JUNGLE_LOG,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.ACACIA_LOG,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.DARK_OAK_LOG,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.OAK_WOOD,
                                buy: 60,
                                sell: -1
                        ],
                        [
                                item: Material.SPRUCE_WOOD,
                                buy: 60,
                                sell: -1
                        ],
                        [
                                item: Material.BIRCH_WOOD,
                                buy: 60,
                                sell: -1
                        ],
                        [
                                item: Material.JUNGLE_WOOD,
                                buy: 60,
                                sell: -1
                        ],
                        [
                                item: Material.ACACIA_WOOD,
                                buy: 60,
                                sell: -1
                        ],
                        [
                                item: Material.DARK_OAK_WOOD,
                                buy: 60,
                                sell: -1
                        ],
                        [
                                item: Material.SAND,
                                buy: 10,
                                sell: -1
                        ],
                        [
                                item: Material.SANDSTONE,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.CHISELED_SANDSTONE,
                                buy: 40,
                                sell: -1
                        ],
                        [
                                item: Material.CUT_SANDSTONE,
                                buy: 40,
                                sell: -1
                        ],
                        [
                                item: Material.SMOOTH_SANDSTONE,
                                buy: 40,
                                sell: -1
                        ],
                        [
                                item: Material.RED_SAND,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.RED_SANDSTONE,
                                buy: 120,
                                sell: -1
                        ],
                        [
                                item: Material.CHISELED_RED_SANDSTONE,
                                buy: 130,
                                sell: -1
                        ],
                        [
                                item: Material.CUT_RED_SANDSTONE,
                                buy: 140,
                                sell: -1
                        ],
                        [
                                item: Material.SMOOTH_RED_SANDSTONE,
                                buy: 150,
                                sell: -1
                        ],
                        [
                                item: Material.NETHER_BRICKS,
                                buy: 150,
                                sell: -1
                        ],
                        [
                                item: Material.NETHER_BRICK_SLAB,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.GLOWSTONE,
                                buy: 150,
                                sell: -1
                        ]
                ]
        ],
        ores_and_gems: [
                icon: Material.EMERALD,
                title: "§9§lOres and Gems",
                items: [
                        [
                                item: Material.COAL,
                                buy: 4.53,
                                sell: 0.47
                        ],
                        [
                                item: Material.IRON_INGOT,
                                buy: 130,
                                sell: 30
                        ],
                        [
                                item: Material.GOLD_INGOT,
                                buy: 115,
                                sell: 50
                        ],
                        [
                                item: Material.LAPIS_LAZULI,
                                buy: 18.12,
                                sell: 1.25
                        ],
                        [
                                item: Material.EMERALD,
                                buy: 1150,
                                sell: 200
                        ],
                        [
                                item: Material.DIAMOND,
                                buy: 390,
                                sell: 100
                        ],
                        [
                                item: Material.REDSTONE,
                                buy: 16.25,
                                sell: 0.94
                        ],
                        [
                                item: Material.COAL_BLOCK,
                                buy: 40,
                                sell: 4
                        ],
                        [
                                item: Material.IRON_BLOCK,
                                buy: 1170,
                                sell: 270
                        ],
                        [
                                item: Material.GOLD_BLOCK,
                                buy: 1850,
                                sell: 450
                        ],
                        [
                                item: Material.LAPIS_BLOCK,
                                buy: 160,
                                sell: 9
                        ],
                        [
                                item: Material.EMERALD_BLOCK,
                                buy: 8775,
                                sell: 1250
                        ],
                        [
                                item: Material.DIAMOND_BLOCK,
                                buy: 3500,
                                sell: 800
                        ],
                        [
                                item: Material.REDSTONE_BLOCK,
                                buy: 195,
                                sell: 10
                        ],
                        [
                                item: Material.COAL_ORE,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.IRON_ORE,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.GOLD_ORE,
                                buy: 200,
                                sell: -1
                        ],
                        [
                                item: Material.LAPIS_ORE,
                                buy: 250,
                                sell: -1
                        ],
                        [
                                item: Material.EMERALD_ORE,
                                buy: 450,
                                sell: -1
                        ],
                        [
                                item: Material.DIAMOND_ORE,
                                buy: 400,
                                sell: -1
                        ],
                        [
                                item: Material.REDSTONE_ORE,
                                buy: 250,
                                sell: -1
                        ],
                        [
                                item: Material.NETHER_QUARTZ_ORE,
                                buy: 500,
                                sell: -1
                        ],
                ]
        ],
        food: [
                icon: Material.COOKED_BEEF,
                title: "§3§lFood and Farming",
                items: [
                        [
                                item: Material.MELON_SEEDS,
                                buy: 5,
                                sell: -1
                        ],
                        [
                                item: Material.PUMPKIN_SEEDS,
                                buy: 5,
                                sell: -1
                        ],
                        [
                                item: Material.WHEAT_SEEDS,
                                buy: 3,
                                sell: -1
                        ],
                        [
                                item: Material.WHEAT_SEEDS,
                                buy: 3,
                                sell: -1
                        ],
                        [
                                item: Material.CACTUS,
                                buy: 20,
                                sell: 2
                        ],
                        [
                                item: Material.SUGAR_CANE,
                                buy: 16,
                                sell: 3
                        ],
                        [
                                item: Material.MELON_SLICE,
                                buy: 20,
                                sell: 3
                        ],
                        [
                                item: Material.PUMPKIN,
                                buy: 325,
                                sell: 3
                        ],
                        [
                                item: Material.WHEAT,
                                buy: 15,
                                sell: 6
                        ],
                        [
                                item: Material.APPLE,
                                buy: 25,
                                sell: 10
                        ],
                        [
                                item: Material.GOLDEN_APPLE,
                                buy: 1500,
                                sell: -1
                        ],
                        [
                                item: Material.CAKE,
                                buy: 1500,
                                sell: -1
                        ],
                        [
                                item: Material.BREAD,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.COOKIE,
                                buy: 6,
                                sell: -1
                        ],
                        [
                                item: Material.BEEF,
                                buy: 9,
                                sell: 2
                        ],
                        [
                                item: Material.COOKED_BEEF,
                                buy: 12,
                                sell: 3
                        ],
                        [
                                item: Material.PORKCHOP,
                                buy: 10,
                                sell: 1.25
                        ],
                        [
                                item: Material.COOKED_PORKCHOP,
                                buy: 12,
                                sell: 2
                        ],
                        [
                                item: Material.CARROT,
                                buy: 25,
                                sell: 1.5
                        ],
                        [
                                item: Material.PUMPKIN_PIE,
                                buy: 1500,
                                sell: -1
                        ],
                        ],
                ],
        mob_drops : [
        icon: Material.BLAZE_ROD,
        title: "§9§lMob Drops",
        items: [
                [
                        item: Material.GUNPOWDER,
                        buy: 25,
                        sell: 4.38
                ],
                [
                        item: Material.ARROW,
                        buy: 6.25,
                        sell: 2.19
                ],
                [
                        item: Material.FEATHER,
                        buy: 25,
                        sell: 4.38
                ],
                [
                        item: Material.BLAZE_ROD,
                        buy: 62.5,
                        sell: 28.75
                ],
                [
                        item: Material.ROTTEN_FLESH,
                        buy: 2.5,
                        sell: 1.09
                ],
                [
                        item: Material.STRING,
                        buy: 8.12,
                        sell: 1.88
                ],
                [
                        item: Material.LEATHER,
                        buy: 16.25,
                        sell: 5.62
                ],
                [
                        item: Material.ENDER_PEARL,
                        buy: 70,
                        sell: 25
                ],
                [
                        item: Material.BONE,
                        buy: 6.25,
                        sell: 0.5
                ],
                [
                        item: Material.IRON_INGOT,
                        buy: 130,
                        sell: 30
                ],
                [
                        item: Material.EMERALD,
                        buy: 1150,
                        sell: 200
                ],
                [
                        item: Material.DIAMOND,
                        buy: 390,
                        sell: 100
                ],
                [
                        item: Material.GOLD_INGOT,
                        buy: 115,
                        sell: 50
                ],

        ]
        ],
        plants: [
        icon: Material.ROSE_BUSH,
        title: "§2§lPlants and Leaves",
        items: [
                [
                        item: Material.OAK_LEAVES,
                        buy: 10,
                        sell: -1
                ],
                [
                        item: Material.SPRUCE_LEAVES,
                        buy: 10,
                        sell: -1
                ],
                [
                        item: Material.BIRCH_LEAVES,
                        buy: 10,
                        sell: -1
                ],
                [
                        item: Material.JUNGLE_LEAVES,
                        buy: 10,
                        sell: -1
                ],
                [
                        item: Material.ACACIA_LEAVES,
                        buy: 10,
                        sell: -1
                ],
                [
                        item: Material.DARK_OAK_LEAVES,
                        buy: 10,
                        sell: -1
                ],
                [
                        item: Material.OAK_SAPLING,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.SPRUCE_SAPLING,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.BIRCH_SAPLING,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.JUNGLE_SAPLING,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.ACACIA_SAPLING,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.DARK_OAK_SAPLING,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.DEAD_BUSH,
                        buy: 50,
                        sell: -1
                ],
                [
                        item: Material.RED_MUSHROOM_BLOCK,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.BROWN_MUSHROOM_BLOCK,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.FERN,
                        buy : 100,
                        sell: -1
                ],
                [
                        item: Material.COBWEB,
                        buy : 500,
                        sell: -1
                ],
                [
                        "item": Material.STRING,
                        buy   : 400,
                        sell  : -1
                ],
                [
                        item: Material.SNOW,
                        buy : 25,
                        sell: -1
                ],
                [
                        item: Material.ALLIUM,
                        buy : 25,
                        sell: -1
                ],
                [
                        item: Material.BLUE_ORCHID,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.POPPY,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.DANDELION,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.SEA_PICKLE,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.SEAGRASS,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.LILY_OF_THE_VALLEY,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.CORNFLOWER,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.OXEYE_DAISY,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.PINK_TULIP,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.WHITE_TULIP,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.ORANGE_TULIP,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.RED_TULIP,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.AZURE_BLUET,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.BROWN_MUSHROOM,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.RED_MUSHROOM,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.PEONY,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.ROSE_BUSH,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.LILAC,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.LILY_PAD,
                        buy: 25,
                        sell: -1
                ],
                [
                        item: Material.VINE,
                        buy: 30,
                        sell: -1
                ],
                [
                        item: Material.CACTUS,
                        buy: 50,
                        sell: -1
                ],
                [
                        item: Material.SUGAR_CANE,
                        buy: 50,
                        sell: -1
                ],
                [
                        item: Material.MELON,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.PUMPKIN,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.POTATO,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.CARROT,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.WHEAT_SEEDS,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.BEETROOT_SEEDS,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.KELP,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.BAMBOO,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.SADDLE,
                        buy: 50,
                        sell: -1
                ],
                [
                        item: Material.LEATHER,
                        buy: 100,
                        sell: -1
                ],
                [
                        item: Material.GRASS,
                        buy: 50,
                        sell: 1,
                ]
        ]
],

        wool_and_glass: [
                icon: Material.RED_WOOL,
                title: "§e§lWool and Glass",
                items: [
                        [
                                item: Material.WHITE_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.ORANGE_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.MAGENTA_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.LIGHT_BLUE_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.YELLOW_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.LIME_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.PINK_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.GRAY_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.LIGHT_GRAY_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.CYAN_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.PURPLE_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.BLUE_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.BROWN_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.GREEN_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.RED_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.BLACK_WOOL,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.GLASS,
                                buy: 500,
                                sell: -1
                        ],
                        [
                                item: Material.WHITE_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.ORANGE_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.MAGENTA_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.LIGHT_BLUE_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.YELLOW_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.LIME_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.PINK_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.GRAY_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.LIGHT_GRAY_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.CYAN_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.PURPLE_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.BLUE_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.BROWN_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.GREEN_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.RED_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.BLACK_STAINED_GLASS,
                                buy: 50,
                                sell: -1
                        ]
                ]
        ],
        concrete_and_terracotta: [
                icon: Material.CYAN_CONCRETE,
                title: "§5§lConcrete and Terracotta",
                items: [
                        [
                                item: Material.WHITE_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.ORANGE_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.MAGENTA_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.LIGHT_BLUE_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.YELLOW_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.LIME_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.PINK_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.GRAY_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.LIGHT_GRAY_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.CYAN_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.PURPLE_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.BLUE_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.BROWN_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.GREEN_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.RED_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.BLACK_CONCRETE,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.WHITE_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.ORANGE_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.MAGENTA_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.LIGHT_BLUE_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.YELLOW_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.LIME_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.PINK_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.GRAY_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.LIGHT_GRAY_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.CYAN_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.PURPLE_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.BLUE_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.BROWN_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.GREEN_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.RED_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.BLACK_CONCRETE_POWDER,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.WHITE_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.ORANGE_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.MAGENTA_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.LIGHT_BLUE_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.YELLOW_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.LIME_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.PINK_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.GRAY_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.LIGHT_GRAY_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.CYAN_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.PURPLE_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.BLUE_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.BROWN_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.GREEN_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.RED_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ],
                        [
                                item: Material.BLACK_TERRACOTTA,
                                buy: 30,
                                sell: -1
                        ]
                ]
        ],
        ocean_blocks: [
                icon: Material.PRISMARINE,
                title: "§3§lOcean Blocks",
                items: [
                        [
                                item: Material.PRISMARINE,
                                buy: 25,
                                sell:-1
                        ],
                        [
                                item: Material.PRISMARINE_BRICKS,
                                buy: 40,
                                sell: -1
                        ],
                        [
                                item: Material.SEA_LANTERN,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.DARK_PRISMARINE,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.TUBE_CORAL_BLOCK,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.BRAIN_CORAL_BLOCK,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.BUBBLE_CORAL_BLOCK,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.FIRE_CORAL_BLOCK,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.HORN_CORAL_BLOCK,
                                buy: 25,
                                sell: -1
                        ],
                        [
                                item: Material.ICE,
                                buy: 100,
                                sell: -1
                        ],
                        [
                                item: Material.PACKED_ICE,
                                buy: 150,
                                sell: -1
                        ],
                        [
                                item: Material.BLUE_ICE,
                                buy: 300,
                                sell: -1
                        ],
                        [
                                item: Material.DRIED_KELP_BLOCK,
                                buy: 250,
                                sell: -1
                        ],
                        [
                                item: Material.WHITE_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.ORANGE_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.MAGENTA_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.YELLOW_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.LIME_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.PINK_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.GRAY_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.CYAN_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.PURPLE_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.BLUE_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.BROWN_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.GREEN_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.RED_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.BLACK_GLAZED_TERRACOTTA,
                                buy: 50,
                                sell: -1
                        ],
                        [
                                item: Material.SPONGE,
                                buy: 100,
                                sell: -1
                        ]
                ]
        ],
        decorative_blocks: [
                icon: Material.CRAFTING_TABLE,
                title: "§6§lDecorative Blocks",
                items: [
                        [
                                item: Material.HONEYCOMB_BLOCK,
                                buy: 500,
                                sell:-1
                        ],
                        [
                                item: Material.LANTERN,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.LECTERN,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.DAYLIGHT_DETECTOR,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.HONEY_BLOCK,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.NOTE_BLOCK,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.END_ROD,
                                buy: 125,
                                sell:-1
                        ],
                        [
                                item: Material.GRINDSTONE,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.OAK_SIGN,
                                buy: 10,
                                sell:-1
                        ],
                        [
                                item: Material.CAMPFIRE,
                                buy: 500,
                                sell:-1
                        ],
                        [
                                item: Material.SMITHING_TABLE,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.FURNACE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.BLAST_FURNACE,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.SLIME_BLOCK,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.STONECUTTER,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.BARREL,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.SCAFFOLDING,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.BEE_NEST,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.BELL,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.BEEHIVE,
                                buy: 500,
                                sell:-1
                        ],
                        [
                                item: Material.SMOKER,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.CRAFTING_TABLE,
                                buy: 50,
                                sell:-1
                        ],
                        [
                                item: Material.FLETCHING_TABLE,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.CARTOGRAPHY_TABLE,
                                buy: 250,
                                sell:-1
                        ],
                        [
                                item: Material.TORCH,
                                buy: 5,
                                sell:-1
                        ],
                        [
                                item: Material.LOOM,
                                buy : 250,
                                sell: -1
                        ],
                        [
                                item: Material.FLOWER_POT,
                                buy : 150,
                                sell: -1
                        ],
                ]
        ], expensive: [
        icon: Material.NETHER_STAR,
        title: "§d§lSpeciality",
        items: [
                [
                        item: Material.CHEST,
                        buy: 50,
                        sell:-1
                ],
                [
                        item: Material.TRAPPED_CHEST,
                        buy: 150,
                        sell:-1
                ],
                [
                        item: Material.ENCHANTING_TABLE,
                        buy: 2500,
                        sell:-1
                ],
                [
                        item: Material.ANVIL,
                        buy: 3500,
                        sell:-1
                ],
                [
                        item: Material.ENDER_CHEST,
                        buy: 7000,
                        sell:-1
                ],
                [
                        item: Material.BEACON,
                        buy: 250000,
                        sell:-1
                ],
                [
                        item: Material.WATER_BUCKET,
                        buy: 50,
                        sell:-1
                ],
                [
                        item: Material.LAVA_BUCKET,
                        buy: 100,
                        sell:-1
                ],
                [
                        item: Material.JUKEBOX,
                        buy: 2500,
                        sell:-1
                ],
                [
                        item: Material.ICE,
                        buy: 50,
                        sell:-1
                ],
                [
                        item: Material.PACKED_ICE,
                        buy: 75,
                        sell:-1
                ],
                [
                        item: Material.SPONGE,
                        buy: 2000,
                        sell:-1
                ],
                [
                        item: Material.OAK_SIGN,
                        buy: 50,
                        sell:-1
                ],


        ]
],
        dyes: [
                icon: Material.BLUE_DYE,
                title: "§d§lDyes",
                items: [
                        [
                                item: Material.WHITE_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.ORANGE_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.MAGENTA_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.LIGHT_BLUE_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.YELLOW_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.LIME_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.PINK_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.GRAY_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.LIGHT_GRAY_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.CYAN_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.PURPLE_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.BLUE_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.BROWN_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.GREEN_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.RED_DYE,
                                buy: 100,
                                sell:-1
                        ],
                        [
                                item: Material.BLACK_DYE,
                                buy: 100,
                                sell:-1
                        ]
                ]
        ]
]

Exports.ptr("shops", shops)

Starlight.watch("scripts/shared/legacy/shop.groovy")