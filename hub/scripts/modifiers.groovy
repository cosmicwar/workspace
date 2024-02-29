package scripts


import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventPriority
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import scripts.shared.legacy.utils.PlayerUtils
import scripts.shared.utils.Temple

Events.subscribe(PlayerJoinEvent.class).handler { event ->
    Player player = event.getPlayer()
    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())
    player.setFoodLevel(20)
    player.setHealth(20.0F)
    player.getInventory().setHeldItemSlot(4)


//    Location initialLocation = player.getLocation()
//    Schedulers.sync().runLater({
//        initialLocation = player.getLocation()
//        if (player.getName() == "Markoo") Starlight.log.info("LOCATION: ${initialLocation.toString()}")
//    }, 20)
//    Schedulers.async().runLater({
//        if (player.getName() == "Markoo") Starlight.log.info("LOCATION: ${player.getLocation().toString()}")
//        if (player.getLocation() == initialLocation) {
//            player.setVelocity(player.getVelocity().add(new Vector(0,10, ThreadLocalRandom.current().nextInt(0, 5) * (ThreadLocalRandom.current().nextBoolean() ? -1 : 1))))
//        }
//    }, 20 * ThreadLocalRandom.current().nextInt(10, 20))
}
Events.subscribe(FoodLevelChangeEvent.class, EventPriority.HIGHEST).handler { event ->
    if (event.getFoodLevel() != 20) {
        event.setCancelled(true)
    }
}

if (Temple.templeId != "techmullet") {
    for (Class<? extends EntityEvent> clazz : [ EntityDamageEvent.class, EntityDamageByBlockEvent.class, EntityDamageByEntityEvent.class ]) {
        Events.subscribe(clazz, EventPriority.HIGHEST).handler { event ->
            // if (event.getEntity() instanceof Player) {
            (event as Cancellable).setCancelled(true)
            //
        }
    }
}

Events.subscribe(PlayerInteractEvent.class, EventPriority.HIGHEST).handler { event ->
    if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getGameMode() != GameMode.CREATIVE && !event.getPlayer().isOp()) {
        event.setCancelled(true)
    }
}

for (Class<? extends BlockEvent> clazz : [ BlockBurnEvent.class, BlockIgniteEvent.class ]) {
    Events.subscribe(clazz, EventPriority.HIGHEST).handler { event ->
        (event as Cancellable).setCancelled(true)
    }
}

Events.subscribe(BlockPlaceEvent.class, EventPriority.HIGHEST).handler { event ->
    if (event.getPlayer().getGameMode() != GameMode.CREATIVE && !event.getPlayer().isOp()) {
        event.setCancelled(true)
    }
}

Events.subscribe(PlayerDropItemEvent.class, EventPriority.HIGHEST).handler { event -> event.setCancelled(true) }

Events.subscribe(InventoryPickupItemEvent.class, EventPriority.HIGHEST).handler { event -> event.setCancelled(true) }

Events.subscribe(InventoryClickEvent.class, EventPriority.HIGHEST).handler { event -> event.setCancelled(true) }

Events.subscribe(PlayerJoinEvent.class).handler { event ->
    Player player = event.getPlayer()
    Schedulers.sync().run({
//        player.world.execute {
            PlayerUtils.giveEffect(player, new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0))
            PlayerUtils.giveEffect(player, new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 9))
            PlayerUtils.giveEffect(player, new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 9))
//        }
    })
}

Events.subscribe(PlayerRespawnEvent.class).handler { event ->
    Player player = event.getPlayer()
    Schedulers.sync().run({
//        player.world.execute {
            PlayerUtils.giveEffect(player, new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0))
            PlayerUtils.giveEffect(player, new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 9))
            PlayerUtils.giveEffect(player, new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 9))
//        }
    })
}

Bukkit.getOnlinePlayers().groupBy { it.getWorld() }.each {
    List<Player> players = it.value
//    it.key.execute {
        players.each {
            PlayerUtils.giveEffect(it, new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 9))
        }
//    }
}
