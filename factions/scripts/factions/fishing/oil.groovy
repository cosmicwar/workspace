package scripts.factions.fishing

import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import scripts.shared.legacy.CurrencyStorage
import scripts.shared.legacy.CurrencyUtils
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.NumberUtils
import scripts.shared.legacy.utils.RandomUtils
import scripts.shared.legacy.wrappers.Console
import scripts.shared.utils.Persistent

CurrencyStorage oil = Persistent.of("oil", CurrencyUtils.register(
        "oil",
        "Oil",
        "oil",
        [ "oil" ],
        false,
        true,
        false,
        "§] §>",
        { balance -> return "§e${NumberUtils.format(balance)} §foil" },
        { top -> }
)).get() as CurrencyStorage

Exports.ptr("oil", oil)

Events.subscribe(PlayerInteractEvent.class, EventPriority.HIGHEST).handler { event ->
    if (event.getHand() != EquipmentSlot.HAND) {
        return
    }
    ItemStack item = event.getItem()

    if (item == null) {
        return
    }
    Action action = event.getAction()

    if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
        return
    }
    int oilAmount

    switch (item.getType()) {
        case Material.PUFFERFISH:
            oilAmount = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? 85 : 5
            break
        case Material.TROPICAL_FISH:
            oilAmount = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? 25 : 5
            break
        case Material.COD:
            oilAmount = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? 35 : 5
            break
        case Material.SALMON:
            oilAmount = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? 60 : 5
            break
        default:
            return
    }
    Player player = event.getPlayer()
    FastInventoryUtils.use(player)
    oil.add(player, oilAmount, true)
}

