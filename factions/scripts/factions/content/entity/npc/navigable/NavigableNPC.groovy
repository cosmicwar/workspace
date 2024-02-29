package scripts.factions.content.entity.npc.navigable

import groovy.transform.CompileStatic
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.util.Mth
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Silverfish
import org.apache.commons.lang3.reflect.FieldUtils
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.util.Vector
import scripts.factions.content.entity.npc.NPCRegistry
import scripts.factions.content.entity.npc.NPCTracker
import scripts.factions.content.entity.npc.navigable.path.NavigablePath
import scripts.shared.legacy.utils.RandomUtils

import java.lang.reflect.Field

@CompileStatic
abstract class NavigableNPC extends Silverfish {

    static final EntityDimensions ENTITY_SIZE = EntityDimensions.fixed(0.48F, 1.44F) // slightly smaller than a player to avoid some navigation issues
    static final Field SIZE_FIELD = FieldUtils.getField(Entity.class, "size", true)

    final String npcId
    final Location spawnLocation

    NPCTracker npcTracker
    Location npcLocation
    double lastX
    double lastY
    double lastZ
    boolean clearFromChunkTracker = true
    boolean teleported = false
    NavigablePath navigationPath
    Location navigationTarget

    NavigableNPC(String npcId, Location spawnLocation) {
        super(EntityType.SILVERFISH, (spawnLocation.getWorld() as CraftWorld).getHandle())

        this.npcId = npcId
        this.spawnLocation = spawnLocation

        setPosRaw(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ())
        setRot(spawnLocation.getYaw(), spawnLocation.getPitch())
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D)

        activatedTick = Long.MAX_VALUE
        setPersistenceRequired()
        persist = false // do not save with chunk
    }

    void spawn() {
        ((CraftWorld) spawnLocation.getWorld()).getHandle().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        // remove the silverfish from the tracker as it's only used as a navigator
        (level as ServerLevel).getChunkSource().removeEntity(this)

        npcLocation = spawnLocation.clone()
        npcTracker = NPCRegistry.get().spawn(npcId, generateName(), npcLocation, RandomUtils.getRandom(getSkins()))

        setLastPos()
        SIZE_FIELD.set(this, ENTITY_SIZE)
        refreshDimensions();
    }

    abstract String generateName()
    abstract List<String> getSkins()

    @Override
    void tick() {
        if (navigationPath != null && !navigationPath.isComplete()) {
            navigateFixedPath()
        }
        doPathing()

        if (!teleported) {
            goalSelector.tick()
            navigation.tick()

            moveControl.tick()
            lookControl.tick()
            jumpControl.tick()

            super.tick()
        }

        teleported = false

        if (npcLocation) {
            npcLocation.set(getX(), getY(), getZ())
            npcLocation.setYaw(getXRot())
            npcLocation.setPitch(getYRot())
        }

        npcTracker?.moveTo(npcLocation)

        checkStuck()
        setLastPos()
    }

    @Override
    void postTick() {
        super.postTick()

        /*if (clearFromChunkTracker) {
            ((ServerLevel)level).doIfNotEntityTicking({ getCurrentChunk()?.entities?.remove(getId()) })
            clearFromChunkTracker = false
        }*/
    }

    /*@Override
    void setCurrentChunk(LevelChunk chunk) {
        super.setCurrentChunk(chunk)
        if (chunk) {
            clearFromChunkTracker = true
        }
    }*/

    int stuckTicks = 0
    void checkStuck() {
        if (Mth.equal(getX(), lastX) && Mth.equal(getY(), lastY) && Mth.equal(getZ(), lastZ)) {
            stuckTicks++
        } else {
            stuckTicks = 0
        }
    }

    boolean isStuck() {
        return stuckTicks > 10
    }

    void setLastPos() {
        lastX = getX()
        lastY = getY()
        lastZ = getZ()
    }

    void tickNpcTracker() {
        if (!npcTracker || npcTracker.npc.dead || !dead) return

        NPCRegistry.get().unregister(npcTracker)
    }

    void navigateFixedPath() {
        if (navigationTarget != null) return

        int pathIndex = navigationPath.pointIndex
        Location nextPathPoint = navigationPath.getNext(level.world)
        if (nextPathPoint) {
            if (pathIndex > 0 || isLocationInRange(nextPathPoint)) { // only self navigate to the starting point, otherwise tp
                teleportToLocation(nextPathPoint)
            } else {
                startNavigatingTo(nextPathPoint)
            }
        }
    }

    // returns true if ai required to continue pathing, false otherwise
    int pathingDuration = 0
    int pathingAttempts = 0
    void doPathing() {
        if (navigationTarget == null) return

        if (++pathingDuration > 400 || isLocationInRange(navigationTarget)) {
            teleportToLocation(navigationTarget)
            return
        }

        if (pathingAttempts > 3) {
            // pathfinding hasn't made it to target, so teleport there in increments
            teleportTowardLocation(navigationTarget)
            return
        }

        if (getNavigation().hasDelayedRecomputation || isStuck()) {
            ++pathingAttempts
            startNavigatingTo(navigationTarget, false)
        }
    }

    void startNavigatingTo(Location location, boolean reset = true) {
        if (reset) {
            pathingAttempts = 0
            pathingDuration = 0
        }

        navigationTarget = location
        getNavigation().moveTo(location.x, location.y, location.z, 1F)
    }

    boolean isLocationInRange(Location location) {
        return Math.sqrt(distanceToSqr(location.x, location.y, location.z)) <= 0.33D
    }

    void teleportTowardLocation(Location targetLocation) {
        Location location = getBukkitEntity().getLocation()
        Vector direction = targetLocation.clone().subtract(location).toVector()
        if (direction.length() > 0.3D) {
            direction.multiply(0.3D / direction.length() as double)
        }

        location.setDirection(direction)
        location.setPitch(0F)
        location.add(direction)
        teleportToLocation(location, false)
    }

    void teleportToLocation(Location location, boolean resetTargetLocation = true) {
        if (resetTargetLocation) {
            navigationTarget = null
        }

        setPosRaw(location.x, location.y, location.z)
        setRot(location.yaw, location.pitch)

        getNavigation().stop()
        teleported = true
    }

    void setNavigationPath(NavigablePath path) {
//        if (path) npcTracker?.setName(path.name)

        this.navigationPath = path
    }

    void setNpcName(String name) {
        if (npcTracker) {
            npcTracker.setName(name)
        }
    }

    String getNpcName() {
        return npcTracker ? npcTracker.name : null
    }

    @Override
    protected void registerGoals() {

    }

    @Override
    void die(DamageSource source) {
        super.die(source)
        if (npcTracker) {
            NPCRegistry.get().unregister(npcTracker)
        }
    }

    @Override
    EntityDimensions getDimensions(Pose pose) {
        return ENTITY_SIZE
    }

    @Override
    boolean canBeCollidedWith() {
        return false
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null
    }

    @Override
    SoundEvent getDeathSound() {
        return null
    }

    @Override
    protected boolean damageEntity0(DamageSource damagesource, float f) {
        return false
    }
}
