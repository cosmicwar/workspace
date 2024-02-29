package scripts

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Schedulers
import org.bukkit.entity.Player
import scripts.shared.legacy.database.mysql.MySQL

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

TrackingUtils.init()

class TrackingUtils {
    static HashMap<String, Integer> joins = new ConcurrentHashMap<>()

    static void init() {
        MySQL.getGlobalAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS tracking_joins_hub (joins INT NOT NULL, total_players INT NOT NULL, insert_time DATETIME NOT NULL, PRIMARY KEY(insert_time))")
        MySQL.getGlobalAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS tracking_joins_v2 (joins INT NOT NULL, hostname VARCHAR(32) NOT NULL, insert_time DATETIME NOT NULL)")

        Exports.ptr("tracking/adduniquejoin", { Player player, Runnable callback ->
            if (!player.hasPlayedBefore()) {
//                MySQL.getGlobalAsyncDatabase().executeQuery("SELECT * FROM latest_ips WHERE uuid_least = ? AND uuid_most = ?", { statement ->
//                    statement.setLong(1, player.getUniqueId().leastSignificantBits)
//                    statement.setLong(2, player.getUniqueId().mostSignificantBits)
//                }, { result ->
//                    if (!result.next()) {
//                        joins.merge("unique", 1, (a, b) -> a + b)
//                    }
//                    callback.run()
//                })
            } else {
                callback.run()
            }
        })

        Exports.ptr("tracking/adduniqueipjoin", { UUID uuid, String hostname ->
            if (hostname.contains("192") || hostname.contains("185")) return
            Starlight.log.info("${uuid} HAS JOINED USING ${hostname}")

            Calendar calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)

            MySQL.getGlobalAsyncDatabase().executeQuery("SELECT date FROM latest_ips WHERE uuid_least = ? AND uuid_most = ?", { statement ->
                statement.setLong(1, uuid.leastSignificantBits)
                statement.setLong(2, uuid.mostSignificantBits)
            }, { result ->

                if (result.next()) {
                    if (!(new Date(result.getLong("date"))).before(calendar.getTime())) return
                }

                joins.merge(hostname + ".total", 1, (a, b) -> a + b)
            })

            MySQL.getGlobalAsyncDatabase().executeQuery("SELECT * FROM latest_ips WHERE uuid_least = ? AND uuid_most = ?", { statement ->
                statement.setLong(1, uuid.leastSignificantBits)
                statement.setLong(2, uuid.mostSignificantBits)
            }, { result ->
                if (!result.next()) {
                    joins.merge(hostname + ".unique", 1, (a, b) -> a + b)
                }
            })
        })

        Schedulers.async().runRepeating({
            Starlight.log.info("SAVING METRICS")
            insertJoins()
        }, 10, TimeUnit.SECONDS, 5, TimeUnit.MINUTES)

//        Events.subscribe(PlayerJoinEvent.class).handler({ event ->
//            //noinspection GrUnresolvedAccess
//            if (((CraftPlayer) event.getPlayer()).getHandle() instanceof FakeEntityPlayer) {
//                return
//            }
//
//            joins.merge("total", 1, (a, b) -> a + b)
//        })
    }

    static void insertJoins() {
        MySQL.getGlobalAsyncDatabase().executeBatch("INSERT INTO tracking_joins_v2 (joins, hostname, insert_time) VALUES (?, ?, NOW())", { statement ->
            joins.each {
                statement.setInt(1, it.value)
                statement.setString(2, it.key)
                statement.addBatch()
            }
            joins.clear()
        })
    }
}
