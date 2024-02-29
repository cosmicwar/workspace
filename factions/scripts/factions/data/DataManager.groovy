package scripts.factions.data

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.configuration.CodecRegistry
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Schedulers
import scripts.shared3.Mongo

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@CompileStatic(TypeCheckingMode.SKIP)
class DataManager {

    static Map<String, RegisteredData> registeredDatas = new ConcurrentHashMap<>()
    static Map<Class<? extends DataObject>, RegisteredData> datasByClass = new ConcurrentHashMap<>()

    DataManager() {
        GroovyScript.addUnloadHook {
            registeredDatas.values().each { it.saveAll() }
        }

        Schedulers.async().runRepeating({
            registeredDatas.values().each { it.saveAll() }
        }, 200L, TimeUnit.MILLISECONDS, 200L, TimeUnit.MILLISECONDS)
    }

    static void register(String id, Class<? extends DataObject> playerDataClass) {
        register(id, playerDataClass, null)
    }

    static void register(String id, Class<? extends DataObject> playerDataClass, CodecRegistry codecRegistry) {
//        RegisteredData existing = registeredDatas.remove(id)
//        if (existing != null) {
//            existing.saveAll()
//        }

        RegisteredData registeredData = new RegisteredData(id, playerDataClass, codecRegistry)
        registeredDatas.put(id, registeredData)
        datasByClass.clear()

        registeredData.loadAll()
    }

    static <T extends DataObject> T getData(String objectId, Class<T> dataObject, boolean create = true) {
        if (create) return (T) getByClass(dataObject)?.objectData?.computeIfAbsent(objectId, v -> dataObject.getDeclaredConstructor(String.class).newInstance(objectId))
        else return (T) getByClass(dataObject)?.objectData?.get(objectId)
    }

    static <T extends DataObject> Collection<T> getAllData(Class<T> dataObject) {
        return getByClass(dataObject)?.objectData?.values() as Collection<T>
    }

    static RegisteredData getByClass(Class<? extends DataObject> dataObject) {
        return datasByClass.computeIfAbsent(dataObject, v -> registeredDatas.find { it.value.dataObject == dataObject }?.getValue())
    }

    static void removeOne(String id, Class<? extends DataObject> dataObject) {
        getByClass(dataObject)?.delete(id)
    }

    static void wipe(Class<? extends DataObject> dataObject) {
        getByClass(dataObject)?.deleteAll()
    }

    static class RegisteredData {

        static final ReplaceOptions REPLACE_OPTIONS = new ReplaceOptions().upsert(true)

        final String id
        final Class<? extends DataObject> dataObject
        final MongoCollection<? extends DataObject> mongoCollection

        Map<String, DataObject> objectData = new ConcurrentHashMap<>()

        RegisteredData(String id, Class<? extends DataObject> dataObject, CodecRegistry codecRegistry) {
            this.id = id
            this.dataObject = dataObject

            MongoCollection<? extends DataObject> mongoCollection = Mongo.get().getTempleCollection("objects_$id".toString(), this.dataObject)
            if (codecRegistry != null) {
                mongoCollection = mongoCollection.withCodecRegistry(codecRegistry)
            }
            this.mongoCollection = mongoCollection
        }

        RegisteredData(String id, Class<? extends DataObject> dataObject) {
            this(id, dataObject, null)
        }

        void delete(String id) {
            objectData.remove(id)

            Schedulers.async().run {
                mongoCollection?.deleteOne(Filters.eq("_id", id))
            }
        }

        void deleteAll() {
            objectData.clear()
            mongoCollection?.drop()
        }

        void save(Set<String> ids) {
            ids.each { id ->
                DataObject dataObject = objectData.get(id)
                if (dataObject == null || !dataObject.requiresSaving) {
                    return
                }

                dataObject.requiresSaving = false

                if (dataObject.isEmpty()) {
                    mongoCollection.deleteOne(Filters.eq("_id", id))
                } else {
                    mongoCollection.replaceOne(Filters.eq("_id", id), dataObject, REPLACE_OPTIONS)
                }
            }
        }

        void saveAll(boolean force = false) {
            objectData.entrySet().findAll { (it.value.requiresSaving || force) }.each {
                it.value.requiresSaving = false

                if (it.value.isEmpty()) {
                    mongoCollection.deleteOne(Filters.eq("_id", it.key))
                } else {
                    mongoCollection.replaceOne(Filters.eq("_id", it.key), it.value, REPLACE_OPTIONS)
                }
            }
        }

        void loadAll() {
            objectData.clear()

            mongoCollection.find().each {
                objectData.put(it.id, it)
            }
        }

    }

}
