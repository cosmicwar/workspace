package scripts.factions.patches

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.potion.PotionEffectType


// -------------------------------------------- //
// This class is used to represent the
// configuration of patches, enabled, disabled,
// and the values which are used.
// -------------------------------------------- //
class Patches {

    static boolean miscDisableBukkitStatusMessages = true
    static boolean miscDisableBukkitAchievementMessages = true
    static boolean miscAutoUnlockAllPlayerRecipes = true
    static boolean miscLimitFireSpread = true
    static boolean miscLimitWeatherChanges = true
    static boolean miscLimitSilverfishBurrowing = true
    static boolean miscWaterproofBlazes = true
    static boolean miscLimitEvadeMobActions = true
    static boolean miscLimitHostileMobActions = true
    static boolean miscLimitHostileMobBlockActions = true
    static boolean miscLimitHostileMobExplosiveActions = true
    static boolean miscLimitMobBlockFormActions = true
    static boolean miscPlayerAutoRespawn = true
    static long miscPlayerAutoRespawnDelay = 40

    static boolean miscSponge = true
    static int miscSpongeRadius = 3
    static boolean miscSpongeHandleWaterlogged = true
    static boolean miscSpongeHandleWaterFoliage = true

    static boolean miscVehicleLimiter = true
    static String miscVehicleLimiterMsgDeny = "&cThis vehicle type is currently disabled, sorry for the inconvenience!"
    static List<EntityType> miscVehicleLimiterTypes = [
            EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA, EntityType.TRADER_LLAMA,
            EntityType.MULE, EntityType.PIG, EntityType.MINECART, EntityType.MINECART_CHEST,
            EntityType.MINECART_COMMAND, EntityType.MINECART_FURNACE, EntityType.MINECART_HOPPER,
            EntityType.MINECART_MOB_SPAWNER, EntityType.MINECART_TNT
    ]

    static boolean miscContainerLimiter = true
    static String miscContainerLimiterMsgDeny = "&cStoring these contents within this container type is currently disabled, sorry for the inconvenience!"
    static Map<InventoryType, List<Material>> miscContainerLimiterTypes = new HashMap() {
        {
            put(InventoryType.HOPPER, [Material.SPAWNER])
            put(InventoryType.ENDER_CHEST, [Material.SPAWNER])
            put(InventoryType.SHULKER_BOX, [
                    Material.SPAWNER, Material.SHULKER_BOX, Material.BLACK_SHULKER_BOX,
                    Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX,
                    Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX,
                    Material.LIGHT_GRAY_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX,
                    Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
                    Material.RED_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX])
            put(InventoryType.FURNACE, [Material.SPAWNER])
            put(InventoryType.BLAST_FURNACE, [Material.SPAWNER])
            put(InventoryType.DISPENSER, [Material.SPAWNER])
            put(InventoryType.DROPPER, [Material.SPAWNER])
            put(InventoryType.SMOKER, [Material.SPAWNER])
        }
    }

    static boolean miscSpawnerPickupTax = false
    static String miscSpawnerPickupTaxMsgNotOwnFactionLand = "&cYou cannot modify spawners in faction land you don't have accesss to!"
    static String miscSpawnerPickupTaxMsgInsufficientFunds = "&cYou do not have enough money to mine this spawner! You require {requiredCost} to mine this spawner."
    static String miscSpawnerPickupTaxMsgPickup = "&7You have &asuccessfully&7 mined a spawner for &f{pickupCost}&7."
//    static Map<EntityType, SpawnerTax> miscSpawnerPickupTaxTypes = MUtil.map(EntityType.PIG, new SpawnerTax(10000.0d, 25.0d), new Object[]{EntityType.COW, new SpawnerTax(10000.0d, 25.0d), EntityType.CREEPER, new SpawnerTax(10000.0d, 25.0d)});

