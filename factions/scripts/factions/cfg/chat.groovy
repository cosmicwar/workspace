package scripts.factions.cfg

import net.jodah.expiringmap.ExpiringMap
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.tags.ItemTagType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import scripts.factions.core.faction.Factions
import scripts.factions.util.Clickable
import scripts.shared.legacy.CooldownUtils
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.utils.*
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.ColorUtil
import scripts.shared.utils.Formats
import scripts.shared.utils.Persistent
import scripts.shared.utils.Temple
import scripts.shared3.ArkGroups

import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import java.util.regex.Pattern

Map<String, MenuBuilder> bragInventoryCache = ExpiringMap.builder().expiration(1, TimeUnit.MINUTES).build()
CurrencyStorage money = Exports.ptr("money") as CurrencyStorage

Pattern itemBragPattern = Pattern.compile("[A-Fa-f]{4}")

// All textcomponent stuff should be done here.
Events.subscribe(AsyncPlayerChatEvent.class, EventPriority.HIGHEST).filter(EventFilters.ignoreCancelled()).handler { event ->
    if (event.isCancelled()) return

    String message = event.getMessage()
    Player player = event.getPlayer()

    def member = Factions.getMember(player.getUniqueId())
    if (member == null) {
        return
    }

    event.setCancelled(true)

    if (message.contains("[item]") && message.contains("[brag]")) {
        Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
        Players.msg(player, "§c§lERROR §8» §fYou cannot use both [item] and [brag] in the same message!")
        return
    }

    // name things
    Clickable nameClickable = new Clickable("")

    // group prefix
    def tag = ArkGroups.getLongTag(player)
    if (tag != null) {
        nameClickable.addText("${tag} ")
    }

    // player name
//    if (player.isOp()) {
//        nameClickable.addText(player.getDisplayName())
//    } else {

    nameClickable.addHoverEvent("${player.getDisplayName()}", HoverEvent.Action.SHOW_TEXT, [
            "${player.getDisplayName()}",
            "§7Rank §8» ${ColorUtil.color("§<${ArkGroups.getGroup(player).color}>")}${ArkGroups.getGroup(player).getGroupName()}",
            "§7Balance §8» §a\$${Formats.formatMoneyShort(money.get(player.getUniqueId()))}",
    ])
//    }

    // message things
    Clickable messageClickable = new Clickable("§8» " + ColorUtils.getColor(player))

    if (message.contains("[item]")) {
        def item = player.getInventory().getItemInMainHand()
        if (item == null || item.type == Material.AIR) {
            Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
            Players.msg(player, "§c§lERROR §8» §fYou are not holding any item that can be shown!")
            return
        }

        if (CooldownUtils.get(player, "item", 15 * 1000) == 0) {
            CooldownUtils.set(player, "item")

            message = message.replace("[item]", "✫")
            def split = message.split("✫")

            if (split.size() >= 1) messageClickable.addText(ColorUtils.getColor(player) + split[0])

            def name = item.getItemMeta().hasDisplayName() ? "${FastItemUtils.getDisplayName(item)}" : "${item.getI18NDisplayName()}"
            messageClickable.addHoverEvent(ColorUtil.color("§f » $name§f « §r"), HoverEvent.Action.SHOW_TEXT, itemToComponents(item))

            if (split.size() >= 2) messageClickable.addText(ColorUtils.getColor(player) + split[1])
        } else {
            Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
            Players.msg(player, "§c§lERROR §8» §fYou cannot use [item] for another §e${Formats.formatTimeMillis((15 * 1000) - CooldownUtils.getLast(player.getUniqueId(), "item"))} §fseconds!")
            return
        }
    } else if (message.contains("[brag]")) {
        if (CooldownUtils.get(player, "brag", 15 * 1000) == 0) {
            CooldownUtils.set(player, "brag")

            message = message.replace("[brag]", "✫")
            def split = message.split("✫")

            if (split.size() >= 1) messageClickable.addText(ColorUtils.getColor(player) + split[0])

            String inventoryId = UUID.randomUUID().toString()
            bragInventoryCache.put(inventoryId, createBragInventory(player))

            messageClickable.addClickHoverEvent("§f » §e§l${player.getName()}'s Inventory§f « ", ClickEvent.Action.RUN_COMMAND, "/viewbragdata ${inventoryId}", HoverEvent.Action.SHOW_TEXT, ["§e§l${player.getName()}'s Inventory"])

            if (split.size() >= 2) messageClickable.addText(ColorUtils.getColor(player) + split[1])
        } else {
            Players.playSound(player, Sound.ENTITY_VILLAGER_NO)
            Players.msg(player, "§c§lERROR §8» §fYou cannot use [brag] for another §e${Formats.formatTimeMillis((15 * 1000) - CooldownUtils.getLast(player.getUniqueId(), "brag"))} §fseconds!")
            return
        }
    } else {
        messageClickable.addText(ColorUtils.getColor(player) + message)
    }

    event.recipients.each { target ->
        Clickable factionRelation = new Clickable("")

//        if (!player.isOp()) {
        def targetMember = Factions.getMember(target.getUniqueId())
        if (member.getFactionId() != null || member.getFactionId() != Factions.wildernessId) {
            def faction = Factions.getFaction(member.getFactionId(), false)
            if (faction.systemFactionData == null) {
                def relation = Factions.getRelationType(member.getFactionId(), targetMember.getFactionId())

                factionRelation.addText("§8(${relation.getColor()}${faction.getName()}§8) ")
            }
        }
//        }

        target.spigot().sendMessage(factionRelation.join(nameClickable).join(messageClickable).build())
    }

}

