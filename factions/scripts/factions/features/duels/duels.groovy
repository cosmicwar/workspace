package scripts.factions.features.duels

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.time.Time
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.CurrencyUtils
import scripts.shared.legacy.ToggleUtils
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.objects.Pair
import scripts.shared.legacy.utils.*
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.ActionableItem
import scripts.shared.utils.MenuDecorator
import scripts.shared.utils.Temple

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

DuelUtils.init()

class DuelUtils {

    static long INVITE_EXPIRE = 60

    static int MAX_INVITES_PER_PLAYER = 3


    static Map<UUID, List<UUID>> PENDING_INVITATIONS
    static Map<UUID, Duel> DUELS_BY_ID

    static void init() {
        DUELS_BY_ID = new HashMap<>(50)
        PENDING_INVITATIONS = new HashMap<>(100)


        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS duels (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, uuid_owner_least BIGINT NOT NULL, uuid_owner_most BIGINT NOT NULL, uuid_opponent_least BIGINT NOT NULL, uuid_opponent_most BIGINT NOT NULL, betters TEXT, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, server_id))")
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS duels_stats (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, wins INT NOT NULL, losses INT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, server_id))")

        //TODO: Load duels from db
        Events.subscribe(PlayerQuitEvent.class, EventPriority.LOW).handler(e -> {
            Duel duel = getPlayerDuel(e.player)
            if (duel == null) {
                return
            }

            duel.eliminatePlayer(e.player)
            if (duel.started) {
                endDuel(duel, DuelEndCause.PLAYER_QUIT_IN_DUEL)
            } else {
                endDuel(duel, DuelEndCause.PLAYER_QUIT_BEFORE_DUEL)
            }

        })

        //todo: add event for handling "deaths"

        Commands.create().assertPlayer().handler(cmd -> {

            AtomicInteger rebootDelay = Exports.ptr("rebootDelay") as AtomicInteger
            if (rebootDelay != null && rebootDelay.intValue() != -1) {
                cmd.reply("§] §> §cYou can't do that while the server is restarting!")
                return
            }

            if (cmd.args().size() == 0) {
                showDuelsMenu(cmd.sender())
            } else if (cmd.args().size() == 1) {
                Optional<Player> optTarget = cmd.arg(0).parse(Player.class)

                if (!optTarget.isPresent()) {
                    cmd.reply("§4§lDUELS §> §cThat player is not online.")
                    return
                }

                createDuelInvitation(cmd.sender(), optTarget.get())
            } else if (cmd.args().size() == 2 && cmd.rawArg(0).equalsIgnoreCase("accept")) {
                Optional<Player> optTarget = cmd.arg(1).parse(Player.class)

                if (!optTarget.isPresent()) {
                    cmd.reply("§4§lDUELS §> §cThat player is not online.")
                    return
                }

                acceptInvitation(cmd.sender(), optTarget.get())
            }
        }).register("duel", "duels")
    }

    static boolean createDuelInvitation(Player player, Player opponent) {

        if (getAmountOfInvitations(player) >= MAX_INVITES_PER_PLAYER) {
            Players.msg(player, "§4§lDUELS §> §cYou can't invite more players.")
            return false
        }

        if (player == opponent) {
            Players.msg(player, "§4§lDUELS §> §cYou can't invite yourself.")
            return false
        }

        if (opponent == null || !opponent.isOnline()) {
            Players.msg(player, "§4§lDUELS §> §cPlayer is offline.")
            return false
        }

        if (ToggleUtils.hasToggled(opponent,"duels")) {
            Players.msg(player, "§4§lDUELS §> §cThis player is currently not accepting any duels.")
            return false
        }

        if (getPlayerDuel(player) != null) {
            Players.msg(player, "§4§lDUELS §> §cYou are already in a duel.")
            return false
        }

        if (PENDING_INVITATIONS.get(player) != null && PENDING_INVITATIONS.get(player).contains(opponent.uniqueId)) {
            acceptInvitation(player, opponent)
            return true
        }

        List<UUID> invitedBy = PENDING_INVITATIONS.get(opponent.uniqueId)

        if (invitedBy == null) {
            invitedBy = new ArrayList<>(4)
        }

        if (invitedBy.contains(player.uniqueId)) {
            Players.msg(player, "§4§lDUELS §> §cYou already invited this player to a duel.")
            return false
        }

        invitedBy.add(player.uniqueId)

        PENDING_INVITATIONS.put(opponent.uniqueId, invitedBy)

        Players.msg(player, "§4§lDUELS §> §fYou have invited §e${opponent.name} §fto a duel. They have §b${INVITE_EXPIRE} seconds §fto accept." )
        opponent.sendMessage("§4§lDUELS §> §fYou have received invitation to duel from §e${player.name}§f. Type §e/duel accept ${player.name} §fto accept.")

        Schedulers.async().runLater({

            List<UUID> list = PENDING_INVITATIONS.get(opponent.uniqueId)
            if (list == null || list.isEmpty()) {
                return
            }

            list.remove(player.uniqueId)
            PENDING_INVITATIONS.put(opponent.uniqueId, list)

            if (opponent.isOnline()) {
                Players.msg(player, "§4§lDUELS §> §fYour invite to §e${opponent.name} §fhas expired.")
            }

            if (player.isOnline()) {
                opponent.sendMessage("§4§lDUELS §> §fInvite from §e${player.name} §fhas expired.")
            }

        },INVITE_EXPIRE,TimeUnit.SECONDS)
        return true
    }

    static boolean acceptInvitation(Player player, Player whoInvited) {

        List<UUID> pendingInvitations = PENDING_INVITATIONS.get(player.uniqueId)

        if (pendingInvitations == null || !pendingInvitations.contains(whoInvited.uniqueId)) {
            Players.msg(player, "§4§lDUELS §> §cThat player has not invited you to a duel.")
            return false
        }

        PENDING_INVITATIONS.remove(player.uniqueId)
        PENDING_INVITATIONS.remove(whoInvited.uniqueId)

        whoInvited.sendMessage("§4§lDUELS §> §e${player.name} §fhas §aaccepted §fyour duel invitation.")
        Players.msg(player, "§4§lDUELS §> §fYou have §aaccepted §fduel invitation from §e${whoInvited.name}.")

        startDuel(new Duel(whoInvited, player))
    }

    static boolean bet(Player better, Player target, Long amount) {
        Duel duel = getPlayerDuel(target)

        if (duel == null) {
            better.sendMessage("§4§lDUELS §> §cThat player is not in duel.")
            return false
        }

        CurrencyStorage storage = CurrencyUtils.get("credits")
        return duel.bet(better, target, amount, storage)
    }

    static void startDuel(Duel duel, boolean announce = true) {

        DUELS_BY_ID.put(duel.uuid, duel)

        if (announce) {
            Players.all().forEach({
                Players.msg(it, " ")
                Players.msg(it, "§4§lDUELS §> §e§l${duel.player.name} §cvs §e§l${duel.opponent.name} §fis starting in 3 minutes. Place your bets!")
                Players.msg(it, " ")
            })
        }
    }

    static void endDuel(Duel duel, DuelEndCause cause) {

        if (duel.winner == null) {
            DUELS_BY_ID.remove(duel.uuid)
            return
        }

        if (cause == DuelEndCause.PLAYER_QUIT_BEFORE_DUEL) {
            if (duel.winner.isOnline()) {
                duel.winner.sendMessage("§4§lDUELS §> §cYour opponent was not brave enough and left the server. Duel was cancelled.")
            }
            refundPlayers(duel)
        } else if (cause == DuelEndCause.NORMAL || cause == DuelEndCause.PLAYER_QUIT_IN_DUEL) {

            if (duel.winner.isOnline()) {
                duel.winner.sendMessage("§4§lDUELS §> §fYou have slaughtered §e${duel.loser.name} §fin duel and §aWON §fthe duel!")
            }

            if (duel.loser.isOnline()) {
                duel.loser.sendMessage("§4§lDUELS §> §fYou have §cLOST §fthe duel against §e${duel.loser.name}§f. Come back stronger!")
            }

            MySQL.getAsyncDatabase().executeBatch("INSERT INTO duels_stats (uuid_least, uuid_most, wins, losses, server_id) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE wins = wins + VALUES(wins), losses = losses + VALUES(losses)", { statement ->
                statement.setLong(1, duel.winner.uniqueId.getLeastSignificantBits())
                statement.setLong(2, duel.winner.uniqueId.getMostSignificantBits())
                statement.setInt(3, 1)
                statement.setInt(4, 0)
                statement.setString(5, Temple.templeId)
                statement.addBatch()
                statement.setLong(1, duel.loser.uniqueId.getLeastSignificantBits())
                statement.setLong(2, duel.loser.uniqueId.getMostSignificantBits())
                statement.setInt(3, 0)
                statement.setInt(4, 1)
                statement.setString(5, Temple.templeId)
                statement.addBatch()
            })

            payoutPlayers(duel)
        }

        DUELS_BY_ID.remove(duel.uuid)
    }

    static Duel getPlayerDuel(Player p) {
        Optional<Duel> opt = DUELS_BY_ID.values().stream().filter({ it.player == p || it.opponent == p }).findAny()
        if (opt.isPresent()) {
            return opt.get()
        }
        return null
    }

    static void handleDuelsEvent(Player player, ClickType type, int slot, MenuBuilder builder) {
        UUID id = UUID.fromString(FastItemUtils.getCustomTag(builder.get().getItem(slot), new NamespacedKey(Starlight.plugin, "duel_id"), ItemTagType.STRING))

        Duel duel = getDuelById(id)

        if (duel == null) {
            Players.msg(player, "§4§lDUELS §> §fThis duel is no longer available!")
            player.closeInventory()
            return
        }

        if (duel.started) {
            Players.msg(player, "§4§lDUELS §> §fThis duel has already started!")
            player.closeInventory()
            return
        }

        showBetMenu(player, duel)
    }

    static Duel getDuelById(UUID uuid) {
        return DUELS_BY_ID.get(uuid)
    }

    static void showBetMenu(Player player, Duel duel) {
        CurrencyStorage storage = CurrencyUtils.get("credits")
        MenuBuilder builder = new MenuBuilder(27, "§8Place your Bet")
        MenuDecorator.decorate(builder, [
                "040404040",
                "40-0-0-04",
                "040404040"
        ],

                new ActionableItem(FastItemUtils.createSkull(duel.player, "§e${duel.player.name} §7(${NumberUtils.format(duel.getWinningPercentage(duel.player))} %)", ["", "§7* Click to place your bet *"]), { p, t, s ->
                    SignUtils.openSign(player, ["", "^ ^ ^", "Enter Amount", "$storage.displayName"], { String[] lines, Player p1 ->
                        LongUtils.LongParseResult result = LongUtils.parseLong(lines[0])

                        if (!result.isPositive()) {
                            Players.msg(player, "§4§lDUELS §> §e${lines[0]} §fis not a valid amount to bet!")
                            return
                        }

                        long amount = result.getValue()

                        bet(p1, duel.player, amount)
                    })
                }),

                new ActionableItem(FastItemUtils.createItem(Material.DIAMOND_SWORD, "§8[§eRefresh§8]", [
                        "",
                        "§e * Click to refresh live percentage * "
                ]), { p1, t, s ->
                    showBetMenu(p1, duel)
                }),

                new ActionableItem(FastItemUtils.createSkull(duel.opponent, "§e${duel.opponent.name} §7(${NumberUtils.format(duel.getWinningPercentage(duel.opponent))} %)", ["", "§7* Click to place your bet *"]), { p, t, s ->
                    SignUtils.openSign(player, ["", "^ ^ ^", "Enter Amount", "$storage.displayName"], { String[] lines, Player p1 ->
                        LongUtils.LongParseResult result = LongUtils.parseLong(lines[0])

                        if (!result.isPositive()) {
                            Players.msg(player, "§4§lDUELS §> §e${lines[0]} §fis not a valid amount to bet!")
                            return
                        }

                        long amount = result.getValue()

                        bet(p1, duel.opponent, amount)
                    })
                }),

        )

        MenuUtils.syncOpen(player, builder)
    }

    static int getAmountOfInvitations(Player player)  {
        return PENDING_INVITATIONS.values().stream().filter({ it.contains(player.getUniqueId())}).count()
    }

    static void showDuelsMenu(Player player, int page = 1) {

        MySQL.getAsyncDatabase().executeQuery("SELECT * FROM duels_stats WHERE ${DatabaseUtils.getServerUserExpression(player)}", { statement -> }, { result ->

            int wins = 0
            int losses = 0

            if (result.next()) {
                wins = result.getInt(3)
                losses = result.getInt(4)
            }

            MenuBuilder builder
            builder = MenuUtils.createPagedMenu("§8Duels", DUELS_BY_ID.values().stream().filter(duel -> duel.getTimeUntilStart() > 0).collect(Collectors.toList()).sort { it.getTimeUntilStart() }.reverse(), { Duel duel, Integer i ->
                return getDuelItem(duel)
            }, page, false, [
                    { p, t, s -> handleDuelsEvent(p, t, s, builder) },
                    { p, t, s -> showDuelsMenu(p, page + 1) },
                    { p, t, s -> showDuelsMenu(p, page - 1) }
            ])

            builder.set(builder.get().getSize() - 8, FastItemUtils.createItem(Material.SUNFLOWER, "§8[§eRefresh§8]", [
                    "",
                    "§e * Click to refresh page * "
            ]), { p1, t, s ->
                showDuelsMenu(p1, page)
            })

            double ratio = 0.0D

            if (losses != 0) {
                ratio = wins * 100.0D / losses
            } else if (wins != 0) {
                ratio = 100.0D
            }

            builder.set(builder.get().getSize() - 2, FastItemUtils.createItem(Material.BOOK, "§8[§9Your Statistics§8]", [
                    "§fWins: §a${NumberUtils.format(wins)}",
                    "§fLosses: §c${NumberUtils.format(losses)}",
                    "§fWLR: §${ratio < 100 ? "c" : "a"}${NumberUtils.formatDouble(ratio)}%"
            ]), { p, t, s -> })

            MenuUtils.syncOpen(player, builder)
        })
    }

    static ItemStack getDuelItem(Duel duel) {
        ItemStack item = FastItemUtils.createItem(Material.DIAMOND_SWORD, "§e${duel.player.name} §c§ovs §e${duel.opponent.name}", [" ", "§7* Starts in §b${duel.getTimeUntilStart()} seconds §7*", "§6* Click To Bet *"])
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "duel_id"), ItemTagType.STRING, duel.uuid.toString())
        return item
    }

    static void refundPlayers(Duel duel) {

        if (duel == null) {
            return
        }

        CurrencyStorage storage = CurrencyUtils.get("credits")

        for (Pair<Player, Long> bet : duel.getAllBets()) {
            storage.add(bet.key, bet.value)
            if (bet.key.isOnline()) {
                bet.key.sendMessage("§4§lDUELS §> §fThe duel you bet on was §ccancelled §fand you were refunded ${storage.map(bet.value)}.")
            }
        }
    }

    static void payoutPlayers(Duel duel) {

        if (duel == null) {
            return
        }

        if (duel.winner == null) {
            //should not happen!
            return
        }

        CurrencyStorage storage = CurrencyUtils.get("credits")

        for (Pair<Player, Long> bet : duel.getBetsOn(duel.winner)) {
            storage.add(bet.key, bet.value * 2)
            if (bet.key.isOnline()) {
                bet.key.sendMessage("§4§lDUELS §> §fPlayer §e${duel.winner.name} §fhas won the duel you bet on. You won ${storage.map(bet.value * 2)}.")
            }
        }
    }
}


