package scripts.database.redis

import io.lettuce.core.RedisFuture
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.sync.RedisCommands
import scripts.utils.Gson

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Function

class Redis {
    static Map<String, Redis> instances = new ConcurrentHashMap<>()

    static Redis register(String identifier, RedisManager redisManager) {
        Redis redis = new Redis(redisManager)
        instances.put(identifier, redis)

        return redis
    }

    static Redis get(String identifier = "local") {
        return instances.get(identifier)
    }

    static Redis getGlobal() {
        return instances.get("global")
    }

    private final RedisManager redisManager

    Redis(RedisManager redisManager) {
        this.redisManager = redisManager
    }

    synchronized void async(Function<RedisAsyncCommands<String, String>, RedisFuture> function) {
        try {
            function.apply(redisManager.redisConnection.async())?.exceptionally { error ->
                System.err.println("Exception running async command: " + error.getMessage());
                error.printStackTrace()
            }
        } catch (Throwable error) {
            error.printStackTrace()
            // looks like that the process was not finished, we reconnect to prevent any issues due to unfinished cursor iterations.
            redisManager.reconnect()
        }
    }

    synchronized void sync(Consumer<RedisCommands<String, String>> consumer) {
        try {
            consumer.accept(redisManager.redisConnection.sync())
        } catch (Throwable error) {
            System.err.println("Exception running sync command: " + error.getMessage());
            error.printStackTrace()
            redisManager.reconnect()
        }
    }

    void publish(String channel, String message) {
        async { commands ->
            commands.publish(channel, message)
        }
    }

    void publish(String channel, Object message) {
        publish(channel, Gson.gson.toJson(message))
    }

    void subscribe(scripts.database.redis.OnMessage onMessage, String... channels) {
        for (String channel : channels) {
            redisManager.subscribe(channel, onMessage)
        }
    }
}