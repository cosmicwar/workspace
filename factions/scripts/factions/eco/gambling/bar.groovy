package scripts.factions.eco.gambling


import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.event.filter.EventFilters
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.tags.ItemTagType
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import scripts.shared.legacy.AntiDupeUtils
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.PlayerUtils
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.systems.MenuBuilder
import org.starcade.starlight.helper.utils.Players

BarUtils.init()

static void showBar(Player player, int page = 1) {
    MenuBuilder builder

    builder = MenuUtils.createPagedMenu("§8Bar", BarUtils.DRINKS, { Map<String, Object> drink, Integer i ->
        long price = drink.get("price") as long

        CurrencyStorage currency = (Exports.ptr("money") as CurrencyStorage)

        ItemStack item = FastItemUtils.createItem(Material.POTION, "${drink.get("name")}", [
                "",
                "§fPrice: §e${currency.map(price)}",
                "",
                "§7 * Click to purchase * "
        ])
        PotionType type = drink.get("type") as PotionType

        PotionMeta meta = item.getItemMeta()
        meta.setBasePotionData(new PotionData(type, false, false))
        meta.addItemFlags(ItemFlag.values())
        item.setItemMeta(meta)

        FastItemUtils.setCustomTag(item, BarUtils.DRINK_PRICE_KEY, ItemTagType.LONG, price)

        return item
    }, page, false, [
            { p, t, s ->
                ItemStack clicked = builder.get().getItem(s)
                long price = FastItemUtils.getCustomTag(clicked, BarUtils.DRINK_PRICE_KEY, ItemTagType.LONG)

                CurrencyStorage currency = (Exports.ptr("money") as CurrencyStorage)
                currency.take(player, price, {
                    PotionMeta metaClicked = clicked.getItemMeta()

                    ItemStack item = FastItemUtils.createItem(Material.POTION, metaClicked.getDisplayName(), [
                            "",
                            "§fReward: §e${currency.map(price * 2)}",
                            "",
                            "§7 * Drink to play * "
                    ])
                    PotionMeta meta = item.getItemMeta()
                    meta.setBasePotionData(metaClicked.getBasePotionData())
                    meta.addItemFlags(ItemFlag.values())
                    item.setItemMeta(meta)

                    FastItemUtils.setCustomTag(item, BarUtils.DRINK_PRICE_KEY, ItemTagType.LONG, price * 2)
                    FastItemUtils.ensureUnique(item)

                    FastInventoryUtils.addOrBox(player.getUniqueId(), player, Bukkit.getConsoleSender(), item, "§e§lBAR §> §fSuccessfully purchased a bottle of ${meta.getDisplayName()}§f!")
                })
            },
            { p, t, s -> showBar(player, page + 1) },
            { p, t, s -> showBar(player, page - 1) }
    ])
    builder.open(player)
}

Events.subscribe(PlayerItemConsumeEvent.class, EventPriority.MONITOR).filter(EventFilters.ignoreCancelled()).handler { event ->
    ItemStack item = event.getItem()

    if (item.getType() != Material.POTION) {
        return
    }
    Long reward = FastItemUtils.getCustomTag(item, BarUtils.DRINK_PRICE_KEY, ItemTagType.LONG)

    if (reward == null) {
        return
    }
    event.setItem(null)

    Player player = event.getPlayer()

    UUID unique = FastItemUtils.getId(item)
    if (unique != null) {
        if (AntiDupeUtils.isDuped(unique)) {
            player.getInventory().removeItem(item)
            Players.msg(player, "§e§lBAR §> §fYour drink had no effect because it was duped!")
            for (Player plr : Bukkit.getOnlinePlayers()) {
                if (plr.hasPermission("group.staff.helper")) {
                    plr.sendMessage("§] §> §c${player.name} has had duped bar drinks removed from their inventory!")
                }
            }
            return
        }
        AntiDupeUtils.useId(unique, {
            Schedulers.sync().execute {
                if (RandomUtils.RANDOM.nextDouble() <= 0.4D) {
                    CurrencyStorage currency = (Exports.ptr("money") as CurrencyStorage)
                    currency.add(player, reward)

                    Players.msg(player, "§e§lBAR §> §fYou won §e${currency.map(reward)}§f!")

                    player.sendTitle("§a§lYou won!", "§eThe reward has been added to your balance", 0, 5, 1)
                } else {
                    Players.msg(player, "§e§lBAR §> §fUnfortunately, you did §c§nnot§f win anything!")
                    player.sendTitle("§c§lYou lost!", "§eTry again next time!", 0, 5, 1)

                    PlayerUtils.giveEffect(player, new PotionEffect(PotionEffectType.CONFUSION, 160, 2))
                }
            }
        })
        return
    }


    if (RandomUtils.RANDOM.nextDouble() <= 0.45D) {
        CurrencyStorage currency = (Exports.ptr("money") as CurrencyStorage)
        currency.add(player, reward)

        Players.msg(player, "§e§lBAR §> §fYou won §e${currency.map(reward)}§f!")

        player.sendTitle("§a§lYou won!", "§eThe reward has been added to your balance", 0, 5, 1)
    } else {
        Players.msg(player, "§e§lBAR §> §fUnfortunately, you did §c§nnot§f win anything!")
        player.sendTitle("§c§lYou lost!", "§eTry again next time!", 0, 5, 1)

        PlayerUtils.giveEffect(player, new PotionEffect(PotionEffectType.CONFUSION, 160, 2))
    }
}

