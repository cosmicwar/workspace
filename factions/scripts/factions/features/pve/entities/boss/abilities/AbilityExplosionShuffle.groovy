package scripts.factions.features.pve.entities.boss.abilities

import net.minecraft.world.entity.Entity
import org.bukkit.Location
import org.bukkit.entity.Player
import scripts.factions.features.pve.entities.boss.Ability

import java.util.stream.Collectors

class AbilityExplosionShuffle extends Ability {

    static final int MAX_SHUFFLES = 10
    static final int DELAY_BETWEEN_SHUFFLES = 10

    int delay = 0
    double originalX, originalY, originalZ
    List<Player> targets = new ArrayList<>()

    AbilityExplosionShuffle(Entity entity) {
        super(entity)
    }

    @Override
    void start() {
        originalX = entity.getX()
        originalY = entity.getY()
        originalZ = entity.getZ()

        targets.addAll(entity.bukkitEntity.location.getNearbyEntitiesByType(Player.class, 32, 8,32).stream().limit(MAX_SHUFFLES).collect(Collectors.toList()))
        Collections.shuffle(targets)
    }

    @Override
    void tick() {
        targets.removeIf({
            return !it.isOnline() || it.world != entity.level().getWorld()
        })

        if (++delay < DELAY_BETWEEN_SHUFFLES || targets.isEmpty() || !entity.isAlive()) return

        delay = 0
        Player target = targets.remove(0)

        Location loc = target.getLocation()
        entity.moveTo(loc.x, loc.y, loc.z)
        entity.bukkitEntity.world.createExplosion(entity.bukkitEntity, 4F, false, false)
    }

    @Override
    void complete() {
        targets.clear()
        if (!entity.isAlive()) return

        entity.moveTo(originalX, originalY, originalZ)
        entity.bukkitEntity.world.createExplosion(entity.bukkitEntity, 4F, false, false)
    }

    @Override
    boolean isFinished() {
        return targets.isEmpty() && delay == DELAY_BETWEEN_SHUFFLES
    }

}
