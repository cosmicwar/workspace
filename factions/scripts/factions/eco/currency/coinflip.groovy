package scripts.factions.eco.currency

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import scripts.exec.Globals
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.CurrencyUtils
import scripts.shared.legacy.ExpUtils
import scripts.shared.legacy.ToggleUtils
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.objects.MutableBoolean
import scripts.shared.systems.Bedrock
import scripts.shared.utils.Persistent
import scripts.shared.utils.Temple
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.LongUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.legacy.utils.PlayerUtils
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.legacy.utils.SignUtils
import scripts.shared.legacy.utils.StringUtils
import scripts.shared.legacy.utils.ThreadUtils
import scripts.shared.systems.MenuBuilder

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.logging.Logger
import java.util.stream.Collectors

CoinflipUtils.init()

static String getColor(String color) {
    return [
            "white"     : "§f",
            "orange"    : "§6",
            "magenta"   : "§d",
            "light blue": "§b",
            "yellow"    : "§e",
            "lime"      : "§a",
            "pink"      : "§c",
            "gray"      : "§8",
            "light gray": "§7",
            "cyan"      : "§3",
            "purple"    : "§5",
            "blue"      : "§9",
            "green"     : "§2",
            "red"       : "§4",
            "black"     : "§0"
    ].get(color.toLowerCase())
}

static ItemStack getCoinflipItem(List<Object> coinflip, Player viewer, boolean doDelete = false) {
    ItemStack item = null

    UUID owner = coinflip.get(1) as UUID

    DatabaseUtils.getLatestUsername(owner, { username ->
        String currency = coinflip.get(2)
        Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)

        String color = StringUtils.capitalize((coinflip.get(4) as String).replace("_", " ")).replace(" Concrete Powder", "")

        item = FastItemUtils.createItem(data.get("item") as Material, "§e${username}'s Coinflip", [])

        List<String> lore

        if (CoinflipUtils.isCoinflipPrivate(coinflip)) {
            lore = [
                    "",
                    viewer.uniqueId == owner ? "§9Your Opponent: §f${Players.getOfflineNullable(coinflip.get(5) as UUID).name}" : "§9Challenged By: §f${Players.getOffline(owner).get().getName()}",
                    "§9Currency: ${data.get("name")}",
                    "§9Amount: ${mapBalance(coinflip)}",
                    "§9Color: §f${getColor(color) + color}",
                    "",
            ]
        } else {
            lore = [
                    "",
                    "§9Currency: ${data.get("name")}",
                    "§9Amount: ${mapBalance(coinflip)}",
                    "§9Color: §f${getColor(color) + color}",
                    "",
            ]
        }
        boolean isOwner = viewer.getUniqueId() == owner

        if (viewer.hasPermission("coinflip.delete") && doDelete && !isOwner) {
            lore.addAll([
                    "§e * Left click to challenge *",
                    "§c * Right click to delete *"
            ])
        } else if (isOwner) {
            lore.add("§c * Click to delete * ")
        } else {
            lore.add("§e * Click to challenge * ")
        }
        FastItemUtils.setLore(item, lore)
    }, false)

    FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "coinflip_id"), ItemTagType.STRING, (coinflip.get(0) as UUID).toString())

    return item
}

static String mapBalance(List<Object> coinflip) {
    String currency = coinflip.get(2) as String
    Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)
    long amount = coinflip.get(3) as long
    if (data.get("special") != null) {
        return (data.get("special") as Map<String, Closure>).get("map").call(amount) as String
    } else {
        return CurrencyUtils.get(currency).map(amount)
    }

}

static String mapBalance(String currency, long amount) {
    Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)
    if (data.get("special") != null) {
        return (data.get("special") as Map<String, Closure>).get("map").call(amount) as String
    } else {
        return CurrencyUtils.get(currency).map(amount)
    }

}

