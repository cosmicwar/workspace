package scripts.factions.features.enchant

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.regions.RegionContainer
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.entity.*
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.projectiles.ProjectileSource
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.factions.features.enchant.utils.SoulUtils
import scripts.factions.features.enchant.data.item.SoulGemData
import scripts.shared.legacy.utils.BroadcastUtils
import scripts.shared.utils.ItemType

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors

class EnchantListener {

    Closure forEquippedEnchants = Exports.ptr("enchantments:forEquippedEnchants") as Closure
    Closure invalidateEquippedEnchants = Exports.ptr("enchantments:invalidateEquippedEnchants") as Closure

    static Map<UUID, Map<UUID, Long>> recentDamagers = new ConcurrentHashMap<>()
    static Map<UUID, Long> silencedPlayers = new ConcurrentHashMap<>()

    Map<String, ?> config

    EnchantListener() {
        registerEvents()
    }

    static Map<CustomEnchantment, Integer> getEnchantments(ItemStack itemStack) {
        return (Exports.ptr("enchantments:getEnchantments") as Closure).call(itemStack) as Map<CustomEnchantment, Integer>
    }

    void registerEvents() {

        // handle soul gems
        Events.subscribe(PlayerInteractEvent.class).handler { event ->
            Player player = event.getPlayer()
            ItemStack inHand = event.item
            if (inHand == null || inHand.type != Material.EMERALD)
                return

            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return
            }

            SoulGemData soulGemData = SoulGemData.read(inHand)
            if (soulGemData == null) return

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block block = event.getClickedBlock()
                if (block != null && block.getState() instanceof DoubleChest) {
                    return
                }
                if (block != null && block.getState() instanceof Chest) {
                    return
                }
            }

            event.setCancelled(true)
            boolean newState = SoulUtils.toggleSoulMode(player)
            String message = String.format("§7§lSoul Mode: %s", newState ? "§a§lACTIVATED" : "§c§lDISABLED")
            player.sendMessage(message)
            if (newState) player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
            else player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.2f)
        }

        //combine soul gems
        Events.subscribe(InventoryClickEvent.class).handler {event ->
            ItemStack currentItem = event.getCurrentItem()
            ItemStack cursor = event.getCursor()

            if (currentItem == null || currentItem.getType() != Material.EMERALD || cursor == null || cursor.getType() != Material.EMERALD) return

            int currentItemAmount = currentItem.getAmount()
            int cursorAmount = cursor.getAmount()

            Player player = (Player) event.getWhoClicked()
            Inventory inventory = player.getOpenInventory().getTopInventory()
            if (inventory.getType() != InventoryType.CRAFTING) return


            SoulGemData soulGemDataCursor = SoulGemData.read(cursor)
            if (soulGemDataCursor == null) return

            SoulGemData soulGemDataClicked = SoulGemData.read(currentItem)
            if (soulGemDataClicked == null) return

            event.setCancelled(true)

            event.getView().setCursor(null)
            player.setItemOnCursor(null)

            SoulGemData combinedData = new SoulGemData((soulGemDataCursor.getSouls() * currentItemAmount) + (soulGemDataClicked.getSouls() * cursorAmount))
            combinedData.write(currentItem)
            currentItem.setAmount(1)
            SoulUtils.updateSoulCount(currentItem, combinedData.getSouls())

            player.updateInventory()
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, (float) (0.5F + (ThreadLocalRandom.current().nextFloat() / 2F)))
        }

        Events.subscribe(EntityDamageByEntityEvent.class).filter(EventFilters.<EntityDamageByEntityEvent> ignoreCancelled()).handler({ event ->
            Entity attacker = EnchantUtils.getLiableDamager(event)
            if ((!(attacker instanceof Player)) || !(event.getEntity() instanceof LivingEntity)) return


            if (event.getDamager() instanceof Arrow) {
                // TODO bow handling
            }

            Player player = (Player) attacker
            UUID uuid = player.getUniqueId()
            if (silencedPlayers.containsKey(uuid)) {
                long time = silencedPlayers.get(uuid)
                if (System.currentTimeMillis() > time) silencedPlayers.remove(uuid)
                else return
            }

            if (!isInPvpZone(player)) return

            if (event.getEntity() instanceof Player) {
                if (!isInPvpZone(event.getEntity() as Player)) return
            }

            forEquippedEnchants(player, { ItemStack itemStack, CustomEnchantment enchantment, int enchantLevel ->
                enchantment.onAttack(player, itemStack, enchantLevel, event.getEntity() as LivingEntity, event)
            })
        })

        //removes recent damagers on log off and unprocs enchants
        Events.subscribe(PlayerQuitEvent.class).handler { event ->
            Player player = event.player
            silencedPlayers.remove(player.getUniqueId())
            recentDamagers.remove(player.getUniqueId())
            invalidateEquippedEnchants(player)
        }

