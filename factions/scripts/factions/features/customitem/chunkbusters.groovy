package scripts.factions.features.customitem

import com.google.common.collect.Sets
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.filter.EventFilters
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import scripts.factions.core.faction.FactionUtils
import scripts.shared.legacy.utils.DatabaseUtils
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils

import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.IntStream

Commands.create().assertPermission("commands.givechunkbuster").assertUsage("<player> [usages]").handler { command ->
    DatabaseUtils.getId(command.rawArg(0), { uuid, username, player ->
        if (uuid == null) {
            command.reply("§] §> §a${username} §fhas never joined the server before!")
            return
        }

        if (command.args().size() == 1) {
            FastInventoryUtils.addOrBox(uuid, player, command.sender(), ChunkBusterUtils.createChunkBuster(), "§b§lSERVER §> §fYou have received a chunk buster!")
            command.reply("§] §> §fSuccessfully gave §a${username} §fa §a1 §fchunk buster!")
            return
        }

        Integer usages = command.arg(1).parseOrFail(Integer.class)

        FastInventoryUtils.addOrBox(uuid, player, command.sender(), ChunkBusterUtils.createChunkBuster(usages), "§b§lSERVER §> §fYou have received a ${usages}x chunk buster!")
        command.reply("§] §> §fSuccessfully gave §a${username} §fa §a${usages}x §fchunk buster!")
    })
}.register("givechunkbuster")

Events.subscribe(BlockPlaceEvent.class, EventPriority.NORMAL).filter(EventFilters.<BlockPlaceEvent> ignoreCancelled()).handler { event ->
    ItemStack item = event.hand == EquipmentSlot.HAND ? event.player.inventory.getItemInMainHand() : event.player.inventory.getItemInOffHand()

    if (item == null || item.type != ChunkBusterUtils.chunkBusterMaterial) return

    Block block = event.block
    if (block == null || block.type == Material.AIR) return

    if (block.type != ChunkBusterUtils.chunkBusterMaterial) return

    if (!FastItemUtils.hasTagFast(item, ChunkBusterUtils.CHUNK_BUSTER_KEY)) return

    if (event.isCancelled()) return

    Player player = event.player

    event.setCancelled(true)

    if (!FactionUtils.hasFaction(player)) {
        player.sendMessage("§c§l * §cYou must be in a faction to use this!")
        player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
        return
    }

    if (!FactionUtils.isPlayerOfFactionLand(player, event.blockPlaced.location)) {
        player.sendMessage("§c§l * §cYou must be in your own faction land to use this!")
        player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
        return
    }

    Integer usages = FastItemUtils.getCustomTag(item, ChunkBusterUtils.CHUNK_BUSTER_KEY, ItemTagType.INTEGER)

    if (usages == 0) {
        player.sendMessage("§c§l * §cYou have no more usages left on this chunk buster!")
        player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
        return
    }

    player.sendMessage("§a§lChunk buster placed, please wait 5 seconds.")
    Schedulers.sync().runLater({
        Set<Block> removeBlocks = Sets.newHashSet()
        Chunk chunk = block.chunk
        int xC = chunk.x << 4
        int zC = chunk.z << 4

        World world = block.world

        boolean fullRemove = !player.isSneaking()

        int removeHeight = fullRemove ? world.getMaxHeight() : block.y

        removeBlocks = (Set) IntStream.range(xC, xC + 16).mapToObj((pX) -> {
            return IntStream.range(zC, zC + 16).mapToObj((pZ) -> {
                return IntStream.rangeClosed(-63, removeHeight).mapToObj((pY) -> {
                    return world.getBlockAt(pX, pY, pZ)
                })
            })
        }).flatMap(Function.identity()).flatMap(Function.identity()).filter((blockC) -> {
            return !blockC.isEmpty()
        }).filter((blockC) -> {
            return blockC.type != Material.AIR && blockC.type != Material.CAVE_AIR
        }).filter((blockC) -> {
            return !(blockC instanceof Container)
        }).filter((blockC) -> {
            return !blockC.type.name().endsWith("_SHULKER_BOX")
        }).filter((blockC) -> {
            return !ChunkBusterUtils.chunkBusterBlacklist.contains(blockC.type)
        }).collect(Collectors.toSet())

        if (removeBlocks == null || removeBlocks.isEmpty()) {
            player.sendMessage("§c§l * §cThere are no blocks to remove in this chunk!")
            player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
            return
        }

        ChunkBusterUtils.updateLore(item, usages - 1)
        FastItemUtils.setCustomTag(item, ChunkBusterUtils.CHUNK_BUSTER_KEY, ItemTagType.INTEGER, usages - 1)

        player.updateInventory()
        block.world.spawnParticle(Particle.EXPLOSION_HUGE, block.location.toCenterLocation(), 1, 0.0D, 0.0D, 0.0D, 1.0D)
        block.world.spawnParticle(Particle.CLOUD, block.location.toCenterLocation(), 8, 0.0, 0.0D, 0.0D, 0.4000000059604645D)
        block.world.spawnParticle(Particle.SMALL_FLAME, block.location.toCenterLocation(), 4, 0.0D, 0.0D, 0.0D, 0.30000001192092896D)
        block.world.playSound(block.location, Sound.ENTITY_GENERIC_EXPLODE, 5.0F, 1.0F)
        removeBlocks.forEach(ChunkBusterUtils.setAir)
        player.sendMessage("§a§l * §aSuccessfully removed §f${removeBlocks.size()} §ablocks from this chunk!")
        player.playSound(player.location, Sound.ENTITY_VILLAGER_YES, 5.0F, 1.0F)
    }, 5, TimeUnit.SECONDS)
}

class ChunkBusterUtils {
    static NamespacedKey CHUNK_BUSTER_KEY = new NamespacedKey(Starlight.plugin, "chunk_buster")
    static Material chunkBusterMaterial = Material.BEACON

    static Set<Material> chunkBusterBlacklist = [
            Material.CHEST,
            Material.SPAWNER
    ]

    static final Consumer<Block> setAir = { Block block ->
        block.setType(Material.AIR)
    }

    static ItemStack createChunkBuster(int usages = 1) {
        ItemStack item = FastItemUtils.createItem(chunkBusterMaterial, "§6§l * Chunk Buster * ", [
                "§8--------------------------",
                "§8➥ §7Place while crouching to remove blocks",
                "§7only at and under the placement location.",
                "",
                "§7Usages: §f$usages",
                ""
        ])
        FastItemUtils.setCustomTag(item, CHUNK_BUSTER_KEY, ItemTagType.INTEGER, usages)
        FastItemUtils.ensureUnique(item)

        return item
    }

    static void updateLore(ItemStack chunkBuster, int usages) {
        List<String> lore = FastItemUtils.getLore(chunkBuster) ?: new ArrayList<String>()

        if (lore.isEmpty()) {
            lore.add("")
            lore.add("§7Usages: §f$usages")
        } else {
            lore.set(4, "§7Usages: §f$usages")
        }

        FastItemUtils.setLore(chunkBuster, lore)
    }
}