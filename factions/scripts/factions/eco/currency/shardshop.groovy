package scripts.factions.eco.currency

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import scripts.exec.Globals
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.PermissionUtils
import scripts.shared.legacy.utils.StringUtils
import scripts.shared.legacy.wrappers.Console
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MenuDecorator
import scripts.shared.utils.Temple
import org.starcade.starlight.helper.utils.Players

import java.util.concurrent.ThreadLocalRandom

static BigDecimal getShopReduction(Player player, BigDecimal price) {
    BigDecimal num = PermissionUtils.getMaxNum("slash.shardshop.reduction.", player)
    if (ThreadLocalRandom.current().nextInt(1, 101) <= 1) {
        price *= (1 - num / 100)
    }
    if (price < BigDecimal.ZERO) {
        return BigDecimal.ZERO
    }
    return price
}

static void showShardSection(Player player, String section, int page) {
    List<Map<String, ?>> entries = ShardShopUtils.SHOP.get(section)

    if (entries == null) {
        return
    }
    CurrencyStorage shards = Exports.ptr("shards") as CurrencyStorage

    MenuBuilder builder

    builder = MenuUtils.createPagedMenu("§8Section: ${StringUtils.capitalize(section)}", entries, {Map<String, ?> entry, Integer slot ->
        String displayName = entry.get("display name")
        Material material = entry.get("display item") as Material
        String command = entry.get("command")
        int price = entry.get("price") as int

        ItemStack item = FastItemUtils.createItem(material, "§8[§e${displayName}§8]", ["", "§7Price: §e${shards.map(price)}" as String])
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_price"), ItemTagType.INTEGER, price)
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_command"), ItemTagType.STRING, command)
        return item
    }, page, true, [
            {p, t, s ->
                ItemStack item = builder.get().getItem(s)

                int price = FastItemUtils.getCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_price"), ItemTagType.INTEGER)

                CurrencyStorage storage = Exports.ptr("shards") as CurrencyStorage
                price = getShopReduction(player, new BigDecimal(price)).toInteger()
                storage.take(player, price, {
                    String command = FastItemUtils.getCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_command"), ItemTagType.STRING)
                    Console.dispatchCommand(command.replace("%player%", player.getName()))
                })
            },
            {p, t, s -> showShardSection(player, section, page + 1)
            },
            {p, t, s -> showShardSection(player, section, page - 1)
            },
            {p, t, s -> showShardShopMenu(player)
            }
    ])
    builder.open(player)
}

static void showShardShopRobots(Player player) {
    MenuBuilder builder = new MenuBuilder(3 * 9, "§8Shard Shop")

    //0  1  2  3  4  5  6  7  8
    //9  10 11 12 13 14 15 16 17
    //18 19 20 21 22 23 24 25 26
    //27 28 29 30 31 32 33 34 35
    //36 37 38 39 40 41 42 43 44
    MenuDecorator.decorate(builder, [
            "000000000",
            "00=====00",
            "00000000=",
    ])

    List<Map<String, ?>> entries = ShardShopUtils.SHOP.get("robots")
    CurrencyStorage shards = Exports.ptr("shards") as CurrencyStorage

    def slots = [11, 12, 13, 14, 15]
    for (def entry : entries) {
        String displayName = entry.get("display name")
        Material material = entry.get("display item") as Material
        String command = entry.get("command")
        int price = entry.get("price") as int

        ItemStack item = FastItemUtils.createItem(material, "§8[§e${displayName}§8]", ["", "§7Price: §e${shards.map(price)}" as String])
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_price"), ItemTagType.INTEGER, price)
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_command"), ItemTagType.STRING, command)
        builder.set(slots[entries.indexOf(entry)], item, {p, t, s ->
            ItemStack item1 = builder.get().getItem(s)

            int price1 = FastItemUtils.getCustomTag(item1, new NamespacedKey(Starlight.plugin, "buff_price"), ItemTagType.INTEGER)
            price1 = getShopReduction(player, new BigDecimal(price1)).toInteger()
            shards.take(player, price1, {
                String command1 = FastItemUtils.getCustomTag(item1, new NamespacedKey(Starlight.plugin, "buff_command"), ItemTagType.STRING)
                Console.dispatchCommand(command1.replace("%player%", player.getName()))
            })
        })
    }

    builder.set(26, FastItemUtils.createItem(Material.PAPER, "§7§lBack", []), {p, t, s ->
        showShardShopMenu(player)
    })
    builder.open(player)
}

