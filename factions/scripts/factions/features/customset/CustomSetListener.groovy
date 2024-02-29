package scripts.factions.features.customset

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.projectiles.ProjectileSource
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import scripts.factions.features.customset.data.CustomSetItemData
import scripts.factions.features.customset.struct.CustomSet
import scripts.factions.features.enchant.utils.EnchantUtils

import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

class CustomSetListener {

    Closure getSet = Exports.ptr("customset:getSet") as Closure
    Closure getSetById = Exports.ptr("customset:getSetById") as Closure
    Closure forEquippedSet = Exports.ptr("customset:forEquippedSet") as Closure
    Closure invalidateEquippedSet = Exports.ptr("customset:invalidateEquippedSet") as Closure
    Closure setEquippedSet = Exports.ptr("customset:setEquippedSet") as Closure

    Map<UUID, Map<UUID, Long>> recentDamagers = new ConcurrentHashMap<>()

    CustomSetListener() {
        addEvents()
    }

    void addEvents() {
        Events.subscribe(EntityDamageByEntityEvent.class).filter(EventFilters.<EntityDamageByEntityEvent> ignoreCancelled()).handler({ event ->
            Entity attacker = EnchantUtils.getLiableDamager(event)
            if ((!(attacker instanceof Player)) || !(event.getEntity() instanceof LivingEntity)) return

            if (event.getDamager() instanceof Arrow) {
                // TODO bow handling
            }

            Player player = (Player) attacker
            forEquippedSet(player, { CustomSet set ->
                set.onAttack(player, event.getEntity() as LivingEntity, event)
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
            forEquippedSet(player, { CustomSet set ->
                set.onDamaged(player, attacker, event)
            })
        })

        Events.subscribe(EntityDeathEvent.class).filter(EventFilters.<EntityDeathEvent> ignoreCancelled()).handler { event ->
            EntityDamageEvent lastDamageCause = event.getEntity().getLastDamageCause()
            if (lastDamageCause == null) return

            Entity lastDamager = EnchantUtils.getLiableDamager(lastDamageCause)
            if (!(lastDamager instanceof Player)) return

            Player player = lastDamager as Player
            forEquippedSet(player, { CustomSet set ->
                set.onKill(player, event.getEntity(), event)
            })
        }

        Events.subscribe(EntityDamageEvent.class).filter(EventFilters.<EntityDamageEvent> ignoreCancelled()).handler({ event ->
            Entity entity = event.getEntity()
            if (!(entity instanceof Player)) return

            Player player = (Player) entity
            forEquippedSet(player, { CustomSet set ->
                set.onEnvironmentDamaged(player, event.getCause(), event)
            })
        })

        Events.subscribe(ProjectileLaunchEvent.class).filter(EventFilters.<ProjectileLaunchEvent> ignoreCancelled()).handler({ event ->
            Projectile projectile = event.getEntity()
            ProjectileSource shooter = projectile.getShooter()
            if (!(shooter instanceof Player)) return

            Player player = shooter as Player
            forEquippedSet(player, { CustomSet set ->
                set.onProjectileLaunch(player, projectile, event)
            })
        })

        Events.subscribe(EntityShootBowEvent.class).filter(EventFilters.<EntityShootBowEvent> ignoreCancelled()).handler({ event ->
            LivingEntity shooter = event.getEntity()
            if (!(shooter instanceof Player)) return

            Projectile arrow = event.getProjectile() as Projectile
            Player player = shooter as Player

            forEquippedSet(player, { CustomSet set ->
                set.onBowShoot(player, arrow, event)
            })
        })

        Events.subscribe(BlockDamageEvent.class, EventPriority.MONITOR).filter(EventFilters.<BlockDamageEvent> ignoreCancelled()).handler { event ->
            Player player = event.player
            if (player.gameMode == GameMode.CREATIVE) return

            Block block = event.getBlock()
            forEquippedSet(player, { CustomSet set ->
                set.onBlockDamage(player, block, event)
            })
        }

        Events.subscribe(PlayerArmorChangeEvent.class).handler({
            Player player = it.player
            ItemStack oldItem = it.oldItem
            ItemStack newItem = it.newItem

            CustomSet set = (getSet.call(player) ?: null) as CustomSet
            def data = CustomSetItemData.read(newItem)

            if (set != null && (newItem == null || newItem.getType() == Material.AIR)) {
                setEquippedSet.call(player, null)
                set?.onUnequip(player)
                return
            }

            if (set != null && data != null) {
                if (areItemsSimilar(oldItem, newItem)) return

                Bukkit.broadcastMessage("removing set")

                setEquippedSet.call(player, null)
                set.onUnequip(player)
            } else if (set == null) {
                CustomSetItemData customSetData = CustomSetItemData.read(newItem)
                if (customSetData == null) return

                if (customSetData.getSetName() == "omni") {
                    customSetData = Arrays.stream(player.getEquipment().getArmorContents()).map(CustomSetItemData::read).filter(Objects::nonNull).filter(omni -> omni.getSetName() != "omni" && getSetById.call(omni.getSetName()) != null).findFirst().orElse(null)
                    if (customSetData == null) return
                }

                List<String> equippedPieces = Arrays.stream(player.getEquipment().getArmorContents()).map(CustomSetItemData::read).filter(Objects::nonNull).map(CustomSetItemData::getSetName).filter { it != "none" }.collect(Collectors.toList())
                if (equippedPieces.size() != 4 || !equippedPieces.stream().allMatch(equippedPiece -> (equippedPiece == customSetData.getSetName() || equippedPiece == "omni") && equippedPiece != "none")) {
                    return null
                }

                CustomSet newEquippedSet = (getSetById.call(customSetData.getSetName()) ?: null) as CustomSet
                if (newEquippedSet == null) return

                setEquippedSet.call(player, newEquippedSet)
                newEquippedSet.onEquip(player)
            }
        })

        // crystals - fire after first event above
        Events.subscribe(PlayerArmorChangeEvent.class, EventPriority.HIGH).handler({
            Player player = it.player
            ItemStack oldItem = it.oldItem
            ItemStack newItem = it.newItem

            if (areItemsSimilar(oldItem, newItem)) return

            CustomSetItemData oldData = CustomSetItemData.read(oldItem)
            if (oldData != null) {
                if (oldData.crystals) {
                    oldData.getCrystalSetNames().each { crystalSet ->
                        CustomSet set = getSetById.call(crystalSet) as CustomSet
                        if (set != null) {
                            set.onCrystalUnequip(player)
                        }
                    }
                }
            }

            CustomSetItemData newData = CustomSetItemData.read(newItem)
            if (newData != null) {
                if (newData.crystals) {
                    newData.getCrystalSetNames().each { crystalSet ->
                        CustomSet set = getSetById.call(crystalSet) as CustomSet
                        if (set != null) {
                            set.onCrystalEquip(player)
                        }
                    }
                }
            }
        })

        Events.subscribe(BlockBreakEvent.class, EventPriority.MONITOR).filter(EventFilters.<BlockBreakEvent> ignoreCancelled()).handler { event ->
            Player player = event.player
            if (player.gameMode == GameMode.CREATIVE || event.getClass().getSimpleName() == "BlockBreakWithDropsEvent") return

            Block block = event.getBlock()
            forEquippedSet(player, { CustomSet set ->
                set.onBlockBreak(player, block, event)
            })
        }
    }

    boolean areItemsSimilar(ItemStack stack1, ItemStack stack2) {
        if ((stack1 == null && stack2 != null) || (stack2 == null && stack1 != null)) return false
        if (stack1 == stack2) return true

        CustomSet set1 = getSetFromPiece(stack1)
        CustomSet set2 = getSetFromPiece(stack2)

        if (set1 == null || set2 == null) return false

        return stack1.getType() == stack2.getType() && stack1.hasItemMeta() == stack2.hasItemMeta() && set1 == set2
    }

    CustomSet getSetFromPiece(ItemStack stack) {
        CustomSetItemData data = CustomSetItemData.read(stack)
        if (data == null) return null

        return getSetById.call(data.getSetName()) as CustomSet
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

    static ItemStack getNonOmniPiece(Player player) {
        ItemStack nonOmni = null

        player.getInventory().armorContents.each { stack ->
            if (stack != null) {
                CustomSetItemData data = CustomSetItemData.read(stack)
                if (data == null) return null

                if (data.getSetName() != "omni" && data.setName != "none") {
                    nonOmni = stack
                }
            }

        }

        return nonOmni
    }

}