static void handleCoinflipEvent(Player player, ClickType type, int slot, MenuBuilder builder, Runnable onDelete) {
    UUID id = UUID.fromString(FastItemUtils.getCustomTag(builder.get().getItem(slot), new NamespacedKey(Starlight.plugin, "coinflip_id"), ItemTagType.STRING))
    List<Object> coinflip = CoinflipUtils.getCoinflip(id)

    if (coinflip == null) {
        Players.msg(player, "§6§lCOINFLIP §> §fThis coinflip is no longer available!")
        player.closeInventory()
        return
    }
    UUID owner = coinflip.get(1) as UUID
    boolean isOwner = owner == player.getUniqueId()

    if ((player.hasPermission("coinflip.delete") && !isOwner && type == ClickType.RIGHT) || isOwner) {
        CoinflipUtils.delete(coinflip, false)
        Players.msg(player, "§6§lCOINFLIP §> §fSuccessfully deleted coinflip!")
        onDelete.run()
        return
    }
    String currency = coinflip.get(2) as String
    Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)

    if (data.get("special") != null) {
        Map<String, Closure> specialCurrency = data.get("special") as Map<String, Closure>
        if (!specialCurrency.get("has").call(player, coinflip.get(3) as long)) {
            Players.msg(player, "not enough")
            return
        }
        specialCurrency.get("take").call(player, coinflip.get(3) as long, true)
        CoinflipUtils.delete(coinflip)


        Material ownerColor = Material.valueOf(coinflip.get(4) as String)
        Material challengerColor = (data.get("colors") as List<Material>).stream().filter { color -> color != ownerColor }.collect(Collectors.toList()).get(0)

        DatabaseUtils.getLatestUsername(owner, { String username ->
            ItemStack ownerSkull = FastItemUtils.createSkull((String) username, "§e${username}", [])
            ItemStack challengerSkull = FastItemUtils.createSkull(player, "§e${player.getName()}", [])

            Schedulers.sync().run {
                Inventory inventory = Bukkit.createInventory(null, 27, "§0§8Coinflip")

                for (int i : [1, 2, 19, 20]) {
                    inventory.setItem(i, FastItemUtils.createItem(Material.valueOf(ownerColor.name().replace("_CONCRETE_POWDER", "_STAINED_GLASS_PANE")), "§0", []))
                }
                inventory.setItem(10, ownerSkull)

                for (int i : [6, 7, 8, 24, 25]) {
                    inventory.setItem(i, FastItemUtils.createItem(Material.valueOf(challengerColor.name().replace("_CONCRETE_POWDER", "_STAINED_GLASS_PANE")), "§0", []))
                }
                inventory.setItem(16, challengerSkull)

                for (int i : [3, 5, 11, 12, 14, 15, 21, 23]) {
                    inventory.setItem(i, FastItemUtils.createItem(Material.WHITE_STAINED_GLASS_PANE, "§0", []))
                }
                for (int i : [4, 22, 0, 9, 18, 8, 17, 26]) {
                    inventory.setItem(i, FastItemUtils.createItem(Material.YELLOW_STAINED_GLASS_PANE, "§0", []))
                }
                int iterations = 25

                boolean isOp = false
                if (Bukkit.getPlayer(owner)?.isOp()) isOp = true
                if (RandomUtils.RANDOM.nextBoolean() && !isOp) {
                    --iterations
                }
                showRollingInventory(player, owner, username, coinflip, inventory, ownerColor, challengerColor, iterations)
            }
        })
        return
    }

    CurrencyUtils.get(currency).take(player, coinflip.get(3) as long, {
        CoinflipUtils.delete(coinflip)


        Material ownerColor = Material.valueOf(coinflip.get(4) as String)
        Material challengerColor = (data.get("colors") as List<Material>).stream().filter { color -> color != ownerColor }.collect(Collectors.toList()).get(0)

        DatabaseUtils.getLatestUsername(owner, { String username ->
            ItemStack ownerSkull = FastItemUtils.createSkull((String) username, "§e${username}", [])
            ItemStack challengerSkull = FastItemUtils.createSkull(player, "§e${player.getName()}", [])

            Schedulers.sync().run {
                Inventory inventory = Bukkit.createInventory(null, 27, "§0§8Coinflip")

                for (int i : [1, 2, 19, 20]) {
                    inventory.setItem(i, FastItemUtils.createItem(Material.valueOf(ownerColor.name().replace("_CONCRETE_POWDER", "_STAINED_GLASS_PANE")), "§0", []))
                }
                inventory.setItem(10, ownerSkull)

                for (int i : [6, 7, 8, 24, 25]) {
                    inventory.setItem(i, FastItemUtils.createItem(Material.valueOf(challengerColor.name().replace("_CONCRETE_POWDER", "_STAINED_GLASS_PANE")), "§0", []))
                }
                inventory.setItem(16, challengerSkull)

                for (int i : [3, 5, 11, 12, 14, 15, 21, 23]) {
                    inventory.setItem(i, FastItemUtils.createItem(Material.WHITE_STAINED_GLASS_PANE, "§0", []))
                }
                for (int i : [4, 22, 0, 9, 18, 8, 17, 26]) {
                    inventory.setItem(i, FastItemUtils.createItem(Material.YELLOW_STAINED_GLASS_PANE, "§0", []))
                }
                int iterations = 25

                boolean isOp = false
                if (Bukkit.getPlayer(owner)?.isOp()) isOp = true
                if (RandomUtils.RANDOM.nextBoolean() && !isOp) {
                    --iterations
                }
                showRollingInventory(player, owner, username, coinflip, inventory, ownerColor, challengerColor, iterations)
            }
        })
    })
}

static void updateInventory(Inventory inventory, Material ownerColor, Material challengerColor, String ownerName, String challengerName, int iteration) {
    Material material = iteration % 2 == 0 ? ownerColor : challengerColor
    String color = StringUtils.capitalize(material.name().replace("_", " ")).replace(" Concrete Powder", "")
    inventory.setItem(13, FastItemUtils.createItem(material, "${getColor(color)}${iteration % 2 == 0 ? ownerName : challengerName}", []))
}

