package scripts.exec

import org.starcade.starlight.Starlight
import scripts.shared.utils.Temple

Globals.GLOBAL_TRACKING = true
Globals.GLOBAL_STARDUST = true
Globals.GLOBAL_BUYCRAFT = true
Globals.GLOBAL_VOTES = true

Starlight.watch(
        "~/universalconfig.groovy",
        "~/${Temple.templeId.replace("_local", "")}/_globals.groovy",
        "~/config.groovy",
        "~/theme.groovy",
        "~/databases.groovy",
        "scripts/shared3/data/ArkPlayer.groovy",
        "scripts/shared3/ArkGpt.groovy",
        "scripts/shared3/ArkTheme.groovy",
        "scripts/shared3/ArkPerms.groovy",
        "scripts/shared3/ArkGroups.groovy",
        "scripts/shared3/Scoreboards.groovy",
        "scripts/shared3/ArkAlerts.groovy",
        "scripts/shared3/ArkExt.groovy",
        "~/permissions.groovy",
        "~/gpt.groovy",
        "~/groups.groovy",
        "~/universal.groovy",
        "~/votesconfig.groovy",
        "~/interactionconfig.groovy",
)

Starlight.watch("~/${Temple.templeId.replace("_local", "")}/_init.groovy")

Starlight.watch("~/postinit.groovy")