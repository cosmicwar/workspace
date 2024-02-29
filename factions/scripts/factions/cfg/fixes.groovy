package scripts.factions.cfg

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import scripts.shared.legacy.utils.FastItemUtils

//@Field Map<Player, Long> factionLogoutCache = Persistent.of("faction_logout_cache", new ConcurrentHashMap<Player, Long>())

//Events.subscribe(PlayerQuitEvent.class).handler {event ->
//    Player player = event.getPlayer()
//
//    MPlayer mPlayer = MPlayer.get(player)
//
//    PS ps = PS.valueOf(player.getLocation())
//
//    def ta = BoardColl.get().getTerritoryAccessAt(ps)
//    def hostFaction = ta.getHostFaction()
//
//    if (hostFaction.isNone()) return
//
//    if (hostFaction == mPlayer.getFaction()) {
//        Persistent.of("faction_logout_cache").put(player, System.currentTimeMillis())
//    }
//}

//Events.subscribe(PlayerJoinEvent.class).handler { event ->
//    Player player = event.getPlayer()
//
//    player.teleportAsync(WorldConfig.getSpawn()).thenAccept {result ->
//        if (result) {
//            (Exports.ptr("handleJoin") as Closure)?.call(player)
//        }
//    }
//}

Events.subscribe(PlayerSpawnLocationEvent.class, EventPriority.LOWEST).handler { event ->
    Player player = event.getPlayer()

    if (player.hasPlayedBefore()) {
        (Exports.ptr("handleJoin") as Closure)?.call(player)
    } /*else {
        event.setSpawnLocation(WorldConfig.getSpawn())
    }*/
}

Events.subscribe(PlayerRespawnEvent.class).handler { event ->
    event.setRespawnLocation(WorldConfig.getSpawn())
}

Commands.create().assertPlayer().handler { command ->
    command.sender().teleportAsync(WorldConfig.getSpawn())
}.register("spawn")

Commands.create().assertPlayer().assertOp().assertUsage("<angle>").handler({ c ->
    Player player = c.sender()

    ItemStack item = FastItemUtils.createItem(Material.STONE_BUTTON, "", [])
    FastItemUtils.setCustomModelData(item, c.arg(1).parseOrFail(Integer.class))

    Double angle = c.arg(0).parseOrFail(Double.class)
    Location loc = player.getLocation()
    Double angleOffset = 30
    angle += angleOffset
    double x = Math.cos(Math.toRadians(angle))
    double z = Math.sin(Math.toRadians(angle))

    z *= 0.72
    x *= 0.72

    double offsetX = 0
    double offsetZ = 0

    Players.msg(player, "\n \n")
    loc.setX(loc.getX() - x + offsetX)
    loc.setZ(loc.getZ() - z + offsetZ)
    loc.setY(loc.getY() - 2)

    Players.msg(player, "§2§lAngle: ${angle}")
    Players.msg(player, "§a ▎ §fSin X: §7${x}")
    Players.msg(player, "§a ▎ §fCos Z: §7${z}")
    Players.msg(player, "§a ▎ §fX: §7${x} §o(${offsetX})")
    Players.msg(player, "§a ▎ §fZ: §7${z} §o(${offsetZ})")
    ArmorStand eas = loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand
    eas.setGravity(false)
    eas.setMarker(true)
    eas.setArms(true)
    eas.setCustomNameVisible(true)
    eas.setRightArmPose(new EulerAngle(Math.toRadians(270), Math.toRadians(0), Math.toRadians(0)))
    eas.setRotation((angle - angleOffset - 90).toFloat(), 0)
    eas.getEquipment().setItem(EquipmentSlot.HAND, item)
    eas.setCustomName("crate_model_${(angle - angleOffset - 90)}_${loc.getWorld().getName()}")

}).register("dev/summoncrate")