static void showRollingInventory(Player player, UUID owner, String ownerName, List<Object> coinflip, Inventory inventory, Material ownerColor, Material challengerColor, int iterations) {
    player.openInventory(inventory)

    Player ownerPlayer = Bukkit.getPlayer(owner)

    if (ownerPlayer != null) {
        ownerPlayer.openInventory(inventory)
    }
    MutableBoolean running = new MutableBoolean(true)

    for (int i = 0; i < iterations; ++i) {
        int iteration = i + 1

        Schedulers.sync().runLater({
            if (!running.booleanValue()) {
                return
            }
            if (iteration == iterations) {
                boolean ownerWon = iterations % 2 == 1
                UUID winner = ownerWon ? owner : player.getUniqueId()
                UUID loser = ownerWon ? player.getUniqueId() : owner

                String currency = coinflip.get(2) as String
                long amount = coinflip.get(3) as long

                CurrencyStorage storage = CurrencyUtils.get(currency)
                String mapped = mapBalance(coinflip)
                String playerName = player.getName()

                if (ownerWon) {
                    PlayerUtils.sendMessage(owner, "§6§lCOINFLIP §> §fYou have defeated §e${playerName} §fin a coinflip match for ${mapped}§f!")
                    Players.msg(player, "§6§lCOINFLIP §> §fYou have §clost §fa coinflip match against §e${ownerName} §ffor ${mapped}§f!")
                    Starlight.log.info("[Coinflip] ${ownerName} won the coinflip for ${amount * 2}")
                    Starlight.log.info("[Coinflip] ${playerName} lost the coinflip for ${amount * 2}")
                } else {
                    Players.msg(player, "§6§lCOINFLIP §> §fYou have defeated §e${ownerName} §fin a coinflip match for ${mapped}§f!")
                    PlayerUtils.sendMessage(owner, "§6§lCOINFLIP §> §fYou have §clost §fa coinflip match against §e${playerName} §ffor ${mapped}§f!")
                    Starlight.log.info("[Coinflip] ${playerName} won the coinflip for ${amount * 2}")
                    Starlight.log.info("[Coinflip] ${ownerName} lost the coinflip for ${amount * 2}")
                }
                long reward = amount * 2

                if (reward > 10) {
                    reward = (reward * 0.9D) as long
                }

                Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)

                if (data.get("special") != null) {
                    Map<String, Closure> specialCurrency = data.get("special") as Map<String, Closure>
                    Player target = Bukkit.getPlayer(winner)
                    if (target) {
                        specialCurrency.get("add").call(target, reward, true)
                    } else CoinflipUtils.addQueue(winner, coinflip)
                } else {
                    storage.add(winner, reward, null, true, false, true)
                }

                MySQL.getAsyncDatabase().executeBatch("INSERT INTO coinflip_stats (uuid_least, uuid_most, wins, losses, server_id) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE wins = wins + VALUES(wins), losses = losses + VALUES(losses)", { statement ->
                    statement.setLong(1, winner.getLeastSignificantBits())
                    statement.setLong(2, winner.getMostSignificantBits())
                    statement.setInt(3, 1)
                    statement.setInt(4, 0)
                    statement.setString(5, Temple.templeId)
                    statement.addBatch()
                    statement.setLong(1, loser.getLeastSignificantBits())
                    statement.setLong(2, loser.getMostSignificantBits())
                    statement.setInt(3, 0)
                    statement.setInt(4, 1)
                    statement.setString(5, Temple.templeId)
                    statement.addBatch()
                })

                if (Globals.CF_VALUES) {
                    MySQL.getAsyncDatabase().executeBatch("INSERT INTO coinflip_stats_value (uuid_least, uuid_most, currency, wins, losses, server_id) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE wins = wins + VALUES(wins), losses = losses + VALUES(losses)", { statement ->
                        //winner
                        statement.setLong(1, winner.getLeastSignificantBits())
                        statement.setLong(2, winner.getMostSignificantBits())
                        statement.setString(3, currency)
                        statement.setLong(4, reward - amount)
                        statement.setLong(5, 0L)
                        statement.setString(6, Temple.templeId)
                        statement.addBatch()
                        //loser
                        statement.setLong(1, loser.getLeastSignificantBits())
                        statement.setLong(2, loser.getMostSignificantBits())
                        statement.setString(3, currency)
                        statement.setLong(4, 0L)
                        statement.setLong(5, amount)
                        statement.setString(6, Temple.templeId)
                        statement.addBatch()
                    })
                }

                if (amount >= (data.get("announce_minimum") as long)) {
                    String message = "\n§6§lCOINFLIP §> §e${ownerWon ? ownerName : playerName} §fhas defeated §e${ownerWon ? playerName : ownerName} §fin a coinflip for ${mapped}§f!\n "
                    Starlight.log.info("[Coinflip] ${ownerWon ? ownerName : playerName} defeated ${ownerWon ? playerName : ownerName} in a coinflip for ${mapped}")

                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (!ToggleUtils.hasToggled(online, "coinflip_announcements")) {
                            Players.msg(online, message)
                        }
                    }
                }
                running.setValue(false)

                Schedulers.sync().runLater({
                    player.closeInventory()
                    Bukkit.getPlayer(owner)?.closeInventory()
                }, 20)
                return
            }
            updateInventory(inventory, ownerColor, challengerColor, ownerName, player.getName(), iteration)
        }, (iteration * (1 + iteration / 10.0D)) as long)
    }
    // put player shit
}

Events.subscribe(PlayerLoginEvent).handler { e ->
    Player player = e.player
    Schedulers.async().runLater({
        MySQL.getAsyncDatabase().executeQuery("SELECT currency, amount FROM coinflip_win_queue WHERE ${DatabaseUtils.getServerUserExpression(player)}", {}, { result ->
            while (result.next()) {
                String currencyKey = result.getString(1)
                long amount = result.getLong(2)
                Map<String, Object> data = CoinflipUtils.COINFLIP.get(currencyKey)
                if (data.get("special") != null) {
                    Map<String, Closure> specialCurrency = data.get("special") as Map<String, Closure>
                    specialCurrency.get("add").call(player, amount, true)
                } else {
                    CurrencyStorage currency = CurrencyUtils.get(currencyKey)
                    currency.add(player, amount, true, true, {})
                }
            }
            CoinflipUtils.deleteQueue(player)
        })
    }, 5L)

}

Commands.create().assertPlayer().assertOp().handler { c ->
    List<Object> coinflip = CoinflipUtils.get(UUID.randomUUID(), c.sender().getUniqueId(), null, null, null)
    coinflip.set(2, "exp")
    coinflip.set(3, 100)
    CoinflipUtils.addQueue(c.sender().uniqueId, coinflip)
    c.reply("§cDone.")
}.register("dev/cf/test2")

Events.subscribe(InventoryClickEvent.class, EventPriority.HIGH).filter(EventFilters.ignoreCancelled()).handler { event ->
    String title = event.getView()?.getTitle()

    if (title != null && title == "§0§8Coinflip") {
        event.setCancelled(true)
    }
}

