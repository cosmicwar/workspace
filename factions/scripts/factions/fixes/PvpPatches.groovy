package scripts.factions.fixes

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket

class PvpPatches {

    static PacketAdapter packetAdapter

    static void main(String[] args) {
        GroovyScript.addUnloadHook {
            removePackets()
        }

        addPackets()
    }

    static void addPackets() {
        packetAdapter = new PacketAdapter(Starlight.plugin, ListenerPriority.NORMAL, PacketType.Play.Server.HURT_ANIMATION) {
            @Override
            void onPacketSending(PacketEvent event) {
                if (event.getPacket().getType() == PacketType.Play.Server.HURT_ANIMATION) {
                    float modifiedYaw = event.getPlayer().getLocation().getYaw()
                    ClientboundHurtAnimationPacket modifiedPacket = new ClientboundHurtAnimationPacket(0, modifiedYaw)
                    event.setPacket(PacketContainer.fromPacket(modifiedPacket))
                }
            }
        }

        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter)
    }


    static void removePackets() {
        if (packetAdapter == null) return

        ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapter)
    }
}