Commands.create().assertPlayer().assertOp().handler({ c ->
    Schedulers.sync().run {
        c.sender().getNearbyEntities(3, 3, 3).findAll{ it instanceof ArmorStand }.each {
            it.remove()
        }
    }
}).register("dev/cleararmourstandspls")

//Commands.create().assertOp().handler {cmd ->
//    def table = new LootTable("test", [
//            new Reward("command:give {player} minecraft:diamond 1", 2.5D),
//            new Reward("command:give {player} minecraft:diamond 1", 2.5D),
//            new Reward("item:${FastItemUtils.convertItemStackToString(FastItemUtils.createItem(Material.DIAMOND, "hi", []))}", 2.5D)
//    ])
//    def json = Gson.gson.toJson(table)
//    println(json)
//}.register("dev/gsontojson")
//
//Commands.create().assertOp().handler {cmd ->
//    def fromJson = Gson.gson.fromJson("{\"name\":\"test\",\"rewards\":[{\"reward\":\"command:give {player} minecraft:diamond 1\",\"weight\":2.5,\"enabled\":true,\"maxPulls\":0,\"timesPulled\":0},{\"reward\":\"command:give {player} minecraft:diamond 1\",\"weight\":2.5,\"enabled\":true,\"maxPulls\":0,\"timesPulled\":0},{\"reward\":\"item:rO0ABXNyABpvcmcuYnVra2l0LnV0aWwuaW8uV3JhcHBlcvJQR+zxEm8FAgABTAADbWFwdAAPTGphdmEvdXRpbC9NYXA7eHBzcgA1Y29tLmdvb2dsZS5jb21tb24uY29sbGVjdC5JbW11dGFibGVNYXAkU2VyaWFsaXplZEZvcm0AAAAAAAAAAAIAAkwABGtleXN0ABJMamF2YS9sYW5nL09iamVjdDtMAAZ2YWx1ZXNxAH4ABHhwdXIAE1tMamF2YS5sYW5nLk9iamVjdDuQzlifEHMpbAIAAHhwAAAABHQAAj09dAABdnQABHR5cGV0AARtZXRhdXEAfgAGAAAABHQAHm9yZy5idWtraXQuaW52ZW50b3J5Lkl0ZW1TdGFja3NyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHAAAA2JdAAHRElBTU9ORHNxAH4AAHNxAH4AA3VxAH4ABgAAAANxAH4ACHQACW1ldGEtdHlwZXQADGRpc3BsYXktbmFtZXVxAH4ABgAAAAN0AAhJdGVtTWV0YXQAClVOU1BFQ0lGSUN0ACN7ImV4dHJhIjpbeyJ0ZXh0IjoiaGkifV0sInRleHQiOiIifQ\\u003d\\u003d\",\"weight\":2.5,\"enabled\":true,\"maxPulls\":0,\"timesPulled\":0}]}", LootTable.class)
//    fromJson.rewards.each {
//        if (it.reward.startsWith("item:")) {
//            println(FastItemUtils.convertStringToItemStack(it.reward.split("item:")[1]))
//            println(FastItemUtils.convertStringToItemStack(it.reward.split("item:")[1]).class.toString())
//        }
//        println(it.reward)
//    }
//    println(fromJson.class.toString())
//    println(fromJson.toString())
//}.register("dev/gsonfromjson")
//
//@Field FloatingEntity floatingEntity
//
//Commands.create().assertOp().assertPlayer().handler {cmd ->
//    if (!floatingEntity) {
//        floatingEntity = new FloatingEntity(cmd.sender().world, cmd.sender().location, {
//            Dolphin dolphin = new Dolphin(net.minecraft.world.entity.EntityType.DOLPHIN, (cmd.sender().world as CraftWorld).handle)
//            return dolphin
//        })
//
//        floatingEntity.setGlow(ChatColor.WHITE)
//        floatingEntity.track()
//    } else {
//        floatingEntity.untrack()
//        floatingEntity = null
//    }
//}.register("dev/entitytest")

