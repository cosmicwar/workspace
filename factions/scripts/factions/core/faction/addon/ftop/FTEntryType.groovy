package scripts.factions.core.faction.addon.ftop

enum FTEntryType {
    SPAWNER_VALUE(ValueType.MONEY),
    CONQUEST(ValueType.POINTS),
    OUTPOST(ValueType.POINTS),
    DARKZONE(ValueType.POINTS);

    ValueType type

    FTEntryType(ValueType type) {
        this.type = type
    }
}

enum ValueType {
    MONEY,
    POINTS
}