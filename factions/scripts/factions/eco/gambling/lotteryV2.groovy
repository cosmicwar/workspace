package scripts.factions.eco.gambling

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import groovy.transform.CompileStatic
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.ToggleUtils
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.IntegerUtils
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.legacy.utils.SignUtils
import scripts.shared.legacy.utils.ThreadUtils
import scripts.shared.legacy.utils.TimeUtils
import scripts.shared.systems.Bedrock
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.MenuDecorator
import scripts.shared.utils.Persistent
import scripts.shared.utils.Temple

import java.text.Format
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer
import java.util.logging.Logger

LotteryUtils2.init()

class LotteryUtils2 {

    static Format formatter = new SimpleDateFormat("HH:mm")
    static final ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/lottery"))
    static final NamespacedKey LOTTERY_KEY = new NamespacedKey(Starlight.plugin, "lotterykey")
    static final NumberFormat DOUBLE_FORMAT = NumberFormat.getNumberInstance(Locale.US)

    static ArrayList<Long> BROADCAST_TIMES_LEFT = [
            (long) 1 * 20,
            (long) 2 * 20,
            (long) 3 * 20,
            (long) 10 * 20,
            (long) 30 * 20,
            (long) 60 * 20,
            (long) 5 * 60 * 20,
            (long) 10 * 60 * 20,
            (long) 20 * 60 * 20,
            (long) 30 * 60 * 20,
            (long) 45 * 60 * 20,
            (long) 60 * 60 * 20
    ]

    static Map<String, Map<String, Object>> config = [
        // don't use menuSlot 13 as its for information.
//        BEACON: [
//                id: 1,
//                name: "Beacons",
//                displayName: "§bBeacons",
//                duration: 60,
//                ticketPrice: 500,
//                ticketLimit: 250000,
//                currencyKey: "beacons",
//                menuSlot: 11,
//                menuItem: FastItemUtils.createItem(Material.DIAMOND, "§b§lBeacons", []),
//                times: ["14:10"]
//        ],
        MONEY: [
                id: 1,
                name: "Money",
                displayName: "§dMoney",
                duration: 60,
                ticketPrice: 500,
                ticketLimit: 250000,
                currencyKey: "money",
                menuSlot: 15,
                menuItem: FastItemUtils.createItem(Material.GOLD_INGOT, "§d§lMoney", []),
                times: [] // "1:00", "3:00", "5:00", "7:00", "9:00", "11:00", "13:00", "15:00", "17:00", "19:00", "21:00", "23:00"
        ]
    ]

    static Map<Integer, Lottery> ACTIVE_LOTTERIES
    static Map<String, LotteryType> LOTTERY_TYPES = Maps.<String, LotteryType> newConcurrentMap()
    static Set<UUID> UPDATE_QUEUE = Sets.<UUID> newConcurrentHashSet()
    static long TIME_BETWEEN = 30 * 60 * 1000 // milliseconds
    static AtomicLong LAST_LOTTERY_STARTED

