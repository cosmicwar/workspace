package scripts

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import scripts.database.mongo.Mongo
import scripts.database.mongo.MongoDatabaseCredentials
import scripts.database.mongo.MongoImpl
import scripts.database.redis.Redis
import scripts.database.redis.RedisManager
import scripts.features.ChangeLogHandler

import scripts.features.application.ApplicationHandler
import scripts.features.invites.InviteHandler
import scripts.features.join.JoinHandler
import scripts.features.ticket.TicketHandler
import scripts.features.SyncHandler

import java.time.Duration

//TODO: redo discord syncing to allow for better discord integration
class Bot {

    private static JDA jda

    static void start() {
        Runtime.getRuntime().addShutdownHook(new Thread({
            stopBot()
        }));

        {
            RedisClient redisClient = RedisClient.create(RedisURI.builder().withHost(Globals.REDIS_HOST).withPort(Globals.REDIS_PORT).withTimeout(Duration.ofMinutes(1)).withPassword(Globals.REDIS_PASSWORD.toCharArray()).build())
            Redis.register("global", new RedisManager(redisClient))
            Redis.register("local", new RedisManager(redisClient))

            Mongo.register("global", new MongoImpl(MongoDatabaseCredentials.of(Globals.MONGO_HOST, Globals.MONGO_PORT, Globals.MONGO_DATABASE, Globals.MONGO_USERNAME, Globals.MONGO_PASSWORD)), Globals.MONGO_DEFAULT_DATABASE)
            Mongo.register("local", new MongoImpl(MongoDatabaseCredentials.of(Globals.MONGO_HOST, Globals.MONGO_PORT, Globals.MONGO_DATABASE, Globals.MONGO_USERNAME, Globals.MONGO_PASSWORD)), Globals.MONGO_DEFAULT_DATABASE)
        }

        getStartedBot().updateCommands().addCommands(
                Commands.slash("sync", "Sync your discord account with your minecraft account.")
                        .addOption(OptionType.STRING, "token", "The code provided from in-game.", true),
                Commands.slash("unsync", "Un-sync your discord account."),
                Commands.slash("invites", "Get your personal invite statistics."),
                Commands.slash("rename", "Rename a ticket.")
                        .addOption(OptionType.STRING, "name", "The new name.", true),
                Commands.slash("suggest", "Suggest something.")
                        .addOption(OptionType.STRING, "suggestion", "The suggestion.", true),
        ).queue()

        getStartedBot().addEventListener(new SyncHandler(), new TicketHandler(), new JoinHandler(), new ChangeLogHandler(), new ApplicationHandler(), new InviteHandler())
    }

    static JDA getStartedBot() {
        if (jda == null) {
            jda = JDABuilder.createDefault(Globals.DISCORD_TOKEN)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.MESSAGE_CONTENT)
                    .setActivity(Activity.playing("play.starcade.org"))
                    .build()
        }

        return jda
    }

    static void stopBot() {
        if (jda != null) {
            jda.shutdown()
            jda = null
        }
    }
}