    static boolean miscLimitItemPlacement = true
    static List<Material> miscLimitItemPlacementTypes = [
            Material.MINECART, Material.CHEST_MINECART, Material.COMMAND_BLOCK_MINECART, Material.FURNACE_MINECART,
            Material.HOPPER_MINECART, Material.TNT_MINECART, Material.RAIL, Material.ACTIVATOR_RAIL,
            Material.DETECTOR_RAIL, Material.POWERED_RAIL, Material.ARMOR_STAND, Material.BLACK_BED,
            Material.BLUE_BED, Material.BROWN_BED, Material.CYAN_BED, Material.GRAY_BED, Material.GREEN_BED,
            Material.LIGHT_BLUE_BED, Material.LIGHT_GRAY_BED, Material.LIME_BED, Material.MAGENTA_BED,
            Material.ORANGE_BED, Material.PINK_BED, Material.PURPLE_BED, Material.RED_BED, Material.WHITE_BED,
            Material.YELLOW_BED, Material.CONDUIT, Material.RESPAWN_ANCHOR, Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM, Material.POTTED_BROWN_MUSHROOM, Material.POTTED_RED_MUSHROOM
    ]
    static String miscLimitItemPlacementMsgDeny = "&cPlacement of this block is currently disabled, sorry for the inconvenience!"

    static boolean miscLimitCraftingRecipes = true
    static List<Material> miscLimitCraftingRecipesTypes = [
            Material.MINECART, Material.CHEST_MINECART, Material.COMMAND_BLOCK_MINECART, Material.FURNACE_MINECART,
            Material.HOPPER_MINECART, Material.TNT_MINECART, Material.RAIL, Material.ACTIVATOR_RAIL,
            Material.DETECTOR_RAIL, Material.POWERED_RAIL, Material.HOPPER, Material.BEACON,
            Material.WRITABLE_BOOK, Material.WRITTEN_BOOK, Material.ENDER_CHEST, Material.COMPASS,
            Material.MAP, Material.ENDER_EYE, Material.FILLED_MAP, Material.GOLDEN_APPLE,
            Material.ENCHANTED_GOLDEN_APPLE, Material.BLACK_BED, Material.BLUE_BED,
            Material.BROWN_BED, Material.CYAN_BED, Material.GRAY_BED, Material.GREEN_BED,
            Material.LIGHT_BLUE_BED, Material.LIGHT_GRAY_BED, Material.LIME_BED, Material.MAGENTA_BED,
            Material.ORANGE_BED, Material.PINK_BED, Material.PURPLE_BED, Material.RED_BED,
            Material.WHITE_BED, Material.YELLOW_BED, Material.CONDUIT, Material.RESPAWN_ANCHOR,
            Material.FIREWORK_ROCKET, Material.FIREWORK_STAR
    ]
    static String miscLimitCraftingMsgFullInvDeny = "&cCrafting of this item could not be complete, as your inventory is full!"
    static String miscLimitCraftingMsgRecipesDeny = "&cCrafting of this item is currently disabled, sorry for the inconvenience!"

    static boolean miscLimitDispenserItems = true
    static List<Material> miscLimitDispenserItemsTypes = [
            Material.MINECART, Material.CHEST_MINECART, Material.COMMAND_BLOCK_MINECART, Material.FURNACE_MINECART,
            Material.HOPPER_MINECART, Material.TNT_MINECART, Material.RAIL, Material.ACTIVATOR_RAIL,
            Material.DETECTOR_RAIL, Material.POWERED_RAIL, Material.POTION, Material.LINGERING_POTION,
            Material.SPLASH_POTION, Material.ACACIA_BOAT, Material.BIRCH_BOAT, Material.DARK_OAK_BOAT,
            Material.JUNGLE_BOAT, Material.OAK_BOAT, Material.SPRUCE_BOAT, Material.GOLDEN_APPLE,
            Material.ENCHANTED_GOLDEN_APPLE, Material.ARMOR_STAND
    ]

