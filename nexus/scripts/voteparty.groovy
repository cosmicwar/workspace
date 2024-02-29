package scripts

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Schedulers
import org.apache.commons.lang3.mutable.MutableLong
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.legacy.utils.ThreadUtils
import scripts.shared.utils.Temple
import scripts.shared3.Redis

import java.util.logging.Logger

(Globals.GLOBAL_VOTES ? MySQL.getGlobalAsyncDatabase() : MySQL.getGlobalSyncDatabase()).execute("CREATE TABLE IF NOT EXISTS votes_registered (votes BIGINT NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(server_id))")

int votesNeeded = Exports.get("votesneeded", 150)

MutableLong votes = new MutableLong(0)

ThreadUtils.runAsync {
    Logger logger = Starlight.plugin.getLogger()
    logger.info("Loading votes...")

    (Globals.GLOBAL_VOTES ? MySQL.getGlobalAsyncDatabase() : MySQL.getGlobalSyncDatabase()).executeQuery("SELECT votes FROM votes_registered WHERE server_id = ?", { statement ->
        statement.setString(1, Temple.templeId)
    }, { result ->
        if (result.next()) {
            votes = new MutableLong(result.getLong(1))
            println "Loaded vote: ${votes.intValue()}"
        }
    })
    logger.info("Loaded votes!")
}

(Globals.GLOBAL_VOTES ? Redis.getGlobal() : Redis.get()).subscribe({ channel, message ->
    Schedulers.sync().run {
        votes.increment()

        long voted = votes.intValue()

        if (voted % votesNeeded == 0) {
            (Globals.GLOBAL_VOTES ? Redis.getGlobal() : Redis.get()).publish("voteparty_rewards", "")
        }
        (Globals.GLOBAL_VOTES ? MySQL.getGlobalAsyncDatabase() : MySQL.getGlobalSyncDatabase()).execute("INSERT INTO votes_registered (votes, server_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE votes = VALUES(votes)", { statement ->
            statement.setLong(1, voted)
            statement.setString(2, Temple.templeId)
        })
    }
}, "vote_registered")

(Globals.GLOBAL_VOTES ? Redis.getGlobal() : Redis.get()).subscribe({ channel, message ->
    if (message == "UPDATE") {
        String response = "_RESPONSE ${votes.intValue() % votesNeeded}/${votesNeeded}"
        (Globals.GLOBAL_VOTES ? Redis.getGlobal() : Redis.get()).publish("voteparty_response", response)
        return
    }
    String response = "§] §8» §e${NumberUtils.format(votesNeeded - votes.intValue() % votesNeeded)} §fvotes needed until vote party!"
    (Globals.GLOBAL_VOTES ? Redis.getGlobal() : Redis.get()).publish("voteparty_response", "${message} ${response.replace(" ", "\0")}")
}, "voteparty_requested")