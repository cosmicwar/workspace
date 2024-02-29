package scripts.database.mongo

import com.mongodb.MongoClientOptions
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.internal.CodecRegistryHelper
import scripts.utils.Gson

import java.util.concurrent.ConcurrentHashMap

import static org.bson.codecs.configuration.CodecRegistries.fromProviders
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries

class Mongo {

    static Map<String, Mongo> instances = new ConcurrentHashMap<>()

    static Mongo register(String identifier, MongoImpl mongoImpl, String defaultDatabase) {
        Mongo mongo = new Mongo(mongoImpl, defaultDatabase)
        instances.put(identifier, mongo)

        return mongo
    }

    static Mongo get(String identifier = "local") {
        return instances.get(identifier)
    }

    static Mongo getGlobal() {
        return instances.get("global")
    }

    static MongoDatabase getWithDefaultDatabase(String identifier = "local") {
        Mongo mongo = instances.get(identifier)
        return mongo.getDatabase(mongo.defaultDatabase)
    }

    static CodecRegistry getCodecRegistry() {
        CodecRegistry registry = MongoClientOptions.builder().uuidRepresentation(UuidRepresentation.JAVA_LEGACY).build().getCodecRegistry()

        return CodecRegistryHelper.createRegistry(registry, UuidRepresentation.JAVA_LEGACY)
    }

    private final MongoImpl mongoImpl
    final String defaultDatabase


    Mongo(MongoImpl mongoImpl, String defaultDatabase) {
        this.mongoImpl = mongoImpl
        this.defaultDatabase = defaultDatabase
    }

    void sync(MongoTask task) {
        task.run(mongoImpl.getDatabase(defaultDatabase))
    }

    MongoDatabase getRaw() {
        return mongoImpl.getDatabase(defaultDatabase)
    }

    void async(MongoTask task) {
        mongoImpl.executor.execute {
            task.run(mongoImpl.getDatabase(defaultDatabase))
        }
    }

    static CodecProvider POJO_PROVIDER = PojoCodecProvider.builder().automatic(true).build()
    static CodecRegistry POGO_REGISTRY = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(POJO_PROVIDER))

    MongoDatabase getDatabase(String name) {
        return mongoImpl.getDatabase(name).withCodecRegistry(POGO_REGISTRY)
    }

    def <T> MongoCollection<T> getCollection(String database, String collection, Class<T> type) {
        return getDatabase(database).getCollection(collection, type)
    }

    interface MongoTask {
        void run(MongoDatabase mongo)
    }

}


