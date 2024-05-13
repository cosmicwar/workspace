package scripts.exec

import org.starcade.starlight.helper.mongo.MongoDatabaseCredentials
import org.starcade.starlight.helper.mongo.plugin.HelperMongo
import org.starcade.starlight.helper.redis.RedisCredentials
import org.starcade.starlight.helper.redis.plugin.HelperRedis
import scripts.shared3.Mongo
import scripts.shared3.Redis

Redis.register("local", new HelperRedis(RedisCredentials.of("172.93.103.228", 6391, "Fd12Ez7TS5bVeL2xDsAiWyH2JPdZUSGO7KVYi9hQJVoLe03j5HQSZhSrG3/XczxMOCmgHFKUdkxvsi4m")))
Redis.register("global", new HelperRedis(RedisCredentials.of("172.93.103.228", 6391, "Fd12Ez7TS5bVeL2xDsAiWyH2JPdZUSGO7KVYi9hQJVoLe03j5HQSZhSrG3/XczxMOCmgHFKUdkxvsi4m")))

Mongo.register("local", new HelperMongo(MongoDatabaseCredentials.of("172.93.103.228", 27335, "admin", "starcade", "gAWbxWHHenIFWfBcpzLuLB33KhdyVfDO")), "starcade")
Mongo.register("global", new HelperMongo(MongoDatabaseCredentials.of("172.93.103.228", 27335, "admin", "starcade", "gAWbxWHHenIFWfBcpzLuLB33KhdyVfDO")), "starcade")

