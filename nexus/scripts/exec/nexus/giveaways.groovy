package scripts.exec.nexus

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import scripts.NotificationUtils
import scripts.shared.features.NewTicketsUtils
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.tickets.GiveawayType
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.utils.DiscordWebhook
import scripts.shared.utils.Temple
import org.bukkit.entity.Player
import scripts.shared.database.Standard
import scripts.shared3.Redis

import java.awt.Color
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom

GiveawayUtils.init()

class GiveawayUtils {
    // this is gonna end up being really really big... maybe move to some other file idk.
    static def rewards = Exports.get("giveaways", [
            2023: [
                    6: [
                            1 : [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            2 : [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            3 : [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            4 : [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            5 : [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            6 : [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            7 : [
                                    daily : [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                                    weekly: [
                                            "§e1x Weekly Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            8 : [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            9 : [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            10: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            11: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            12: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            13: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            14: [
                                    daily : [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                                    weekly: [
                                            "§e1x Weekly Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            15: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            16: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            17: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            18: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            19: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            20: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            21: [
                                    daily : [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                                    weekly: [
                                            "§e1x Weekly Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            22: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            23: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            24: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            25: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            26: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            27: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
                            28: [
                                    daily : [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                                    weekly: [
                                            "§e1x Weekly Giveaway Token (/giveawayrewards)"
                                    ],
                                    monthly: [
                                            "§e1x Monthly Giveaway Token (/giveawayrewards)"
                                    ]
                            ],
                            29: [
                                    daily: [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                            ],
//                            30: [
//                                    daily: [
//                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
//                                    ],
//                            ],
                            30: [
                                    daily  : [
                                            "§e1x Daily Giveaway Token (/giveawayrewards)"
                                    ],
                                    monthly: [
                                            "§e1x Monthly Giveaway Token (/giveawayrewards)"
                                    ],

                            ]
                    ]
            ]
    ])

    static float getRealTicketAmount(int tickets) {
        if (tickets > 100) {
            return Math.sqrt(tickets / 100) * 100
        } else {
            return tickets
        }
    }

    static void init() {
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS giveaway_rolled (giveaway_date VARCHAR(10) NOT NULL, giveaway_type TINYINT(1) NOT NULL)")
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS giveaway_winners (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, username VARCHAR(32) NOT NULL, giveaway_date VARCHAR(10) NOT NULL, giveaway_type TINYINT(1) NOT NULL, timestamp TIMESTAMP NOT NULL)")
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS giveaway_tickets (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, tickets INT NOT NULL, giveaway_date VARCHAR(10), giveaway_type TINYINT(1) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, giveaway_date, giveaway_type))")
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS giveaway_discord_announcements (name VARCHAR(100) NOT NULL, description VARCHAR(1024) NOT NULL, PRIMARY KEY(name))")
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS giveaway_tokens (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, daily INTEGER NOT NULL, weekly INTEGER NOT NULL, monthly INTEGER NOT NULL, PRIMARY KEY(uuid_least, uuid_most))")
        Schedulers.async().runRepeating({
            Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("EST"))
            calendar.setTimeInMillis(System.currentTimeMillis())
            calendar.set(Calendar.HOUR_OF_DAY, 12 + 3)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                rollGiveaways(calendar)
            }

            NewTicketsUtils.checkAndWipe()
        }, 0, 20 * 5)

        Commands.create().assertOp().assertUsage("<player> <type> <amount>").handler({ command ->
            DatabaseUtils.getId(command.rawArg(0), { uuid, username, player ->
                if (uuid == null) {
                    command.reply("§] §> §a${username} §fhas never joined the server before!")
                    return
                }

                String type = command.rawArg(1).toLowerCase()
                if (!["daily", "weekly", "monthly"].contains(type)) {
                    command.reply("§] §> §a${command.rawArg(1)} §fis not a valid type, available types: ${["daily", "weekly", "monthly"].join(", ")} .")
                    return
                }

                int amount
                try {
                    amount = Integer.parseInt(command.rawArg(2))
                } catch (NumberFormatException ignore) {
                    command.reply("§] §> §a${command.rawArg(2)} §fis not a valid amount. Make sure to use whole numbers.")
                    return
                }

                MySQL.getAsyncDatabase().execute("INSERT INTO giveaway_tokens (uuid_least, uuid_most, daily, weekly, monthly) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE ${type} = ${type} + ${amount}", { statement ->
                    statement.setLong(1, uuid.leastSignificantBits)
                    statement.setLong(2, uuid.mostSignificantBits)
                    statement.setInt(3, type == "daily" ? amount : 0)
                    statement.setInt(4, type == "weekly" ? amount : 0)
                    statement.setInt(5, type == "monthly" ? amount : 0)
                })

                if (player != null) {
                    Players.msg(player, "§] §> §aYou have received ${amount}x ${type.capitalize()} giveaway tokens! Open it in §b/giveawayrewards")
                }

                command.reply("§] §> §aYou have successfully given ${username} ${amount}x ${type} giveaway tokens.")
            })
        }).register("givegiveawaytoken")

        Commands.create().assertOp().assertPlayer().assertUsage("<type>").handler({ command ->
            Player player = command.sender()
            rewardAutomatically(player.uniqueId, command.rawArg(0))
        }).register("dev/testgiveawayreward")

        Commands.create().assertConsole().assertUsage("<ymd> [type]").handler { c ->
            String ymd = c.arg(0).parseOrFail(String)
            Optional<Integer> type = c.arg(1).parse(Integer)
            MySQL.getAsyncDatabase().executeUpdate("DELETE FROM giveaway_rolled WHERE giveaway_date = ?${type.isPresent() ? " AND giveaway_type = ?" : ""}", { statement ->
                statement.setString(1, ymd)
                if (type.isPresent()) {
                    statement.setInt(2, type.get())
                }
            }, { res ->
                if (res != 0) {
                    c.reply("§cDone. ${res} giveaways effected.")
                } else {
                    c.reply("§cFailed. no giveaways effected.")
                }
            })
        }.register("dev/giveaway/reroll")

        Commands.create().assertUsage("<year> <month> <day>").assertConsole().handler { c ->
            int year = c.arg(0).parseOrFail(Integer.class)
            int month = c.arg(1).parseOrFail(Integer.class)
            int day = c.arg(2).parseOrFail(Integer.class)
            Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("EST"))
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month - 1)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            calendar.set(Calendar.HOUR_OF_DAY, 12 + 3)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            rollGiveaways(calendar)
        }.register("dev/giveaway/roll")
    }

    static void rollGiveaways(Calendar calendar) {
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            for (GiveawayType type in GiveawayType.values()) {
                final GiveawayType typeFinal = type
                rollGiveaway(calendar, typeFinal)

            }
        }
    }

    static void rollGiveaway(Calendar calendar, GiveawayType type) {
        int year = calendar.get(Calendar.YEAR)
        int month = calendar.get(Calendar.MONTH)
        int day = calendar.get(Calendar.DAY_OF_MONTH)
        String ymdStr = "${year}-${month}-${day}"
        String announcementYmdStr = "${year}-${month + 1}-${day}"
        LinkedHashMap<String, ArrayList<String>> reward = rewards[year] ?[month + 1] ?[day]
        if (!reward) return
        if ((reward.daily && type == GiveawayType.DAILY) || (reward.weekly && type == GiveawayType.WEEKLY) || (reward.monthly && type == GiveawayType.MONTHLY)) {
            MySQL.getAsyncDatabase().executeQuery("SELECT COUNT(*) from giveaway_rolled WHERE giveaway_date = ? AND giveaway_type = ?", { statement ->
                statement.setString(1, ymdStr)
                statement.setInt(2, type.ordinal())
            }, { rs ->
                if (rs.next()) {
                    if (rs.getInt(1) == 0) {
                        MySQL.getAsyncDatabase().executeUpdate("INSERT INTO giveaway_rolled (giveaway_date, giveaway_type) VALUES(?, ?)", { statement ->
                            statement.setString(1, ymdStr)
                            statement.setInt(2, type.ordinal())
                        }, {
                            MySQL.getAsyncDatabase().executeQuery("SELECT uuid_least,uuid_most,tickets FROM giveaway_tickets WHERE giveaway_date = ? AND giveaway_type = ?", { statement ->
                                statement.setString(1, ymdStr)
                                statement.setInt(2, type.ordinal())
                            }, { ticketRs ->
                                def totalTickets = 0
                                def winMap = new HashMap<UUID, Integer>()
                                while (ticketRs.next()) {
                                    def tickets = getRealTicketAmount(ticketRs.getInt(3)).intValue()
                                    //TODO check ip, activity, whatever
                                    winMap.put(new UUID(ticketRs.getLong(2), ticketRs.getLong(1)), tickets)
                                    totalTickets += tickets
                                }

                                println "Picking winners for ${ymdStr} ${type.name()}..."
                                pickWinners(winMap, ymdStr, type, totalTickets).thenAcceptAsync({ Map<UUID, String> winners ->
                                    StringBuilder prizeMsg = new StringBuilder()
                                    List<String> rewards = new ArrayList<>()
                                    switch (type.name().toLowerCase()) {
                                        case "daily":
                                            rewards.addAll(reward.daily as List<String>)
                                            break
                                        case "weekly":
                                            rewards.addAll(reward.weekly as List<String>)
                                            break
                                        case "monthly":
                                            rewards.addAll(reward.monthly as List<String>)
                                            break
                                    }
                                    for (String r : rewards) {
                                        prizeMsg.append(ChatColor.stripColor(r))
                                        if (rewards.size() > 1) {
                                            prizeMsg.append(", ")
                                        }
                                    }
                                    String prizeMessage = prizeMsg.toString().replace("[", "").replace("]", "")

                                    MySQL.getAsyncDatabase().executeBatch("INSERT INTO giveaway_winners (uuid_least, uuid_most, username, giveaway_date, giveaway_type, prizes) VALUES (?,?,?,?,?,?)", { statement ->
                                        for (def winner in winners) {
                                            statement.setLong(1, winner.key.getLeastSignificantBits())
                                            statement.setLong(2, winner.key.getMostSignificantBits())
                                            statement.setString(3, winner.getValue())
                                            statement.setString(4, ymdStr)
                                            statement.setInt(5, type.ordinal())
                                            statement.setString(6, prizeMessage)
                                            statement.addBatch()
                                        }
                                    })

                                    for (def winner in winners) {
                                        rewardAutomatically(winner.key, type.name())
                                    }

                                    Redis.get().publish(Standard.SRV_ALERT, "Network|${type.name().toLowerCase().capitalize()} giveaway winner${winners.values().size() > 1 ? "s" : ""} for ${announcementYmdStr} are ${winners.values().join(", ")}")

                                    alertDiscord(winners, type.name().toLowerCase().capitalize(), prizeMessage, announcementYmdStr, "https://discord.com/api/webhooks/1160790335757226115/Kk8f5_DpyJxA_LQy462bO4oJI_No9SyS1S8DS-kWJQ8i7SnDVX6yBXGm2Cm3SGMFo0rd")
                                })
                            })
                        })
                    }
                }
            })
        }
    }

    static void alertDiscord(Map<UUID, String> winners, String type, String prize, String date, String webhookUrl) {
        println "Dispatching discord alert!"
        String entryName = "${type}_${date}"

        Schedulers.async().run({
            MySQL.getAsyncDatabase().executeQuery("SELECT * FROM giveaway_discord_announcements WHERE name = ?", { statement ->
                statement.setString(1, entryName)
            }, { result ->
                if (result.next()) {
                    return
                }

                MySQL.getAsyncDatabase().execute("INSERT INTO giveaway_discord_announcements (name, description) VALUES (?, ?)", { statement ->
                    statement.setString(1, entryName)
                    statement.setString(2, "${prize}_${winners.values().join(", ")}")
                })
                StringBuilder str = new StringBuilder()
                int i = 0
                for (Map.Entry<UUID, String> entry : winners) {
                    MySQL.getSyncDatabase().executeQuery("SELECT discord_id FROM discord_sync_state WHERE state = ? AND uuid = ?", { statement ->
                        statement.setInt(1, 1)
                        statement.setString(2, entry.getKey().toString())
                    }, { dcResult ->
                        String dcId = ""
                        if (dcResult.next()) {
                            dcId = " (<@${dcResult.getString(1)}>)"
                        }
                        str.append("**#").append(i + 1).append(":** ").append("`").append(entry.getValue()).append("`").append(dcId).append("\\n")
                    })
                    i++
                }

                try {
                    DiscordWebhook webhook = new DiscordWebhook(webhookUrl)
                    webhook.setAvatarUrl("https://cdn.discordapp.com/attachments/1159374655140941854/1160789975755935774/S-Logo.png")
                    webhook.setUsername("Starcade")
                    webhook.addEmbed(
                            new DiscordWebhook.EmbedObject()
                                    .setTitle(" :crown: Giveaway Winners :crown: ")
                                    .setColor(Color.GREEN)
                                    .addField("Type", type, true)
                                    .addField("Prize(s)", prize, true)
                                    .addField("Date", date, true)
                                    .addField("Winners", str.toString(), false)
                                    .setFooter("Alert sent out ${new Date().toString()}", "")
                    )
                    webhook.execute() //Handle exception
                } catch (Exception ex) {
                    ex.printStackTrace()
                }
            })
        })
    }

    static CompletableFuture<Map<UUID, String>> pickWinners(Map<UUID, Integer> winMap, String ymdStr, GiveawayType typeFinal, int totalTickets) {
        CompletableFuture<Map<UUID, String>> future = new CompletableFuture<Map<UUID, String>>()
        println "Picking winners for ${ymdStr} ${typeFinal.name()}..."
        Map<UUID, String> winners = new HashMap<UUID, String>()
        for (def i in 0..typeFinal.winners - 1) {
            def selected = ThreadLocalRandom.current().nextInt(0, totalTickets)

            def iterated = 0
            for (Map.Entry<UUID, Integer> entry in winMap) {
                iterated += entry.getValue()
                if (iterated > selected) {
                    UUID key = entry.getKey()
                    boolean isDone = i == typeFinal.winners - 1
                    DatabaseUtils.getLatestUsername(key, { String username ->
                        winners.put(key, username)
                        if (isDone) {
                            future.complete(winners)
                        }
                    })
                    winMap.remove(key)
                    totalTickets -= entry.getValue()
                    break
                }
            }
        }
        return future
    }

    static void rewardAutomatically(UUID uuid, String giveawayType) {
        if (!Exports.get("useNewGiveaways", false) && Temple.templeId != "nexus") {
            return
        }

        DatabaseUtils.getLatestUsername(uuid, { username ->
            if (username == null) {
                return
            }
            String type = giveawayType.toLowerCase()
            String cmd
            if (type == "daily") {
                cmd = "givegiveawaytoken ${username} daily 1"
            } else if (type == "weekly") {
                cmd = "givegiveawaytoken ${username} weekly 1"
            } else if (type == "monthly") {
                cmd = "givegiveawaytoken ${username} monthly 1"
            }

            if (cmd != null) {
                println "[Giveaways] executing command \"${cmd}\" for ${type} giveaway winner ${username}!"
                Schedulers.sync().run({
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
                })
                NotificationUtils.sendNotification(uuid, [
                        " ",
                        "§6§lGIVEAWAYS",
                        "§aCongrats you have won the ${type.capitalize()} giveaway!",
                        "§aYour reward is: §f1x ${type.capitalize()} giveaway token.",
                        " ",
                        "§eRedeem it in /giveawayrewards (read this",
                        "§enotification to open the giveaways menu)"
                ], [
                        "sudo {name} giveawayrewards"
                ], {})
            }
        })
    }
}