    static boolean miscAntiPVPLootStealer = true
    static int miscAntiPVPLootStealerMsgProtectionDuration = 15
    static String miscAntiPVPLootStealerMsgProtectionDrop = "&7Your kills dropped items are being protected for &f{duration}&7."
    static String miscAntiPVPLootStealerMsgProtectionProtected = "&cYou must wait {duration} before you can pick up this loot!"

    static boolean miscPreventTNTPlayerDamage = true
    static boolean miscPreventArrowBounce = true

    static boolean miscKillNotification = true
    static String miscKillNotificationMsgVictim = "&fYou &7were killed by &c{killer} &6[&e{killerHealth}{heart}&6]"
    static String miscKillNotificationMsgKiller = "&fYou &7killed &c{victim}."

    static boolean miscBowHitNotification = true
    static String miscBowHitMsgNotification = "&f{victim}&7 is now at &c{victimHealth}{heart}"

    static boolean miscRevertCombatDelay = true
    static double miscRevertCombatDelayAttackSpeed = 40.0d
    static int miscRevertCombatAttackFrequencyPlayer = 18
    static int miscRevertCombatAttackFrequencyMob = 16

    static boolean miscRevertArmorDurability = true
    static int miscRevertArmorDurabilityBaseReduction = 1

    static boolean miscShieldDamageReductionModifier = false
    static int miscShieldDamageReductionGeneralAmount = 1
    static int miscShieldDamageReductionGeneralPercentage = 50
    static int miscShieldDamageReductionProjectileAmount = 1
    static int miscShieldDamageReductionProjectilePercentage = 50

    static boolean miscPlayerRegenModifier = true
    static long miscPlayerRegenInterval = 3990
    static int miscPlayerRegenHealAmount = 1
    static double miscPlayerRegenExhaustionAmount = 3.0d

    static boolean miscLimitBowBoosting = true

    static boolean miscKnockbackModifier = true
    static boolean miscKnockbackNetheriteResistance = false
    static double miscKnockbackFriction = 2.0d
    static double miscKnockbackHorizontal = 0.35d
    static double miscKnockbackVertical = 0.35d
    static double miscKnockbackVerticalLimit = 0.4d
    static double miscKnockbackExtraHorizontal = 0.425d
    static double miscKnockbackExtraVertical = 0.085d

    static boolean miscGoldenAppleCooldown = true
    static int miscGoldenAppleCooldownLengthSeconds = 15
    static int miscGoldenSuperAppleCooldownLengthSeconds = 60
    static String miscGoldenAppleCooldownMsgDeny = "&cWait {duration} before eating your next golden apple!"
    static String miscGoldenSuperAppleCooldownMsgDeny = "&cWait {duration} before eating your next super golden apple!"

    static boolean miscEnderpearlCooldown = true
    static boolean miscEnderpearlCooldownItemVisual = false
    static int miscEnderpearlCooldownLength = 15
    static int miscEnderpearlDoorCooldownLength = 2
    static String miscEnderpearlCooldownMsgDeny = "&cWait {duration} before throwing your next enderpearl!"
    static String miscEnderpearlCooldownMsgDenyNonLand = "&cWait for your first enderpearl to land, before throwing your next!"

    static boolean miscEnchantmentTableAutoLapis = true
    static boolean miscEnchantmentTypeLimiter = true
    static List<String> miscEnchantmentTypeLimiterTypes = [Enchantment.SWEEPING_EDGE.getName()]

    static boolean miscLimitOffHandItems = false
    static List<Material> miscLimitOffHandItemsTypes = [Material.PLAYER_HEAD]
    static String miscLimitOffHandItemsMsgDeny = "&cThis offhand item is currently disabled, sorry for the inconvenience!"

    static boolean exploitCropFoliageDuplication = false

