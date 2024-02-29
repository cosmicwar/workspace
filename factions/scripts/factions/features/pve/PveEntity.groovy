package scripts.factions.features.pve

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.level.Level
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.persistence.PersistentDataType
import org.starcade.starlight.Starlight
import scripts.factions.features.pve.entities.NametagSpacerSlime
import scripts.factions.features.pve.entities.boss.PveBoss
import scripts.shared.features.EntityGlow

class PveEntity<T> {

    static NamespacedKey pveEntity = new NamespacedKey(Starlight.plugin, "pveEntity")
    static NamespacedKey parentPveEntityId = new NamespacedKey(Starlight.plugin, "pveEntity_parentId")

    Mob nmsEntity

    String name
    String displayName
    Location spawnLocation
    PveMobDifficulty difficulty
    ArmorStand healthStand = null
    List<LivingEntity> closables = new ArrayList<>()
    boolean isMiniboss = false
    UUID owner = null

    float lastHealth

    PveEntity(String name, String displayName, Location spawnLocation, PveMobDifficulty difficulty, Class<T> entityClass, float scale = 1F) {
        this.name = name
        this.displayName = displayName
        this.spawnLocation = spawnLocation
        this.difficulty = difficulty
        if (PveBoss.class.isAssignableFrom(entityClass)) {
            this.nmsEntity = (Mob) entityClass.getDeclaredConstructor(Level, PveMobDifficulty, float).newInstance(((CraftWorld) spawnLocation.getWorld()).getHandle(), difficulty, scale)
        } else {
            this.nmsEntity = (Mob) entityClass.getDeclaredConstructor(Level, float).newInstance(((CraftWorld) spawnLocation.getWorld()).getHandle(), scale)
        }

        nmsEntity.getBukkitEntity().getPersistentDataContainer().set(pveEntity, PersistentDataType.BYTE, (byte) 1)
        spawn()
    }

    private void spawn() {
        nmsEntity.moveTo(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch())
        ((CraftWorld) spawnLocation.getWorld()).addEntity(nmsEntity, CreatureSpawnEvent.SpawnReason.CUSTOM)
        def nameStand = createDisplayStand(this.nmsEntity, false)
        nameStand.setCustomName(CraftChatMessage.fromStringOrNull(displayName))
        this.healthStand = createDisplayStand(nameStand, true)
        this.updateHealthbar(nmsEntity.getHealth())

        nmsEntity.setCustomName(CraftChatMessage.fromStringOrNull(displayName))
        nmsEntity.setCustomNameVisible(true)

        if (nmsEntity instanceof PveBoss) {
            ChatColor glowColor = (nmsEntity as PveBoss).getGlowColor()
            if (glowColor) {
                EntityGlow.addGlow(nmsEntity.getBukkitLivingEntity(), glowColor)
            }
        }
    }

    private ArmorStand createDisplayStand(LivingEntity attachTo, boolean marker) {
        NametagSpacerSlime nametagSpacer = new NametagSpacerSlime(((CraftWorld) spawnLocation.getWorld()).getHandle())
        nmsEntity.setPos(spawnLocation.getX(), spawnLocation.getY() + nmsEntity.getEyeHeight(), spawnLocation.getZ())
        nmsEntity.setRot(spawnLocation.getYaw(), spawnLocation.getPitch())
        ((CraftWorld) spawnLocation.getWorld()).addEntity(nametagSpacer, CreatureSpawnEvent.SpawnReason.CUSTOM)
        nametagSpacer.setCustomName(CraftChatMessage.fromStringOrNull(displayName))
        nametagSpacer.setSize(marker ? 0 : -1, false)

        nametagSpacer.bukkitEntity.getPersistentDataContainer().set(parentPveEntityId, PersistentDataType.STRING, nmsEntity.getUUID().toString())

        ArmorStand retr = new ArmorStand(EntityType.ARMOR_STAND, ((CraftWorld) spawnLocation.getWorld()).getHandle())
        retr.setPos(spawnLocation.getX(), spawnLocation.getY() + nmsEntity.getEyeHeight(), spawnLocation.getZ())
        retr.setRot(spawnLocation.getYaw(), spawnLocation.getPitch())
        ((CraftWorld) spawnLocation.getWorld()).addEntity(retr, CreatureSpawnEvent.SpawnReason.CUSTOM)
        retr.setCustomNameVisible(true)
        retr.setSmall(true)
        retr.setInvisible(true)
        retr.setNoGravity(true)
        retr.setMarker(marker)
        retr.canTick = false
        retr.setInvulnerable(true)

        retr.bukkitEntity.getPersistentDataContainer().set(parentPveEntityId, PersistentDataType.STRING, nmsEntity.getUUID().toString())

        retr.startRiding(nametagSpacer)
        nametagSpacer.startRiding(attachTo)

        this.closables.add(nametagSpacer)
        this.closables.add(retr)

        return retr
    }

    void updateHealthbar(float health = nmsEntity.getHealth()) {
        if (nmsEntity == null || health == lastHealth || nmsEntity.dead) return
        lastHealth = health

        healthStand.setCustomName(CraftChatMessage.fromStringOrNull("§c§l" + (int) Math.ceil(health) + " §f§l/ §c§l" + (int) this.nmsEntity.getMaxHealth()))
    }

    void close() {
        for (LivingEntity entityLiving : closables) {
            entityLiving.getBukkitEntity().remove()
        }
    }

    void kill() {
        this.closables.each {entity ->
            entity.die(nmsEntity.damageSources().magic())
            ((CraftWorld) spawnLocation.getWorld()).getHandle().getChunkSource().removeEntity(entity)
        }

        nmsEntity.die(nmsEntity.damageSources().magic())
        ((CraftWorld) spawnLocation.getWorld()).getHandle().getChunkSource().removeEntity(nmsEntity)
    }

    String getDisplayName() {
        return displayName
    }

    String getName() {
        return name
    }

    Location getSpawnLocation() {
        return spawnLocation
    }

    PveMobDifficulty getDifficulty() {
        return difficulty
    }
}
