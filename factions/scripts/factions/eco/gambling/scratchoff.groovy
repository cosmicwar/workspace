package scripts.factions.eco.gambling

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import scripts.shared.systems.Bedrock
import scripts.shared.utils.Persistent
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.objects.MutableBoolean
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.LongUtils
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.legacy.utils.SignUtils
import scripts.shared.systems.MenuBuilder

import java.text.NumberFormat
import java.util.concurrent.ConcurrentHashMap

ScratchOffUtils.init()

static ItemStack getRewardItem(Material reward) {
    Map<String, Object> data = ScratchOffUtils.REWARDS.get(reward)

    if (data == null) {
        return FastItemUtils.createItem(ScratchOffUtils.LOSING_MATERIAL, "§c§lYOU LOST", [])
    }
    ItemStack item = FastItemUtils.createItem(reward, data.get("name") as String, [
            "",
            "§7Reward: §ax${ScratchOffUtils.DOUBLE_FORMAT.format(data.get("multiplier") as double)}",
            "§7Chance: §e${data.get("chance")}%"
    ])
    return item
}

static void displayReward(MenuBuilder builder, List<Integer> slots, Material reward) {
    ItemStack item = getRewardItem(reward)

    for (int slot : slots) {
        builder.set(slot, item, { p, t, s -> })
    }
}

