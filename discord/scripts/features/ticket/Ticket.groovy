package scripts.features.ticket

import scripts.utils.Callback
import scripts.utils.mysql.MySQL

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
        MySQL.getGlobalAsyncDatabase().executeUpdate('INSERT INTO discord_tickets (ticket_id, member, type, channel, time) VALUES (?, ?, ?, ?, ?)', { statement ->
            statement.setInt(1, ticketId as int)
            statement.setLong(2, creatorId)
            statement.setString(3, type.toString())
            statement.setLong(4, channelId)
            statement.setLong(5, time)
        }, {
            if (it == 0) {
                println("Failed to create ticket ${ticketId}.")
                callback.exec(false)
            } else {
                println("Successfully created ticket ${ticketId}.")
                callback.exec(true)
            }
        })
    }

    void close(long closeMember, String transcript) {
        MySQL.getGlobalAsyncDatabase().executeUpdate('UPDATE discord_tickets SET close_member = ?, close_time = NOW(), transcript = ? WHERE ticket_id = ?', { statement ->
            statement.setLong(1, closeMember)
            statement.setString(2, transcript)
            statement.setInt(3, ticketId)
        }, {
            if (it == 0) {
                println("Failed to close ticket ${ticketId}.")
            } else {
                println("Successfully closed ticket ${ticketId}.")
            }
        })
    }
}