static void showCoinflips(Player player, UUID target = player.getUniqueId(), int page = 1) {
    List<List<Object>> coinflips = new ArrayList<>()

    for (List<Object> coinflip : CoinflipUtils.COINFLIPS) {
        if (coinflip.get(1) as UUID == target) {
            coinflips.add(coinflip)
        }
    }

    for (List<Object> coinflip : CoinflipUtils.PRIVATE_COINFLIPS) {
        if (coinflip.get(1) as UUID == target) {
            coinflips.add(coinflip)
        }
    }

    Schedulers.async().run {
        CoinflipUtils.sort(coinflips)

        MenuBuilder builder

        builder = MenuUtils.createPagedMenu("§8Your Coinflips", coinflips, { List<Object> coinflip, Integer i ->
            return getCoinflipItem(coinflip, player)
        }, page, target == player.getUniqueId(), [
                { p, t, s -> handleCoinflipEvent(player as Player, t as ClickType, s as Integer, builder, { showCoinflips(player, target) }) },
                { p, t, s -> showCoinflips(player, target, page + 1) },
                { p, t, s -> showCoinflips(player, target, page - 1) },
                { p, t, s -> showCoinflipMenu(player) }
        ])
        MenuUtils.syncOpen(player, builder)
    }
}

static void showPrivateCoinflips(Player player, int page = 1) {
    List<List<Object>> coinflips = new ArrayList<>()

    for (List<Object> coinflip : CoinflipUtils.PRIVATE_COINFLIPS) {
        if (coinflip.get(5) as UUID == player.uniqueId) {
            coinflips.add(coinflip)
        }
    }

    Schedulers.async().run {
        CoinflipUtils.sort(coinflips)

        MenuBuilder builder

        builder = MenuUtils.createPagedMenu("§8Private Coinflips", coinflips, { List<Object> coinflip, Integer i ->
            return getCoinflipItem(coinflip, player)
        }, page, true, [
                { p, t, s -> handleCoinflipEvent(player as Player, t as ClickType, s as Integer, builder, { showPrivateCoinflips(player, page) }) },
                { p, t, s -> showPrivateCoinflips(player, page + 1) },
                { p, t, s -> showPrivateCoinflips(player, page - 1) },
                { p, t, s -> showCoinflipMenu(player) }
        ])
        MenuUtils.syncOpen(player, builder)
    }
}

static void startCoinflip(Player player, List<Object> coinflip, int stage = 0) {
    switch (stage) {
        case 0:
            NamespacedKey currencyKey = new NamespacedKey(Starlight.plugin, "currency")

            MenuBuilder builder

            builder = MenuUtils.createPagedMenu("§8Coinflip", new ArrayList<>(CoinflipUtils.COINFLIP.entrySet()), { Map.Entry<String, LinkedHashMap<String, Object>> entry, Integer i ->
                String currency = entry.getKey()
                Map<String, Object> data = entry.getValue()

                ItemStack item = FastItemUtils.createItem(data.get("item") as Material, data.get("name") as String, [
                        "",
                        "§7Minimum Bet: §f${mapBalance(currency, data.get("minimum_bet") as long)}",
                        "",
                        "§e * Click to select currency * "
                ])
                FastItemUtils.setCustomTag(item, currencyKey, ItemTagType.STRING, currency)

                return item
            }, 1, true, [
                    { p, t, Integer s ->
                        String currency = FastItemUtils.getCustomTag(builder.get().getItem(s), currencyKey, ItemTagType.STRING)
                        coinflip.set(2, currency)
                        int plus = 1
                        startCoinflip(player, coinflip, stage + plus)
                    },
                    { p, t, s -> },
                    { p, t, s -> },
                    { p, t, s -> showCoinflipMenu(player) }
            ])
            builder.open(player)
            break
        case 1:
            String currency = coinflip.get(2) as String
            Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)

            Consumer<String> handleInput = { String input ->
                long amount = getAmount(input)
                if (amount <= 0) {
                    Players.msg(player, "§6§lCOINFLIP §> §e${input} §fis not a valid amount!")
                    return
                }
                long minimum = data.get("minimum_bet") as long

                if (amount < minimum) {
                    Players.msg(player, "§6§lCOINFLIP §> §fThe minimum bet for this currency is ${mapBalance(currency, minimum)}§f!")
                    return
                }

                long maxAmount = data.getOrDefault("max_amount", Long.MAX_VALUE) as long

                if (amount > maxAmount) {
                    Players.msg(player, "§! §> §fThe maximum amount of this currency that can be coinflipped is ${mapBalance(currency, maxAmount)}")
                    return
                }

                if (data.get("special") != null) {
                    Map<String, Closure> specialCurrenc = data.get("special") as Map<String, Closure>
                    if (!specialCurrenc.get("has").call(player, amount)) {
                        Players.msg(player, "§c§lERROR §8» §fYou do not have enough ${currency}!")
                        return
                    }
                    coinflip.set(3, amount)
                    startCoinflip(player, coinflip, stage + 1)
                    return
                }
                CurrencyStorage storage = CurrencyUtils.get(currency)
                storage.get(player, { balance ->
                    if ((balance <=> amount) < 0) {
                        player.closeInventory()
                        storage.notEnough(player)
                        return
                    }
                    coinflip.set(3, amount)
                    startCoinflip(player, coinflip, stage + 1)
                })
            }
            if (Bedrock.isBedrockPlayer(player)) {
                new Bedrock.CustomGui("Enter Amount", { Map<String, Object> response ->
                    handleInput.accept(response.get("coinflip-deposit") as String)
                }).textInput("coinflip-deposit", "Enter Amount", "10000").open(player)

            } else {
                SignUtils.openSign(player, ["", "^ ^ ^", "Enter Amount", data.get("name") as String], { String[] lines, Player p ->
                    handleInput.accept(lines[0])
                })
            }
            break
        case 2:
            String currency = coinflip.get(2) as String
            Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)

            NamespacedKey colorKey = new NamespacedKey(Starlight.plugin, "color")

            MenuBuilder builder

            builder = MenuUtils.createPagedMenu("§8Coinflip", data.get("colors") as List<Material>, { Material material, Integer i ->
                String color = StringUtils.capitalize(material.name().replace("_", " ")).replace(" Concrete Powder", "")
                ItemStack item = FastItemUtils.createItem(material, getColor(color) + color, ["", "§e * Click to select color * "])
                FastItemUtils.setCustomTag(item, colorKey, ItemTagType.STRING, material.name())

                return item
            }, 1, true, [
                    { p, t, Integer s ->
                        String color = FastItemUtils.getCustomTag(builder.get().getItem(s), colorKey, ItemTagType.STRING)
                        if (data.get("special") != null) {
                            Map<String, Closure> specialCurrency = data.get("special") as Map<String, Closure>
                            specialCurrency.get("take").call(player, coinflip.get(3) as long, true)
                            coinflip.set(4, color)
                            startCoinflip(player, coinflip, stage + 1)
                            return
                        }
                        CurrencyUtils.get(currency).take(player, coinflip.get(3) as long, {
                            coinflip.set(4, color)
                            startCoinflip(player, coinflip, stage + 1)
                        })
                    },
                    { p, t, s -> },
                    { p, t, s -> },
                    { p, t, s ->

                        startCoinflip(player, coinflip, stage - 1)
                    }
            ])
            builder.open(player)
            break
        case 3:
            boolean isPrivate = CoinflipUtils.isCoinflipPrivate(coinflip)
            if (isPrivate && ToggleUtils.hasToggled(coinflip.get(5) as UUID, "private_coinflips")) {
                Players.msg(player, "§6§lCOINFLIP §> §cThis player is currently not accepting private coinflips.")
                break
            }
            CoinflipUtils.register(coinflip)

            Players.msg(player, "§6§lCOINFLIP §> §fSuccessfully created a" + (isPrivate ? " §e§nprivate§f " : " §e§npublic§f ") + "coinflip!")
            showCoinflipMenu(player)

            if (CoinflipUtils.isCoinflipPrivate(coinflip)) {
                UUID opponentUUID = coinflip.get(5) as UUID
                Player opponent = Players.getNullable(opponentUUID)

                if (opponent != null && opponent.isOnline()) {
                    long amount = coinflip.get(3) as long
                    CurrencyStorage storage = CurrencyUtils.get(coinflip.get(2) as String)
                    opponent.sendMessage(" ")
                    opponent.sendMessage("§6§lCOINFLIP §> §e${player.name} §fhas challenged you to a coinflip for ${storage.map(amount)}")
                    opponent.sendMessage(" ")
                }
            }
            break
        default:
            break
    }
}

