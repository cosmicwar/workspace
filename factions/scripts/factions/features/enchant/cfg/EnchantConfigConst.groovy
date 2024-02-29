package scripts.factions.features.enchant.cfg

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import scripts.factions.content.dbconfig.data.ConfigEntry
import scripts.factions.content.dbconfig.entries.BooleanEntry
import scripts.factions.content.dbconfig.entries.IntEntry
import scripts.factions.content.dbconfig.entries.MaterialEntry
import scripts.factions.content.dbconfig.entries.StringEntry
import scripts.factions.content.dbconfig.entries.list.StringListEntry

@CompileStatic(TypeCheckingMode.SKIP)
class EnchantConfigConst {
    static BooleanEntry debug = new BooleanEntry("debug", false, "Enchant debug mode")

    // books
    static StringEntry enchantBookName = new StringEntry("enchantBookName", "{tierColor}§l§n{enchantName} {romanNumeral}")
    static StringListEntry enchantBookLore = new StringListEntry("enchantBookLore", [
            "§r",
            "§7{description}",
            "§r",
            "§7Success Rate: §a{percent}%",
            "§7Destroy Rate: §c{destroyPercent}%",
            "§r",
            "§7Click to apply to an item: §f{applicability}"
    ])

    static StringEntry mysteryEnchantBookName = new StringEntry("mysteryEnchantBookName", "{tierColor}{tierName} Enchantment Book §7§o(Right-Click)")
    static StringListEntry mysteryEnchantBookLore = new StringListEntry("mysteryEnchantBookLore", [
            "§7Examine to receive a random",
            "§7{tierColor}§l{tierName} §7enchantment book."
    ])

    static StringEntry messageEnchantFailed = new StringEntry("messageEnchantFailed", "§] §> §cThe enchantment book failed to apply!")
    static StringEntry messageEnchantFailedDestroyed = new StringEntry("messageEnchantFailedDestroyed", "§] §> §cThe enchantment failed and your {item} has been destroyed!")
    static StringEntry messageEnchantSuccess = new StringEntry("messageEnchantSuccess", "§] §> §aSuccessfully applied {enchant} §ato your {item}§a!")

    // whitescroll
    static MaterialEntry whiteScrollMaterial = new MaterialEntry("whiteScrollMaterial", Material.PAPER)
    static StringEntry whiteScrollName = new StringEntry("whiteScrollName", "§eWhite Scroll")
    static StringListEntry whiteScrollLore = new StringListEntry("whiteScrollLore", [
            "§7Prevents an item from being destroyed",
            "§7due to a failed Enchantment Book.",
            "§r",
            "§ePlace scroll onto an item to apply."
    ])

    static StringEntry whiteScrollSuccess = new StringEntry("whiteScrollSuccess", "§] §> §aSuccessfully applied a white scroll to your {item}§a!")
    static StringEntry whiteScrollFailed = new StringEntry("whiteScrollFailed", "§] §> §cFailed to apply a white scroll to your {item}§c!")
    static IntEntry whiteScrollModelData = new IntEntry("whiteScrollModelData", 0)

    // holy whitescroll
    static MaterialEntry holyWhiteScrollMaterial = new MaterialEntry("holyWhiteScrollMaterial", Material.PAPER)
    static StringEntry holyWhiteScrollName = new StringEntry("holyWhiteScrollName", "§6§lHoly White Scroll")
    static StringListEntry holyWhiteScrollLore = new StringListEntry("holyWhiteScrollLore", [
            "§r",
            "§eA legendary scroll item that",
            "§ecan be applied to armor/weapons,",
            "§egives a 100% chance of not losing",
            "§ethe blessed item on death.",
            "§r",
            "§6§nREQ:§e White Scroll"
    ])

    static StringEntry holyWhiteScrollSuccess = new StringEntry("holyWhiteScrollSuccess", "§] §> §aSuccessfully applied a §6Holy White Scroll §ato your {item}§a!")
    static StringEntry holyWhiteScrollFailed = new StringEntry("holyWhiteScrollFailed", "§] §> §cYou have already applied the max §6Holy White Scroll's §cto this item!")
    static IntEntry holyWhiteScrollMaxUses = new IntEntry("holyWhiteScrollMaxUses", 2)
    static IntEntry holyWhiteScrollModelData = new IntEntry("holyWhiteScrollModelData", 0)

