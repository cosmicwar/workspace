package scripts.factions.content.entity.npc

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.Slime
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage

class ChatBubble
{

    final NPCTracker npcTracker
    final List<String> lines
    long timeout
    final Set<Integer> spawnedEntities = new HashSet<>()

    ChatBubble(NPCTracker npcTracker, List<String> lines, long timeout) {
        this.npcTracker = npcTracker
        this.lines = lines
        this.timeout = timeout
    }

    void extend(long duration) {
        long now = System.currentTimeMillis()
        if (timeout < now) {
            timeout = now
        }

        timeout += duration
    }

    void spawn() {
        if (lines.isEmpty()) return

        List<Packet> packets = new ArrayList<>()
        double offset = 1.75D
        Entity attachTo = npcTracker.nametag
        Entity npc = npcTracker.npc
        for (int i = lines.size() - 1; i >= 0; i--) {
            offset += 0.4D

            ArmorStand chatLine = new ArmorStand(EntityType.ARMOR_STAND, npc.level)
            chatLine.setPosRaw(npc.getX(), npc.getY() + offset, npc.getZ())
            chatLine.setCustomName(CraftChatMessage.fromStringOrNull(lines.get(i)))
            chatLine.setCustomNameVisible(true)
            chatLine.setSmall(true)
            chatLine.setInvisible(true)
            chatLine.setNoGravity(true)
            chatLine.setMarker(true)
            spawnedEntities.add(chatLine.id)

            Slime spacer = new Slime(EntityType.SLIME, npc.level)
            spacer.setPosRaw(npc.getX(), npc.getY() + offset, npc.getZ())
            spacer.setInvisible(true)
            spacer.setSize(1, false)
            spawnedEntities.add(spacer.id)

            (chatLine as Entity).startRiding(spacer as Entity, true)
            (spacer as Entity).startRiding(attachTo as Entity, true)

            packets.add(new ClientboundAddEntityPacket(spacer))
            packets.add(new ClientboundSetEntityDataPacket(spacer.getId(), spacer.getEntityData().packAll()))
            packets.add(new ClientboundAddEntityPacket(chatLine))
            packets.add(new ClientboundSetEntityDataPacket(chatLine.getId(), chatLine.getEntityData().packAll()))

            packets.add(new ClientboundSetPassengersPacket(attachTo))
            packets.add(new ClientboundSetPassengersPacket(spacer))

            attachTo = chatLine
        }

        npcTracker.sendPacketsToAllDistanceChecked(packets)
    }

    ClientboundRemoveEntitiesPacket getDestroyPacket() {
        return new ClientboundRemoveEntitiesPacket(spawnedEntities as int[])
    }

    void destroy() {
        npcTracker.sendPacketsToAll([getDestroyPacket()] as List<Packet>)
    }

}