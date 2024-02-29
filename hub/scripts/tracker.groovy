package scripts

import com.destroystokyo.paper.event.player.PlayerHandshakeEvent
import com.destroystokyo.paper.profile.ProfileProperty
import com.google.gson.JsonObject
import com.mojang.util.UUIDTypeAdapter
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import scripts.shared.legacy.database.mysql.AsyncDatabase
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.wrappers.Gson
import scripts.shared.utils.Persistent
import scripts.shared3.Redis

import java.util.concurrent.ConcurrentHashMap

AsyncDatabase datababase = Globals.GLOBAL_TRACKING ? MySQL.getGlobalAsyncDatabase() : MySQL.getAsyncDatabase()

datababase.createTable("all_usernames", [
        "uuid_least": "BIGINT NOT NULL",
        "uuid_most" : "BIGINT NOT NULL",
        "username"  : "VARCHAR(16) NOT NULL",
        "date"      : "BIGINT UNSIGNED NOT NULL"
], ["uuid_least", "uuid_most", "username"])

datababase.createTable("latest_usernames", [
        "uuid_least": "BIGINT NOT NULL",
        "uuid_most" : "BIGINT NOT NULL",
        "username"  : "VARCHAR(16) NOT NULL",
        "date"      : "BIGINT UNSIGNED NOT NULL"
], ["uuid_least", "uuid_most"])

datababase.createTable("all_ips", [
        "uuid_least": "BIGINT NOT NULL",
        "uuid_most" : "BIGINT NOT NULL",
        "ip"        : "VARCHAR(15) NOT NULL",
        "date"      : "BIGINT UNSIGNED NOT NULL"
], ["uuid_least", "uuid_most", "ip"])

datababase.createTable("latest_ips", [
        "uuid_least": "BIGINT NOT NULL",
        "uuid_most" : "BIGINT NOT NULL",
        "ip"        : "VARCHAR(15) NOT NULL",
        "date"      : "BIGINT UNSIGNED NOT NULL"
], ["uuid_least", "uuid_most"])

datababase.createTable("latest_skins", [
        "uuid_least": "BIGINT NOT NULL",
        "uuid_most" : "BIGINT NOT NULL",
        "value"     : "TEXT CHARACTER SET utf8mb4 NOT NULL",
        "signature" : "TEXT CHARACTER SET utf8mb4 NOT NULL",
        "date"      : "BIGINT UNSIGNED NOT NULL"
], ["uuid_least", "uuid_most"])

Events.subscribe(PlayerJoinEvent.class, EventPriority.MONITOR).handler { event ->
    Player player = event.getPlayer()

    UUID uuid = player.getUniqueId()
    long least = uuid.getLeastSignificantBits()
    long most = uuid.getMostSignificantBits()

    String username = player.getName()
    String ip = player.getAddress().getHostString()
    long time = System.currentTimeMillis()

    (Exports.ptr("tracking/adduniquejoin") as Closure)?.call(player, {
        Schedulers.async().runLater({
            DatabaseUtils.getLatestUsername(uuid, { String latestUsername ->
                if (latestUsername != username) {
                    //has new ign?
                    Redis.getGlobal().publish("username_change", "${uuid.leastSignificantBits}|${uuid.mostSignificantBits}|${latestUsername}|${username}")
                }
            }, false)

            datababase.insert("all_usernames", [least, most, username, time] as Object[], "date = VALUES(date)")
            datababase.insert("latest_usernames", [least, most, username, time] as Object[], "username = VALUES(username), date = VALUES(date)")
            datababase.insert("all_ips", [least, most, ip, time] as Object[], "date = VALUES(date)")
            datababase.insert("latest_ips", [least, most, ip, time] as Object[], "ip = VALUES(ip), date = VALUES(date)")

            Iterator<ProfileProperty> iterator = player.getPlayerProfile().getProperties().iterator()

            if (iterator.hasNext()) {
                ProfileProperty property = iterator.next()
                datababase.insert("latest_skins", [least, most, property.getValue(), property.getSignature(), time] as Object[], "value = VALUES(value), signature = VALUES(signature), date = VALUES(date)")
            }
        }, 1)
    })
}

MySQL.getGlobalAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS srv_joins (player_uuid_least BIGINT NOT NULL, player_uuid_most BIGINT NOT NULL, srv VARCHAR(64) NOT NULL, insert_time DATETIME NOT NULL, PRIMARY KEY(player_uuid_least, player_uuid_most))")

Map<String, String> servers = [
        "sky.mcprison-server.dawn.gg"   : "pacific",
        "spoofy.mcprison-server.dawn.gg": "pacific",
        "henwy.mcprison-server.dawn.gg" : "pacific"
]

