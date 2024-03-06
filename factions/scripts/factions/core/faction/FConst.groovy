package scripts.factions.core.faction

import scripts.shared.core.cfg.data.ConfigEntry
import scripts.shared.core.cfg.entries.BooleanEntry
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.shared.core.cfg.entries.IntEntry
import scripts.shared.core.cfg.entries.StringEntry

class FConst {

    static BooleanEntry DEBUG = new BooleanEntry("debug", false)

    /*
        combatSettings = config.getOrCreateCategory("combat", "§3Combat Settings", Material.NETHERITE_SWORD, ["§3Combat Settings"])
        permSettings = config.getOrCreateCategory("perms", "§3Permission Settings", Material.ANVIL, ["§3Permission Settings"])
        claimSettings = config.getOrCreateCategory("claims", "§3Claim Settings", Material.GRASS_BLOCK, ["§3Claim Settings"])
        factionSettings = config.getOrCreateCategory("faction", "§3Faction Settings", Material.GOLDEN_SWORD, ["§3Faction Settings"])
        economySettings = config.getOrCreateCategory("economy", "§3Economy Settings", Material.GOLD_INGOT, ["§3Economy Settings"])
        messageSettings = config.getOrCreateCategory("messages", "§3Message Settings", Material.PAPER, ["§3Message Settings"])
    */

    /*

    *  FACTIONS CONFIG

    */
    static IntEntry maxFactionSize = new IntEntry("max_faction_size", 20)
    static DoubleEntry maxFactionPower = new DoubleEntry("max_faction_power", 1000.0D)
    static IntEntry maxClaimsPerFaction = new IntEntry("max_claims_per_faction", 10)
    static IntEntry maxAllies = new IntEntry("max_allies", 2)
    static IntEntry maxTruces = new IntEntry("max_truces", 2)

    static IntEntry maxWarps = new IntEntry("max_warps", 5)
    static IntEntry chestSize = new IntEntry("chest_size", 9 * 3)

    static IntEntry maxTntStorage = new IntEntry("max_tnt_storage", 1000)
    static IntEntry startingSandBots = new IntEntry("starting_sand_bots", 1)

    static IntEntry memberStartingPower = new IntEntry("member_starting_power", 10)
    static DoubleEntry memberMaxPower = new DoubleEntry("max_member_power", 500.0D)

    static IntEntry maxFactionNameLength = new IntEntry("max_faction_name_length", 15)
    static IntEntry minFactionNameLength = new IntEntry("min_faction_name_length", 3)

    /**\
     *
     *   MESSAGES
     *
    /**/
    static StringEntry defaultFactionName = new StringEntry("default_faction_name", "Wilderness")
    static StringEntry defaultFactionColor = new StringEntry("default_faction_color", "§2")
    static StringEntry defaultFactionDescription = new StringEntry("default_faction_desc", "§f ~ none")

    static Collection<ConfigEntry> DEFAULT_FACTION_VALUES = [
            maxFactionSize,
            maxFactionPower,
            maxClaimsPerFaction,
            maxAllies,
            maxTruces,
            maxWarps,
            chestSize,
            maxTntStorage,
            startingSandBots,
            memberStartingPower,
            memberMaxPower,
            maxFactionNameLength,
            minFactionNameLength
    ]

    static Collection<ConfigEntry> DEFAULT_FACTION_MESSAGE_ENTRIES = [
            defaultFactionName,
            defaultFactionColor,
            defaultFactionDescription
    ]

}