static void showShardShopPotions(Player player) {
    MenuBuilder builder = new MenuBuilder(5 * 9, "§8Shard Shop")

    //0  1  2  3  4  5  6  7  8
    //9  10 11 12 13 14 15 16 17
    //18 19 20 21 22 23 24 25 26
    //27 28 29 30 31 32 33 34 35
    //36 37 38 39 40 41 42 43 44
    //45 46 47 48 49 50 51 52 53
    MenuDecorator.decorate(builder, [
            "000000000",
            "00=====00",
            "000000000",
            "00=====00",
            "00000000=",
    ])

    List<Map<String, ?>> entries = ShardShopUtils.SHOP.get("potions")
    CurrencyStorage shards = Exports.ptr("shards") as CurrencyStorage

    def slots = [11, 12, 13, 14, 15, 29, 30, 31, 32, 33]
    for (def entry : entries) {
        String displayName = entry.get("display name")
        Material material = entry.get("display item") as Material
        String command = entry.get("command")
        int price = entry.get("price") as int

        ItemStack item = FastItemUtils.createItem(material, "§8[§e${displayName}§8]", ["", "§7Price: §e${shards.map(price)}" as String])
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_price"), ItemTagType.INTEGER, price)
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_command"), ItemTagType.STRING, command)
        builder.set(slots[entries.indexOf(entry)], item, {p, t, s ->
            ItemStack item1 = builder.get().getItem(s)

            int price1 = FastItemUtils.getCustomTag(item1, new NamespacedKey(Starlight.plugin, "buff_price"), ItemTagType.INTEGER)
            price1 = getShopReduction(player, new BigDecimal(price1)).toInteger()
            shards.take(player, price1, {
                String command1 = FastItemUtils.getCustomTag(item1, new NamespacedKey(Starlight.plugin, "buff_command"), ItemTagType.STRING)
                Console.dispatchCommand(command1.replace("%player%", player.getName()))
            })
        })
    }
    builder.set(44, FastItemUtils.createItem(Material.PAPER, "§7§lBack", []), {p, t, s ->
        showShardShopMenu(player)
    })
    builder.open(player)
}

static void showShardShopOreGenerator(Player player) {
    MenuBuilder builder = new MenuBuilder(4 * 9, "§8Shard Shop")

    //0  1  2  3  4  5  6  7  8
    //9  10 11 12 13 14 15 16 17
    //18 19 20 21 22 23 24 25 26
    //27 28 29 30 31 32 33 34 35
    //36 37 38 39 40 41 42 43 44
    //45 46 47 48 49 50 51 52 53
    MenuDecorator.decorate(builder, [
            "000000000",
            "0=======0",
            "0000=0000",
            "000000000",
    ])

    List<Map<String, ?>> entries = ShardShopUtils.SHOP.get("ore")
    CurrencyStorage shards = Exports.ptr("shards") as CurrencyStorage

    def slots = [10, 11, 12, 13, 14, 15, 16, 22]
    for (def entry : entries) {
        String displayName = entry.get("display name")
        Material material = entry.get("display item") as Material
        String command = entry.get("command")
        int price = entry.get("price") as int

        ItemStack item = FastItemUtils.createItem(material, "§8[§e${displayName}§8]", ["", "§7Price: §e${shards.map(price)}" as String])
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_price"), ItemTagType.INTEGER, price)
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_command"), ItemTagType.STRING, command)
        builder.set(slots[entries.indexOf(entry)], item, {p, t, s ->
            ItemStack item1 = builder.get().getItem(s)

            int price1 = FastItemUtils.getCustomTag(item1, new NamespacedKey(Starlight.plugin, "buff_price"), ItemTagType.INTEGER)
            price1 = getShopReduction(player, new BigDecimal(price1)).toInteger()
            shards.take(player, price1, {
                String command1 = FastItemUtils.getCustomTag(item1, new NamespacedKey(Starlight.plugin, "buff_command"), ItemTagType.STRING)
                Console.dispatchCommand(command1.replace("%player%", player.getName()))
            })
        })
    }
    builder.set(35, FastItemUtils.createItem(Material.PAPER, "§7§lBack", []), {p, t, s ->
        showShardShopMenu(player)
    })
    builder.open(player)
}

