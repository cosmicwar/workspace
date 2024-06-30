package scripts.exec

import org.starcade.starlight.helper.mongo.MongoDatabaseCredentials
import org.starcade.starlight.helper.mongo.plugin.HelperMongo
import org.starcade.starlight.helper.redis.RedisCredentials
import org.starcade.starlight.helper.redis.plugin.HelperRedis
import scripts.shared3.Mongo
import scripts.shared3.Redis

Redis.register("local", new HelperRedis(RedisCredentials.of("104.243.38.125", 6391, "Hpkfp2jkn4+yKLi501GU5TY4UGEcVV7mobOKFAz32oSGKSos6FymAPF29Cn8+roRZc9bCMJCuFQLKDu4")))
Redis.register("global", new HelperRedis(RedisCredentials.of("104.243.38.125", 6391, "Hpkfp2jkn4+yKLi501GU5TY4UGEcVV7mobOKFAz32oSGKSos6FymAPF29Cn8+roRZc9bCMJCuFQLKDu4")))

//left same
Mongo.register("local", new HelperMongo(MongoDatabaseCredentials.of("104.243.38.125", 27335, "admin", "starcade", "gAWbxWHHenIFWfBcpzLuLB33KhdyVfDO")), "starcade")
Mongo.register("global", new HelperMongo(MongoDatabaseCredentials.of("104.243.38.125", 27335, "admin", "starcade", "gAWbxWHHenIFWfBcpzLuLB33KhdyVfDO")), "starcade")

