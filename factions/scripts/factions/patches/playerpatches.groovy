package scripts.factions.patches

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.starcade.starlight.helper.utils.Players
import groovy.transform.Field
import io.netty.util.internal.ThreadLocalRandom
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerVelocityEvent
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffectType
import org.bukkit.projectiles.ProjectileSource
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Member
import scripts.shared.utils.BukkitUtils
import scripts.shared.utils.DurationUtils
import scripts.shared.utils.Formats
import scripts.shared.utils.MathUtils
import scripts.shared.utils.PlayerUtils

import javax.annotation.Nonnull
import java.text.DecimalFormat
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import java.util.stream.Stream

@Field DecimalFormat format = new DecimalFormat("##.##")
@Field final Map<UUID, Vector> playerKnockbackHashMap = new WeakHashMap()
@Field final Map<UUID, List<ItemStack>> explosionDamaged = new WeakHashMap()
@Field final Map<UUID, List<ItemStack>> fullyBlocked = new WeakHashMap()
@Field final Map<UUID, Long> healTimes = new WeakHashMap()
@Field static final String ITEM_META_NAME = "miscellaneous-antiloot"

//throws ExecutionException, InterruptedException
Events.subscribe(AsyncPlayerPreLoginEvent.class, EventPriority.MONITOR).handler { event ->
    if (Patches.limitPlayerDuplicateLogin) {
        final Player player = Bukkit.getPlayer(event.getUniqueId())
        if (player != null) {
            Bukkit.getScheduler().callSyncMethod(Starlight.plugin, new Callable<Object>() {
                Object call() throws Exception {
                    player.kickPlayer("You logged in from another location")
                    return null
                }
            }).get()
        }

    }
}