static void showShardShopGenerator(Player player) {
    MenuBuilder builder = new MenuBuilder(3 * 9, "§8Shard Shop")

    //0  1  2  3  4  5  6  7  8
    //9  10 11 12 13 14 15 16 17
    //18 19 20 21 22 23 24 25 26
    //27 28 29 30 31 32 33 34 35
    //36 37 38 39 40 41 42 43 44
    //45 46 47 48 49 50 51 52 53
    MenuDecorator.decorate(builder, [
            "000000000",
            "0===0===0",
            "000000000",
    ])

    List<Map<String, ?>> entries = ShardShopUtils.SHOP.get("generators")
    CurrencyStorage shards = Exports.ptr("shards") as CurrencyStorage

    def slots = [10, 11, 12, 14, 15, 16]
    for (def entry : entries) {
        String displayName = entry.get("display name")
        Material material = entry.get("display item") as Material
        String command = entry.get("command")
        int price = entry.get("price") as int

        ItemStack item = FastItemUtils.createItem(material, "§8[§e${displayName}§8]", ["", "§7Price: §e${shards.map(price)}" as String])
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_price"), ItemTagType.INTEGER, price)
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_command"), ItemTagType.STRING, command)
        builder.set(slots[entries.indexOf(entry)], item, {p, t, s ->
            ItemStack item1 = builder.get().getItem(s)

            int price1 = FastItemUtils.getCustomTag(item1, new NamespacedKey(Starlight.plugin, "buff_price"), ItemTagType.INTEGER)
            price1 = getShopReduction(player, new BigDecimal(price1)).toInteger()
            shards.take(player, price1, {
                String command1 = FastItemUtils.getCustomTag(item1, new NamespacedKey(Starlight.plugin, "buff_command"), ItemTagType.STRING)
                Console.dispatchCommand(command1.replace("%player%", player.getName()))
            })
        })
    }
    builder.set(26, FastItemUtils.createItem(Material.PAPER, "§7§lBack", []), {p, t, s ->
        showShardShopMenu(player)
    })
    builder.open(player)
}

