package scripts

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.item.ItemStackBuilder
import org.starcade.starlight.helper.menu.Gui
import org.starcade.starlight.helper.menu.Item
import org.starcade.starlight.helper.metadata.Metadata
import org.starcade.starlight.helper.utils.Players
import org.starcade.starlight.helper.utils.SimpleItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.systems.*
import scripts.shared.utils.MenuDecorator
import scripts.shared.utils.ServerUtils
import scripts.shared.utils.Temple

import java.util.stream.Collectors

Integer selectorSlot = Exports.ptr("hub_selector_slot") as Integer
ItemStack selectorItem = Exports.ptr("hub_selector_item") as ItemStack

if (Temple.templebase == "hub") {
    Commands.create().assertPlayer().handler({
        openGUI(it.sender())
    }).register("serverselector")

    for (def p in Bukkit.getOnlinePlayers()) p.inventory.setItem(selectorSlot, selectorItem)

    Events.subscribe(PlayerJoinEvent.class, EventPriority.HIGH).handler { event ->
        Player player = event.getPlayer()

        Inventory inventory = player.getInventory()
        inventory.clear()

        String server = (Exports.ptr("redirects") as Map<UUID, String>)?.get(player.getUniqueId())

        if (server != null) {
            return
        }
        inventory.setHeldItemSlot(selectorSlot)
        inventory.setItem(selectorSlot, selectorItem)
    }

    Events.subscribe(PlayerInteractEvent.class, EventPriority.LOW).handler { e ->
        if (e.item && selectorItem.isSimilar(e.item)) {
            e.player.world.execute {
                openGUI(e.player)
            }
            return
        }
    }
}
Schedulers.sync().runRepeating({
    for (def p in Metadata.players().getAllWithKey(Gui.OPEN_GUI_KEY)) {
        p.value.redraw()
    }
}, 20, 20)

Exports.ptr("server_selector", { Player player -> new SelectorGUI(player).open() })

static void openGUI(Player player) {
    MenuBuilder builder = new MenuBuilder(SelectorGUI.selectorMaskDesign.size() * 9, SelectorGUI.selectorTitle)

    MenuDecorator.decorate(builder, SelectorGUI.selectorMaskDesign as List<String>)

    Bedrock.SimpleGui gui = new Bedrock.SimpleGui("Server Navigator", "Use the buttons below to navigate between our servers!")

    for (Map<String, Object> serverObj : SelectorGUI.servers.toList()) {
        boolean isNetwork = false
        String id = serverObj["id"] as String
        Integer slot = serverObj["slot"] as Integer

        if (id == null) {
            id = serverObj["network"] as String

            if (id != null) {
                isNetwork = true
            }
        }
        if (id == null) {
            return
        }
        CachedServer server = isNetwork ? null : ServerCache.servers.get(id) // ensure null if isNetwork
        def jsonLore = serverObj["lore"] as List<String>
        boolean enchanted = serverObj["enchanted"] as boolean

        Integer queuepos = isNetwork ? null : ServerQueue.getQueuePosition(player)
        Integer queuesize = isNetwork ? null : (server != null ? ServerQueue.getQueueSize(server.address) : 0)

        String players = isNetwork ? ServerCache.getNetworkPlayerCount(id).toString() : (server != null ? server.players.toString() : "#")
        String maxPlayers = isNetwork ? (ServerCache.getNetworkPlayerCount(id) + 1).toString() : (server != null ? server.maxPlayers.toString() : "###")

        List<String> lore = SelectorGUI.lorearray.toList().stream().map({ String lore ->
            return lore
                    .replace("%players%", players)
                    .replace("%maxplayers%", maxPlayers)
                    .replace("%currentqueue%", (queuepos != null ? queuepos : "#").toString())
                    .replace("%queue%", (queuesize != null ? queuesize : "0").toString())
        }).collect(Collectors.toList())

//        boolean hasCorrectVersion = !(!Bedrock.isBedrockPlayer(player) && Globals.MIN_PROTOCOL_VERSION > 0 && ViaVersionPlugin.instance.getApi().getPlayerVersion(player) < Globals.MIN_PROTOCOL_VERSION)
//        if (!hasCorrectVersion) {
//            lore.addAll([
//                    "",
//                    "§c§lYou must upgrade your client to join this realm!"
//            ])
//        }

        ItemStack item = FastItemUtils.createItem(serverObj["icon"] as Material, serverObj["name"] as String, jsonLore + lore, false)
        if (enchanted) {
            FastItemUtils.addGlow(item)
        }
        builder.set(slot, item, { p, t, s ->
            if (server != null) {
                ServerUtils.sendToServer(player, server.address)
            } else if (isNetwork) {
                List<CachedServer> hubs = ServerCache.servers.values().stream().filter { server1 -> server1.name.startsWith("${id}_hub") }.collect(Collectors.toList())

                if (!hubs.isEmpty()) {
                    player.performCommand("goto ${hubs.get(new Random().nextInt(hubs.size())).name}")
                }
            } else {
                Players.msg(player, "§cThat server is currently offline!")
            }
        })
        gui.button(serverObj["name"] as String + "\n" + "Players: ${players}", {
            if (server != null) {
                ServerUtils.sendToServer(player, server.address)
            } else if (isNetwork) {
                List<CachedServer> hubs = ServerCache.servers.values().stream().filter { server1 -> server1.name.startsWith("${id}_hub") }.collect(Collectors.toList())

                if (!hubs.isEmpty()) {
                    player.performCommand("goto ${hubs.get(new Random().nextInt(hubs.size())).name}")
                }
            } else {
                Players.msg(player, "§cThat server is currently offline!")
            }
        })
    }

    builder.addBedrockOverride(gui)
    builder.open(player)
}

