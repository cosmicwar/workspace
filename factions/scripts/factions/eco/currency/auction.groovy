package scripts.factions.eco.currency

import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.tags.ItemTagType
import scripts.shared.legacy.Auction
import scripts.shared.legacy.utils.FastItemUtils

import java.util.function.BiPredicate

BiPredicate<Auction, Player> armorWeapons = { Auction auction, Player player ->
    if (auction.item == null) {
        return false
    }
    Material material = auction.item.getType()
    String name = material.name()

    return material == Material.BOW || name.endsWith("_SWORD") || name.endsWith("_HELMET") || name.endsWith("_LEGGINGS") || name.endsWith("_CHESTPLATE") || name.endsWith("_BOOTS") || name.endsWith("_AXE")
}

BiPredicate<Auction, Player> gemstones = { Auction auction, Player player ->
    if (auction.item == null) {
        return false
    }

    Closure<Boolean> isGemstone = Exports.ptr("isGemstone") as Closure<Boolean>
    if (isGemstone != null && isGemstone.call(auction.item)) {
        return true
    }

    Closure<Boolean> isWandGemstone = Exports.ptr("isWandGemstone") as Closure<Boolean>
    if (isWandGemstone != null && isWandGemstone.call(auction.item)) {
        return true
    }
    
    Closure<Boolean> isPhantomGemstone = Exports.ptr("isPhantomGemstone") as Closure<Boolean>
    if (isPhantomGemstone != null && isPhantomGemstone.call(auction.item)) {
        return true
    }

    Closure<Boolean> isQuarryGemstone = Exports.ptr("quarries:isGemstone") as Closure<Boolean>
    if (isQuarryGemstone != null && isQuarryGemstone.call(auction.item)) {
        return true
    }

    Closure<Boolean> isAscendantGemstone = Exports.ptr("ascendantmine:isGemstone") as Closure<Boolean>
    if (isAscendantGemstone != null && isAscendantGemstone.call(auction.item)) {
        return true
    }

    return false
}

BiPredicate<Auction, Player> customEnchants = { Auction auction, Player player ->
    if (auction.item == null) {
        return false
    }

    Closure<Boolean> isEnchant = Exports.ptr("isPvPPvEEnchant") as Closure<Boolean>
    if (isEnchant != null && isEnchant.call(auction.item)) {
        return true
    }

    Closure<Boolean> isDust = Exports.ptr("isEnchantDust") as Closure<Boolean>
    if (isDust != null && isDust.call(auction.item)) {
        return true
    }

    return false
}

BiPredicate<Auction, Player> pets = { Auction auction, Player player -> return (Exports.ptr("pets_is_withdrawn_pet_item") as Closure)?.call(auction.item).asBoolean() }

BiPredicate<Auction, Player> bankNotes = { Auction auction, Player player ->
    Closure<Boolean> isBankNote = Exports.ptr("isBankNote") as Closure<Boolean>
    if (isBankNote != null && isBankNote.call(auction.item)) {
        return auction.item.getItemMeta().getDisplayName().contains("Bank Note")
    }
    return false
}

BiPredicate<Auction, Player> pouches = { Auction auction, Player player ->
    {
        return auction.item != null && FastItemUtils.hasCustomTag(auction.item, new NamespacedKey(Starlight.plugin, "pouch"), ItemTagType.STRING)
    }
}

BiPredicate<Auction, Player> omnitoolSkins = { Auction auction, Player player ->
    Closure<Boolean> isOmnitoolSkin = Exports.ptr("isOmnitoolSkin") as Closure<Boolean>
    return isOmnitoolSkin != null && isOmnitoolSkin.call(auction.item)
}

BiPredicate<Auction, Player> itemSkins = { Auction auction, Player player ->
    Closure<Boolean> isItemSkin = (Exports.ptr("itemskins:isWithdrawnSkinItem") as Closure<Boolean>)
    return isItemSkin != null && isItemSkin.call(auction.item)
}

BiPredicate<Auction, Player> petSkins = { Auction auction, Player player ->
    return (Exports.ptr("headpets:isPetSkin") as Closure<Boolean>)?.call(auction.item).asBoolean()
}

BiPredicate<Auction, Player> robotsAndDrills = { Auction auction, Player player ->
    Closure<Boolean> isRobot = Exports.ptr("isRobotItem") as Closure<Boolean>
    Closure<Boolean> isDrill = Exports.ptr("drills:isDrillItemStack") as Closure<Boolean>
    return (isRobot != null && isRobot.call(auction.item)) || (isDrill != null && isDrill.call(auction.item))
}