static void showShardShopMisc(Player player) {
    MenuBuilder builder = new MenuBuilder(5 * 9, "§8Shard Shop")

    //0  1  2  3  4  5  6  7  8
    //9  10 11 12 13 14 15 16 17
    //18 19 20 21 22 23 24 25 26
    //27 28 29 30 31 32 33 34 35
    //36 37 38 39 40 41 42 43 44
    //45 46 47 48 49 50 51 52 53
    MenuDecorator.decorate(builder, [
            "000000000",
            "00=000=00",
            "000000000",
            "000===000",
            "000000000",
    ])

    List<Map<String, ?>> entries = ShardShopUtils.SHOP.get("miscellaneous")
    CurrencyStorage shards = Exports.ptr("shards") as CurrencyStorage

    Map<ItemStack, ?> knownEntries = new HashMap<>() // This shit is super weird, work around it.
    def slots = [11, 15, 30, 31, 32]
    for (def entry : entries) {
        if (Globals.isArk || Exports.get("newTitles", false)) {
            if (entries.indexOf(entry) == 0 || entries.indexOf(entry) == 2) {
                builder.set(slots[entries.indexOf(entry)], FastItemUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, "§l", []), {p, t, s ->
                })
                continue
            }
        }
        String displayName = entry.get("display name")
        Material material = entry.get("display item") as Material
        String command = entry.get("command")
        List<String> description = new ArrayList<>()
        for (String line : entry.getOrDefault("description", "").toString().split("<br>")) {
            if (line == "") {
                continue
            }
            description.add(line)
        }
        int price = entry.get("price") as int

        ItemStack item = FastItemUtils.createItem(material, "§8[§e${displayName}§8]", ["", "§7Price: §e${shards.map(price)}" as String])
        if (description.size() > 0) {
            description.add("")
            description.add("§7Price: §e${shards.map(price)}")
            item = FastItemUtils.createItem(material, "§8[§e${displayName}§8]", description)
        }
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_price"), ItemTagType.INTEGER, price)
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "buff_command"), ItemTagType.STRING, command)
        knownEntries.put(item, entry)
        builder.set(slots[entries.indexOf(entry)], item, { p, t, s ->
            ItemStack item1 = builder.get().getItem(s)
            String itemName = knownEntries.get(item1).get("display name") as String
            int limit = knownEntries.get(item1).get("limit") == null ? -1 : (knownEntries.get(item1).get("limit") as int)
            if (limit > 0 && getPlayerLimit(player, itemName) >= limit) {
                Players.msg(player, "§! §> §fYou may not purchase any more of this item.")
                return
            }
            int price1 = FastItemUtils.getCustomTag(item1, new NamespacedKey(Starlight.plugin, "buff_price"), ItemTagType.INTEGER)
            price1 = getShopReduction(player, new BigDecimal(price1)).toInteger()
            shards.take(player, price1, {
                String command1 = FastItemUtils.getCustomTag(item1, new NamespacedKey(Starlight.plugin, "buff_command"), ItemTagType.STRING)
                Console.dispatchCommand(command1.replace("%player%", player.getName()))
                if(limit > 0) {
                    addPlayerLimit(player, itemName, 1)
                }
            })
        })
    }
    builder.set(44, FastItemUtils.createItem(Material.PAPER, "§7§lBack", []), { p, t, s ->
        showShardShopMenu(player)
    })
    builder.open(player)
}

static int getPlayerLimit(Player player, String itemName) {
    itemName = Temple.templeId + "/" + itemName
    def playerStorage = Exports.ptr("playerData/PersistentStorage") as Closure<Map<String, ?>>
    Map<String, ?> map = playerStorage.call(player)
    Map<String, ?> limitMap = map.computeIfAbsent("shardshop/limits", {
        return new LinkedHashMap()
    }) as Map<String, ?>
    int current = limitMap.getOrDefault(itemName, 0) as int
    return current
}

static void addPlayerLimit(Player player, String itemName, int amount = 1) {
    itemName = Temple.templeId + "/" + itemName
    def playerStorage = Exports.ptr("playerData/PersistentStorage") as Closure<Map<String, ?>>
    Map<String, ?> map = playerStorage.call(player)
    Map<String, ?> limitMap = map.computeIfAbsent("shardshop/limits", {
        return new LinkedHashMap()
    }) as Map<String, ?>
    int current = limitMap.getOrDefault(itemName, 0) as int
    limitMap.put(itemName, current + amount)
}