static List<String> itemToComponents(ItemStack item) {
    List<String> lore = new ArrayList<>()

    if (item == null || item.getType() == Material.AIR) return lore

    ItemMeta meta = item.getItemMeta()

    lore.add("§f${meta.hasDisplayName() ? "${FastItemUtils.getDisplayName(item)}" : "${item.getI18NDisplayName()}"}§r")

    if (!meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS) && meta.hasEnchants()) {
        item.getEnchantments().entrySet().each { entry ->
            lore.add("§3${FastItemUtils.getEnchantmentName(entry.getKey())} ${NumberUtils.roman(entry.getValue())}§r")
        }
    }

    if (FastItemUtils.getLore(item) != null)
        FastItemUtils.getLore(item).each { line -> lore.add("§f${line}§r") }

    return lore
}

static MenuBuilder createBragInventory(Player player) {
    MenuBuilder builder

    builder = new MenuBuilder(45, "§e§l${player.getName()}'s Inventory")

    PlayerInventory playerInventory = player.getInventory()
    for (int i = 0; i < playerInventory.getSize(); i++) {
        ItemStack item = playerInventory.getItem(i)
        if (item == null || item.getType() == Material.AIR) continue

        ItemStack itemClone = item.clone()
        ItemMeta itemMeta = itemClone.getItemMeta()

        itemClone.setItemMeta(itemMeta)
        if (i >= 9 && i <= 17) {
            builder.get().setItem(i + 18, itemClone)
        } else if (i >= 27 && i <= 35) {
            builder.get().setItem(i - 18, itemClone)
        } else {
            builder.get().setItem(i, itemClone)
        }
    }

    builder.set(5, 3, player.getInventory().getHelmet() ?: new ItemStack(Material.AIR))
    builder.set(5, 4, player.getInventory().getChestplate() ?: new ItemStack(Material.AIR))
    builder.set(5, 6, player.getInventory().getLeggings() ?: new ItemStack(Material.AIR))
    builder.set(5, 7, player.getInventory().getBoots() ?: new ItemStack(Material.AIR))
    builder.set(5, 1, FastItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", []))
    builder.set(5, 2, FastItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", []))
    builder.set(5, 5, FastItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", []))
    builder.set(5, 8, FastItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", []))
    builder.set(5, 9, FastItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", []))

    return builder
}

static void showColorsMenu(Player player, int page = 1) {
    List<String> colors = ColorUtils.getAvailableColors(player)
    MenuBuilder builder

    builder = MenuUtils.createPagedMenu("§8Chat Colors", colors, { String color, Integer i ->
        ItemStack item

        switch (color) {
            case "§a":
                item = FastItemUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "§aGreen", [])
                break
            case "§b":
                item = FastItemUtils.createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "§bAqua", [])
                break
            case "§c":
                item = FastItemUtils.createItem(Material.PINK_STAINED_GLASS_PANE, "§cRed", [])
                break
            case "§d":
                item = FastItemUtils.createItem(Material.MAGENTA_STAINED_GLASS_PANE, "§dMagenta", [])
                break
            case "§e":
                item = FastItemUtils.createItem(Material.YELLOW_STAINED_GLASS_PANE, "§eYellow", [])
                break
            case "§f":
                item = FastItemUtils.createItem(Material.WHITE_STAINED_GLASS_PANE, "§fWhite", [])
                break
            case "§0":
                item = FastItemUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, "§8Black", [])
                break
            case "§1":
                item = FastItemUtils.createItem(Material.BLUE_STAINED_GLASS_PANE, "§1Dark Blue", [])
                break
            case "§2":
                item = FastItemUtils.createItem(Material.GREEN_STAINED_GLASS_PANE, "§2Dark Green", [])
                break
            case "§3":
                item = FastItemUtils.createItem(Material.CYAN_STAINED_GLASS_PANE, "§3Dark Aqua", [])
                break
            case "§4":
                item = FastItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "§4Dark Red", [])
                break
            case "§5":
                item = FastItemUtils.createItem(Material.PURPLE_STAINED_GLASS_PANE, "§5Purple", [])
                break
            case "§6":
                item = FastItemUtils.createItem(Material.ORANGE_STAINED_GLASS_PANE, "§6Gold", [])
                break
            case "§7":
                item = FastItemUtils.createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§7Light Gray", [])
                break
            case "§8":
                item = FastItemUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, "§8GRAY", [])
                break
            case "§9":
                item = FastItemUtils.createItem(Material.BLUE_STAINED_GLASS_PANE, "§9Blue", [])
                break
            default:
                return null
        }
        FastItemUtils.setCustomTag(item, new NamespacedKey(Starlight.plugin, "color"), ItemTagType.STRING, color)

        return item
    }, page, false, [
            { p, t, s ->
                ItemStack item = builder.get().getItem(s)

                String color = FastItemUtils.getCustomTag(item, new NamespacedKey(Starlight.plugin, "color"), ItemTagType.STRING)

                if (color != ColorUtils.getColor(player)) {
                    UUID uuid = player.getUniqueId()
                    ColorUtils.COLORS.put(uuid, color)

                    MySQL.getAsyncDatabase().execute("INSERT INTO chat_colors (uuid_least, uuid_most, color, server_id) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE color = VALUES(color)", { statement ->
                        statement.setLong(1, uuid.getLeastSignificantBits())
                        statement.setLong(2, uuid.getMostSignificantBits())
                        statement.setString(3, color)
                        statement.setString(4, Temple.templeId)
                    })
                }
                Players.msg(player, "§] §8» §fSuccessfully updated your chat color!")
                player.closeInventory()
            },
            { p, t, s -> showColorsMenu(player, page + 1) },
            { p, t, s -> showColorsMenu(player, page - 1) }
    ])
    player.openInventory(builder.get())
}

