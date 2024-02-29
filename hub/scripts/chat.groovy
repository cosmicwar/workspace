package scripts

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.tags.ItemTagType
import scripts.shared.legacy.database.mysql.MySQL
import scripts.shared.legacy.objects.SplitMessage
import scripts.shared.legacy.utils.*
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.Persistent
import scripts.shared.utils.Temple
import scripts.shared3.ArkGroups

import java.util.function.Consumer
import java.util.logging.Logger

String rainbowColors = "4c6e2ab319d5"
String independenceColors = "4f1"

// Gets called before text component stuff
Events.subscribe(AsyncPlayerChatEvent.class, EventPriority.HIGHEST).filter(EventFilters.ignoreCancelled()).handler { event ->
    String format = event.getFormat()
    Player player = event.getPlayer()

    List<String> prefixes = new ArrayList<>()

    Closure<String> getTitle = Exports.ptr("getTitle") as Closure<String>

    if (getTitle != null) {
        String title = getTitle.call(player)

        if (title != null) {
            prefixes.add("§8[${title}§8]")
        }
    }

    prefixes.add(ArkGroups.getLongTag(player))

    if (!prefixes.isEmpty()) {
        format = StringUtils.asString(prefixes) + " " + format
    }
    List<String> suffixes = new ArrayList<>()

    if (!suffixes.isEmpty()) {
        format = format.replace(": §f%2\$s", " ${StringUtils.asString(suffixes)}§f: %2\$s")
    }

    format = format.replace("<%1\$s>", "%1\$s:")
    format = format.replace(player.getName(), "%1\$s")
    event.setMessage(ColorUtils.getColor(player) + event.getMessage())
    event.setFormat(format)

    println event.getFormat()
}

// All textcomponent stuff should be done here.
Events.subscribe(AsyncPlayerChatEvent.class, EventPriority.MONITOR).filter(EventFilters.ignoreCancelled()).handler { event ->
    String message = event.getMessage()

    Player player = event.getPlayer()

    event.setCancelled(true)

    ItemStack item = player.getItemInHand()

    if (message.contains("[item]") && (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName())) {
        Players.msg(player, "§c§lERROR §8» §fYou are not holding any item that can be shown!")
        return
    }

    message = message.replaceFirst("\\[item]", "{item}").replace("[item]", "")

    BaseComponent[] itemComponents = new BaseComponent[1]
    itemComponents[0] = new TextComponent(itemToString(item))
    String itemString = item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName() ? ""
            : "§ex${item.getAmount()} ${item.getItemMeta().getDisplayName()}"

    String format = String.format(event.getFormat(), "{name}", "")

    SplitMessage splitPrefix = SplitMessage.fromString(format, [
            name: player.getDisplayName()
    ])
    TextComponent nameComponent = new TextComponent(player.getDisplayName())

    playerToComponents(player, { components ->
        nameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, components))
        Collection<org.bukkit.ChatColor> nameColors = (Exports.ptr("getNicknameColors") as Closure<Collection<org.bukkit.ChatColor>>).call(player)
        for (org.bukkit.ChatColor color : nameColors) {
            if (color == org.bukkit.ChatColor.RESET) {
                String name = ""
                for (int i = 0; i < nameComponent.text.length(); i++) {
                    name += "§" + rainbowColors.charAt(i % rainbowColors.length()) + nameComponent.text.charAt(i)
                }
                nameComponent.setText("")
                nameComponent.setExtra(Arrays.asList(TextComponent.fromLegacyText(name)))
            } else if (color == org.bukkit.ChatColor.MAGIC) {
                String name = ""
                for (int i = 0; i < nameComponent.text.length(); i++) {
                    name += "§" + independenceColors.charAt(i % independenceColors.length()) + nameComponent.text.charAt(i)
                }
                nameComponent.setText("")
                nameComponent.setExtra(Arrays.asList(TextComponent.fromLegacyText(name)))
            } else if (color.isColor()) {
                nameComponent.setColor(color.asBungee())
            } else if (color.isFormat()) {
                nameComponent.setBold(color.asBungee() == ChatColor.BOLD)
                nameComponent.setUnderlined(color.asBungee() == ChatColor.UNDERLINE)
                nameComponent.setItalic(color.asBungee() == ChatColor.ITALIC)
            }
        }
        SplitMessage splitMessage = SplitMessage.fromString(message, [
                item: "${itemString}"
        ])
        if (splitMessage.componentMap.containsKey("item"))
            splitMessage.componentMap.get("item").setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itemComponents))
        for (TextComponent component : splitMessage.messageComponents) {
            component.setColor(ChatColor.getByChar(ColorUtils.getColor(player).charAt(1)))
        }
        BaseComponent[] mergedArray = new BaseComponent[splitPrefix.messageComponents.length + splitMessage.messageComponents.length]
        System.arraycopy(splitPrefix.messageComponents, 0, mergedArray, 0, splitPrefix.messageComponents.length)
        System.arraycopy(splitMessage.messageComponents, 0, mergedArray, splitPrefix.messageComponents.length, splitMessage.messageComponents.length)
        for (Player recipient : event.getRecipients()) {
            recipient.spigot().sendMessage(mergedArray)
        }
    })
}

