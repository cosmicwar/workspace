package scripts.factions.features.customitem.buckets.util

import com.google.common.collect.Sets
import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.MaxChangedBlocksException
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.world.block.BaseBlock
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.enviorment.Exports
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.relation.RelationType
import scripts.factions.features.customitem.buckets.data.GenBucketData
import scripts.shared.legacy.utils.FastItemUtils

import java.util.function.BiConsumer

@CompileStatic(TypeCheckingMode.SKIP)
class GenUtils {
    static Map<String, ?> config = Exports.ptr("genbucketconfig") as Map<String, ?>

    static ItemStack createGenBucket(GenDirection direction, Material material) {
        println()
        String mat = material.toString().toLowerCase()
        String dir = direction.toString().toLowerCase()
        String name = config["direction"][dir][mat]["name"] as String
        int cost = config["direction"][dir][mat]["cost"] as int

        ItemStack genBucket = FastItemUtils.createItem(
                Material.LAVA_BUCKET, //material,
                "§l${name}",
                ["§c ▎ §7Price§7: §c${cost.toString()}"] as List<String>
        )

        FastItemUtils.setShiny(genBucket)
        new GenBucketData(name, direction, material, cost).write(genBucket)

        return genBucket
    }

    static void attemptSetBlock(EditSession editSession, Location location, Material material, int data) {
        try {
            BlockVector3 blockVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ())
            BaseBlock baseBlock = new BaseBlock(material.getKey().getKey() as int, data)
            editSession.setBlock(blockVector, baseBlock)

        } catch (MaxChangedBlocksException e) {
            e.printStackTrace()
        }
    }

    static boolean isInsideWorldBorder(Location location, boolean inside) {
        WorldBorder worldBorder = location.getWorld().getWorldBorder()

        double radius = worldBorder.getSize() / 2.0D

        if (inside) radius -= 1.0D

        double x = location.getX()
        double z = location.getZ()
        double lowerX = worldBorder.getCenter().getBlockX() - radius
        double lowerZ = worldBorder.getCenter().getBlockZ() - radius
        double upperX = worldBorder.getCenter().getBlockX() + radius
        double upperZ = worldBorder.getCenter().getBlockZ() + radius

        return (!(x >= upperX)) && (!(x < lowerX)) && (!(z >= upperZ)) && (!(z < lowerZ))
    }

    static Set<Player> getNearbyEnemyPlayers(Player player, double range, boolean includeSelf) {
        if (player == null) return Collections.emptySet()

        return getNearbyEnemyPlayers(player, player.getLocation(), range, includeSelf)
    }

    static Set<Player> getNearbyEnemyPlayers(Player player, Location source, double range, boolean includeSelf) {
        Set<Player> nearbyEnemies = Sets.newHashSet()
        if (player == null) return nearbyEnemies

        Member p1 = Factions.getMember(player.getUniqueId())
        getNearbyPlayers(player, source, range, includeSelf).forEach(otherPlayer -> {
            Member otherP = Factions.getMember(otherPlayer.getUniqueId())
            if (Factions.getRelationType(p1, otherP).isAtLeast(RelationType.TRUCE)) return

            nearbyEnemies.add(otherPlayer)
        })

        return nearbyEnemies
    }

    static Set<Player> getNearbyPlayers(Player player, double range, boolean includeSelf) {
        return getNearbyPlayers(player, player.getLocation(), range, includeSelf)
    }

    static Set<Player> getNearbyPlayers(Player player, Location source, double range, boolean includeSelf) {
        Set<Player> nearbyPlayers = Sets.newHashSet()

        player.getNearbyEntities(range, range, range).forEach(entity -> {
            if (!(entity instanceof Player)) return

            Player otherPlayer = (Player) entity

            if (otherPlayer.getGameMode() != GameMode.SURVIVAL) return
            if (!player.canSee(otherPlayer)) return

            nearbyPlayers.add((Player) entity)
        })

        if (includeSelf) nearbyPlayers.add(player)

        return nearbyPlayers
    }

    static boolean isEnemyNearby(Player player, int range) {
        if (getNearbyEnemyPlayers(player, (double) range, false).isEmpty()) return false
        return true
    }

    static final BiConsumer<Block, Material> setBlock = { Block block, Material material ->
        block.setType(material)
    }
}
