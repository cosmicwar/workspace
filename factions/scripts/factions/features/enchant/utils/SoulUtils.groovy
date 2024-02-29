package scripts.factions.features.enchant.utils

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import org.starcade.starlight.enviorment.Exports
import groovy.transform.CompileStatic
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.data.item.SoulGemData
import scripts.shared.legacy.utils.FastItemUtils

@CompileStatic
class SoulUtils {

    static Set<UUID> soulMode = Sets.newConcurrentHashSet()

    static ItemStack createSoulGem(int souls) {
        String name = "§c§lSoul Gem [{color}§l{souls}§c§l]"
        name = name.replace("{color}", (souls < 5000 ? "§6" : "§4")).replace("{souls}", souls.toString())

        ItemStack soulGem = FastItemUtils.createItem(
                Material.EMERALD,
                name,
                [
                        "§r",
                        "§c* Click this item to toggle §nSoul Mode",
                        "§7While in \"Soul Mode\" your ACTIVE god tier",
                        "§7enchantments will activate and drain souls",
                        "§7for as long as this mode is enabled.",
                        "§r",
                        "§c* §7Use §c§n/splitsouls§7 with this item",
                        "§7to split souls out of it.",
                        "§r",
                        "§c* §7Stack other §cSoul Gems §7on top of this",
                        "§7one to combine their soul counts."
                ]
        )

        FastItemUtils.setShiny(soulGem)
        new SoulGemData(souls).write(soulGem)

        return soulGem
    }

    static ItemStack updateSoulCount(ItemStack stack, int souls) {
        String name = "§c§lSoul Gem [{color}§l{souls}§c§l]"
        name = name.replace("{color}", (souls < 5000 ? "§6" : "§4")).replace("{souls}", souls.toString())

        FastItemUtils.setDisplayName(stack, name)
        return stack
    }

    static boolean toggleSoulMode(Player player) {
        boolean isSoulMode = soulMode.contains(player.getUniqueId())
        if (isSoulMode) {
            soulMode.remove(player.getUniqueId())
        } else {
            soulMode.add(player.getUniqueId())
        }
        return !isSoulMode
    }

    static boolean isSoulMode(Player player) {
        return soulMode.contains(player.getUniqueId())
    }

    static Map<ItemStack, SoulGemData> getCarriedGems(Player player) {
        Map<ItemStack, SoulGemData> carriedGems = Maps.newHashMap()

        for (int slot = 0; slot <= player.getInventory().getSize(); slot++) {
            ItemStack itemStack = player.getInventory().getItem(slot)
            if (itemStack == null || itemStack.getType() != Material.EMERALD) {
                continue
            }

            SoulGemData soulGemData = SoulGemData.read(itemStack)
            if (soulGemData == null || soulGemData.getSouls() <= 0) {
                continue
            }

            carriedGems.put(itemStack, soulGemData)
        }

        return carriedGems
    }

    static boolean consumeSouls(Player player, int consume, boolean force = false) {
        if (isSoulMode(player) || force) {
            for (Map.Entry<ItemStack, SoulGemData> entry : getCarriedGems(player).entrySet()) {
                ItemStack soulGem = entry.getKey()
                SoulGemData soulData = entry.getValue()

                if (soulData.getSouls() > consume) {
                    soulData.setSouls(soulData.getSouls() - consume)
                    soulData.write(soulGem)
                    updateSoulCount(soulGem, soulData.getSouls())
                    player.updateInventory()
                    return true
                }
            }
        }
        return false
    }
}