//Events.subscribe(AsyncPlayerPreLoginEvent.class, EventPriority.HIGHEST).filter(EventFilters.<AsyncPlayerPreLoginEvent> ignoreCancelled()).handler { event ->
//    if (Patches.limitPlayerAltAccounts) {
//        String ipAddress = event.getAddress().getHostAddress()
//        int matches = 0
//        Iterator var5 = Bukkit.getOnlinePlayers().iterator()
//
//        while (var5.hasNext()) {
//            Player onlinePlayer = (Player) var5.next()
//            if (onlinePlayer.getAddress().getAddress().getHostAddress() == ipAddress) {
//                ++matches
//            }
//
//            if (matches >= Patches.limitPlayerAltLimitCount) {
//                break
//            }
//        }
//
//        if (matches >= Patches.limitPlayerAltLimitCount) {
//            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageUtils.colorHexMessage(Patches.limitPlayerAltLimitCountMsgReached))
//            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER)
//            event.setKickMessage(MessageUtils.colorHexMessage(Patches.limitPlayerAltLimitCountMsgReached))
//        }
//
//    }
//}
Events.subscribe(PlayerJoinEvent.class, EventPriority.HIGHEST).handler { event ->
    if (Patches.miscDisableBukkitStatusMessages) {
        event.setJoinMessage("")
    }

    final Player player = event.getPlayer()
    if (Patches.miscRevertCombatDelay) {
        PlayerUtils.setAttackFrequencyDelay(player, Patches.miscRevertCombatAttackFrequencyPlayer)
        PlayerUtils.setAttackSpeed(player, Patches.miscRevertCombatDelayAttackSpeed)
    }

    if (Patches.miscAutoUnlockAllPlayerRecipes) {
        Iterator<Recipe> it = Starlight.plugin.getServer().recipeIterator()

        while (it.hasNext()) {
            Recipe rec = (Recipe) it.next()
            if (rec instanceof Keyed) {
                player.discoverRecipe(((Keyed) rec).getKey())
            }
        }
    }

//    if (Patches.exploitPlayerStuckPortal) {
//        (new TaskPortalStuck(player)).runTaskTimer(Starlight.plugin, 0L, 20L)
//    }

    if (Patches.limitPlayerInvalidHealth) {
        if (player.getHealth() == Double.NaN || Double.isInfinite(player.getHealth())) {
            player.setHealth(0.0)
        }

        if (player.getHealth() == 0.0) {
            Bukkit.getScheduler().runTaskLater(Starlight.plugin, new Runnable() {
                void run() {
                    player.spigot().respawn()
                }
            }, Patches.miscPlayerAutoRespawnDelay)
        }
    }
}
Events.subscribe(PlayerQuitEvent.class, EventPriority.HIGHEST).handler { event ->
    if (Patches.miscDisableBukkitStatusMessages) {
        event.setQuitMessage("")
    }

    Player player = event.getPlayer()
    if (Patches.miscRevertCombatDelay) {
        PlayerUtils.setAttackFrequencyDelay(player, Patches.miscRevertCombatAttackFrequencyPlayer)
        PlayerUtils.setAttackSpeed(player, Patches.miscRevertCombatDelayAttackSpeed)
    }

    if (Patches.miscKnockbackModifier) {
        this.playerKnockbackHashMap.remove(player.getUniqueId())
    }

    if (Patches.miscPlayerRegenModifier) {
        this.healTimes.remove(player.getUniqueId())
    }

//    if (Patches.exploitLimitAnvilActions && anvilUsagePlayers.contains(player.getUniqueId())) {
//        anvilUsagePlayers.remove(player.getUniqueId())
//    }

    if (Patches.limitInventorySanitize && event.getPlayer().isInsideVehicle()) {
        PlayerUtils.closeViewPassengers(event.getPlayer().getVehicle())
    }
}
Events.subscribe(PlayerChangedWorldEvent.class, EventPriority.HIGHEST).handler { event ->
    if (Patches.miscRevertCombatDelay) {
        PlayerUtils.setAttackFrequencyDelay(event.getPlayer(), Patches.miscRevertCombatAttackFrequencyPlayer)
        PlayerUtils.setAttackSpeed(event.getPlayer(), Patches.miscRevertCombatDelayAttackSpeed)
    }
}
Events.subscribe(PlayerRespawnEvent.class, EventPriority.HIGHEST).handler { event ->
    if (Patches.miscRevertCombatDelay) {
        PlayerUtils.setAttackFrequencyDelay(event.getPlayer(), Patches.miscRevertCombatAttackFrequencyPlayer)
    }
}
Events.subscribe(PlayerDeathEvent.class, EventPriority.HIGHEST).filter(EventFilters.<PlayerDeathEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscDisableBukkitStatusMessages) {
        event.setDeathMessage("")
    }

    if (Patches.miscPlayerAutoRespawn) {
        if (!(event.getEntity() instanceof Player)) {
            return
        }

        final Player player = event.getEntity()
        Bukkit.getScheduler().runTaskLater(Starlight.plugin, new Runnable() {
            void run() {
                player.spigot().respawn()
            }
        }, Patches.miscPlayerAutoRespawnDelay)
    }
}
Events.subscribe(PlayerTeleportEvent.class, EventPriority.HIGHEST).filter(EventFilters.<PlayerTeleportEvent> ignoreCancelled()).handler { event ->
    if (Patches.limitPlayerTeleport) {
        Player player = event.getPlayer()
        boolean isCitizensNPC = player.hasMetadata("NPC")
        if (!isCitizensNPC && !player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
            if (Patches.limitPlayerTeleportTypes.contains(event.getCause())) {
                event.setCancelled(true)
                Players.msg(player, Patches.limitPlayerTeleportMsgDenied)
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
            }

        }
    }
}
Events.subscribe(PlayerItemConsumeEvent.class, EventPriority.HIGH).filter(EventFilters.<PlayerItemConsumeEvent> ignoreCancelled()).handler { event ->
    if (event.getItem() != null && event.getItem().getType() != Material.AIR) {
        if (Patches.limitPlayerConsumable) {
            Player player = event.getPlayer()
            if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                if (Patches.limitPlayerConsumableTypes.contains(event.getItem().getType())) {
                    event.setCancelled(true)
                    player.updateInventory()
                    Players.msg(player, Patches.limitPlayerConsumableMsgDenied)
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                }

            }
        }
    }
}
//Events.subscribe(PlayerMoveEvent.class, EventPriority.HIGH).filter(EventFilters.<PlayerMoveEvent> ignoreCancelled()).handler { event ->
//    if (!MovementUtils.isPlayerSameChunk(event.getFrom(), event.getTo())) {
//        if (Patches.limitPlayerVoidMovement) {
//            Player player = event.getPlayer()
//            if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
//                if (player.getLocation().getBlockY() <= Patches.limitPlayerVoidYLevel) {
//                    PlayerUtils.teleportPlayer(player, this.essentialsSpawn.getSpawn(Patches.limitPlayerVoidEssentialsSpawn))
//                }
//
//            }
//        }
//    }
//}
//Events.subscribe(EntityDamageEvent.class, EventPriority.HIGH).filter(EventFilters.<EntityDamageEvent> ignoreCancelled()).handler { event ->
//    if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
//        Entity entity = event.getEntity()
//        if (entity != null) {
//            if (entity instanceof Player) {
//                if (Patches.limitPlayerVoidMovement) {
//                    Player player = ((Player) entity).getPlayer()
//                    if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
//                        event.setCancelled(true)
//                        PlayerUtils.teleportPlayer(player, this.essentialsSpawn.getSpawn(Patches.limitPlayerVoidEssentialsSpawn))
//                    }
//                }
//            }
//        }
//    }
//}
Events.subscribe(PlayerCommandPreprocessEvent.class, EventPriority.MONITOR).filter(EventFilters.<PlayerCommandPreprocessEvent> ignoreCancelled()).handler { event ->
    if (event.getPlayer() != null && !event.getPlayer().isOp() && !event.getPlayer().hasPermission("miscellaneous.bypass")) {
        if (Patches.filterPlayerCommands) {
            String fullCommand = event.getMessage().substring(1).toLowerCase()
            if (restrictedCommand(fullCommand)) {
                event.setCancelled(true)
                Players.msg(event.getPlayer(), Patches.filterPlayerCommandsMsgDisabled.replace("{command}", fullCommand.toString()))
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
            }

        }
    }
}
Events.subscribe(PlayerCommandPreprocessEvent.class, EventPriority.LOWEST).filter(EventFilters.<PlayerCommandPreprocessEvent> ignoreCancelled()).handler { event ->
    if (event.getPlayer() != null && !event.getPlayer().isOp() && !event.getPlayer().hasPermission("miscellaneous.bypass")) {
        if (Patches.filterPlayerCommands) {
            String fullCommand = event.getMessage().substring(1).toLowerCase()
            if (fullCommand.toLowerCase().startsWith("massivecore click ")) {
                event.setMessage(fullCommand.substring("massivecore click ".length()))
            }

            if (fullCommand.toLowerCase().startsWith("mcore click ")) {
                event.setMessage(fullCommand.substring("mcore click ".length()))
            }

        }
    }
}
Events.subscribe(AsyncTabCompleteEvent.class, EventPriority.NORMAL).filter(EventFilters.<AsyncTabCompleteEvent> ignoreCancelled()).handler { event ->
    if (event.getSender() != null && !event.getSender().isOp() && !event.getSender().hasPermission("miscellaneous.bypass")) {
        if (Patches.filterPlayerTabCommands) {
            String command = event.getBuffer().toLowerCase()
            Patches.disabledCommandsTab.each { disableMessage ->
                if (command.startsWith(disableMessage) && !command.contains("  ")) {
                    event.setCancelled(true)
                }
            }
        }
    }
}