    static boolean exploitLimitAnvilActions = true
    static String exploitLimitAnvilActionsMsgDropItemDeny = "&cDropping items while within a anvil is currently disabled, sorry for the inconvenience!"
    static String exploitLimitAnvilActionsMsgPickupItemDeny = "&cPicking up items while within a anvil is currently disabled, sorry for the inconvenience!"
    static String exploitLimitAnvilActionsMsgShiftClickDeny = "&cShift clicking items while within a anvil is currently disabled, sorry for the inconvenience!"
    static String exploitLimitAnvilActionsMsgNumberClickDeny = "&cNumber clicking items while within a anvil is currently disabled, sorry for the inconvenience!"

    static boolean exploitLimitPortalCreation = false
    static boolean exploitLimitPortalEntityEntry = true
    static boolean exploitPlayerStuckPortal = true
    static int exploitPlayerStuckPortalCheckDelay = 10
    static int exploitPlayerStuckPortalNotifyDelaySeconds = 5
    static String exploitPlayerStuckPortalMsgAlert = "&cIf you are stuck in a portal stay logged in for another &4{duration} &cand you will be teleported to spawn!"
    static String exploitPlayerStuckPortalMsgNotify = "&cIf you are stuck in a nether portal, re-log to be sent to spawn! If you are not stuck ignore this message."

    static boolean exploitRespawnAnchorLimitations = true
    static String exploitRespawnAnchorLimitationsMsgDeny = "&cRespawn Anchors are currently disabled, sorry for the inconvenience!"

    static boolean exploitPistonLimitations = true
    static List<Material> exploitPistonLimitationsTypes = [
            Material.COAL_ORE, Material.COPPER_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE,
            Material.GOLD_ORE, Material.IRON_ORE, Material.LAPIS_ORE, Material.REDSTONE_ORE,
            Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_LAPIS_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.NETHER_GOLD_ORE,
            Material.NETHER_QUARTZ_ORE, Material.GLOWSTONE, Material.ICE, Material.BLUE_ICE,
            Material.FROSTED_ICE, Material.PACKED_ICE, Material.SLIME_BLOCK, Material.HONEY_BLOCK,
            Material.HONEYCOMB, Material.HONEYCOMB_BLOCK, Material.PUMPKIN, Material.MELON,
            Material.SPAWNER, Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST,
            Material.HOPPER, Material.NOTE_BLOCK, Material.COCOA, Material.SUGAR_CANE
    ]

    static boolean exploitExplosiveAltitudeLimitations = false
    static boolean exploitWaterLoggedExplosionCorrection = false
    static int exploitWaterLoggedExplosionCorrectionRadius = 5

    static boolean exploitFallingBlockNonFullBlockCorrection = true
    static boolean exploitFallingBlockNonFullBlockCorrectionIgnoreNonWaterLogged = true
    static boolean exploitFallingBlockWebLimitations = true

    static boolean exploitEnderpearlLimitations = true
    static String exploitEnderpearlLimitationsMsgWithinBlockDeny = "&cEnderpearls are disabled whilst within a block."
    static String exploitEnderpearlLimitationsMsgDeny = "&cEnderpearl teleportation cancelled. Target location appears to be invalid!"

    static boolean exploitMovementLimitations = true
    static boolean exploitSpawnEggLimitations = true

    static boolean glitchLimitFactionBeaconEffects = true
    static boolean glitchEssentialsHome = true
    static String glitchEssentialsHomeMsgDeny = "&cIt appears this home is in faction territory you no longer own, deleting home!"

    static boolean glitchFactionsOfflineTimer = true
    static int glitchFactionsOfflineTimerLengthSeconds = 300
    static boolean glitchFactionsOfflineTimerTruce = true
    static boolean glitchFactionsOfflineTimerAlly = true
    static boolean glitchFactionsOfflineTimerEnemy = true
    static boolean glitchFactionsOfflineTimerNeutral = true
    static String glitchFactionsOfflineTimerMsgDeny = "&cIt appears you have exceeded the maximum offline time in foreign faction land, sending you to spawn!"