enum DuelEndCause {
    NORMAL,
    PLAYER_QUIT_IN_DUEL,
    PLAYER_QUIT_BEFORE_DUEL
}

class Duel {

    UUID uuid

    Player player
    Player opponent

    Player winner
    Player loser

    // key - player who betted
    // value - player/opponent + amount betted

    Map<UUID, Pair<Player, Long>> betters

    boolean started
    long createdAt

    Duel(Player player, Player opponent) {
        this.uuid = UUID.randomUUID()
        this.createdAt = Time.nowMillis()
        this.player = player
        this.opponent = opponent
        this.betters = new HashMap<>(50)
        this.winner = null
        this.loser = null
        this.started = false

        Schedulers.sync().runLater({
            if (!DuelUtils.DUELS_BY_ID.containsValue(this.uuid)) {
                //Duel is no longer active.
                return
            }
            this.started = true

            DuelUtils.endDuel(this, DuelEndCause.NORMAL)
            //todo: teleport players to arena and start pvp
        }, 3, TimeUnit.MINUTES)
    }

    Pair<Player, Long> getPlayerBet(Player p) {
        return betters.get(p.uniqueId)
    }

    Collection<Pair<Player, Long>> getAllBets() {
        return betters.values()
    }

    Collection<Pair<Player, Long>> getBetsOn(Player player) {
        return betters.get(player.uniqueId)
    }

