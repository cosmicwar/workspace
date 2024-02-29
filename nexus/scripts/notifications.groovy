package scripts

import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.TimeUtils
import scripts.shared.legacy.wrappers.Console
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.ActionableItem
import scripts.shared.utils.MenuDecorator
import scripts.shared.utils.Temple
import scripts.shared3.Redis

//TODO needs to be recode.

NotificationUtils.init()

enum NotificationType {
    UNREAD,
    READ,
    ARCHIVED
}

class Notification {
    boolean read
    boolean archived
    boolean commandsRan
    List<String> commands
    List<String> lines
    String unique
    long time

    Notification(boolean read, boolean archived, boolean commandsRan, List<String> commands, List<String> lines, String unique, long time) {
        this.read = read
        this.archived = archived
        this.commandsRan = commandsRan
        this.commands = commands
        this.lines = lines
        this.unique = unique
        this.time = time
    }

    @Override
    String toString() {
        return "Notification{" +
                "read=" + read +
                ", archived=" + archived +
                ", commandsRan=" + commandsRan +
                ", commands=" + commands +
                ", lines=" + lines +
                ", unique='" + unique +
                ", time='" + time +
                '}';
    }
}

class NotificationUtils {
    static NamespacedKey NOTIFICATION_UNIQUE_KEY = new NamespacedKey(Starlight.plugin, "notification_unique")

    static void init() {
        Redis.get().subscribe({channel, message ->
            try {
                notifyPlayer(Bukkit.getPlayer(UUID.fromString(message)))
            } catch (Exception ignore) {}
        }, "notification_sent")

        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS notifications (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, date BIGINT NOT NULL, notif_lines TEXT NOT NULL, notif_commands TEXT NOT NULL, is_read BOOLEAN NOT NULL, is_archived BOOLEAN NOT NULL, commands_ran BOOLEAN NOT NULL, notif_unique VARCHAR(16) NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, notif_unique, server_id))")
        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS notifications_deleted (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, date BIGINT NOT NULL, notif_lines TEXT NOT NULL, notif_commands TEXT NOT NULL, is_read BOOLEAN NOT NULL, is_archived BOOLEAN NOT NULL, commands_ran BOOLEAN NOT NULL, notif_unique VARCHAR(16) NOT NULL, server_id VARCHAR(16) NOT NULL, deleter_least BIGINT NOT NULL, deleter_most BIGINT NOT NULL, date_deleted BIGINT NOT NULL, PRIMARY KEY(uuid_least, uuid_most, notif_unique, server_id))")

        Commands.create().assertPlayer().handler({command ->
            Player player = command.sender()
            showNotificationsMenu(player, player.uniqueId)
        }).register("notifs", "notifications")

        Commands.create().assertPlayer().assertOp().assertUsage("<username>").handler({command ->
            DatabaseUtils.getId(command.rawArg(0), {uuid, username, player ->
                if (uuid == null) {
                    command.reply("§! §> §e${username} §fhas never joined the server before!")
                    return
                }
                showNotificationsMenu(command.sender(), uuid)
            })
        }).register("notifsother", "notificationsother")

        Commands.create().assertOp().assertUsage("<username> <message> <command>").handler({command ->
            DatabaseUtils.getId(command.rawArg(0), {uuid, username, player ->
                if (uuid == null) {
                    command.reply("§! §> §e${username} §fhas never joined the server before!")
                    return
                }

                List<String> lines = new ArrayList<>()
                lines.add(command.rawArg(1).replace("_", " "))

                List<String> commands = new ArrayList<>()
                commands.add(command.rawArg(2).replace("_", " "))

                sendNotification(uuid, lines, commands, {
                    command.reply("§aSuccessfully sent out test notification to ${username}!")
                })
            })
        }).register("testnotification", "testnotif")

        Commands.create().assertOp().handler({command ->
            MySQL.getAsyncDatabase().executeUpdate("UPDATE notifications SET notif_commands = ?, commands_ran = ? WHERE server_id = ?", {statement ->
                statement.setString(1, "")
                statement.setBoolean(2, true)
                statement.setString(3, Temple.templeId)
            }, {result ->
                if (result != 0) {
                    command.reply("SUCCESSFULLY CLEARED NOTIFICATION COMMANDS FOR TEMPLE ${Temple.templeId}!")
                }
            })
        }).register("dev/clearnotifcommands")

