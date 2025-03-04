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
                "url": "jdbc:mariadb://127.0.0.1:3306/starcade",
                "username": "starcade",
                "password": "eQ8fZtyIp3uN6FAZ0P87JW3BVbx5AEHU",
                "aliases": []
        ],
        [
                "name": "global",
                "url": "jdbc:mariadb://127.0.0.1:3306/global",
                "username": "global",
                "password": "eQ8fZtyIp3uN6FAZ0P87JW3BVbx5AEHU",
                "aliases": []
        ]
])

Redis.register("local", new HelperRedis(RedisCredentials.of("127.0.0.1", 6379, "Hpkfp2jkn4+yKLi501GU5TY4UGEcVV7mobOKFAz32oSGKSos6FymAPF29Cn8+roRZc9bCMJCuFQLKDu4")))
Redis.register("global", new HelperRedis(RedisCredentials.of("127.0.0.1", 6379, "Hpkfp2jkn4+yKLi501GU5TY4UGEcVV7mobOKFAz32oSGKSos6FymAPF29Cn8+roRZc9bCMJCuFQLKDu4")))

Mongo.register("local", new HelperMongo(MongoDatabaseCredentials.of("127.0.0.1", 27017, "admin", "starcade", "gAWbxWHHenIFWfBcpzLuLB33KhdyVfDO")), "starcade")
Mongo.register("global", new HelperMongo(MongoDatabaseCredentials.of("127.0.0.1", 27017, "admin", "starcade", "gAWbxWHHenIFWfBcpzLuLB33KhdyVfDO")), "starcade")