    static boolean glitchEssentialsBackLimitations = true
    static List<String> glitchEssentialsBackCommandExact = [
            "back", "return"
    ]
    static List<String> glitchEssentialsBackCommandIgnoreArgs = [
            "back", "eback", "return", "ereturn"
    ]
    static List<String> glitchEssentialsBackCommandStartsWith = [
            "back", "eback", "return", "ereturn", "essentials:back",
            "essentials:eback", "essentials:return", "essentials:ereturn"
    ]
    static String glitchEssentialsBackMsgDeny = "&cIt appears your previous location is in foreign faction territory you cannot teleport back to!"

    static boolean limitBukkitSpawnerMobCollision = true
    static boolean limitBukkitSpawnReason = true
    static boolean limitBukkitSpawnReasonEntityOverride = false
    static List<EntityType> limitBukkitSpawnReasonEntityOverrideTypes = [
            EntityType.PHANTOM,
            EntityType.WITHER_SKELETON
    ]
    static List<CreatureSpawnEvent.SpawnReason> limitBukkitSpawnReasonTypes = [
            CreatureSpawnEvent.SpawnReason.BEEHIVE, CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM,
            CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN, CreatureSpawnEvent.SpawnReason.COMMAND,
            CreatureSpawnEvent.SpawnReason.CURED, CreatureSpawnEvent.SpawnReason.CUSTOM,
            CreatureSpawnEvent.SpawnReason.DEFAULT, CreatureSpawnEvent.SpawnReason.DISPENSE_EGG,
            CreatureSpawnEvent.SpawnReason.DROWNED, CreatureSpawnEvent.SpawnReason.EGG,
            CreatureSpawnEvent.SpawnReason.EGG, CreatureSpawnEvent.SpawnReason.ENDER_PEARL,
            CreatureSpawnEvent.SpawnReason.EXPLOSION, CreatureSpawnEvent.SpawnReason.FROZEN,
            CreatureSpawnEvent.SpawnReason.INFECTION, CreatureSpawnEvent.SpawnReason.JOCKEY,
            CreatureSpawnEvent.SpawnReason.LIGHTNING, CreatureSpawnEvent.SpawnReason.MOUNT,
            CreatureSpawnEvent.SpawnReason.NETHER_PORTAL, CreatureSpawnEvent.SpawnReason.OCELOT_BABY,
            CreatureSpawnEvent.SpawnReason.PATROL, CreatureSpawnEvent.SpawnReason.PIGLIN_ZOMBIFIED,
            CreatureSpawnEvent.SpawnReason.RAID, CreatureSpawnEvent.SpawnReason.SHEARED,
            CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY, CreatureSpawnEvent.SpawnReason.SILVERFISH_BLOCK,
            CreatureSpawnEvent.SpawnReason.SLIME_SPLIT, CreatureSpawnEvent.SpawnReason.SPAWNER,
            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG, CreatureSpawnEvent.SpawnReason.TRAP,
            CreatureSpawnEvent.SpawnReason.VILLAGE_DEFENSE, CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION,
            CreatureSpawnEvent.SpawnReason.BUILD_WITHER
    ]

    static boolean modifyBukkitEntitySpawnHealth = true
    static List<CreatureSpawnEvent.SpawnReason> modifyBukkitEntitySpawnHealthReasons = [
            CreatureSpawnEvent.SpawnReason.SPAWNER, CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
    ]
    static Map<EntityType, Double> modifyBukkitEntitySpawnHealthTypes =  new HashMap() {
        {
            put(EntityType.CREEPER, 5.0D)
        }
    }