static void showCoinflipMenu(Player player, int page = 1) {

    List<List<Object>> coinflips = new ArrayList<>(CoinflipUtils.COINFLIPS)
    getCfStats(player).thenAccept(result -> {
        int wins = result.getOrDefault("wins", 0)
        int losses = result.getOrDefault("losses", 0)
        CoinflipUtils.sort(coinflips)

        MenuBuilder builder
        builder = MenuUtils.createPagedMenu("§8Coinflip", coinflips, { List<Object> coinflip, Integer i ->
            return getCoinflipItem(coinflip, player, true)
        }, page, false, [
                { p, t, s -> handleCoinflipEvent(player as Player, t as ClickType, s as Integer, builder, { showCoinflipMenu(player, page) }) },
                { p, t, s -> showCoinflipMenu(player, page + 1) },
                { p, t, s -> showCoinflipMenu(player, page - 1) }
        ])

        int size = builder.get().getSize()
        builder.set(builder.get().getSize() - 8, FastItemUtils.createItem(Material.SUNFLOWER, "§8[§eRefresh§8]", [
                "",
                "§e * Click to refresh page * "
        ]), { p, t, s ->
            showCoinflipMenu(player, page)
        })

        builder.set(size - 6, FastItemUtils.createItem(Material.WRITABLE_BOOK, "§8[§aCreate§8]", ["", "§e * Click to create a coinflip * "]), { p, t, s ->
            if (CoinflipUtils.getCoinflips(player.getUniqueId()).size() >= CoinflipUtils.MAXIMUM_ACTIVE_COINFLIPS) {
                Players.msg(player, "§6§lCOINFLIP §> §fYou cannot have more than §e${CoinflipUtils.MAXIMUM_ACTIVE_COINFLIPS} §factive coinflips!")
                player.closeInventory()
                return
            }
            List<Object> coinflip = CoinflipUtils.get(UUID.randomUUID(), player.getUniqueId(), null, null, null)
            startCoinflip(player, coinflip)
        })

        builder.set(size - 4, FastItemUtils.createSkull(player, "§8[§3Your Coinflips§8]", ["", "§e * Click to view your coinflips * "]), { p, t, s -> showCoinflips(player) })

        double ratio = 0.0D

        if (wins > 0) {
            ratio = (double) 100.0D / (wins + losses) * wins
        }
        ArrayList<String> statsLore = new ArrayList<>()
        statsLore.addAll([
                "§fWins: §a${NumberUtils.format(wins)}",
                "§fLosses: §c${NumberUtils.format(losses)}",
                "§fWin Ratio: §${ratio < 50 ? "c" : "a"}${NumberUtils.formatDouble(ratio)}%",
        ])
        if (Globals.CF_VALUES) {
            getCfStatsValue(player).thenAccept(resultValue -> {

                for (Map.Entry<String, Map<String, Long>> entry : resultValue) {
                    CurrencyStorage storage = CurrencyUtils.get(entry.key)
                    statsLore.add("")
                    statsLore.add("§f${storage?.displayName ?: StringUtils.capitalize(entry.key)} Win/Loss values: ")
                    statsLore.add("§a${NumberUtils.formatBalance(entry.value.get("win").toBigDecimal())}§f/§c${NumberUtils.formatBalance(entry.value.get("loss").toBigDecimal())}")
                }

                builder.set(size - 2, FastItemUtils.createItem(Material.BOOK, "§8[§9Your Statistics§8]", statsLore), { p, t, s -> })

                builder.set(size - 3, FastItemUtils.createSkull(player, "§8[§aPrivate Coinflips§8]", ["", "§e * Click to view players who challenged you * "]), { p, t, s -> showPrivateCoinflips(player) })

                MenuUtils.syncOpen(player, builder)
            })
        } else {
            builder.set(size - 2, FastItemUtils.createItem(Material.BOOK, "§8[§9Your Statistics§8]", statsLore), { p, t, s -> })

            builder.set(size - 3, FastItemUtils.createSkull(player, "§8[§aPrivate Coinflips§8]", ["", "§e * Click to view players who challenged you * "]), { p, t, s -> showPrivateCoinflips(player) })

            MenuUtils.syncOpen(player, builder)
        }
    })
}

