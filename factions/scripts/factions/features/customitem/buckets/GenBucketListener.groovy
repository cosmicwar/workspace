package scripts.factions.features.customitem.buckets

import com.google.common.collect.Maps
//import com.massivecraft.factions.Factions
//import com.massivecraft.factions.engine.EnginePermBuild
//import com.massivecraft.factions.entity.BoardColl
//import com.massivecraft.factions.entity.Faction
//import com.massivecraft.massivecore.ps.PS
import org.bukkit.ChatColor
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.claim.Board
import scripts.factions.core.faction.claim.Claim
import scripts.factions.core.faction.data.Faction
import scripts.shared.data.obj.BlockPosition
import scripts.factions.features.customitem.buckets.util.GenDirection
import scripts.factions.features.customitem.buckets.util.GenUtils
import scripts.factions.features.customitem.buckets.data.GenBucketData
import scripts.shared.legacy.CurrencyStorage

import java.util.concurrent.atomic.AtomicInteger

class GenBucketListener {
    private static final BlockFace[] axis = [BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST]
    static CurrencyStorage currency
    public static Map<String, ?> config

    GenBucketListener() {
        registerEvents()
        registerCommands()
        currency = Exports.ptr((Exports.get("shop/currency", "money")) as String) as CurrencyStorage
    }

    static Map<String, ?> getConfig() {
        return config == null ? config = Exports.ptr("genbucketconfig") as Map<String, ?> : config
    }

    private static BlockFace locationToFace(Player player) {
        return yawToFace(player.getLocation().getYaw());
    }

    private static BlockFace yawToFace(float yaw) {
        println((Math.round(yaw / 90f) & 0x3) as int)
        return axis[(Math.round(yaw / 90f) & 0x3) as int]
    }

    static final Map<String, AtomicInteger> placedBuckets = Maps.newHashMap();

    static Map<String, AtomicInteger> getPlacedBuckets() {
        return placedBuckets;
    }

    static void registerEvents() {
        Events.subscribe(PlayerBucketEmptyEvent.class).filter(EventFilters.<PlayerBucketEmptyEvent> ignoreCancelled()).handler({ event ->
            Player player = event.getPlayer()
            if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) return
            GenBucketData data = GenBucketData.read(player.getItemInHand())
            if (data == null) return
            event.setCancelled(true)
            Location clicked = event.getBlockClicked().getLocation()
            BlockFace face = event.getBlockFace()
            Location block = new Location(clicked.getWorld(), clicked.getX() + face.getModX(), clicked.getY() + face.getModY(), clicked.getZ() + face.getModZ())
            Chunk chunk = block.getChunk()

            BlockPosition blockPos = new BlockPosition(block.getBlockX(), block.getBlockY(), block.getBlockZ())
            blockPos.chunkX = chunk.getX()
            blockPos.chunkZ = chunk.getZ()
            blockPos.world = block.world.name

//            PS blockPs = PS.valueOf(block)
//            blockPs = blockPs.withBlockX()
//            blockPs = blockPs.withBlockY(block.getBlockY())
//            blockPs = blockPs.withBlockZ(block.getBlockZ())
//            blockPs = blockPs.withChunkX(chunk.getX())
//            blockPs = blockPs.withChunkZ(chunk.getZ())

//            if (!EnginePermBuild.canPlayerBuildAt(player, blockPs, true)) {
//                event.setCancelled(true)
//                player.updateInventory()
//                return
//            }

            Board board = Factions.getBoard(block.world)

            if (board == null) return

            Claim claim = board.getClaimAtPos(clicked)
            Faction faction
            if (claim == null) {
                faction = Factions.getFaction(Factions.getWildernessId())
            } else {
                faction = Factions.getFaction(claim.factionId)
            }

            if (faction == null || faction.id == Factions.warZoneId || faction.id == Factions.wildernessId ) {
                event.setCancelled(true)
                player.updateInventory()
                player.sendMessage(ChatColor.RED.toString() + "You may not do that here!")
                return
            }
//            if (getConfig()["denyGenbucketPlacementInWild"] as boolean faction != null) {
//                event.setCancelled(true)
//                player.updateInventory()
//                player.sendMessage(ChatColor.RED.toString() + "You may not do that here!")
//                return
//            }
//            if (GenUtils.isEnemyNearby(player, getConfig()["nearbyenemyradius"] as int)) {
//                player.sendMessage(getConfig()["nearbyEnemyGenbucketMessage"] as String)
//                event.setCancelled(true)
//                player.updateInventory()
//                return
//            }

            if (data.getDirection() == GenDirection.VERTICAL) {
                if (block.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                    if (data.getMaterial() == Material.SAND) {
                        player.sendMessage(getConfig()["mustHaveBlockUnderMessage"] as String)
                        event.setCancelled(true)
                        return
                    }
                    currency.take(player, data.getCost() as long, {
                        GenBuckets.verticalGenTask(data, blockPos, true)
//                        player.sendMessage("§c-${data.cost}")
                    })
                } else {
                    currency.take(player, data.getCost() as long, {
                        GenBuckets.verticalGenTask(data, blockPos)
//                        player.sendMessage("§c-${data.cost}")
                    })
                }
            } else if (data.getDirection() == GenDirection.HORIZONTAL) {
                currency.take(player, data.getCost() as long, {
                    GenBuckets.horizontalGenTask(data, blockPos, locationToFace(player))
//                    player.sendMessage("§c-${data.cost}")
                })
            }
        })
    }


    static void registerCommands() {
        Commands.create().assertPlayer().handler { command ->
            Player player = command.sender()
            player.getInventory().addItem(GenUtils.createGenBucket(GenDirection.VERTICAL, Material.COBBLESTONE))
            player.updateInventory()
        }.register("gbucket")

        Commands.create().assertPlayer().handler { command ->
            GenBuckets.openGenShopMenu(command.sender())
        }.register("genshop")
    }

}
