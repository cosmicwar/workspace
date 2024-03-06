package scripts.factions.features.beta

import com.google.common.collect.Sets
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Commands
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.items.EnchantmentOrbType
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.shared.legacy.utils.BroadcastUtils
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder


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
        items.add(Enchantments.holyWhiteScroll.getStack(Enchantments.createHolyWhiteScroll()))
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
