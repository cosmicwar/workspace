package scripts.factions.content.combat

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction
import com.google.common.collect.Maps
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.jodah.expiringmap.ExpiringMap
import net.minecraft.Util
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.PlayerDataStorage
import net.minecraft.world.phys.AABB
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack
import org.bukkit.entity.Arrow
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import scripts.exec.Globals
import scripts.shared.legacy.ImmovableUtils
import scripts.shared.legacy.utils.PacketUtils
import scripts.shared.legacy.utils.npc.NPCRegistry
import scripts.shared.legacy.utils.npc.NPCTracker

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@CompileStatic(TypeCheckingMode.SKIP)
class CombatTag {

    static final long DURATION = 15_000L
    static Map<UUID, CombatData> combatData = Maps.newConcurrentMap()

    static Map<UUID, UUID> taggedPlayers = ExpiringMap.builder().expiration(15, TimeUnit.SECONDS).expirationListener { UUID uuid, UUID npc ->
        def player = Bukkit.getPlayer(uuid)
        if (player != null) Players.msg(player, "§cYou are no longer in combat.")

        def data = combatData.get(uuid)
        if (data != null) data.combatTagExpiration = null
    }.build()

    static final long COMBAT_NPC_DESPAWN_TIMER = TimeUnit.SECONDS.toMillis(30L)
    static final double COMBAT_NPC_DAMAGE_PER_HIT = 1D

    static Map<UUID, CombatNPC> combatLoggers = new ConcurrentHashMap<>()
    static PacketAdapter packetAdapter

    CombatTag() {

        Exports.ptr("combattag:hasActiveCombatNpc", { Player player -> return combatLoggers.containsKey(player.uniqueId)})
        Exports.ptr("combattag:isCombatTagged", { Player player -> return taggedPlayers.containsKey(player.uniqueId)})

        GroovyScript.addUnloadHook {
            removePackets()
            combatLoggers.values().each { it.despawn() }
            combatLoggers.clear()
        }

        Bukkit.getOnlinePlayers().each {
            combatData.computeIfAbsent(it.uniqueId, { new CombatData(it) })
        }
        events()
        schedulers()
        addPackets()
    }

    static void events() {
        Events.subscribe(PlayerJoinEvent.class).handler {event ->
            if (!combatData.containsKey(event.player.uniqueId)) {
                combatData.put(event.player.uniqueId, new CombatData(event.player.uniqueId))
            } else {
                def data = combatData.get(event.player.uniqueId)
            }
        }

        Events.subscribe(PlayerQuitEvent.class, EventPriority.LOW).handler {event ->
            def data = combatData.get(event.player.uniqueId)
            if (data != null) {
                if (data.isTagged()) {
                    spawnCombatLogger(event.player)
                } else {
                    combatData.remove(event.player.getUniqueId())
                }
            }
        }

        Events.subscribe(EntityDamageByEntityEvent.class).handler { event ->
            if (event.isCancelled()) return

            def entity = event.entity
            def damager = event.damager

            if (entity !instanceof Player) return
            def player = (Player) entity

            if (damager instanceof Player) {
                def data = combatData.computeIfAbsent(player.uniqueId, { new CombatData(it) })
                if (!data.combatTagExpiration) {
                    tag(player, damager, DURATION)
                }

                def damagerData = combatData.computeIfAbsent(damager.uniqueId, { new CombatData(it) })
                if (!damagerData.combatTagExpiration) {
                    tag(damager, DURATION)
                }
            }
        }
    }

