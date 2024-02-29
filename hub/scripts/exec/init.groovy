package scripts.exec

import org.starcade.starlight.Starlight
import scripts.Globals
import scripts.shared.utils.Temple

Globals.GLOBAL_TRACKING = true
Globals.GLOBAL_COINS = true
Globals.GLOBAL_BUYCRAFT = true
Globals.GLOBAL_VOTES = true

if (Temple.templebase == "nexus") {
    Starlight.watch(
            "~/interactionconfig.groovy",
            "~/nexus/_init.groovy",
    )
} else {
    Starlight.watch(
            "scripts/universalconfig.groovy",
            "~/hub/_globals.groovy",
            "~/config.groovy",
            "~/theme.groovy",
            "~/databases.groovy",
            "scripts/shared3/data/ArkPlayer.groovy",
            "scripts/shared3/ArkGpt.groovy",
            "scripts/shared3/ArkTheme.groovy",
            "scripts/shared3/ArkPerms.groovy",
            "scripts/shared3/ArkGroups.groovy",
            "scripts/shared3/ArkAlerts.groovy",
            "~/permissions.groovy",
            "~/gpt.groovy",
            "~/groups.groovy",
            "scripts/universal.groovy",
            "~/votesconfig.groovy",
            "~/interactionconfig.groovy",
    )

    Starlight.watch("~/hub/_init.groovy")
}

Starlight.watch("~/postinit.groovy")