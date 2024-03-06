package scripts.features.ticket

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import org.bson.Document
import scripts.Globals
import scripts.database.mongo.Mongo
import scripts.utils.Callback
import scripts.utils.Gson

class Ticket {

//    ticket_id, member, channel, time

    final TicketType type

    final int ticketId
    final Long creatorId
    final Long channelId
    final Long time

    Ticket(int ticketId, long creatorId, long channelId, TicketType type, long time) {
        this.ticketId = ticketId
        this.creatorId = creatorId
        this.channelId = channelId
        this.type = type
        this.time = time
    }

    Ticket(int ticketId, long creatorId, long channelId, String type, long time) {
        this.ticketId = ticketId
        this.creatorId = creatorId
        this.channelId = channelId
        this.type = TicketType.valueOf(type)
        this.time = time
    }

    void create(Callback<Boolean> callback) {
        Mongo.getGlobal().async { mongo -> mongo.getCollection(Globals.TICKET_COLLECTION).insertOne(Document.parse(Gson.gson.toJson(this))).with {
            if (it.wasAcknowledged()) callback.exec(true)
            else callback.exec(false)
        }}
    }

    void update(TicketStatus status) {
        Mongo.getGlobal().async {mongo ->
            mongo.getCollection(Globals.TICKET_COLLECTION).updateOne(Filters.eq("creatorId", creatorId), Updates.set("status", status), new UpdateOptions().upsert(true))
        }
    }

    void close(long closeMember, String transcript) {

    }

    static void getTicketByChannelId(long id, Callback<Ticket> callback) {
        Mongo.getGlobal().async { mongo ->
            def document = mongo.getCollection(Globals.TICKET_COLLECTION).find(Filters.eq("channelId", id)).first()

            if (document != null) {
                Ticket ticket = Gson.gson.fromJson(document.toJson(), Ticket.class);
                callback.exec(ticket)
            } else {
                callback.exec(null) // Ticket not found
            }

        }
    }

//    static void getTicketByChannelId(long id) {
//        Mongo.getGlobal().async { mongo ->
//            mongo.getCollection(Globals.APPLICATION_COLLECTION).find(Filters.eq("channelId", id)).first()
//        }
//    }

//    void create(Callback<Boolean> callback) {
//        MySQL.getGlobalAsyncDatabase().executeUpdate('INSERT INTO discord_tickets (ticket_id, member, type, channel, time) VALUES (?, ?, ?, ?, ?)', { statement ->
//            statement.setInt(1, ticketId as int)
//            statement.setLong(2, creatorId)
//            statement.setString(3, type.toString())
//            statement.setLong(4, channelId)
//            statement.setLong(5, time)
//        }, {
//            if (it == 0) {
//                println("Failed to create ticket ${ticketId}.")
//                callback.exec(false)
//            } else {
//                println("Successfully created ticket ${ticketId}.")
//                callback.exec(true)
//            }
//        })
//    }
//
//    void close(long closeMember, String transcript) {
//        MySQL.getGlobalAsyncDatabase().executeUpdate('UPDATE discord_tickets SET close_member = ?, close_time = NOW(), transcript = ? WHERE ticket_id = ?', { statement ->
//            statement.setLong(1, closeMember)
//            statement.setString(2, transcript)
//            statement.setInt(3, ticketId)
//        }, {
//            if (it == 0) {
//                println("Failed to close ticket ${ticketId}.")
//            } else {
//                println("Successfully closed ticket ${ticketId}.")
//            }
//        })
//    }
}