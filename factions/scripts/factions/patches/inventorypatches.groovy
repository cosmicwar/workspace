package scripts.factions.patches

import com.google.common.collect.Lists
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import io.netty.util.internal.ThreadLocalRandom
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentOffer
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.EnchantingInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

//Events.subscribe(EntityDamageEvent.class, EventPriority.HIGHEST).filter(EventFilters.<EntityDamageEvent> ignoreCancelled()).handler { event ->
//}

// Duping / Ghost Item Patches
Events.subscribe(PlayerInteractEvent.class, EventPriority.LOW).handler {event ->
    if (Patches.limitInventoryGhostItems) {
        Player player = event.getPlayer();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (offHand != null && offHand.getAmount() <= 0) {
            player.getInventory().setItemInOffHand((ItemStack)null);
        }

        if (mainHand != null && mainHand.getAmount() <= 0) {
            player.getInventory().setItemInMainHand((ItemStack)null);
        }

    }
}

Events.subscribe(ItemSpawnEvent.class, EventPriority.LOW).filter(EventFilters.<ItemSpawnEvent> ignoreCancelled()).handler { event ->
    if (event.getEntity().getItemStack().getAmount() <= 0) {
        if (Patches.limitInventoryGhostItems) {
            event.setCancelled(true);
        }
    }
}

Events.subscribe(BlockDispenseEvent.class, EventPriority.LOW).filter(EventFilters.<BlockDispenseEvent> ignoreCancelled()).handler { event ->
    if (Patches.limitInventoryGhostItems) {
        InventoryHolder inventoryHolder = (InventoryHolder)event.getBlock().getState();
        
        inventoryHolder.inventory.each {itemstack ->
            if (itemstack != null && itemstack.getType() != Material.AIR && itemstack.getAmount() <= 0) {
                event.setCancelled(true);
            }
        }
//        Iterator var4 = inventoryHolder.getInventory().iterator();
//
//        while(var4.hasNext()) {
//            ItemStack itemStack = (ItemStack)var4.next();
//            if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() < 0) {
//                event.setCancelled(true);
//            }
//        }
    }
}

Events.subscribe(PlayerDropItemEvent.class, EventPriority.HIGHEST).filter(EventFilters.<PlayerDropItemEvent> ignoreCancelled()).handler { event ->
    if (Patches.limitInventoryGhostItems) {
        if (!event.getPlayer().isOnline()) {
            event.setCancelled(true);
        }
    }
}

Events.subscribe(PlayerCommandPreprocessEvent.class, EventPriority.NORMAL).filter(EventFilters.<PlayerCommandPreprocessEvent> ignoreCancelled()).handler { event ->
    if (Patches.limitInventoryGhostItems) {
        event.getPlayer().closeInventory();
    }
}

Events.subscribe(PlayerTeleportEvent.class, EventPriority.NORMAL).filter(EventFilters.<PlayerTeleportEvent> ignoreCancelled()).handler { event ->
    if (Patches.limitInventoryOnTeleport) {
        Player player = event.getPlayer();
        boolean isCitizensNPC = player.hasMetadata("NPC");
        if (!isCitizensNPC) {
            if (Patches.limitInventoryOnTeleportReasons.contains(event.getCause())) {
                event.getPlayer().closeInventory();
            }
        }
    }
}

