package scripts.factions.content.entity.npc

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

class NpcStuff {

    // moved to its own file as a workaround for missing remapping
    static void setSkinFlags(ServerPlayer npc) {
        npc.getEntityData().set(Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte) 127)
    }

}
