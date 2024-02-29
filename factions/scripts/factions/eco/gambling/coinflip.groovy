package scripts.factions.eco.gambling

import com.google.common.collect.Sets
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
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.tags.ItemTagType
import scripts.exec.Globals
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.CurrencyUtils
import scripts.shared.legacy.ToggleUtils
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.objects.MutableBoolean
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.systems.Bedrock
import scripts.shared.systems.MenuEvent
import scripts.shared.utils.MenuDecorator
import scripts.shared.utils.Persistent
import scripts.shared.utils.Temple
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.LongUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.legacy.utils.PlayerUtils
import scripts.shared.legacy.utils.SignUtils
import scripts.shared.legacy.utils.StringUtils
import scripts.shared.legacy.utils.ThreadUtils
import scripts.shared.systems.MenuBuilder

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
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
            "black"     : "§0",
            "diamond"   : "§b",
            "emerald"   : "§a"
    ].get(color.toLowerCase())
}

static ItemStack getCoinflipItem(Coinflip coinflip, Player viewer, boolean doDelete = false) {
    if (coinflip.isItem()) {
        return getCoinflipMenuItem(coinflip, viewer, doDelete)
    }
    ItemStack item = null

    UUID owner = coinflip.getOwner()

    DatabaseUtils.getLatestUsername(owner, { username ->
        String currency = coinflip.getCurrency()
        Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)

        String color = StringUtils.capitalize(coinflip.getColor().replace("_", " ")).replace(" Concrete Powder", "")

        item = FastItemUtils.createItem(data.get("item") as Material, "§e${username}'s Coinflip", [])

        List<String> lore

        if (coinflip.isPrivate()) {
            lore = [
                    "",
                    viewer.uniqueId == owner ? "§9Your Opponent: §f${Players.getOfflineNullable(coinflip.opponent).name}" : "§9Challenged By: §f${Players.getOffline(owner).get().getName()}",
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

    FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "coinflip_id"), ItemTagType.STRING, coinflip.getId().toString())

    return item
}

static ItemStack getCoinflipMenuItem(Coinflip coinflip, Player viewer, boolean doDelete) {
    ItemStack item = coinflip.getItem().clone()
    List<String> oldLore = item.getLore() ?: new ArrayList<String>()
    List<String> newLore = new ArrayList<String>(oldLore)
    String color = StringUtils.capitalize(coinflip.getColor().replace("_", " ")).replace(" Concrete Powder", "")
    UUID owner = coinflip.getOwner()
    DatabaseUtils.getLatestUsername(owner, { String username ->
        newLore.addAll([
                "",
                "§c§m------------------",
                "",
                "§9Owner: §e${username}",
                "§9Color: §e${getColor(color) + color}",
                ""
        ])
        boolean isOwner = viewer.getUniqueId() == owner

        if (viewer.hasPermission("coinflip.delete") && doDelete && !isOwner) {
            newLore.addAll([
                    "§e * Left click to challenge *",
                    "§c * Right click to delete *"
            ])
        } else if (isOwner) {
            newLore.add("§c * Click to delete * ")
        } else {
            newLore.add("§e * Click to challenge * ")
        }
        FastItemUtils.setLore(item, newLore)
    }, false)

    FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "coinflip_id"), ItemTagType.STRING, coinflip.getId().toString())
    return item
}

static String mapBalance(Coinflip coinflip) {
    String currency = coinflip.getCurrency()
    Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)
    long amount = coinflip.getAmount()
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
    Coinflip coinflip = CoinflipUtils.getCoinflip(id)

    if (coinflip == null) {
        Players.msg(player, "§6§lCOINFLIP §> §fThis coinflip is no longer available!")
        player.closeInventory()
        return
    }

    UUID owner = coinflip.getOwner()
    boolean isOwner = owner == player.getUniqueId()

    if ((player.hasPermission("coinflip.delete") && !isOwner && type == ClickType.RIGHT) || isOwner) {
        CoinflipUtils.delete(coinflip, false)
        Players.msg(player, "§6§lCOINFLIP §> §fSuccessfully deleted coinflip!")
        onDelete.run()
        return
    }
    String currency = coinflip.getCurrency()
    Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)

    if (coinflip.isItem()) {
        takeItemFromInv(player, coinflip).thenAccept {
            if (it == null) {
                Players.msg(player, "§! §> §cCan not find a similar item in your inventory!")
                return
            }
            coinflip.setOpponentItem(it)
            rollCoinflip(coinflip, player)
        }
        return
    }

    if (data.get("special") != null) {
        Map<String, Closure> specialCurrency = data.get("special") as Map<String, Closure>
        if (!specialCurrency.get("has").call(player, coinflip.getAmount())) {
            Players.msg(player, "not enough")
            return
        }
        specialCurrency.get("take").call(player, coinflip.getAmount(), true)
        rollCoinflip(coinflip, player)
        return
    }

    CurrencyUtils.get(currency).take(player, coinflip.getAmount(), {
        rollCoinflip(coinflip, player)
    })
}

static void rollCoinflip(Coinflip coinflip, Player player) {
    CoinflipUtils.COINFLIPPING.add(player.uniqueId)
    CoinflipUtils.COINFLIPPING.add(coinflip.owner)

    CoinflipUtils.delete(coinflip)
    UUID owner = coinflip.getOwner()
    Map<String, Object> data = CoinflipUtils.COINFLIP.get(coinflip.getCurrency())
    Material ownerColor = Material.valueOf(coinflip.getColor())
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
            if (Bukkit.getPlayer(owner)?.isOp()) {
                if (ownerColor == Material.DIAMOND) {
                    // win
                } else if (ownerColor == Material.EMERALD) {
                    --iterations
                } else if (ThreadLocalRandom.current().nextBoolean()) {
                    --iterations
                }
            } else if (ThreadLocalRandom.current().nextBoolean()) {
                --iterations
            }

            showRollingInventory(player, owner, username, coinflip, inventory, ownerColor, challengerColor, iterations)
        }
    })
}

static void updateInventory(Inventory inventory, Material ownerColor, Material challengerColor, String ownerName, String challengerName, int iteration) {
    Material material = iteration % 2 == 0 ? ownerColor : challengerColor
    String color = StringUtils.capitalize(material.name().replace("_", " ")).replace(" Concrete Powder", "")
    inventory.setItem(13, FastItemUtils.createItem(material, "${getColor(color)}${iteration % 2 == 0 ? ownerName : challengerName}", []))
}

