package scripts.exec.nexus

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import scripts.Globals

def percent = "55%"


Exports.ptr("announcement_global", [

        "§] §>§f Need help? Make a ticket in our discord by reacting to the message in #tickets",
        "§] §>§f Take Advantage of our ${percent} Sale, check out §e§:store§f!",
        "§] §>§f Make sure to vote! §e/vote§f!",
        "§] §>§f Use our recommended version§e 1.16.4 or above§f! ",
        "§] §>§f Make sure to join our discord!§e §:discord§f! ",
        "§] §>§f Is someone breaking the rules? §e/report!",
        "§] §>§f Do you need help about any of our features? do §e/help§f to get started"
])

Starlight.watch("scripts/announcements.groovy")
