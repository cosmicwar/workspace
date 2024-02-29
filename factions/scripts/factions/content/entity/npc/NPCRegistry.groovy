package scripts.factions.content.entity.npc

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.starcade.wazowski.fake.FakeEntityPlayer
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import scripts.exec.Globals
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.utils.Predicates

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

class NPCRegistry {
    private static int NPC_ID = 100000000

    private static NPCRegistry registry = new NPCRegistry()

    static NPCRegistry get() {
        return registry
    }

    public boolean initialized = false

    public Map<String, NPCTracker> registered = new ConcurrentHashMap<>()
    public Map<Integer, NPCTracker> entities = new ConcurrentHashMap<>()
    public Map<UUID, Long> lastUsed = new ConcurrentHashMap<>()

    static void main(String[] args) {
        get().init()
    }

    void init() {
        if (initialized) {
            return
        }
        initialized = true

        Schedulers.async().runRepeating({
            updateAllNearby(entities)
        }, 4, 4)

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Starlight.plugin, PacketType.Play.Client.USE_ENTITY) {
            @Override
            void onPacketReceiving(PacketEvent event) {
                if (!event.isPlayerTemporary()) {
                    Integer entityId = event.getPacket().getIntegers().read(0)

                    if (entityId == null) {
                        return
                    }
                    NPCTracker tracker = entities.get(entityId)

                    if (tracker == null) {
                        return
                    }
                    Player player = event.getPlayer()
                    UUID uuid = player.getUniqueId()

                    if (System.currentTimeMillis() - (long) lastUsed.getOrDefault(uuid, 0L) < 200L) {
                        return
                    }
                    lastUsed.put(uuid, System.currentTimeMillis())

                    if (tracker.onClick != null) {
                        Schedulers.sync().run {
                            tracker.onClick.accept(player)
                        }
                    }
                }
            }
        })

        Events.subscribe(PlayerQuitEvent.class).handler { event -> lastUsed.remove(event.getPlayer().getUniqueId()) }

        Events.subscribe(PlayerChangedWorldEvent.class).handler({
            Player player = it.player
            ServerLevel world = ((CraftWorld) it.getFrom()).getHandle()

            entities.values().findAll { it.npc.level() == world && it.viewers.contains(player) }.each { it.removeViewer(((CraftPlayer) player).getHandle()) }
        })

    }

    static void updateAllNearby(Map<Integer, NPCTracker> entities) {
        //println "update-all"

        entities.values().groupBy { it.npc.level() }.each {
            ServerLevel world = it.key
            List<NPCTracker> npcTrackers = it.value

            Schedulers.sync().run {
                long now = System.currentTimeMillis()
                Set<ServerPlayer> worldPlayers = world.getPlayersInternal().findAll { !(it instanceof FakeEntityPlayer) }
                for (NPCTracker npcTracker : npcTrackers) {
                    //println("${npcTracker} ${npcTracker.ready} ${worldPlayers}")

                    if (!npcTracker.ready) continue

                    for (def entityPlayer : worldPlayers) {
                        Player bukkitPlayer = (Player) entityPlayer.getBukkitEntity()
                        double distance = npcTracker.distance(entityPlayer)

                        if (npcTracker.viewers.contains(bukkitPlayer)) {
                            if (npcTracker.npc.dead || distance > 48D) {
                                npcTracker.removeViewer(entityPlayer)
                            } else if (npcTracker.turnTowardPlayers && distance <= 8D) {
                                npcTracker.turnToward(entityPlayer)
                            }
                        } else if (!npcTracker.npc.dead && distance < 30D) {
                            npcTracker.viewers.add(bukkitPlayer)
                            npcTracker.update(entityPlayer)
                        }
                    }

                    if (npcTracker.chatBubble != null && now >= npcTracker.chatBubble.timeout) {
                        npcTracker.chatBubble.destroy()
                        npcTracker.chatBubble = null
                    }

                    npcTracker.viewers.removeIf(Predicates.PLAYER_OFFLINE)
                }
            }
        }
    }

    NPCTracker spawn(String id, String name, Location location, Object skinHolder = null, Consumer<Player> onClick = null) {
        return spawn(id, name, location, false, true, skinHolder, onClick)
    }

    NPCTracker spawn(String id, String name, Location location, boolean invisibleNpc, boolean showNametag, Object skinHolder = null, Consumer<Player> onClick = null) {
        ServerLevel world = (location.getWorld() as CraftWorld).getHandle()
        NPCTracker previous = registered.get(id)

        if (previous != null) {
            if (previous.npc.getX() == location.getX() && previous.npc.getY() == location.getY() && previous.npc.getZ() == location.getZ() && previous.npc.getYRot() == location.getYaw() && previous.npc.getXRot() == location.getPitch() && previous.name == name && previous.skinHolder == skinHolder) {
                previous.onClick = onClick
                return previous
            }
            entities.remove(previous.npc.getId())
            previous.remove()
        }

        UUID npcId = UUID.randomUUID()
        FakeEntityPlayer npc = new FakeEntityPlayer(new GameProfile(npcId, npcId.toString().replace("-", "").substring(0, 16)), world)
        npc.setId(NPC_ID++)
        npc.setOnGround(true)
        npc.setPosRaw(location.getX(), location.getY(), location.getZ())
        npc.setYRot((location.getYaw() % 360.0F).toFloat())
        npc.setYHeadRot(location.getYaw())
        npc.setXRot((Mth.clamp(location.getPitch(), -90.0F, 90.0F) % 360.0F).toFloat())
        npc.latency = 0x71230000
        if (invisibleNpc) {
            npc.setInvisible(true)
        }

        NPCTracker tracker = new NPCTracker()
        tracker.npc = npc
        tracker.id = id
        tracker.name = name
        tracker.init(showNametag)
        tracker.skinHolder = skinHolder
        tracker.onClick = onClick

        tracker.npc.bukkitEntity.disconnect("") // force perm subs clear

        Closure complete = { UUID uuid ->
            GameProfile profile = npc.getGameProfile()
            PropertyMap properties = profile.getProperties()
            Collection<Property> collection = properties.get("textures")

            if (!collection.isEmpty()) {
                properties.remove("textures", collection.iterator().next())
            }
            (Globals.GLOBAL_TRACKING ? MySQL.getGlobalAsyncDatabase() : MySQL.getAsyncDatabase()).executeQuery("SELECT value, signature FROM latest_skins WHERE uuid_least = ? AND uuid_most = ?", { statement ->
                statement.setLong(1, uuid.getLeastSignificantBits())
                statement.setLong(2, uuid.getMostSignificantBits())
            }, { result ->
                if (result.next()) {
                    properties.put("textures", new Property("textures", result.getString(1), result.getString(2)))
                }

                tracker.ready = true
                tracker.updateNearby()
            })
        }

        if (skinHolder instanceof String) {
            try {
                UUID uuid = UUID.fromString((String) skinHolder)
                complete.call(uuid)
            } catch (IllegalArgumentException ignored) {
                DatabaseUtils.getId((String) skinHolder, { uuid, username, player ->
                    if (uuid != null) {
                        complete.call(uuid)
                    } else {
                        tracker.ready = true
                    }
                })
            }
        } else if (skinHolder instanceof UUID) {
            complete.call(skinHolder as UUID)
        } else if (skinHolder instanceof OfflinePlayer) {
            GameProfile profile = npc.getGameProfile()
            PropertyMap properties = profile.getProperties()
            if (skinHolder instanceof CraftPlayer) {
                properties.putAll(((CraftPlayer) skinHolder).getProfile().getProperties())
            } else {
//                properties.putAll(((CraftOfflinePlayer) skinHolder).getPlayerProfile().getProperties())
            }

            tracker.ready = true
        } else {
            tracker.ready = true
        }

        if (tracker.ready) {
            tracker.updateNearby()
        }
        registered.put(id, tracker)
        entities.put(npc.getId(), tracker)
        return tracker
    }

    void unregister(NPCTracker tracker) {
        tracker.remove()
        registered.entrySet().removeIf { it.value == tracker }
        entities.entrySet().removeIf { it.value == tracker }
    }

    def unload() {
        for (NPCTracker tracker : registered.values()) {
            tracker.remove()
        }
        registered.clear()
        entities.clear()
    }
}