static void showRollingInventory(Player player, UUID owner, String ownerName, Coinflip coinflip, Inventory inventory, Material ownerColor, Material challengerColor, int iterations) {
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

                String currency = coinflip.getCurrency()
                long amount = coinflip.getAmount()

                CurrencyStorage storage = CurrencyUtils.get(currency)
                String mapped = mapBalance(coinflip)
                String playerName = player.getName()

                if (ownerWon) {
                    PlayerUtils.sendMessage(owner, "§6§lCOINFLIP §> §fYou have defeated §e${playerName} §fin a coinflip match for ${mapped}§f!")
                    Players.msg(player, "§6§lCOINFLIP §> §fYou have §clost §fa coinflip match against §e${ownerName} §ffor ${mapped}§f!")
                    Starlight.log.info("${ownerName} won the coinflip for ${amount * 2}")
                    Starlight.log.info("${playerName} lost the coinflip for ${amount * 2}")
                } else {
                    Players.msg(player, "§6§lCOINFLIP §> §fYou have defeated §e${ownerName} §fin a coinflip match for ${mapped}§f!")
                    PlayerUtils.sendMessage(owner, "§6§lCOINFLIP §> §fYou have §clost §fa coinflip match against §e${playerName} §ffor ${mapped}§f!")
                    Starlight.log.info("${playerName} won the coinflip for ${amount * 2}")
                    Starlight.log.info("${ownerName} lost the coinflip for ${amount * 2}")
                }
                long reward = amount * 2

                if (reward > 10) {
                    reward = (reward * 0.9D) as long
                }

                Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)

                if (coinflip.isItem()) {
                    FastInventoryUtils.addOrBox(winner, null, Bukkit.getConsoleSender(), coinflip.getItem(), null)
                    FastInventoryUtils.addOrBox(winner, null, Bukkit.getConsoleSender(), coinflip.getOpponentItem(), "§6§lCOINFLIP §> §fItems have been added to your inventory or box!")
                } else if (data.get("special") != null) {
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
                    String message
                    if (coinflip.isItem()) {
                        message = "\n§6§lCOINFLIP §> §e${ownerWon ? ownerName : playerName} §fhas defeated §e${ownerWon ? playerName : ownerName} §fin an item coinflip and won ${coinflip.getItem().getItemMeta().getDisplayName() ?: StringUtils.capitalize(coinflip.getItem().type.name().toLowerCase().replace("_", " "))}"
                        Starlight.log.info("[Coinflip] ${ownerWon ? ownerName : playerName} defeated ${ownerWon ? playerName : ownerName} in an item coinflip and won ${coinflip.getItem().toString()}")
                    } else {
                        message = "\n§6§lCOINFLIP §> §e${ownerWon ? ownerName : playerName} §fhas defeated §e${ownerWon ? playerName : ownerName} §fin a coinflip for ${mapped}§f!\n "
                        Starlight.log.info("[Coinflip] ${ownerWon ? ownerName : playerName} defeated ${ownerWon ? playerName : ownerName} in a coinflip for ${mapped}")
                    }
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (!ToggleUtils.hasToggled(target, "coinflip_announcements")) {
                            Players.msg(target, message)
                        }
                    }
                }
                running.setValue(false)

                CoinflipUtils.COINFLIPPING.remove(player.uniqueId)
                CoinflipUtils.COINFLIPPING.remove(coinflip.owner)

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

Events.subscribe(InventoryClickEvent.class, EventPriority.HIGH).filter(EventFilters.ignoreCancelled()).handler { event ->
    String title = event.getView()?.getTitle()

    if (title != null && title == "§0§8Coinflip") {
        event.setCancelled(true)
    }
}

static void showCoinflips(Player player, UUID target = player.getUniqueId(), int page = 1, boolean itemsCoinflip = false) {
    List<Coinflip> coinflips = new ArrayList<>()

    coinflips.addAll(CoinflipUtils.COINFLIPS.findAll { it.value.getOwner() == target }.values())
    coinflips.addAll(CoinflipUtils.PRIVATE_COINFLIPS.findAll { it.value.getOwner() == target }.values())

    Schedulers.async().run {
        CoinflipUtils.sort(coinflips)

        MenuBuilder builder

        builder = MenuUtils.createPagedMenu("§8Your${itemsCoinflip ? " Items" : ""} Coinflips", coinflips, { Coinflip coinflip, Integer i ->
            return getCoinflipItem(coinflip, player)
        }, page, target == player.getUniqueId(), [
                { p, t, s -> handleCoinflipEvent(player as Player, t as ClickType, s as Integer, builder, { showCoinflips(player, target, page, itemsCoinflip) }) },
                { p, t, s -> showCoinflips(player, target, page + 1, itemsCoinflip) },
                { p, t, s -> showCoinflips(player, target, page - 1, itemsCoinflip) },
                { p, t, s -> showCoinflipMenu(player) }
        ])

        if (CoinflipUtils.COINFLIP.containsKey("items")) {
            builder.set(builder.get().size() - 6, FastItemUtils.createItem(Material.DIAMOND, "§8[§cShow ${itemsCoinflip ? "Normal" : "Item"} Coinflips§8]", ["", "§e * Click to show ${itemsCoinflip ? "normal" : "item"} coinflips * "]), { p, t, s ->
                showCoinflips(player, target, 1, !itemsCoinflip)
            })
        }
        MenuUtils.syncOpen(player, builder)
    }
}

static void showPrivateCoinflips(Player player, int page = 1, boolean itemsCoinflip = false) {
    List<Coinflip> coinflips = new ArrayList<>()

    coinflips.addAll(CoinflipUtils.PRIVATE_COINFLIPS.findAll { (it.value.getOpponent() == player.uniqueId || it.value.getOwner() == player.uniqueId) && it.value.isItem() == itemsCoinflip }.values())

    Schedulers.async().run {
        CoinflipUtils.sort(coinflips)

        MenuBuilder builder

        builder = MenuUtils.createPagedMenu("§8Private${itemsCoinflip ? " Items" : ""} Coinflips", coinflips, { Coinflip coinflip, Integer i ->
            return getCoinflipItem(coinflip, player)
        }, page, true, [
                { p, t, s -> handleCoinflipEvent(player as Player, t as ClickType, s as Integer, builder, { showPrivateCoinflips(player, page, itemsCoinflip) }) },
                { p, t, s -> showPrivateCoinflips(player, page + 1, itemsCoinflip) },
                { p, t, s -> showPrivateCoinflips(player, page - 1, itemsCoinflip) },
                { p, t, s -> showCoinflipMenu(player) }
        ])

        if (CoinflipUtils.COINFLIP.containsKey("items")) {
            builder.set(builder.get().size() - 6, FastItemUtils.createItem(Material.DIAMOND, "§8[§cShow ${itemsCoinflip ? "Normal" : "Item"} Coinflips§8]", ["", "§e * Click to show ${itemsCoinflip ? "normal" : "item"} coinflips * "]), { p, t, s ->
                showPrivateCoinflips(player, 1, !itemsCoinflip)
            })
        }

        MenuUtils.syncOpen(player, builder)
    }
}