    static void init() {
        boolean isLoaded = Persistent.persistentMap.containsKey("lottery/active")
        ACTIVE_LOTTERIES = Persistent.of("lottery/active", Maps.<Integer, Lottery> newConcurrentMap()).get()
        LAST_LOTTERY_STARTED = Persistent.of("lottery/last_started", new AtomicLong(0L)).get()

        for (Map.Entry<String, Map<String, Object>> entry : config) {
            int id = entry.value.get("id") as int
            String name = entry.value.get("name") as String
            String displayName = entry.value.get("displayName") as String
            int duration = entry.value.get("duration") as int
            long durationInTicks = duration * 60 * 20
            int ticketPrice = entry.value.get("ticketPrice") as int
            int ticketLimit = entry.value.get("ticketLimit") as int
            String currencyKey = entry.value.get("currencyKey") as String
            int menuSlot = entry.value.get("menuSlot") as int
            ItemStack menuItem = entry.value.get("menuItem") as ItemStack
            ArrayList<String> times = entry.value.get("times") as ArrayList<String>
            LOTTERY_TYPES.put(entry.key, new LotteryType(id, name, displayName, duration, durationInTicks, ticketPrice, ticketLimit, currencyKey, menuSlot, menuItem, times))
        }

        reInitiateCache()

        TIME_BETWEEN = (60 / LOTTERY_TYPES.size() * 60 * 1000).longValue()

        Schedulers.async().runRepeating({
            saveDatabase()
        }, 30 * 20L, 30 * 20L)

        GroovyScript.addUnloadHook {
            saveDatabase()
        }

        Schedulers.async().runRepeating({
            if (System.currentTimeMillis() - LAST_LOTTERY_STARTED.get() < TIME_BETWEEN) return
            Collection types = LOTTERY_TYPES.values().findAll { !ACTIVE_LOTTERIES.containsKey(it.id) }
            LotteryType type = types.isEmpty() ? null : types.first()
            if (type) {
                startLottery(new Lottery(type, type.durationInTicks))
            }
        }, 20L, 20L)

        Schedulers.sync().runRepeating({
            ACTIVE_LOTTERIES.each {  entry ->
                Lottery lottery = entry.value
                if (lottery.ticksLeft <= 0L) {
                    drawLottery(lottery)
                }
                if (BROADCAST_TIMES_LEFT.contains(lottery.ticksLeft)) {
                    announce(["§3§lLOTTERY §f) §> §fDrawing in §e${TimeUtils.getTimeAmount(lottery.ticksLeft.intdiv(20) * 1000L)}§f! §7§o(/lottery)\n",
                              "§3§lLOTTERY §f) §> §fThe pot is currently §e${(Exports.ptr(lottery.type.currencyKey) as CurrencyStorage)?.map(lottery.calcPot())}§f!"])
                }
                lottery.tick()
            }
        }, 1L, 1L)

        Commands.create().assertPlayer().handler { c ->
            showLotteryMenu(c.sender())
        }.register("lottery", "lotto")

        Commands.create().assertOp().handler { c ->
            c.reply(formatter.format(new Date()))
        }.register("dev/getformattedtime")

        Commands.create().assertOp().handler { c ->
            ACTIVE_LOTTERIES.clear()
            c.reply("§cDone.")
        }.register("dev/lottery/clear")

        Commands.create().assertOp().handler { c ->
            c.reply("§c${ACTIVE_LOTTERIES.size()} lotteries active.")
        }.register("dev/lottery/active")

        Commands.create().assertOp().handler { c ->
            for (Map.Entry<Integer, Lottery> entry : ACTIVE_LOTTERIES) {
                c.reply("§c${entry.value.type.name} : ${entry.value.ticksLeft}")
            }
        }.register("dev/lottery/ticksleft")

        Commands.create().assertPlayer().assertUsage("<id>").assertOp().handler { c ->
            int id = c.arg(0).parseOrFail(Integer)
            LotteryType type = LOTTERY_TYPES.values().find { t -> t.id == id}
            if (!type) return c.reply("§cCan't find that lotteryType.")
            Lottery lottery = new Lottery(type, type.durationInTicks)
            startLottery(lottery)
        }.register("dev/lottery/start")

        Commands.create().assertUsage("<ticks>").assertOp().handler { c ->
            ACTIVE_LOTTERIES.values().first().setTicksLeft(c.arg(0).parseOrFail(Long))
        }.register("dev/lottery/setticks")

        Commands.create().assertUsage("<lotteryId>").assertOp().handler { c ->
            int id = c.arg(0).parseOrFail(Integer)
            if (!ACTIVE_LOTTERIES.containsKey(id)) {
                return c.reply("§cLottery is not active.")
            }
            drawLottery(ACTIVE_LOTTERIES.get(id))
            c.reply("§cDone.")
        }.register("dev/lottery/draw")

        Commands.create().assertPlayer().assertUsage("<lotteryId> <amount>").assertOp().handler { c ->
            int id = c.arg(0).parseOrFail(Integer)
            int amount = c.arg(1).parseOrFail(Integer)
            if (!ACTIVE_LOTTERIES.containsKey(id)) {
                return c.reply("§cLottery is not active.")
            }

            Lottery lottery = ACTIVE_LOTTERIES.get(id)
            lottery.addTicket(c.sender().uniqueId, amount)
            c.reply("§cDone.")
        }.register("dev/lottery/addtickets")

        if (!isLoaded) {
            initDatabase()
        }
    }

