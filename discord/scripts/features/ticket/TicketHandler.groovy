package scripts.features.ticket

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.annotations.NotNull
import scripts.Bot
import scripts.Globals
import scripts.utils.Callback
import scripts.utils.StringUtils
import scripts.utils.TimeUtils
import scripts.utils.mysql.MySQL

import java.awt.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.List

//@Field JDA bot = (Exports.ptr("bot") as Closure<JDA>).call()
//bot.addEventListener(new ButtonTest())

class TicketHandler extends ListenerAdapter {

    private static Long messageId = null

    @Override
    void onReady(ReadyEvent event) {
        EmbedBuilder ticketEmbed = new EmbedBuilder()
        ticketEmbed.setTitle("🎫 **Starcade Tickets**")
        ticketEmbed.setColor(Color.BLUE) // Replace with your desired color
        ticketEmbed.setDescription(
                "**Welcome to Starcade Tickets!** \r\n" +
                        "If you're having an issue, simply fill out the form bellow let us know what kind of issue you're having!  🙂 \n"
        )

        def menu = StringSelectMenu.create("ticketType")

        TicketType.values().each {ticketType ->
            menu.addOption("${ticketType.emoji.getFormatted()} ${StringUtils.capitalize(ticketType.name)}", ticketType.toString())
        }

        TextChannel channel = event.getJDA().getGuildById(Globals.MAIN_GUILD_ID).getTextChannelById(Globals.TICKET_CHANNEL_ID)
        channel.getHistoryFromBeginning(1).queue {
            List<Message> messages = it.getRetrievedHistory()
            if (messages.size()  ==  1) {
                def msg = messages[0]
                msg.editMessageEmbeds(ticketEmbed.build()).setActionRow(menu.build()).queue {
                    messageId = it.getIdLong()
                }
            } else if (messages.size() == 0) {
                channel.sendMessageEmbeds(ticketEmbed.build()).setActionRow(menu.build()).queue {
                    messageId = it.getIdLong()
                }
            } else {
                messages.each { it.delete().queue() }
                channel.sendMessageEmbeds(ticketEmbed.build()).setActionRow(menu.build()).queue {
                    messageId = it.getIdLong()
                }
            }
        }

    }

