package scripts.factions.patches

import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.GlassPane
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.projectiles.ProjectileSource
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.relation.FactionUserData
import scripts.shared.legacy.utils.BroadcastUtils
import scripts.shared.utils.BukkitUtils
import scripts.shared.utils.DurationUtils
import scripts.shared.utils.Formats
import scripts.shared.utils.WorldUtils

import java.lang.ref.WeakReference

//Events.subscribe(EntityDamageEvent.class, EventPriority.HIGHEST).filter(EventFilters.<EntityDamageEvent> ignoreCancelled()).handler { event ->
//}

Events.subscribe(PlayerTeleportEvent.class, EventPriority.NORMAL).filter(EventFilters.<PlayerTeleportEvent> ignoreCancelled()).handler { event ->
    if (Patches.exploitEnderpearlLimitations) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Location to = event.getTo()
            Block block = to.getBlock()
            Player player = event.getPlayer()
            boolean isCitizensNPC = player.hasMetadata("NPC")
            if (!isCitizensNPC) {
                Material toMaterial = to.getBlock().getType()
//                if (toMaterial != Material.GLASS_PANE && !toMaterial.name().contains("_FENCE") && !toMaterial.name().contains("_FENCE_GATE") && !toMaterial.name().contains("_WALL") && !(block.getBlockData() instanceof GlassPane) && !(block.getBlockData() instanceof Stairs) && !(block.getBlockData() instanceof Slab)) {
                    Block up1 = block.getRelative(BlockFace.UP, 1)
                    Block up2 = block.getRelative(BlockFace.UP, 2)
                    if (!WorldUtils.isBlockLocationSafe(up1) || !WorldUtils.isBlockLocationSafe(up2)) {
                        Block down1 = block.getRelative(BlockFace.DOWN, 1)
                        Block down2 = block.getRelative(BlockFace.DOWN, 2)
                        if (!WorldUtils.isBlockLocationSafe(down1) || !WorldUtils.isBlockLocationSafe(down2)) {
                            event.setCancelled(true)
                            Players.msg(player, Patches.exploitEnderpearlLimitationsMsgDeny)
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                            return
                        }
                    }

                    if (!block.getType().isSolid()) {
                        event.setTo(event.getTo().subtract(0.0, to.getY() - (double) ((int) to.getY()), 0.0))
                    }

                    if (block.getRelative(BlockFace.UP).getType().isSolid()) {
                        event.setTo(to.subtract(0.0, 1.0, 0.0))
                    }

                    to.setX((double) to.getBlockX() + 0.5)
                    to.setZ((double) to.getBlockZ() + 0.5)
                    event.setTo(to)
//
            }
        }
    }
}

Events.subscribe(PlayerInteractEvent.class, EventPriority.NORMAL).handler { event ->
    if (Patches.exploitEnderpearlLimitations) {
        Player player = event.getPlayer()
        if (BukkitUtils.hasItemSelected(player, Material.ENDER_PEARL)) {
            if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true)
                } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                    Block block = player.getLocation().getBlock()
                    if (block.getRelative(BlockFace.UP).getType().isSolid() || block.getType().isSolid()) {
                        event.setCancelled(true)
                        Players.msg(player, Patches.exploitEnderpearlLimitationsMsgWithinBlockDeny)
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                    }
                }
            }
        }
    }
}

Events.subscribe(PlayerInteractEvent.class, EventPriority.NORMAL).handler { event ->
    if (Patches.miscEnderpearlCooldownLength > 0) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (event.getHand() == EquipmentSlot.HAND) {
                Player player = event.getPlayer()
                if (BukkitUtils.hasItemSelected(player, Material.ENDER_PEARL)) {
                    if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                        Member factionUserData = Factions.getMember(player.getUniqueId())
                        if (factionUserData.isThrowingPearl()) {
                            event.setCancelled(true)
                            player.updateInventory()
                            Players.msg(player, Patches.miscEnderpearlCooldownMsgDenyNonLand)
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                        } else {
                            long enderpearl = DurationUtils.calculateRemaining(factionUserData.getEnderpearlCooldown())
                            long door = DurationUtils.calculateRemaining(factionUserData.getEnderpearlDoorCooldown())
                            if (enderpearl >= 0L || door >= 0L) {
                                event.setCancelled(true)
                                player.updateInventory()
                                Players.msg(player, Patches.miscEnderpearlCooldownMsgDeny.replace("{duration}", Formats.formatTimeMillis(enderpearl > door ? enderpearl : door)))
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                            }
                        }
                    }
                }
            }
        }
    }
}

Events.subscribe(PlayerTeleportEvent.class, EventPriority.MONITOR).filter(EventFilters.<PlayerTeleportEvent> ignoreCancelled()).handler { event ->
    if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
        Player player = event.getPlayer()
        if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
            Member factionUserData = Factions.getMember(player.getUniqueId())
            factionUserData.setEnderpearlCooldown(System.currentTimeMillis() + (long) Patches.miscEnderpearlCooldownLength * 1000L)
            if (Patches.miscEnderpearlCooldownItemVisual) {
                player.setCooldown(Material.ENDER_PEARL, Patches.miscEnderpearlCooldownLength * 20)
            }

        }
    }
}

Events.subscribe(PlayerInteractEvent.class, EventPriority.MONITOR).handler { event ->
    if (Patches.miscEnderpearlDoorCooldownLength > 0) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getHand() == EquipmentSlot.HAND) {
                if (event.getClickedBlock().getType().name().contains("_DOOR")) {
                    Player player = event.getPlayer()
                    if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                        Member factionUserData = Factions.getMember(player.getUniqueId())
                        factionUserData.setEnderpearlDoorCooldown(System.currentTimeMillis() + (long) Patches.miscEnderpearlDoorCooldownLength * 1000L)
                    }
                }
            }
        }
    }
}

Events.subscribe(ProjectileLaunchEvent.class, EventPriority.MONITOR).filter(EventFilters.<ProjectileLaunchEvent> ignoreCancelled()).handler { event ->
    if (event.getEntityType() == EntityType.ENDER_PEARL) {
        EnderPearl enderpearl = (EnderPearl) event.getEntity()
        ProjectileSource shooter = enderpearl.getShooter()
        if (shooter instanceof Player) {
            Member factionUserData = Factions.getMember(((Player) shooter).getUniqueId())
            if (factionUserData != null) {
                factionUserData.setPearl(new WeakReference(enderpearl))
            }
        }
    }
}