Events.subscribe(PlayerLoginEvent.class, EventPriority.MONITOR).handler({ event ->
    UUID uuid = event.getPlayer().getUniqueId()
    String hostname = event.getHostname().split(":")[0]

    if (event.result == PlayerLoginEvent.Result.ALLOWED) {
        (Exports.ptr("tracking/adduniqueipjoin") as Closure)?.call(uuid, hostname.toLowerCase())
        Schedulers.async().runLater({
            if (!event.getPlayer().isOnline()) return // quit instantly or bot attack
            JsonObject locationData = getGeoPlayer(event.getPlayer())
            String zip = locationData?.get("zip")?.getAsString()
            String regionName = locationData?.get("regionName")?.getAsString()
            String region = locationData?.get("region")?.getAsString()
            String city = locationData?.get("city")?.getAsString()
            float longitude = locationData?.get("lon")?.getAsFloat() ?: 0f
            float latitude = locationData?.get("lat")?.getAsFloat() ?: 0f
            String country = locationData?.get("country")?.getAsString() ?: null
            String countryCode = locationData?.get("countryCode")?.getAsString() ?: null
            String timezone = locationData?.get("timezone")?.getAsString() ?: null

            MySQL.getGlobalAsyncDatabase().execute("INSERT IGNORE INTO srv_joins " +
                    "(player_uuid_least, player_uuid_most, srv, longitude, latitude, country, countryCode, zip, regionName, region, city, timezone, insert_time) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "longitude = VALUES(longitude), " +
                    "latitude = VALUES(latitude), " +
                    "country = VALUES(country), " +
                    "countryCode = VALUES(countryCode), " +
                    "zip = VALUES(zip), " +
                    "regionName = VALUES(regionName), " +
                    "region = VALUES(region), " +
                    "city = VALUES(city), " +
                    "timezone = VALUES(timezone), " +
                    "insert_time = VALUES(insert_time)",
                    { statement ->
                        statement.setLong(1, uuid.leastSignificantBits)
                        statement.setLong(2, uuid.mostSignificantBits)
                        statement.setString(3, hostname.toLowerCase())
                        statement.setFloat(4, longitude)
                        statement.setFloat(5, latitude)
                        statement.setString(6, country)
                        statement.setString(7, countryCode)
                        statement.setString(8, zip)
                        statement.setString(9, regionName)
                        statement.setString(10, region)
                        statement.setString(11, city)
                        statement.setString(12, timezone)
                        Starlight.log.info("ADDING ${hostname.toLowerCase()} FOR ${uuid.toString()}")
                    })
        }, 3 * 20)
    }
})

static JsonObject getGeoPlayer(Player player) {
    try {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://ip-api.com/json/${player.address.getHostString() == "127.0.0.1" ? "1.1.1.1" : player.address.getHostString()}").openConnection()
        connection.setRequestMethod("GET")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setUseCaches(false)
        connection.setDoInput(true)
        connection.setDoOutput(true)
        def code = connection.getResponseCode()

        if (code == 200 || code == 201) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))
            StringBuilder builder = new StringBuilder()

            String line

            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n")
            }
            reader.close()

            JsonObject json = Gson.gson.fromJson(builder.toString(), JsonObject.class)
            return json
        } else {
            println "IP PARSING FAILED: ${connection.getResponseMessage()}"
            return null
        }
    } catch (Exception e) {
        e.printStackTrace()
        return null
    }
}

Map<UUID, String> redirects = Persistent.of("redirects", new ConcurrentHashMap<UUID, String>()).get()
Exports.ptr("redirects", redirects)

Events.subscribe(PlayerHandshakeEvent.class).handler { event ->
    String[] split = event.getOriginalHandshake().split("\00")

    if (split.length == 3 || split.length == 4) {
        String hostname = split[0]
        String ip = split[1]
        UUID uuid = UUIDTypeAdapter.fromString(split[2])

        event.setServerHostname(hostname)
        event.setSocketAddressHostname(ip)
        event.setUniqueId(uuid)

        String server = servers.get(hostname)

//        if (server != null) {
//            MySQL.getGlobalAsyncDatabase().execute("INSERT IGNORE INTO srv_joins (uuid_least, uuid_most, ip, srv, date) VALUES (?, ?, ?, ?, ?)", { statement ->
//                statement.setLong(1, uuid.getLeastSignificantBits())
//                statement.setLong(2, uuid.getMostSignificantBits())
//                statement.setString(3, ip)
//                statement.setString(4, hostname)
//                statement.setLong(5, System.currentTimeMillis())
//            })
//            redirects.put(uuid, server)
//        }
    } else {
        event.setFailMessage("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!")
        event.setCancelled(true)
        return;
    }
    if (split.length == 4) {
        event.setPropertiesJson(split[3])
    }
}

Events.subscribe(AsyncPlayerPreLoginEvent.class, EventPriority.MONITOR).handler { event ->
    if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
        redirects.remove(event.getUniqueId())
    }
}

Events.subscribe(PlayerLoginEvent.class, EventPriority.MONITOR).handler { event ->
    if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
        redirects.remove(event.getPlayer().getUniqueId())
    }
}

Events.subscribe(PlayerQuitEvent.class).handler { event ->
    redirects.remove(event.getPlayer().getUniqueId())
}