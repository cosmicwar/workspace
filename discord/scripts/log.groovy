package scripts

import net.dv8tion.jda.api.EmbedBuilder

import java.awt.Color
import java.time.Instant

static def log(String title, String message, long channelId = Globals.STAFF_ALERT_CHANNEL_ID) {
    Bot.getStartedBot().getTextChannelById(channelId)
            .sendMessage("").addEmbeds(getEmbed(title, message)).queue()
}

static def getEmbed (String title, String message) {
    return new EmbedBuilder()
            .setTitle(title)
            .setDescription(message)
            .addField("", "<@Staff>", true)
            .setColor(Color.BLUE)
            .setFooter("starcade.org", Bot.getStartedBot().selfUser.avatarUrl)
            .setTimestamp(Instant.now())
            .build()
}