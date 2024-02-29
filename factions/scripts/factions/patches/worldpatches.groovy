package scripts.factions.patches

import com.destroystokyo.paper.event.entity.EnderDragonFireballHitEvent
import com.destroystokyo.paper.event.entity.EnderDragonFlameEvent
import com.destroystokyo.paper.event.entity.EnderDragonShootFireballEvent
import com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent
import com.destroystokyo.paper.event.entity.EndermanEscapeEvent
import com.destroystokyo.paper.event.entity.WitchConsumePotionEvent
import com.destroystokyo.paper.event.entity.WitchReadyPotionEvent
import com.destroystokyo.paper.event.entity.WitchThrowPotionEvent
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Chunk
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.entity.Wither
import org.bukkit.entity.WitherSkull
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.EntityBlockFormEvent
import org.bukkit.event.block.SpongeAbsorbEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.event.world.PortalCreateEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import scripts.factions.core.faction.FactionUtils
import scripts.factions.core.faction.perm.perms.BuildPermission
import scripts.shared.utils.WorldUtils

import javax.annotation.Nonnull

Events.subscribe(WorldLoadEvent.class, EventPriority.MONITOR).handler { event ->
    if (Patches.miscDisableBukkitAchievementMessages) {
        event.getWorld().setGameRuleValue("announceAdvancements", "false")
        event.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
    }
}

Events.subscribe(BlockFromToEvent.class, EventPriority.NORMAL).filter(EventFilters.<BlockFromToEvent> ignoreCancelled()).handler { event ->
    Block block = event.getBlock()
    Block blockTo = event.getToBlock()
    if (blockTo.getLocation() != block.getLocation()) {
        if (WorldUtils.isLiquid(block.getType())) {
            if (!WorldUtils.isLiquid(blockTo.getType())) {
                if (WorldUtils.isOutsideWorldBorder(blockTo, false)) {
                    event.setCancelled(true)
                }

            }
        }
    }
}

Events.subscribe(EntityExplodeEvent.class, EventPriority.NORMAL).filter(EventFilters.<EntityExplodeEvent> ignoreCancelled()).handler { event ->
    event.blockList().removeIf((block) -> {
        return WorldUtils.isOutsideWorldBorder(block, false)
    })
}

Events.subscribe(ExplosionPrimeEvent.class, EventPriority.NORMAL).filter(EventFilters.<ExplosionPrimeEvent> ignoreCancelled()).handler { event ->
    if (event.getEntity().getType() == EntityType.PRIMED_TNT) {
        if (WorldUtils.isOutsideWorldBorder(event.getEntity(), true)) {
            event.setCancelled(true)
            event.getEntity().remove()
        }
    }
}

