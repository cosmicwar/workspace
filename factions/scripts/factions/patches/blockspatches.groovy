package scripts.factions.patches

import com.destroystokyo.paper.event.block.BeaconEffectEvent
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockPistonEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

import javax.annotation.Nonnull

// Limit Item Placements
Events.subscribe(PlayerInteractEvent.class, EventPriority.HIGHEST).handler { event ->
    if (Patches.miscLimitItemPlacement) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getHand() == EquipmentSlot.HAND ? event.getPlayer().getInventory().getItemInMainHand() : event.getPlayer().getInventory().getItemInOffHand()
            if (item != null && item.getType() != Material.AIR) {
                if (Patches.miscLimitItemPlacementTypes.contains(item.getType())) {
                    Player player = event.getPlayer()
                    if (player.isOp() || player.hasPermission("miscellaneous.bypass")) {
                        return
                    }

                    Block block = event.getClickedBlock()
                    if (block == null || block.getType() == Material.AIR) {
                        return
                    }

                    event.setCancelled(true)
                    event.setUseItemInHand(Event.Result.DENY)
                    player.updateInventory()
                    Players.msg(player, Patches.miscLimitItemPlacementMsgDeny)
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                }

            }
        }
    }
}

// Limit Item Placements
Events.subscribe(BlockPlaceEvent, EventPriority.NORMAL).handler { event ->
    if (Patches.miscLimitItemPlacement) {
        ItemStack item = event.getHand() == EquipmentSlot.HAND ? event.getPlayer().getInventory().getItemInMainHand() : event.getPlayer().getInventory().getItemInOffHand()
        if (item != null && item.getType() != Material.AIR) {
            if (Patches.miscLimitItemPlacementTypes.contains(item.getType())) {
                Player player = event.getPlayer()
                if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                    Block block = event.getBlockPlaced()
                    if (block != null && block.getType() != Material.AIR) {

                        event.setCancelled(true)
                        player.updateInventory()
                        Players.msg(player, Patches.miscLimitItemPlacementMsgDeny)
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                    }
                }
            }
        }
    }
}

// Limit Dispenser Items
Events.subscribe(BlockDispenseEvent.class, EventPriority.HIGHEST).filter(EventFilters.<BlockDispenseEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscLimitDispenserItems) {
        ItemStack item = event.getItem()
        if (item != null && item.getType() != Material.AIR) {
            if (Patches.miscLimitDispenserItemsTypes.contains(item.getType())) {
                event.setCancelled(true)
                event.getItem().setType(Material.AIR)
            }

        }
    }
}

// Limit Piston Glitch
Events.subscribe(BlockPistonExtendEvent.class, EventPriority.NORMAL).filter(EventFilters.<BlockPistonExtendEvent> ignoreCancelled()).handler { event ->
    denyPistonGlitch(event, event.getBlocks())
}

Events.subscribe(BlockPistonRetractEvent.class, EventPriority.NORMAL).filter(EventFilters.<BlockPistonRetractEvent> ignoreCancelled()).handler { event ->
    denyPistonGlitch(event, event.getBlocks())
}

static def denyPistonGlitch(BlockPistonEvent blockPistonEvent, List<Block> blocks) {
    if (Patches.exploitPistonLimitations) {
        blocks.forEach((toCheck) -> {
            if (Patches.exploitPistonLimitationsTypes.contains(toCheck.getType())) {
                blockPistonEvent.setCancelled(true)
            }
        })
    }
}

// Disable Respawn Anchors
Events.subscribe(PlayerInteractEvent.class, EventPriority.NORMAL).filter(EventFilters.<PlayerInteractEvent> ignoreCancelled()).handler { event ->
    if (Patches.exploitRespawnAnchorLimitations) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null) {
                Player player = event.getPlayer()
                if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                    Block block = event.getClickedBlock()
                    if (block.getType() == Material.RESPAWN_ANCHOR) {
                        event.setCancelled(true)
                        event.setUseItemInHand(Event.Result.DENY)
                        event.setUseInteractedBlock(Event.Result.DENY)
                        Players.msg(player, Patches.exploitRespawnAnchorLimitationsMsgDeny)
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                    }

                }
            }
        }
    }
}

// Limit Beacon Effects
Events.subscribe(BeaconEffectEvent.class, EventPriority.NORMAL).filter(EventFilters.<BeaconEffectEvent> ignoreCancelled()).handler { event ->
    if (event.getBlock() != null && event.getEffect() != null) {
        if (Patches.glitchLimitFactionBeaconEffects) {
            Player player = event.getPlayer()
            Location location = event.getBlock().getLocation()
            if (!FactionUtils.isNonPlayerFactionLand(player, location)) {
                if (!FactionUtils.isPlayerOfFactionLand(player, location)) {
                    event.setCancelled(true)
                }

            }
        }
    }
}

// Disable Crop Foliage Duplication
Events.subscribe(BlockPlaceEvent.class, EventPriority.HIGHEST).handler { event ->
    ItemStack item = event.getItemInHand()
    if (item != null && item.getType() != Material.AIR) {
        if (item.getType().toString().contains("BED") || item.getType().toString().contains("BANNER") || item.getType().toString().contains("DOOR")) {
            if (event.isCancelled() && Patches.exploitCropFoliageDuplication) {
                Schedulers.sync().runLater(() -> {
                    scanPlacementRadius(event.getBlock(), 1, event.getBlock().getLocation(), event.getBlock().getWorld())
                }, 1L)
            }

        }
    }
}

static def scanPlacementRadius(@Nonnull Block block, int radius, @Nonnull Location blockLocation, @Nonnull World world) {
    int xMin = blockLocation.getBlockX() - radius
    int xMax = blockLocation.getBlockX() + radius
    int zMin = blockLocation.getBlockZ() - radius
    int zMax = blockLocation.getBlockZ() + radius

    for (int x = xMin; x <= xMax; ++x) {
        for (int z = zMin; z <= zMax; ++z) {
            Block b = world.getBlockAt(x, blockLocation.getBlockY(), z)
            if (b.getBlockData() instanceof Ageable && b.getLightLevel() == 0 && b.getType() != Material.NETHER_WART) {
                b.setType(Material.AIR, true)
            }
        }
    }

}