Commands.create().assertPlayer().handler { command ->
    showBar(command.sender())
}.register("bar", "drinks", "drink")

Commands.create().assertOp().assertUsage("<player> <drink> <amount>").handler({ command ->
    Player target = Bukkit.getPlayer(command.rawArg(0))
    if (target == null) {
        command.reply("§! §> §fThe player §e${command.rawArg(0)} §fwas not found!")
        return
    }

    String drinkName = command.rawArg(1).toLowerCase()
    Map<String, ? extends Object> drink = null
    for (Map<String, ? extends Object> drank : BarUtils.DRINKS) {
        if (ChatColor.stripColor(drank.get("name") as String).toLowerCase() == drinkName) {
            drink = drank
            break
        }
    }
    if (drink == null) {
        command.reply("§! §> §fDrink §e${drinkName} §fnot found!")
        return
    }

    int amount
    try {
        amount = Integer.parseInt(command.rawArg(2))
    } catch (NumberFormatException ignore) {
        command.reply("§! §> §fInvalid amount specified!")
        return
    }

    long price = drink.get("price") as long
    CurrencyStorage currency = (Exports.ptr("money") as CurrencyStorage)

    ItemStack item = FastItemUtils.createItem(Material.POTION, "${drink.get("name")}", [
            "",
            "§fPrice: §e${currency.map(price)}",
            "",
            "§7 * Click to purchase * "
    ])
    PotionType type = drink.get("type") as PotionType

    PotionMeta meta = item.getItemMeta() as PotionMeta
    meta.setBasePotionData(new PotionData(type, false, false))
    meta.addItemFlags(ItemFlag.values())
    item.setItemMeta(meta)

    FastItemUtils.setCustomTag(item, BarUtils.DRINK_PRICE_KEY, ItemTagType.LONG, price)

    for (int i = 0; i < amount; i++) {
        FastItemUtils.ensureUnique(item)
        FastInventoryUtils.addOrBox(target.getUniqueId(), target, Bukkit.getConsoleSender(), item, "§e§lBAR §> §fYou were given a bottle of ${meta.getDisplayName()}§f!")
    }
}).register("givebar", "bargive", "givedrink", "drinkgive")

@CompileStatic(TypeCheckingMode.SKIP)
class BarUtils {
    public static final NamespacedKey DRINK_PRICE_KEY = new NamespacedKey(Starlight.plugin, "drink_price")

    public static final List<Map<String, ? extends Object>> DRINKS = [
            [
                    name : "§c§lCola",
                    price: 1000000L,
                    type : PotionType.INSTANT_DAMAGE
            ] as Map<String, Object>,
            [
                    name : "§6§lBeer",
                    price: 2500000L,
                    type : PotionType.FIRE_RESISTANCE
            ],
            [
                    name : "§6§lWhiskey",
                    price: 5000000L,
                    type : PotionType.FIRE_RESISTANCE
            ],
            [
                    name : "§a§lTequila",
                    price: 7500000L,
                    type : PotionType.SPEED
            ],
            [
                    name : "§e§lAbsinthe",
                    price: 10000000L,
                    type : PotionType.NIGHT_VISION
            ],
            [
                    name : "§d§lBourbon",
                    price: 25000000L,
                    type : PotionType.FIRE_RESISTANCE
            ],
            [
                    name : "§a§lJägerbomb",
                    price: 50000000L,
                    type : PotionType.INSTANT_DAMAGE
            ],
            [
                    name : "§f§lVodka",
                    price: 75000000L,
                    type : PotionType.SPEED
            ],
            [
                    name : "§e§lChampagne",
                    price: 100000000L,
                    type : PotionType.REGEN
            ]
    ]

    public static final long MINIMUM_ANNOUNCE_VALUE = 50000000L

    static void init() {}
}