//        Events.subscribe(BossDamageEvent.class).filter(EventFilters.<BossDamageEvent> ignoreCancelled()).handler({ event ->
//            Entity attacker = scripts.enchant.util.EnchantUtils.getLiableDamager(event)
//            if ((!(attacker instanceof Player)) || !(event.getEntity() instanceof LivingEntity)) return
//
//            if (event.getDamager() instanceof Arrow) {
//                // TODO bow handling
//            }
//
//            Player player = (Player) attacker
//            forEquippedEnchants(player, { ItemStack itemStack, scripts.enchant.Enchantment enchantment, int enchantLevel ->
//                enchantment.onAttack(player, itemStack, enchantLevel, event.getEntity() as LivingEntity, event)
//            })
//        })

        Events.subscribe(EntityDamageByEntityEvent.class).filter(EventFilters.<EntityDamageByEntityEvent> ignoreCancelled()).handler({ event ->
            Entity entity = event.getEntity()
            if (!(entity instanceof Player)) return

            Player player = (Player) entity
            UUID uuid = player.getUniqueId()
            if (silencedPlayers.containsKey(uuid)) {
                long time = silencedPlayers.get(uuid)
                if (System.currentTimeMillis() > time) silencedPlayers.remove(uuid)
                else return
            }
            Entity attacker = EnchantUtils.getLiableDamager(event)

            if (!isInPvpZone(player)) {
                player.sendMessage("no pvp zone")
                return
            }

            if (attacker instanceof Player) {
                if (!isInPvpZone(attacker)) {
                    player.sendMessage("no pvp zone - attacker")
                    return
                }
            }

            recentDamagers.computeIfAbsent(event.getEntity().getUniqueId(), v -> new ConcurrentHashMap<>()).put(attacker.getUniqueId(), System.currentTimeMillis())

            forEquippedEnchants(player, { ItemStack itemStack, CustomEnchantment enchantment, int enchantLevel ->
                if (!event.isCancelled()) enchantment.onDamaged(player, itemStack, enchantLevel, attacker, event as EntityDamageByEntityEvent)
            })
        })

        Events.subscribe(EntityDeathEvent.class).filter(EventFilters.<EntityDeathEvent> ignoreCancelled()).handler { event ->
            EntityDamageEvent lastDamageCause = event.getEntity().getLastDamageCause()
            if (lastDamageCause == null) return

            Entity lastDamager = EnchantUtils.getLiableDamager(lastDamageCause)
            if (!(lastDamager instanceof Player)) return

            Player player = lastDamager as Player
            if (isInPvpZone(player)) {
                forEquippedEnchants(player, { ItemStack itemStack, CustomEnchantment enchantment, int enchantLevel ->
                    if (!event.isCancelled()) enchantment.onKill(player, itemStack, enchantLevel, event.getEntity(), event)
                })
            }
        }

        Events.subscribe(EntityDamageEvent.class).filter(EventFilters.<EntityDamageEvent> ignoreCancelled()).handler({ event ->
            Entity entity = event.getEntity()
            if (!(entity instanceof Player)) return

            Player player = (Player) entity
            if (isInPvpZone(player)) { // TODO: silence here?
                forEquippedEnchants(player, { ItemStack itemStack, CustomEnchantment enchantment, int enchantLevel ->
                    if (!event.isCancelled()) enchantment.onEnvironmentDamaged(player, itemStack, enchantLevel, event.getCause(), event)
                })
            }
        })

        Events.subscribe(ProjectileLaunchEvent.class).filter(EventFilters.<ProjectileLaunchEvent> ignoreCancelled()).handler({ event ->
            Projectile projectile = event.getEntity()
            ProjectileSource shooter = projectile.getShooter()
            if (!(shooter instanceof Player)) return

            Player player = shooter as Player
            if (isInPvpZone(player)) {
                forEquippedEnchants(player, { ItemStack itemStack, CustomEnchantment enchantment, int enchantLevel ->
                    if (!event.isCancelled()) enchantment.onProjectileLaunch(player, itemStack, enchantLevel, projectile, event)
                })
            }
        })

        Events.subscribe(EntityShootBowEvent.class).filter(EventFilters.<EntityShootBowEvent> ignoreCancelled()).handler({ event ->
            LivingEntity shooter = event.getEntity()
            if (!(shooter instanceof Player)) return

            Projectile arrow = event.getProjectile() as Projectile
            Player player = shooter as Player

            if (isInPvpZone(player)) {
                forEquippedEnchants(player, { ItemStack itemStack, CustomEnchantment enchantment, int enchantLevel ->
                    if (!event.isCancelled()) enchantment.onBowShoot(player, itemStack, enchantLevel, arrow, event)
                })
            }
        })

        Events.subscribe(BlockDamageEvent.class, EventPriority.MONITOR).filter(EventFilters.<BlockDamageEvent> ignoreCancelled()).handler { event ->
            Player player = event.player
            if (player.gameMode == GameMode.CREATIVE) return

            Block block = event.getBlock()
            forEquippedEnchants(player, { ItemStack itemStack, CustomEnchantment enchantment, int enchantLevel ->
                if (!event.isCancelled()) enchantment.onBlockDamage(player, itemStack, enchantLevel, block, event)
            })
        }

        Events.subscribe(PlayerArmorChangeEvent.class).handler({
            Player player = it.player
            ItemStack oldItem = it.oldItem
            ItemStack newItem = it.newItem

            invalidateEquippedEnchants(player)

            if (areItemsSimilar(oldItem, newItem)) return

            if (oldItem != null) {
                getEnchantments(oldItem).each { it.key.onUnequip(player, oldItem, it.value) }
            }

            if (newItem != null) {
                getEnchantments(newItem).each {
                    it.key.onEquip(player, newItem, it.value)
                }
            }
        })

        //for held enchants || hotbar slot change
        Events.subscribe(PlayerItemHeldEvent.class).handler({
            Player player = it.player
            ItemStack oldItem = player.getInventory().getItem(it.getPreviousSlot())
            ItemStack newItem = player.getInventory().getItem(it.getNewSlot())

            if (areItemsSimilar(oldItem, newItem)) return

            invalidateEquippedEnchants(player)

            if (oldItem != null) {
                if (ItemType.getTypeOf(oldItem)?.isHoldable()) {
                    getEnchantments(oldItem).each { it.key.onUnequip(player, oldItem, it.value) }
                }
            }

            if (newItem != null) {
                if (ItemType.getTypeOf(newItem)?.isHoldable()) {
                    getEnchantments(newItem).each { it.key.onEquip(player, newItem, it.value) }
                }
            }
        })

        //for held enchants || inventory click
        Events.subscribe(PlayerInventorySlotChangeEvent.class).handler { event ->
            Player player = event.player
            ItemStack oldItem = event.oldItemStack
            ItemStack newItem = event.newItemStack

            if (areItemsSimilar(oldItem, newItem)) return

            invalidateEquippedEnchants(player)

            if (oldItem != null) {
                if (ItemType.getTypeOf(oldItem)?.isHoldable()) {
                    getEnchantments(oldItem).each { it.key.onUnequip(player, oldItem, it.value) }
                }
            }

            if (newItem != null) {
                if (event.slot != player.getInventory().heldItemSlot) return
                if (ItemType.getTypeOf(newItem)?.isHoldable()) {
                    getEnchantments(newItem).each { it.key.onEquip(player, newItem, it.value) }
                }
            }
        }

        Events.subscribe(BlockBreakEvent.class, EventPriority.MONITOR).filter(EventFilters.<BlockBreakEvent> ignoreCancelled()).handler { event ->
            Player player = event.player
            if (player.gameMode == GameMode.CREATIVE || event.getClass().getSimpleName() == "BlockBreakWithDropsEvent") return

            Block block = event.getBlock()
            forEquippedEnchants(player, { ItemStack itemStack, CustomEnchantment enchantment, int enchantLevel ->
                if (!event.isCancelled()) enchantment.onBlockBreakMonitor(player, itemStack, enchantLevel, block, event)
            })
        }

        Events.subscribe(BlockBreakEvent.class, EventPriority.MONITOR).filter(EventFilters.<BlockBreakEvent> ignoreCancelled()).handler { event ->
            Player player = event.player
            if (player.gameMode == GameMode.CREATIVE || event.getClass().getSimpleName() == "BlockBreakWithDropsEvent") return

            Block block = event.getBlock()
            forEquippedEnchants(player, { ItemStack itemStack, CustomEnchantment enchantment, int enchantLevel ->
                if (!event.isCancelled()) enchantment.onBlockBreakMonitor(player, itemStack, enchantLevel, block, event)
            })
        }

