package scripts.utils

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import scripts.utils.mysql.MySQL

import java.awt.Color
import java.time.Instant
import java.util.concurrent.TimeUnit

class BotUtils {

    static boolean isSynced(long discordId) {
        MySQL.getGlobalAsyncDatabase().executeQuery("SELECT * FROM `discord_sync_state` WHERE `discord_id` = ? LIMIT 1", {
            it.setLong(1, discordId)
        }, { result ->
            return true
        })
        return false
    }

    static void say(Guild guild, TextChannel channel, String message, long timeout = -1) {
        EmbedBuilder builder = new EmbedBuilder()
        builder.setColor(Color.BLUE)
        builder.setDescription(message)
        if (timeout > 0) {
            Message embedMessage = guild.getTextChannelById(channel.idLong).sendMessage("").addEmbeds(builder.build()).queue {
                it.delete().queueAfter(timeout, TimeUnit.SECONDS)
            }
        }
        else
            guild.getTextChannelById(channel.idLong).sendMessage("").addEmbeds(builder.build()).queue()
    }

    static void alert(Guild guild, TextChannel channel, String message, Role role = null) {
        EmbedBuilder builder = new EmbedBuilder()
        builder.setColor(Color.BLUE)
        builder.setDescription(message)
        builder.setTimestamp(Instant.now())
        builder.setFooter("Starcade.org", guild.getIconUrl())

        guild.getTextChannelById(channel.idLong).sendMessage(role != null ? "<@&${role.id}>" : "").addEmbeds(builder.build()).queue()
    }

}