static void showScratchOff(Player player, Map<String, Object> playData = null) {
    MenuBuilder builder

    builder = new MenuBuilder(6 * 9, ScratchOffUtils.SCRATCH_OFF_INVENTORY_NAME)
    displayReward(builder, [ 0, 8 ], Material.EMERALD)
    displayReward(builder, [ 9, 17 ], Material.DIAMOND)
    displayReward(builder, [ 18, 26 ], Material.LAPIS_LAZULI)
    displayReward(builder, [ 27, 35 ], Material.GOLD_INGOT)
    displayReward(builder, [ 36, 44 ], Material.IRON_INGOT)
    displayReward(builder, [ 45, 53 ], Material.COAL)

    if (playData == null) {
        builder.set(4, FastItemUtils.createItem(Material.MAGMA_CREAM, "§8[§aYour Bet§8]", [
                "",
                "§e * Click to bet Credits * "
        ]), { p, t, s ->
            Closure acceptInput = { String input ->
                LongUtils.LongParseResult result = LongUtils.parseLong(input)
                if (!result.isPositive()) {
                    Players.msg(player, "§! §> §e${input} §fis not a valid amount!")
                    return
                }
                CurrencyStorage money = Exports.ptr("money") as CurrencyStorage
                long value = result.getValue()

                if (value < ScratchOffUtils.MINIMUM_BET) {
                    Players.msg(player, "§! §> §fThe minimum bet is §e${money.map(ScratchOffUtils.MINIMUM_BET)}§f!")
                    return
                }
                if (value > ScratchOffUtils.MAXIMUM_BET) {
                    Players.msg(player, "§! §> §fThe maximum bet is §e${money.map(ScratchOffUtils.MAXIMUM_BET)}§f!")
                    return
                }
                money.take(player, value, {
                    Map<Material, Integer> rewards = new HashMap<>()

                    for (Map.Entry<Material, Map<String, ? extends Object>> entry : ScratchOffUtils.REWARDS.entrySet()) {
                        rewards.put(entry.getKey(), entry.getValue().get("chance") as Integer)
                    }
                    rewards.put(ScratchOffUtils.LOSING_MATERIAL, 50)

                    Material reward = RandomUtils.get(rewards)

                    Map<String, Object> data = [
                            bet          : value,
                            reward       : reward,
                            shown_rewards: new HashMap<>(),
                            scratched    : 0
                    ]
                    showScratchOff(player, data)

                    ScratchOffUtils.ACTIVE_SCRATCH_OFFS.put(player.getUniqueId(), data)
                })
            }

            if (Bedrock.isBedrockPlayer(player)) {
                new Bedrock.CustomGui("Enter Bet", { Map<String, Object> responses ->
                    acceptInput.call(responses.get("bet") as String)
                })
                        .textInput("bet", "Enter Amount", "1")
                        .open(player)
                return
            }

            SignUtils.openSign(player, [ "", "^ ^ ^", "Enter Bet", "§dMoney" ], { lines , p1 ->
                acceptInput.call(lines[0])
            })
        })
    } else {
        builder.set(4, FastItemUtils.createItem(Material.MAGMA_CREAM, "§8[§aYour Bet§8]", [
                "",
                "${(Exports.ptr("money") as CurrencyStorage).map(playData.get("bet") as long)}"
        ]), { p, t, s -> })
    }
    ItemStack white = FastItemUtils.createItem(Material.WHITE_STAINED_GLASS_PANE, "§0", [])
    ItemStack magenta = FastItemUtils.createItem(Material.MAGENTA_STAINED_GLASS_PANE, "§0", [])
    ItemStack blue = FastItemUtils.createEnchantedItem(Material.BLUE_STAINED_GLASS_PANE, "§9§lSCRATCH", playData == null ? [
            "",
            "§fClick on the §6§nMagma Cream§f to place bet!"
    ] : [
            "",
            "§e * Click to scratch * "
    ])

    for (int i : [ 2, 6, 10, 12, 14, 16, 20, 24, 28, 34, 38, 42, 46, 48, 50, 52 ]) {
        builder.set(i, white, { p, t, s -> })
    }
    for (int i : [ 1, 3, 5, 7, 11, 13, 15, 19, 25, 29, 33, 37, 43, 47, 49, 51 ]) {
        builder.set(i, magenta, { p, t, s -> })
    }
    for (int i : [ 21, 22, 23, 30, 31, 32, 39, 40, 41 ]) {
        builder.set(i, blue, { p, t, s ->
            ItemStack item = builder.get().getItem(s)

            if (item.getType() != blue.getType() || playData == null) {
                return
            }
            Material reward = playData.get("reward") as Material
            Map<Material, Integer> shown = playData.get("shown_rewards") as Map<Material, Integer>

            int scratched = playData.get("scratched")

            if (reward == ScratchOffUtils.LOSING_MATERIAL) {
                modifyItemToRandomMaterial(item, shown, reward)
            } else {
                int placed = shown.getOrDefault(reward, 0)

                if (scratched < 6) {
                    if (placed != 3 && RandomUtils.RANDOM.nextDouble() < 1.0D / ScratchOffUtils.REWARDS.size()) {
                        modifyItemToReward(item, shown, reward, placed)
                    } else {
                        modifyItemToRandomMaterial(item, shown, reward)
                    }
                } else {
                    if (9 - scratched == 3 - placed) {
                        modifyItemToReward(item, shown, reward, placed)
                    } else {
                        modifyItemToRandomMaterial(item, shown, reward)
                    }
                }
            }
            if (++scratched < 9) {
                playData.put("scratched", scratched)
            } else {
                List<Material> scratches = new ArrayList<>()
                MutableBoolean showing = new MutableBoolean(true)

                for (int j = 0; j < 5; ++j) {
                    Schedulers.sync().runLater({
                        boolean isShowing = showing.booleanValue()

                        int iterated = 0

                        for (int index : [ 21, 22, 23, 30, 31, 32, 39, 40, 41 ]) {
                            ItemStack currentItem = builder.get().getItem(index)

                            if (isShowing) {
                                if (scratches.size() != 9) {
                                    scratches.add(currentItem.getType())
                                }
                                modifyItemToReward(currentItem, shown, reward, 0)
                            } else {
                                modifyItemToReward(currentItem, shown, scratches.get(iterated++), 0)
                            }
                        }
                        showing.setValue(!isShowing)
                    }, (j + 1) * 20L)
                }
                Schedulers.sync().runLater({
                    for (HumanEntity viewer : builder.get().getViewers()) {
                        Schedulers.sync().runLater({
                            viewer.closeInventory()
                        }, 1L)
                    }
                }, 140L)
            }
        })
    }
    builder.open(player)
}

static void modifyItemToReward(ItemStack item, Map<Material, Integer> shown, Material reward, int placed) {
    ItemMeta rewardItemMeta = getRewardItem(reward).getItemMeta()

    item.setType(reward)
    FastItemUtils.setDisplayName(item, "§f§l${rewardItemMeta.getDisplayName()}")
    FastItemUtils.setLore(item, rewardItemMeta.hasLore() ? rewardItemMeta.getLore() : [])

    shown.put(reward, placed + 1)
}