Commands.create().assertPlayer().handler { command ->

    AtomicInteger rebootDelay = Exports.ptr("rebootDelay") as AtomicInteger
    if (rebootDelay != null && rebootDelay.intValue() != -1) {
        command.reply("§6§lCOINFLIP §> §cYou can't do that while the server is restarting!")
        return
    }

    showCoinflipMenu(command.sender())
}.register("coinflip", "cf")

Commands.create().assertOp().handler {
    String targetName = it.arg(0).parseOrFail(String)
    String currency = it.arg(1).parseOrFail(String)
    long amount = it.arg(2).parseOrFail(Long)
    Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)
    String color = (data.get("colors") as List<Material>).first().name()
    DatabaseUtils.getId(targetName, { uuid, name, player ->
        if (!uuid) {
            it.reply("§cPlayer never joined before.")
            return
        }
        List<Object> cf = CoinflipUtils.get(UUID.randomUUID(), player.getUniqueId(), null, null, null)
        cf.set(2, currency)
        cf.set(3, amount)
        cf.set(4, color)
        CoinflipUtils.register(cf)
        it.reply("§cDone.")
    })

}.register("dev/cf/test")

static CompletableFuture<Map<String, Map<String, Long>>> getCfStatsValue(Player player) {
    CompletableFuture<Map<String, Map<String, Long>>> stats = new CompletableFuture()
    Map<String, Map<String, Long>> map = new HashMap<String, Map<String, Long>>()
    MySQL.getAsyncDatabase().executeQuery("SELECT * FROM coinflip_stats_value WHERE ${DatabaseUtils.getServerUserExpression(player)}", { statement -> }, { result ->

        while (result.next()) {
            String currency = result.getString(3)
            long winValue = result.getLong(4)
            long lossValue = result.getLong(5)
            map.put(currency, ["win": winValue, "loss": lossValue])
        }

        stats.complete(map)
    })
    return stats
}

static CompletableFuture<Map<String, Integer>> getCfStats(Player player) {
    CompletableFuture<Map<String, Integer>> stats = new CompletableFuture()
    MySQL.getAsyncDatabase().executeQuery("SELECT * FROM coinflip_stats WHERE ${DatabaseUtils.getServerUserExpression(player)}", { statement -> }, { result ->
        long wins = 0
        long losses = 0
        if (result.next()) {
            wins = result.getInt(3)
            losses = result.getInt(4)
        }
        stats.complete([
                "wins"  : wins,
                "losses": losses
        ])
    })
    return stats
}

static long getAmount(String amountString) {
    String raw = amountString.toLowerCase().replace(",", "")
    int decimalPlaces = 0
    if (raw.contains(".")) {
        int integerPlaces = raw.indexOf('.')
        decimalPlaces = raw.length() - integerPlaces - 2
    }

    int count = 0
    if (raw.contains("k")) {
        count += raw.count("k")
        raw = raw.replace("k", "000")
    }
    if (raw.contains("m")) {
        count += raw.count("m")
        raw = raw.replace("m", "000000")
    }
    if (raw.contains("b")) {
        count += raw.count("b")
        raw = raw.replace("b", "000000000")
    }
    if (raw.contains("t")) {
        count += raw.count("t")
        raw = raw.replace("t", "000000000000")
    }
    if (raw.contains("q")) {
        count += raw.count("q")
        raw = raw.replace("q", "0000000000000000")
    }

    if (count > 1) {
        raw = "0"
    }

    if (count == 1 && decimalPlaces > 0) {
        raw = raw.substring(0, raw.length() - decimalPlaces)
        raw = raw.replace(".", "")
    }

    LongUtils.LongParseResult priceResult = LongUtils.parseLong(raw)
    return priceResult.value
}

Commands.create().assertPermission("commands.resetcoinflips").handler { command ->
    CoinflipUtils.clear()
    command.reply("§6§lCOINFLIP §> §fSuccessfully cleared coinflip data!")
}.register("resetcoinflips", "resetcoinflip", "resetcf")

@CompileStatic(TypeCheckingMode.SKIP)
class CoinflipUtils {
    static int MAXIMUM_ACTIVE_COINFLIPS = 3

