package scripts.factions.content.log.v2.api

enum LogFilterType {
    ALL("§aAll", true),
    FACTION("§eFaction", true, LogType.FACTION),
    PLAYER("§ePlayer", true, LogType.PLAYER),
    ANTI_CHEAT("§eAnti-Cheat", false, LogType.ANTI_CHEAT),
    NONE("§cNone", false)


    String prefix
    LogType targetType
    boolean storedType

    LogFilterType(String prefix, boolean storedType = false, LogType targetType = null) {
        this.prefix = prefix
        this.storedType = storedType
        this.targetType = targetType
    }

    LogFilterType getNext(boolean stored = false) {
        switch (this) {
            case ALL:
                return FACTION
            case FACTION:
                return PLAYER
            case PLAYER:
                if (stored) {
                    return ALL
                }

                return ANTI_CHEAT
            case ANTI_CHEAT:
                return NONE
            case NONE:
                return ALL
            default:
                return ALL
        }
    }

    LogFilterType getPreviousType(boolean stored = false) {
        switch (this) {
            case ALL:
                if (stored) {
                    return PLAYER
                }

                return NONE
            case FACTION:
                return ALL
            case PLAYER:
                return FACTION
            case ANTI_CHEAT:
                return PLAYER
            case NONE:
                return ANTI_CHEAT
            default:
                return ALL
        }
    }
}