static void startCoinflip(Player player, Coinflip coinflip, int stage = 0, long amt = 0) {
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
                        coinflip.setCurrency(currency)
                        if (coinflip.isItem()) {
                            coinflip.setAmount(1)
                            handleCreateCoinflipItems(player, coinflip)
                            return
                        }
                        int plus = 1
                        if (amt > 0 && amt >= (CoinflipUtils.COINFLIP.get(currency).get("minimum_bet") as long)) {
                            CurrencyUtils.get(currency).get(player, { balance ->
                                if ((balance <=> amt) >= 0) {
                                    plus = 2
                                    coinflip.setAmount(amt)
                                }
                                startCoinflip(player, coinflip, stage + plus)
                            })
                            return
                        }
                        startCoinflip(player, coinflip, stage + plus)
                    },
                    { p, t, s -> },
                    { p, t, s -> },
                    { p, t, s -> showCoinflipMenu(player) }
            ])
            builder.open(player)
            break
        case 1:
            String currency = coinflip.getCurrency()
            Map<String, Object> data = CoinflipUtils.COINFLIP.get(currency)
            Consumer<String> handleInput = { String input ->
                long amount = getAmount(input)
                if (amount <= 0) {
                    Players.msg(player, "§6§lCOINFLIP §> §e${input} §fis not a valid amount!")
                    return
                }

                showCoinflipInputMenu(player, amount, data, currency, coinflip, stage)
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
            String currency = coinflip.getCurrency()
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
                            specialCurrency.get("take").call(player, coinflip.getAmount() as long, true)
                            coinflip.setColor(color)
                            startCoinflip(player, coinflip, stage + 1)
                            return
                        }
                        CurrencyUtils.get(currency).take(player, coinflip.getAmount() as long, {
                            coinflip.setColor(color)
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
            boolean isPrivate = coinflip.isPrivate()
            if (isPrivate && ToggleUtils.hasToggled(coinflip.getOpponent(), "private_coinflips")) {
                Players.msg(player, "§6§lCOINFLIP §> §cThis player is currently not accepting private coinflips.")
                break
            }
            CoinflipUtils.register(coinflip)

            Players.msg(player, "§6§lCOINFLIP §> §fSuccessfully created a" + (isPrivate ? " §e§nprivate§f " : " §e§npublic§f ") + "coinflip!")
            showCoinflipMenu(player)

            if (coinflip.isPrivate()) {
                UUID opponentUUID = coinflip.getOpponent()
                Player opponent = Players.getNullable(opponentUUID)

                if (opponent != null && opponent.isOnline()) {
                    long amount = coinflip.getAmount()
                    opponent.sendMessage(" ")
                    opponent.sendMessage("§6§lCOINFLIP §> §e${player.name} §fhas challenged you to a coinflip for ${mapBalance(coinflip.getCurrency(), amount)}")
                    opponent.sendMessage(" ")
                }
            }
            break
        default:
            break
    }
}

static void showCoinflipInputMenu(Player player, long amount, Map<String, Object> data, String currency, Coinflip coinflip, int stage) {
    long minimum = data.get("minimum_bet") as long

    if (amount < minimum) {
        Players.msg(player, "§6§lCOINFLIP §> §fThe minimum bet for this currency is ${mapBalance(currency, minimum)}§f!")
        return
    }

    if (data.get("special") != null) {
        Map<String, Closure> specialCurrenc = data.get("special") as Map<String, Closure>
        if (!specialCurrenc.get("has").call(player, amount)) {
            Players.msg(player, "§c§lERROR §8» §fYou do not have enough ${currency}!")
            return
        }
        coinflip.setAmount(amount)
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
        coinflip.setAmount(amount)
        startCoinflip(player, coinflip, stage + 1)
    })
}

