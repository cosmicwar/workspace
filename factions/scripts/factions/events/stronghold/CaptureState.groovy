package scripts.factions.events.stronghold

enum CaptureState
{

    CONTROLLED("§a§lCONTROLLED"),
    CAPTURING("§e§lCAPTURING"),
    CONTESTED("§c§lCONTESTED"),
    ATTACKING("§4§lATTACKING"),
    NEUTRAL("§7§lNEUTRAL")

    String displayName

    CaptureState(String displayName)
    {
        this.displayName = displayName
    }

}