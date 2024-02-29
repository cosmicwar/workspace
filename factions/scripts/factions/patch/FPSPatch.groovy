package scripts.factions.patch

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.EntityType
import org.starcade.starlight.helper.protocol.Protocol
import scripts.factions.core.faction.Factions

class FPSPatch  {

    FPSPatch() {
        setupPackets()
    }

    static def setupPackets() {

        Protocol.subscribe(PacketType.Play.Server.SPAWN_ENTITY).handler({
            if (it.playerTemporary) return

            def player = it.player
            def member = Factions.getMember(player.getUniqueId(), false)
            if (member == null) return

            def fpsData = member.fpsData

            PacketContainer packetContainer = it.getPacket()

            def type = packetContainer.getEntityTypeModifier().read(0)
            if (type != null) {
                if (type == EntityType.PRIMED_TNT) if (!fpsData.showFallingTnt) it.setCancelled(true)
                if (type == EntityType.FALLING_BLOCK) {
                    if (!fpsData.showFallingSand) it.setCancelled(true)
                }
            }

        })
    }


}
