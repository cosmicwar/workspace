package scripts.features.invites

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.UpdateOptions
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Invite
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.entities.User
import org.bson.Document
import org.jetbrains.annotations.NotNull
import scripts.Globals
import scripts.database.mongo.Mongo
import scripts.utils.Callback

import java.awt.Color
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap


import static java.time.temporal.ChronoUnit.DAYS;


class InviteHandler extends ListenerAdapter {

    private final Map<String, Map<String, CachedInvite>> inviteCache = new ConcurrentHashMap<>()
    private static final long FAKE_INVITE_DAYS_OFFSET = 14

    private static Long messageId = 1214992378956349521

    static void updateUserInvites(String userId, int realInvites, int fakeInvites) {
        Mongo.getGlobal().sync(mongo -> {
            Document query = new Document("userId", userId)
            Document update = new Document("\$set", new Document("realInvites", realInvites).append("fakeInvites", fakeInvites).append("userId", userId))
            mongo.getCollection(Globals.INVITE_COLLECTION).updateOne(query, update, new UpdateOptions().upsert(true))
        })
    }

    static void storeInviter(String inviterId, String invitedId, Boolean real) {
        Mongo.getGlobal().sync(mongo -> {
            Document query = new Document("userId", invitedId)
            Document update = new Document("\$set", new Document("inviter", inviterId).append("real", real).append("invited", invitedId))
            mongo.getCollection(Globals.INVITERS).updateOne(query, update, new UpdateOptions().upsert(true))
        })
    }

    static def getInviter(String invitedId, Callback<Document> callback) {
        Mongo.getGlobal().sync(mongo ->
                callback.exec(mongo.getCollection(Globals.INVITERS).find(Filters.eq("invited", invitedId)).first())
        )
    }

    static def getUserInvites(String userId, Callback<Document> callback) {
        Mongo.getGlobal().sync(mongo ->
                callback.exec(mongo.getCollection(Globals.INVITE_COLLECTION).find(Filters.eq("userId", userId)).first())
        )
    }

    @Override
    void onGuildReady(GuildReadyEvent event) {
        if (event.getGuild().idLong != Globals.MAIN_GUILD_ID) return
        createLeaderboard(event)
        this.cacheGuildInvites(event.getGuild());
    }

    @Override
    void onGuildInviteCreate(GuildInviteCreateEvent event) {
        if (event.getGuild().idLong != Globals.MAIN_GUILD_ID) return
        this.cacheGuildInvites(event.getGuild());
    }

    @Override
    void onGuildInviteDelete(GuildInviteDeleteEvent event) {
        if (event.getGuild().idLong != Globals.MAIN_GUILD_ID) return
        this.cacheGuildInvites(event.getGuild());
    }

    @Override
    void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (event.getGuild().idLong != Globals.MAIN_GUILD_ID) return
        final User user = event.getUser()
        if (user.isBot()) return