        Events.subscribe(PlayerJoinEvent.class).handler({event ->
            Player player = event.player
            Schedulers.sync().runLater({
                showNotificationsIfHasUnread(player)
            }, 20)
        })
    }

    static void showNotifications(Player player, UUID uuid, int page = 1, NotificationType type) {
        Schedulers.async().run({
            List<Notification> notifications = new ArrayList<>()

            String query = "SELECT * FROM notifications WHERE uuid_least = '${uuid.leastSignificantBits}' AND uuid_most = '${uuid.mostSignificantBits}' AND is_archived = '1' AND server_id = '${Temple.templeId}'"
            String title = "§cArchived"
            if (type == NotificationType.UNREAD) {
                title = "§aUnread"
                query = "SELECT * FROM notifications WHERE uuid_least = '${uuid.leastSignificantBits}' AND uuid_most = '${uuid.mostSignificantBits}' AND is_read = '0' AND is_archived = '0' AND server_id = '${Temple.templeId}'"
            } else if (type == NotificationType.READ) {
                title = "§eRead"
                query = "SELECT * FROM notifications WHERE uuid_least = '${uuid.leastSignificantBits}' AND uuid_most = '${uuid.mostSignificantBits}' AND is_read = '1' AND is_archived = '0' AND server_id = '${Temple.templeId}'"
            }

            MySQL.getSyncDatabase().executeQuery(query, {statement ->}, {result ->
                while (result.next()) {
                    Notification notif = new Notification(result.getBoolean("is_read"), result.getBoolean("is_archived"), result.getBoolean("commands_ran"), result.getString("notif_commands").split("<!!>").toList(), result.getString("notif_lines").split("<!!>").toList(), result.getString("notif_unique"), result.getLong("date"))
                    notifications.add(notif)
                }
            })

            MenuBuilder builder
            builder = MenuUtils.createPagedMenu("${title} Notifications", notifications, { Notification notification, Integer i ->
                List<String> lines = notification.lines
                if (type == NotificationType.ARCHIVED) {
                    lines.add(" ")
                    lines.add("§aRight-Click to unarchive")
                } else {
                    if (type == NotificationType.UNREAD) {
                        if (player.uniqueId == uuid) {
                            lines.add(" ")
                            lines.add("§aLeft-Click to read")
                            if (!notification.commandsRan && notification.commands != null && notification.commands.size() > 0) {
                                lines.add("§e[Will run pending commands]")
                                lines.add(" ")
                            }
                        }
                    } else {
                        if (player.uniqueId == uuid) {
                            lines.add(" ")
                            lines.add("§aLeft-Click to mark as unread")
                        }
                    }

                    if (player.uniqueId == uuid) {
                        lines.add("§cRight-Click to archive")
                    } else {
                        lines.add(" ")
                        lines.add("§4§lStaff Only:")
                        lines.add("§4Shift + Right-Click to delete")
                    }

                    lines.add(" ")
                    lines.add("§7Sent ${TimeUtils.getTimeAmount(System.currentTimeMillis() - notification.time, true)} ago")
                }


                ItemStack item = FastItemUtils.createItem(Material.PAPER, title, lines)
                FastItemUtils.setCustomTag(item, NOTIFICATION_UNIQUE_KEY, ItemTagType.STRING, notification.unique)

                return item
            }, page, true, [
                    { p, t, s ->
                        String unique = FastItemUtils.getCustomTag(builder.get().getItem(s), NOTIFICATION_UNIQUE_KEY, ItemTagType.STRING)
                        if (unique == null) {return}

                        MySQL.getAsyncDatabase().executeQuery("SELECT * FROM notifications WHERE uuid_least = ? AND UUID_MOST = ? AND notif_unique = ? AND server_id = ?", {statement ->
                            statement.setLong(1, uuid.leastSignificantBits)
                            statement.setLong(2, uuid.mostSignificantBits)
                            statement.setString(3, unique)
                            statement.setString(4, Temple.templeId)
                        }, {result ->
                            if (!result.next()) {
                                player.world.execute({
                                    player.closeInventory()
                                    Players.msg(player, "§cNotification not found.")
                                })
                                return
                            }

                            boolean commandsRan = result.getBoolean("commands_ran")
                            List<String> commands = result.getString("notif_commands").split("<!!>").toList()

                            if (t == ClickType.LEFT) {
                                if (player.uniqueId != uuid) {
                                    player.world.execute({
                                        player.closeInventory()
                                        Players.msg(player, "§cYou can't read/archive a players notifications, only delete them.")
                                    })
                                    return
                                }
                                if (type == NotificationType.UNREAD) {
                                    MySQL.getSyncDatabase().executeUpdate("UPDATE notifications SET is_read = ?, commands_ran = ? WHERE uuid_least = ? AND UUID_MOST = ? AND notif_unique = ? AND server_id = ?", {statement ->
                                        statement.setBoolean(1, true)
                                        statement.setBoolean(2, true)
                                        statement.setLong(3, uuid.leastSignificantBits)
                                        statement.setLong(4, uuid.mostSignificantBits)
                                        statement.setString(5, unique)
                                        statement.setString(6, Temple.templeId)
                                    }, {result2 ->
                                        if (result2 != 0) {
                                            if (commands != null && commands.size() > 0 && !commandsRan) {
                                                for (String cmd : commands) {
                                                    Console.dispatchCommand(cmd.replace("{name}", player.name).replace("{uuid}", player.uniqueId.toString()))
                                                }
                                            }
                                        }
                                    })
                                } else if (type == NotificationType.READ) {
                                    MySQL.getSyncDatabase().execute("UPDATE notifications SET is_read = ? WHERE uuid_least = ? AND UUID_MOST = ? AND notif_unique = ? AND server_id = ?", {statement ->
                                        statement.setBoolean(1, false)
                                        statement.setLong(2, uuid.leastSignificantBits)
                                        statement.setLong(3, uuid.mostSignificantBits)
                                        statement.setString(4, unique)
                                        statement.setString(5, Temple.templeId)
                                    })
                                }
                            } else if (t == ClickType.RIGHT) {
                                if (player.uniqueId != uuid) {
                                    player.world.execute({
                                        player.closeInventory()
                                        Players.msg(player, "§cYou can't read/archive a players notifications, only delete them.")
                                    })
                                    return
                                }
                                if (type == NotificationType.ARCHIVED) {
                                    MySQL.getSyncDatabase().execute("UPDATE notifications SET is_archived = ? WHERE uuid_least = ? AND uuid_most = ? AND notif_unique = ? AND is_archived = ? AND server_id = ?", {statement ->
                                        statement.setBoolean(1, false)
                                        statement.setLong(2, uuid.leastSignificantBits)
                                        statement.setLong(3, uuid.mostSignificantBits)
                                        statement.setString(4, unique)
                                        statement.setBoolean(5, true)
                                        statement.setString(6, Temple.templeId)
                                    })
                                } else {
                                    MySQL.getSyncDatabase().executeUpdate("UPDATE notifications SET is_archived = ? WHERE uuid_least = ? AND uuid_most = ? AND notif_unique = ? AND is_read = ? AND server_id = ?", {statement ->
                                        statement.setBoolean(1, true)
                                        statement.setLong(2, uuid.leastSignificantBits)
                                        statement.setLong(3, uuid.mostSignificantBits)
                                        statement.setString(4, unique)
                                        statement.setBoolean(5, true)
                                        statement.setString(6, Temple.templeId)
                                    }, {result2 ->
                                        if (result2 == 0) {
                                            Players.msg(player, "§] §> §cYou can only archive notifications you have read.")
                                            return
                                        }
                                    })
                                }
                            } else if (t == ClickType.SHIFT_RIGHT) {
                                MySQL.getSyncDatabase().executeUpdate("DELETE FROM notifications WHERE uuid_least = ? AND uuid_most = ? AND notif_unique = ? AND server_id = ?", {statement ->
                                    statement.setLong(1, uuid.leastSignificantBits)
                                    statement.setLong(2, uuid.mostSignificantBits)
                                    statement.setString(3, unique)
                                    statement.setString(4, Temple.templeId)
                                }, {result2 ->
                                    if (result2 != 0) {
                                        MySQL.getAsyncDatabase().execute("INSERT INTO notifications_deleted (uuid_least, uuid_most, date, notif_lines, notif_commands, is_read, is_archived, notif_unique, server_id, commands_ran, deleter_least, deleter_most, date_deleted) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", {statement ->
                                            statement.setLong(1, result.getLong("uuid_least"))
                                            statement.setLong(2, result.getLong("uuid_most"))
                                            statement.setLong(3, result.getLong("date"))
                                            statement.setString(4, result.getString("notif_lines"))
                                            statement.setString(5, result.getString("notif_commands"))
                                            statement.setBoolean(6, result.getBoolean("is_read"))
                                            statement.setBoolean(7, result.getBoolean("is_archived"))
                                            statement.setString(8, result.getString("notif_unique"))
                                            statement.setString(9, result.getString("server_id"))
                                            statement.setBoolean(10, result.getBoolean("commands_ran"))
                                            statement.setLong(11, player.uniqueId.leastSignificantBits)
                                            statement.setLong(12, player.uniqueId.mostSignificantBits)
                                            statement.setLong(13, System.currentTimeMillis())
                                        })
                                        Players.msg(player, "§] §> §aYou have successfully deleted a notification!")
                                    }
                                })
                            }

                            showNotifications(player, uuid, page, type)
                        })
                    },
                    { p, t, s -> showNotifications(player, uuid, page + 1, type) },
                    { p, t, s -> showNotifications(player, uuid, page - 1, type) },
                    { p, t, s -> showNotificationsMenu(player, uuid) }
            ])

            MenuUtils.syncOpen(player, builder)
        })
    }

    static void showNotificationsIfHasUnread(Player player) {
        MySQL.getAsyncDatabase().executeQuery("SELECT * FROM notifications WHERE uuid_least = '${player.uniqueId.leastSignificantBits}' AND uuid_most = '${player.uniqueId.mostSignificantBits}' AND is_read = '0' AND is_archived = '0' AND server_id = '${Temple.templeId}'", {statement ->}, {result ->
            if (result.next()) {
                showNotifications(player, player.uniqueId, NotificationType.UNREAD)
            }
        })
    }

    static void showNotificationsMenu(Player player, UUID uuid) {
        MenuBuilder builder
        builder = new MenuBuilder(27, "§8Notifications")

        Schedulers.async().run({
            MySQL.getSyncDatabase().executeQuery("SELECT * FROM notifications WHERE uuid_least = ? AND uuid_most = ? AND server_id = ?", {statement ->
                statement.setLong(1, uuid.leastSignificantBits)
                statement.setLong(2, uuid.mostSignificantBits)
                statement.setString(3, Temple.templeId)
            }, {result ->
                int read = 0
                int unread = 0
                int archived = 0

                while (result.next()) {
                    boolean isRead = result.getBoolean("is_read")
                    boolean isArchived = result.getBoolean("is_archived")

                    if (isRead && !isArchived) {
                        read++
                        continue
                    }

                    if (!isRead && !isArchived) {
                        unread++
                        continue
                    }

                    archived++
                }

                List<ActionableItem> actionableItems = new ArrayList<>()
                actionableItems.add(new ActionableItem(FastItemUtils.createItem(Material.LIME_WOOL, "§aUnread Notifications (${unread})", []), {p, t, s ->
                    showNotifications(player, uuid, NotificationType.UNREAD)
                }))
                actionableItems.add(new ActionableItem(FastItemUtils.createItem(Material.YELLOW_WOOL, "§eRead Notifications (${read})", []), {p, t, s ->
                    showNotifications(player, uuid, NotificationType.READ)
                }))
                actionableItems.add(new ActionableItem(FastItemUtils.createItem(Material.RED_WOOL, "§cArchived Notifications (${archived})", []), {p, t, s ->
                    showNotifications(player, uuid, NotificationType.ARCHIVED)
                }))
                actionableItems.add(new ActionableItem(FastItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "§cClose", []), {p, t, s ->
                    player.world.execute({
                        player.closeInventory()
                    })
                }))

                MenuDecorator.decorate(builder, [
                        "888888888",
                        "88-8-8-88",
                        "8888-8888",
                ], actionableItems.toArray() as ActionableItem[])

                MenuUtils.syncOpen(player, builder)
            })
        })
    }

    /**
     *
     * @param receiver The uuid of the person receiving the notification
     * @param lines The lines that will be displayed as an item lore to the player. Allows color codes using the § character
     * @param commands A list of commands to be executed when the player reads the notification. Available placeholders:
     * {name} => the players username
     * {uuid} => the players uuid
     * NOTE: you may use: sudo {name} {command} to run the command as the user
     */
    static void sendNotification(UUID receiver, List<String> lines, List<String> commands, Closure success) {
        MySQL.getAsyncDatabase().executeUpdate("INSERT INTO notifications (uuid_least, uuid_most, date, notif_lines, notif_commands, is_read, is_archived, notif_unique, server_id, commands_ran) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", {statement ->
            statement.setLong(1, receiver.leastSignificantBits)
            statement.setLong(2, receiver.mostSignificantBits)
            statement.setLong(3, System.currentTimeMillis())
            statement.setString(4, lines.join("<!!>"))
            statement.setString(5, commands.join("<!!>"))
            statement.setBoolean(6, false)
            statement.setBoolean(7, false)
            statement.setString(8, UUID.randomUUID().toString().replace("-", "").substring(0, 16))
            statement.setString(9, Temple.templeId)
            statement.setBoolean(10, false)
        }, {result ->
            if (result != 0) {
                Redis.get().publish("notification_sent", receiver.toString())
                success.call()
            }
        })
    }

    static void notifyPlayer(Player player) {
        if (player == null) {return}

        showNotifications(player, player.uniqueId, NotificationType.UNREAD)
    }
}