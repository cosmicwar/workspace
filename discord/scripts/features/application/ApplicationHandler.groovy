package scripts.features.application

import com.mongodb.client.model.Filters
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.jetbrains.annotations.NotNull
import scripts.Globals
import scripts.database.mongo.Mongo
import scripts.utils.Gson

import java.awt.*
import java.time.Instant
import java.util.List

class ApplicationHandler extends ListenerAdapter {

    static boolean open = Globals.APPLICATION_OPEN
    private static Long messageId = null

    @Override
    void onReady(ReadyEvent event) {
        EmbedBuilder applicationEmbed = new EmbedBuilder()

        applicationEmbed.setTitle("✎ **Starcade Applications**")
        applicationEmbed.setColor(Color.BLUE)
        applicationEmbed.setDescription(
        """
        **Welcome to Starcade Applications!** \n
        \n
        Application Status: ${open ? "**OPEN**" : "**CLOSED**"}
        \n
        """)
        applicationEmbed.setFooter("starcade.org", event.getJDA().getSelfUser().getAvatarUrl())

        TextChannel channel = event.getJDA().getGuildById(Globals.MAIN_GUILD_ID).getTextChannelById(Globals.APPLICATION_CHANNEL_ID)
        channel.getHistoryFromBeginning(1).queue {
            List<Message> messages = it.getRetrievedHistory()
            if (messages.size()  ==  1) {
                def msg = messages[0]
                msg.editMessageEmbeds(applicationEmbed.build()).setActionRow(Button.success("application", "Click to Apply")).queue {
                    messageId = it.getIdLong()
                }
            } else if (messages.size() == 0) {
                channel.sendMessageEmbeds(applicationEmbed.build()).addActionRow(Button.success("application", "Click to Apply")).queue {
                    messageId = it.getIdLong()
                }
            } else {
                messages.each { it.delete().queue() }
                channel.sendMessageEmbeds(applicationEmbed.build()).addActionRow(Button.success("application", "Click to Apply")).queue {
                    messageId = it.getIdLong()
                }
            }
        }
    }

    @Override
    void onButtonInteraction(ButtonInteractionEvent event) {
        if (open) {
            if (event.getButton().getId() == "application") {
                if (hasApplication(event.getMember().getIdLong())) {
                    event.reply("You already have an application").setEphemeral(true).queue()
                    return
                }

                TextInput username = TextInput.create("username", "1) Username & Age", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Respond with your in-game name followed by your age.")
                        .setRequired(true)
                        .build()

                TextInput hours = TextInput.create("hours", "2) Hours & Time-Zone", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Respond with how many hours you can contribute to our server followed by your timezone.")
                        .setRequired(true)
                        .build()

                TextInput why = TextInput.create("why", "3) Why & Experience", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Respond with why you are interested in being staff followed by any past experience.")
                        .setRequired(true)
                        .build()

                TextInput other = TextInput.create("other", "4) Other Information", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Anything else you would like to add?")
                        .setRequired(true)
                        .build()

                event.replyModal(Modal.create("application", "Staff Application")
                        .addActionRows(ActionRow.of(username), ActionRow.of(hours), ActionRow.of(why), ActionRow.of(other))
                        .build()
                ).queue()
            } else if (event.getButton().getId() == "accept_application") {
                def application = Application.getApplicationByChannelId(event.getChannel().getIdLong())
                event.reply("Application accepted").setEphemeral(true).queue()
            } else if (event.getButton().getId() == "deny_application") {
                event.reply("Application denied").setEphemeral(true).queue()
            }
        }
    }

    @Override
    void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId() == "application") {
            if (open) {
                Member member = event.getMember()

                String ign = event.getValue("username").getAsString()
                String hours = event.getValue("hours").getAsString()
                String why = event.getValue("why").getAsString()
                String other = event.getValue("other").getAsString()

                TextChannel applicationChannel = event.getGuild()
                        .createTextChannel("${member.getUser().getEffectiveName()} - Staff Application", event.getGuild().getCategoryById(Globals.APPLICATION_CATEGORY_ID))
                        .addRolePermissionOverride(event.getGuild().getPublicRole().idLong, null, [Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND])
                        .addPermissionOverride(member, [Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_ATTACH_FILES], null)
                        .complete()

                event.reply(":white_check_mark: Hey ${member.asMention}! Your application has been created, ${applicationChannel.asMention}.").setEphemeral(true).complete()

                Button acceptApplication = Button.success("accept_application", "Accept")
                Button declineApplication = Button.danger("deny_application", "Deny")

                def embed = new EmbedBuilder()
                        .setColor(Color.BLUE)
                        .setDescription(
                        """
                        **Staff Application**
                        """)
                        .setTimestamp(Instant.now())
                        .addField("**1) Username & Age**", ign, false)
                        .addField("**2) Hours & Time-Zone**", hours, false)
                        .addField("**3) Why & Experience**", why, false)
                        .addField("**4) Other Information**", other, false)
                        .setFooter(event.getMember().getUser().getEffectiveName(), event.getMember().getUser().getAvatarUrl())
                        .build()

                applicationChannel.sendMessageEmbeds(embed).addActionRow(acceptApplication, declineApplication).queue()

                new Application(member.getIdLong(), applicationChannel.getIdLong()).create {success ->
                    if (!success) {
                        applicationChannel.delete().queue {
                            event.reply(":x: An error occurred while creating your application. Please try again later.").setEphemeral(true).queue()
                            println("Error while creating application for ${member.user.name} (${member.idLong})")
                        }
                    }
                }
            }
        }
    }

    static boolean hasApplication(Long userId) {
        Mongo.getGlobal().sync {mongo ->
            mongo.getCollection(Globals.APPLICATION_COLLECTION).find(Filters.eq("userId", userId)).each {
                def application = Gson.gson.fromJson(Gson.gson.toJson(it), Application)
                return application.status == ApplicationStatus.WAITING
            }
        }
    }


}