Events.subscribe(PlayerFishEvent.class, EventPriority.HIGHEST).filter(EventFilters.ignoreCancelled()).handler { event ->
    if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
        return
    }
    event.getCaught().remove()

    Player player = event.getPlayer()

    int luckLevel = 0

    ItemStack rod = player.getItemInHand()

    if (rod != null) {
        luckLevel = rod.getEnchantmentLevel(Enchantment.LUCK)
    }
    Map<String, Integer> rewards

    switch (luckLevel) {
        case 0:
            rewards = [
                    "give %player% tropicalfish 1": 65,
                    "giverankupmoney %player% 30": 3,
                    "giverankupmoney %player% 40": 3,
                    "givepouch %player% credits 5000 50000": 3,
                    "givecrate %player% vote 1": 1,
                    "give %player% clownfish 1 name:&6&lGuppy": 8,
                    "givepouch %player% xp 10 50": 4,
                    "givebooster %player% random 2 5": 1,
                    "give %player% leather_boots 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_chestplate 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_helmet 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_leggings 1 name:&f&lFisherman's_Attire": 1,
                    "nothing": 8
            ]
            break
        case 1:
            rewards = [
                    "give %player% tropicalfish 1": 65,
                    "give %player% clownfish 1 name:&6&lGuppy": 10,
                    "giverankupmoney %player% 30": 4,
                    "giverankupmoney %player% 40": 3,
                    "givepouch %player% credits 5000 50000": 2,
                    "givepouch %player% xp 10 50": 3,
                    "givecrate %player% vote 1": 1,
                    "givebooster %player% random 2 5": 1,
                    "give %player% leather_boots 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_chestplate 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_helmet 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_leggings 1 name:&f&lFisherman's_Attire": 1,
                    "nothing": 7
            ]
            break
        case 2:
            rewards = [
                    "give %player% tropicalfish 1": 55,
                    "give %player% pufferfish 1 name:&b&lBlubber_Fish": 1,
                    "give %player% fish 1 name:&1&lNorthern_Pike": 3,
                    "give %player% clownfish 1 name:&6&lGuppy": 10,
                    "give %player% salmon 1 name:&a&lGumbo": 8,
                    "giverankupmoney %player% 30": 3,
                    "giverankupmoney %player% 40": 2,
                    "givepouch %player% credits 5000 50000": 3,
                    "givecrate %player% vote 1": 1,
                    "givecrate %player% lobster 1": 1,
                    "givebooster %player% random 2 5": 1,
                    "givepouch %player% xp 10 50": 2,
                    "give %player% leather_boots 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_chestplate 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_helmet 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_leggings 1 name:&f&lFisherman's_Attire": 1,
                    "nothing": 6
            ]
            break
        case 3:
            rewards = [
                    "give %player% tropicalfish 1": 54,
                    "give %player% pufferfish 1 name:&b&lBlubber_Fish": 1,
                    "give %player% fish 1 name:&1&lNorthern_Pike": 6,
                    "give %player% clownfish 1 name:&6&lGuppy": 10,
                    "give %player% salmon 1 name:&a&lGumbo": 9,
                    "giverankupmoney %player% 30": 3,
                    "giverankupmoney %player% 40": 2,
                    "givepouch %player% credits 5000 50000": 2,
                    "givecrate %player% vote 1": 1,
                    "givecrate %player% lobster 1": 1,
                    "givebooster %player% random 2 5": 1,
                    "givepouch %player% xp 10 50": 1,
                    "give %player% leather_boots 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_chestplate 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_helmet 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_leggings 1 name:&f&lFisherman's_Attire": 1,
                    "nothing": 5
            ]
            break
        case 4:
            rewards = [
                    "give %player% tropicalfish 1": 53,
                    "give %player% pufferfish 1 name:&b&lBlubber_Fish": 2,
                    "give %player% fish 1 name:&1&lNorthern_Pike": 8,
                    "give %player% clownfish 1 name:&6&lGuppy": 12,
                    "give %player% salmon 1 name:&a&lGumbo": 10,
                    "giverankupmoney %player% 30": 1,
                    "giverankupmoney %player% 40": 1,
                    "givepouch %player% credits 5000 50000": 1,
                    "givecrate %player% vote 1": 1,
                    "givecrate %player% lobster 1": 1,
                    "givebooster %player% random 2 5": 1,
                    "givepouch %player% xp 10 50": 1,
                    "give %player% leather_boots 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_chestplate 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_helmet 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_leggings 1 name:&f&lFisherman's_Attire": 1,
                    "nothing": 4
            ]
            break
        case 5:
            rewards = [
                    "give %player% tropicalfish 1": 50,
                    "give %player% pufferfish 1 name:&b&lBlubber_Fish": 3,
                    "give %player% fish 1 name:&1&lNorthern_Pike": 9,
                    "give %player% clownfish 1 name:&6&lGuppy": 12,
                    "give %player% salmon 1 name:&a&lGumbo": 10,
                    "giverankupmoney %player% 30": 2,
                    "giverankupmoney %player% 40": 2,
                    "givepouch %player% credits 5000 50000": 1,
                    "givecrate %player% vote 1": 1,
                    "givecrate %player% lobster 1": 1,
                    "givebooster %player% random 2 5": 1,
                    "givepouch %player% xp 10 50": 1,
                    "give %player% leather_boots 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_chestplate 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_helmet 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_leggings 1 name:&f&lFisherman's_Attire": 1,
                    "nothing": 3
            ]
            break
        case 6:
            rewards = [
                    "give %player% tropicalfish 1": 45,
                    "give %player% pufferfish 1 name:&b&lBlubber_Fish": 5,
                    "give %player% fish 1 name:&1&lNorthern_Pike": 10,
                    "give %player% clownfish 1 name:&6&lGuppy": 15,
                    "give %player% salmon 1 name:&a&lGumbo": 12,
                    "giverankupmoney %player% 30": 1,
                    "giverankupmoney %player% 40": 1,
                    "givepouch %player% credits 5000 50000": 1,
                    "givecrate %player% vote 1": 1,
                    "givecrate %player% lobster 1": 1,
                    "givebooster %player% random 2 5": 1,
                    "givepouch %player% xp 10 50": 1,
                    "give %player% leather_boots 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_chestplate 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_helmet 1 name:&f&lFisherman's_Attire": 1,
                    "give %player% leather_leggings 1 name:&f&lFisherman's_Attire": 1,
                    "nothing": 2
            ]
            break
        default:
            rewards = [ "nothing": 100 ]
            break
    }
    String reward = RandomUtils.get(rewards)

    if (reward == "nothing") {
        Players.msg(player, "§b§lFISHING §> §fWhoops, looks like you didn't catch anything!")
        return
    }
    Console.dispatchCommand(reward.replace("%player%", player.getName()))
}