static boolean restrictedCommand(@Nonnull String command) {
    Stream var10000 = Patches.disabledCommandsStartsWith.stream()
    command.getClass()
    if (var10000.anyMatch(command::startsWith)) {
        return true
    } else {
        String cmdNoArgs = command
        if (command.contains(" ")) {
            if (Patches.disabledCommandsExact.contains(command)) {
                return true
            }

            cmdNoArgs = command.split(" ")[0]
        }

        return Patches.disabledCommandsIgnoreArgs.contains(cmdNoArgs)
    }
}

Events.subscribe(PlayerVelocityEvent.class, EventPriority.LOWEST).filter(EventFilters.<PlayerVelocityEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscKnockbackModifier) {
        UUID uuid = event.getPlayer().getUniqueId()
        if (this.playerKnockbackHashMap.containsKey(uuid)) {
            event.setVelocity((Vector) this.playerKnockbackHashMap.get(uuid))
            this.playerKnockbackHashMap.remove(uuid)
        }
    }
}
Events.subscribe(EntityDamageEvent.class, EventPriority.NORMAL).filter(EventFilters.<EntityDamageEvent> ignoreCancelled()).handler { event ->
    if (event.getEntity() instanceof Player) {
        if (Patches.miscKnockbackModifier) {
            if (!Patches.miscKnockbackNetheriteResistance) {
                AttributeInstance attribute = ((Player) event.getEntity()).getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)
                attribute.getModifiers().forEach(attribute::removeModifier)
            }
        }
    }
}
Events.subscribe(EntityDamageByEntityEvent.class, EventPriority.MONITOR).filter(EventFilters.<EntityDamageByEntityEvent> ignoreCancelled()).handler { event ->
    if (event.getDamager() instanceof LivingEntity) {
        if (event.getEntity() instanceof Player) {
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                if (!(event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) > 0.0)) {
                    if (Patches.miscKnockbackModifier) {
                        Player victim = (Player) event.getEntity()
                        LivingEntity attacker = (LivingEntity) event.getDamager()
                        Location attackerLocation = attacker.getLocation()
                        Location victimLocation = victim.getLocation()
                        double d0 = attackerLocation.getX() - victimLocation.getX()

                        double d1
                        for (d1 = attackerLocation.getZ() - victimLocation.getZ(); d0 * d0 + d1 * d1 < 1.0E-4; d1 = (Math.random() - Math.random()) * 0.01) {
                            d0 = (Math.random() - Math.random()) * 0.01
                        }

                        Vector playerVelocity = victim.getVelocity()
                        double magnitude = Math.sqrt(d0 * d0 + d1 * d1)
                        playerVelocity.setX(playerVelocity.getX() / Patches.miscKnockbackFriction - d0 / magnitude * Patches.miscKnockbackHorizontal)
                        playerVelocity.setY(playerVelocity.getY() / Patches.miscKnockbackFriction + Patches.miscKnockbackVertical)
                        playerVelocity.setZ(playerVelocity.getZ() / Patches.miscKnockbackFriction - d1 / magnitude * Patches.miscKnockbackHorizontal)
                        EntityEquipment equipment = attacker.getEquipment()
                        if (equipment != null) {
                            ItemStack heldItem = equipment.getItemInMainHand().getType() == Material.AIR ? equipment.getItemInOffHand() : equipment.getItemInMainHand()
                            int bonusKnockback = heldItem.getEnchantmentLevel(Enchantment.KNOCKBACK)
                            if (attacker instanceof Player && ((Player) attacker).isSprinting()) {
                                ++bonusKnockback
                            }

                            if (playerVelocity.getY() > Patches.miscKnockbackVerticalLimit) {
                                playerVelocity.setY(Patches.miscKnockbackVerticalLimit)
                            }

                            if (bonusKnockback > 0) {
                                playerVelocity.add(new Vector(-Math.sin((double) (attacker.getLocation().getYaw() * 3.1415927F / 180.0F)) * (double) ((float) bonusKnockback) * Patches.miscKnockbackExtraHorizontal, Patches.miscKnockbackExtraVertical, Math.cos((double) (attacker.getLocation().getYaw() * 3.1415927F / 180.0F)) * (double) ((float) bonusKnockback) * Patches.miscKnockbackExtraHorizontal))
                            }
                        }

                        if (Patches.miscKnockbackNetheriteResistance) {
                            double resistance = 1.0 - victim.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getValue()
                            playerVelocity * new Vector(resistance, 1.0, resistance)
                        }

                        UUID victimId = victim.getUniqueId()
                        this.playerKnockbackHashMap.put(victimId, playerVelocity)
                        Bukkit.getScheduler().runTaskLater(Starlight.plugin, () -> {
                            this.playerKnockbackHashMap.remove(victimId)
                        }, 1L)
                    }
                }
            }
        }
    }
}
Events.subscribe(PlayerItemDamageEvent.class, EventPriority.LOWEST).handler { event ->
    if (event.getDamage() > 0) {
        ItemStack item = event.getItem()
        if (item != null && item.getType() != Material.AIR) {
            if (Patches.miscRevertArmorDurability) {
                Material itemType = item.getType()
                if (!Arrays.stream(event.getPlayer().getInventory().getArmorContents()).noneMatch((armourPiece) -> {
                    return armourPiece != null && armourPiece.getType() == itemType && armourPiece.getType() != Material.ELYTRA
                })) {
                    UUID uuid = event.getPlayer().getUniqueId()
                    if (this.explosionDamaged.containsKey(uuid)) {
                        List<ItemStack> armour = (List) this.explosionDamaged.get(uuid)
                        List<ItemStack> matchedPieces = (List) armour.stream().filter((piece) -> {
                            return piece == item
                        }).collect(Collectors.toList())
                        armour.removeAll(matchedPieces)
                        if (!matchedPieces.isEmpty()) {
                            return
                        }
                    }

                    int reduction = Patches.miscRevertArmorDurabilityBaseReduction
                    int damageChance = (int) (60 + 40 / (item.getEnchantmentLevel(Enchantment.DURABILITY) + 1))
                    ThreadLocalRandom random = ThreadLocalRandom.current()
                    int randomInt = random.nextInt(100)
                    if (randomInt >= damageChance) {
                        reduction = 0
                    }

                    event.setDamage(reduction)
                }
            }
        }
    }
}
Events.subscribe(EntityDamageEvent.class, EventPriority.MONITOR).handler { event ->
    if (!event.isCancelled()) {
        if (event.getEntityType() == EntityType.PLAYER) {
            EntityDamageEvent.DamageCause cause = event.getCause()
            if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                if (Patches.miscRevertArmorDurability) {
                    Player player = (Player) event.getEntity()
                    final UUID uuid = player.getUniqueId()
                    List<ItemStack> armour = (List) Arrays.stream(player.getInventory().getArmorContents()).filter(Objects::nonNull).collect(Collectors.toList())
                    this.explosionDamaged.put(uuid, armour)
                    BukkitRunnable runnable = new BukkitRunnable() {
                        void run() {
                            explosionDamaged.remove(uuid)
                        }
                    }
                    runnable.runTaskLater(Starlight.plugin, 1L)
                }
            }
        }
    }
}
Events.subscribe(PlayerItemDamageEvent.class, EventPriority.LOWEST).handler { event ->
    if (event.getDamage() > 0) {
        ItemStack item = event.getItem()
        if (item != null && item.getType() != Material.AIR) {
            if (item.getType() == Material.SHIELD) {
                if (Patches.miscShieldDamageReductionModifier) {
                    UUID uuid = event.getPlayer().getUniqueId()
                    if (this.fullyBlocked.containsKey(uuid)) {
                        List<ItemStack> armour = (List) this.fullyBlocked.get(uuid)
                        List<ItemStack> matchedPieces = (List) armour.stream().filter((piece) -> {
                            return piece == item
                        }).collect(Collectors.toList())
                        armour.removeAll(matchedPieces)
                        if (!matchedPieces.isEmpty()) {
                            event.setCancelled(true)
                        }
                    }

                }
            }
        }
    }
}
Events.subscribe(EntityDamageByEntityEvent.class, EventPriority.LOWEST).handler { event ->
    if (event.getEntity() instanceof Player) {
        if (Patches.miscShieldDamageReductionModifier) {
            double currentDamage = event.getDamage(EntityDamageEvent.DamageModifier.BASE) + event.getDamage(EntityDamageEvent.DamageModifier.HARD_HAT)
            if (shieldBlockedDamage(currentDamage, event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING))) {
                double damageReduction = getDamageReduction(currentDamage, event.getCause())
                event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, -damageReduction)
                currentDamage -= damageReduction
                Player player = (Player) event.getEntity()
                final UUID uuid = player.getUniqueId()
                if (currentDamage <= 0.0) {
                    List<ItemStack> armour = (List) Arrays.stream(player.getInventory().getArmorContents()).filter(Objects::nonNull).collect(Collectors.toList())
                    this.fullyBlocked.put(uuid, armour)
                    (new BukkitRunnable() {
                        void run() {
                            fullyBlocked.remove(uuid)
                        }
                    }).runTaskLater(Starlight.plugin, 1L)
                }

            }
        }
    }
}

