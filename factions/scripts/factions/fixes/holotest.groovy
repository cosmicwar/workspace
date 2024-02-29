package scripts.factions.fixes


import org.starcade.starlight.helper.Commands
import groovy.transform.Field
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import scripts.shared.features.holograms.HologramRegistry
import scripts.shared.legacy.utils.npc.NPCRegistry
import scripts.shared.legacy.utils.npc.NPCTracker

import java.util.function.Consumer

Commands.create().assertPlayer().assertOp().handler { command ->
    def holo = HologramRegistry.get().spawn("test", command.sender().location, ["test", "ITEM:DIAMOND_BLOCK"], true, null)

}.register("createtestholo")

Commands.create().assertPlayer().assertOp().handler { command ->
    def holo = HologramRegistry.get().spawn("teststring", command.sender().location, ["§c§lDIVINE IMMOLATION", "§3aqua?", "§7lol"], true, null)

}.register("createtestholostringsonly")

//    static void addHologram(City city, String id, Location location, List<String> lines, boolean isDynamic) {
//        city.holograms.add(HologramRegistry.get().spawn(id, location, lines, isDynamic, PLACEHOLDERS))
//    }

//new CityHologramData(
//                                        id: "grinding1",
//                                        offset: new BlockPos(45, 124, 22),
//                                        lines: [
//                                                "§c§lGrinding Sector",
//                                                "§cTier: §f#grinding_tier",
//                                                "§cLevel: §f#grinding_level",
//                                                "§cXP: §f#grinding_xp_current §c/ §f#grinding_xp_required §7(#grinding_xp_percent)",
//                                                "§cCurrently Harvesting:",
//                                                "§7Rotten Flesh"
//                                        ],
//                                        isDynamic: true
//                                )

Commands.create().assertPlayer().assertOp().handler { command ->
    if (HologramRegistry.get().registered.containsKey("test")) {
        HologramRegistry.get().unregister(HologramRegistry.get().registered.get("test"))
    }
}.register("removetestholo")

Commands.create().assertPlayer().assertOp().handler { command ->
    if (HologramRegistry.get().registered.containsKey("teststring")) {
        HologramRegistry.get().unregister(HologramRegistry.get().registered.get("teststring"))
    }
}.register("removetestholostringsonly")

@Field static NPCTracker tracker = null

Commands.create().assertPlayer().assertOp().handler { cmd ->
    tracker = spawnNpc("test", "HIII", cmd.sender().location, "ukaywifi", { Player player ->
        player.sendMessage("hi")
    })
}.register("createnpctest")

Commands.create().assertPlayer().assertOp().handler {cmd ->
    tracker?.chat(cmd.args(), 5 * 20L)
}.register("npctestchat")

Commands.create().assertPlayer().assertOp().handler { cmd ->
    if (tracker != null) NPCRegistry.get().unregister(tracker)
}.register("removenpctest")

Commands.create().assertPlayer().assertOp().handler { cmd ->
    for (def type in EntityType.values()) {
        println("\"${type}\", 1.0D,")
    }
}.register("entitytest")

static NPCTracker spawnNpc(String id, String name, Location location, Object skinHolder = null, Consumer<Player> onClick = null) {
    NPCTracker npcTracker = NPCRegistry.get().spawn(id, name, location, skinHolder, onClick)
    npcTracker.turnTowardPlayers = true
    return npcTracker
}

//@Field FloatingEntity floatingEntity = null
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