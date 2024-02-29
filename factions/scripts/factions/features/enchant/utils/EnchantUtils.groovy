package scripts.factions.features.enchant.utils

import org.starcade.starlight.enviorment.Exports
import groovy.transform.CompileStatic
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.projectiles.ProjectileSource
import scripts.factions.core.faction.Factions
import scripts.factions.core.faction.data.Member
import scripts.factions.core.faction.data.relation.RelationType
import scripts.factions.features.enchant.data.item.ItemEnchantmentData
import com.google.common.collect.Sets
import scripts.shared.utils.ItemType

@CompileStatic
class EnchantUtils {

    static boolean isArmor(ItemStack itemStack) {
        if (itemStack == null) return false
        String itemName = itemStack.type.name()

        return itemName.endsWith("_HELMET") || itemName.endsWith("_CHESTPLATE") || itemName.endsWith("_LEGGINGS") || itemName.endsWith("_BOOTS")
    }

    static boolean isSword(ItemStack itemStack) {
        if (itemStack == null) return false

        return ItemType.getTypeOf(itemStack) == ItemType.SWORD
    }

    static void scaleDamage(EntityDamageEvent event, double factor) {
        EntityDamageEvent.DamageModifier.values().findAll { event.isApplicable(it) }.each {
            event.setDamage(it, factor * event.getDamage(it))
        }
    }

    static int getMaxSlots(ItemStack stack) {
        ItemEnchantmentData itemEnchantmentData = ItemEnchantmentData.read(stack)
        if (itemEnchantmentData == null) return 0

        int maxSlots = Exports.ptr("enchantconfig")["customItems"]["enchantmentOrb"]["maxSlots"] as int

        return maxSlots
    }

    static Entity getLiableDamager(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent)) return null

        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event
        Entity damager = entityDamageByEntityEvent.getDamager()
        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager
            ProjectileSource projectileSource = projectile.getShooter()
            if (projectileSource instanceof Entity) damager = (Entity) projectileSource
        }

        return damager
    }

    static void pushAway(Location center, Entity entity, double multiplier) {
        entity.setVelocity(entity.getLocation().add(0D, 0.1D, 0D).toVector().subtract(center.toVector()).normalize().multiply(multiplier))
    }

    static boolean isComplex(ItemStack stack) {
        if (!stack.hasItemMeta()) return false

        ItemMeta meta = stack.getItemMeta()
        return meta.hasDisplayName() || meta.hasLore()
    }

    static BlockFace getMiningDirection(Player player) {
        float yaw = player.getLocation().getYaw()
        if (yaw < 0) {
            yaw += 360
        }
        if (yaw < 45 || yaw >= 315) {
            return BlockFace.SOUTH
        } else if (yaw < 135) {
            return BlockFace.WEST
        } else if (yaw < 225) {
            return BlockFace.NORTH
        } else if (yaw < 315) {
            return BlockFace.EAST
        }
    }

    static ChatColor getGlowColor(Material material) {
        switch (material) {
            case Material.LAPIS_ORE:
            case Material.LAPIS_LAZULI:
            case Material.LAPIS_BLOCK:
                return ChatColor.BLUE
            case Material.GOLD_ORE:
            case Material.GOLD_INGOT:
            case Material.GOLD_BLOCK:
                return ChatColor.GOLD
            case Material.IRON_ORE:
            case Material.IRON_INGOT:
            case Material.IRON_BLOCK:
                return ChatColor.GRAY
            case Material.REDSTONE_ORE:
            case Material.REDSTONE:
            case Material.REDSTONE_BLOCK:
                return ChatColor.RED
            case Material.DIAMOND_ORE:
            case Material.DIAMOND:
            case Material.DIAMOND_BLOCK:
                return ChatColor.AQUA
            case Material.EMERALD_ORE:
            case Material.EMERALD:
            case Material.EMERALD_BLOCK:
                return ChatColor.GREEN
            case Material.NETHERITE_BLOCK:
                return ChatColor.DARK_PURPLE
            default:
                return ChatColor.BLACK
        }
    }

    static Set<Player> getNearbyFriendlyPlayers(Player player, double range, boolean includeSelf) {
        Set<Player> nearbyFriendlies = Sets.newHashSet()
        if (player == null) return nearbyFriendlies

        Member member = Factions.getMember(player.getUniqueId(), false)
        if (member == null) return nearbyFriendlies

        getNearbyPlayers(player, range, includeSelf).forEach(otherPlayer -> {
            Member otherMember = Factions.getMember(otherPlayer.getUniqueId(), false)
            if (otherMember == null) return

            def rel = Factions.getRelationType(member, otherMember)
            if (rel == null || rel != RelationType.ALLY || rel != RelationType.MEMBER) {
                return
            }

            nearbyFriendlies.add(otherPlayer)
        });

        return nearbyFriendlies
    }

    static Set<Player> getNearbyEnemyPlayers(Player player, double range, boolean includeSelf) {
        if (player == null) return Collections.emptySet()

        return getNearbyEnemyPlayers(player, player.getLocation(), range, includeSelf)
    }

    static Set<Player> getNearbyEnemyPlayers(Player player, Location source, double range, boolean includeSelf) {
        Set<Player> nearbyEnemies = Sets.newHashSet()
        if (player == null) return nearbyEnemies

        Member member = Factions.getMember(player.getUniqueId(), true)
        if (member == null) return nearbyEnemies

        getNearbyPlayers(player, source, range, includeSelf).forEach(otherPlayer -> {
            Member otherMember = Factions.getMember(otherPlayer.getUniqueId())
            if (otherMember == null) return
            def rel = Factions.getRelationType(member, otherMember)
            if (rel.isAtMost(RelationType.NEUTRAL)) nearbyEnemies.add(otherPlayer)
        })

        return nearbyEnemies
    }

    static Set<Player> getNearbyPlayers(Player player, double range, boolean includeSelf) {
        return getNearbyPlayers(player, player.getLocation(), range, includeSelf)
    }

    static Set<Player> getNearbyPlayers(Player player, Location source, double range, boolean includeSelf) {
        Set<Player> nearbyPlayers = Sets.newHashSet()

        player.getNearbyEntities(range, range, range).forEach(entity -> {
            if (!(entity instanceof Player)) return

            Player otherPlayer = (Player) entity

            if (otherPlayer.getGameMode() != GameMode.SURVIVAL) return
            if (!player.canSee(otherPlayer)) return

            nearbyPlayers.add((Player) entity)
        })

        if (includeSelf) nearbyPlayers.add(player)

        return nearbyPlayers
    }

    static void heal(LivingEntity livingEntity, double healAmount) {
        livingEntity.setHealth(Math.min(livingEntity.getHealth() + healAmount, livingEntity.getMaxHealth()))
    }

    static int getItemDamage(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().getMaxDurability() == (short) 0) return 0

        ItemMeta itemMeta = itemStack.getItemMeta()
        return ((Damageable)itemMeta).getDamage()
    }

    static List<LivingEntity> getNearbyMobTargets(Location origin, double radiusX, double radiusY = radiusX, double radiusZ = radiusY) {
        return origin.getWorld().getNearbyLivingEntities(origin, radiusX, radiusY, radiusZ)
                .findAll { !(it instanceof Player) /*&& !(((CraftEntity) it).getHandle() instanceof NametagSpacerSlime)*/ /*&& !(((CraftEntity) it).getHandle() instanceof NametagArmorStand)*/ } as List<LivingEntity>
    }

}
