package scripts.factions.features.pack.itemskins

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Arrow
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.projectiles.ProjectileSource
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.factions.features.pack.itemskins.data.SkinnedItemData
import scripts.factions.features.pack.itemskins.data.SkinOrbData

import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

class ItemSkinListener {

    Closure getItemSkin = Exports.ptr("itemskins:getItemSkin") as Closure
    Closure createItemSkin = Exports.ptr("itemskins:createItemSkin") as Closure
    Closure addSkinToItem = Exports.ptr("itemskins:addSkinToItem") as Closure
    Closure removeSkinFromItem = Exports.ptr("itemskins:removeSkinFromItem") as Closure
    Closure hasItemSkin = Exports.ptr("itemskins:hasItemSkin") as Closure

    Closure forEquippedSkins = Exports.ptr("itemskins:forEquippedSkins") as Closure
    Closure invalidateEquippedSkins = Exports.ptr("itemskins:invalidateEquippedSkins") as Closure

    Map<UUID, Map<UUID, Long>> recentDamagers = new ConcurrentHashMap<>()

    Map<String, ?> config

    ItemSkinListener() {
        events()
    }

    void events() {

        Events.subscribe(InventoryClickEvent.class).handler { event ->
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != event.getWhoClicked()) return

            if (!event.rightClick) return

            Player player = event.getWhoClicked() as Player
            ItemStack currentItem = event.getCurrentItem()
            ItemStack cursor = event.getCursor()

            if (currentItem == null || currentItem.type == Material.AIR) return
            if (cursor != null && cursor.type != Material.AIR) return

            Inventory inventory = player.getOpenInventory().getTopInventory()
            if (inventory.getType() != InventoryType.CRAFTING) return

            SkinnedItemData skinData = SkinnedItemData.read(currentItem)
            if (skinData == null) return

            ItemSkin skin = skinData.getItemSkin()
            if (skin == null) return

            event.setCancelled(true)

            ItemStack skinItem = createItemSkin.call(skinData.getItemSkin()) as ItemStack
            removeSkinFromItem(currentItem)

            event.getView().setCursor(skinItem)
            player.setItemOnCursor(skinItem)
            player.updateInventory()

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1F, 1F)
            Players.msg(player, getConfig()["messageRemoved"] as String)
        }

        Events.subscribe(InventoryClickEvent.class).filter(EventFilters.<InventoryClickEvent> ignoreCancelled()).handler { event ->
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != event.getWhoClicked()) return

            if (!event.isLeftClick()) return

            Player player = event.getWhoClicked() as Player
            ItemStack currentItem = event.getCurrentItem()
            ItemStack cursor = event.getCursor()

            if (currentItem == null || currentItem.type == Material.AIR || cursor == null || cursor.type == Material.AIR) return

            Inventory inventory = player.getOpenInventory().getTopInventory()
            if (inventory.getType() != InventoryType.CRAFTING) return

            SkinOrbData skinOrbData = SkinOrbData.read(cursor)
            if (skinOrbData == null) return

            event.setCancelled(true)

            if(!skinOrbData.canBeAppliedTo(currentItem)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F)
                Players.msg(player, "§cThis orb cannot be applied to this item.")
                return
            }

            if (hasItemSkin.call(currentItem)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F)
                Players.msg(player, "§cThis item already has a skin applied to it.")
                return
            }

            ItemSkin orbSkin = skinOrbData.getItemSkin()
            if (orbSkin == null) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F)
                Players.msg(player, "§cThis orb is invalid.")
                return
            }

            cursor.amount == 1 ? (cursor = null) : (cursor.setAmount(cursor.amount - 1))
            event.getView().setCursor(cursor)
            player.setItemOnCursor(cursor)

            addSkinToItem.call(currentItem, orbSkin)
            player.updateInventory()

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1F, 1F)
            Players.msg(player, getConfig()["messageApplied"] as String)
        }

        Events.subscribe(EntityDamageByEntityEvent.class).filter(EventFilters.<EntityDamageByEntityEvent> ignoreCancelled()).handler({ event ->
            Entity attacker = EnchantUtils.getLiableDamager(event)
            if ((!(attacker instanceof Player)) || !(event.getEntity() instanceof LivingEntity)) return

            if (event.getDamager() instanceof Arrow) {
                // TODO bow handling
            }

            Player player = (Player) attacker
            forEquippedSkins(player, { ItemStack itemStack, ItemSkin skin ->
                skin.onAttack(player, itemStack, event.getEntity() as LivingEntity, event)
            })
        })

        //removes recent damagers on log off
        Events.subscribe(PlayerQuitEvent.class).handler { event ->
            recentDamagers.remove(event.getPlayer().getUniqueId())
        }

        Events.subscribe(EntityDamageByEntityEvent.class).filter(EventFilters.<EntityDamageByEntityEvent> ignoreCancelled()).handler({ event ->
            Entity entity = event.getEntity()
            if (!(entity instanceof Player)) return

            Player player = (Player) entity

            Entity attacker = EnchantUtils.getLiableDamager(event)
            recentDamagers.computeIfAbsent(event.getEntity().getUniqueId(), v -> new ConcurrentHashMap<>()).put(attacker.getUniqueId(), System.currentTimeMillis())
            forEquippedSkins(player, { ItemStack itemStack, ItemSkin skin ->
                skin.onDamaged(player, itemStack, attacker, event)
            })
        })

        Events.subscribe(EntityDeathEvent.class).filter(EventFilters.<EntityDeathEvent> ignoreCancelled()).handler { event ->
            EntityDamageEvent lastDamageCause = event.getEntity().getLastDamageCause()
            if (lastDamageCause == null) return

            Entity lastDamager = EnchantUtils.getLiableDamager(lastDamageCause)
            if (!(lastDamager instanceof Player)) return

            Player player = lastDamager as Player
            forEquippedSkins(player, { ItemStack itemStack, ItemSkin skin ->
                skin.onKill(player, itemStack, event.getEntity(), event)
            })
        }

        Events.subscribe(EntityDamageEvent.class).filter(EventFilters.<EntityDamageEvent> ignoreCancelled()).handler({ event ->
            Entity entity = event.getEntity()
            if (!(entity instanceof Player)) return

            Player player = (Player) entity
            forEquippedSkins(player, { ItemStack itemStack, ItemSkin skin ->
                skin.onEnvironmentDamaged(player, itemStack, event.getCause(), event)
            })
        })

        Events.subscribe(ProjectileLaunchEvent.class).filter(EventFilters.<ProjectileLaunchEvent> ignoreCancelled()).handler({ event ->
            Projectile projectile = event.getEntity()
            ProjectileSource shooter = projectile.getShooter()
            if (!(shooter instanceof Player)) return

            Player player = shooter as Player
            forEquippedSkins(player, { ItemStack itemStack, ItemSkin skin ->
                skin.onProjectileLaunch(player, itemStack, projectile, event)
            })
        })

        Events.subscribe(EntityShootBowEvent.class).filter(EventFilters.<EntityShootBowEvent> ignoreCancelled()).handler({ event ->
            LivingEntity shooter = event.getEntity()
            if (!(shooter instanceof Player)) return

            Projectile arrow = event.getProjectile() as Projectile
            Player player = shooter as Player

            forEquippedSkins(player, { ItemStack itemStack, ItemSkin skin ->
                skin.onBowShoot(player, itemStack, arrow, event)
            })
        })

        Events.subscribe(BlockDamageEvent.class, EventPriority.MONITOR).filter(EventFilters.<BlockDamageEvent> ignoreCancelled()).handler { event ->
            Player player = event.player
            if (player.gameMode == GameMode.CREATIVE) return

            Block block = event.getBlock()
            forEquippedSkins(player, { ItemStack itemStack, ItemSkin skin ->
                skin.onBlockDamage(player, itemStack, block, event)
            })
        }

        Events.subscribe(PlayerArmorChangeEvent.class).handler({
            Player player = it.player
            ItemStack oldItem = it.oldItem
            ItemStack newItem = it.newItem
            if (areItemsSimilar(oldItem, newItem)) return

            invalidateEquippedSkins(player)

            if (oldItem != null) {
                ((getItemSkin.call(oldItem) ?: null) as ItemSkin)?.onUnequip(player, oldItem)
            }

            if (newItem != null) {
                ((getItemSkin.call(newItem) ?: null) as ItemSkin)?.onEquip(player, newItem)
            }
        })

        Events.subscribe(BlockBreakEvent.class, EventPriority.MONITOR).filter(EventFilters.<BlockBreakEvent> ignoreCancelled()).handler { event ->
            Player player = event.player
            if (player.gameMode == GameMode.CREATIVE || event.getClass().getSimpleName() == "BlockBreakWithDropsEvent") return

            Block block = event.getBlock()
            forEquippedSkins(player, { ItemStack itemStack, ItemSkin skin ->
                skin.onBlockBreak(player, itemStack, block, event)
            })
        }
    }

    boolean areItemsSimilar(ItemStack stack1, ItemStack stack2) {
        if ((stack1 == null && stack2 != null) || (stack2 == null && stack1 != null)) return false
        if (stack1 == stack2) return true

        return stack1.getType() == stack2.getType() && stack1.hasItemMeta() == stack2.hasItemMeta() && getItemSkin.call(stack1) == getItemSkin.call(stack2)
    }

    int getRecentDamagerCount(Player player, long withinMillis) {
        Map<UUID, Long> damagers = recentDamagers.get(player.getUniqueId())
        if (damagers == null) {
            return 0
        }

        long now = System.currentTimeMillis()
        return (int) damagers.entrySet().stream().filter(entry -> now - entry.getValue() <= withinMillis).count()
    }

    List<UUID> getRecentDamagers(Player player, long withinMillis, int limit) {
        Map<UUID, Long> damagers = recentDamagers.get(player.getUniqueId())
        if (damagers == null) {
            return Collections.emptyList()
        }

        long now = System.currentTimeMillis()
        return damagers.entrySet().stream()
                .filter(entry -> now - entry.getValue() <= withinMillis)
                .sorted(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .limit(limit)
                .collect(Collectors.toList())
    }


    Map<String, ?> getConfig() {
        return config == null ? config = Exports.ptr("skinconfig") as Map<String, ?> : config
    }

}

