package scripts.database.redis

import io.lettuce.core.KeyScanCursor
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisFuture
import io.lettuce.core.ScanArgs
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import scripts.Globals

class RedisManager {

    RedisClient redisClient
    StatefulRedisConnection<String, String> redisConnection
    StatefulRedisPubSubConnection<String, String> redisPubSub
    Hashtable<String, OnMessage> listeners = new Hashtable<>()

    RedisManager(RedisClient redisClient) {
        this.redisClient = redisClient
        redisConnection = redisClient.connect()
        redisPubSub = redisClient.connectPubSub()

        RedisManagerListener listener = new RedisManagerListener(this)
        redisPubSub.addListener(listener)
    }

    Set<String> getKeys(String query) {
        ScanArgs sa = new ScanArgs().match(query)
        KeyScanCursor<String> cursor = redisConnection.async().scan(sa).get()
        Set<String> keys = new HashSet<>(cursor.getKeys())
        while (!cursor.isFinished()) {
            cursor = redisConnection.async().scan(cursor, sa).get()
            keys.addAll(cursor.getKeys())
        }
        return keys
    }

    RedisFuture<Long> publish(String channel, String message) {
        return redisConnection.async().publish(channel, message)
    }

    void subscribe(String channel, OnMessage m) {
        redisPubSub.sync().subscribe(channel)
        addListener(channel, m)
    }

    void psubscribe(String pattern, OnMessage m) {
        redisPubSub.sync().psubscribe(pattern)
        addListener(pattern, m)
    }

    void psubscribe(String[] patterns, OnMessage m) {
        for (String pattern : patterns) {
            redisPubSub.sync().psubscribe(pattern)
            addListener(pattern, m)
        }
    }

    private void addListener(String channel, OnMessage m) {
        OnMessage old = listeners.get(channel)
        if (old != null) {
            LinkingEventProxy proxy = new LinkingEventProxy(m, old)
            listeners.put(channel, proxy)
        } else {
            listeners.put(channel, m)
        }
    }

    void reconnect() {
        try {
            if (redisConnection != null) {
                redisConnection.close()
            }
            redisConnection = this.redisClient.connect()
            redisConnection.sync().auth(Globals.REDIS_PASSWORD)
        } catch (Exception e) {
            // uh oh.
            e.printStackTrace()
        }
    }

    class RedisManagerListener implements RedisPubSubListener<String, String> {
        private RedisManager redis

        RedisManagerListener(RedisManager redis) {
            this.redis = redis
        }

        @Override
        void message(String channel, String message) {
            var e = redis.getListeners().get(channel)
            if (e != null) e.message(channel, message)
        }

        @Override
        void message(String pattern, String channel, String message) {
            var e = redis.getListeners().get(pattern)
            if (e != null) e.message(channel, message)
        }

        @Override
        void subscribed(String s, long l) {
        }

        @Override
        void psubscribed(String s, long l) {
        }

        @Override
        void unsubscribed(String s, long l) {
        }

        @Override
        void punsubscribed(String s, long l) {
        }

    }

}

