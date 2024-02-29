package scripts.factions.content.combat;

import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;

import java.util.UUID;

class CombatLogNPC {

    final UUID playerId
    final Location location
    final long spawnTime

    AtomicBoolean killed = new AtomicBoolean(false) // what is dead may never die
    long lastHurtTime
    double health
    NPCTracker npcTracker

    CombatLogNPC(Player player) {
        this.playerId = player.uniqueId
        this.location = player.location
        this.spawnTime = System.currentTimeMillis()

        spawn(player)
    }

    private void spawn(Player player) {
        npcTracker = NPCRegistry.get().spawn("combatlognpc_${playerId.toString()}", "§cCombatLogger: §f${player.getName()}", location, playerId)
        health = player.getHealth()

        PlayerInventory playerInventory = player.getInventory()
        npcTracker.setHand(playerInventory.getItemInMainHand())
        npcTracker.setHelmet(playerInventory.getHelmet())
        npcTracker.setChestplate(playerInventory.getChestplate())
        npcTracker.setLeggings(playerInventory.getLeggings())
        npcTracker.setBoots(playerInventory.getBoots())
    }

    void die() {
        if (killed.getAndSet(true)) return

        CombatLogNPCs.onNpcDeath(this)

        npcTracker.npc.setHealth(0F)
        npcTracker.npc.setPose(Pose.DYING)
        ClientboundEntityEventPacket packetPlayOutEntityStatus = new ClientboundEntityEventPacket(npcTracker.npc, (byte) 3)
        ClientboundSetEntityDataPacket packetPlayOutEntityMetadata = new ClientboundSetEntityDataPacket(npcTracker.npc.getId(), npcTracker.npc.getEntityData().packAll())
        npcTracker.viewers.each {
            it.playSound(location, Sound.ENTITY_PLAYER_DEATH, 1F, 1F)
            PacketUtils.send(it, packetPlayOutEntityStatus)
            PacketUtils.send(it, packetPlayOutEntityMetadata)
        }

        Schedulers.async().runLater({ despawn() }, 20L)
    }

    void damage(double damage = CombatLogNPCs.COMBAT_NPC_DAMAGE_PER_HIT) {
        long now = System.currentTimeMillis()
        if (now - lastHurtTime < 500L) return
        lastHurtTime = now

        ClientboundEntityEventPacket packetPlayOutEntityStatus = new ClientboundEntityEventPacket(npcTracker.npc, (byte) 2)
        npcTracker.viewers.each {
            it.playSound(location, Sound.ENTITY_GENERIC_HURT, 1F, 1F)
            PacketUtils.send(it, packetPlayOutEntityStatus)
        }

        if ((health -= damage) <= 0D) {
            die()
        }
    }

    void relMove(double x, double y, double z) {
        location.add(x, y, z)
        npcTracker?.moveTo(location)
    }

    void despawn() {
        killed.set(true) // prevent death of despawned npc

        if (npcTracker) {
            NPCRegistry.get().unregister(npcTracker)
        }
    }

}