        this.handleWelcome(event.getGuild(), user) {
            createLeaderboard(event)
        }
    }

    @Override
    void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        final User user = event.getUser()
        if (user.isBot()) return
        this.handleFarewell(user) {
            createLeaderboard(event)
        }

    }

    @Override
    void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName() == "invites") {
            getUserInvites(event.user.id) { doc ->
                if (doc == null) doc = new Document()

                Integer real = doc.getOrDefault("realInvites", 0) as Integer
                event.replyEmbeds(new EmbedBuilder().setTitle("${event.user.name} - ${real} invites.").build()).setEphemeral(true).queue()
            }
        } else if (event.getName() == "inviteleaderboard") {
            def sortedUsers
            Mongo.getGlobal().sync { mongo ->
                sortedUsers = mongo.getCollection(Globals.INVITE_COLLECTION).find().sort(Sorts.descending("realInvites"))
            }

            if (sortedUsers == null) println("ERROR could not retrieve sortedUsers")

            String leaderboard = "\n"
            ArrayList<String> places = ["🥇", "🥈", "🥉", "🎖️"]
            int usersOnLeaderBoard = 10
            int counter = 0
            for (Document doc : sortedUsers) {
                if (counter > usersOnLeaderBoard) break
                String userId = doc.get("userId")
                String place = counter < 3 ? places[counter] : places[3]
                Integer invites =  doc.getOrDefault("realInvites", 0) as Integer
                leaderboard += place + " " + event.getJDA().getUserById(userId).name + " | " + invites + " ${invites == 1 ? "invite" : "invites"} \n"
                counter++
            }

            EmbedBuilder inviteEmbed = new EmbedBuilder()
            inviteEmbed.setTitle("📩 **Starcade Invites**")
            inviteEmbed.setColor(Color.BLUE)
            inviteEmbed.setDescription(
                    "**Welcome to Starcade Invites!** \r\n Bellow are the invite rankings, reach the top for rewards!\n" + leaderboard
            )

            event.replyEmbeds(inviteEmbed.build()).setEphemeral(true).queue()
        }
    }

    private static boolean isFakeInvite(User user) {
        final String avatar = user.getAvatarUrl()
        final OffsetDateTime timeCreated = user.getTimeCreated()
        final long days = DAYS.between(timeCreated, OffsetDateTime.now())
        return days < FAKE_INVITE_DAYS_OFFSET && avatar == null
    }

    private void handleWelcome(Guild guild, User user, Callback<Boolean> success = {}) {
        guild.retrieveInvites().queue((invites) -> {
            final Map<String, CachedInvite> cachedInvites = inviteCache.get(guild.getId())
            this.cacheGuildInvites(guild, invites)
            final Map<String, CachedInvite> newInvites = inviteCache.get(guild.getId())

            CachedInvite inviteUsed = null

            for (Map.Entry<String,CachedInvite> entry : newInvites.entrySet()) {
                String code = entry.getKey()
                CachedInvite invite = entry.getValue()

                if (!cachedInvites.containsKey(code)) {
                    if (invite.uses == 1) {
                        inviteUsed = invite
                        break
                    }
                } else {
                    if (invite.uses > cachedInvites.get(code).uses) {
                        inviteUsed = invite
                        break
                    }
                }
            }

            if (inviteUsed == null) {
                println("ERROR could not find guild")
                return
            }
            if (inviteUsed.inviter != null) {
                User inviter = inviteUsed.inviter

                getUserInvites(inviter.id) {doc ->
                    if (doc == null) doc = new Document()

                    Integer real = doc.getOrDefault("realInvites", 0) as Integer
                    Integer fake = doc.getOrDefault("fakeInvites", 0) as Integer

                    if (isFakeInvite(user)) {
                        storeInviter(inviter.id, user.id, false)
                        updateUserInvites(inviter.id, real, fake + 1)

                        success.exec(false)
                    } else {
                        storeInviter(inviter.id, user.id, true)
                        updateUserInvites(inviter.id, real + 1, fake)

                        success.exec(true)
                    }
                }
            }
        })
    }

    private void handleFarewell(User user, Closure success = {}) {
        getInviter(user.id, {doc ->
            if (doc == null) return

            String inviterId = doc.get("inviter")
            if (inviterId == null) return

            Boolean realInvite = doc.get("real")

            getUserInvites(inviterId) {inviterDoc ->
                Integer real = inviterDoc.getOrDefault("realInvites", 0) as Integer
                Integer fake = inviterDoc.getOrDefault("fakeInvites", 0) as Integer

                if (realInvite) updateUserInvites(inviterId, real - 1, fake)
                else updateUserInvites(inviterId, real, fake - 1)

                success.run()
            }
        })
    }


    private void cacheGuildInvites(Guild guild) {
        guild.retrieveInvites().queue(invites -> cacheGuildInvites(guild, invites));
    }

    private void cacheGuildInvites(Guild guild, List<Invite> invites) {
        Map<String, CachedInvite> cache = new ConcurrentHashMap<>()
        for (Invite invite : invites) {
            cache.put(invite.getCode(), new CachedInvite(invite))
        }

        inviteCache.put(guild.getId(), cache)
    }


    static void createLeaderboard(GenericGuildEvent event) {

        def sortedUsers
        Mongo.getGlobal().sync { mongo ->
            sortedUsers = mongo.getCollection(Globals.INVITE_COLLECTION).find().sort(Sorts.descending("realInvites"))
        }

        if (sortedUsers == null) println("ERROR could not retrieve sortedUsers")

        String leaderboard = "\n"
        ArrayList<String> places = ["🥇", "🥈", "🥉", "🎖️"]
        int usersOnLeaderBoard = 10
        int counter = 0
        for (Document doc : sortedUsers) {
            if (counter > usersOnLeaderBoard) break
            String userId = doc.get("userId")
            String place = counter < 3 ? places[counter] : places[3]
            Integer invites = doc.getOrDefault("realInvites", 0) as Integer
            leaderboard += place + " " + event.getJDA().getUserById(userId).name + " | " + invites + " ${invites == 1 ? "invite" : "invites"} \n"
            counter++
        }

        EmbedBuilder inviteEmbed = new EmbedBuilder()
        inviteEmbed.setTitle("📩 **Starcade Invites**")
        inviteEmbed.setColor(Color.BLUE) // Replace with your desired color
        inviteEmbed.setDescription(
                "**Welcome to Starcade Invites!** \r\n Bellow are the invite rankings, reach the top for rewards!\n" + leaderboard
        )

        TextChannel inviteChannel = event.getJDA().getTextChannelById(Globals.INVITE_CHANNEL_ID)

        inviteChannel.getHistoryFromBeginning(5).queue {
            List<Message> messages = it.getRetrievedHistory()
            if (messages.size() < 3) {

                for (Message msg : messages) {
                    println(msg.getId())
                    try {
                        if (msg.isPinned()) continue
                        println(messageId + " - messageId")
                        msg.editMessageEmbeds(inviteEmbed.build()).queue {
                            messageId = it.getIdLong()
                        }
                    } catch (Exception ignored) {
                        println("msg not sent by bot edit failed")
                    }
                }
            } else if (messages.size() == 0) {

                inviteChannel.sendMessageEmbeds(inviteEmbed.build()).queue {
                    messageId = it.getIdLong()
                }
            } else {

                messages.each {
                    if (!it.isPinned()) it.delete().queue()
                }
                inviteChannel.sendMessageEmbeds(inviteEmbed.build()).queue {
                    messageId = it.getIdLong()
                }
            }
        }
    }
}