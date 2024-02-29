package scripts.factions.features.customitem.clickenchant

import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players
import groovy.transform.Field
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.utils.RomanNumerals
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils

@Field static Map<String, ClickableEnchant> REGISTERED_ENCHANTMENTS = [
        "depthstrider"        : new ClickableEnchant("Depth Strider", "DEPTH_STRIDER"),
        "protection"          : new ClickableEnchant("Protection", "PROTECTION_ENVIRONMENTAL"),
        "projectileprotection": new ClickableEnchant("Projectile Protection", "PROTECTION_PROJECTILE"),
        "featherfalling"      : new ClickableEnchant("Feather Falling", "PROTECTION_FALL"),

        "sharpness"           : new ClickableEnchant("Sharpness", "DAMAGE_ALL"),
        "smite"               : new ClickableEnchant("Smite", "DAMAGE_UNDEAD"),
        "baneofarthropods"    : new ClickableEnchant("Bane of Arthropods", "DAMAGE_ARTHROPODS"),
        "knockback"           : new ClickableEnchant("Knockback", "KNOCKBACK"),
        "fireaspect"          : new ClickableEnchant("Fire Aspect", "FIRE_ASPECT"),
        "looting"             : new ClickableEnchant("Looting", "LOOT_BONUS_MOBS"),

        "power"               : new ClickableEnchant("Power", "ARROW_DAMAGE"),
        "punch"               : new ClickableEnchant("Punch", "ARROW_KNOCKBACK"),
        "flame"               : new ClickableEnchant("Flame", "ARROW_FIRE"),
        "infinity"            : new ClickableEnchant("Infinity", "ARROW_INFINITE"),

        "efficiency"          : new ClickableEnchant("Efficiency", "DIG_SPEED"),
        "silktouch"           : new ClickableEnchant("Silk Touch", "SILK_TOUCH"),
        "fortune"             : new ClickableEnchant("Fortune", "LOOT_BONUS_BLOCKS"),

        "unbreaking"          : new ClickableEnchant("Unbreaking", "DURABILITY"),
]

Commands.create().assertOp().assertUsage("<player> <enchantment> [level] [unsafe]").handler { cmd ->
    def playerName = cmd.arg(0).parseOrFail(String.class)
    if (Bukkit.getPlayer(playerName) == null) {
        cmd.reply("§! §> §cPlayer not found")
        return
    }

    def player = Bukkit.getPlayer(playerName)

    def enchantName = cmd.arg(1).parseOrFail(String.class)
    def enchant = REGISTERED_ENCHANTMENTS.get(enchantName.toLowerCase())
    if (enchant == null) {
        cmd.reply("§! §> §cEnchantment not found!")
        cmd.reply("§aAvailable Enchantments: ${REGISTERED_ENCHANTMENTS.keySet().join(", ")}")
        return
    }

    if (cmd.args().size() == 3) {
        def level = cmd.arg(2).parseOrFail(Integer.class)
        if (level < 1) {
            cmd.reply("§! §> §cLevel must be greater than 0")
            return
        }
        cmd.reply("§^ §> §aGiven ${player.getName()} ${enchant.displayName} ${RomanNumerals.numeralOf(level)} enchantment shard.")
        FastInventoryUtils.addOrBox(player.getUniqueId(), player, null, createItem(enchant, level), null)
    } else if (cmd.args().size() == 4) {
        def level = cmd.arg(2).parseOrFail(Integer.class)
        if (level < 1) {
            cmd.reply("§! §> §cLevel must be greater than 0")
            return
        }

        def unsafe = cmd.arg(3).parseOrFail(Boolean.class)
        if (!unsafe) {
            cmd.reply("§! §> §cUnsafe must be true or false")
            return
        }

        cmd.reply("§^ §> §aGiven ${player.getName()} ${enchant.displayName} ${RomanNumerals.numeralOf(level)} enchantment shard.")
        FastInventoryUtils.addOrBox(player.getUniqueId(), player, null, createItem(enchant, level, unsafe), null)
    } else {
        cmd.reply("§^ §> §aGiven ${player.getName()} ${enchant.displayName} ${RomanNumerals.numeralOf(1)} enchantment shard.")
        FastInventoryUtils.addOrBox(player.getUniqueId(), player, null, createItem(enchant), null)
    }

}.register("giveclickenchant")

Events.subscribe(InventoryClickEvent.class).handler { event ->
    if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != event.getWhoClicked()) return

    Player player = event.getWhoClicked() as Player
    ItemStack currentItem = event.getCurrentItem()
    ItemStack cursor = event.getCursor()

    if (currentItem == null || currentItem.type == Material.AIR || cursor == null || cursor.type == Material.AIR) return

    Inventory inventory = player.getOpenInventory().getTopInventory()
    if (inventory.getType() != InventoryType.CRAFTING) return

    ClickableEnchantData clickableEnchantData = ClickableEnchantData.read(cursor)

    if (clickableEnchantData == null) {
        return
    }

    event.setCancelled(true)

    Enchantment enchantment = getEnchantmentByName(clickableEnchantData.clickableEnchant.enchantName)

    if (enchantment == null) {
        player.sendMessage("Enchantment not found")
        return
    }

    if (currentItem.enchantments.keySet().contains(enchantment)) {
        int level = currentItem.enchantments.get(enchantment)

        if (level >= clickableEnchantData.level) {
            Players.msg(player, "§! §> §cYour item already has this enchantment at a higher level.")
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5F, 1F)
            return
        }
    }

    if (clickableEnchantData.unsafe)
    {
        currentItem.addUnsafeEnchantment(enchantment, clickableEnchantData.level)
    } else
    {
        if (clickableEnchantData.level > enchantment.getMaxLevel()) {
            Players.msg(player, "§! §> §cInvalid Enchant Shard.")
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5F, 1F)
            return
        }
        currentItem.addEnchantment(enchantment, clickableEnchantData.level)
    }


    if (cursor.getAmount() > 1)
        cursor.setAmount(cursor.getAmount() - 1)
    else
        player.setItemOnCursor(null)
}

static def createItem(ClickableEnchant clickableEnchant, int level = 1, boolean unsafe = false) {
    ItemStack item = FastItemUtils.createItem(Material.PAPER, "§b§lEnchantment Shard §7($clickableEnchant.displayName ${RomanNumerals.numeralOf(level)})", [
            "§7Click to enchant your item with",
            "§7the enchantment $clickableEnchant.displayName ${RomanNumerals.numeralOf(level)}.",
            "§7",
            "§7Drag n' Drop onto item to use."
    ])

    ClickableEnchantData data = new ClickableEnchantData(clickableEnchant, level, unsafe)
    data.write(item)

    return item
}

static Enchantment getEnchantmentByName(String name) {
    return Enchantment.getByName(name)
}