//        Events.subscribe(EntityKnockbackByEntityEvent.class).filter(EventFilters.ignoreCancelled()).handler({ event ->
//            if (event.getHitBy() instanceof Player) {
//                Player player = event.getHitBy() as Player
//                forEquippedEnchants(player, { ItemStack itemStack, scripts.enchant.Enchantment enchantment, int enchantLevel ->
//                    enchantment.onKnockback(player, itemStack, enchantLevel, event.getEntity(), event)
//                })
//            }
//
//            if (event.getEntity() instanceof Player) {
//                Player player = event.getEntity() as Player
//                forEquippedEnchants(player, { ItemStack itemStack, scripts.enchant.Enchantment enchantment, int enchantLevel ->
//                    enchantment.onKnockedback(player, itemStack, enchantLevel, event.getHitBy(), event)
//                })
//            }
//        })
    }


    static Long addSilencedPlayer(Player player, int level) {
        long timeAdded = level * 1000
        return silencedPlayers.putIfAbsent(player.getUniqueId(), System.currentTimeMillis() + timeAdded)
    }

    static Long addSilencedPlayer(Player player, Long time) {
        return silencedPlayers.putIfAbsent(player.getUniqueId(), System.currentTimeMillis() + time)
    }

    static boolean areItemsSimilar(ItemStack stack1, ItemStack stack2) {
        if ((stack1 == null && stack2 != null) || (stack2 == null && stack1 != null)) return false
        if (stack1 == stack2) return true

        return stack1.getType() == stack2.getType() && stack1.hasItemMeta() == stack2.hasItemMeta() && getEnchantments(stack1) == getEnchantments(stack2)
    }

    static int getRecentDamagerCount(Player player, long withinMillis) {
        Map<UUID, Long> damagers = recentDamagers.get(player.getUniqueId())
        if (damagers == null) {
            return 0
        }

        long now = System.currentTimeMillis()
        return (int) damagers.entrySet().stream().filter(entry -> now - entry.getValue() <= withinMillis).count()
    }

    static List<UUID> getRecentDamagers(Player player, long withinMillis, int limit) {
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
        return config == null ? config = Exports.ptr("enchantconfig") as Map<String, ?> : config
    }

    static boolean isInPvpZone(Player player) {
        def query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
        def location = BukkitAdapter.adapt(player.location)

        def regions = query.getApplicableRegions(location)
        if (regions.size() == 0) return true

        return query.testState(location, WorldGuardPlugin.inst().wrapPlayer(player), Flags.PVP)
    }
}