Commands.create().assertPlayer().handler { cmd ->
    if (cmd.args().isEmpty()) {
        return
    }

    if (cmd.args().size() == 1) {
        def inventoryId = cmd.arg(0).parseOrFail(String.class)
        def menu = bragInventoryCache.get(inventoryId)

        if (menu == null) {
            cmd.reply("§] §8» §e${inventoryId} §fdoes not exist!")
            return
        }

        menu.openSync(cmd.sender())
    }
}.register("viewbragdata")

Commands.create().assertPlayer().assertPermission("chat.color").handler { command ->
    showColorsMenu(command.sender())
}.register("chatcolor", "ccolor", "color", "chatcolour", "ccolour", "colour")

Commands.create().assertPermission("commands.resetchatcolor").assertUsage("<player>").handler { command ->
    DatabaseUtils.getId(command.rawArg(0), { uuid, username, player ->
        if (uuid == null) {
            command.reply("§] §8» §e${username} §fhas never joined the server before!")
            return
        }
        ColorUtils.COLORS.remove(uuid)
        MySQL.getAsyncDatabase().execute("DELETE FROM chat_colors WHERE ${DatabaseUtils.getServerUserExpression(uuid)}")

        command.reply("§] §8» §fSuccessfully reset §e${username}§f's chat color!")
    })
}.register("resetchatcolor")

ColorUtils.init()

class ColorUtils {
    static boolean LOADED
    static Map<UUID, String> COLORS

    static void init() {
        LOADED = Persistent.persistentMap.containsKey("chat_colors")
        COLORS = Persistent.of("chat_colors", new HashMap<UUID, String>()).get()

        MySQL.getAsyncDatabase().execute("CREATE TABLE IF NOT EXISTS chat_colors (uuid_least BIGINT NOT NULL, uuid_most BIGINT NOT NULL, color VARCHAR(4) NOT NULL, server_id VARCHAR(16) NOT NULL, PRIMARY KEY(uuid_least, uuid_most, server_id))")

        if (LOADED) {
            return
        }
        ThreadUtils.runAsync {
            Logger logger = Starlight.plugin.getLogger()
            logger.info("Loading colors...")

            MySQL.getSyncDatabase().executeQuery("SELECT * FROM chat_colors WHERE ${DatabaseUtils.getServerIdExpression()}", { statement -> }, { result ->
                while (result.next()) {
                    UUID uuid = new UUID(result.getLong(2), result.getLong(1))
                    String color = result.getString(3)
                    COLORS.put(uuid, color)
                }
            })
            logger.info("Loaded colors!")
        }
    }

    static String getColor(Player player) {
        String color = COLORS.get(player.getUniqueId())

        if (color == null) {
            color = "§f"
        }
        return color
    }

    static List<String> getAvailableColors(Player player) {
        List<String> colors = [
                "§b",
                "§d",
                "§f",
                "§e",
                "§a",
                "§5",
                "§3",
                "§6",
                "§9",
                "§2"
        ]
        if (player.hasPermission("group.staff.helper")) {
            colors.addAll([
                    "§c",
                    "§4",
                    "§0"
            ])
        }
        return colors
    }
}
