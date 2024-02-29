package scripts.factions.features.pve.entities.boss.abilities

import net.minecraft.world.entity.Entity
import org.bukkit.Sound
import org.bukkit.entity.AreaEffectCloud
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import scripts.factions.features.pve.entities.boss.Ability
import scripts.shared.legacy.utils.RandomUtils

import java.util.concurrent.ThreadLocalRandom

class AbilityLingeringPools extends Ability {

    static final List<PotionEffect> EFFECT_POOL = [
            new PotionEffect(PotionEffectType.HARM, 60, 2),
            new PotionEffect(PotionEffectType.SLOW, 60, 1),
            new PotionEffect(PotionEffectType.WEAKNESS, 60, 1)
    ]

    AbilityLingeringPools(Entity entity) {
        super(entity)
    }

    @Override
    void start() {
        List<Player> nearbyPlayers = new ArrayList<>(entity.bukkitEntity.location.getNearbyEntitiesByType(Player.class, 16, 4, 16))
        nearbyPlayers.shuffle()

        entity.bukkitEntity.world.playSound(entity.bukkitEntity.location, Sound.ENTITY_WITCH_CELEBRATE, 1F, 1F)
        for (int i = 0; i < Math.min(8, nearbyPlayers.size()); i++) {
            Player player = nearbyPlayers.get(i)

            PotionEffect potionEffect = RandomUtils.getRandom(EFFECT_POOL)
            AreaEffectCloud areaEffectCloud = entity.bukkitEntity.world.spawnEntity(player.location, EntityType.AREA_EFFECT_CLOUD) as AreaEffectCloud
            areaEffectCloud.setBasePotionData(new PotionData(PotionType.getByEffect(potionEffect.type)))
            areaEffectCloud.addCustomEffect(potionEffect, true)
            areaEffectCloud.setDuration(ThreadLocalRandom.current().nextInt(160, 200))
            areaEffectCloud.setRadius(1.25F)
            areaEffectCloud.setRadiusPerTick(0F)
        }
    }

    @Override
    void tick() {}

    @Override
    void complete() {}

    @Override
    boolean isFinished() {
        return true
    }

}