static void showCoinflipMenu(Player player, int page = 1, boolean itemsCoinflip = false) {
    List<Coinflip> coinflips = new ArrayList<>(CoinflipUtils.COINFLIPS.values()).findAll { it.isItem() == itemsCoinflip }
    getCfStats(player).thenAccept(result -> {
        int wins = result.getOrDefault("wins", 0)
        int losses = result.getOrDefault("losses", 0)
        CoinflipUtils.sort(coinflips)

        MenuBuilder builder
        builder = MenuUtils.createPagedMenu("§8${itemsCoinflip ? "Items " : ""}Coinflip", coinflips, { Coinflip coinflip, Integer i ->
            return getCoinflipItem(coinflip, player, true)
        }, page, false, [
                { p, t, s -> handleCoinflipEvent(p as Player, t as ClickType, s as Integer, builder, { showCoinflipMenu(player, page, itemsCoinflip) }) },
                { p, t, s -> showCoinflipMenu(player, page + 1, itemsCoinflip) },
                { p, t, s -> showCoinflipMenu(player, page - 1, itemsCoinflip) }
        ])


        int size = builder.get().getSize()
        builder.set(builder.get().getSize() - 8, FastItemUtils.createItem(Material.SUNFLOWER, "§8[§eRefresh§8]", [
                "",
                "§e * Click to refresh page * "
        ]), { p, t, s ->
            showCoinflipMenu(player, page, itemsCoinflip)
        })


        builder.set(size - 6, FastItemUtils.createItem(Material.WRITABLE_BOOK, "§8[§aCreate§8]", ["", "§e * Click to create a coinflip * "]), { p, t, s ->
            if (CoinflipUtils.getCoinflips(player.getUniqueId()).size() >= CoinflipUtils.MAXIMUM_ACTIVE_COINFLIPS) {
                Players.msg(player, "§6§lCOINFLIP §> §fYou cannot have more than §e${CoinflipUtils.MAXIMUM_ACTIVE_COINFLIPS} §factive coinflips!")
                player.closeInventory()
                return
            }
            Coinflip coinflip = new Coinflip(UUID.randomUUID(), player.getUniqueId(), null, null, null)
            startCoinflip(player, coinflip)
        })

        if (CoinflipUtils.COINFLIP.containsKey("items")) {
            builder.set(size - 7, FastItemUtils.createItem(Material.DIAMOND, "§8[§cShow ${itemsCoinflip ? "Normal" : "Item"} Coinflips§8]", ["", "§e * Click to show ${itemsCoinflip ? "normal" : "item"} coinflips * "]), { p, t, s ->
                showCoinflipMenu(player, 1, !itemsCoinflip)
            })
        }


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
    if (command.args().size() == 0) {
        showCoinflipMenu(command.sender())
        return
    }
    long amount = getAmount(command.rawArg(0))
    Player target = Bukkit.getPlayer(command.rawArg(0))
    if (amount > 0L && target == null) {
        if (CoinflipUtils.getCoinflips(command.sender().getUniqueId()).size() >= CoinflipUtils.MAXIMUM_ACTIVE_COINFLIPS) {
            command.reply("§6§lCOINFLIP §> §fYou cannot have more than §e${CoinflipUtils.MAXIMUM_ACTIVE_COINFLIPS} §factive coinflips!")
            return
        }
        startCoinflip(command.sender(), new Coinflip(UUID.randomUUID(), command.sender().getUniqueId(), null, null, null), 0, amount)
        return
    } else if (command.args().size() > 1) {
        amount = getAmount(command.rawArg(1))
    }

    if (target == null || !target.isOnline()) {
        command.reply("§6§lCOINFLIP §> §cThat player is not online.")
        return
    }

    if (target == command.sender()) {
        command.sender().sendMessage("§6§lCOINFLIP §> §cYou can't challenge yourself.")
        return
    }

    if (ToggleUtils.hasToggled(target, "private_coinflips")) {
        command.sender().sendMessage("§6§lCOINFLIP §> §cThis player is currently not accepting private coinflips.")
        return
    }

    if (CoinflipUtils.getCoinflips(command.sender().getUniqueId()).size() >= CoinflipUtils.MAXIMUM_ACTIVE_COINFLIPS) {
        command.sender().sendMessage("§6§lCOINFLIP §> §fYou cannot have more than §e${CoinflipUtils.MAXIMUM_ACTIVE_COINFLIPS} §factive coinflips!")
        command.sender().closeInventory()
        return
    }

    Coinflip coinflip = new Coinflip(UUID.randomUUID(), command.sender().getUniqueId(), null, null, null, target.getUniqueId())
    startCoinflip(command.sender(), coinflip, 0, amount)

}.register("coinflip", "cf")

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


static void handleCreateCoinflipItems(Player player, Coinflip coinflip = new Coinflip(UUID.randomUUID(), player.uniqueId, "items", 1)) {
    if (!coinflip.getColor()) {
        handlePickColor(player, coinflip)
        return
    }
    if (!coinflip.getItem()) {
        getQualifiedItemsFromInv(player, coinflip)
        return
    }
    takeItemFromInv(player, coinflip).thenAccept {
        if (it == null) {
            player.closeInventory()
            Players.msg(player, "§! §> §cCan not find the selected item in your inventory anymore.")
            return
        }
        CoinflipUtils.register(coinflip)

        Players.msg(player, "§6§lCOINFLIP §> §fSuccessfully created a" + (coinflip.isPrivate() ? " §e§nprivate§f " : " §e§npublic§f ") + "coinflip!")

        if (coinflip.isPrivate()) {
            UUID opponentUUID = coinflip.getOpponent()
            Player opponent = Players.getNullable(opponentUUID)

            if (opponent != null && opponent.isOnline()) {
                long amount = coinflip.getAmount()
                opponent.sendMessage("\n§6§lCOINFLIP §> §e${player.name} §fhas challenged you to a coinflip for ${mapBalance(coinflip.getCurrency(), amount)}\n ")
            }

            showPrivateCoinflips(player, 1, true)
            return
        }

        showCoinflipMenu(player, 1, true)
    }
}

static CompletableFuture<ItemStack> takeItemFromInv(Player player, Coinflip coinflip, boolean exact = player.getUniqueId() == coinflip.getOwner()) {
    CompletableFuture<ItemStack> cF = new CompletableFuture<ItemStack>()
    ItemStack item = coinflip.getItem()
    PlayerInventory inv = player.getInventory()
    Integer found = exact ? inv.getContents().findIndexOf { it != null && it == item && FastItemUtils.getId(it) == FastItemUtils.getId(item) } : inv.getContents().findIndexOf { it != null && compareQualifiedItems(it, item) }
    if (found != -1) {
        ItemStack opponentItem = inv.getItem(found)
        inv.setItem(found, null)
        cF.complete(opponentItem)
    } else
        cF.complete(null)

    return cF
}

static void handlePickColor(Player player, Coinflip coinflip) {
    Map<String, Object> data = CoinflipUtils.COINFLIP.get("items")
    Material color1 = (data.get("colors") as List<Material>)[0]
    Material color2 = (data.get("colors") as List<Material>)[1]
    NamespacedKey colorKey = new NamespacedKey(Starlight.plugin, "color")
    MenuBuilder builder = new MenuBuilder(3 * 9, "Pick your color")
    MenuDecorator.decorate(builder, [
            "c0c0c0c0c",
            "0c=c0c=c0",
            "c0c0c0c0c",
    ])

    String colorString1 = StringUtils.capitalize(color1.name().replace("_", " ")).replace(" Concrete Powder", "")
    ItemStack item1 = FastItemUtils.createItem(color1, getColor(colorString1) + colorString1, ["", "§e * Click to select color * "])
    FastItemUtils.setCustomTag(item1, colorKey, ItemTagType.STRING, color1.name())
    String colorString2 = StringUtils.capitalize(color2.name().replace("_", " ")).replace(" Concrete Powder", "")
    ItemStack item2 = FastItemUtils.createItem(color2, getColor(colorString2) + colorString2, ["", "§e * Click to select color * "])
    FastItemUtils.setCustomTag(item2, colorKey, ItemTagType.STRING, color2.name())

    MenuEvent clickEvent = { Player p, ClickType t, Integer s ->
        String color = FastItemUtils.getCustomTag(builder.get().getItem(s), colorKey, ItemTagType.STRING)
        coinflip.setColor(color)
        handleCreateCoinflipItems(p, coinflip)
    }

    builder.set(11, item1, clickEvent)
    builder.set(15, item2, clickEvent)
    builder.open(player)
}

static void getQualifiedItemsFromInv(Player player, Coinflip coinflip) {
    ItemStack[] inventoryContents = player.getInventory().getStorageContents()
    List<ItemStack> items = new ArrayList<>()
    for (int i = 0; i < inventoryContents.size(); i++) {
        ItemStack item = inventoryContents[i]
        if (item == null || item.type == Material.AIR) continue
        if (isQualifiedItem(item)) {
            items.add(item)
        }
    }
    openQualifiedItemsSelector(player, coinflip, items)
}

static void openQualifiedItemsSelector(Player player, Coinflip coinflip, List<ItemStack> items, int page = 1) {
    if (items.isEmpty()) {
        player.closeInventory()
        Players.msg(player, "§! §> §fYou do not have any qualified items in your inventory!")
        return
    }
    MenuBuilder builder

    builder = MenuUtils.createPagedMenu("§9§lSelect Item", items, { ItemStack item, Integer slot ->
        return item.clone()
    }, page, false, [
            { Player p, t, Integer s ->
                ItemStack item = items[s]
                coinflip.setItem(item)
                handleCreateCoinflipItems(p, coinflip)
            },
            { p, t, s -> openQualifiedItemsSelector(player, coinflip, items, page + 1) },
            { p, t, s -> openQualifiedItemsSelector(player, coinflip, items, page - 1) },
    ])

    MenuUtils.syncOpen(player, builder)
}

static boolean isQualifiedItem(ItemStack item) {
    return CoinflipUtils.isSpecialItem(item) || FastItemUtils.hasId(item)
}

static boolean compareQualifiedItems(ItemStack item1, ItemStack item2) {
    return CoinflipUtils.compareItems(item1, item2)
}


@CompileStatic(TypeCheckingMode.SKIP)
class CoinflipUtils {
    static int MAXIMUM_ACTIVE_COINFLIPS = 3
    static final NamespacedKey qualifiedItemSlotKey = new NamespacedKey(Starlight.plugin, "coinflip/qualifiedslotkey")

    static Map<String, LinkedHashMap<String, Object>> COINFLIP = [
//            stardust : [
//                    priority        : 2,
//                    name            : "§bStardust",
//                    item            : Material.NETHER_STAR,
//                    minimum_bet     : 1,
//                    colors          : [
//                            Material.LIGHT_BLUE_CONCRETE_POWDER,
//                            Material.CYAN_CONCRETE_POWDER
//                    ],
//                    announce_minimum: 50
//            ],
            money: [
                    priority        : 1,
                    name            : "§dMoney",
                    item            : Material.MAGMA_CREAM,
                    minimum_bet     : 100,
                    colors          : [
                            Material.MAGENTA_CONCRETE_POWDER,
                            Material.PURPLE_CONCRETE_POWDER
                    ],
                    announce_minimum: 50000
            ]
    ]

    static ConcurrentHashMap<UUID, Coinflip> COINFLIPS
    static ConcurrentHashMap<UUID, Coinflip> PRIVATE_COINFLIPS
    static Map<UUID, Set<UUID>> PLAYER_COINFLIPS
    static Set<UUID> COINFLIPPING = Sets.newConcurrentHashSet()

    static void init() {
        boolean loaded = Persistent.persistentMap.containsKey("coinflips")
        if (Globals.ITEM_CF) {
            COINFLIP.put("items", [
                    priority        : 3,
                    name            : "§cItems",
                    item            : Material.DIAMOND,
                    minimum_bet     : 1,
                    colors          : [
                            Material.DIAMOND,
                            Material.EMERALD
                    ],
                    announce_minimum: 1,
                    special         : [
                            has : { Player p, long amount -> false },
                            add : { Player p, long amount, boolean message = false -> },
                            take: { Player p, long amount, boolean message = false -> },
                            map : { long amount -> return "§e${NumberUtils.format(amount)} §citem${Math.abs(amount) == 1 ? "" : "s"}" }
                    ]
            ])
        }

        COINFLIPS = Persistent.of("coinflips", new ConcurrentHashMap<UUID, Coinflip>()).get()
        PLAYER_COINFLIPS = Persistent.of("player_coinflips", new HashMap<UUID, Set<UUID>>()).get()
        PRIVATE_COINFLIPS = Persistent.of("private_coinflips", new ConcurrentHashMap<UUID, Coinflip>()).get()

        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS coinflips (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, uuid_owner_least BIGINT NOT NULL, uuid_owner_most BIGINT NOT NULL, currency VARCHAR(32) NOT NULL, amount BIGINT NOT NULL, color VARCHAR(32) NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, server_id))")
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS private_coinflips (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, uuid_owner_least BIGINT NOT NULL, uuid_owner_most BIGINT NOT NULL, uuid_opponent_least BIGINT NOT NULL, uuid_opponent_most BIGINT NOT NULL, currency VARCHAR(32) NOT NULL, amount BIGINT NOT NULL, color VARCHAR(32) NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, server_id))")
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS coinflip_stats (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, wins INT NOT NULL, losses INT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, server_id))")
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS coinflip_stats_value (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, currency VARCHAR(32) NOT NULL, wins BIGINT NOT NULL, losses BIGINT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, currency, server_id))")
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS coinflip_win_queue (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, currency VARCHAR(32) NOT NULL, amount BIGINT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, currency, server_id))")
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS coinflip_items (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, item TEXT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, server_id))")

        //for some reason it will cause issues if the coinflips are not reinitialized
        Map<UUID, Coinflip> cfs = new HashMap<UUID, Coinflip>(COINFLIPS)
        Map<UUID, Coinflip> privateCfs = new HashMap<UUID, Coinflip>(PRIVATE_COINFLIPS)
        COINFLIPS.clear()
        PRIVATE_COINFLIPS.clear()
        cfs.each { COINFLIPS.put(it.key, new Coinflip(it.value)) }
        privateCfs.each { PRIVATE_COINFLIPS.put(it.key, new Coinflip(it.value)) }

        if (loaded) {
            return
        }

        ThreadUtils.runAsync {
            Logger logger = Starlight.plugin.getLogger()
            logger.info("Loading coinflips...")

            MySQL.getSyncDatabase().executeQuery("SELECT * FROM coinflips WHERE ${DatabaseUtils.getServerIdExpression()}", { statement -> }, { result ->
                while (result.next()) {
                    UUID id = new UUID(result.getLong(2), result.getLong(1))
                    UUID owner = new UUID(result.getLong(4), result.getLong(3))

                    String currency = result.getString(5)
                    long amount = result.getLong(6)
                    String color = result.getString(7)

                    Coinflip coinflip = new Coinflip(id, owner, currency, amount, color)
                    register(coinflip, false)
                }
            })

            logger.info("Loaded coinflips!")
            logger.info("Loading private coinflips...")

            MySQL.getSyncDatabase().executeQuery("SELECT * FROM private_coinflips WHERE ${DatabaseUtils.getServerIdExpression()}", { statement -> }, { result ->
                while (result.next()) {
                    UUID id = new UUID(result.getLong(2), result.getLong(1))
                    UUID owner = new UUID(result.getLong(4), result.getLong(3))
                    UUID opponent = new UUID(result.getLong(6), result.getLong(5))

                    String currency = result.getString(7)
                    long amount = result.getLong(8)
                    String color = result.getString(9)

                    Coinflip coinflip = new Coinflip(id, owner, currency, amount, color, opponent)
                    register(coinflip, false)
                }
            })

            logger.info("Loaded private coinflips!")
            logger.info("Loading coinflip items...")

            MySQL.getSyncDatabase().executeQuery("SELECT * FROM coinflip_items WHERE ${DatabaseUtils.getServerIdExpression()}", { statement -> }, { result ->
                while (result.next()) {
                    UUID id = new UUID(result.getLong(2), result.getLong(1))
                    Coinflip coinflip = getCoinflip(id)
                    if (!coinflip) {
                        deleteItem(id)
                        return
                    }
                    ItemStack item = FastItemUtils.convertStringToItemStack(result.getString(3))
                    coinflip.setItem(item)
                }
            })

            logger.info("Loaded coinflip items!")
        }
    }

    static void register(Coinflip coinflip, boolean updateDatabase = true) {
        if (coinflip.isPrivate()) {
            registerPrivateCoinflip(coinflip, updateDatabase)
            return // needs return cause different from normal coinflips.
        }

        UUID owner = coinflip.getOwner()
        UUID id = coinflip.getId()

        COINFLIPS.put(coinflip.getId(), coinflip)
        PLAYER_COINFLIPS.computeIfAbsent(owner, { k -> new HashSet<>() }).add(id)

        if (!updateDatabase) {
            return
        }

        if (coinflip.isItem()) {
            registerItemCoinflip(coinflip)
        }

        MySQL.getAsyncDatabase().execute("INSERT INTO coinflips (uuid_least, uuid_most, uuid_owner_least, uuid_owner_most, currency, amount, color, server_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", { statement ->
            statement.setLong(1, id.leastSignificantBits)
            statement.setLong(2, id.mostSignificantBits)
            statement.setLong(3, owner.leastSignificantBits)
            statement.setLong(4, owner.mostSignificantBits)
            statement.setString(5, coinflip.getCurrency())
            statement.setLong(6, coinflip.getAmount())
            statement.setString(7, coinflip.getColor())
            statement.setString(8, Temple.templeId)
        })
    }

    static void registerPrivateCoinflip(Coinflip coinflip, boolean updateDatabase) {
        UUID id = coinflip.getId()
        UUID owner = coinflip.getOwner()
        UUID opponent = coinflip.getOpponent()

        PRIVATE_COINFLIPS.put(coinflip.getId(), coinflip)
        PLAYER_COINFLIPS.computeIfAbsent(owner, { k -> new HashSet<>() }).add(id)

        if (!updateDatabase) {
            return
        }

        if (coinflip.isItem()) {
            registerItemCoinflip(coinflip)
        }

        MySQL.getAsyncDatabase().execute("INSERT INTO private_coinflips (uuid_least, uuid_most, uuid_owner_least, uuid_owner_most, uuid_opponent_least, uuid_opponent_most, currency, amount, color, server_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", { statement ->
            statement.setLong(1, id.leastSignificantBits)
            statement.setLong(2, id.mostSignificantBits)
            statement.setLong(3, owner.leastSignificantBits)
            statement.setLong(4, owner.mostSignificantBits)
            statement.setLong(5, opponent.leastSignificantBits)
            statement.setLong(6, opponent.mostSignificantBits)
            statement.setString(7, coinflip.getCurrency())
            statement.setLong(8, coinflip.getAmount())
            statement.setString(9, coinflip.getColor())
            statement.setString(10, Temple.templeId)
        })
    }

    static void registerItemCoinflip(Coinflip coinflip) {
        UUID id = coinflip.getId()
        ItemStack item = coinflip.getItem()
        String itemString = FastItemUtils.convertItemStackToString(item)
        println "INSERTING INTO COINFLIP ITEMS => ITEM => ${item.toString()}"
        MySQL.getAsyncDatabase().execute("INSERT INTO coinflip_items (uuid_least, uuid_most, item, server_id) VALUES(?, ?, ?, ?)", { statement ->
            statement.setLong(1, id.leastSignificantBits)
            statement.setLong(2, id.mostSignificantBits)
            statement.setString(3, itemString)
            statement.setString(4, Temple.templeId)
        })
    }

    static void addQueue(UUID owner, Coinflip coinflip) {
        MySQL.getAsyncDatabase().execute("INSERT INTO coinflip_win_queue (uuid_least, uuid_most, currency, amount, server_id) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount)", { statement ->
            statement.setLong(1, owner.leastSignificantBits)
            statement.setLong(2, owner.mostSignificantBits)
            statement.setString(3, coinflip.getCurrency())
            statement.setLong(4, coinflip.getAmount())
            statement.setString(5, Temple.templeId)
        })
    }

    static void deleteQueue(Player player) {
        MySQL.getAsyncDatabase().execute("DELETE FROM coinflip_win_queue WHERE ${DatabaseUtils.getServerUserExpression(player)}")
    }

    static Set<UUID> getCoinflips(UUID owner) {
        return PLAYER_COINFLIPS.getOrDefault(owner, Collections.<UUID>emptySet())
    }

    static void delete(Coinflip coinflip, boolean played = true) {
        UUID id = coinflip.getId()

        int index

        if (coinflip.isPrivate()) {
            PRIVATE_COINFLIPS.remove(coinflip.getId())
        } else {
            COINFLIPS.remove(coinflip.getId())
        }

        UUID owner = coinflip.getOwner()
        getCoinflips(owner).remove(id)

        if (coinflip.isPrivate()) {
            MySQL.getAsyncDatabase().execute("DELETE FROM private_coinflips WHERE ${DatabaseUtils.getServerUserExpression(id)}")
        } else {
            MySQL.getAsyncDatabase().execute("DELETE FROM coinflips WHERE ${DatabaseUtils.getServerUserExpression(id)}")
        }

        if (coinflip.isItem()) deleteItem(coinflip)

        if (!played) {
            Map<String, Object> data = COINFLIP.get(coinflip.getCurrency())
            Player player = Bukkit.getPlayer(owner)
            if (coinflip.isItem()) {
                FastInventoryUtils.addOrBox(owner, player, Bukkit.getConsoleSender(), coinflip.getItem(), "§6§lCOINFLIP §> §fItem has been added to your inventory or box!")
                return
            } else if (data.get("special") != null) {
                Map<String, Closure> specialCurrency = data.get("special") as Map<String, Closure>
                if (player) {
                    specialCurrency.get("add").call(player, coinflip.getAmount(), true)
                } else addQueue(owner, coinflip)
                return
            }
            CurrencyUtils.get(coinflip.getCurrency()).add(owner, coinflip.getAmount(), null, true)
        }
    }

    static void deleteItem(Coinflip coinflip) {
        deleteItem(coinflip.getId())
    }

    static void deleteItem(UUID id) {
        MySQL.getAsyncDatabase().execute("DELETE FROM coinflip_items WHERE ${DatabaseUtils.getServerUserExpression(id)}")
    }

    static Coinflip getCoinflip(UUID id) {
        return COINFLIPS.get(id) ?: PRIVATE_COINFLIPS.get(id)
    }

    //Only for global coinflips
    static void sort(List<Coinflip> coinflips) {
        coinflips.sort { coinflip, coinflip1 ->
            Map<String, Object> data = COINFLIP.get(coinflip.getCurrency())
            Map<String, Object> data1 = COINFLIP.get(coinflip1.getCurrency())
            return 1 * (coinflip.getAmount() <=> coinflip.getAmount()) + 2 * ((data1.get("priority") as Integer) <=> (data.get("priority") as Integer))
        }
    }

    static void clear() {
        COINFLIPS.clear()
        PRIVATE_COINFLIPS.clear()
        PLAYER_COINFLIPS.clear()
        MySQL.getAsyncDatabase().execute("DELETE FROM coinflips WHERE ${DatabaseUtils.getServerIdExpression()}")
        MySQL.getAsyncDatabase().execute("DELETE FROM private_coinflips WHERE ${DatabaseUtils.getServerIdExpression()}")
        MySQL.getAsyncDatabase().execute("DELETE FROM coinflip_stats WHERE ${DatabaseUtils.getServerIdExpression()}")
        MySQL.getAsyncDatabase().execute("DELETE FROM coinflip_stats_value WHERE ${DatabaseUtils.getServerIdExpression()}")
        MySQL.getAsyncDatabase().execute("DELETE FROM coinflip_win_queue WHERE ${DatabaseUtils.getServerIdExpression()}")
        MySQL.getAsyncDatabase().execute("DELETE FROM coinflip_items WHERE ${DatabaseUtils.getServerIdExpression()}")
    }

    //TODO should kinda have its own module