    // blackscroll
    static MaterialEntry blackScrollMaterial = new MaterialEntry("blackScrollMaterial", Material.INK_SAC)
    static StringEntry blackScrollName = new StringEntry("blackScrollName", "§f§lBlack Scroll")
    static StringListEntry blackScrollLore = new StringListEntry("blackScrollLore", [
            "§7Removes a random enchantment",
            "§7from an item and converts",
            "§7it into an Enchantment Book.",
            "§r",
            "§7Book Success Rate: §f{percent}%",
            "§fPlace scroll onto an item to extract."
    ])

    static StringEntry blackScrollSuccess = new StringEntry("blackScrollSuccess", "§] §> §aSuccessfully applied a black scroll to your {item}§a!")
    static IntEntry blackScrollModelData = new IntEntry("blackScrollModelData", 0)

    // enchant dust
    static MaterialEntry enchantDustMaterial = new MaterialEntry("enchantDustMaterial", Material.SUGAR)
    static StringEntry enchantDustName = new StringEntry("enchantDustName", "{tierColor}{tierName} Magic Dust")
    static StringListEntry enchantDustLore = new StringListEntry("enchantDustLore", [
            "§a+{percent}% Success Rate",
            "§7Apply to any {tierColor}{tierName} enchantment book",
            "§7to increase the success rate by §a{percent}%§7.",
    ])

    static StringEntry enchantDustSuccess = new StringEntry("enchantDustSuccess", "§] §> §aSuccessfully applied {tierColor}{tierName} Magic Dust §ato your {item}§a!")
    static IntEntry enchantDustModelData = new IntEntry("enchantDustModelData", 0)

    // mystery enchant dust
    static MaterialEntry mysteryEnchantDustMaterial = new MaterialEntry("mysteryEnchantDustMaterial", Material.LIGHT)
    static StringEntry mysteryEnchantDustName = new StringEntry("mysteryEnchantDustName", "§dMystery Magic Dust §7§o(Right-Click)")
    static StringListEntry mysteryEnchantDustLore = new StringListEntry("mysteryEnchantDustLore", [
            "§a+§fUNKNOWN% Success Rate",
            "§7Apply to any enchantment book",
            "§7to increase the success rate by §a§fUNKNOWN%§7.",
    ])

    static StringEntry mysteryEnchantDustSuccess = new StringEntry("mysteryEnchantDustSuccess", "§] §> §aYou have receieved §dMystery Magic Dust §afrom your {item}§a!")
    static IntEntry mysteryEnchantDustModelData = new IntEntry("mysteryEnchantDustModelData", 0)

    // enchant orb
    static MaterialEntry enchantOrbMaterial = new MaterialEntry("enchantOrbMaterial", Material.ENDER_EYE)
    static StringEntry enchantOrbName = new StringEntry("enchantOrbName", "§6§l{type} Enchantment Orb [§a§n{maxSlots}§6§l]")
    static StringListEntry enchantOrbLore = new StringListEntry("enchantOrbLore", [
            "§a{percent}% Success Rate",
            "§r",
            "§6+{increaseSlots}§7 Enchantment Slots",
            "§6{maxSlots} Max Enchantment Slots",
            "§r",
            "§eIncreases the # of enchantment",
            "§eslots on an item by §6{increaseSlots}§e,",
            "§eup to a maximum of {maxSlots}.",
            "§r",
            "§7Drag n' Drop onto {type} to apply."
    ])

    static IntEntry enchantOrbMaxSlots = new IntEntry("enchantOrbMaxSlotsArmour", 12)
    static IntEntry enchantOrbDefSlots = new IntEntry("enchantOrbDefSlotsArmour", 9)
    static StringEntry enchantOrbSuccess = new StringEntry("enchantOrbSuccess", "§] §> §aSuccessfully applied a §6{type} Enchantment Orb §ato your {item}§a!")
    static StringEntry enchantOrbFailed = new StringEntry("enchantOrbFailed", "§] §> §cFailed to apply a §6{type} Enchantment Orb §cto your {item}§c!")
    static IntEntry enchantOrbModelData = new IntEntry("enchantOrbModelData", 0)

    // transmog
    static MaterialEntry transmogMaterial = new MaterialEntry("transmogMaterial", Material.PAPER)
    static StringEntry transmogName = new StringEntry("transmogName", "§e§lTransmog Scroll")
    static StringListEntry transmogLore = new StringListEntry("transmogLore", [
            "§7Organizes enchants by &e&nrarity &7on item",
            "§7and adds the §dlore §bcount §7to name.",
            "§r",
            "§e§oPlace scroll on item to apply."
    ])

    static StringEntry transmogSuccess = new StringEntry("transmogSuccess", "§a§l(!) §aSuccessfully applied a §eTransmog Scroll §ato your {item}§a!")
    static IntEntry transmogModelData = new IntEntry("transmogModelData", 0)

