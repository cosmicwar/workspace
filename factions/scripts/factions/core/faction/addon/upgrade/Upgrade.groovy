package scripts.factions.core.faction.addon.upgrade

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Material
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.content.dbconfig.entries.IntEntry
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.addon.upgrade.data.StoredUpgrade
import scripts.factions.core.faction.data.Faction

@CompileStatic(TypeCheckingMode.SKIP)
class Upgrade {

    String internalId

    Upgrade(String internalId) {
        this.internalId = internalId
    }

    boolean upgradeFaction(Faction faction, int count = 1) {
        def upgrade = faction.getUpgradeData().getUpgrade(this.getInternalId(), new StoredUpgrade(this.getInternalId()))

        if (upgrade.level + count > this.getMaxLevel()) {
            return false
        }

        upgrade.level += count
        faction.queueSave()
        return true
    }

    double getPrice(int level) {
        if (level <= 3) {
            return priceCalc * (level + 2)
        }
        if (level <= 10) {
            return priceCalc * (level + 5)
        }
        if (level <= 20) {
            return priceCalc * (level + 10)
        }
        return priceCalc * (level + 15) // ?? this many
    }

    int getMaxLevel() {
        return Upgrades.getConfig(this.getInternalId()).getIntEntry("max_level").getValue()
    }

    double getPriceCalc() {
        return Upgrades.getConfig(this.getInternalId()).getDoubleEntry("priceCalc").getValue()
    }

    Material getIcon() {
        return Upgrades.getConfig(this.getInternalId()).getMaterialEntry("icon").getValue()
    }

    List<String> getDescription() {
        return Upgrades.getConfig(this.getInternalId()).getStringListEntry("description").getValue()
    }

    String getDisplayName() {
        return Upgrades.getConfig(this.getInternalId()).getStringEntry("display_name").getValue()
    }

    String getCurrencyId() {
        return Upgrades.getConfig(this.getInternalId()).getStringEntry("currency_id").getValue()
    }


}

