package scripts

import com.google.common.util.concurrent.RateLimiter
import org.starcade.starlight.helper.Events
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.projectiles.ProjectileSource
import org.bukkit.util.Vector
import scripts.shared.legacy.DropableUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.utils.Persistent
import scripts.shared.utils.Temple

ItemRateUtils.init()

if (Temple.templeId != "techmullet") {
    Events.subscribe(PlayerJoinEvent.class, EventPriority.HIGHEST).handler { event ->
        Inventory inventory = event.getPlayer().getInventory()

        ItemStack bow = FastItemUtils.createItem(Material.BOW, "§d§lTeleport Bow", [])
        FastItemUtils.setUnbreakable(bow)
        bow.addEnchantment(Enchantment.ARROW_INFINITE, 1)
        DropableUtils.setNotDropable(bow)

        ItemStack arrow = FastItemUtils.createItem(Material.ARROW, "", [])
        DropableUtils.setNotDropable(arrow)

        ItemStack rod = FastItemUtils.createItem(Material.FISHING_ROD, "§6§lGrappling Hook", [])
        FastItemUtils.setUnbreakable(rod)
        DropableUtils.setNotDropable(rod)

        hideData(bow, rod)

        inventory.setItem(1, bow)
        inventory.setItem(27, arrow)
        inventory.setItem(7, rod)
    }
}

static void hideData(ItemStack... items) {
    for (ItemStack item : items) {
        ItemMeta meta = item.getItemMeta()
        meta.addItemFlags(ItemFlag.values())
        item.setItemMeta(meta)
    }
}

Events.subscribe(PlayerQuitEvent.class).handler { event ->
    ItemRateUtils.PLAYER_LIMITERS.remove(event.getPlayer().getUniqueId())
}

static boolean acquireFromLimiter(Player player) {
    UUID uuid = player.getUniqueId()

    if (!ItemRateUtils.PLAYER_LIMITERS.containsKey(uuid)) {
        ItemRateUtils.PLAYER_LIMITERS.put(uuid, RateLimiter.create(5.0))
    }
    return ItemRateUtils.PLAYER_LIMITERS.get(uuid).tryAcquire()
}

static void onGrapple(Player player, Entity caught, Location location) {
    if (acquireFromLimiter(player)) {
        if (player == caught) {
            if (player.getLocation().distance(location) < 3) {
                pullPlayerSlightly(player, location)
            } else {
                pullEntityToLocation(player, location)
            }
        } else {
            pullEntityToLocation(caught, location)
        }
        playGrappleSound(player)
    }
}

static double clamp(double min, double value, double max) {
    if (min > value) {
        return min
    } else if (max < value) {
        return max
    } else {
        return value
    }
}

static Vector clampVelocity(Vector velocity) {
    velocity.setX(clamp(-4.0, velocity.getX(), 4.0))
    velocity.setY(clamp(-4.0, velocity.getY(), 4.0))
    velocity.setZ(clamp(-4.0, velocity.getZ(), 4.0))
    return velocity
}

static void pullPlayerSlightly(Player player, Location location) {
    if (location.getY() > player.getLocation().getY()) {
        player.setVelocity(new Vector(0.0, 0.4, 0.0))
    } else {
        player.setVelocity(clampVelocity(location.toVector().subtract(player.getLocation().toVector())))
    }
}

static void pullEntityToLocation(Entity entity, Location location) {
    Location entityLocation = entity.getLocation()
    entityLocation.setY(entityLocation.getY() + 0.5D)
    entity.teleport(entityLocation)
    double g = -0.08D
    double d = location.distance(entityLocation)
    double vX = (1.0D + 0.12D * d) * (location.getX() - entityLocation.getX()) / d
    double vY = (1.0D + 0.06D * d) * (location.getY() - entityLocation.getY()) / d - 0.5D * g * d
    double vZ = (1.0D + 0.12D * d) * (location.getZ() - entityLocation.getZ()) / d
    entity.setVelocity(clampVelocity(new Vector(vX, vY, vZ)))
}

static void playGrappleSound(Player player) {
    player.playSound(player.getLocation(), Sound.ENTITY_MAGMA_CUBE_JUMP, 1.0, 2.0)
}

Events.subscribe(PlayerFishEvent.class).handler { event ->
    if (event.isCancelled()) {
        return
    }
    Player player = event.getPlayer()
    PlayerFishEvent.State state = event.getState()

    if (state == PlayerFishEvent.State.IN_GROUND || state == PlayerFishEvent.State.FAILED_ATTEMPT) {
        onGrapple(player, player, event.getHook().getLocation())
    } else if (state == PlayerFishEvent.State.CAUGHT_ENTITY) {
        event.setCancelled(true)

        Entity caught = event.getCaught()

        if (caught instanceof Player) {
            //if (caught.isOp()) {
            //	Players.msg(player, caught.getDisplayName() + colorize("&c can not be pulled with grappling hooks!"))
            //} else {
            onGrapple(player, caught, player.getLocation())
            //}
            event.setCancelled(false)
        } else {
            onGrapple(player, caught, player.getLocation())
        }
    } else if (state == PlayerFishEvent.State.CAUGHT_FISH) {
        event.setCancelled(true)
    }
}

Events.subscribe(ProjectileHitEvent.class).handler { event ->
    Entity entity = event.getEntity()

    if (!(entity instanceof Arrow)) {
        return
    }
    ProjectileSource shooter = entity.getShooter()

    if (!(shooter instanceof Player)) {
        return
    }
    World world = entity.getWorld()
    WorldBorder border = world.getWorldBorder()

    if (border != null && !border.isInside(entity.getLocation())) {
        (shooter as Player).sendMessage("§c§lERROR §> §fYou cannot go there!")
        entity.remove()
        return
    }
    Location location = entity.getLocation().clone()
    location.setYaw(shooter.getLocation().getYaw())
    location.setPitch(shooter.getLocation().getPitch())
    shooter.teleport(location)
    entity.remove()
}

//Events.subscribe(PlayerInteractEvent.class, EventPriority.MONITOR).filter(EventFilters.ignoreNotCancelled()).handler { event ->
//    if ((event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) || event.getItem().getType() != Material.FISHING_ROD) {
//        return
//    }
//    event.setCancelled(false)
//}

/*Events.subscribe(PlayerInteractEvent.class).handler { event ->
    if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
        return
    }
    Player player = event.getPlayer()
    ItemStack itemInHand = event.getItem()

    if (itemInHand?.getType() == Material.BOW && acquireFromLimiter(player)) {
        player.launchProjectile(Arrow.class)
    }
}*/

class ItemRateUtils {
    static Map<UUID, RateLimiter> PLAYER_LIMITERS

    static void init() {
        PLAYER_LIMITERS = Persistent.of("player_limiters", new HashMap<>()).get() as Map<UUID, RateLimiter>
    }
}