    static void schedulers() {
        Schedulers.async().runRepeating({
            long now = System.currentTimeMillis()

            combatLoggers.entrySet().removeIf({
                CombatNPC combatLogNPC = it.getValue()
                if (now - combatLogNPC.getSpawnTime() > COMBAT_NPC_DESPAWN_TIMER) {
                    combatLogNPC.despawn()
                    return true
                }

//                if (!combatLogNPC.getKilled().get()) {
////                    long remainingSeconds = TimeUnit.MILLISECONDS.toSeconds((COMBAT_NPC_DESPAWN_TIMER + combatLogNPC.getSpawnTime()) - now)
////                    combatLogNPC.npcTracker?.chat(["Despawning in: ${remainingSeconds}s"], 1000L)
//                }

                return false
            })
        }, 250L, TimeUnit.MILLISECONDS, 250L, TimeUnit.MILLISECONDS)

        Schedulers.async().runRepeating({
            World world = Globals.PVP_WORLD
            if (world == null) return

            Schedulers.sync().run {
                world.entities.findAll { it instanceof Arrow }.each {
                    Arrow arrow = (Arrow) it
                    if (!(arrow.getShooter() instanceof Player)) return

                    Map.Entry<UUID, CombatNPC> match = combatLoggers.find { it.value.location.clone().add(0D, 0.9D, 0D).distanceSquared(arrow.location) <= 2D }
                    if (match) {
                        tag(arrow.getShooter() as Player, DURATION)

                        match.value.damage(arrow.getVelocity().length())
                        arrow.remove()
                    }
                }

                Level worldServer = (world as CraftWorld).handle
                combatLoggers.values().each {
                    if (it.location.y > 1D && worldServer.noCollision(null, new AABB(it.location.x - 0.5D, it.location.y - 0.1D, it.location.z - 0.5D, it.location.x + 0.5D, it.location.y, it.location.z + 0.5D))) {
                        Location newLocation = it.location.clone().add(0D, -0.65D, 0D)
                        Block block = newLocation.getBlock()
                        double maxY = block.getBoundingBox().getMaxY()
                        if (maxY > 0D) {
                            it.relMove(0D, Math.max(-0.65D, maxY - it.location.y), 0D)
                        } else {
                            it.relMove(0D, -0.65D, 0D)
                        }
                    }
                }
            }

        }, 50L, TimeUnit.MILLISECONDS, 50L, TimeUnit.MILLISECONDS)

        Commands.create().assertOp().assertPlayer().handler({ command ->
            spawnCombatLogger(command.sender())
        }).register("dev/clog")
    }

    static def isTagged(Player player) {
        return taggedPlayers.containsKey(player.uniqueId)
    }

    static def tag(Player player, Long duration) {
        def data = combatData.computeIfAbsent(player.uniqueId, { new CombatData(it) })

        if (!data.isTagged()) {
            Players.msg(player, "§c(!) You are now in combat for §l${duration / 1000 as Integer}§c seconds.")
        }

        data.combatTagExpiration = System.currentTimeMillis() + duration
        taggedPlayers.put(player.uniqueId, player.uniqueId)
    }

    static def tag(Player player, Entity damager, Long duration) {
        def data = combatData.computeIfAbsent(player.uniqueId, { new CombatData(it) })

        if (!data.isTagged()) {
            Players.msg(player, "§c(!) You are now in combat for §l${duration / 1000 as Integer}§c seconds.")
        }

        data.combatTagExpiration = System.currentTimeMillis() + duration
        data.setLastDamager(damager)
        taggedPlayers.put(player.uniqueId, player.uniqueId)

    }

    static CombatNPC spawnCombatLogger(Player player) {
        CombatNPC combatNPC = combatLoggers.get(player.uniqueId)
        if (combatNPC != null) return combatNPC

        return combatLoggers.computeIfAbsent(player.uniqueId, v -> new CombatNPC(player))
    }

    static void addPackets() {
        packetAdapter = new PacketAdapter(Starlight.plugin, ListenerPriority.NORMAL, [PacketType.Play.Client.USE_ENTITY]) {
            @Override
            void onPacketReceiving(PacketEvent event) {
                if (event.isPlayerTemporary()) return

                Player player = event.getPlayer()
                PacketContainer packet = event.packet
                switch (packet.type) {
                    case PacketType.Play.Client.USE_ENTITY:

                        final WrappedEnumEntityUseAction wrappedAction = packet.getEnumEntityUseActions().read(0)
                        final EnumWrappers.EntityUseAction action = wrappedAction.getAction()

                        if (action != EnumWrappers.EntityUseAction.ATTACK) return

                        int entityId = packet.getIntegers().read(0)

                        CombatNPC combatLogNPC = combatLoggers.values().find { it.npcTracker.npc.id == entityId }
                        if (combatLogNPC && combatLogNPC.npcTracker.npc.level.getWorld() == player.world) {
                            Schedulers.sync().run {
                                tag(player, DURATION)
                                combatLogNPC.damage()
                            }
                        }
                        break
                }
            }
        }

        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter)
    }