BiPredicate<Auction, Player> crystals = { Auction auction, Player player ->
    Closure<Boolean> isCrystal = Exports.ptr("isCrystal") as Closure<Boolean>
    if (isCrystal == null) {
        isCrystal = Exports.ptr("crystals:isCrystal") as Closure<Boolean>
    }

    return isCrystal != null && isCrystal.call(auction.item)
}

BiPredicate<Auction, Player> boosters = { Auction auction, Player player ->
    Closure<Boolean> isBooster = Exports.ptr("isBooster") as Closure<Boolean>
    return isBooster != null && isBooster.call(auction.item)
}

BiPredicate<Auction, Player> ranks = { Auction auction, Player player ->
    return FastItemUtils.hasCustomTag(auction.item, new NamespacedKey(Starlight.plugin, "rank_note"), ItemTagType.STRING)
}

BiPredicate<Auction, Player> luckyAndShiny = { Auction auction, Player player ->
    Closure<Boolean> isLuckyBall = Exports.ptr("isLuckyBall") as Closure<Boolean>
    Closure<Boolean> isLuckyChest = Exports.ptr("isLuckyChest") as Closure<Boolean>
    Closure<Boolean> isShinyTreasure = Exports.ptr("isShinyTreasure") as Closure<Boolean>
    Closure<Boolean> isShinyBox = Exports.ptr("isShinyBox") as Closure<Boolean>
    Closure<Boolean> isNewShinyTreasure = Exports.ptr("shinytreasures:isShinyTreasureItemStack") as Closure<Boolean>

    return (isLuckyBall != null && isLuckyBall.call(auction.item)) ||
            (isLuckyChest != null && isLuckyChest.call(auction.item)) ||
            (isShinyTreasure != null && isShinyTreasure.call(auction.item)) ||
            (isShinyBox != null && isShinyBox.call(auction.item)) ||
            isNewShinyTreasure?.call(auction.item)

}

BiPredicate<Auction, Player> nicknames = { Auction auction, Player player ->
    return (auction.item.itemMeta.displayName != null && auction.item.itemMeta.displayName.endsWith("Nickname")) && FastItemUtils.hasId(auction.item) && auction.item.lore?.size() ?: 0 >= 2
}

//0  1  2  3  4  5  6  7  8
//9  10 11 12 13 14 15 16 17
//18 19 20 21 22 23 24 25 26
//27 28 29 30 31 32 33 34 35
//36 37 38 39 40 41 42 43 44
//45 46 47 48 49 50 51 52 53

Exports.ptr("auction_decoration", [
        "0a0f0f0a0",
        "a0f0f0f0a",
        "0a0f0f0a0",
        "a0f0f0f0a",
        "0a0f0f0a0",
        "a0f0f0f0a",
])

// Don't use slot 49. (will be replaced)
Exports.ptr("auction_sections", [
        // CUSTOM ONES
        armor_weapons : [
                title: "Armor & Weapons",
                icon : "diamond_chestplate 1",
                slot : 2,
                sort : armorWeapons
        ],
//        // DEFAULT ONES
//        misc          : [
//                title: "Miscellaneous",
//                icon : "clock 1",
//                slot : 32,
//                sort : { Auction auction, Player player ->
//                    return !armorWeapons.test(auction, player) &&
//                            !gemstones.test(auction, player) &&
////                            !customEnchants.test(auction, player) &&
//                            !bankNotes.test(auction, player) &&
//                            !omnitoolSkins.test(auction, player) &&
//                            !itemSkins.test(auction, player) &&
//                            !petSkins.test(auction, player) &&
//                            !pouches.test(auction, player) &&
//                            !crystals.test(auction, player) &&
//                            !robotsAndDrills.test(auction, player) &&
//                            !boosters.test(auction, player) &&
//                            !pets.test(auction, player) &&
//                            !luckyAndShiny.test(auction, player) &&
//                            !ranks.test(auction, player) &&
//                            !nicknames.test(auction, player)
//                }
//        ],
        all_items     : [
                title: "All Listings",
                icon : "compass 1",
                slot : 46,
                sort : { Auction auction, Player player -> return true }
        ],
        your_items    : [
                title: "Your Listings",
                icon : "diamond 1",
                slot : 52,
                sort : { Auction auction, Player player -> return auction.owner == player.getUniqueId() }
        ]
])

Starlight.watch("scripts/shared/legacy/_auction2.groovy")
