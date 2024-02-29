package scripts.factions.content.entity.npc

import com.google.common.collect.Sets
import com.mojang.datafixers.util.Pair
import net.minecraft.ChatFormatting
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.Slime
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.starcade.wazowski.fake.FakeEntityPlayer
import org.starcade.starlight.helper.Schedulers
import scripts.shared.legacy.utils.PacketUtils
import scripts.shared.legacy.utils.Predicates

import java.util.function.Consumer

class NPCTracker {
    public static Scoreboard fakeScoreboard = new Scoreboard()

    public ServerPlayer npc
    public String id = null
    public String name = null
    public Object skinHolder = null
    public Consumer<Player> onClick = null
    public boolean ready = false
    boolean turnTowardPlayers = false
    ChatBubble chatBubble = null

    public Set<Player> viewers = Sets.newConcurrentHashSet()

    public PlayerTeam scoreboard = null
    public ArmorStand nametag = null
    public Slime entitySlime = null

    public List<Packet> addPackets = null
    public List<Packet> delayedAddPackets = null
    public List<Packet> removePackets = null
    public ClientboundSetEquipmentPacket equipmentPacket = null

    boolean showNametag

    void init(boolean showNametag) {
        this.showNametag = showNametag

        NpcStuff.setSkinFlags(npc)
        //npc.getEntityData().set(new EntityDataAccessor<>(16, EntityDataSerializers.INT), (byte) 127)

        scoreboard = new PlayerTeam(fakeScoreboard, npc.getScoreboardName())
        scoreboard.setNameTagVisibility(Team.Visibility.NEVER)
        scoreboard.getPlayers().add(npc.getScoreboardName())
        addPackets = [
                // npc
                new ClientboundSetEntityDataPacket(npc.getId(), npc.getEntityData().packAll()),
                ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(scoreboard, true),
        ] as List<Packet>

        if (showNametag) {
            entitySlime = new Slime(EntityType.SLIME, npc.level())
            entitySlime.setPosRaw(npc.getX(), npc.getY() + 1.75D, npc.getZ())
            entitySlime.setInvisible(true)
            entitySlime.setSize(1, false)

            nametag = new ArmorStand(EntityType.ARMOR_STAND, npc.level())
            nametag.setPosRaw(npc.getX(), npc.getY() + 1.75D, npc.getZ())
            nametag.setCustomName(CraftChatMessage.fromStringOrNull(name))
            nametag.setCustomNameVisible(true)
            nametag.setSmall(true)
            nametag.setInvisible(true)
            nametag.setNoGravity(true)
            nametag.setMarker(true)

            (nametag as Entity).startRiding(entitySlime as Entity, true)
            (entitySlime as Entity).startRiding(npc as Entity, true)

            addPackets.addAll([
                    // slime
                    new ClientboundSetEntityDataPacket(entitySlime.getId(), entitySlime.getEntityData().packAll()),

                    // hologram (nametag)
                    new ClientboundSetEntityDataPacket(nametag.getId(), nametag.getEntityData().packAll()),
                    new ClientboundSetPassengersPacket(npc),
                    new ClientboundSetPassengersPacket(entitySlime)
            ] as List<Packet>)
        }

        delayedAddPackets = [
                new ClientboundPlayerInfoRemovePacket(Collections.singletonList(npc.getUUID()))
        ] as List<Packet>

        removePackets = [
                new ClientboundRemoveEntitiesPacket([nametag?.getId(), entitySlime?.getId(), npc.getId()].findResults { it } as int[]),
                ClientboundSetPlayerTeamPacket.createRemovePacket(scoreboard)
        ] as List<Packet>
    }

