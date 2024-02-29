package scripts.features.ticket

import net.dv8tion.jda.api.entities.emoji.Emoji

enum TicketType {

    GENERAL("general",
            Emoji.fromUnicode("🧾"),
            "How can we help you today?",
    ),
    REPORT("report",
            Emoji.fromUnicode("⛓️"),
            "Please provide us with the username of the person you are reporting, as well as proof of the offense.",
    ),
    APPEAL("appeal",
            Emoji.fromUnicode("📝"),
            "Please provide us with a reason as to why you should be unbanned. As well as why you were banned in the first place."
    ),
    BUG("bug",
            Emoji.fromUnicode("🐛"),
            "How can we help you today?"
    ),
    LEADER("leader",
            Emoji.fromUnicode("👑"),
            "Thank you for opening a leader inquiry. Please provide us with some basic information about your faction/island/gang.",
            true
    );

    String name
    Emoji emoji
    String openingMessage
    boolean adminOnly

    TicketType(String name, Emoji emoji, String openingMessage, boolean adminOnly = false) {
        this.name = name
        this.emoji = emoji
        this.openingMessage = openingMessage
        this.adminOnly = adminOnly
    }
}

