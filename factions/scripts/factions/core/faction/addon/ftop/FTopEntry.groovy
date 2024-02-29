package scripts.factions.core.faction.addon.ftop

class FTopEntry {
    UUID factionId
    int amount
    FTEntryType entryType

    FTopEntry(UUID factionId, int amount, FTEntryType entryType) {
        this.factionId = factionId
        this.amount = amount
        this.entryType = entryType
    }
}
