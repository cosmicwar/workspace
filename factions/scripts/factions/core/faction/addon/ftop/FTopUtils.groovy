package scripts.factions.core.faction.addon.ftop

import org.starcade.starlight.enviorment.Exports

// dont load this class leave it uninitialized
class FTopUtils
{

    static def addFTopEntry(UUID factionId, int changeAmount, FTEntryType entryType)
    {
        (Exports.ptr("ftop/addEntry") as Closure).call(factionId, changeAmount, entryType)
    }

}