static void showShardShopMenu(Player player) {
    MenuBuilder builder = new MenuBuilder(27, "§8Shard Shop")

    MenuDecorator.decorate(builder, [
            "9be9be9be",
            "9be9be9be",
            "9be9be9be",
    ])

    builder.set(0, FastItemUtils.createItem(Material.PHANTOM_SPAWN_EGG, "§c§lRobots", ["", " §7* Click to open the §cRobots §7menu! ", "§7 * Each §crobot §7costs §bshards§7! "]), {p, t, s -> showShardShopRobots(player)
    })
    builder.set(11, FastItemUtils.createItem(Material.BREWING_STAND, "§6§lPotions", ["", " §7* Click to open the §6Potions §7menu! ", "§7 * Each §6potion §7costs §bshards§7! "]), {p, t, s -> showShardShopPotions(player)
    })
    builder.set(4, FastItemUtils.createItem(Material.END_CRYSTAL, "§e§lGenerators", ["", " §7* Click to open the §eGenerators §7menu! ", "§7 * Each §egenerator §7costs §bshards§7! "]), {p, t, s -> showShardShopGenerator(player)
    })
    builder.set(15, FastItemUtils.createItem(Material.ENDER_EYE, "§a§lMiscellaneous", ["", " §7* Click to open the §aMiscellaneous §7menu! ", "§7 * Each §amiscellaneous item §7costs §bshards§7! "]), {p, t, s -> showShardShopMisc(player)
    })
    builder.set(8, FastItemUtils.createItem(Material.DIAMOND_BLOCK, "§3§lOre Generators", ["", " §7* Click to open the §3§lOre Generators §7menu! ", "§7 * Each §3§lOre Generators  §7costs §bshards§7! "]), {p, t, s -> showShardShopOreGenerator(player)
    })
    builder.open(player)
}

Commands.create().assertPlayer().handler {command ->
    showShardShopMenu(command.sender())
}.register("shardshop", "sshop", "shardsshop")

