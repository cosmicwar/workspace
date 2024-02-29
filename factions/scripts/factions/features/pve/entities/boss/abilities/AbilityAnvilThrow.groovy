package scripts.factions.features.pve.entities.boss.abilities

import net.minecraft.world.entity.Entity
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import scripts.factions.features.pve.entities.boss.Ability
import scripts.shared.visuals.floating.FloatingBlock

class AbilityAnvilThrow extends Ability {

    Player target
    Vector direction
    Location currentLocation
    FloatingBlock floatingBlock

    boolean finished = false
    int maxDuration
    int duration

    AbilityAnvilThrow(Entity entity) {
        super(entity)
    }

    @Override
    void start() {
        Location location = entity.bukkitEntity.location
        target = location.getNearbyEntitiesByType(Player.class, 32, 12 ,32).sort {-it.location.distanceSquared(location)}.find()
        if (!target) return

        entity.level().getWorld().playSound(target.location, Sound.BLOCK_ANVIL_USE, 1F, 1F)
        Location origin = entity.bukkitEntity.location.add(0, 2, 0)

        floatingBlock = new FloatingBlock(entity.level().getWorld(), origin, Material.ANVIL)
        floatingBlock.track()

        Location targetLoc = target.location.add(0, 1.5, 0)
        this.direction = targetLoc.clone().subtract(origin).toVector().normalize().multiply(0.8)
        this.currentLocation = origin
        this.maxDuration = (int) (origin.distance(targetLoc) / direction.length()) + 2
    }

    @Override
    void tick() {
        if (target == null || !target.isOnline() || target.world != entity.level().getWorld() || ++duration >= maxDuration) {
            finished = true
            return
        }

        // add a bit of "heat seeking"
        Location targetLoc = target.location.add(0, 1.5, 0)
        Vector newDirection = targetLoc.subtract(currentLocation).toVector().normalize().multiply(0.8)
        direction = direction.midpoint(newDirection)
        currentLocation.add(direction)

        floatingBlock.moveTo(currentLocation.x, currentLocation.y, currentLocation.z)

        if (duration % 2 == 0) {
            entity.level().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, currentLocation, 1)
        }

        // complete early if the target has moved into range
        if (currentLocation.distanceSquared(targetLoc) <= 2) {
            finished = true
        }
    }

    @Override
    void complete() {
        floatingBlock?.untrack()

        currentLocation?.getNearbyEntitiesByType(Player.class, 5, 5, 5)?.each {
            it.damage(15, entity.bukkitEntity)
            it.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1))
            it.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 50, 1))
            it.playSound(it.location, Sound.BLOCK_ANVIL_PLACE, 1F, 1F)
        }
    }

    @Override
    boolean isFinished() {
        return finished
    }

}
