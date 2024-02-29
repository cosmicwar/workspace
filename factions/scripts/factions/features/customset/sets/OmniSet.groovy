package scripts.factions.features.customset.sets

import scripts.factions.features.customset.struct.CustomSet

class OmniSet extends CustomSet {

    OmniSet() {
        super("omni",
                "Omni",
                [
                        "",
                        "§<bold>§<primaryColor>OMNI SET BONUS",
                        "§<primaryColor>Acts as any §<secondaryColor>armor piece §<primaryColor>for a set.",
                        "",
                ],
                "",
                "",
                "Crown",
                "Robe",
                "Guards",
                "Soles",
                ["#373EFB", "#DC61FB"]
        )

        canHaveCrystal = false
    }

}