class ShardShopUtils {
    static final Map<String, List<Map<String, ?>>> SHOP = Exports.get("shard-shop", [
            "robots"       : [
                    [
                            "display name": "Iron Robot",
                            "display item": Material.GHAST_SPAWN_EGG,
                            "command"     : "giverobot %player% iron",
                            "price"       : 8000
                    ], [
                            "display name": "Gold Robot",
                            "display item": Material.BLAZE_SPAWN_EGG,
                            "command"     : "giverobot %player% gold",
                            "price"       : 16000
                    ], [
                            "display name": "Diamond Robot",
                            "display item": Material.ZOMBIE_SPAWN_EGG,
                            "command"     : "giverobot %player% diamond",
                            "price"       : 32000
                    ], [
                            "display name": "Emerald Robot",
                            "display item": Material.SLIME_SPAWN_EGG,
                            "command"     : "giverobot %player% emerald",
                            "price"       : 64000
                    ], [
                            "display name": "Obsidian Robot",
                            "display item": Material.SHULKER_SPAWN_EGG,
                            "command"     : "giverobot %player% obsidian",
                            "price"       : 128000
                    ]
            ],
            "generators"   : [
                    [
                            "display name": "Iron Generator",
                            "display item": Material.IRON_BLOCK,
                            "command"     : "givegenerator %player% iron",
                            "price"       : 1250
                    ], [
                            "display name": "Gold Generator",
                            "display item": Material.GOLD_BLOCK,
                            "command"     : "givegenerator %player% gold",
                            "price"       : 5500
                    ], [
                            "display name": "Diamond Generator",
                            "display item": Material.DIAMOND_BLOCK,
                            "command"     : "givegenerator %player% diamond",
                            "price"       : 12500
                    ], [
                            "display name": "Emerald Generator",
                            "display item": Material.EMERALD_BLOCK,
                            "command"     : "givegenerator %player% emerald",
                            "price"       : 25000
                    ]
            ],
            "ore"   : [
                    [
                            "display name": "Coal Generator",
                            "display item": Material.COAL_ORE,
                            "command"     : "givemininggen %player% coal",
                            "price"       : 8000
                    ],
                    [
                            "display name": "Lapis Generator",
                            "display item": Material.LAPIS_ORE,
                            "command"     : "givemininggen %player% lapis",
                            "price"       : 15000
                    ],
                    [
                            "display name": "Iron Generator",
                            "display item": Material.IRON_ORE,
                            "command"     : "givemininggen %player% iron",
                            "price"       : 29000
                    ],
                    [
                            "display name": "Gold Generator",
                            "display item": Material.GOLD_ORE,
                            "command"     : "givemininggen %player% gold",
                            "price"       : 50000
                    ],
                    [
                            "display name": "Diamond Generator",
                            "display item": Material.DIAMOND_ORE,
                            "command"     : "givemininggen %player% diamond",
                            "price"       : 80000
                    ],
                    [
                            "display name": "Emerald Generator",
                            "display item": Material.EMERALD_ORE,
                            "command"     : "givemininggen %player% emerald",
                            "price"       : 125000
                    ],
                    [
                            "display name": "Netherite Generator",
                            "display item": Material.NETHERITE_BLOCK,
                            "command"     : "givemininggen %player% netherite",
                            "price"       : 200000
                    ],
                    [
                            "display name": "Foundation Blocks",
                            "display item": Material.BEDROCK,
                            "command"     : "givefoundationblock %player% 1",
                            "price"       : 10000
                    ]
            ],
            "potions"      : [
                    [
                            "display name": "Speed I Potion (8 Minutes)",
                            "display item": Material.POTION,
                            "command"     : "addorbox %player% potion{Potion:\"minecraft:long_swiftness\"} 1",
                            "price"       : 25
                    ], [
                            "display name": "Speed II Potion (1.30 Minutes)",
                            "display item": Material.POTION,
                            "command"     : "addorbox %player% potion{Potion:\"minecraft:strong_swiftness\"} 1",
                            "price"       : 35
                    ], [
                            "display name": "Jump Boost I (8 Minutes)",
                            "display item": Material.POTION,
                            "command"     : "addorbox %player% potion{Potion:\"minecraft:long_leaping\"} 1",
                            "price"       : 25
                    ], [
                            "display name": "Splash Potion Of Healing",
                            "display item": Material.SPLASH_POTION,
                            "command"     : "addorbox %player% splash_potion{Potion:\"minecraft:strong_healing\"} 1",
                            "price"       : 25
                    ], [
                            "display name": "Invisibility Potion (3 Minutes)",
                            "display item": Material.POTION,
                            "command"     : "addorbox %player% potion{Potion:\"minecraft:invisibility\"} 1",
                            "price"       : 200
                    ], [
                            "display name": "Splash Potion of Weakness (1 Minute)",
                            "display item": Material.SPLASH_POTION,
                            "command"     : "addorbox %player% splash_potion{Potion:\"minecraft:weakness\"} 1",
                            "price"       : 25
                    ], [
                            "display name": "Splash Potion of Poison (45 Seconds)",
                            "display item": Material.POTION,
                            "command"     : "addorbox %player% splash_potion{Potion:\"minecraft:poison\"} 1",
                            "price"       : 25
                    ], [
                            "display name": "Potion of Fire Resistance (8 Minutes)",
                            "display item": Material.POTION,
                            "command"     : "addorbox %player% potion{Potion:\"minecraft:long_fire_resistance\"} 1",
                            "price"       : 25
                    ], [
                            "display name": "Regeneration I (2 Minutes)",
                            "display item": Material.POTION,
                            "command"     : "addorbox %player% potion{Potion:\"minecraft:long_regeneration\"} 1",
                            "price"       : 25
                    ], [
                            "display name": "Regeneration II (22 Seconds)",
                            "display item": Material.POTION,
                            "command"     : "addorbox %player% potion{Potion:\"minecraft:strong_regeneration\"} 1",
                            "price"       : 35
                    ],
            ],
            "miscellaneous": [
                    [
                            "display name": "ShardGrinder Title",
                            "display item": Material.NAME_TAG,
                            "command"     : "givetitles %player% &cShardGrinder",
                            "price"       : 12000
                    ], [
                            "display name": "NPC SELL SHOP",
                            "display item": Material.GHAST_SPAWN_EGG,
                            "description" : "§cEach player may only place<br>§c1 NPC Shop at all times.",
                            "command"     : "nshop give %player%",
                            "price"       : 64000
                    ], [
                            "display name": "ShardMaster Title",
                            "display item": Material.NAME_TAG,
                            "command"     : "givetitles %player% &4ShardMaster",
                            "price"       : 20000
                    ]
            ]
    ])
}