    static void initDatabase() {
        ThreadUtils.runAsync {
            Logger logger = Starlight.plugin.getLogger()
            logger.info("Loading lottery from database...")

            MySQL.getSyncDatabase().execute("CREATE TABLE IF NOT EXISTS lottery_entries_v3 (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, lottery_id INT NOT NULL, ticketcount INT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, lottery_id, server_id))")
            MySQL.getSyncDatabase().execute("CREATE TABLE IF NOT EXISTS lottery_ticks_until_draw_v2 (ticks BIGINT NOT NULL, lottery_id INT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(lottery_id, server_id))")
//            MySQL.getSyncDatabase().execute("CREATE TABLE IF NOT EXISTS lottery_pot_v2 (pot DECIMAL(65) NOT NULL, lottery_id INT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(lottery_id, server_id))")

            MySQL.getSyncDatabase().executeQuery("SELECT * FROM lottery_ticks_until_draw_v2 WHERE server_id = ?", { statement ->
                statement.setString(1, Temple.templeId)
            }, { result ->
                while (result.next()) {
                    LotteryType type = getLotteryTypeFromId(result.getInt(2))
                    if (type != null) {
                        ACTIVE_LOTTERIES.put(type.id, new Lottery(type, result.getLong(1)))
                    }
                }
            })

            MySQL.getSyncDatabase().executeQuery("SELECT * FROM lottery_entries_v3 WHERE server_id = ?", { statement ->
                statement.setString(1, Temple.templeId)
            }, { result ->
                while (result.next()) {
                    ACTIVE_LOTTERIES.get(result.getInt(3))?.addTicket(new UUID(result.getLong(2), result.getLong(1)), result.getInt(4))
                }
            })
            logger.info("Loaded lottery from database!")
        }
    }

    static void reInitiateCache() {
        Map<Integer, Object> old = new HashMap<Integer, Object>(ACTIVE_LOTTERIES)
        ACTIVE_LOTTERIES.clear()

        for (Map.Entry<Integer, Object> entry : old) {
            def type = entry.value["type"]
            LotteryType lotteryType = LOTTERY_TYPES.values().find {it.id == type["id"] }
            if (!lotteryType) continue
            long ticksLeft = entry.value["ticksLeft"] as long
            long ticketAmount = entry.value["ticketAmount"] as long
            HashMap<UUID, Integer> tickets = entry.value["tickets"] as HashMap<UUID, Integer>
            ACTIVE_LOTTERIES.put(lotteryType.id, new Lottery(lotteryType, ticksLeft, ticketAmount, tickets))
        }

    }

    static void startLottery(Lottery lottery) {
        LAST_LOTTERY_STARTED.set(System.currentTimeMillis())
        ACTIVE_LOTTERIES.put(lottery.type.id, lottery)
    }

    static void showLotteryMenu(Player player) {
        MenuBuilder menuBuilder

        menuBuilder = new MenuBuilder(3 * 9, "§c§lLottery")
        MenuDecorator.decorate(menuBuilder, [
                "4b4b4b4b4",
                "b4b4b4b4b",
                "4b4b4b4b4"
        ])
        for (Lottery lottery : ACTIVE_LOTTERIES.values()) {
            LotteryType lotteryType = lottery.type
            ItemStack item = lotteryType.menuItem
            FastItemUtils.setLore(item, ["", "§9§nNext Draw", "§f${TimeUtils.getTimeAmount(lottery.ticksLeft.intValue().intdiv(20) * 1000L)}", "", "§e * §6Click to purchase tickets §e* "])
            menuBuilder.set(lotteryType.menuSlot, item, { p, t, s ->
                int lotteryTypeId = FastItemUtils.getCustomTag(menuBuilder.get().getItem(s), LOTTERY_KEY, ItemTagType.INTEGER)
                LotteryType type = getLotteryTypeFromId(lotteryTypeId)
                showLotteryTypeMenu(player, type)
            })
        }
        menuBuilder.set(13, FastItemUtils.createItem(Material.BOOK, "§8[§dInformation§8]", [
                "",
                "§7§oCompete with players by purchasing ",
                "§7§otickets for a chance to win the pool prize!",
        ]))

        player.openInventory(menuBuilder.get())
    }

