package scripts.factions.features.pack

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.inventory.ItemStack
import scripts.exec.Globals
import scripts.shared.legacy.command.SubCommandBuilder
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.MenuBuilder
import us.myles.ViaVersion.api.Via

GroovyScript.addUnloadHook {
    Starlight.unload("~/itemskins/ItemSkins.groovy")
}

Starlight.watch("~/itemskins/ItemSkins.groovy")

Events.subscribe(PlayerJoinEvent.class, EventPriority.MONITOR).handler({ event ->
    if (!event.player.isOnline() || Via.API.getPlayerVersion(event.player) < 472) return // do nothing for players <1.14)

    event.player.setResourcePack(Globals.RESOURCE_PACK_URL, Globals.RESOURCE_PACK_HASH)
})

Events.subscribe(PlayerResourcePackStatusEvent.class).handler({
    if (it.getStatus() != PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) return

    it.player.updateInventory()
})

SubCommandBuilder builder = new SubCommandBuilder("dev/testpack")

builder.create("testmodel").requirePermission("*").register {command ->
    Player player = command.sender()

    int modelId = command.arg(0).parseOrFail(Integer.class)
    ItemStack stack = player.getInventory().getItemInMainHand()
    if (stack == null || stack.type == Material.AIR) {
        command.reply("Invalid item")
        return
    }

    FastItemUtils.setCustomModelData(stack, modelId)
    FastItemUtils.setDisplayName(stack, "&e$modelId")
    player.updateInventory()
    command.reply("Set model to " + modelId)
}.create("testgui").requirePermission("*").register {command ->
    Player player = command.sender()

    int inventorySize = command.arg(0).parseOrFail(Integer.class)
    String title = "美"

    new MenuBuilder(inventorySize, title).openSync(player)
}.create("update").requirePermission("*").register {command ->
    command.sender().updateInventory()
}.create("testarmor").requirePermission("*").register {command ->
    command.sender().getInventory().armorContents.each {
        if (it != null && it.hasItemMeta()) {
            command.sender().sendMessage(it.toString())
            command.sender().sendMessage(it.getItemMeta().toString())
        }
    }
}.build()

Commands.create().assertOp().assertPlayer().handler({ command ->
    Player player = command.sender()

    ItemStack fromHead = player.getInventory().getHelmet()
    ItemStack toHead = player.getInventory().getItemInMainHand()

    if (fromHead == null || fromHead.type == Material.AIR) {
        if (toHead == null || toHead.type == Material.AIR) {
            command.reply("You need to be holding an item to test")
            return
        }
        player.getInventory().setHelmet(toHead)
        player.getInventory().setItemInMainHand(null)
        player.updateInventory()
    } else {
        if (toHead == null || toHead.type == Material.AIR) {
            command.reply("You need to be holding an item to test")
            return
        }
        player.getInventory().setHelmet(toHead)
        player.getInventory().setItemInMainHand(fromHead)
        player.updateInventory()
    }
}).register("dev/testhat")

