package scripts.factions.features.pve

import com.comphenix.protocol.PacketType
import groovy.transform.CompileStatic
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.monster.Slime
import org.starcade.starlight.helper.protocol.Protocol

import java.lang.reflect.Method

@CompileStatic
class PveEntityTools {

    static final Method METHOD_MOB_TICK

    static {
        METHOD_MOB_TICK = Mob.class.getDeclaredMethod("fc") // customServerAiStep
        METHOD_MOB_TICK.setAccessible(true)
    }

    PveEntityTools() {
        Protocol.subscribe(PacketType.Play.Client.USE_ENTITY).handler({
            if (it.isPlayerTemporary()) return

            /*PacketContainer packetContainer = it.getPacket()
            if (packetContainer.getEntityUseActions().read(0) != EnumWrappers.EntityUseAction.ATTACK) return

            org.bukkit.entity.Entity attackedEntity = packetContainer.getEntityModifier(it).read(0)
            if (attackedEntity == null) return

            if (attackedEntity instanceof Player) {
                Location attackedEntityLoc = attackedEntity.getLocation()

                List<Entity> nearbyPveEntities = attackedEntity.getNearbyEntities(0.25D, 0.25D, 0.25D)
                        .findAll { it instanceof LivingEntity && !it.isDead() && it.getPersistentDataContainer().has(PveEntity.pveEntity, PersistentDataType.BYTE) }
                        .sort { e1, e2 -> e1.getLocation().distanceSquared(attackedEntityLoc) <=> e2.getLocation().distanceSquared(attackedEntityLoc) ?: (e1 as LivingEntity).getHealth() <=> (e2 as LivingEntity).getHealth() }
                        .findResults { (it as CraftEntity)?.getHandle() } as List<Entity>

                if (nearbyPveEntities.isEmpty()) return

                packetContainer.getEntityModifier(it).write(0, nearbyPveEntities.get(0).getBukkitEntity())
            } else {
                String parentId = attackedEntity.getPersistentDataContainer().get(PveEntity.parentPveEntityId, PersistentDataType.STRING)
                if (parentId != null) {
                    org.bukkit.entity.Entity parentEntity = attackedEntity.world.getEntity(UUID.fromString(parentId))
                    if (parentEntity == null) return

                    packetContainer.getEntityModifier(it).write(0, parentEntity)
                }
            }*/
        })
    }

    static void tick(Mob entity) {
        if (entity.dead) return

        entity.getSensing().tick()
        entity.targetSelector.tick()
        entity.goalSelector.tick()
        entity.getNavigation().tick()
        entity.tick()
        METHOD_MOB_TICK.invoke(entity)
        entity.getMoveControl().tick()
        entity.getLookControl().tick()
        entity.getJumpControl().tick()

        collide(entity)
    }

    static void inactiveTick(Mob entity) {
        entity.goalSelector.inactiveTick(1, true)
        if (entity.targetSelector.inactiveTick(1, true)) {
            entity.targetSelector.tick()
        }
    }

    static void collide(Entity entity) {
        entity.level().getEntities(entity, entity.getBoundingBox(), EntitySelector.pushableBy(entity)).findAll { !(it instanceof Slime) }.each { it ->
            Entity otherEntity = it
            double d0 = entity.getX() - otherEntity.getX()
            double d1 = entity.getZ() - otherEntity.getZ()
            double d2 = Mth.absMax(d0, d1);

            if (d2 >= 0.009999999776482582D) {
                d2 = Math.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0D / d2;

                if (d3 > 1.0D) {
                    d3 = 1.0D;
                }

                d0 *= d3;
                d1 *= d3;
                d0 *= 0.05000000074505806D;
                d1 *= 0.05000000074505806D;

                if (!entity.isVehicle()) {
                    entity.push(d0, 0.0D, d1);
                }
            }
        }
    }

}