    static boolean limitBukkitItemEntityDamage = true
    static List<EntityDamageEvent.DamageCause> limitBukkitItemEntityDamageReasons = [EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, EntityDamageEvent.DamageCause.CONTACT, EntityDamageEvent.DamageCause.DRAGON_BREATH, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, EntityDamageEvent.DamageCause.FIRE, EntityDamageEvent.DamageCause.FIRE_TICK, EntityDamageEvent.DamageCause.HOT_FLOOR, EntityDamageEvent.DamageCause.LAVA, EntityDamageEvent.DamageCause.LIGHTNING, EntityDamageEvent.DamageCause.MELTING]
    static List<Material> limitBukkitItemEntityDamageTypes = [Material.SPAWNER, Material.BEACON, Material.HOPPER, Material.DRAGON_EGG, Material.NETHER_STAR, Material.GOLDEN_APPLE]
    static boolean limitPlayerNetherHeight = true
    static int limitPlayerNetherHeightYLevel = 126
    static String limitPlayerNetherHeightMsgDeny = "&cAction cancelled due to the destination being above the nether roof!"
    static boolean limitInventoryGhostItems = true
    static boolean limitInventoryOnTeleport = true
    static List<PlayerTeleportEvent.TeleportCause> limitInventoryOnTeleportReasons = [
            PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT, PlayerTeleportEvent.TeleportCause.COMMAND,
            PlayerTeleportEvent.TeleportCause.END_GATEWAY, PlayerTeleportEvent.TeleportCause.END_PORTAL,
            PlayerTeleportEvent.TeleportCause.NETHER_PORTAL, PlayerTeleportEvent.TeleportCause.PLUGIN,
            PlayerTeleportEvent.TeleportCause.SPECTATE
    ]
    static boolean limitInventorySanitize = true
    static boolean limitInventoryHopperIntake = true
    static List<Material> limitInventoryHopperIntakeTypes = [
            Material.SPAWNER, Material.SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, Material.LIME_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX
    ]
    static boolean limitPlayerInvalidHealth = true
    static boolean limitRedstoneActivity = true
    static int limitRedstoneActivityThreshold = 5000
    static List<Material> limitRedstoneMaterialTypes = [
            Material.REDSTONE_BLOCK, Material.DAYLIGHT_DETECTOR, Material.OBSERVER, Material.SCULK_SENSOR,
            Material.REDSTONE_WIRE, Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH,
            Material.COMPARATOR, Material.DROPPER, Material.PISTON, Material.STICKY_PISTON,
            Material.MOVING_PISTON, Material.LEVER
    ]
    static boolean limitPlayerTileChunkPlacement = true
    static boolean limitPlayerTileItemFrameChunkPlacement = true
    static boolean limitPlayerTileArmorStandChunkPlacement = true
    static boolean limitPlayerTileBoatChunkPlacement = true
    static boolean limitPlayerTileSignChunkPlacement = true
    static boolean limitPlayerTileSpawnerChunkPlacement = false
    static int limitPlayerTileItemFrameThreshold = 50
    static int limitPlayerTileArmorStandThreshold = 35
    static int limitPlayerTileBoatThreshold = 10
    static int limitPlayerTileSignThreshold = 100
    static int limitPlayerTileSpawnerThreshold = 500
    static String limitPlayerItemFrameMsgDenyPlacement = "&cThe maximum chunk limit for item frames of {limitCount} has been reached, sorry for the inconvenience!"
    static String limitPlayerArmorStandMsgDenyPlacement = "&cThe maximum chunk limit for armor stands of {limitCount} has been reached, sorry for the inconvenience!"
    static String limitPlayerBoatMsgDenyPlacement = "&cThe maximum chunk limit for boats of {limitCount} has been reached, sorry for the inconvenience!"
    static String limitPlayerSignMsgDenyPlacement = "&cThe maximum chunk limit for signs of {limitCount} has been reached, sorry for the inconvenience!"
    static String limitPlayerSpawnerMsgDenyPlacement = "&cThe maximum chunk limit for spawners of {limitCount} has been reached, sorry for the inconvenience!"

    static boolean limitPlayerTeleport = true
    static List<PlayerTeleportEvent.TeleportCause> limitPlayerTeleportTypes = [
            PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
    ]
    static String limitPlayerTeleportMsgDenied = "&cThis teleportation type is currently disabled, sorry for the inconvenience!"

