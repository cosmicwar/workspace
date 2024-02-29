package scripts.factions.content.log.v2.api

enum LogType
{
    UNKNOWN,
    PLAYER(true),
    FACTION(true),
    ANTI_CHEAT

    boolean stored = false

    LogType(boolean stored = false) {
        this.stored = stored
    }
}
