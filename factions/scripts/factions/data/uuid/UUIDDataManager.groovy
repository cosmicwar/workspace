package scripts.factions.data.uuid

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.configuration.CodecRegistry
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.bucket.Bucket
import org.starcade.starlight.helper.bucket.factory.BucketFactory
import org.starcade.starlight.helper.bucket.partitioning.PartitioningStrategies
import scripts.shared.utils.BukkitUtils
import scripts.shared3.Mongo

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@CompileStatic(TypeCheckingMode.SKIP)
class UUIDDataManager {

    static Bucket<UUID> bucket = BucketFactory.newConcurrentBucket(1000, PartitioningStrategies.lowestSize())

    static Map<String, RegisteredData> registeredDatas = new ConcurrentHashMap<>()
    static Map<Class<? extends UUIDDataObject>, RegisteredData> datasByClass = new ConcurrentHashMap<>()

    UUIDDataManager() {
        GroovyScript.addUnloadHook {
            registeredDatas.values().each { it.saveAll() }
        }

        Schedulers.async().runRepeating({
            Set<UUID> storedIds = bucket.asCycle().next()
            registeredDatas.values().each { it.save(storedIds) }
        }, 50L, TimeUnit.MILLISECONDS, 50L, TimeUnit.MILLISECONDS)

        Events.subscribe(PlayerQuitEvent.class).handler {
            Player player = it.getPlayer()

            registeredDatas.values().each { it.save(Collections.singleton(player.getUniqueId())) }
            bucket.remove(player.getUniqueId())
        }

        Events.subscribe(PlayerJoinEvent.class).handler {
            Player player = it.getPlayer()

            bucket.add(player.getUniqueId())
        }

        bucket.addAll(BukkitUtils.getOnlineNonSpoofPlayers().findResults { it.getUniqueId() })
    }

    static void register(String id, Class<? extends UUIDDataObject> uuidDataClass) {
        register(id, uuidDataClass, null)
    }

    static void register(String id, Class<? extends UUIDDataObject> uuidDataClass, CodecRegistry codecRegistry) {
        RegisteredData registeredData = new RegisteredData(id, uuidDataClass, codecRegistry)
        registeredDatas.put(id, registeredData)
        datasByClass.clear()

        registeredData.loadAll()
    }

    static <T extends UUIDDataObject> T getData(Player player, Class<T> uuidDataClass, boolean create = true) {
        return getData(player.getUniqueId(), uuidDataClass, create)
    }

    static <T extends UUIDDataObject> T getData(UUID id, Class<T> uuidDataClass, boolean create = true) {
        if (create) {
            return (T) getByClass(uuidDataClass)?.storedData?.computeIfAbsent(id, v -> uuidDataClass.getDeclaredConstructor(UUID.class).newInstance(id))
        } else {
            return (T) getByClass(uuidDataClass)?.storedData?.get(id)
        }
    }

    static <T extends UUIDDataObject> Collection<T> getAllData(Class<T> uuidDataClass) {
        return getByClass(uuidDataClass)?.storedData?.values() as Collection<T>
    }

    static RegisteredData getByClass(Class<? extends UUIDDataObject> uuidDataClass) {
        return datasByClass.computeIfAbsent(uuidDataClass, v -> registeredDatas.find { it.value.uuidDataClass == uuidDataClass }?.getValue())
    }

    static void removeOne(UUID playerId, Class<? extends UUIDDataObject> uuidDataClass) {
        getByClass(uuidDataClass)?.delete(playerId)
    }

    static void wipe(Class<? extends UUIDDataObject> uuidDataClass) {
        getByClass(uuidDataClass)?.deleteAll()
    }

    static class RegisteredData {

        static final ReplaceOptions REPLACE_OPTIONS = new ReplaceOptions().upsert(true)

        final String id
        final Class<? extends UUIDDataObject> uuidDataClass
        final MongoCollection<? extends UUIDDataObject> mongoCollection

        Map<UUID, UUIDDataObject> storedData = new ConcurrentHashMap<>()

        RegisteredData(String id, Class<? extends UUIDDataObject> uuidDataClass, CodecRegistry codecRegistry) {
            this.id = id
            this.uuidDataClass = uuidDataClass

            MongoCollection<? extends UUIDDataObject> mongoCollection = Mongo.get().getTempleCollection("uuid_$id".toString(), uuidDataClass)
            if (codecRegistry != null) {
                mongoCollection = mongoCollection.withCodecRegistry(codecRegistry)
            }
            this.mongoCollection = mongoCollection
        }

        RegisteredData(String id, Class<? extends UUIDDataObject> uuidDataClass) {
            this(id, uuidDataClass, null)
        }

        void delete(UUID id) {
            storedData.remove(id)

            Schedulers.async().run {
                mongoCollection?.deleteOne(Filters.eq("_id", id))
            }
        }

        void deleteAll() {
            storedData.clear()
            mongoCollection?.drop()
        }

        void save(Set<UUID> playerIds, boolean force = false) {
            playerIds.each { playerId ->
                save(playerId, force)
            }
        }

        void save(UUID id, boolean force = false) {
            UUIDDataObject UUIDDataObject = storedData.get(id)
            if (UUIDDataObject == null) {
                return
            }

            if (!UUIDDataObject.requiresSaving && !force) {
                return
            }

            UUIDDataObject.requiresSaving = false

            if (UUIDDataObject.isEmpty()) {
                mongoCollection.deleteOne(Filters.eq("_id", id))
            } else {
                mongoCollection.replaceOne(Filters.eq("_id", id), UUIDDataObject, REPLACE_OPTIONS)
            }
        }

        void saveAll(boolean force = false) {
            storedData.entrySet().findAll { (it.value.requiresSaving || force) }.each {
                it.value.requiresSaving = false

                if (it.value.isEmpty()) {
                    mongoCollection.deleteOne(Filters.eq("_id", it.key))
                } else {
                    mongoCollection.replaceOne(Filters.eq("_id", it.key), it.value, REPLACE_OPTIONS)
                }
            }
        }

        void loadAll() {
            storedData.clear()

            mongoCollection.find().each {
                storedData.put(it.id, it)
                bucket.add(it.id)
            }
        }

    }

}