static void modifyItemToRandomMaterial(ItemStack item, Map<Material, Integer> shown, Material reward) {
    Material place = null
    int placed = 0

    while (true) {
        place = RandomUtils.getRandom(new ArrayList<>(ScratchOffUtils.REWARDS.keySet()))

        if (place == reward) {
            continue
        }
        placed = shown.getOrDefault(place, 0)

        if (placed < 2) {
            break
        }
    }
    modifyItemToReward(item, shown, place, placed)
}

Events.subscribe(InventoryCloseEvent.class).handler { event ->
    Player player = event.getPlayer()
    Map<String, Object> playData = ScratchOffUtils.ACTIVE_SCRATCH_OFFS.remove(player.getUniqueId())

    if (playData != null) {
        handleReward(player, playData)
    }
}

static void handleReward(Player player, Map<String, Object> playData) {
    Material reward = playData.get("reward") as Material

    if (reward == ScratchOffUtils.LOSING_MATERIAL) {
        Players.msg(player, "§b§lSCRATCH OFF §> §fUnfortunately, you did §c§nnot§f win anything!")
        return
    }
    long bet = playData.get("bet") as long

    Map<String, ? extends Object> rewardData = ScratchOffUtils.REWARDS.get(reward)

    CurrencyStorage money = Exports.ptr("money") as CurrencyStorage

    if (rewardData == null) {
        money.add(player, bet)
        Players.msg(player, "§b§lSCRATCH OFF §> §fAn unknown error has occurred, you were refunded your bet!")
        return
    }
    double multiplier = rewardData.get("multiplier") as double

    long total = (bet * multiplier) as long
    money.add(player, total)
    Players.msg(player, "§b§lSCRATCH OFF §> §fYou won §e${money.map(total)} §ffrom §e${money.map(bet)}§f! §7(§ax${ScratchOffUtils.DOUBLE_FORMAT.format(multiplier)}§7)")
}

Commands.create().assertPlayer().handler { command ->
    showScratchOff(command.sender())
}.register("scratchoff", "scratchoffs", "scratch")

@CompileStatic(TypeCheckingMode.SKIP)
class ScratchOffUtils {
    public static final String SCRATCH_OFF_INVENTORY_NAME = "§8Scratch Off"

    public static final long MINIMUM_BET = 100
    public static final long MAXIMUM_BET = 100000000
    public static final Material LOSING_MATERIAL = Material.BARRIER

    public static Map<Material, Map<String, ? extends Object>> REWARDS = [
            (Material.COAL): [
                    name: "§8§lCoal",
                    multiplier: 1.25D,
                    chance: 25
            ] as Map<String, ? extends Object>,
            (Material.IRON_INGOT): [
                    name: "§f§lIron Ingot",
                    multiplier: 1.5D,
                    chance: 12
            ],
            (Material.GOLD_INGOT): [
                    name: "§6§lGold Ingot",
                    multiplier: 2.0D,
                    chance: 6
            ],
            (Material.LAPIS_LAZULI): [
                    name: "§9§lLapis Lazuli",
                    multiplier: 2.5D,
                    chance: 4
            ],
            (Material.DIAMOND): [
                    name: "§b§lDiamond",
                    multiplier: 3.0D,
                    chance: 2
            ],
            (Material.EMERALD): [
                    name: "§a§lEmerald",
                    multiplier: 5.0D,
                    chance: 1
            ]
    ]

    public static final NumberFormat DOUBLE_FORMAT = NumberFormat.getNumberInstance(Locale.US)

    static {
        DOUBLE_FORMAT.setMinimumFractionDigits(0)
        DOUBLE_FORMAT.setMaximumFractionDigits(2)
    }

    public static Map<UUID, Map<String, Object>> ACTIVE_SCRATCH_OFFS

    static void init() {
        ACTIVE_SCRATCH_OFFS = Persistent.of("active_scratch_offs", new ConcurrentHashMap<UUID, Map<String, Object>>()).get()
    }
}