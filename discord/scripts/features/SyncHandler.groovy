package scripts.features

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import scripts.database.redis.Redis
import scripts.utils.mysql.MySQL

import java.sql.Date

class SyncHandler extends ListenerAdapter {

    @Override
    void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName() == "sync") {
            if (event.getOption("token") != null) {
                event.deferReply()
                String token = event.getOption("token").getAsString()

                MySQL.getGlobalAsyncDatabase().executeQuery("SELECT * FROM discord_sync_tokens WHERE token = ?", { statement ->
                    statement.setString(1, token)
                }, { result ->
                    if (result.next()) {
                        MySQL.getGlobalAsyncDatabase().executeUpdate("DELETE FROM discord_sync_tokens WHERE token = ?", { statement ->
                            statement.setString(1, token)
                        }, {})

                        MySQL.getGlobalAsyncDatabase().executeUpdate("INSERT INTO discord_sync_state (uuid_least, uuid_most, discord_id, last_updated) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE discord_id = VALUES(discord_id)", { statement ->
                            statement.setLong(1, result.getLong("uuid_least"))
                            statement.setLong(2, result.getLong("uuid_most"))
                            statement.setString(3, event.getUser().getId())
                            statement.setDate(4, new Date(System.currentTimeMillis()))
                        }, {
                            Redis.getGlobal().publish("discord_sync_complete", "${result.getLong("uuid_least")}|${result.getLong("uuid_most")}|${event.getUser().getName()}")
                            event.reply("Successfully synced your discord account!").setEphemeral(true).queue()
                        })
                    } else {
                        event.reply("That is not a valid code!").setEphemeral(true).queue()
                    }
                })
            }
        } else if (event.getName() == "unsync") {
            event.deferReply()
            try {
                MySQL.getGlobalAsyncDatabase().executeUpdate("DELETE FROM discord_sync_state WHERE discord_id = ?", { statement ->
                    statement.setString(1, event.getUser().getId())
                }, {
                    Redis.getGlobal().publish("discord_unsync_complete", event.getUser().getId())
                    event.reply("Successfully un-synced your discord account!").setEphemeral(true).queue()
                })
            } catch (Exception ignored) {
                event.reply("You are not synced!").setEphemeral(true).queue()
            }
        }
    }
}