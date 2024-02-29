package scripts.exec.nova1

import org.starcade.starlight.Starlight

Starlight.watch(
        "~/Config.groovy",
        "scripts/factions/init.groovy",
        "~/cratesconfig.groovy",
        "~/buy.groovy",
        "~/kits.groovy",
        "~/npcs.groovy"
)