class SelectorGUI extends Gui {
    static def selectorTitle = Exports.ptr("hub_selector_title") as String
    static def selectorMaskPositions = Exports.ptr("hub_selector_mask_positions") as List<String>
    static def selectorMaskDesign = Exports.ptr("hub_selector_design") as List<String>
    static def selectorMaskColors = Exports.ptr("hub_selector_mask_colors") as List<Integer>
    static def lorearray = Exports.ptr("hub_selector_suffix") as List<String>
    static def servers = Exports.ptr("hub_selector_server") as List<Map<String, Object>>
    static final def emptyArray = new ArrayList<String>()

    SelectorGUI(Player player) {
        super(player, selectorMaskPositions.size(), selectorTitle)
    }

    @Override
    void redraw() {

        def items = new ArrayList<Item>()
        servers.forEach({ serverobj ->
            boolean isNetwork = false
            String id = serverobj["id"] as String

            if (id == null) {
                id = serverobj["network"] as String

                if (id != null) {
                    isNetwork = true
                }
            }
            if (id == null) {
                return
            }
            def server = isNetwork ? null : ServerCache.servers.get(id) // ensure null if isNetwork
            def item = new SimpleItem(serverobj["icon"] as Material).setName(serverobj["name"] as String)
            def jsonLore = serverobj["lore"] as List<String>
            boolean enchanted = serverobj["enchanted"] as boolean

            def queuepos = isNetwork ? null : ServerQueue.getQueuePosition(player)
            def queuesize = isNetwork ? null : (server != null ? ServerQueue.getQueueSize(server.address) : 0)

            String players = isNetwork ? ServerCache.getNetworkPlayerCount(id).toString() : (server != null ? server.players.toString() : "#")
            String maxPlayers = isNetwork ? (ServerCache.getNetworkPlayerCount(id) + 1).toString() : (server != null ? server.maxPlayers.toString() : "###")

            def parsedLore = jsonLore != null ? jsonLore.stream().map({ lore ->
                return lore
                        .replace("%players%", players)
                        .replace("%maxplayers%", maxPlayers)
                        .replace("%currentqueue%", (queuepos != null ? queuepos : "#").toString())
                        .replace("%queue%", (queuesize != null ? queuesize : "0").toString())
            }).collect(Collectors.toList()) : emptyArray

            def lore = lorearray.toList().stream().map({ lore ->
                return lore
                        .replace("%players%", players)
                        .replace("%maxplayers%", maxPlayers)
                        .replace("%currentqueue%", (queuepos != null ? queuepos : "#").toString())
                        .replace("%queue%", (queuesize != null ? queuesize : "0").toString())
            }).collect(Collectors.toList())

            ItemStackBuilder builder = ItemStackBuilder.of(item).lore(parsedLore).lore(lore).hideAttributes()
            if (enchanted) {
                builder.enchant(Enchantment.DURABILITY)
            }
            items.add(builder.build({
                if (server != null) {
                    ServerUtils.sendToServer(player, server.address)
                } else if (isNetwork) {
                    List<CachedServer> hubs = ServerCache.servers.values().stream().filter { server1 -> server1.name.startsWith("${id}_hub") }.collect(Collectors.toList())

                    if (!hubs.isEmpty()) {
                        player.performCommand("goto ${hubs.get(new Random().nextInt(hubs.size())).name}")
                    }
                } else {
                    Players.msg(player, "§cThat server is currently offline!")
                }
            }))
        })
        drawFor(this, items)
    }

    static def drawFor(Gui gui, List<Item> items) {
        char[] mask = selectorMaskPositions.join("").toCharArray()
        int colorindex = 0
        int itemsindex = 0
        for (def i in 0..(mask.length - 1)) {
            if (mask[i] == '1' as char) {
                if (selectorMaskColors.size() <= colorindex) continue
                def color = selectorMaskColors.get(colorindex++)
                gui.setItem(i, ItemStackBuilder.of(Material.valueOf("LEGACY_STAINED_GLASS_PANE")).data(color).name("§0").build({

                }))
            } else {
                if (items.size() <= itemsindex) continue
                gui.setItem(i, items.get(itemsindex++))
            }
        }
    }
}