//    static NamespacedKey ROBOT_KEY = new NamespacedKey(Starlight.plugin, "robotv2")
//    static NamespacedKey CRATE_TYPE = new NamespacedKey(Starlight.plugin, "crate_type")
//    static NamespacedKey SHINYBOX_KEY = new NamespacedKey(Starlight.plugin, "shinybox")
//    static NamespacedKey TREASURE_TYPE_KEY = new NamespacedKey(Starlight.plugin, "treasure_type")
//    static NamespacedKey BOOSTER_TYPE_KEY = new NamespacedKey(Starlight.plugin, "booster_type")
//    static NamespacedKey BOOSTER_USES_KEY = new NamespacedKey(Starlight.plugin, "booster_uses")
//    static NamespacedKey BOOSTER_TIME_KEY = new NamespacedKey(Starlight.plugin, "booster_time")
//    static NamespacedKey BOOSTER_MULTIPLIER = new NamespacedKey(Starlight.plugin, "booster_multiplier")
//    static NamespacedKey GEM_OF_BRIGHTNESS_KEY = new NamespacedKey(Starlight.plugin, "new_enchantment_gem_of_brightness")
//    static NamespacedKey DARK_SCROLL_KEY = new NamespacedKey(Starlight.plugin, "new_dark_scroll")
//    static NamespacedKey QUARRIES_GEMSTONE_KEY = new NamespacedKey(Starlight.plugin, "quarry_gemstone")
//    static NamespacedKey SKINS_KEY = new NamespacedKey(Starlight.plugin, "omnitoolskins_skinId")
//    static NamespacedKey SKINS_KEY_V2 = new NamespacedKey(Starlight.plugin, "itemskins:withdrawnskin")

    static boolean isSpecialItem(ItemStack item) {
        if (item == null) return false
//        if (FastItemUtils.hasCustomTag(item, ROBOT_KEY, ItemTagType.STRING)) {
//            return true
//        }
//        if (FastItemUtils.hasCustomTag(item, CRATE_TYPE, ItemTagType.STRING)) {
//            return true
//        }
//        if (FastItemUtils.hasCustomTag(item, SHINYBOX_KEY, ItemTagType.BYTE)) {
//            return true
//        }
//        if (FastItemUtils.hasCustomTag(item, TREASURE_TYPE_KEY, ItemTagType.STRING)) {
//            return true
//        }
//        if (FastItemUtils.hasCustomTag(item, BOOSTER_TYPE_KEY, ItemTagType.STRING)) {
//            return true
//        }
//        if (FastItemUtils.hasCustomTag(item, GEM_OF_BRIGHTNESS_KEY, ItemTagType.BYTE) && item.type == Material.EMERALD) {
//            return true
//        }
//        if (FastItemUtils.hasCustomTag(item, DARK_SCROLL_KEY, ItemTagType.BYTE)) {
//            return true
//        }
//        if (FastItemUtils.hasTagFast(item, QUARRIES_GEMSTONE_KEY)) {
//            return true
//        }
//        if (FastItemUtils.hasCustomTag(item, SKINS_KEY, ItemTagType.STRING)) {
//            return true
//        }
//        if (FastItemUtils.hasCustomTag(item, SKINS_KEY_V2, ItemTagType.STRING)) {
//            return true
//        }
//        if ((Exports.ptr("drills:isDrillItemStack") as Closure<Boolean>)?.call(item)) {
//            return true
//        }
        return true
    }

    static boolean compareItems(ItemStack player1, ItemStack player2) {
        if (player1 == null || player2 == null) return false

//        // robots
//        if ((Exports.ptr("robots:areSimilarRobotItems") as Closure<Boolean>)?.call(player1, player2)) return true

//        // crate keys
//        if (FastItemUtils.hasCustomTag(player1, CRATE_TYPE, ItemTagType.STRING) && FastItemUtils.hasCustomTag(player2, CRATE_TYPE, ItemTagType.STRING)) {
//            if (FastItemUtils.getCustomTag(player1, CRATE_TYPE, ItemTagType.STRING) == FastItemUtils.getCustomTag(player2, CRATE_TYPE, ItemTagType.STRING)) {
//                return true
//            }
//        }
//
//        // shiny boxes
//        if (FastItemUtils.hasCustomTag(player1, SHINYBOX_KEY, ItemTagType.BYTE) && FastItemUtils.hasCustomTag(player2, SHINYBOX_KEY, ItemTagType.BYTE)) {
//            return true
//        }
//
//        // shiny treasures
//        if (FastItemUtils.hasCustomTag(player1, TREASURE_TYPE_KEY, ItemTagType.STRING) && FastItemUtils.hasCustomTag(player2, TREASURE_TYPE_KEY, ItemTagType.STRING)) {
//            if (FastItemUtils.getCustomTag(player1, TREASURE_TYPE_KEY, ItemTagType.STRING) == FastItemUtils.getCustomTag(player2, TREASURE_TYPE_KEY, ItemTagType.STRING)) {
//                return true
//            }
//        }
//
//        //boosters
//        if (FastItemUtils.hasCustomTag(player1, BOOSTER_TYPE_KEY, ItemTagType.STRING) && FastItemUtils.hasCustomTag(player1, BOOSTER_MULTIPLIER, ItemTagType.DOUBLE) && FastItemUtils.hasCustomTag(player2, BOOSTER_TYPE_KEY, ItemTagType.STRING) && FastItemUtils.hasCustomTag(player2, BOOSTER_MULTIPLIER, ItemTagType.DOUBLE)) {
//            if (FastItemUtils.getCustomTag(player1, BOOSTER_TYPE_KEY, ItemTagType.STRING) == FastItemUtils.getCustomTag(player2, BOOSTER_TYPE_KEY, ItemTagType.STRING) && FastItemUtils.getCustomTag(player1, BOOSTER_MULTIPLIER, ItemTagType.DOUBLE) == FastItemUtils.getCustomTag(player2, BOOSTER_MULTIPLIER, ItemTagType.DOUBLE)) {
//                //same uses
//                if (FastItemUtils.hasCustomTag(player1, BOOSTER_USES_KEY, ItemTagType.INTEGER) && FastItemUtils.hasCustomTag(player2, BOOSTER_USES_KEY, ItemTagType.INTEGER)) {
//                    if (FastItemUtils.getCustomTag(player1, BOOSTER_USES_KEY, ItemTagType.INTEGER) > 0 && FastItemUtils.getCustomTag(player1, BOOSTER_USES_KEY, ItemTagType.INTEGER) == FastItemUtils.getCustomTag(player2, BOOSTER_USES_KEY, ItemTagType.INTEGER)) {
//                        return true
//                    }
//                }
//
//                //same time
//                if (FastItemUtils.hasCustomTag(player1, BOOSTER_TIME_KEY, ItemTagType.INTEGER) && FastItemUtils.hasCustomTag(player2, BOOSTER_TIME_KEY, ItemTagType.INTEGER)) {
//                    if (FastItemUtils.getCustomTag(player1, BOOSTER_TIME_KEY, ItemTagType.INTEGER) > 0 && FastItemUtils.getCustomTag(player1, BOOSTER_TIME_KEY, ItemTagType.INTEGER) == FastItemUtils.getCustomTag(player2, BOOSTER_TIME_KEY, ItemTagType.INTEGER)) {
//                        return true
//                    }
//                }
//            }
//            return false
//        }
//
//        // gem of brightness
//        if (FastItemUtils.hasCustomTag(player1, GEM_OF_BRIGHTNESS_KEY, ItemTagType.BYTE) && FastItemUtils.hasCustomTag(player2, GEM_OF_BRIGHTNESS_KEY, ItemTagType.BYTE) && player2.type == Material.EMERALD && player1.type == Material.EMERALD) {
//            return true
//        }
//
//        // dark scrolls
//        if (FastItemUtils.hasCustomTag(player1, DARK_SCROLL_KEY, ItemTagType.BYTE) && FastItemUtils.hasCustomTag(player2, DARK_SCROLL_KEY, ItemTagType.BYTE)) {
//            return true
//        }
//
//        //quarry gemstones
//        if (FastItemUtils.hasTagFast(player1, QUARRIES_GEMSTONE_KEY) && FastItemUtils.hasTagFast(player2, QUARRIES_GEMSTONE_KEY)) {
//            Closure<Boolean> testQuarryGemstones = Exports.ptr("quarries:comparegemstones") as Closure<Boolean>
//            if (testQuarryGemstones != null && testQuarryGemstones.call(player1, player2)) {
//                return true
//            }
//        }
//
//        //omni skins
//        if (FastItemUtils.hasCustomTag(player1, SKINS_KEY, ItemTagType.STRING) && FastItemUtils.hasCustomTag(player2, SKINS_KEY, ItemTagType.STRING)) {
//            return FastItemUtils.getCustomTag(player1, SKINS_KEY, ItemTagType.STRING) == FastItemUtils.getCustomTag(player2, SKINS_KEY, ItemTagType.STRING)
//        }
//
//        if ((Exports.ptr("itemskins:areSameWithdrawnSkin") as Closure<Boolean>)?.call(player1, player2)) {
//            return true
//        }
//
//        if ((Exports.ptr("drills:areDrillItemsComparable") as Closure<Boolean>)?.call(player1, player2)) {
//            return true
//        }

        return false
    }
}