Events.subscribe(EntitySpawnEvent.class, EventPriority.NORMAL).filter(EventFilters.<EntitySpawnEvent> ignoreCancelled()).handler { event ->
    if (WorldUtils.isOutsideWorldBorder(event.getLocation(), false)) {
        event.setCancelled(true)
    }
}
Events.subscribe(ProjectileHitEvent.class, EventPriority.NORMAL).filter(EventFilters.<ProjectileHitEvent> ignoreCancelled()).handler { event ->
    if (WorldUtils.isOutsideWorldBorder(event.getEntity().getLocation(), false)) {
        event.getEntity().remove()
    }
}
Events.subscribe(EntityTeleportEvent.class, EventPriority.NORMAL).filter(EventFilters.<EntityTeleportEvent> ignoreCancelled()).handler { event ->
    if (WorldUtils.isOutsideWorldBorder(event.getTo(), false)) {
        event.setCancelled(true)
    }
}
Events.subscribe(EntityChangeBlockEvent.class, EventPriority.NORMAL).filter(EventFilters.<EntityChangeBlockEvent> ignoreCancelled()).handler { event ->
    if (event.getEntity() instanceof FallingBlock) {
        if (WorldUtils.isOutsideWorldBorder(event.getBlock(), true)) {
            event.setCancelled(true)
            event.getBlock().breakNaturally()
        }
    }
}
Events.subscribe(PlayerBucketFillEvent.class, EventPriority.NORMAL).filter(EventFilters.<PlayerBucketFillEvent> ignoreCancelled()).handler { event ->
    if (WorldUtils.isOutsideWorldBorder(event.getBlockClicked(), false)) {
        event.setCancelled(true)
    }
}
Events.subscribe(PlayerBucketEmptyEvent.class, EventPriority.NORMAL).filter(EventFilters.<PlayerBucketEmptyEvent> ignoreCancelled()).handler { event ->
    if (WorldUtils.isOutsideWorldBorder(event.getBlockClicked(), false)) {
        event.setCancelled(true)
    }
}
Events.subscribe(BlockBreakEvent.class, EventPriority.NORMAL).filter(EventFilters.<BlockBreakEvent> ignoreCancelled()).handler { event ->
    if (WorldUtils.isOutsideWorldBorder(event.getBlock(), false)) {
        event.setCancelled(true)
    }
}
Events.subscribe(BlockPlaceEvent.class, EventPriority.NORMAL).filter(EventFilters.<BlockPlaceEvent> ignoreCancelled()).handler { event ->
    if (WorldUtils.isOutsideWorldBorder(event.getBlock(), false)) {
        event.setCancelled(true)
    }
}
Events.subscribe(PlayerTeleportEvent.class, EventPriority.NORMAL).filter(EventFilters.<PlayerTeleportEvent> ignoreCancelled()).handler { event ->
    if (WorldUtils.isOutsideWorldBorder(event.getTo(), false)) {
        event.setCancelled(true)
    }
}
Events.subscribe(VehicleEnterEvent.class, EventPriority.NORMAL).filter(EventFilters.<VehicleEnterEvent> ignoreCancelled()).handler { event ->
    if (WorldUtils.isOutsideWorldBorder(event.getVehicle().getLocation(), false)) {
        event.setCancelled(true)
    }
}
Events.subscribe(BlockDamageEvent.class, EventPriority.HIGHEST).filter(EventFilters.<BlockDamageEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscSponge) {
        Block block = event.getBlock()
        if (block != null && block.getType() != Material.AIR) {
            if (block.getType() == Material.SPONGE) {
                if (!event.isCancelled()) {
                    Player player = event.getPlayer()
                    if (FactionUtils.isBreakNotAllowed(player, block.getLocation())) {
                        event.setCancelled(true)
                    } else {
                        event.setInstaBreak(true)
                    }
                }
            }
        }
    }
}
Events.subscribe(BlockFromToEvent.class, EventPriority.HIGH).filter(EventFilters.<BlockFromToEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscSponge) {
        Block block = event.getBlock()
        Block blockTo = event.getToBlock()
        if (!blockTo.getLocation().equals(block.getLocation())) {
            if (WorldUtils.isLiquid(block.getType())) {
                if (!WorldUtils.isLiquid(blockTo.getType())) {
                    Chunk chunk = blockTo.getChunk()

                    for (int x = blockTo.getX() - Patches.miscSpongeRadius; x <= blockTo.getX() + Patches.miscSpongeRadius; ++x) {
                        for (int y = blockTo.getY() - Patches.miscSpongeRadius; y <= blockTo.getY() + Patches.miscSpongeRadius; ++y) {
                            for (int z = blockTo.getZ() - Patches.miscSpongeRadius; z <= blockTo.getZ() + Patches.miscSpongeRadius; ++z) {
                                if (y > -64 && y < 320) {
                                    if (x >> 4 != chunk.getX() || z >> 4 != chunk.getZ()) {
                                        chunk = block.getWorld().getChunkAt(x >> 4, z >> 4)
                                    }

                                    Block b = chunk.getBlock(x & 15, y, z & 15)
                                    if (b.getType() == Material.SPONGE) {
                                        event.setCancelled(true)
                                        return
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}
Events.subscribe(BlockPlaceEvent.class, EventPriority.MONITOR).filter(EventFilters.<BlockPlaceEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscSponge) {
        ItemStack item = event.getHand() == EquipmentSlot.HAND ? event.getPlayer().getInventory().getItemInMainHand() : event.getPlayer().getInventory().getItemInOffHand()
        if (item != null && item.getType() != Material.AIR) {
            if (item.getType() == Material.SPONGE) {
                if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().hasLore()) {
                    Block block = event.getBlock()
                    if (block != null && block.getType() != Material.AIR) {
                        if (block.getType() == Material.SPONGE) {
                            if (!event.isCancelled()) {
                                Player player = event.getPlayer()
                                // TODO
                                if (!BuildPermission.canBuild(player, block.getLocation())) {
                                    event.setCancelled(true)
                                } else {
                                    for (int x = -Patches.miscSpongeRadius; x <= Patches.miscSpongeRadius; ++x) {
                                        for (int y = -Patches.miscSpongeRadius; y <= Patches.miscSpongeRadius; ++y) {
                                            for (int z = -Patches.miscSpongeRadius; z <= Patches.miscSpongeRadius; ++z) {
                                                if (y > -64 && y < 320) {
                                                    Location loc = block.getLocation()
                                                    Block b = loc.getWorld().getBlockAt(loc.clone().add((double) x, (double) y, (double) z))
                                                    double distanceY = loc.getY() > (double) b.getY() ? loc.getY() - (double) b.getY() : (double) b.getY() - loc.getY()
                                                    if (!(distanceY > (double) Patches.miscSpongeRadius)) {
                                                        if (WorldUtils.isOceanFoliage(b.getType()) && Patches.miscSpongeHandleWaterFoliage) {
                                                            b.breakNaturally()
                                                            b.setType(Material.AIR)
                                                        } else {
                                                            boolean isWaterLogged = WorldUtils.isWaterlogged(b)
                                                            if (WorldUtils.isLiquid(b.getType()) || isWaterLogged) {
                                                                if (Patches.miscSpongeHandleWaterlogged && isWaterLogged) {
                                                                    WorldUtils.setWaterlogged(b, false)
                                                                } else {
                                                                    b.setType(Material.AIR)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
Events.subscribe(BlockPhysicsEvent.class, EventPriority.HIGH).filter(EventFilters.<BlockPhysicsEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscSponge) {
        Block block = event.getBlock()
        if (block != null && block.getType() != Material.AIR) {
            if (block.getType() == Material.SPONGE) {
                event.setCancelled(true)
            }
        }
    }
}
Events.subscribe(SpongeAbsorbEvent.class, EventPriority.HIGH).filter(EventFilters.<SpongeAbsorbEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscSponge) {
        event.setCancelled(true)
    }
}
Events.subscribe(EntityExplodeEvent.class, EventPriority.MONITOR).filter(EventFilters.<EntityExplodeEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscSponge) {
        event.blockList().forEach((block) -> {
            if (block != null && block.getType() == Material.SPONGE) {
                block.breakNaturally()
            }

        })
    }
}
Events.subscribe(BlockPistonExtendEvent.class, EventPriority.HIGH).filter(EventFilters.<BlockPistonExtendEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscSponge) {
        event.getBlocks().forEach((block) -> {
            if (block != null && block.getType() == Material.SPONGE) {
                event.setCancelled(true)
            }

        })
    }
}
Events.subscribe(BlockPistonRetractEvent.class, EventPriority.HIGH).filter(EventFilters.<BlockPistonRetractEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscSponge) {
        BlockFace direction = event.getDirection()
        Block movedBlock = event.getBlock().getRelative(direction).getRelative(direction)
        if (movedBlock != null && movedBlock.getType() == Material.SPONGE) {
            event.setCancelled(true)
        }

    }
}
Events.subscribe(PlayerTeleportEvent.class, EventPriority.NORMAL).filter(EventFilters.<PlayerTeleportEvent> ignoreCancelled()).handler { event ->
    if (Patches.limitPlayerNetherHeight) {
        Player player = event.getPlayer()
        if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
            Location location = event.getTo()
            if (location.getWorld().getEnvironment() == World.Environment.NETHER) {
                if (!(location.getY() < (double) Patches.limitPlayerNetherHeightYLevel)) {
                    event.setCancelled(true)
                    Players.msg(player, Patches.limitPlayerNetherHeightMsgDeny)
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                    if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
                        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ENDER_PEARL, 1)})
                    }

                }
            }
        }
    }
}
Events.subscribe(BlockBreakEvent.class, EventPriority.NORMAL).filter(EventFilters.<BlockBreakEvent> ignoreCancelled()).handler { event ->
    if (denyBlockInteract(event.getPlayer(), event.getBlock())) {
        event.setCancelled(true)
    }
}
Events.subscribe(BlockPlaceEvent.class, EventPriority.NORMAL).filter(EventFilters.<BlockPlaceEvent> ignoreCancelled()).handler { event ->
    if (denyBlockInteract(event.getPlayer(), event.getBlock())) {
        event.setCancelled(true)
    }
}

static boolean denyBlockInteract(@Nonnull Player player, @Nonnull Block block) {
    if (!Patches.limitPlayerNetherHeight) {
        return false
    } else if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
        if (block.getY() < Patches.limitPlayerNetherHeightYLevel) {
            return false
        } else if (block.getWorld().getEnvironment() != World.Environment.NETHER) {
            return false
        } else {
            Players.msg(player, Patches.limitPlayerNetherHeightMsgDeny)
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
            return true
        }
    } else {
        return false
    }
}

Events.subscribe(PlayerInteractEvent.class, EventPriority.HIGHEST).filter(EventFilters.<PlayerInteractEvent> ignoreCancelled()).handler { event ->
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        ItemStack item = event.getHand() == EquipmentSlot.HAND ? event.getPlayer().getInventory().getItemInMainHand() : event.getPlayer().getInventory().getItemInOffHand()
        if (item != null && item.getType() != Material.AIR) {
            if (item.getType() == Material.ITEM_FRAME || item.getType() == Material.ARMOR_STAND || item.getType().name().endsWith("_BOAT") || item.getType() == Material.SPAWNER) {
                Block block = event.getClickedBlock()
                if (block != null && block.getType() != Material.AIR) {
                    if (Patches.limitPlayerTileChunkPlacement) {
                        Player player = event.getPlayer()
                        if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                            int itemFrameMax = Patches.limitPlayerTileItemFrameThreshold
                            int armorStandMax = Patches.limitPlayerTileArmorStandThreshold
                            int boatMax = Patches.limitPlayerTileBoatThreshold
                            int spawnerMax = Patches.limitPlayerTileSpawnerThreshold
                            Chunk chunk = block.getChunk()
                            long spawnerCount
                            if (item.getType() == Material.ITEM_FRAME && itemFrameMax >= 1) {
                                spawnerCount = Arrays.stream(chunk.getEntities()).filter((entity) -> {
                                    return entity != null && !entity.isDead()
                                }).filter((entity) -> {
                                    return entity.getType() == EntityType.ITEM_FRAME
                                }).count()
                                if (spawnerCount >= (long) itemFrameMax) {
                                    event.setCancelled(true)
                                    player.updateInventory()
                                    Players.msg(player, Patches.limitPlayerItemFrameMsgDenyPlacement.replace("{limitCount}", String.valueOf(itemFrameMax)))
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                                    return
                                }
                            } else if (item.getType() == Material.ARMOR_STAND && armorStandMax >= 1) {
                                spawnerCount = Arrays.stream(chunk.getEntities()).filter((entity) -> {
                                    return entity != null && !entity.isDead()
                                }).filter((entity) -> {
                                    return entity.getType() == EntityType.ARMOR_STAND
                                }).count()
                                if (spawnerCount >= (long) armorStandMax) {
                                    event.setCancelled(true)
                                    player.updateInventory()
                                    Players.msg(player, Patches.limitPlayerArmorStandMsgDenyPlacement.replace("{limitCount}", String.valueOf(armorStandMax)))
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                                    return
                                }
                            } else if (item.getType().name().endsWith("_BOAT") && boatMax >= 1) {
                                spawnerCount = Arrays.stream(chunk.getEntities()).filter((entity) -> {
                                    return entity != null && !entity.isDead()
                                }).filter((entity) -> {
                                    return entity.getType() == EntityType.BOAT
                                }).count()
                                if (spawnerCount >= (long) boatMax) {
                                    event.setCancelled(true)
                                    player.updateInventory()
                                    Players.msg(player, Patches.limitPlayerBoatMsgDenyPlacement.replace("{limitCount}", String.valueOf(boatMax)))
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                                    return
                                }
                            } else if (item.getType() == Material.SPAWNER && spawnerMax >= 1) {
                                spawnerCount = Arrays.stream(chunk.getTileEntities()).filter((blockState) -> {
                                    return blockState != null
                                }).filter((blockState) -> {
                                    return blockState.getType() != Material.AIR && blockState.getType() != Material.CAVE_AIR
                                }).filter((blockState) -> {
                                    return blockState.getType() == Material.SPAWNER
                                }).count()
                                if (spawnerCount > (long) spawnerMax) {
                                    event.setCancelled(true)
                                    player.updateInventory()
                                    Players.msg(player, Patches.limitPlayerSpawnerMsgDenyPlacement.replace("{limitCount}", String.valueOf(spawnerMax)))
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                                    return
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}
Events.subscribe(BlockPlaceEvent.class, EventPriority.HIGHEST).filter(EventFilters.<BlockPlaceEvent> ignoreCancelled()).handler { event ->
    ItemStack item = event.getHand() == EquipmentSlot.HAND ? event.getPlayer().getInventory().getItemInMainHand() : event.getPlayer().getInventory().getItemInOffHand()
    if (item != null && item.getType() != Material.AIR) {
        if (item.getType().name().contains("SIGN") || item.getType() == Material.SPAWNER) {
            Block block = event.getBlockPlaced()
            if (block != null && block.getType() != Material.AIR) {
                if (Patches.limitPlayerTileChunkPlacement) {
                    Player player = event.getPlayer()
                    if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                        int signChunkMax = Patches.limitPlayerTileSignThreshold
                        int spawnerChunkMax = Patches.limitPlayerTileSpawnerThreshold
                        Chunk chunk = block.getChunk()
                        long spawnerCount
                        if (item.getType().name().contains("SIGN") && signChunkMax >= 1) {
                            spawnerCount = Arrays.stream(chunk.getTileEntities()).filter((blockState) -> {
                                return blockState != null
                            }).filter((blockState) -> {
                                return blockState.getType() != Material.AIR && blockState.getType() != Material.CAVE_AIR
                            }).filter((blockState) -> {
                                return blockState.getType().name().contains("SIGN") || blockState.getType().name().contains("WALL_SIGN")
                            }).count()
                            if (spawnerCount > (long) signChunkMax) {
                                event.setCancelled(true)
                                player.closeInventory()
                                player.updateInventory()
                                Players.msg(player, Patches.limitPlayerSignMsgDenyPlacement.replace("{limitCount}", String.valueOf(signChunkMax)))
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                                return
                            }
                        } else if (item.getType() == Material.SPAWNER && spawnerChunkMax >= 1) {
                            spawnerCount = Arrays.stream(chunk.getTileEntities()).filter((blockState) -> {
                                return blockState != null
                            }).filter((blockState) -> {
                                return blockState.getType() != Material.AIR && blockState.getType() != Material.CAVE_AIR
                            }).filter((blockState) -> {
                                return blockState.getType() == Material.SPAWNER
                            }).count()
                            if (spawnerCount > (long) spawnerChunkMax) {
                                event.setCancelled(true)
                                player.closeInventory()
                                player.updateInventory()
                                Players.msg(player, Patches.limitPlayerSpawnerMsgDenyPlacement.replace("{limitCount}", String.valueOf(spawnerChunkMax)))
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                                return
                            }
                        }

                    }
                }
            }
        }
    }
}
Events.subscribe(BlockIgniteEvent.class, EventPriority.HIGH).filter(EventFilters.<BlockIgniteEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscLimitFireSpread) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD || event.getCause() == BlockIgniteEvent.IgniteCause.LAVA) {
            event.setCancelled(true)
        }
    }
}
Events.subscribe(WeatherChangeEvent.class, EventPriority.HIGH).filter(EventFilters.<WeatherChangeEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscLimitWeatherChanges) {
        event.setCancelled(true)
    }
}
Events.subscribe(EndermanAttackPlayerEvent.class, EventPriority.NORMAL).filter(EventFilters.<EndermanAttackPlayerEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscLimitEvadeMobActions) {
        event.setCancelled(true)
    }
}
Events.subscribe(EndermanEscapeEvent.class, EventPriority.NORMAL).filter(EventFilters.<EndermanEscapeEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscLimitEvadeMobActions) {
        event.setCancelled(true)
    }
}
Events.subscribe(WitchThrowPotionEvent.class, EventPriority.NORMAL).filter(EventFilters.<WitchThrowPotionEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscLimitHostileMobActions) {
        event.setCancelled(true)
    }
}
Events.subscribe(WitchConsumePotionEvent.class, EventPriority.NORMAL).filter(EventFilters.<WitchConsumePotionEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscLimitHostileMobActions) {
        event.setCancelled(true)
    }
}
Events.subscribe(WitchReadyPotionEvent.class, EventPriority.NORMAL).filter(EventFilters.<WitchReadyPotionEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscLimitHostileMobActions) {
        event.setCancelled(true)
    }
}
Events.subscribe(EntityExplodeEvent.class, EventPriority.HIGHEST).filter(EventFilters.<EntityExplodeEvent> ignoreCancelled()).handler { event ->
    Entity entity = event.getEntity()
    if (entity != null) {
        if (entity instanceof Wither || entity instanceof WitherSkull || entity instanceof EnderDragon && Patches.miscLimitHostileMobExplosiveActions) {
            event.setCancelled(true)
        }

    }
}
Events.subscribe(EnderDragonShootFireballEvent.class, EventPriority.HIGHEST).filter(EventFilters.<EnderDragonShootFireballEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscLimitHostileMobExplosiveActions) {
        event.setCancelled(true)
    }
}
Events.subscribe(EnderDragonFireballHitEvent.class, EventPriority.HIGHEST).filter(EventFilters.<EnderDragonFireballHitEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscLimitHostileMobExplosiveActions) {
        event.setCancelled(true)
    }
}
Events.subscribe(EnderDragonFlameEvent.class, EventPriority.HIGHEST).filter(EventFilters.<EnderDragonFlameEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscLimitHostileMobExplosiveActions) {
        event.setCancelled(true)
    }
}
Events.subscribe(EntityChangeBlockEvent.class, EventPriority.HIGHEST).filter(EventFilters.<EntityChangeBlockEvent> ignoreCancelled()).handler { event ->
    EntityType entity = event.getEntityType()
    if (entity != null) {
        if (entity == EntityType.WITHER && Patches.miscLimitHostileMobExplosiveActions) {
            event.setCancelled(true)
        } else if (entity == EntityType.ENDERMAN && Patches.miscLimitHostileMobBlockActions) {
            event.setCancelled(true)
        }

    }
}
Events.subscribe(EntityBlockFormEvent.class, EventPriority.HIGHEST).filter(EventFilters.<EntityBlockFormEvent> ignoreCancelled()).handler { event ->
    EntityType entity = event.getEntity().getType()
    if (entity != null && entity == EntityType.SNOWMAN) {
        if (event.getNewState() != null && event.getNewState().getType() == Material.SNOW) {
            if (Patches.miscLimitMobBlockFormActions) {
                event.setCancelled(true)
                event.getNewState().setType(Material.AIR)
            }

        }
    }
}
Events.subscribe(EntityChangeBlockEvent.class, EventPriority.HIGH).filter(EventFilters.<EntityChangeBlockEvent> ignoreCancelled()).handler { event ->
    if (event.getEntity().getType() == EntityType.SILVERFISH) {
        if (Patches.miscLimitSilverfishBurrowing) {
            event.setCancelled(true)
        }
    }
}

Events.subscribe(EntityDamageEvent.class, EventPriority.HIGH).filter(EventFilters.<EntityDamageEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscWaterproofBlazes) {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            EntityType entity = event.getEntityType()
            if (entity != null) {
                if (entity == EntityType.BLAZE) {
                    if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
                        event.setCancelled(true)
                    }
                }
            }
        }
    }
}
Events.subscribe(PortalCreateEvent.class, EventPriority.NORMAL).filter(EventFilters.<PortalCreateEvent> ignoreCancelled()).handler { event ->
    if (Patches.exploitLimitPortalCreation) {
        if (event.getWorld().getEnvironment() == World.Environment.NETHER) {
            event.setCancelled(true)
        }
    }
}
Events.subscribe(EntityPortalEvent.class, EventPriority.HIGHEST).filter(EventFilters.<EntityPortalEvent> ignoreCancelled()).handler { event ->
    if (Patches.exploitLimitPortalEntityEntry) {
        if (event.getFrom().getWorld().getEnvironment() == World.Environment.NETHER) {
            Entity entity = event.getEntity()
            if (entity != null) {
                if (event.getEntityType() == EntityType.PLAYER) {
                    Player player = (Player) event.getEntity()
                    if (player.isInsideVehicle()) {
                        player.leaveVehicle()
                    }
                }

                if (event.getEntityType() != EntityType.PLAYER && event.getEntityType() != EntityType.WOLF && event.getEntityType() != EntityType.PARROT) {
                    event.setCancelled(true)
                }
            }
        }
    }
}