Events.subscribe(InventoryPickupItemEvent.class, EventPriority.NORMAL).filter(EventFilters.<InventoryPickupItemEvent> ignoreCancelled()).handler { event ->
    if (Patches.limitInventoryHopperIntake) {
        Inventory inventory = event.getInventory();
        if (inventory != null) {
            if (inventory.getType() == InventoryType.HOPPER) {
                Item item = event.getItem();
                if (item != null) {
                    if (Patches.limitInventoryHopperIntakeTypes.contains(item.getItemStack().getType())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}

Events.subscribe(EnchantItemEvent.class, EventPriority.HIGHEST).filter(EventFilters.<EnchantItemEvent> ignoreCancelled()).handler { event ->
    if (event.getEnchantBlock() != null && event.getInventory() != null) {
        if (Patches.miscEnchantmentTableAutoLapis) {
            EnchantingInventory enchantingInventory = (EnchantingInventory)event.getInventory();
            if (enchantingInventory != null) {
                ItemStack lapisItem = new ItemStack(Material.LAPIS_LAZULI);
                lapisItem.setAmount(64);
                enchantingInventory.setSecondary(lapisItem);
            }
        }
    }
}

Events.subscribe(InventoryClickEvent.class, EventPriority.HIGHEST).filter(EventFilters.<InventoryClickEvent> ignoreCancelled()).handler { event ->
    if (event.getView() != null && event.getInventory() != null) {
        if (event.getInventory().getType() == InventoryType.ENCHANTING) {
            if (Patches.miscEnchantmentTableAutoLapis) {
                ItemStack item = event.getCurrentItem();
                if (item != null && item.getType() != Material.AIR) {
                    if (item.getType() == Material.LAPIS_LAZULI) {
                        ItemStack lapisItem = new ItemStack(Material.LAPIS_LAZULI);
                        lapisItem.setAmount(64);
                        Player player = (Player)event.getWhoClicked();
                        if (lapisItem.isSimilar(item) && event.getRawSlot() == 1) {
                            event.setCancelled(true);
                            event.setResult(Event.Result.DENY);
                            player.updateInventory();
                        } else if (event.getCursor() != null && lapisItem.isSimilar(event.getCursor()) && event.getClick() == ClickType.DOUBLE_CLICK) {
                            event.setCancelled(true);
                            event.setResult(Event.Result.DENY);
                            player.updateInventory();
                        }

                    }
                }
            }
        }
    }
}

Events.subscribe(InventoryCloseEvent.class, EventPriority.HIGHEST).handler { event ->
    if (event.getView() != null && event.getInventory() != null) {
        if (event.getInventory().getType() == InventoryType.ENCHANTING) {
            if (Patches.miscEnchantmentTableAutoLapis) {
                ((EnchantingInventory)event.getInventory()).setSecondary((ItemStack)null);
            }
        }
    }
}

Events.subscribe(InventoryOpenEvent.class, EventPriority.HIGHEST).filter(EventFilters.<InventoryOpenEvent> ignoreCancelled()).handler { event ->
    if (event.getView() != null && event.getInventory() != null) {
        if (event.getInventory().getType() == InventoryType.ENCHANTING) {
            if (Patches.miscEnchantmentTableAutoLapis) {
                ItemStack lapisItem = new ItemStack(Material.LAPIS_LAZULI);
                lapisItem.setAmount(64);
                ((EnchantingInventory)event.getInventory()).setSecondary(lapisItem);
            }
        }
    }
}

Events.subscribe(PrepareItemEnchantEvent.class, EventPriority.LOW).filter(EventFilters.<PrepareItemEnchantEvent> ignoreCancelled()).handler { event ->
    if (event.getEnchantBlock() != null && event.getOffers() != null) {
        if (Patches.miscEnchantmentTypeLimiter) {
            EnchantmentOffer[] var5;
            int var4 = (var5 = (EnchantmentOffer[])event.getOffers().clone()).length;

            for(int var3 = 0; var3 < var4; ++var3) {
                EnchantmentOffer enchantmentOffer = var5[var3];
                if (enchantmentOffer != null && Patches.miscEnchantmentTypeLimiterTypes.contains(enchantmentOffer.getEnchantment().getName())) {
                    enchantmentOffer.setEnchantment(Enchantment.DURABILITY);
                    enchantmentOffer.setEnchantmentLevel(ThreadLocalRandom.current().nextInt(Enchantment.DURABILITY.getStartLevel(), Enchantment.DURABILITY.getMaxLevel()));
                }
            }

        }
    }
}

Events.subscribe(EnchantItemEvent.class, EventPriority.HIGHEST).filter(EventFilters.<EnchantItemEvent> ignoreCancelled()).handler { event ->
    if (event.getEnchantBlock() != null && event.getEnchantsToAdd() != null) {
        if (Patches.miscEnchantmentTypeLimiter) {
            List<Enchantment> toChange = Lists.newArrayList();
            Iterator var4 = event.getEnchantsToAdd().entrySet().iterator();

            while(var4.hasNext()) {
                Map.Entry<Enchantment, Integer> entry = (Map.Entry)var4.next();
                if (entry.getKey() != null && Patches.miscEnchantmentTypeLimiterTypes.contains(((Enchantment)entry.getKey()).getName())) {
                    toChange.add((Enchantment)entry.getKey());
                }
            }

            if (!toChange.isEmpty()) {
                var4 = toChange.iterator();

                while(var4.hasNext()) {
                    Enchantment enchantment = (Enchantment)var4.next();
                    event.getEnchantsToAdd().remove(enchantment, event.getEnchantsToAdd().get(enchantment));
                    event.getEnchantsToAdd().put(Enchantment.DURABILITY, ThreadLocalRandom.current().nextInt(Enchantment.DURABILITY.getStartLevel(), Enchantment.DURABILITY.getMaxLevel()));
                }
            }

        }
    }
}

Events.subscribe(InventoryClickEvent.class, EventPriority.HIGHEST).handler { event ->
    if (event.getWhoClicked() instanceof Player) {
        if (Patches.miscContainerLimiter) {
            Player player = (Player)event.getWhoClicked();
            if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                Inventory inventory = event.getClickedInventory();
                Iterator var5 = Patches.miscContainerLimiterTypes.entrySet().iterator();

                while(true) {
                    while(var5.hasNext()) {
                        Map.Entry<InventoryType, List<Material>> containerFilter = (Map.Entry)var5.next();
                        ItemStack clickedItem;
                        if (inventory != null && inventory.getType() == containerFilter.getKey()) {
                            if (event.getClick() == ClickType.SWAP_OFFHAND) {
                                clickedItem = player.getInventory().getItemInOffHand();
                                if (clickedItem != null && ((List)containerFilter.getValue()).contains(clickedItem.getType())) {
                                    event.setCancelled(true);
                                    Players.msg(player, Patches.miscContainerLimiterMsgDeny);
                                }

                                return;
                            }

                            clickedItem = event.getClick() == ClickType.NUMBER_KEY ? event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) : event.getCursor();
                            if (clickedItem != null && ((List)containerFilter.getValue()).contains(clickedItem.getType())) {
                                event.setCancelled(true);
                                Players.msg(player, Patches.miscContainerLimiterMsgDeny);
                            }
                        } else if (event.getClick().isShiftClick() && event.getInventory().getType() == containerFilter.getKey() && inventory == event.getWhoClicked().getInventory()) {
                            clickedItem = event.getCurrentItem();
                            if (clickedItem != null && ((List)containerFilter.getValue()).contains(clickedItem.getType())) {
                                event.setCancelled(true);
                                Players.msg(player, Patches.miscContainerLimiterMsgDeny);
                            }
                        }
                    }

                    return;
                }
            }
        }
    }
}

Events.subscribe(InventoryDragEvent.class, EventPriority.HIGHEST).filter(EventFilters.<InventoryDragEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscContainerLimiter) {
        Inventory inventory = event.getInventory();
        Iterator var4 = Patches.miscContainerLimiterTypes.entrySet().iterator();

        while(true) {
            Map.Entry containerFilter;
            do {
                do {
                    if (!var4.hasNext()) {
                        return;
                    }

                    containerFilter = (Map.Entry)var4.next();
                } while(inventory == null);
            } while(inventory.getType() != containerFilter.getKey());

            Player player = (Player)event.getWhoClicked();
            if (player.isOp() || player.hasPermission("miscellaneous.bypass")) {
                return;
            }

            ItemStack itemMoved = event.getOldCursor();
            if (itemMoved != null && ((List)containerFilter.getValue()).contains(itemMoved.getType())) {
                int inventorySize = event.getInventory().getSize();
                Iterator var9 = event.getRawSlots().iterator();

                while(var9.hasNext()) {
                    int rawSlots = (Integer)var9.next();
                    if (rawSlots < inventorySize) {
                        event.setCancelled(true);
                        Players.msg(player, Patches.miscContainerLimiterMsgDeny);
                        break;
                    }
                }
            }
        }
    }
}
