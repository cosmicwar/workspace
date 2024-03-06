package scripts.factions.features.customset.struct

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scripts.shared.core.cfg.entries.MaterialEntry
import scripts.shared.core.cfg.entries.StringEntry
import scripts.shared.core.cfg.entries.list.StringListEntry
import scripts.factions.features.customset.data.CustomWeaponData
import scripts.factions.features.customset.struct.CustomSet
import scripts.shared.legacy.utils.FastItemUtils

class CustomSetWeapon {

    CustomSet customSet

    CustomSetWeapon(CustomSet customSet, Material weaponMaterial, String weaponName, List<String> weaponLore) {
        this.customSet = customSet

        customSet.config.addDefault([
                new MaterialEntry("weaponType", weaponMaterial),
                new StringEntry("weaponName", weaponName),
                new StringListEntry("weaponLore", weaponLore)
        ])
    }

    ItemStack getWeapon() {
        def lore = this.getWeaponLore().collect { line ->
            this.customSet.colorRemapper.apply(line)
        }

        def weapon = FastItemUtils.createItem(this.getWeaponType(), this.customSet.colorRemapper.apply("§<bold>§<rainbow>${this.customSet.getDisplayName()} ${this.getWeaponName()}"), lore, false)

        CustomWeaponData data = new CustomWeaponData(this.customSet.internalName, false)
        data.write(weapon)

        return weapon
    }

    boolean isHolding(Player player) {
        def stack = player.getInventory().getItemInMainHand()

        if (stack == null || stack.getType() == Material.AIR) {
            return false
        }

        CustomWeaponData data = CustomWeaponData.read(stack)

        if (data == null) {
            return false
        }

        return data.getSet().internalName == this.customSet.internalName
    }

    Material getWeaponType() {
        return customSet.config.getMaterialEntry("weaponType").value
    }

    String getWeaponName() {
        return customSet.config.getStringEntry("weaponName").value
    }

    List<String> getWeaponLore() {
        return customSet.config.getStringListEntry("weaponLore").value
    }

}