    static Map<String, LinkedHashMap<String, Object>> COINFLIP = [
            shards : [
                    priority        : 2,
                    name            : "§bShards",
                    item            : Material.PRISMARINE_SHARD,
                    minimum_bet     : 1,
                    colors          : [
                            Material.LIGHT_BLUE_CONCRETE_POWDER,
                            Material.CYAN_CONCRETE_POWDER
                    ],
                    announce_minimum: 1000
            ],
            money: [
                    priority        : 1,
                    name            : "§6Money",
                    item            : Material.MAGMA_CREAM,
                    minimum_bet     : 100,
                    colors          : [
                            Material.MAGENTA_CONCRETE_POWDER,
                            Material.PURPLE_CONCRETE_POWDER
                    ],
                    announce_minimum: 50000000
            ],
            "exp"  : [
                    priority        : 3,
                    name            : "§eExperience",
                    item            : Material.EXPERIENCE_BOTTLE,
                    minimum_bet     : 100,
                    colors          : [
                            Material.YELLOW_CONCRETE_POWDER,
                            Material.ORANGE_CONCRETE_POWDER
                    ],
                    announce_minimum: 1000,
                    max_amount      : 500_000_000,
                    special         : [
                            has : { Player p, long amount -> ExpUtils.hasExperience(p, amount.intValue()) },
                            add : { Player p, long amount, boolean message = false ->
                                ExpUtils.giveExperience(p, amount.intValue(), { ExpUtils.ExpCallbackReason cb ->
                                    if (cb.success) {
                                        if (message)
                                            Players.msg(p, "§] §> §e${NumberUtils.format(amount)} Experience §fhave been added to your account.")
                                    } else {
                                        Players.msg(p, "§! §> §f${cb.errorMessage}")
                                    }
                                } as Closure)
                            },
                            take: { Player p, long amount, boolean message = false ->
                                ExpUtils.takeExperience(p, amount.intValue(), { ExpUtils.ExpCallbackReason cb ->
                                    if (cb.success) {
                                        if (message)
                                            Players.msg(p, "§] §> §e${NumberUtils.format(amount)} Experience §fhave been taken from your account.")
                                    } else {
                                        Players.msg(p, "§! §> §f${cb.errorMessage}")
                                    }
                                } as Closure)
                            },
                            map : { long amount -> return "§e${NumberUtils.format(amount)} §eExperience" }
                    ]
            ]
    ]

    static List<List<Object>> COINFLIPS
    static List<List<Object>> PRIVATE_COINFLIPS
    static Map<UUID, Set<UUID>> PLAYER_COINFLIPS

