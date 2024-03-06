package scripts.factions.core.faction.addon.upgrade

import com.google.common.collect.Maps
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.command.context.PlayerContext
import org.starcade.starlight.helper.utils.Players
import scripts.shared.core.cfg.ConfigCategory
import scripts.shared.core.cfg.utils.DBConfigUtil
import scripts.shared.core.cfg.RegularConfig
import scripts.shared.core.cfg.data.ConfigType
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.shared.core.cfg.entries.IntEntry
import scripts.shared.core.cfg.entries.MaterialEntry
import scripts.shared.core.cfg.entries.StringEntry
import scripts.shared.core.cfg.entries.list.StringListEntry
import scripts.factions.core.faction.FCommandUtil
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.addon.upgrade.data.StoredUpgrade
import scripts.factions.core.faction.data.Faction
import scripts.factions.core.faction.data.Member
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.utils.MenuUtils
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.systems.MenuBuilder
import scripts.shared.systems.MenuEvent
import scripts.shared.utils.DataUtils
import scripts.shared.utils.MenuDecorator

@CompileStatic(TypeCheckingMode.SKIP)
class Upgrades {

    static Map<String, Upgrade> upgrades = Maps.newConcurrentMap()

    private static NamespacedKey UPGRADE_KEY = new NamespacedKey(Starlight.plugin, "faction_upgrade_gui")

    static ConfigCategory config

    Upgrades() {
        GroovyScript.addUnloadHook {
            Factions.fCommand.subCommands.removeIf { it.aliases.find { it.equalsIgnoreCase("upgrades") } != null }
            Factions.fCommand.build()
        }

        config = Factions.config.getOrCreateCategory("upgrades", "Upgrades", Material.GRASS_BLOCK, [
                "§aUpgrade your faction with",
                "§aupgrades to make it stronger."
        ])

        createUpgrade("faction_power", "§aFaction Power", [
                "§aIncrease the max power of",
                "§ayour faction by 25."
        ], Material.GRASS_BLOCK, 5, "money", 1000D)

        createUpgrade("faction_size", "§aFaction Size", [
                "§aIncrease the max size of",
                "§ayour faction by 1 member."
        ], Material.GRASS_BLOCK, 10, "money", 1000D)

        createUpgrade("faction_sandbot_radius", "§aFaction Radius", [
                "§aIncrease the max radius of",
                "§ayour sandbot's by 1 block."
        ], Material.GRASS_BLOCK, 5, "money", 100000D)

        events()

        Factions.fCommand.create("upgrades").description("View faction upgrades").register { PlayerContext ctx ->
            FCommandUtil.factionMemberFromCommand(ctx) { faction, member ->
                openUpgradeGui(ctx.sender(), member, faction)
            }
        }.build()
    }

    static def createUpgrade(String internalId, String displayName, List<String> description = [], Material material = Material.BOOK, int maxLevel = 1, String currencyId = "money", Double priceCalc = 1000D) {
        def upgrade = new Upgrade(internalId)

        upgrades.put(internalId, upgrade)

        config.getOrCreateConfig(internalId, displayName, material, description)
        config.addEntries(internalId, [
                new IntEntry("max_level", maxLevel),
                new DoubleEntry("priceCalc", priceCalc),
                new StringEntry("currency_id", currencyId),
                new StringEntry("display_name", displayName),
                new StringListEntry("description", description),
                new MaterialEntry("icon", material)
        ], true)

        Factions.config.queueSave()
    }

    static RegularConfig getConfig(String internalId) {
        return config.getConfig(internalId)
    }

    static def events() {

    }

    static def openUpgradeGui(Player player, Member member, Faction faction, int page = 1) {
        MenuBuilder menu
        menu = MenuUtils.createPagedMenu("§3Faction Upgrades", upgrades.values().toList(), { Upgrade upgrade, Integer i ->
            def lore = []
            lore.addAll(upgrade.description)

            def factionUpgrade = faction.upgradeData.getUpgrade(upgrade.internalId)

            lore.add("")
            lore.add("§3Level: §a${factionUpgrade ? factionUpgrade.level : "§c0"}§7/§f${upgrade.maxLevel}")
            lore.add("")
            lore.add("§3Price: §a\$${NumberUtils.format(upgrade.getPrice(factionUpgrade ? factionUpgrade.level + 1: 1))} ${upgrade.currencyId == "money" ? "" : upgrade.currencyId}")

            def item = FastItemUtils.createItem(upgrade.icon, upgrade.displayName, lore, false)
            FastItemUtils.addGlow(item)

            DataUtils.setTag(item, UPGRADE_KEY, PersistentDataType.STRING, upgrade.internalId)

            return item
        }, page, false, [
                { Player p, ClickType t, int slot ->
                    def item = menu.get().getItem(slot)
                    if (item == null || item.type == Material.AIR) return

                    if (!DataUtils.hasTag(item, UPGRADE_KEY, PersistentDataType.STRING)) return

                    def upgrade = upgrades.get(DataUtils.getTag(item, UPGRADE_KEY, PersistentDataType.STRING))
                    if (upgrade == null) return

                    def factionUpgrade = faction.upgradeData.getUpgrade(upgrade.internalId, new StoredUpgrade(upgrade.internalId, 0))

                    openUpgrade(p, member, faction, factionUpgrade)
                },
                { Player p, ClickType t, int slot ->
                    openUpgradeGui(p, member, faction, page + 1)
                },
                { Player p, ClickType t, int slot ->
                    openUpgradeGui(p, member, faction, page - 1)
                },
        ] as List<MenuEvent>)

        menu.openSync(player)
    }

    static def openUpgrade(Player player, Member member, Faction faction, StoredUpgrade storedUpgrade) {
        def upgrade = upgrades.get(storedUpgrade.internalId)
        if (upgrade == null) return

        MenuBuilder menu = new MenuBuilder(54, "§3Faction Upgrade")

        MenuDecorator.decorate(menu, [
                "aaa333aaa",
                "aa33b33aa",
                "a33bbb33a",
                "a33bbb33a",
                "a33bbb33a",
                "aaa333aaa",
        ])

        def lore = []
        lore.addAll(upgrade.description)
        lore.add("")
        lore.add("§3Level: §a${storedUpgrade.level}§7/§f${upgrade.maxLevel}")
        lore.add("")
        lore.add("§3Price: §a\$${NumberUtils.format(upgrade.getPrice(storedUpgrade.level + 1))}")

        def item = FastItemUtils.createItem(upgrade.icon, upgrade.displayName, lore, false)
        FastItemUtils.addGlow(item)

        menu.set(2, 5, item) { Player p, ClickType t, int slot ->
            if (upgrade.upgradeFaction(faction)) {
                Players.playSound(p, Sound.ENTITY_PLAYER_LEVELUP)
                openUpgrade(p, member, faction, storedUpgrade)
            }
        }

        menu.set(6, 5, FastItemUtils.createItem(Material.BARRIER, "§cBack", [], false)) { Player p, ClickType t, int slot ->
            openUpgradeGui(p, member, faction)
        }

        menu.openSync(player)
    }

}

