package scripts.factions.eco.currency

import org.starcade.starlight.helper.Commands
import groovy.transform.Field
import org.bukkit.Material
import org.bukkit.entity.Player
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.systems.MenuBuilder

Commands.create().assertPlayer().handler { c ->
    Player sender = c.sender()
    showExpShopMenu(sender)
}.register("expshop", "xpshop")

class ExpShopItem {
    String name
    String displayName
    Material material
    List<String> commands
    int price

    ExpShopItem(String name, String displayName, Material material, int price, List<String> commands) {
        this.name = name
        this.displayName = displayName
        this.material = material
        this.price = price
        this.commands = commands
    }

}

@Field List<ExpShopItem> config = [
        new ExpShopItem("Helmet Enchants Lootbox", "§c§l§nHelmet§r §f§lEnchants Lootbox", Material.BOOK, 5000, ["mc give {player} lootbox_helmet"]),
        new ExpShopItem("Chestplate Enchants Lootbox", "§c§l§nChestplate§r §f§lEnchants Lootbox", Material.BOOK, 5000, ["mc give {player} lootbox_chestplate"]),
        new ExpShopItem("Leggings Enchants Lootbox", "§c§l§nLeggings§r §f§lEnchants Lootbox", Material.BOOK, 5000, ["mc give {player} lootbox_leggings"]),
        new ExpShopItem("Boots Enchants Lootbox", "§c§l§nBoots§r §f§lEnchants Lootbox", Material.BOOK, 5000, ["mc give {player} lootbox_boots"]),
        new ExpShopItem("Sword Enchants Lootbox", "§c§l§nSwords§r §f§lEnchants Lootbox", Material.BOOK, 5000, ["mc give {player} lootbox_swordbooks"]),

        new ExpShopItem("Soul Pearl", "§5§lSoul Pearl", Material.ENDER_PEARL, 50000, ["givesoulpearl {player} 1"]),

        new ExpShopItem("Mystery Pet Box", "§6§lMystery Pet Box", Material.ENDER_CHEST, 100000, ["customitems give {player} mystery_pet_box"]),
        new ExpShopItem("Omni Gem", "§6§lOmni Gem", Material.DIAMOND, 100000, ["customitems give {player} wildcard_gkit"]),

        new ExpShopItem("Soul Gem Generator", "§c§lSoul Gem Generator", Material.EMERALD, 250000, ["mc give {player} lootbox_soulgem"]),
        new ExpShopItem("Crystal Extractor", "§6§lCrystal Extractor §7§n75", Material.GHAST_TEAR, 250000, ["customitems give {player} extractCrystal 75"]),
        new ExpShopItem("Random double generator", "§f§lRandom §6§l(§c§kOOO§6§l,§c§kOOO§6§l) §f§lGenerator", Material.NETHER_STAR, 250000, ["mc give {player} lootbox_duo100"]),
        new ExpShopItem("Heroic Lootbox", "§c§kIII§4§l Heroic Lootbox §c§kIII", Material.NETHER_STAR, 250000, ["mc give {player} lootbox_heroic"]),


        new ExpShopItem("15 Lore Armor Orb", "§6§lArmor Enchantment Orb  [§a§n15§r§6§l]", Material.ENDER_EYE, 500000, ["enchorb ARMOR 100 15 {player} "]),
        new ExpShopItem("15 Weapon Armor Orb", "§6§lWeapon Enchantment Orb  [§a§n15§r§6§l]", Material.ENDER_EYE, 500000, ["enchorb WEAPON 100 15 {player} "]),
        new ExpShopItem("Essentials Bundle", "§d§lEssensial §f§lBundle", Material.BEACON, 500000, ["mc give {player} lootbox_essential"]),
        new ExpShopItem("Random M-Kit Generator", "§c§lRandom M-Kit Generator", Material.CHEST, 500000, ["mkitgenerator {player}"]),

        new ExpShopItem("Dungeon Lootbag: Abandoned Spaceship", "§6§lDungeon Lootbag §d§l§nAbandoned Spaceship§r:", Material.CHEST, 750000, ["givedloot {player} 0"]),
        new ExpShopItem("Secret Weapon Cache", "§7§l~§4§l-§8§l==§7§l= §8§lSecret §7§lWeapon §8§lCache §7§l=§8§l==§4§l-§7§l~", Material.ENDER_CHEST, 750000, ["givedloot {player} 1"]),

        new ExpShopItem("Mystery Mob Spawner", "§6§lMystery Mob Spawner", Material.SPAWNER, 1000000, ["mc give {player} lootbox_spawners"]),
        new ExpShopItem("Multi-Mask Generator", "§f§l§nMulti-Mask§r §9§lGeneratior", Material.DRAGON_EGG, 1000000, ["mc give {player} lootbox_maskgenerator"]),
        new ExpShopItem("Random Trio Generator", "§r§l✱ §e§lTriple Armour §r§lGenerator §r§l✱", Material.NETHER_STAR, 1000000, ["mc give {player} lootbox_triocrystal"]),
        new ExpShopItem("Crystal Extractor", "§6§lCrystal Extractor §7§n100", Material.GHAST_TEAR, 1000000, ["customitems give {player} extractCrystal 100"]),

        new ExpShopItem("Dungeon Lootbag: Destroyed Outpost", "§6§lDungeon Lootbag §4§l§nDestroyed Outpost§r:", Material.CHEST, 1250000, ["givedloot {player} 1"]),
]

void showExpShopMenu(Player player, int page = 1) {
    MenuBuilder builder = MenuUtils.createPagedMenu("§7Exp Shop", config, { ExpShopItem expItem, slot ->
        return FastItemUtils.createItem(expItem.material, expItem.displayName, ["", "§c§lPrice: §c" + expItem.price.toString() + " XP", ""])
    }, page, false, [
            { p, t, s ->

            },
            { p, t, s -> showExpShopMenu(player, page + 1) },
            { p, t, s -> showExpShopMenu(player, page - 1) }
    ])
    MenuUtils.syncOpen(player, builder)
}