static String itemToString(ItemStack item) {
    if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return ""
    ItemMeta meta = item.getItemMeta()
    StringBuilder itemBuilder = new StringBuilder(meta.getDisplayName())
    if (meta.hasLore()) {
        for (String lore : meta.getLore()) {
            itemBuilder.append("\n").append(lore)
        }
    }
    if (!meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS) && meta.hasEnchants()) {
        for (Map.Entry<Enchantment, Integer> enchantmentEntry : item.getEnchantments().entrySet()) {
            itemBuilder.append("\n").append(ChatColor.GRAY.toString())
                    .append(FastItemUtils.getEnchantmentName(enchantmentEntry.getKey()))
                    .append(" ").append(NumberUtils.roman(enchantmentEntry.getValue()))
        }
    }
    return itemBuilder.toString()
}

static void playerToComponents(Player player, Consumer<BaseComponent[]> consumer) {
    List<BaseComponent> components = new ArrayList<>()
    components.add(new TextComponent("§dName §8»§c ${player.getName()}\n"))

    consumer.accept(components.toArray() as BaseComponent[])
}

static void showColorsMenu(Player player, int page = 1) {
    List<String> colors = ColorUtils.getAvailableColors(player)
    MenuBuilder builder

    builder = MenuUtils.createPagedMenu("§8Chat Colors", colors, { String color, Integer i ->
        ItemStack item

        switch (color) {
            case "§a":
                item = FastItemUtils.createItem(Material.LIME_WOOL, "§aGreen", [])
                break
            case "§b":
                item = FastItemUtils.createItem(Material.LIGHT_BLUE_WOOL, "§bAqua", [])
                break
            case "§c":
                item = FastItemUtils.createItem(Material.PINK_WOOL, "§cRed", [])
                break
            case "§d":
                item = FastItemUtils.createItem(Material.MAGENTA_WOOL, "§dMagenta", [])
                break
            case "§e":
                item = FastItemUtils.createItem(Material.YELLOW_WOOL, "§eYellow", [])
                break
            case "§f":
                item = FastItemUtils.createItem(Material.WHITE_WOOL, "§fWhite", [])
                break
            case "§0":
                item = FastItemUtils.createItem(Material.BLACK_WOOL, "§8Black", [])
                break
            case "§1":
                item = FastItemUtils.createItem(Material.BLUE_WOOL, "§1Dark Blue", [])
                break
            case "§2":
                item = FastItemUtils.createItem(Material.GREEN_WOOL, "§2Dark Green", [])
                break
            case "§3":
                item = FastItemUtils.createItem(Material.CYAN_WOOL, "§3Dark Aqua", [])
                break
            case "§4":
                item = FastItemUtils.createItem(Material.RED_WOOL, "§4Dark Red", [])
                break
            case "§5":
                item = FastItemUtils.createItem(Material.PURPLE_WOOL, "§5Purple", [])
                break
            case "§6":
                item = FastItemUtils.createItem(Material.ORANGE_WOOL, "§6Gold", [])
                break
            case "§7":
                item = FastItemUtils.createItem(Material.LIGHT_GRAY_WOOL, "§7Light Gray", [])
                break
            case "§8":
                item = FastItemUtils.createItem(Material.GRAY_WOOL, "§8GRAY", [])
                break
            case "§9":
                item = FastItemUtils.createItem(Material.BLUE_WOOL, "§9Blue", [])
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
    builder.open(player)
}

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

        Exports.ptr("chat/color/get", { Player player -> return getColor(player)})

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
