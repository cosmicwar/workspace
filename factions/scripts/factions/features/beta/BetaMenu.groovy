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
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Commands
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.items.EnchantmentOrbType
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.shared.legacy.utils.BroadcastUtils
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.utils.gens.VoidWorldGen17


class BetaMenu {

    static Set<ItemStack> items = Sets.newConcurrentHashSet()
    static List enchTiers = [EnchantmentTier.SIMPLE, EnchantmentTier.UNIQUE, EnchantmentTier.ELITE, EnchantmentTier.ULTIMATE, EnchantmentTier.LEGENDARY, EnchantmentTier.SOUL, EnchantmentTier.HEROIC]
    private static NamespacedKey BETA_ITEM = new NamespacedKey(Starlight.plugin, "betaItem")

    BetaMenu() {
        def world = WorldCreator.name("beta_map").environment(World.Environment.THE_END)
                .generator(new VoidWorldGen17())
                .createWorld()

        world.setDifficulty(Difficulty.NORMAL)

        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        world.setGameRule(GameRule.MOB_GRIEFING, false)
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false)
        world.setGameRule(GameRule.DO_INSOMNIA, false)
        world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false)
        world.setGameRule(GameRule.DO_VINES_SPREAD, false)
        world.setGameRule(GameRule.FALL_DAMAGE, false)
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0)

        world.setTime(14000L)

        def nmsworld = ((CraftWorld) world).getHandle()

        nmsworld.paperConfig().tickRates.mobSpawner = 2
        nmsworld.paperConfig().tickRates.containerUpdate = 4
        nmsworld.paperConfig().collisions.maxEntityCollisions = 0
        nmsworld.paperConfig().entities.armorStands.tick = false
        nmsworld.paperConfig().entities.armorStands.doCollisionEntityLookups = false
        nmsworld.paperConfig().chunks.preventMovingIntoUnloadedChunks = true

        nmsworld.spigotConfig.playerTrackingRange = 48
        nmsworld.spigotConfig.animalTrackingRange = 48
        nmsworld.spigotConfig.monsterTrackingRange = 64
        nmsworld.spigotConfig.otherTrackingRange = 48
        nmsworld.spigotConfig.miscTrackingRange = 48

        nmsworld.spigotConfig.monsterActivationRange = 16
        nmsworld.spigotConfig.animalActivationRange = 16
        nmsworld.spigotConfig.miscActivationRange = 8
        nmsworld.spigotConfig.itemMerge = 8
        nmsworld.spigotConfig.expMerge = 8
        nmsworld.spigotConfig.itemDespawnRate = 20 * 5 * 60
        nmsworld.spigotConfig.arrowDespawnRate = 20 * 10
        nmsworld.spigotConfig.hopperTransfer = 10
        nmsworld.spigotConfig.hopperCheck = 10
        nmsworld.spigotConfig.hopperAmount = 64

        nmsworld.spigotConfig.cactusModifier = 10
        nmsworld.spigotConfig.melonModifier = 10
        nmsworld.spigotConfig.mushroomModifier = 10
        nmsworld.spigotConfig.caneModifier = 10
        nmsworld.spigotConfig.pumpkinModifier = 10
        nmsworld.spigotConfig.saplingModifier = 10
        nmsworld.spigotConfig.wartModifier = 10
        nmsworld.spigotConfig.wheatModifier = 10
        
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
