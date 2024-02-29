package scripts.factions.content.entity.holograms

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.Level
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.wazowski.fake.FakeEntityPlayer
import scripts.shared.legacy.utils.FastItemUtils

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

class HologramTracker {

    public String id
    public Location location
    private List<String> lines
    public boolean dynamic
    public boolean forceUpdate = false
    public Closure onUpdate

    public Set<String> placeholders
    Predicate<Player> visibilityPredicate

    public Map<Player, Map<Integer, HologramLine>> viewerHolograms = new ConcurrentHashMap<>()

    void init(String id, Location location, List<?> lines, boolean dynamic, Set<String> placeholders, Predicate<Player> visibilityPredicate = null) {
        this.id = id
        this.location = location
        updateLines(lines)
        this.dynamic = dynamic
        this.placeholders = placeholders
        if (visibilityPredicate != null) this.visibilityPredicate = visibilityPredicate
        updateNearby()
    }

    void updateLines(List<?> lines) {
        this.lines = lines.findResults { it.toString() } as List<String>
        this.forceUpdate = true
    }

    void updateNearby() {
        onUpdate?.call()
        viewerHolograms.entrySet().removeIf { !it.key.isOnline() }

        List<ServerPlayer> players = (location.world as CraftWorld).getHandle().getPlayersInternal()
        players.each { ServerPlayer serverPlayer ->
            if (serverPlayer instanceof FakeEntityPlayer) {
                return
            }

            Player bukkitPlayer = serverPlayer.bukkitEntity
            boolean isTracked = viewerHolograms.containsKey(bukkitPlayer)
            boolean visible = shouldBeVisibleTo(bukkitPlayer)

            if (isTracked && (distance(serverPlayer) > 48D || !visible)) {
                removeForPlayer(bukkitPlayer)
            } else if (distance(serverPlayer) < 30D && visible) {
                update(serverPlayer)
            }
        }
    }

    boolean shouldBeVisibleTo(Player player) {
        return visibilityPredicate == null || visibilityPredicate.test(player)
    }

    double distance(ServerPlayer player) {
        if (player.level() == (location.world as CraftWorld).getHandle()) {
            double dx = player.getX() - location.x
            double dy = player.getY() - location.y
            double dz = player.getZ() - location.z
            return Math.sqrt(dx * dx + dy * dy + dz * dz)
        } else {
            return 999.0d
        }
    }

    void update(ServerPlayer player) {
        Player bukkitPlayer = player.bukkitEntity
        Map<Integer, HologramLine> hologramLines = viewerHolograms.get(bukkitPlayer)
        if (!dynamic && hologramLines != null && !forceUpdate) {
            return
        }

        List<String> newLines
        if (dynamic) {
            newLines = new ArrayList<>()
            lines.each { String line ->
                placeholders?.each { placeHolder ->
                    line = HologramRegistry.get().placeholders.get(placeHolder)?.update(bukkitPlayer, line)
                }
                newLines.add(line.toString())
            }
        } else {
            newLines = new ArrayList<>(lines)
        }

        hologramLines = viewerHolograms.computeIfAbsent(bukkitPlayer, v -> new ConcurrentHashMap<>())
        if (newLines.size() != hologramLines.size()) {
            hologramLines.each { it.value?.destroy(bukkitPlayer) }
            hologramLines.clear()
        }

        Level level = (location.world as CraftWorld).getHandle()
        double offset = 0D
        int index = newLines.size()
        newLines.reverse().each {
            String str = it.toString()
            HologramLine existingLine = hologramLines.get(index)
            if (str.startsWith("ITEM:")) {
                offset += 0.325D
                if (existingLine instanceof ItemLine) {
                    ItemStack newItemStack = ItemLine.parseItemStack(str.substring("ITEM:".length()))
                    if (newItemStack != existingLine.getItemStack()) {
                        existingLine.setItemStack(newItemStack)
                        existingLine.update(bukkitPlayer)
                    }
                } else {
                    existingLine?.destroy(bukkitPlayer)
                    ItemLine itemLine = new ItemLine(str.substring("ITEM:".length()), location.x, location.y + offset, location.z, level)
                    itemLine.send(bukkitPlayer)
                    hologramLines.put(index, itemLine)
                }

                offset += 0.4D
            } else if (!str.isEmpty()) {
                if (existingLine instanceof TextLine) {
                    TextLine textLine = (TextLine) existingLine
                    if (textLine.text != str) {
                        textLine.setText(str)
                        textLine.update(bukkitPlayer)
                    }
                } else {
                    existingLine?.destroy(bukkitPlayer)

                    TextLine textLine = new TextLine(str, location.x, location.y + offset, location.z, level)
                    textLine.send(bukkitPlayer)
                    hologramLines.put(index, textLine)
                }
                offset += 0.25D
            } else {
                hologramLines.put(index, new TextLine("", location.x, location.y + offset, location.z, level))
                offset += 0.25D
            }

            index -= 1
        }
    }

    void moveTo(Location location) {
        double dx = location.getX() - this.location.getX()
        double dy = location.getY() - this.location.getY()
        double dz = location.getZ() - this.location.getZ()
        this.location = location

        viewerHolograms.each {
            Player player = it.key
            it.value.values().each {
                it.setPosition(it.getX() + dx, it.getY() + dy, it.getZ() + dz)
                it.updatePosition(player)
            }
        }
    }

    void removeForAll() {
        viewerHolograms.entrySet().each {
            Player player = it.key
            it.value.values().each { it.destroy(player) }
        }

        viewerHolograms.clear()
    }

    void removeForPlayer(Player player) {
        viewerHolograms.remove(player)?.values()?.each { it.destroy(player) }
    }