@CompileStatic
class Coinflip {

    UUID id
    UUID owner
    String currency
    Long amount
    String color
    UUID opponent
    ItemStack item
    ItemStack opponentItem


    Coinflip(UUID id, UUID owner = null, String currency = null, Long amount = null, String color = null, UUID opponent = null, ItemStack item = null, ItemStack opponentItem = null) {
        this.id = id
        this.owner = owner
        this.currency = currency
        this.amount = amount
        this.color = color
        this.opponent = opponent
        this.item = item
        this.opponentItem = opponentItem
    }

    Coinflip(Object coinflip) {
        this.id = coinflip["id"] as UUID ?: null
        this.owner = coinflip["owner"] as UUID ?: null
        this.currency = coinflip["currency"] as String ?: null
        this.amount = coinflip["amount"] as Long ?: null
        this.color = coinflip["color"] as String ?: null
        this.opponent = coinflip["opponent"] as UUID ?: null
        this.item = coinflip["item"] as ItemStack ?: null
        this.opponentItem = coinflip["opponentItem"] as ItemStack ?: null
    }


    boolean isPrivate() {
        return opponent != null && opponent != owner
    }

    boolean isItem() {
        return item != null || currency == "items"
    }

    boolean isValidCoinflip() {
        return id != null && owner != null && currency != null && amount != null && color != null
    }
}