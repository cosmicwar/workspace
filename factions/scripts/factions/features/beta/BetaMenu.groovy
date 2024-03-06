package scripts.factions.features.beta

import com.google.common.collect.Sets
import org.bukkit.Difficulty
import org.bukkit.GameRule
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Repairable
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Commands
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.data.item.BookEnchantmentData
import scripts.factions.features.enchant.items.EnchantmentOrbType
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.shared.legacy.utils.BroadcastUtils
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.BukkitUtils
import scripts.shared.utils.gens.VoidWorldGen17


class BetaMenu {

    static Set<ItemStack> items = Sets.newConcurrentHashSet()
    static List enchTiers = [EnchantmentTier.SIMPLE, EnchantmentTier.UNIQUE, EnchantmentTier.ELITE, EnchantmentTier.ULTIMATE, EnchantmentTier.LEGENDARY, EnchantmentTier.SOUL, EnchantmentTier.HEROIC]
    private static NamespacedKey BETA_ITEM = new NamespacedKey(Starlight.plugin, "betaItem")

    BetaMenu() {
        for (EnchantmentTier tier : enchTiers) {
            items.add(Enchantments.mysteryBook.getStack(Enchantments.createMysteryBook(tier)))
        }

        items.add(Enchantments.enchantmentOrb.getStack(Enchantments.createEnchantmentOrb(100, 6, EnchantmentOrbType.WEAPON)))
        items.add(Enchantments.enchantmentOrb.getStack(Enchantments.createEnchantmentOrb(100, 3, EnchantmentOrbType.ARMOR)))
        items.add(Enchantments.whiteScroll.getStack(Enchantments.createWhiteScroll()))
//        items.add(Enchantments.holyWhiteScroll.getStack(Enchantments.createHolyWhiteScroll()))
        items.add(Enchantments.blackScroll.getStack(Enchantments.createBlackScroll(75)))
        items.add(Enchantments.transmogScroll.getStack(Enchantments.createTransmogScroll()))
        items.add(Enchantments.itemNametag.getStack(Enchantments.createItemNametag()))
        items.add(Enchantments.soulPearl.getStack(Enchantments.createSoulPearl()))
        items.add(Enchantments.randomSoulGenerator.getStack(Enchantments.createRandomSoulGenerator()))

        commands()
    }

    static void commands() {
        Commands.create().assertPlayer().handler { ctx ->
            createBetaMenu(ctx.sender())
        }.register("beta", "betamenu")

        Commands.create().assertPlayer().assertOp().handler {ctx ->
            def item = ctx.sender().getInventory().getItemInMainHand()

            if (item == null || item.type.isAir()) {
                ctx.reply("§cYou must be holding an item to give all players.")
                return
            }

            BukkitUtils.getOnlineNonSpoofPlayers().each {
                FastInventoryUtils.addOrBox(it.getUniqueId(), it, null, item, null)
            }

            ctx.reply("§aGiven §e${item.getAmount()}x §a${item.type.name()} §ato all players.")
        }.register("giveall")

        Commands.create().assertPlayer().handler {ctx ->
            def item = ctx.sender().getInventory().getItemInMainHand()

            if (item == null || item.type.isAir()) {
                ctx.reply("§cYou must be holding an item to dupe.")
                return
            }

            def bookData = BookEnchantmentData.read(item)
            if (bookData == null) {
                ctx.reply("§cYou must be holding an enchanted book to dupe.")
                return
            }

            item.setAmount(64)
            ctx.reply("§aDuplicated §e${item.getAmount()}x §a${item.type.name()} §afor you.")
        }.register("dupe")

        Commands.create().assertPlayer().handler {ctx ->
            ctx.sender().setFoodLevel(20)
            ctx.sender().setSaturation(20)

            ctx.reply("§aFed you.")
        }.register("feed")

        Commands.create().assertPlayer().handler {ctx ->
            ctx.sender().inventory.contents.findAll { it != null }.each {
                if (it instanceof Repairable) {
                    it.setDurability(it.type.maxDurability)
                }
            }

            ctx.reply("§aRepaired all items in your inventory.")
        }.register("fixall")
    }

    static void createBetaMenu(Player player, int page = 1) {
        MenuBuilder menu

        menu = MenuUtils.createPagedMenu("Beta Menu", items.toList(), {ItemStack item, int index ->
            return item
        }, page, false, [
                { Player p, ClickType t, int slot ->
                    def item = menu.get().getItem(slot)
                    FastInventoryUtils.addOrBox(player.getUniqueId(), player, null, item, null)
                    p.updateInventory()
                },
                { Player p, ClickType t, int slot ->
                    createBetaMenu(p, page + 1)
                },
                { Player p, ClickType t, int slot ->
                    createBetaMenu(p, page - 1)
                },
        ])

        menu.openSync(player)
    }
}