    static void showLotteryTypeMenu(Player player, LotteryType type) {
        if (!type) {
            player.closeInventory()
            Players.msg(player, "§! §> Could not find this lottery.")
            return
        }
        MenuBuilder builder
        builder = new MenuBuilder(3 * 9, "§c§l${type.name} Lottery")
        Lottery lottery = ACTIVE_LOTTERIES.values().find { it.type.id == type.id }
        if (!lottery) {
            player.closeInventory()
            Players.msg(player, "§! §> §fLottery is no longer active.")
            return
        }

        builder.set(12, FastItemUtils.createItem(Material.PAPER, "§aTickets", [
                "",
                "§9Your Amount",
                "§e${NumberUtils.format(lottery.getTicketsUser(player.uniqueId))} §fTickets",
                "",
                "§9Total Amount",
                "§e${NumberUtils.format(lottery.getTotalTickets())} §fTickets",
                "",
                "§9Ticket Price",
                "§e${NumberUtils.format(type.ticketPrice)} ${type.displayName}",
                "",
                "§e * §6Click to purchase tickets §e* "

        ]), { p, t, s ->
            Consumer<String> consumer = { String input ->
                IntegerUtils.IntegerParseResult result = IntegerUtils.parseInt(input)
                if (!result.isPositive()) {
                    Players.msg(player, "§! §> §e${input} §fis not a valid amount!")
                    return
                }
                int ticketAmount = result.getValue()
                long price = ticketAmount * lottery.type.ticketPrice
                if (lottery.getTicketsUser(p.uniqueId) + ticketAmount > lottery.type.ticketLimit) {
                    Players.msg(player, "§! §> §cYou may not put more than ${lottery.type.ticketLimit} tickets into the pot.")
                    return
                }

                (Exports.ptr(lottery.type.currencyKey) as CurrencyStorage).take(player, price, {
                    UUID uuid = player.getUniqueId()
                    lottery.addTicket(uuid, ticketAmount)
                    UPDATE_QUEUE.add(uuid)
                    Players.msg(player, "§c§lLOTTERY §f(${lottery.type.displayName}§f) §> §fSuccessfully purchased §e${NumberUtils.format(ticketAmount)} §fticket${ticketAmount == 1 ? "" : "s"}!")
                })
                showLotteryTypeMenu(player, type)
            }

            if (Bedrock.isBedrockPlayer(player)) {
                new Bedrock.CustomGui("Ticking Quantity", { Map<String, Object> responses ->
                    consumer.accept(responses.get("quantity") as String)
                })
                        .textInput("quantity", "Enter Quantity", "1")
                        .open(player)
            } else {
                SignUtils.openSign(player, ["", "^ ^ ^", "§aTicket Quantity"], { lines, p1 ->
                    consumer.accept(lines[0])
                })
            }
        })
        builder.set(14, FastItemUtils.createItem(Material.CHEST, "§6Price Pool", [
                "",
                "§9Lottery Pot",
                "§e${NumberUtils.format(lottery.calcPot())} ${lottery.type.displayName}",
                "",
                "§9Your Odds",
                "§a${DOUBLE_FORMAT.format(Math.sqrt(((100.0D * lottery.getTicketsUser(player.uniqueId)) / (lottery.getTotalTickets() == 0 ? 1 : lottery.getTotalTickets())) / 10) * 10)}%"
        ]))

        builder.set(19, FastItemUtils.createItem(Material.PAPER, "§cBack", []), { p, t, s ->
            showLotteryMenu(p)
        })

        builder.open(player)
    }

