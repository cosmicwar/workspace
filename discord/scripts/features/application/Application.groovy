package scripts.features.application

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import org.bson.Document
import scripts.Globals
import scripts.database.mongo.Mongo
import scripts.utils.Callback
import scripts.utils.Gson

class Application {

    final long userId
    final long channelId

    ApplicationStatus status = ApplicationStatus.WAITING

    Application(long userId, long channelId) {
        this.userId = userId
        this.channelId = channelId
    }

    void create(Callback<Boolean> callback) {
        Mongo.getGlobal().async {mongo -> mongo.getCollection(Globals.APPLICATION_COLLECTION).insertOne(Document.parse(Gson.gson.toJson(this))).with {
            if (it.wasAcknowledged()) callback.exec(true)
            else callback.exec(false)
        }}
    }

    void update(ApplicationStatus status) {
        Mongo.getGlobal().async {mongo ->
            mongo.getCollection(Globals.APPLICATION_COLLECTION).updateOne(Filters.eq("userId", userId), Updates.set("status", status), new UpdateOptions().upsert(true))
        }
    }

    //TODO make this work (return application object)
    static def getApplicationByChannelId(long id, Callback<Application> callback) {

        Mongo.getGlobal().async { mongo ->
            def document = mongo.getCollection(Globals.APPLICATION_COLLECTION).find(Filters.eq("channelId", id)).first()

            if (document != null) {
                Application application = Gson.gson.fromJson(document.toJson(), Application.class);
                callback.exec(application)
            } else {
                callback.exec(null) // Ticket not found
            }
        }
    }

}