    static boolean limitPlayerConsumable = true
    static List<Material> limitPlayerConsumableTypes = [
            Material.CHORUS_FRUIT, Material.ENCHANTED_GOLDEN_APPLE
    ]
    static String limitPlayerConsumableMsgDenied = "&cThis consumable type is currently disabled, sorry for the inconvenience!"

    static boolean limitPlayerVoidMovement = true
    static int limitPlayerVoidYLevel = -64
    static String limitPlayerVoidEssentialsSpawn = "default"

    static boolean limitPlayerDuplicateLogin = true
    static boolean limitPlayerAltAccounts = true
    static int limitPlayerAltLimitCount = 10
    static String limitPlayerAltLimitCountMsgReached = "&cYou already have the maximum amount of alts allowed on this realm!"

    static boolean filterPlayerCommands = true
    static boolean filterPlayerTabCommands = true
    static String filterPlayerCommandsMsgDisabled = "&cCommand {command} is currently disabled, sorry for the inconvenience!"
    static List<String> disabledCommandsTab = [
            "/plugins", "/bukkit:plugins", "/pl", "/bukkit:pl",
            "/icanhasbukkit", "/ver", "/bukkit:ver", "/version",
            "/bukkit:version", "/?", "/bukkit:?", "/a", "/about",
            "/bukkit:about", "/help", "/bukkit:help", "/me", "/kill",
            "/massivecore click", "/mcore click", "/minecraft:tell",
            "/mv", "/multiversecore:mv", "/massivecore click /minecraft:tell"]
    static List<String> disabledCommandsStartsWith = [
            "we update", "fawe update", "fastasyncworldedit update", "hd readtext",
            "holographicdisplays readtext", "holographicdisplays:hd readtext",
            "holographicdisplays:holographicdisplays readtext", "ban Abeoji",
            "ban Kezi", "mv", "/mv", "multiversecore:mv", "/multiversecore:mv", ":"
    ]
    static List<String> disabledCommandsExact = [
            "tp Abeoji",
            "god Abeoji"
    ]
    static List<String> disabledCommandsIgnoreArgs = [
            ":", "pl", "plugins", "bukkit", "epl", "end", "server", "eplugins", "bukkit:help", "/?", "?", "calc", "about",
            "ver", "icanhasbukkit", "version", "bukkit:pl", "bukkit:plugins", "bukkit:?", "eabout", "bukkit:about", "bukkit:ver",
            "calculate", "/calculate", "eval", "/eval", "evaluate", "/evaluate", "/worldedit:/eval", "worldedit:/eval", "solve",
            "/solve", "/calc", "stop", "restart", "eop", "essentials:op", "essentials:eop", "edeop", "superpickaxe", "/superpickaxe",
            "sp", "/sp", "essentials:deop", "essentials:edeop", "icanhasbukkit ?", "me", "minecraft:me", "minecraft:op", "minecraft:pl",
            "minecraft:deop", "minecraft:?", "minecraft:tp", "minecraft:defaultgamemode", "minecraft:execute", "e:help", "essentials:help",
            "essentials:ehelp", "minecraft:summon", "minecraft:ban", "minecraft:help", "evolve", "/evolve", "minecraft:give", "minecraft",
            "minecraft:", "we", "worldedit", "a", "kill", "plugman", "plugman list", "/sel", "/desel", "/deselect", "searchitem",
            "/searchitem", "/search", "/l", "toggleplace", "/toggleplace", "we update", "fawe update", "fastasyncworldedit update"
            , "hd readtext", "holographicdisplays readtext", "holographicdisplays:hd readtext", "holographicdisplays:holographicdisplays readtext",
            "targetoffset", "/targetoffset", "to", "/to", "brush", "/brush", ":", "targetmask", "/targetmask", "none", "/none", "secondary", "/secondary",
            "visualize", "/visualize", "target", "/target", "patterns", "/patterns", "masks", "/masks", "mask", "/mask", "mv", "/mv",
            "multiversecore:mv", "/multiversecore:mv", "transforms", "/transforms"
    ]

}

