package scripts.exec.nova1

import com.google.common.io.BaseEncoding
import groovy.transform.Field
import org.bukkit.World
import scripts.exec.Globals

import java.time.LocalTime

/**
 * Needs to be separate file to be loaded in before everything else so universal scripts are also able to use Globals.
 */

@Field static String RESOURCE_PACK_HASH = "cb6e3ab390164a1968474fa92ffc40c81f7de472"
Globals.RESOURCE_PACK_HASH = BaseEncoding.base16().lowerCase().decode(RESOURCE_PACK_HASH)
Globals.RESOURCE_PACK_URL = "https://download.mc-packs.net/pack/cb6e3ab390164a1968474fa92ffc40c81f7de472.zip"

Globals.scheduledRebootTime = LocalTime.of(13, 30)
Globals.DONATOR_RANKS = new LinkedList<>(["emperor", "vader", "commander", "captain", "voyager", "sentinel", "nomad"])

Globals.worldTimeFunction = { World world ->
    if (world.getName().startsWith("world_")) {
        return 6000
    }

    return 6000
}

Globals.TRACKING_METRICS = true

Globals.ITEM_CF = true
Globals.ITEM_SKINS = true

Globals.CUSTOM_TAGS = true

Globals.INTERACTION_REWARDS = true
Globals.RULES = true
Globals.STAFF = true
Globals.statistics = true
Globals.STAFF2 = false

Globals.CF_VALUES = true
Globals.BATTLEPASS = true
Globals.STORE = true

Globals.ACTION_BAR_INFO = true