    static void announce(ArrayList<String> messages, boolean addClickHereFunction = true) {
        println messages.join("\n")
        if (addClickHereFunction) {
            announceWithClickFunction(messages)
            return
        }
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (ToggleUtils.hasToggled(target, "lottery_notifications")) continue
            for (String message : messages) {
                Players.msg(target, message)
            }
        }
    }

    static void announce(String message, boolean addClickHereFunction = true) {
        announce([message], addClickHereFunction)
    }

    static void announceWithClickFunction(ArrayList<String> msgs) {
        ArrayList<TextComponent> messages = new ArrayList<TextComponent>()
        for (String msg : msgs) {
            TextComponent comp = new TextComponent()
            comp.setText(msg)
            comp.setClickEvent(clickEvent)
            messages.add(comp)
        }
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (ToggleUtils.hasToggled(target, "lottery_notifications")) continue
            for (TextComponent message : messages) {
                target.sendMessage(message)
            }
        }
    }

    static LotteryType getLotteryTypeFromId(int id) {
        return LOTTERY_TYPES.values().find { it.id == id }
    }

    static void reset(int lotteryId) {
        LotteryType type = getLotteryTypeFromId(lotteryId)
        ACTIVE_LOTTERIES.remove(lotteryId)
        resetDatabase(lotteryId, {
            startLottery(new Lottery(type, type.durationInTicks))
        })
    }

    static void resetDatabase(int lotteryId, Runnable onSuccess = null) {
        MySQL.getAsyncDatabase().executeUpdate("DELETE lottery_entries_v3, lottery_ticks_until_draw_v2 FROM " +
                "lottery_entries_v3 INNER JOIN lottery_ticks_until_draw_v2 WHERE " +
                "lottery_entries_v3.lottery_id = lottery_ticks_until_draw_v2.lottery_id AND " +
                "lottery_entries_v3.server_id = lottery_ticks_until_draw_v2.server_id AND " +
                "lottery_entries_v3.lottery_id = ? AND " +
                "lottery_entries_v3.server_id = ?", { statement ->
            statement.setInt(1, lotteryId)
            statement.setString(2, Temple.templeId)
        }, { success ->
            if (onSuccess) {
                onSuccess.run()
            }
        })
    }

    static void saveDatabase() {
        Set<UUID> update = new HashSet<UUID>(UPDATE_QUEUE)
        UPDATE_QUEUE.clear()
        MySQL.getAsyncDatabase().executeBatch("INSERT INTO lottery_entries_v3 (uuid_least, uuid_most, lottery_id, ticketcount, server_id) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE ticketcount = VALUES(ticketcount)", { statement ->
            for (Map.Entry<Integer, Lottery> entry : ACTIVE_LOTTERIES) {
                for (UUID uuid : update) {
                    statement.setLong(1, uuid.leastSignificantBits)
                    statement.setLong(2, uuid.mostSignificantBits)
                    statement.setInt(3, entry.key)
                    statement.setInt(4, entry.value.getTicketsUser(uuid))
                    statement.setString(5, Temple.templeId)
                    statement.addBatch()
                }
            }
        })

        MySQL.getAsyncDatabase().executeBatch("INSERT INTO lottery_ticks_until_draw_v2 (ticks, lottery_id, server_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE ticks = VALUES(ticks)", { statement ->
            for (Map.Entry<Integer, Lottery> entry : ACTIVE_LOTTERIES) {
                statement.setLong(1, entry.value.ticksLeft)
                statement.setInt(2, entry.key)
                statement.setString(3, Temple.templeId)
                statement.addBatch()
            }
        })
    }

    static void drawLottery(Lottery lottery) {
        if (lottery.isDrawing()) return
        lottery.setDrawing()
        if (lottery.getTotalTickets() <= 0L || lottery.tickets.isEmpty()) {
            println "§c§lLOTTERY §f(${lottery.type.displayName}§f) §> §fThe lottery had no entries!"
            Schedulers.sync().run({
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (!ToggleUtils.hasToggled(target, "lottery_notifications")) {
                        Players.msg(target, "§c§lLOTTERY §f(${lottery.type.displayName}§f) §> §fThe lottery had no entries!")
                    }
                }
            })
            reset(lottery)
            return
        }

        UUID winner = RandomUtils.get(lottery.tickets.findAll {it.value > 0})
        BigDecimal reward = lottery.calcPot()
        int tickets = lottery.getTicketsUser(winner)

        println "[lottery] [debug] WINNER => ${winner.toString()}"

        DatabaseUtils.getLatestUsername(winner, { username ->
            CurrencyStorage currency = Exports.ptr(lottery.type.currencyKey) as CurrencyStorage
            long rounded = Math.round(reward * 0.75D) as long
            currency.add(winner, rounded, null, true, true)
            println "§c§lLOTTERY §f(${lottery.type.displayName}§f) §> §e${username} §fwon §e${NumberUtils.format(rounded)} §fwith §e${NumberUtils.format(tickets)} §fticket${tickets == 1 ? "" : "s"}!"
            Schedulers.sync().run({
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (!ToggleUtils.hasToggled(target, "lottery_notifications")) {
                        Players.msg(target, "§c§lLOTTERY §f(${lottery.type.displayName}§f) §> §e${username} §fwon §e${NumberUtils.format(rounded)} §fwith §e${NumberUtils.format(tickets)} §fticket${tickets == 1 ? "" : "s"}!")
                    }
                }
                reset(lottery)
            })
        })
    }

    static void reset(Lottery lottery) {
        Schedulers.async().run {
            ACTIVE_LOTTERIES.remove(lottery.type.id)
            MySQL.getSyncDatabase().execute("DELETE FROM lottery_entries_v3 WHERE lottery_id = ? AND server_id = ?", { statement ->
                statement.setInt(1, lottery.type.id)
                statement.setString(2, Temple.templeId)
            })
            startLottery(new Lottery(lottery.type, lottery.type.durationInTicks))

        }
    }

    @CompileStatic
    static class Lottery {
        LotteryType type
        long ticksLeft
        long ticketAmount
        Map<UUID, Integer> tickets
        boolean drawing

        Lottery(LotteryType type, long ticksLeft, long ticketAmount = 0L, HashMap<UUID, Integer> tickets = new HashMap<UUID, Integer>(), boolean drawing = false) {
            this.type = type
            this.ticksLeft = ticksLeft
            this.ticketAmount = ticketAmount
            this.tickets = tickets
            this.drawing = drawing
        }

        void tick() {
            this.ticksLeft -= 1L
        }

        void addTicket(UUID uuid, int amount) {
            tickets.put(uuid, tickets.getOrDefault(uuid, 0) + amount)
        }

        BigDecimal calcPot() {
            BigDecimal pot = new BigDecimal(0)
            for (Map.Entry<UUID, Integer> ticket : tickets) {
                pot += (ticket.value * type.ticketPrice)
            }
            return pot
        }

        int getTicketsUser(UUID uuid) {
            return tickets.getOrDefault(uuid, 0)
        }

        long getTotalTickets() {
            return tickets.values().sum() as Long ?: 0L
        }

        boolean setDrawing() {
            this.drawing = true
        }

        boolean isDrawing() {
            return this.drawing
        }
    }

    @CompileStatic
    static class LotteryType {
        int id
        String name
        String displayName
        int duration // in minutes
        long durationInTicks
        int ticketPrice
        int ticketLimit
        String currencyKey
        int menuSlot
        ItemStack menuItem
        ArrayList<String> times

        LotteryType(int id, String name, String displayName, int duration, long durationInTicks, int ticketPrice, int ticketLimit, String currencyKey, int menuSlot, ItemStack menuItem, ArrayList<String> times) {
            this.id = id
            this.name = name
            this.displayName = displayName
            this.duration = duration
            this.durationInTicks = durationInTicks
            this.ticketPrice = ticketPrice
            this.ticketLimit = ticketLimit
            this.currencyKey = currencyKey
            this.menuSlot = menuSlot
            this.menuItem = menuItem
            this.times = times

            FastItemUtils.setCustomTag(this.menuItem, LOTTERY_KEY, ItemTagType.INTEGER, this.id)
        }
    }
}