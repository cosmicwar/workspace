package scripts.factions.fixes

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.google.common.collect.Sets
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.util.Vector
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.protocol.Protocol
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scripts.shared.legacy.utils.FastItemUtils

import java.util.concurrent.ThreadLocalRandom

Commands.create().assertPlayer().assertOp().handler { command ->
    Player player = command.sender()
    ItemStack stack = player.getInventory().getItemInMainHand()
    if (stack == null || stack.type == Material.AIR) return
    if (!stack.hasItemMeta()) return

    Players.msg(player, "§7§m----------------------------------------")
    Players.msg(player, "§7§lItemStack: §f${stack.type}")
    Players.msg(player, "§7§lMeta Data: §f${stack.itemMeta.toString()}")
    stack.itemMeta.getPersistentDataContainer().getKeys().each {
        Players.msg(player, "§7§lNBT Data: §f${it.toString()}")
    }
    Players.msg(player, "§7§m----------------------------------------")

}.register("dev/nbtdebug")

Commands.create().assertOp().assertPlayer().handler {command ->
    Player player = command.sender()

    player.sendMessage("before")
    player.sendMessage("§7§m----------------------------------------")
    Schedulers.async().run {
        player.sendMessage("async")
        player.sendMessage("async1")
        player.sendMessage("async2")
        player.sendMessage("async3")
        player.sendMessage("async4")
    }
    player.sendMessage("next")
    Schedulers.sync().run {
        player.sendMessage("sync")
        player.sendMessage("sync1")
        player.sendMessage("sync2")
        player.sendMessage("sync3")
        player.sendMessage("sync4")
    }
    player.sendMessage("next")
    Schedulers.sync().run {
        player.sendMessage("sync")
        player.sendMessage("sync1")
        player.sendMessage("sync2")
        player.sendMessage("sync3")
        player.sendMessage("sync4")
    }
    player.sendMessage("next")
    Schedulers.async().run {
        player.sendMessage("async")
        player.sendMessage("async1")
        player.sendMessage("async2")
        player.sendMessage("async3")
        player.sendMessage("async4")
    }
    player.sendMessage("after")
    player.sendMessage("§7§m----------------------------------------")
    int[] test = [0,1]
    test.length
}.register("dev/asynctest")

Commands.create().assertPlayer().assertOp().handler {cmd ->
    def degrees = cmd.sender().getLocation().getYaw()

    degrees = (degrees - 157) % 360;
    if (degrees < 0) degrees += 360;

    cmd.reply("${degrees}")

    int ordinal = (int) Math.floor(degrees / 45)
    cmd.reply("${ordinal}")

}.register("dev/testyaw")

Commands.create().assertOp().assertPlayer().handler { c ->
    ItemStack item = c.sender().getItemInHand()

    if (item == null) {
        c.reply("§cYou are not holding an item in your hand.")
        return
    }

    UUID id = FastItemUtils.getId(item)

    if (id == null) {
        c.reply("§cThis item does not have an id.")
        return
    }

    c.reply("§aThe ID of this item is >> ${id.toString()}")
}.register("dev/getitemid")

def pingLogToggle = new WeakHashMap<Player, Boolean>()
def hasSent = new WeakHashMap<Player, Boolean>()
Set<Long> sentHash = Sets.newHashSet()

Protocol.subscribe(PacketType.Play.Client.TRANSACTION).handler { p ->
    def enabled = pingLogToggle.get(p.player)
    if (enabled) {
        def recv = p.getPacket().getLongs().read(0)
        hasSent.put(p.player, false)
        if (sentHash.remove(recv)) {
            def ping = (System.nanoTime() / 1000000L).toLong() - recv
            Schedulers.sync().run {
                Players.msg(p.player, "Network latency: ${ping}ms")
            }
            p.setCancelled(true)
        }
    }
}

Protocol.subscribe(PacketType.Play.Client.KEEP_ALIVE).handler { p ->
    def enabled = pingLogToggle.get(p.player)
    if (enabled) {
        def recv = p.getPacket().getLongs().read(0)
        hasSent.put(p.player, false)
        if (sentHash.remove(recv)) {
            def ping = (System.nanoTime() / 1000000L).toLong() - recv
            Schedulers.sync().run {
                Players.msg(p.player, "Network latency: ${ping}ms")
            }
            p.setCancelled(true)
        }
    }
}

Schedulers.sync().runRepeating({
    pingLogToggle.each { entry ->
        if (entry.value && !hasSent.getOrDefault(entry.key, false)) {
            def time = (System.nanoTime() / 1000000L).toLong()
            def packet = new PacketContainer(PacketType.Play.Server.KEEP_ALIVE)
            packet.getLongs().write(0, time)
            sentHash.add(time)
            hasSent.put(entry.key, true)
            Protocol.sendPacket(entry.key, packet)
        }
    }
}, 1, 1)

Commands.create().assertOp().assertPlayer().handler { c ->
    def toggle = !pingLogToggle.getOrDefault(c.sender(), false)
    pingLogToggle.put(c.sender(), toggle)

    c.reply("ping toggle: ${toggle}")
}.register("pinglog")

Commands.create().assertPlayer().handler {ctx ->
    ctx.reply("§aYour ping is §f${ctx.sender().ping}ms")
}.register("ping")

Commands.create().assertOp().assertPlayer().assertUsage("<player>").handler({ c ->
    Player target = Bukkit.getPlayer(c.rawArg(0))
    if (target == null) {
        c.sender().sendMessage("Player not found")
        return
    }

    target.spigot().respawn()
    c.sender().sendMessage("Forced " + target.getName() + " to respawn!")
}).register("dev/fixrespawn")

static void alias(String to, String... from) {
    Commands.create().assertPlayer().handler { command ->
        def cmd = command.args().join(" ")
        command.sender().chat("/$to $cmd")
    }.register(from)
}

alias("factions", "f", "gang")

Commands.create().assertOp().assertPlayer().handler { c ->
    def block = c.sender().location.add(1, 0, 0).block
    block.setType(Material.STONE)
    Schedulers.sync().runLater({
        c.sender().sendBlockChange(block.location, Material.AIR, 0 as byte)
    }, 20)
}.register("dev/makeghostblock")

Commands.create().assertOp().assertPlayer().handler { c ->
    disableTp = true
    c.sender().getWorld().players.each { it ->
        if (it.location.distance(c.sender().location) < 10) {
            it.setVelocity(new Vector(ThreadLocalRandom.current().nextDouble(0, 2) - 1, 0.5, ThreadLocalRandom.current().nextDouble(0, 2) - 1))
        }
    }
    c.reply("Exploding!")
    Schedulers.sync().runLater({
        disableTp = false
    }, 50)
}.register("explodenearme")

Commands.create().assertOp().assertPlayer().handler {ctx ->
    def reach = ctx.arg(0).parseOrFail(Double)

    def serverPlayer = (ctx.sender() as CraftPlayer).getHandle()
    serverPlayer.range = reach
}.register("dev/setreach")