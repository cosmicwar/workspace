package scripts.factions.patches

import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Vehicle
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.vehicle.VehicleEnterEvent

// Mob Patches
Events.subscribe(CreatureSpawnEvent.class, EventPriority.HIGHEST).filter(EventFilters.<CreatureSpawnEvent> ignoreCancelled()).handler { event ->
    if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
        if (Patches.limitBukkitSpawnerMobCollision) {
            event.getEntity().setCollidable(false)
        }
    }
}

Events.subscribe(CreatureSpawnEvent.class, EventPriority.HIGHEST).filter(EventFilters.<CreatureSpawnEvent> ignoreCancelled()).handler { event ->
    if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
        if (Patches.limitBukkitSpawnerMobCollision) {
            event.getEntity().setCollidable(false)
        }
    }
}

Events.subscribe(CreatureSpawnEvent.class, EventPriority.NORMAL).filter(EventFilters.<CreatureSpawnEvent> ignoreCancelled()).handler { event ->
    if (Patches.limitBukkitSpawnReason) {
        if (!Patches.limitBukkitSpawnReasonEntityOverride || !Patches.limitBukkitSpawnReasonEntityOverrideTypes.contains(event.getEntity().getType())) {
            if (!Patches.limitBukkitSpawnReasonTypes.contains(event.getSpawnReason())) {
                event.setCancelled(true)
            }

        }
    }
}

// Mob Combat Patches
Events.subscribe(CreatureSpawnEvent.class, EventPriority.LOW).filter(EventFilters.<CreatureSpawnEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscRevertCombatDelay) {
        event.getEntity().setMaximumNoDamageTicks(Patches.miscRevertCombatAttackFrequencyMob)
    }
}

Events.subscribe(EntityTeleportEvent.class, EventPriority.NORMAL).filter(EventFilters.<EntityTeleportEvent> ignoreCancelled()).handler { event ->
    if (event.getEntity() instanceof LivingEntity) {
        if (Patches.miscRevertCombatDelay) {
            LivingEntity livingEntity = (LivingEntity) event.getEntity()
            if (livingEntity.getType() != EntityType.PLAYER) {
                livingEntity.setMaximumNoDamageTicks(Patches.miscRevertCombatAttackFrequencyMob)
            }
        }
    }
}


// Limit Item Entity Damage
Events.subscribe(EntityDamageEvent.class, EventPriority.NORMAL).filter(EventFilters.<EntityDamageEvent> ignoreCancelled()).handler { event ->
    Entity entity = event.getEntity()
    if (entity != null) {
        if (entity instanceof Item) {
            if (Patches.limitBukkitItemEntityDamage) {
                if (Patches.limitBukkitItemEntityDamageReasons.contains(event.getCause())) {
                    Item item = (Item) entity
                    if (Patches.limitBukkitItemEntityDamageTypes.contains(item.getItemStack().getType())) {
                        event.setCancelled(true)
                    }

                }
            }
        }
    }
}

// Vehicle Limiter
Events.subscribe(VehicleEnterEvent.class, EventPriority.HIGHEST).filter(EventFilters.<VehicleEnterEvent> ignoreCancelled()).handler { event ->
    if (event.getVehicle() != null && event.getEntered() != null) {
        if (event.getEntered() instanceof Player) {
            if (Patches.miscVehicleLimiter) {
                Player player = event.getEntered() as Player
                Vehicle vehicle = event.getVehicle()
                if (Patches.miscVehicleLimiterTypes.contains(vehicle.getType())) {
                    event.setCancelled(true)
                    Players.msg(player, Patches.miscVehicleLimiterMsgDeny)
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                }

            }
        }
    }
}