    static void init() {
        boolean loaded = Persistent.persistentMap.containsKey("coinflips")
        COINFLIPS = Persistent.of("coinflips", new ArrayList<List<Object>>()).get()
        PLAYER_COINFLIPS = Persistent.of("player_coinflips", new HashMap<UUID, Set<UUID>>()).get()
        PRIVATE_COINFLIPS = Persistent.of("private_coinflips", new ArrayList<List<Object>>()).get()

        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS coinflips (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, uuid_owner_least BIGINT NOT NULL, uuid_owner_most BIGINT NOT NULL, currency VARCHAR(32) NOT NULL, amount BIGINT NOT NULL, color VARCHAR(32) NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, server_id))")

        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS private_coinflips (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, uuid_owner_least BIGINT NOT NULL, uuid_owner_most BIGINT NOT NULL, uuid_opponent_least BIGINT NOT NULL, uuid_opponent_most BIGINT NOT NULL, currency VARCHAR(32) NOT NULL, amount BIGINT NOT NULL, color VARCHAR(32) NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, server_id))")

        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS coinflip_stats (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, wins INT NOT NULL, losses INT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, server_id))")

        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS coinflip_stats_value (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, currency VARCHAR(32) NOT NULL, wins BIGINT NOT NULL, losses BIGINT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, currency, server_id))")

        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS coinflip_win_queue (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, currency VARCHAR(32) NOT NULL, amount BIGINT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, currency, server_id))")

        if (loaded) {
            return
        }

        ThreadUtils.runAsync {
            Logger logger = Starlight.plugin.getLogger()
            logger.info("[Coinflip] Loading coinflips...")

            MySQL.getSyncDatabase().executeQuery("SELECT * FROM coinflips WHERE ${DatabaseUtils.getServerIdExpression()}", { statement -> }, { result ->
                while (result.next()) {
                    UUID id = new UUID(result.getLong(2), result.getLong(1))
                    UUID owner = new UUID(result.getLong(4), result.getLong(3))

                    String currency = result.getString(5)
                    long amount = result.getLong(6)
                    String color = result.getString(7)

                    List<Object> coinflip = get(id, owner, currency, amount, color)
                    register(coinflip, false)
                }
            })
            logger.info("[Coinflip] Loaded coinflips!")
        }

        ThreadUtils.runAsync {
            Logger logger = Starlight.plugin.getLogger()
            logger.info("[Coinflip] Loading private coinflips...")

            MySQL.getSyncDatabase().executeQuery("SELECT * FROM private_coinflips WHERE ${DatabaseUtils.getServerIdExpression()}", { statement -> }, { result ->
                while (result.next()) {
                    UUID id = new UUID(result.getLong(2), result.getLong(1))
                    UUID owner = new UUID(result.getLong(4), result.getLong(3))
                    UUID opponent = new UUID(result.getLong(6), result.getLong(5))

                    String currency = result.getString(7)
                    long amount = result.getLong(8)
                    String color = result.getString(9)

                    List<Object> coinflip = get(id, owner, currency, amount, color, opponent)
                    register(coinflip, false)
                }
            })
            logger.info("[Coinflip] Loaded private coinflips!")
        }
    }

    // UUID id, UUID owner, String currency, long amount, String color, UUID opponent (only in private ones)
    static List<Object> get(Object... objects) {
        return Arrays.asList(objects)
    }

    static void register(List<Object> coinflip, boolean updateDatabase = true) {

        UUID owner = coinflip.get(1) as UUID
        UUID id = coinflip.get(0) as UUID

        if (updateDatabase) {
            Starlight.log.info("[Coinflip] ${Bukkit.getOfflinePlayer(owner)?.name ?: owner.toString()} created a ${coinflip.get(2)} coinflip with amount: ${NumberUtils.format(coinflip.get(3) as long)}")
        }

        long ownerLeast = owner.getLeastSignificantBits()
        long ownerMost = owner.getMostSignificantBits()

        if (isCoinflipPrivate(coinflip)) {
            UUID opponent = coinflip.get(5) as UUID

            long opponentLeast = opponent.leastSignificantBits
            long opponentMost = opponent.mostSignificantBits

            PRIVATE_COINFLIPS.add(coinflip)

            PLAYER_COINFLIPS.computeIfAbsent(owner, { k -> new HashSet<>() }).add(id)

            if (!updateDatabase) {
                return
            }

            MySQL.getAsyncDatabase().execute("INSERT INTO private_coinflips (uuid_least, uuid_most, uuid_owner_least, uuid_owner_most, uuid_opponent_least, uuid_opponent_most, currency, amount, color, server_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", { statement ->
                statement.setLong(1, id.getLeastSignificantBits())
                statement.setLong(2, id.getMostSignificantBits())
                statement.setLong(3, ownerLeast)
                statement.setLong(4, ownerMost)
                statement.setLong(5, opponentLeast)
                statement.setLong(6, opponentMost)
                statement.setString(7, coinflip.get(2) as String)
                statement.setLong(8, coinflip.get(3) as long)
                statement.setString(9, coinflip.get(4) as String)
                statement.setString(10, Temple.templeId)
            })
        } else {

            COINFLIPS.add(coinflip)

            PLAYER_COINFLIPS.computeIfAbsent(owner, { k -> new HashSet<>() }).add(id)

            if (!updateDatabase) {
                return
            }

            MySQL.getAsyncDatabase().execute("INSERT INTO coinflips (uuid_least, uuid_most, uuid_owner_least, uuid_owner_most, currency, amount, color, server_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", { statement ->
                statement.setLong(1, id.getLeastSignificantBits())
                statement.setLong(2, id.getMostSignificantBits())
                statement.setLong(3, ownerLeast)
                statement.setLong(4, ownerMost)
                statement.setString(5, coinflip.get(2) as String)
                statement.setLong(6, coinflip.get(3) as long)
                statement.setString(7, coinflip.get(4) as String)
                statement.setString(8, Temple.templeId)
            })
        }
    }

    static void addQueue(UUID owner, List<Object> coinflip) {
        MySQL.getAsyncDatabase().execute("INSERT INTO coinflip_win_queue (uuid_least, uuid_most, currency, amount, server_id) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount)", { statement ->
            statement.setLong(1, owner.leastSignificantBits)
            statement.setLong(2, owner.mostSignificantBits)
            statement.setString(3, coinflip.get(2) as String)
            statement.setLong(4, coinflip.get(3) as long)
            statement.setString(5, Temple.templeId)
        })
    }

    static void deleteQueue(Player player) {
        MySQL.getAsyncDatabase().execute("DELETE FROM coinflip_win_queue WHERE ${DatabaseUtils.getServerUserExpression(player)}")
    }

    static Set<UUID> getCoinflips(UUID owner) {
        return PLAYER_COINFLIPS.getOrDefault(owner, new HashSet<>())
    }

    static boolean isCoinflipPrivate(List<Object> coinflip) {
        try {
            coinflip.get(5) as UUID
            return true
        } catch (Exception ignore) {
            return false
        }
    }

    static void delete(List<Object> coinflip, boolean played = true) {
        UUID id = coinflip.get(0) as UUID

        int index

        if (isCoinflipPrivate(coinflip)) {
            index = PRIVATE_COINFLIPS.indexOf(coinflip)
            if (index == -1) {
                return
            }
            PRIVATE_COINFLIPS.remove(index)
        } else {
            index = COINFLIPS.indexOf(coinflip)
            if (index == -1) {
                return
            }
            COINFLIPS.remove(index)
        }

        UUID owner = coinflip.get(1) as UUID
        getCoinflips(owner).remove(id)

        if (isCoinflipPrivate(coinflip)) {
            MySQL.getAsyncDatabase().execute("DELETE FROM private_coinflips WHERE ${DatabaseUtils.getServerUserExpression(id)}")
        } else {
            MySQL.getAsyncDatabase().execute("DELETE FROM coinflips WHERE ${DatabaseUtils.getServerUserExpression(id)}")
        }

        if (!played) {
            Map<String, Object> data = COINFLIP.get(coinflip.get(2) as String)
            Player player = Bukkit.getPlayer(owner)
            if (data.get("special") != null) {
                Map<String, Closure> specialCurrency = data.get("special") as Map<String, Closure>
                if (player) {
                    specialCurrency.get("add").call(player, coinflip.get(3) as long, true)
                } else addQueue(owner, coinflip)
                return
            }
            CurrencyUtils.get(coinflip.get(2) as String).add(owner, coinflip.get(3) as long, null, true)
        }
    }

    static List<Object> getCoinflip(UUID id) {

        for (List<Object> coinflip : COINFLIPS) {
            if (coinflip.get(0) as UUID == id) {
                return coinflip
            }
        }

        for (List<Object> privateCoinflip : PRIVATE_COINFLIPS) {
            if (privateCoinflip.get(0) as UUID == id) {
                return privateCoinflip
            }
        }

        return null
    }

    //Only for global coinflips
    static void sort(List<List<Object>> coinflips) {
        coinflips.sort { coinflip, coinflip1 ->
            Map<String, Object> data = COINFLIP.get(coinflip.get(2) as String)
            Map<String, Object> data1 = COINFLIP.get(coinflip1.get(2) as String)

            return 1 * ((coinflip1.get(3) as Integer) <=> (coinflip.get(3) as Integer)) + 2 * ((data1.get("priority") as Integer) <=> (data.get("priority") as Integer))
        }
    }

    static void clear() {

        COINFLIPS.clear()
        PRIVATE_COINFLIPS.clear()
        PLAYER_COINFLIPS.clear()

        MySQL.getAsyncDatabase().execute("DELETE FROM coinflips WHERE ${DatabaseUtils.getServerIdExpression()}")
        MySQL.getAsyncDatabase().execute("DELETE FROM private_coinflips WHERE ${DatabaseUtils.getServerIdExpression()}")

    }
}