    static MaterialEntry randomSoulGenMaterial = new MaterialEntry("randomSoulGenMaterial", Material.EMERALD)
    static StringEntry randomSoulGenName = new StringEntry("randomSoulGenName", "§c§lRandom Soul Gem Generator")
    static StringListEntry randomSoulGenLore = new StringListEntry("randomSoulGenLore", [
            "§r",
            "§c* Click this item to receive one of the following §nSoul Gems",
            "§r",
            "§c§lSoul Gem [§6§l500§c§l]",
            "§c§lSoul Gem [§6§l1000§c§l]",
            "§c§lSoul Gem [§6§l1500§c§l]",
            "§c§lSoul Gem [§6§l2500§c§l]",
            "§c§lSoul Gem [§4§l5000§c§l]",
            "§c§lSoul Gem [§4§l10000§c§l]",
    ])

    static MaterialEntry soulPearlMaterial = new MaterialEntry("soulPearlMaterial", Material.ENDER_PEARL)
    static StringEntry soulPearlName = new StringEntry("soulPearlName", "§5§lSoul Pearl")
    static StringListEntry soulPearlLore = new StringListEntry("soulPearlLore", [
            "§5A pearl with a lower cooldown, costs §c§l10 souls §r§5per use"
    ])

    static MaterialEntry timeMachineMaterial = new MaterialEntry("timeMachineMaterial", Material.CLOCK)
    static StringEntry timeMachineName = new StringEntry("timeMachineName", "§e§lTime Machine")
    static StringListEntry timeMachineLore = new StringListEntry("timeMachineLore", [
            "§6A powerful machine that can bring you back in time.",
            "",
            "§6Click once to activate, and click again within 30 seconds",
            "§6to bring you back to your starting location.",
            "§6Careful, this item breaks after 30 seconds, so be cautious",
            "§6with your time."
    ])

    static Collection<ConfigEntry<?>> getWhiteScrollEntries() {
        return [
                whiteScrollMaterial, whiteScrollName, whiteScrollLore,
                whiteScrollModelData, whiteScrollSuccess, whiteScrollFailed
        ]
    }

    static Collection<ConfigEntry<?>> getHolyWhiteScrollEntries() {
        return [
                holyWhiteScrollMaterial, holyWhiteScrollName, holyWhiteScrollLore,
                holyWhiteScrollSuccess, holyWhiteScrollFailed, holyWhiteScrollMaxUses, holyWhiteScrollModelData
        ]
    }

    static Collection<ConfigEntry<?>> getBlackScrollEntries() {
        return [
                blackScrollMaterial, blackScrollName, blackScrollLore,
                blackScrollSuccess, blackScrollModelData
        ]
    }

    static Collection<ConfigEntry<?>> getEnchantDustEntries() {
        return [
                enchantDustMaterial, enchantDustName, enchantDustLore,
                enchantDustSuccess, enchantDustModelData
        ]
    }

    static Collection<ConfigEntry<?>> getMysteryEnchantDustEntries() {
        return [
                mysteryEnchantDustMaterial, mysteryEnchantDustName, mysteryEnchantDustLore,
                mysteryEnchantDustSuccess, mysteryEnchantDustModelData
        ]
    }

    static Collection<ConfigEntry<?>> getEnchantOrbEntries() {
        return [
                enchantOrbMaterial, enchantOrbName, enchantOrbLore,
                enchantOrbMaxSlots, enchantOrbDefSlots,
                enchantOrbSuccess, enchantOrbFailed, enchantOrbModelData
        ]
    }

    static Collection<ConfigEntry<?>> getRandomSoulGenEntries() {
        return [
                randomSoulGenMaterial, randomSoulGenName, randomSoulGenLore
        ]
    }

    static Collection<ConfigEntry<?>> getMysteryEnchantBookEntries() {
        return [
                mysteryEnchantBookName, mysteryEnchantBookLore
        ]
    }

    static Collection<ConfigEntry<?>> getTransmogEntries() {
        return [
                transmogSuccess, transmogName, transmogLore, transmogModelData, transmogMaterial
        ]
    }

    static Collection<ConfigEntry<?>> getEnchantBookEntries() {
        return [
                enchantBookName, enchantBookLore, messageEnchantFailed, messageEnchantFailedDestroyed, messageEnchantSuccess
        ]
    }

    static Collection<ConfigEntry<?>> getSoulPearlEntries() {
        return [
                soulPearlMaterial, soulPearlName, soulPearlLore
        ]
    }

    static Collection<ConfigEntry<?>> getTimeMachineEntries() {
        return [
                timeMachineMaterial, timeMachineName, timeMachineLore
        ]
    }

}