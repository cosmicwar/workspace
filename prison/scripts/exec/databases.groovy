package scripts.exec

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.mongo.MongoDatabaseCredentials
import org.starcade.starlight.helper.mongo.plugin.HelperMongo
import org.starcade.starlight.helper.redis.RedisCredentials
import org.starcade.starlight.helper.redis.plugin.HelperRedis
import scripts.shared3.Mongo
import scripts.shared3.Psql
import scripts.shared3.Redis

Exports.ptr("mysql_creds", [
        [
                "name": "default",
                "url": "jdbc:mariadb://104.243.38.125:3306/starcade",
                "username": "starcade",
                "password": "rgSxzPVOjdYFLBVMPmrF5oeq1",
                "aliases": []
        ],
        [
                "name": "global",
                "url": "jdbc:mariadb://104.243.38.125:3306/global",
                "username": "global",
                "password": "rgSxzPVOjdYFLBVMPmrF5oeq1",
                "aliases": []
        ]
])

Redis.register("local", new HelperRedis(RedisCredentials.of("104.243.38.125", 6391, "gsO3jbDE68Y8nfPXZ508Jjr7fiDI7exZttjNTmp59K1ynJhhgB")))
Redis.register("global", new HelperRedis(RedisCredentials.of("104.243.38.125", 6391, "gsO3jbDE68Y8nfPXZ508Jjr7fiDI7exZttjNTmp59K1ynJhhgB")))

Mongo.register("local", new HelperMongo(MongoDatabaseCredentials.of("104.243.38.125", 27037, "admin", "starcade", "DiIqy0ygmj4B9JrO4DNjknQmV")), "starcade")
Mongo.register("global", new HelperMongo(MongoDatabaseCredentials.of("104.243.38.125", 27037, "admin", "starcade", "DiIqy0ygmj4B9JrO4DNjknQmV")), "starcade")

