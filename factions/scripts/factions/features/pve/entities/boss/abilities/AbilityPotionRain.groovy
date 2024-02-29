package scripts.factions.features.pve.entities.boss.abilities

import net.minecraft.world.entity.Entity
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.AreaEffectCloud
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import scripts.factions.features.pve.entities.boss.Ability

import java.util.concurrent.ThreadLocalRandom

class AbilityPotionRain extends Ability {

    static final int MAX_DURATION = 250

    int duration
    List<PotionCloud> potionClouds = new ArrayList<>()

    AbilityPotionRain(Entity entity) {
        super(entity)
    }

    @Override
    void start() {
        Location location = entity.bukkitEntity.location
        List<Player> nearbyPlayers = new ArrayList<>(location.getNearbyEntitiesByType(Player.class, 16, 4, 16))
        nearbyPlayers.shuffle()

        for (int i = 0; i < 5; i++) {
            Player player = i >= nearbyPlayers.size() ? null : nearbyPlayers.get(i)
            Location cloudLocation = player == null ? location.clone().add(ThreadLocalRandom.current().nextInt(-12, 12), 0, ThreadLocalRandom.current().nextInt(-12, 12)) : nearbyPlayers.get(i).location
            int trackingHeight = ThreadLocalRandom.current().nextInt(5, 8)
            cloudLocation.setY(location.y + trackingHeight)
            PotionCloud potionCloud = new PotionCloud(player, cloudLocation, trackingHeight)
            potionCloud.spawn()
            potionClouds.add(potionCloud)
        }
    }

    @Override
    void tick() {
        if (++duration >= MAX_DURATION) return

        potionClouds.each { it.tick() }
    }

    @Override
    void complete() {
        potionClouds.each { it.die() }
    }

    @Override
    boolean isFinished() {
        return duration >= MAX_DURATION
    }

    class PotionCloud {

        final Player player
        final Location spawnLocation
        final int trackingHeight

        AreaEffectCloud areaEffectCloud
        int delay = ThreadLocalRandom.current().nextInt(10, 20)

        PotionCloud(Player player, Location spawnLocation, int trackingHeight) {
            this.player = player
            this.spawnLocation = spawnLocation
            this.trackingHeight = trackingHeight
        }

        void spawn() {
            areaEffectCloud = spawnLocation.world.spawnEntity(spawnLocation, EntityType.AREA_EFFECT_CLOUD) as AreaEffectCloud
            areaEffectCloud.setParticle(Particle.CLOUD)
            areaEffectCloud.setRadius(1.5F)
            areaEffectCloud.setRadiusPerTick(0F)
        }

        void tick() {
            if (areaEffectCloud == null) return

            if (player?.isOnline() && player?.world == areaEffectCloud.world && player.location.distance(areaEffectCloud.location) > trackingHeight + 1) {
                Location target = player.location.add(0, trackingHeight, 0)
                areaEffectCloud.teleport(areaEffectCloud.location.add(target.subtract(areaEffectCloud.location).toVector().normalize().multiply(0.5)))
            }

            Location loc = areaEffectCloud.location.add(ThreadLocalRandom.current().nextDouble(-2D, 2D), 0, ThreadLocalRandom.current().nextDouble(-2D, 2D))
            if (areaEffectCloud.ticksLived % 2 == 0) {
                spawnLocation.world.spawnParticle(Particle.WATER_DROP, loc, 0)
            }

            if (--delay <= 0) {
                spawnLocation.world.playSound(areaEffectCloud.location, Sound.WEATHER_RAIN_ABOVE, 1F, 1F)
                delay = ThreadLocalRandom.current().nextInt(20, 30)

                ThrownPotion thrownPotion = spawnLocation.world.spawnEntity(loc, EntityType.SPLASH_POTION) as ThrownPotion
                ItemStack potion = new ItemStack(Material.SPLASH_POTION)
                PotionMeta potionMeta = potion.getItemMeta() as PotionMeta

                if (ThreadLocalRandom.current().nextBoolean()) {
                    potionMeta.setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE))
                    potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 0, 2), true)
                } else {
                    potionMeta.setBasePotionData(new PotionData(PotionType.POISON))
                    potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 60, 2), true)
                }

                potion.setItemMeta(potionMeta)
                thrownPotion.setItem(potion)
            }
        }

        void die() {
            areaEffectCloud?.remove()
        }
    }

}