static double getDamageReduction(double damage, @Nonnull EntityDamageEvent.DamageCause damageCause) {
    damage -= (double) (damageCause == EntityDamageEvent.DamageCause.PROJECTILE ? Patches.miscShieldDamageReductionProjectileAmount : Patches.miscShieldDamageReductionGeneralAmount)
    damage *= (double) (damageCause == EntityDamageEvent.DamageCause.PROJECTILE ? Patches.miscShieldDamageReductionProjectilePercentage : Patches.miscShieldDamageReductionGeneralPercentage) / 100.0
    if (damage < 0.0) {
        damage = 0.0
    }

    return damage
}

static boolean shieldBlockedDamage(double attackDamage, double blockingReduction) {
    return attackDamage > 0.0 && blockingReduction < 0.0
}

Events.subscribe(EntityRegainHealthEvent.class, EventPriority.HIGHEST).handler { event ->
    if (!event.isCancelled()) {
        if (event.getEntityType() == EntityType.PLAYER && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            if (Patches.miscPlayerRegenModifier) {
                event.setCancelled(true)
                Player player = (Player) event.getEntity()
                float previousExhaustion = player.getExhaustion()
                float previousSaturation = player.getSaturation()
                UUID playerId = player.getUniqueId()
                long currentTime = System.currentTimeMillis()
                boolean hasLastHealTime = this.healTimes.containsKey(playerId)
                long lastHealTime = (Long) this.healTimes.computeIfAbsent(playerId, (id) -> {
                    return currentTime
                })
                if (hasLastHealTime && currentTime - lastHealTime <= Patches.miscPlayerRegenInterval) {
                    Bukkit.getScheduler().runTaskLater(Starlight.plugin, () -> {
                        player.setExhaustion(previousExhaustion)
                    }, 1L)
                } else {
                    double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
                    double playerHealth = player.getHealth()
                    if (playerHealth < maxHealth) {
                        player.setHealth(MathUtils.clamp(playerHealth + (double) Patches.miscPlayerRegenHealAmount, 0.0, maxHealth))
                        this.healTimes.put(playerId, currentTime)
                    }

                    float exhaustionToApply = (float) Patches.miscPlayerRegenExhaustionAmount
                    Bukkit.getScheduler().runTaskLater(Starlight.plugin, () -> {
                        player.setExhaustion((previousExhaustion + exhaustionToApply) as float)
                    }, 1L)
                }
            }
        }
    }
}
Events.subscribe(EntityDamageByEntityEvent.class, EventPriority.HIGH).filter(EventFilters.<EntityDamageByEntityEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscLimitBowBoosting) {
        Entity damager = event.getDamager()
        if (damager != null) {
            if (damager instanceof Arrow) {
                Arrow arrow = (Arrow) damager
                ProjectileSource shooter = arrow.getShooter()
                if (arrow != null && shooter != null) {
                    if (shooter instanceof Player) {
                        if (event.getEntity() == shooter) {
                            arrow.setKnockbackStrength(0)
                        }
                    }
                }
            }
        }
    }
}
Events.subscribe(PlayerItemConsumeEvent.class, EventPriority.MONITOR).filter(EventFilters.<PlayerItemConsumeEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscGoldenAppleCooldown) {
        Player player = event.getPlayer()
        if (BukkitUtils.hasItemSelected(player, Material.GOLDEN_APPLE) || BukkitUtils.hasItemSelected(player, Material.ENCHANTED_GOLDEN_APPLE)) {
            if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                ItemStack item = event.getItem()
                Member user
                if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
                    if (Patches.miscGoldenSuperAppleCooldownLengthSeconds > 0) {
                        user = Factions.getMember(player.getUniqueId())
                        user.setSuperAppleCooldown(System.currentTimeMillis() + (long) Patches.miscGoldenSuperAppleCooldownLengthSeconds * 1000L)
                    }
                } else if (Patches.miscGoldenAppleCooldownLengthSeconds > 0) {
                    user = Factions.getMember(player.getUniqueId())
                    user.setRegularAppleCooldown(System.currentTimeMillis() + (long) Patches.miscGoldenAppleCooldownLengthSeconds * 1000L)
                }
            }
        }
    }
}
Events.subscribe(PlayerItemConsumeEvent.class, EventPriority.NORMAL).filter(EventFilters.<PlayerItemConsumeEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscGoldenAppleCooldown) {
        Player player = event.getPlayer()
        if (BukkitUtils.hasItemSelected(player, Material.GOLDEN_APPLE) || BukkitUtils.hasItemSelected(player, Material.ENCHANTED_GOLDEN_APPLE)) {
            if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                ItemStack item = event.getItem()
                Member user
                long apple
                if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
                    if (Patches.miscGoldenSuperAppleCooldownLengthSeconds > 0) {
                        user = Factions.getMember(player.getUniqueId())
                        apple = DurationUtils.calculateRemaining(user.getSuperAppleCooldown())
                        if (apple >= 0L) {
                            event.setCancelled(true)
                            player.updateInventory()
                            Players.msg(player, Patches.miscGoldenSuperAppleCooldownMsgDeny.replace("{duration}", Formats.formatTimeMillis(apple)))
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                        }
                    }
                } else if (Patches.miscGoldenAppleCooldownLengthSeconds > 0) {
                    user = Factions.getMember(player.getUniqueId())
                    apple = DurationUtils.calculateRemaining(user.getRegularAppleCooldown())
                    if (apple >= 0L) {
                        event.setCancelled(true)
                        player.updateInventory()
                        Players.msg(player, Patches.miscGoldenAppleCooldownMsgDeny.replace("{duration}", Formats.formatTimeMillis(apple)))
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                    }
                }
            }
        }
    }
}
Events.subscribe(PlayerDeathEvent.class, EventPriority.NORMAL).filter(EventFilters.<PlayerDeathEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscKillNotification) {
        Player victim = event.getEntity()
        Entity killer = victim.getKiller()
        if (victim != null && killer != null) {
            if (!victim.hasMetadata("NPC")) {
                if (victim instanceof Player || killer instanceof Player) {
                    if (!victim.getName().equalsIgnoreCase(killer.getName())) {
                        Player killerPlayer = (Player) killer
                        Players.msg(victim, Patches.miscKillNotificationMsgVictim.replace("{killer}", killerPlayer.getName()).replace("{killerHealth}", this.format.format(killerPlayer.getHealth() / 2.0)).replace("{heart}", "&c❤"))
                        Players.msg(killerPlayer, Patches.miscKillNotificationMsgKiller.replace("{victim}", victim.getName()))
                    }
                }
            }
        }
    }
}
Events.subscribe(EntityDamageByEntityEvent.class, EventPriority.HIGH).filter(EventFilters.<EntityDamageByEntityEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscBowHitNotification) {
        Entity damager = event.getDamager()
        if (damager != null) {
            if (damager instanceof Arrow) {
                Arrow arrow = (Arrow) damager
                ProjectileSource shooter = arrow.getShooter()
                if (arrow != null && shooter != null) {
                    if (shooter instanceof Player) {
                        Player playerDamager = (Player) shooter
                        Damageable playerVictim = (Damageable) event.getEntity()
                        if (playerDamager != null && playerVictim != null) {
                            if (playerVictim instanceof Player) {
                                if (!playerDamager.getName().equalsIgnoreCase(playerVictim.getName())) {
                                    Player victim = (Player) playerVictim
                                    double vh = playerVictim.getHealth()
                                    double damage = event.getFinalDamage()
                                    if (!victim.isDead() && !(damage <= 0.0)) {
                                        double victimHealth = vh - damage
                                        if (victimHealth > 0.0) {
                                            Players.msg(playerDamager, Patches.miscBowHitMsgNotification.replace("{victim}", victim.getName()).replace("{victimHealth}", this.format.format(victimHealth / 2.0)).replace("{heart}", "&c❤"))
                                            playerDamager.playSound(playerDamager.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5.0F, 1.0F)
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
Events.subscribe(EntityDamageByEntityEvent.class, EventPriority.NORMAL).filter(EventFilters.<EntityDamageByEntityEvent> ignoreCancelled()).handler { event ->
    if (Patches.miscPreventTNTPlayerDamage) {
        Entity damager = event.getDamager()
        if (damager != null) {
            if (damager instanceof TNTPrimed) {
                Entity damaged = event.getEntity()
                if (damaged != null) {
                    if (damaged.getType() == EntityType.PLAYER) {
                        event.setCancelled(true)
                        event.setDamage(0.0)
                    }
                }
            }
        }
    }
}
Events.subscribe(EntityPickupItemEvent.class, EventPriority.NORMAL).filter(EventFilters.<EntityPickupItemEvent> ignoreCancelled()).handler { event ->
    if (event.getEntity() instanceof Player) {
        if (Patches.miscAntiPVPLootStealer) {
            Player player = (Player) event.getEntity()
            if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                Item item = event.getItem()
                if (item.hasMetadata("miscellaneous-antiloot")) {
                    UUID playerId = player.getUniqueId()
                    String[] itemData = ((MetadataValue) item.getMetadata("miscellaneous-antiloot").get(0)).asString().split("\\|")
                    long remaining = DurationUtils.calculateRemaining(Long.valueOf(itemData[1]))
                    if (remaining > 0L && playerId.toString() != itemData[0]) {
                        event.setCancelled(true)
                        Member factionUserData = Factions.getMember(playerId)
                        if (DurationUtils.calculateRemaining(factionUserData.getLooterMessageCooldown()) <= 0L) {
                            Players.msg(player, Patches.miscAntiPVPLootStealerMsgProtectionProtected.replace("{duration}", Formats.formatTimeMillis(remaining)))
                            factionUserData.setLooterMessageCooldown(System.currentTimeMillis() + 1000L)
                        }
                    } else {
                        event.getItem().removeMetadata("miscellaneous-antiloot", Starlight.plugin)
                    }
                }
            }
        }
    }
}
Events.subscribe(PlayerDeathEvent.class, EventPriority.NORMAL).handler { event ->
    if (Patches.miscAntiPVPLootStealer) {
        Player victim = event.getEntity()
        if (victim.getKiller() != null) {
            Player killer = victim.getKiller()
            Players.msg(killer, Patches.miscAntiPVPLootStealerMsgProtectionDrop.replace("{duration}", Formats.formatTimeMillis(TimeUnit.SECONDS.toMillis((long) Patches.miscAntiPVPLootStealerMsgProtectionDuration))))
            Iterator var5 = event.getDrops().iterator()

            while (var5.hasNext()) {
                ItemStack item = (ItemStack) var5.next()
                if (item != null && item.getType() != Material.AIR) {
                    Entity newItem = victim.getWorld().dropItemNaturally(victim.getLocation(), item)
                    newItem.setMetadata("miscellaneous-antiloot", new FixedMetadataValue(Starlight.plugin, killer.getUniqueId().toString() + "|" + (System.currentTimeMillis() + TimeUnit.SECONDS.toMillis((long) Patches.miscAntiPVPLootStealerMsgProtectionDuration))))
                }
            }

            event.getDrops().clear()
        }
    }
}
Events.subscribe(CraftItemEvent.class, EventPriority.HIGH).filter(EventFilters.<CraftItemEvent> ignoreCancelled()).handler { event ->
    if (event.getWhoClicked() instanceof Player) {
        if (Patches.miscLimitCraftingRecipes) {
            Player player = (Player) event.getWhoClicked()
            if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                if (player.getInventory().firstEmpty() == -1) {
                    event.setCancelled(true)
                    player.updateInventory()
                    Players.msg(player, Patches.miscLimitCraftingMsgFullInvDeny)
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                } else {
                    ItemStack item = event.getCurrentItem()
                    if (item != null && item.getType() != Material.AIR) {
                        if (Patches.miscLimitCraftingRecipesTypes.contains(item.getType())) {
                            event.setCancelled(true)
                            player.updateInventory()
                            Players.msg(player, Patches.miscLimitCraftingMsgRecipesDeny)
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                        }
                    }
                }
            }
        }
    }
}
Events.subscribe(PlayerSwapHandItemsEvent.class, EventPriority.HIGHEST).handler { event ->
    ItemStack item = event.getOffHandItem()
    if (item != null && item.getType() != Material.AIR) {
        if (Patches.miscLimitOffHandItemsTypes.contains(item.getType())) {
            if (Patches.miscLimitOffHandItems) {
                Player player = event.getPlayer()
                if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                    event.setCancelled(true)
                    player.updateInventory()
                    Players.msg(player, Patches.miscLimitOffHandItemsMsgDeny)
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                }
            }
        }
    }
}
Events.subscribe(InventoryClickEvent.class, EventPriority.HIGHEST).handler { event ->
    if (event.getSlot() == 40 || event.getAction() == InventoryAction.HOTBAR_SWAP && !event.getClick().isKeyboardClick()) {
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            if (Patches.miscLimitOffHandItems) {
                ClickType clickType = event.getClick()
                Inventory clickedInventory = event.getClickedInventory()
                ItemStack item
                if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
                    if (!clickType.isKeyboardClick()) {
                        if (clickedInventory.getItem(event.getSlot()) == null) {
                            return
                        }

                        item = clickedInventory.getItem(event.getSlot())
                    } else {
                        if (clickedInventory.getItem(event.getHotbarButton()) == null) {
                            return
                        }

                        item = clickedInventory.getItem(event.getHotbarButton())
                    }
                } else {
                    item = event.getCursor()
                }

                if (item != null && item.getType() != Material.AIR) {
                    Player player = (Player) event.getWhoClicked()
                    if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                        if (Patches.miscLimitOffHandItemsTypes.contains(item.getType())) {
                            event.setCancelled(true)
                            event.setResult(Event.Result.DENY)
                            player.updateInventory()
                            Players.msg(player, Patches.miscLimitOffHandItemsMsgDeny)
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                        }

                    }
                }
            }
        }
    }
}
Events.subscribe(InventoryDragEvent.class, EventPriority.HIGHEST).handler { event ->
    if (event.getInventory().getType() == InventoryType.CRAFTING && event.getInventorySlots().contains(40)) {
        ItemStack item = event.getOldCursor()
        if (item != null && item.getType() != Material.AIR) {
            if (Patches.miscLimitOffHandItemsTypes.contains(item.getType())) {
                if (Patches.miscLimitOffHandItems) {
                    Player player = (Player) event.getWhoClicked()
                    if (!player.isOp() && !player.hasPermission("miscellaneous.bypass")) {
                        event.setCancelled(true)
                        event.setResult(Event.Result.DENY)
                        player.updateInventory()
                        Players.msg(player, Patches.miscLimitOffHandItemsMsgDeny)
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5.0F, 1.0F)
                    }
                }
            }
        }
    }
}