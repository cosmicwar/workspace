package scripts

import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Schedulers
import scripts.shared.content.systems.InteractionStreak
import scripts.shared.content.systems.InteractionTokens
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.utils.Exports2
import scripts.shared.utils.MojangAPI
import scripts.shared3.Redis

import java.util.concurrent.TimeUnit

class Interactions {
    static int CURRENT_VOTES = 0;

    Interactions() {
        Exports2.registerFor("Interactions", "player_vote_event") { String username ->
            MySQL.getGlobalAsyncDatabase().executeQuery("SELECT `uuid_least`,`uuid_most` FROM latest_usernames WHERE username = ? LIMIT 1", { st ->
                st.setString(1, username);
            }, { rs ->
                if (rs.next()) {
                    registerInteraction(new UUID(rs.getLong(2), rs.getLong(1)), InteractionTokens.TokenType.VOTE);
                } else {
                    if (!username.startsWith("*")) {
                        MySQL.getGlobalAsyncDatabase().executeQuery("SELECT `uuid_least`,`uuid_most` FROM latest_usernames WHERE username = ? LIMIT 1", { st ->
                            st.setString(1, "*" + username);
                        }, { rs2 ->
                            if (rs2.next()) {
                                registerInteraction(new UUID(rs2.getLong(2), rs2.getLong(1)), InteractionTokens.TokenType.VOTE);
                            } else {
                                println("[WARN] Could not find a uuid for ${username}")
                            }
                        });
                    } else {
                        println("[WARN] Could not find a uuid for ${username}")
                    }
                }
            });
        }

        Schedulers.async().runRepeating({
            Redis.getGlobal().publish("network_announcements", "§] §>  §fThere have been ${CURRENT_VOTES} votes in the past hour! Receive rewards by voting using /vote. " + (doubleVotes(System.currentTimeMillis()) ? "§7(Today is double vote day! Receive double the rewards.)" : ""));
            CURRENT_VOTES = 0;
        }, 1, TimeUnit.HOURS, 1, TimeUnit.HOURS);

        Commands.create().assertConsole().handler { c ->
            registerInteraction(MojangAPI.getUUID(c.rawArg(0)), InteractionTokens.TokenType.parse(c.rawArg(1)))
        }.register("dev/testinteraction")
    }

    void registerInteraction(UUID player, InteractionTokens.TokenType type) {
        println("registering interaction ${type.name()} for ${player}")

        int amount = 1;
        boolean doubleVotes = doubleVotes(System.currentTimeMillis());

        if (type == InteractionTokens.TokenType.VOTE) {
            CURRENT_VOTES++;

            InteractionStreak.updateStreak(InteractionStreak.SERVER_UUID);
            InteractionStreak.updateStreak(player);
            if (doubleVotes) {
                amount = 2;
            }
        }

        InteractionTokens.giveTokens(player, type, amount);

        if (type == InteractionTokens.TokenType.VOTE) {
            Redis.getGlobal().publish("player_message", "${player.toString()}\u0000§] §> §aYou have received vote rewards. View them using /vote. " + (doubleVotes ? "§7(Today is double vote day! Received double the rewards.)" : ""))
        } else {
            Redis.getGlobal().publish("player_message", "${player.toString()}\u0000§] §> §aYou have received ad rewards. View them using /ads.")
        }
    }

    static boolean doubleVotes(long timestamp) {
        Calendar calendar = Calendar.getInstance()
        calendar.setTimeInMillis(timestamp)
        int date = calendar.get(Calendar.DAY_OF_MONTH)
        if (date == 1 || date == 2 || date == 3) {
            return true
        }

        int day = calendar.get(Calendar.DAY_OF_WEEK)
        if (day == 1 || day == 6 || day == 7) {
            return true
        }

        return false
    }
}