    boolean bet(Player whoBetted, Player player, Long amount, CurrencyStorage storage) {

        if (started) {
            whoBetted.sendMessage("§4§lDUELS §> §cYou can't bet on duel that is in progress!")
            return false
        }

        if (whoBetted.uniqueId == this.player.uniqueId || whoBetted.uniqueId == this.opponent.uniqueId) {
            whoBetted.sendMessage("§4§lDUELS §> §cYou can't bet on yourself or on your opponent!")
            return false
        }

        if (storage.get(whoBetted.uniqueId) < amount) {
            storage.notEnough(whoBetted)
            return false
        }

        storage.take(whoBetted, amount, {
            Pair<Player, Long> currentBet = this.getPlayerBet(whoBetted)

            if (currentBet == null) {
                currentBet = new Pair<>(whoBetted, 0L)
            }

            //todo: perhaps player can change his bet selection of player ?

            currentBet.value += amount

            betters.put(whoBetted.uniqueId, currentBet)

            whoBetted.sendMessage("§4§lDUELS §> §fYou have successfully betted ${storage.map(amount)} §fon §e$player.name")
            return true
        })
    }

    long getTimeUntilStart() {
        long startsAt = this.createdAt + TimeUnit.MINUTES.toMillis(3)
        long current = Time.nowMillis()

        long secsDiff = TimeUnit.MILLISECONDS.toSeconds(startsAt - current)
        return secsDiff
    }

    boolean eliminatePlayer(Player player) {

        if (player == null) {
            return false
        }

        if (this.player == player) {
            this.winner = this.opponent
            this.loser = this.player
        } else if (this.opponent == player) {
            this.winner = this.player
            this.loser = this.opponent
        }

        return true
    }

    int getAmountOfBetters(Player p) {
        return this.betters.values().stream().findAll({ it.key == p }).size()
    }

    double getWinningPercentage(Player p) {
        int totalBetters = this.betters.values().size()
        int betters = this.getAmountOfBetters(p)

        if (totalBetters == 0) {
            return 0.00
        }

        return betters / totalBetters
    }
}

