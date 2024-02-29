package scripts.factions.core.faction.chat

enum ChatMode {

    PUBLIC("public", "global", "p", "pc"),
    ALLY("ally", "a", "allychat", "ac"),
    TRUCE("truce", "t", "trucechat", "tc"),
    FACTION("faction", "f", "factionchat", "fc"),
    NONE("none", "n", "nochat", "nc")

    List<String> aliases = []

    ChatMode(String... aliases) {
        this.aliases.addAll(aliases)
    }

    ChatMode getNext() {
        def next = this.ordinal() + 1
        if (next >= values().length) {
            next = 0
        }
        return values()[next]
    }

    ChatMode getPrevious() {
        def previous = this.ordinal() - 1
        if (previous < 0) {
            previous = values().length - 1
        }
        return values()[previous]
    }

}