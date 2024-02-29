package scripts.features

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.jetbrains.annotations.NotNull
import scripts.Globals

import java.awt.Color
import java.time.Instant

class ChangeLogHandler extends ListenerAdapter {

    @Override
    void onGuildReady(@NotNull GuildReadyEvent event) {
        if (event.getGuild().getIdLong() == Globals.MAIN_GUILD_ID) {
            event.getGuild().updateCommands().addCommands(
                    Commands.slash("changelog", "Create a new change log.")
                            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
            ).queue()
        }
    }

    @Override
    void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName() == "changelog") {
            event.replyModal(createModal("changelog", "Change-log Creation")).queue()
        }
    }

    @Override
    void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId() == "changelog") {
            String title = event.getValue("title").getAsString()
            String description = event.getValue("description").getAsString()

            if (title.contains(":starcade:")) {
                title = title.replace(":starcade:", "<:starcade:1160114351739523183>")
            }

            if (description.contains(":starcade:")) {
                description = description.replace(":starcade:", "<:starcade:1160114351739523183>")
            }

            EmbedBuilder builder = new EmbedBuilder()
            builder.setTitle(title)
            builder.setColor(Color.BLUE)
            builder.setDescription(description)
            builder.setTimestamp(Instant.now())
            builder.setFooter("${event.getMember().user.getName()}", event.getMember().user.getAvatarUrl())

            Button create = Button.success("changelog_create", "Create")
            Button cancel = Button.danger("changelog_cancel", "Cancel")

            event.reply("").addEmbeds(builder.build()).setActionRow([create, cancel]).setEphemeral(true).queue()
        }
    }

    @Override
    void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("changelog")) {

            if (event.getComponentId().endsWith("create")) {
                event.editComponents(ActionRow.of(
                        Button.success("created_success", "Created").asDisabled(),
                        Button.danger("created_danger", "Created").asDisabled()
                )).queue()
                event.getGuild().getTextChannelById(Globals.CHANGE_LOG_CHANNEL_ID).sendMessage("<@everyone>").addEmbeds(event.getMessage().getEmbeds()).queue()
            }
            else if (event.getComponentId().endsWith("cancel")) {
                event.editComponents(ActionRow.of(
                        Button.success("cancelled_success", "Cancelled").asDisabled(),
                        Button.danger("cancelled_danger", "Cancelled").asDisabled()
                )).queue()}
        }
    }

    static Modal createModal(String id, String title) {
        TextInput name = TextInput.create("title", "Title", TextInputStyle.SHORT)
                .setPlaceholder("Enter the title...")
                .setMinLength(5)
                .setRequired(true)
                .build()

        TextInput message = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Enter your issue...")
                .setMinLength(5)
                .setRequired(true)
                .build()

        Modal modal = Modal.create("${id}", "${title}")
                .addActionRows(ActionRow.of(name), ActionRow.of(message))
                .build()

        return modal
    }
}