    @Override
    void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId() == "ticketType") {
            TicketType ticketType = TicketType.valueOf(event.getValues().get(0))

            if (ticketType != null) {
                TextInput name = TextInput.create("username", "Username", TextInputStyle.SHORT)
                        .setPlaceholder("Enter your username...")
                        .setMinLength(2)
                        .setRequired(true)
                        .build()

                TextInput message = TextInput.create("issue", "Briefly describe your issue", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Enter your issue...")
                        .setMinLength(5)
                        .setMaxLength(100)
                        .setRequired(false)
                        .build()

                Modal modal = Modal.create("type_" + ticketType.toString(), "Ticket Support")
                        .addActionRows(ActionRow.of(name), ActionRow.of(message))
                        .build()

                event.replyModal(modal).queue()
            }
        }
    }

    @Override
    void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName() == "rename") {

        }
    }

    @Override
    void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        // handle ticket closes
        if (event.getButton().getId() == "close_ticket") closeTicket(event)
    }

    @Override
    void onModalInteraction(@NotNull ModalInteractionEvent event) {
        // ticket creation
        if (event.getModalId().startsWith("type_")) {
            String type = event.getModalId().split("type_")[1]
            TicketType ticketType = TicketType.valueOf(type)

            if (ticketType != null) {
                Member member = event.getMember()

                String username = event.getValue("username").getAsString()
                String issue = event.getValue("issue").getAsString() == "" ? "No Issue Provided." : event.getValue("issue").getAsString()

                int ticketId = (int) Math.floor(Math.random() * 90000) + 10000
                def staffRole = findRoleByName(event.getGuild(), "Staff Team")

                if (!staffRole) return

                TextChannel newTicketChannel
                if (ticketType.adminOnly) {
                    newTicketChannel = event.getGuild().createTextChannel("${ticketType.name}-${ticketId}", event.getGuild().getCategoryById(Globals.TICKET_CATEGORY_ID))
                            .addRolePermissionOverride(event.getGuild().getPublicRole().idLong, null, [Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND])
                            .addRolePermissionOverride(staffRole.idLong, null, [Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND])
                            .addPermissionOverride(member, [Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_ATTACH_FILES], null)
                            .complete()
                } else {
                    newTicketChannel = event.getGuild().createTextChannel("${ticketType.name}-${ticketId}", event.getGuild().getCategoryById(Globals.TICKET_CATEGORY_ID))
                            .addRolePermissionOverride(event.getGuild().getPublicRole().idLong, null, [Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND])
                            .addRolePermissionOverride(staffRole.idLong, [Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND], null)
                            .addPermissionOverride(member, [Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_ATTACH_FILES], null)
                            .complete()
                }

                event.reply(":white_check_mark: Hey ${member.asMention}! Your ticket has been created, ${newTicketChannel.asMention}.").setEphemeral(true).complete()

                Button closeTicket = Button.danger("close_ticket", "Close Ticket")

                def embed = new EmbedBuilder()
                        .setColor(Color.BLUE)
                        .setDescription("**${member.user.name}'s ${ticketType.name}** ticket\n\nPlease wait patiently, our staff will assist you soon.\n")
                        .setTimestamp(Instant.now())
                        .addField("**Username**", "`$username`", true)
                        .addField("**Issue**", "`$issue`", true)
                        .setFooter(ticketType as String, Bot.getStartedBot().getSelfUser().getAvatarUrl())
                        .build()

                newTicketChannel.sendMessageEmbeds(embed).addActionRow(closeTicket).complete()

                new Ticket(ticketId, member.idLong, newTicketChannel.idLong, ticketType, System.currentTimeMillis()).create {success ->
                    if (!success) {
                        newTicketChannel.delete().queue {
                            event.reply(":x: An error occurred while creating your ticket. Please try again later.").setEphemeral(true).queue()
                            println("Error while creating ticket for ${member.user.name} (${member.idLong})")
                        }
                    }
                }
            }
        }
    }

    static Role findRoleByName(guild, roleName) {
        for (Role role : guild.getRoles()) {
            if (role.getName() == roleName) {
                return role
            }
        }
        return null
    }

    static def closeTicket(ButtonInteractionEvent event) {
        def reactor = event.getMember()
        def channel = event.getMessageChannel()
        def channelName = channel.getName()
        def channelNameSplit = channelName.split('-')
        def ticketId = channelNameSplit[1] as Integer
        def ticketLogChannel = event.getGuild().getTextChannelById(Globals.TICKET_LOG_CHANNEL_ID)

        if (!reactor.roles.any { it.name in ["Staff Team", "*", "**"] }) return

        channel.history.retrievePast(100).queue { results ->
            def messages = []
            results.each { result ->
                def name
                if (result.author) {
                    name = "${result.author.name}#${result.author.discriminator}"
                } else {
                    name = result.author
                }
                def content = result.getContentRaw()
                if (content && content.length() > 0) {
                    messages << "${name}: ${clean(content)}"
                }
            }

            messages << "${channelName} closed on ${new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())}"
            messages.reverse()

            def transcript = messages.join('\n')

            Ticket.getTicketByChannelId(channel.idLong, { Ticket ticket ->
                if (ticket == null) {
                    event.reply(":x: An error occurred while closing your ticket. Please try again later.").setEphemeral(true).complete()
                    println("Error while closing ticket ${ticketId} (ticket not found)")
                    return
                }

                ticket.close(reactor.idLong, transcript)

                // Get the current working directory where the JAR file is located - will have to redo this with new code structure
                String currentDirectory = System.getProperty("user.dir")

                File directory = new File(currentDirectory, "tickets")
                if (!directory.exists()) directory.mkdir() // force create directory if it doesn't exist

                File file = new File(directory, "${channelName}.txt")
                try {
                    def fileWriter = new FileWriter(file)
                    fileWriter.write(transcript)

                    fileWriter.close()
                } catch (Exception exception) {
                    exception.printStackTrace()
                }

                def ticketAlert = new EmbedBuilder()
                        .setTitle("Ticket #${ticket.getTicketId()}")
                        .setColor(Color.BLUE)
                        .setDescription(
                                """
                            **Author: ** <@${ticket.creatorId}>
                            **Closed By: ** <@${reactor.user.id}>
                            **Duration: ** ${TimeUtils.formatTime(System.currentTimeMillis() - ticket.time)}
                            **Channel: ** ${channelName}
                        """
                                        .trim())
                        .setTimestamp(Instant.now())
                        .setFooter("starcade.org", Bot.getStartedBot().selfUser.getAvatarUrl())
                        .build()

                ticketLogChannel.sendMessage("").addEmbeds(ticketAlert).addFiles(FileUpload.fromData(file)).queue()

                def member = event.guild.getMemberById(ticket.creatorId)
                if (member)
                    member.user.openPrivateChannel().flatMap {
                        it.sendMessage("Your ticket has been closed. Please find the transcript below.")
                        it.sendFiles(FileUpload.fromData(file)).queue({
                            println("Ticket DM sent successfully.")
                        }) { error ->
                            println("Could not send Ticket DM.")
                            error.printStackTrace()
                        }
                    }
                else
                    println("No member for ticket to be sent to.")

                channel.delete().queue()
            })
        }
    }

//    static void getTicket(int ticketId, MessageChannel channel, Callback<Ticket> callback) {
//        try {
//            MySQL.getGlobalAsyncDatabase().executeQuery('SELECT * FROM discord_tickets WHERE ticket_id = ? AND channel = ?', { statement ->
//                statement.setInt(1, ticketId)
//                statement.setLong(2, channel.idLong)
//            }, { result ->
//                if (result.next()) callback.exec((new Ticket(ticketId, result.getLong("member"), result.getLong("channel"), result.getString("type"), result.getLong("time"))))
//            })
//        } catch (Exception exception) {
//            exception.printStackTrace()
//        }
//    }

    static String clean(String string) {
        string = string.replaceAll(/[\u2700-\u27BF\uE000-\uF8FF\uD83C\uD83D\u2011-\u26FF\uD83E]/, '')
        string = string.replaceAll(/[^\x00-\x7F]/, '')
        return string
    }

}



