package scripts.database.mongo

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoDatabase
import dev.morphia.Datastore
import dev.morphia.Morphia
import dev.morphia.mapping.MapperOptions
import org.bson.UuidRepresentation
import scripts.Bot
import scripts.utils.ManagedThreadFactory

import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class MongoImpl {

    private final MongoClient client
    private final MongoDatabase database
    private final Morphia morphia
    private final Datastore morphiaDatastore

    private Executor executor
    private ThreadPoolExecutor ownedExecutor

    MongoImpl(MongoDatabaseCredentials credentials) {
        MongoCredential mongoCredential = MongoCredential.createCredential(
                credentials.getUsername(),
                credentials.getDatabase(),
                credentials.getPassword().toCharArray()
        )

        this.client = new MongoClient(
                new ServerAddress(credentials.getAddress(), credentials.getPort()),
                mongoCredential,
                MongoClientOptions.builder().uuidRepresentation(UuidRepresentation.JAVA_LEGACY).build()
        )
        this.database = this.client.getDatabase(credentials.getDatabase())
        this.morphia = new Morphia()
        this.morphiaDatastore = this.morphia.createDatastore(this.client, credentials.getDatabase())
        this.morphia.getMapper().setOptions(MapperOptions.builder().classLoader(Bot.class.getClassLoader()).build())

        this.ownedExecutor = new ThreadPoolExecutor(
                2,
                20,
                30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ManagedThreadFactory("Discord Database Executor - %d"),
                { runnable, executor -> runnable.run() }
        )
        this.executor = this.ownedExecutor
    }


    MongoClient getClient() {
        return this.client
    }


    MongoDatabase getDatabase() {
        return this.database
    }

    MongoDatabase getDatabase(String name) {
        return this.client.getDatabase(name)
    }

    void close() {
        // Don't close
        /*if (this.client != null) {
            this.client.close();
        }*/
    }

    Morphia getMorphia() {
        return this.morphia
    }

    Datastore getMorphiaDatastore() {
        return this.morphiaDatastore
    }

    Datastore getMorphiaDatastore(String name) {
        return this.morphia.createDatastore(this.client, name)
    }

    Executor getExecutor() {
        return this.executor
    }
}