//    static void addPackets() {
//        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Starlight.plugin, PacketType.Play.Client.USE_ENTITY) {
//            @Override
//            void onPacketReceiving(PacketEvent event) {
//                if (!event.isPlayerTemporary()) {
//                    Integer entityId = event.getPacket().getIntegers().read(0)
//
//                    if (entityId == null) {
//                        return
//                    }
//                    NPCTracker tracker = entities.get(entityId)
//
//                    if (tracker == null) {
//                        return
//                    }
//                    Player player = event.getPlayer()
//                    UUID uuid = player.getUniqueId()
//
//                    if (System.currentTimeMillis() - (long) lastUsed.getOrDefault(uuid, 0L) < 200L) {
//                        return
//                    }
//                    lastUsed.put(uuid, System.currentTimeMillis())
//
//                    if (tracker.onClick != null) {
//                        Schedulers.sync().run {
//                            tracker.onClick.accept(player)
//                        }
//                    }
//                }
//            }
//        })
//    }

    static void removePackets() {
        if (packetAdapter == null) return

        ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapter)
    }

    static void onNpcDeath(CombatNPC combatLogNPC) {
        combatLoggers.remove(combatLogNPC.getPlayerId())

        Player player = Bukkit.getPlayer(combatLogNPC.getPlayerId())
        if (player != null) return // do not fuck with online players

        PlayerDataStorage worldStorage = MinecraftServer.getServer().playerDataStorage
        CompoundTag playerNbt = worldStorage.getPlayerData(combatLogNPC.getPlayerId().toString())
        if (playerNbt == null) return

        Inventory playerInventory = new Inventory(combatLogNPC.npcTracker.npc)
        ListTag nbttaglist = playerNbt.getList("Inventory", 10);
        playerInventory.load(nbttaglist)

        List<ItemStack> itemsToDrop = new ArrayList<>()
        consumeInventoryItems(itemsToDrop, playerInventory.armorContents)
        consumeInventoryItems(itemsToDrop, playerInventory.items)

        playerNbt.put("Inventory", playerInventory.save(new ListTag()))
        playerNbt.putFloat("Health", 0F)

        try {
            File file = File.createTempFile(combatLogNPC.getPlayerId().toString() + "-", ".dat", worldStorage.playerDir)
            NbtIo.writeCompressed(playerNbt, file)
            File file1 = new File(worldStorage.playerDir, "${combatLogNPC.getPlayerId().toString()}.dat")
            File file2 = new File(worldStorage.playerDir, "${combatLogNPC.getPlayerId().toString()}.dat_old")
            Util.safeReplaceFile(file1, file, file2)
        } catch (Exception e) {
            println("Failed to update offline inventory for ${combatLogNPC.getPlayerId().toString()}")
            return
        }

        itemsToDrop.each { combatLogNPC.location.world.dropItemNaturally(combatLogNPC.location, it) }
    }

    static void consumeInventoryItems(List<ItemStack> consumedItems, List<net.minecraft.world.item.ItemStack> inventoryItems) {
        inventoryItems.eachWithIndex { net.minecraft.world.item.ItemStack nmsItem, int slot ->
            if (nmsItem == null || nmsItem == net.minecraft.world.item.ItemStack.EMPTY) return

            ItemStack bukkitStack = CraftItemStack.asCraftMirror(nmsItem)
            if (!canRemove(bukkitStack)) return

            consumedItems.add(bukkitStack)
            inventoryItems.set(slot, net.minecraft.world.item.ItemStack.EMPTY)
        }
    }

    static boolean canRemove(ItemStack itemStack) {
        if (itemStack == null || ImmovableUtils.isImmovable(itemStack)) {
            return false
        }

        return true
    }
}

class CombatNPC {
    final UUID playerId
    final Location location
    final long spawnTime

    AtomicBoolean killed = new AtomicBoolean(false)
    long lastHurtTime
    double health

    NPCTracker npcTracker

    CombatNPC(Player player) {
        this.playerId = player.uniqueId
        this.location = player.location
        this.spawnTime = System.currentTimeMillis()

        spawn(player)
    }

    private void spawn(Player player) {
        npcTracker = NPCRegistry.get().spawn("combatlognpc_${playerId.toString()}", "§cCombatLogger: §f${player.getName()}", location, playerId)
        health = player.getHealth()

        def playerInventory = player.getInventory()
        npcTracker.setHand(playerInventory.getItemInMainHand())
        npcTracker.setHelmet(playerInventory.getHelmet())
        npcTracker.setChestplate(playerInventory.getChestplate())
        npcTracker.setLeggings(playerInventory.getLeggings())
        npcTracker.setBoots(playerInventory.getBoots())
    }

    void die() {
        if (killed.getAndSet(true)) return

        CombatTag.onNpcDeath(this)

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

    void damage(double damage = CombatTag.COMBAT_NPC_DAMAGE_PER_HIT) {
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
