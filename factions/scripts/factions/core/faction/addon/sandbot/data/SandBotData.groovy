package scripts.factions.core.faction.addon.sandbot.data

import com.google.common.collect.Maps
import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material

@CompileStatic
class SandBotData {

    Material sandBotPlaceMaterial = Material.WHITE_WOOL
    Integer sandBotRadius = 2

    @BsonIgnore
    transient Map<Integer, SandBot> bots = Maps.newConcurrentMap()

    SandBotData() {}

}