    static void sendPacketsToPlayer(Player player, List<Packet> packets) {
        ServerPlayer nmsPlayer = (player as CraftPlayer).getHandle()
        sendPackets(nmsPlayer, packets)
    }

    static void sendPackets(ServerPlayer player, List<Packet> packets) {
        packets.each { Packet packet ->
            if (player.getClass() == ServerPlayer) {
                player.connection.send(packet)
            }
        }
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof HologramTracker)) return false

        HologramTracker that = (HologramTracker) o
        if (id != that.id) return false

        return true
    }

    @Override
    int hashCode() {
        return id.hashCode()
    }

    static abstract class HologramLine {

        Level level
        double x, y, z
        final Set<Integer> entityIds = new HashSet<>()

        HologramLine(double x, double y, double z, Level level) {
            this.x = x
            this.y = y
            this.z = z
            this.level = level
        }

        void destroy(Player viewer) {
            sendPacketsToPlayer(viewer, [new ClientboundRemoveEntitiesPacket(entityIds as int[])] as List<Packet>)
        }

        abstract void setPosition(double x, double y, double z)

        abstract void updatePosition(Player viewer)

        abstract void update(Player viewer)

        abstract void send(Player viewer)

    }

    static class TextLine extends HologramLine {

        final ArmorStand hologram
        String text

        TextLine(String text, double x, double y, double z, Level level) {
            super(x, y, z, level)
            this.text = text

            hologram = new HologramArmorStand(EntityType.ARMOR_STAND, level)
            hologram.setPosRaw(x, y, z)
            hologram.setCustomName(CraftChatMessage.fromStringOrNull(text))
            hologram.setCustomNameVisible(true)
            hologram.setSmall(true)
            hologram.setInvisible(true)
            hologram.setNoGravity(true)
            hologram.setMarker(true)

            entityIds.add(hologram.getId())
        }

        void setText(String text) {
            if (this.text != text) {
                hologram.setCustomName(CraftChatMessage.fromStringOrNull(text))
            }

            this.text = text
        }

        @Override
        void setPosition(double x, double y, double z) {
            this.x = x
            this.y = y
            this.z = z

            hologram.setPosRaw(x, y, z)
        }

        @Override
        void updatePosition(Player viewer) {
            sendPacketsToPlayer(viewer, [
                    new ClientboundTeleportEntityPacket(hologram)
            ] as List<Packet>)
        }

        //                    new ClientboundSetEntityDataPacket(hologram.getId(), hologram.getEntityData(), true)
        @Override
        void update(Player viewer) {
            sendPacketsToPlayer(viewer, [
                new ClientboundSetEntityDataPacket(hologram.getId(), hologram.getEntityData().packAll())
            ] as List<Packet>)
        }

        @Override
        void send(Player viewer) {
            sendPacketsToPlayer(viewer, [
                    new ClientboundAddEntityPacket(hologram),
                    new ClientboundSetEntityDataPacket(hologram.getId(), hologram.getEntityData().packAll())
//                    new ClientboundSetEntityDataPacket(hologram.getId(), hologram.getEntityData(), true)
            ] as List<Packet>)
        }

    }

    static class ItemLine extends HologramLine {

        final ArmorStand vehicle
        final ItemEntity entityItem
        ItemStack itemStack

        ItemLine(String itemString, double x, double y, double z, Level level) {
            super(x, y, z, level)

            itemStack = parseItemStack(itemString)

            vehicle = new HologramArmorStand(EntityType.ARMOR_STAND, level)
            vehicle.setPosRaw(x, y, z)
            vehicle.setSmall(true)
            vehicle.setInvisible(true)
            vehicle.setNoGravity(true)
            vehicle.setMarker(true)
            entityIds.add(vehicle.getId())

            entityItem = new ItemEntity(level, x, y, z, CraftItemStack.asNMSCopy(itemStack))
            entityIds.add(entityItem.getId())
//            vehicle.passengers.add(entityItem)

            vehicle.@passengers.add(entityItem)
        }

        void setItem(String itemString) {
            this.itemStack = parseItemStack(itemString)
        }

        @Override
        void setPosition(double x, double y, double z) {
            this.x = x
            this.y = y
            this.z = z

            vehicle.setPosRaw(x, y, z)
        }

        @Override
        void updatePosition(Player viewer) {
            sendPacketsToPlayer(viewer, [
                    new ClientboundTeleportEntityPacket(vehicle)
            ] as List<Packet>)
        }

        @Override
        void update(Player viewer) {
            sendPacketsToPlayer(viewer, [
                    new ClientboundSetEntityDataPacket(entityItem.getId(), entityItem.getEntityData().packAll())
//                    new ClientboundSetEntityDataPacket(entityItem.getId(), entityItem.getEntityData(), true)
            ] as List<Packet>)
        }

        @Override
        void send(Player viewer) {
            sendPacketsToPlayer(viewer, [
                    new ClientboundAddEntityPacket(vehicle),
                    new ClientboundSetEntityDataPacket(vehicle.getId(), vehicle.getEntityData().packAll()),
//                    new ClientboundSetEntityDataPacket(vehicle.getId(), vehicle.getEntityData(), true),
                    new ClientboundAddEntityPacket(entityItem),
                    new ClientboundSetEntityDataPacket(entityItem.getId(), entityItem.getEntityData().packAll()),
//                    new ClientboundSetEntityDataPacket(entityItem.getId(), entityItem.getEntityData(), true),
                    new ClientboundSetPassengersPacket(vehicle)
            ] as List<Packet>)
        }

        static ItemStack parseItemStack(String itemString) {
            ItemStack itemStack
            try {
                itemStack = FastItemUtils.getEssentialsItem(itemString)
            } catch (Exception ignored) {
                itemStack = null
            }

            return itemStack
        }

    }

}