    List<Packet> getSpawnPackets() {
        return [
                new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc),
                new ClientboundAddPlayerPacket(npc),
                new ClientboundRotateHeadPacket(npc, (byte) ((npc.getYHeadRot() * 256.0F) / 360.0F)),
                entitySlime != null ? new ClientboundAddEntityPacket(entitySlime) : null,
                nametag != null ? new ClientboundAddEntityPacket(nametag) : null,
                addPackets,
                equipmentPacket
        ].flatten().findResults { it } as List<Packet>
    }

    void updateNearby() {
        Schedulers.sync().execute {
            if (!ready) return

            npc.level().getNearbyPlayers(npc, npc.position().x(), npc.position().y(), npc.position().z(), 64.0D, {it instanceof Player && it !instanceof FakeEntityPlayer }).forEach { player ->
                Player bukkitPlayer = (Player) player.getBukkitEntity()
                double distance = distance(player)

                if (viewers.contains(bukkitPlayer)) {
                    if (npc.dead || distance > 48D) {
                        removeViewer(player)
                    } else if (turnTowardPlayers && distance <= 8D) {
                        turnToward(player)
                    }
                } else if (distance < 30D) {
                    viewers.add(bukkitPlayer)
                    update(player)
                }
            }

            viewers.removeIf(Predicates.PLAYER_OFFLINE)
        }
    }

    double distance(net.minecraft.world.entity.player.Player player) {
        if (player.level == npc.level) {
            double dx = player.getX() - npc.getX()
            double dy = player.getY() - npc.getY()
            double dz = player.getZ() - npc.getZ()
            return Math.sqrt(dx * dx + dy * dy + dz * dz)
        } else {
            return 999.0d
        }
    }

    void removeViewer(ServerPlayer entityPlayer) {
        sendPackets(entityPlayer, removePackets)
        viewers.remove(entityPlayer.bukkitEntity)

        if (chatBubble != null) {
            sendPackets(entityPlayer, [chatBubble.getDestroyPacket()] as List<Packet>)
        }
    }

    void update(Player player) {
        Schedulers.sync().execute {
            if (npc.dead) return

            net.minecraft.world.entity.player.Player nmsPlayer = (player as CraftPlayer).getHandle()
            if (distance(nmsPlayer) <= 64) {
                update(nmsPlayer)
            }
        }
    }

    void update(ServerPlayer player) {
        Schedulers.sync().execute {
            if (npc.dead || player.level != npc.level) return

            sendPackets(player, getSpawnPackets())

            Schedulers.async().runLater({
                sendPackets(player, delayedAddPackets)
            }, 100)
        }
    }

    // removals should be handled with NPCRegistry#unregister
    void remove() {
        npc.bukkitEntity.disconnect("") // clear up perm subs
        clearChat()
        nametag.dead = true
        entitySlime.dead = true

        sendPacketsToAllDistanceChecked(removePackets)

        Schedulers.sync().execute {
            npc.remove(Entity.RemovalReason.KILLED)
        }
    }

    void sendPacketsToAll(List<Packet> packets) {
        Set<ServerPlayer> worldPlayers = (npc.level().players() as List<ServerPlayer>).findAll { !(it instanceof FakeEntityPlayer) }
        for (def entityPlayer : worldPlayers) {
            sendPackets(entityPlayer, packets)
        }
    }

    void sendPacketsToAllDistanceChecked(List<Packet> packets) {
        for (Player player : viewers) {
            sendPackets(((CraftPlayer) player).getHandle(), packets)
        }
    }

    void setHelmet(Material item) {
        setHelmet(new ItemStack(item))
    }

    void setHelmet(ItemStack item) {
        addEquipment(EquipmentSlot.HEAD, item)
    }

    void setChestplate(Material item) {
        setChestplate(new ItemStack(item))
    }

    void setChestplate(ItemStack item) {
        addEquipment(EquipmentSlot.CHEST, item)
    }

    void setLeggings(Material item) {
        setLeggings(new ItemStack(item))
    }

    void setLeggings(ItemStack item) {
        addEquipment(EquipmentSlot.LEGS, item)
    }

    void setBoots(Material item) {
        setBoots(new ItemStack(item))
    }

    void setBoots(ItemStack item) {
        addEquipment(EquipmentSlot.FEET, item)
    }

    void setHand(Material item) {
        setHand(new ItemStack(item))
    }

    void setHand(ItemStack item) {
        addEquipment(EquipmentSlot.MAINHAND, item)
    }

    void setOffHand(Material item) {
        setOffHand(new ItemStack(item))
    }

    void setOffHand(ItemStack item) {
        addEquipment(EquipmentSlot.OFFHAND, item)
    }

    void clearSlot(EquipmentSlot itemSlot) {
        addEquipment(itemSlot, new ItemStack(Material.AIR))
    }

    void addEquipment(EquipmentSlot itemSlot, ItemStack itemStack) {
        if (equipmentPacket == null) {
            equipmentPacket = new ClientboundSetEquipmentPacket(npc.getId(), new ArrayList<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>>())
        }

        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> equipment = equipmentPacket.getSlots()
        equipment.removeIf({ it.first == itemSlot })
        equipment.add(new Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>(itemSlot, CraftItemStack.asNMSCopy(itemStack)))
        sendPacketsToAllDistanceChecked([equipmentPacket] as List<Packet>)
    }

    void chat(List<String> message, long duration) {
        if (chatBubble != null && chatBubble.lines == message) {
            chatBubble.extend(duration)
            return
        }

        clearChat()
        chatBubble = new ChatBubble(this, message, System.currentTimeMillis() + duration)
        chatBubble.spawn()
    }

    void clearChat() {
        chatBubble?.destroy()
    }

    void setGlow(ChatFormatting color) {
        npc.bukkitEntity.setGlowing(true)
        scoreboard.setColor(color)

        Packet metaPacket = new ClientboundSetEntityDataPacket(npc.getId(), npc.getEntityData().packAll())
        addPackets.set(0, metaPacket)
        Packet teamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(scoreboard, true)
        addPackets.set(1, teamPacket)

        Packet updateTeamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(scoreboard, false)
        Schedulers.async().run({
            Schedulers.sync().execute {
                PacketUtils.send(viewers, metaPacket)
                PacketUtils.send(viewers, updateTeamPacket)
            }
        })
    }

    void moveTo(Location location) {
        npc.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch())
        entitySlime?.setPosRaw(location.getX(), location.getY() + 1.75D, location.getZ())
        nametag?.setPosRaw(location.getX(), location.getY() + 1.75D, location.getZ())

        List<Packet> packets = new ArrayList<>()
        packets.add(new ClientboundTeleportEntityPacket(npc))
        if (npc.getYHeadRot() != location.getYaw()) {
            npc.setYHeadRot(location.getYaw())
            packets.add(new ClientboundRotateHeadPacket(npc, (byte) ((npc.getYHeadRot() * 256.0F) / 360.0F)))
        }

        sendPacketsToAllDistanceChecked(packets)
    }

    void setName(String name) {
        this.name = name

        if (nametag) {
            nametag.setCustomName(CraftChatMessage.fromStringOrNull(name))
            ClientboundSetEntityDataPacket newMetaDeta = new ClientboundSetEntityDataPacket(nametag.getId(), nametag.getEntityData().packAll())
            addPackets.set(3, newMetaDeta)
            sendPacketsToAllDistanceChecked([newMetaDeta] as List<Packet>)
        }
    }

    void turnToward(ServerPlayer entityPlayer) {
        double x = entityPlayer.getX()
        double y = entityPlayer.getY() + entityPlayer.getEyeHeight()
        double z = entityPlayer.getZ()

        Location npcLoc = npc.bukkitEntity.getLocation()
        Vector angle = new Vector(x, y, z).subtract(new Vector(npcLoc.x, npcLoc.y + npc.getEyeHeight(), npcLoc.z))
        npcLoc.setDirection(angle)

//        i = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
//        j = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);

        byte yaw = (byte) Mth.lfloor(npcLoc.getYaw() * 256.0F / 360.0F)
        byte pitch = (byte) Mth.lfloor(npcLoc.getPitch() * 256.0F / 360.0F)
        sendPackets(entityPlayer, [new ClientboundMoveEntityPacket.Rot(npc.getId(), yaw, pitch, npc.onGround()), new ClientboundRotateHeadPacket(npc, yaw)] as List<Packet>)
    }

    void lookAtBlock(Location location) {
        double x = location.getX()
        double y = location.getY()
        double z = location.getZ()

        Location npcLoc = npc.bukkitEntity.getLocation()
        Vector angle = new Vector(x, y, z).subtract(new Vector(npcLoc.x, npcLoc.y + npc.getEyeHeight(), npcLoc.z))
        npcLoc.setDirection(angle)

        byte yaw = (byte) Mth.lfloor(npcLoc.yaw * 256.0F / 360.0F)
        byte pitch = (byte) Mth.lfloor(npcLoc.pitch * 256.0F / 360.0F)
        sendPacketsToAllDistanceChecked([new ClientboundMoveEntityPacket.Rot(npc.id, yaw, pitch, npc.onGround), new ClientboundRotateHeadPacket(npc, yaw)] as List<Packet>)
    }

    void lookAt(Location location) {
        npc.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch())
        sendPacketsToAllDistanceChecked([new ClientboundTeleportEntityPacket(npc), new ClientboundRotateHeadPacket(npc, (byte) ((location.getYaw() * 256.0F) / 360.0F))])
    }

    void swing() {
        sendPacketsToAllDistanceChecked([new ClientboundAnimatePacket(npc, 0)] as List<Packet>)
    }

    static void sendPackets(ServerPlayer player, List<Packet> packets) {
        for (def packet : packets) {
            if (player.getClass() == ServerPlayer) {
                player.connection.send(packet)
                //player.world.execute({player.playerConnection.sendPacket(packet)})
            }
        }
    }

}