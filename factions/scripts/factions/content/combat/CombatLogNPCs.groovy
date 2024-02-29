package scripts.factions.content.combat

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.github.sirblobman.combatlogx.api.ICombatLogX
import com.github.sirblobman.combatlogx.api.event.PlayerPunishEvent
import com.github.sirblobman.combatlogx.api.manager.ICombatManager
import com.github.sirblobman.combatlogx.api.object.TagReason
import com.github.sirblobman.combatlogx.api.object.TagType
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.regions.RegionContainer
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
import org.apache.commons.lang3.reflect.FieldUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.plugin.RegisteredListener
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.Subscription
import org.starcade.starlight.helper.event.filter.EventFilters
import scripts.exec.Globals
import scripts.shared.legacy.ImmovableUtils
import scripts.shared.legacy.utils.PacketUtils
import scripts.shared.legacy.utils.npc.NPCRegistry
import scripts.shared.legacy.utils.npc.NPCTracker

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class CombatLogNPCs {

    static final long COMBAT_NPC_DESPAWN_TIMER = TimeUnit.SECONDS.toMillis(30L)
    static final double COMBAT_NPC_DAMAGE_PER_HIT = 1D

    static ICombatManager combatlogx
    static PacketAdapter packetAdapter
    static List<Subscription> subscriptions = new ArrayList<>()
    static Map<UUID, CombatLogNPC> combatLoggers = new ConcurrentHashMap<>()

    static void main(String[] args) {
        if (!Globals.COMBATLOG_NPC) {
            return
        }

        ICombatLogX combatLogXPlugin = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX")
        combatlogx = combatLogXPlugin.getCombatManager()

        Exports.ptr("combatlognpcs:hasActiveCombatNpc", { Player player -> return combatLoggers.get(player.uniqueId) != null })

        GroovyScript.addUnloadHook {
            removePackets()
            subscriptions.each { it.unregister() }

            combatLoggers.values().each { it.despawn() }
            combatLoggers.clear()
        }
        addPackets()

        PlayerQuitEvent.class.getHandlerList().getRegisteredListeners().findAll { it.plugin == combatLogXPlugin && it.priority == EventPriority.LOWEST }.each {
            PlayerQuitEvent.class.getHandlerList().unregister(it)
            FieldUtils.getField(RegisteredListener.class, "priority", true).set(it, EventPriority.LOW)
            PlayerQuitEvent.class.getHandlerList().register(it)
        }

        subscriptions.addAll([
                Events.subscribe(PlayerPunishEvent.class).filter(EventFilters.ignoreCancelled()).handler({
                    if (it.player.world != Globals.PVP_WORLD) return

                    it.setCancelled(true)
                }),

                Events.subscribe(PlayerQuitEvent.class, EventPriority.LOWEST).handler({
                    if (it.player.isOp() || it.player.world != Globals.PVP_WORLD) return

                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer()
                    if (!container.createQuery().testState(BukkitAdapter.adapt(it.player.location), WorldGuardPlugin.inst().wrapPlayer(it.player), Flags.PVP)) {
                        return
                    }

                    Level worldServer = (it.player.world as CraftWorld).handle
                    net.minecraft.world.entity.player.Player entityPlayer = (it.player as CraftPlayer).handle

                    int moves = 0
                    while (!worldServer.noCollision(entityPlayer) && entityPlayer.getY() < 256.0D) {
                        if (++moves > 10) break

                        Location newLocation = it.player.location.add(0D, 1D, 0D)
                        entityPlayer.connection.teleport(newLocation)
                    }

                    spawnCombatLogger(it.player)
                }),

                Events.subscribe(PlayerJoinEvent.class, EventPriority.HIGH).handler({
                    CombatLogNPC combatLogNPC = combatLoggers.remove(it.player.uniqueId)
                    if (combatLogNPC == null) return

                    combatLogNPC.despawn()
                    combatlogx.tag(it.player, null, TagType.PLAYER, TagReason.UNKNOWN)
                    if (combatLogNPC.health > 0D) {
                        it.player.setHealth(combatLogNPC.health)
                        net.minecraft.world.entity.player.Player entityPlayer = (it.player as CraftPlayer).handle
                        entityPlayer.setInvulnerable(false)
                        if (it.player.world == combatLogNPC.location.world) {
                            entityPlayer.connection.teleport(combatLogNPC.location.add(0D, 0.6D, 0D)) // bypass events
                        }
                    }
                })
        ])

        Schedulers.async().runRepeating({
            long now = System.currentTimeMillis()

            combatLoggers.entrySet().removeIf({
                CombatLogNPC combatLogNPC = it.getValue()
                if (now - combatLogNPC.getSpawnTime() > COMBAT_NPC_DESPAWN_TIMER) {
                    combatLogNPC.despawn()
                    return true
                }

                if (!combatLogNPC.getKilled().get()) {
                    long remainingSeconds = TimeUnit.MILLISECONDS.toSeconds((COMBAT_NPC_DESPAWN_TIMER + combatLogNPC.getSpawnTime()) - now)
                    combatLogNPC.npcTracker?.chat(["Despawning in: ${remainingSeconds}s"], 1000L)
                }

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

                    Map.Entry<UUID, CombatLogNPC> match = combatLoggers.find { it.value.location.clone().add(0D, 0.9D, 0D).distanceSquared(arrow.location) <= 2D }
                    if (match) {
                        combatlogx.tag(arrow.getShooter() as Player, null, TagType.PLAYER, TagReason.UNKNOWN)

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

    static CombatLogNPC spawnCombatLogger(Player player) {
        CombatLogNPC combatLogNPC = combatLoggers.get(player.uniqueId)
        if (combatLogNPC != null) return combatLogNPC

        return combatLoggers.computeIfAbsent(player.uniqueId, v -> new CombatLogNPC(player))
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
                        EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0)
                        if (action != EnumWrappers.EntityUseAction.ATTACK) return

                        int entityId = packet.getIntegers().read(0)
                        CombatLogNPC combatLogNPC = combatLoggers.values().find { it.npcTracker.npc.getId() == entityId }
                        if (combatLogNPC && combatLogNPC.npcTracker.npc.level.getWorld() == player.world) {
                            Schedulers.sync().run {
                                combatlogx.tag(player, null, TagType.PLAYER, TagReason.UNKNOWN)
                                combatLogNPC.damage()
                            }
                        }
                        break
                }
            }
        }

        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter)
    }

    static void removePackets() {
        if (packetAdapter == null) return

        ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapter)
    }

    static void onNpcDeath(CombatLogNPC combatLogNPC) {
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
