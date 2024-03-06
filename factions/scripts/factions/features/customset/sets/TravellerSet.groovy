package scripts.factions.features.customset.sets

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import net.minecraft.world.entity.item.FallingBlockEntity
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import scripts.shared.core.cfg.entries.DoubleEntry
import scripts.shared.core.cfg.entries.LongEntry
import scripts.factions.core.faction.Factions
import scripts.factions.features.customset.struct.CustomSet
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.Persistent
import scripts.shared.visuals.floating.FloatingBlock

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

class TravellerSet extends CustomSet {

    Map<UUID, TravellerAbility> abilities = Persistent.of("set-traveler-ability-cache", Maps.<UUID, TravellerAbility> newConcurrentMap()).get() // caches local server abilities, should work w/ restart script?

    TravellerSet() {
        super(
                "traveller",
                "Traveller",
                [
                        "",
                        "§<bold>§<primaryColor>TRAVELLER SET BONUS",
                        "§<primaryColor>Deal §<secondaryColor>20% more §<primaryColor>damage to all enemies.",
                        "§<primaryColor>Take §<secondaryColor>5% less §<primaryColor>incoming damage.",
                        "",
                        "§<bold>§<rainbow>Traveller's Warp Ability Effect",
                ],
                "§<bold>§<rainbow>TRAVELLER SET EQUIPPED",
                "§<bold>§<rainbow>TRAVELLER SET REMOVED",
                "Helm",
                "Plate",
                "Trousers",
                "Boots",
                ["#8925a6", "#a5f8b9"],
                [" §<primaryColor>§l* §<primaryColor>§<secondaryColor>20% more §<primaryColor>damage to all enemies."]
        )

        getConfig().addDefault([
                new DoubleEntry("damageModifier", .20D),
                new DoubleEntry("incomingDamageModifier", .05D),
                new DoubleEntry("abilityProcChance", 0.02D),
                new LongEntry("abilityCooldown", 45_000L)
        ])

        Schedulers.async().runRepeating({
            abilities.values().removeIf {
                return !it.active
            }

            abilities.values().each { it.tick() }
        }, 50L, TimeUnit.MILLISECONDS, 50L, TimeUnit.MILLISECONDS)
    }

    @Override
    void onEquip(Player player) { sendEquippedMessage(player) }

    @Override
    void onUnequip(Player player) { sendUnequippedMessage(player) }

    @Override
    void onAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        def modifier = damageModifier + 1.0D
        EnchantUtils.scaleDamage(event, modifier)
    }

    @Override
    void onDamaged(Player player, Entity attacker, EntityDamageByEntityEvent event) {
        def modifier = 1.0D - incomingDamageModifier

        EnchantUtils.scaleDamage(event, modifier)

        if (abilities.containsKey(player.getUniqueId())) return
        if (hasAbilityCd(player)) return


        if (ThreadLocalRandom.current().nextDouble(0, 1) < abilityProcChance) {
            setAbilityCd(player, abilityCooldown)

            def ability = new TravellerAbility(player, player.world)

            def member = Factions.getMember(player.getUniqueId())
            if (member.factionId != null && member.factionId != Factions.wildernessId)
                ability.playerFactionId = member.factionId

            abilities.put(player.getUniqueId(), ability)

            Players.msg(player, "§5-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")
            Players.msg(player, "")
            Players.msg(player, "§5§l      TRAVELLER ABILITY ACTIVATED")
            Players.msg(player, "")
            Players.msg(player, "§5-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

            Players.playSound(player, Sound.ENTITY_WOLF_GROWL)
        }
    }

    double getDamageModifier() {
        return getConfig().getDoubleEntry("damageModifier").value
    }

    double getIncomingDamageModifier() {
        return getConfig().getDoubleEntry("incomingDamageModifier").value
    }

    double getAbilityProcChance() {
        return getConfig().getDoubleEntry("abilityProcChance").value
    }

    Long getAbilityCooldown() {
        return getConfig().getLongEntry("abilityCooldown").value
    }

    class TravellerAbility {

        UUID playerFactionId = null

        Player player
        World world
        Location startingLocation

        int tick = 0
        int layersSpawned = 0
        boolean active = true

        Long start = null

        Set<TravellerFallingBlockEntity> blocks = Sets.newConcurrentHashSet()

        TravellerAbility(Player player, World world) {
            this.player = player
            this.world = world

            startingLocation = player.location
        }

        def tick() {
            if (start == null) {
                start = System.currentTimeMillis()
            }
            if (tick % 20 == 0) {
                if (layersSpawned <= 4) {
                    spawnBlocks(player.location)
                    layersSpawned++
                }
            }

            blocks.each {block ->
                block.move(0, -1, 0)

                block.currentLocation.world.getChunkAtAsync(block.currentLocation, false) { chunk ->
                    chunk.getEntities()
                            .findAll { it instanceof Player && it != player && it.location.distance(block.currentLocation) <= 2 }
                            .collect { it as Player }
                            .each {
                                def member = Factions.getMember(it.getUniqueId())
                                if (member != null) {
                                    if (playerFactionId != null && member.factionId != null && member.factionId == playerFactionId) return
                                    it.damage(ThreadLocalRandom.current().nextDouble(it.getHealth() / 6, it.getMaxHealth() / 3), player)
                                }
                            }
                }
            }

            blocks.removeIf {block ->
                if (block.origin.getY() - block.currentLocation.y >= 13) {
                    block.untrack()
                    return true
                }

                return false
            }

            if (System.currentTimeMillis() - start > 3_500) {
                stop()
            } else {
                tick++
            }
        }

        def stop() {
            active = false
            blocks.each { it.untrack() }
        }

        def spawnBlocks(Location startingLoc) {
            for (int x = -5; x < 5; x++) {
                for (int z = -5; z < 5; z++) {
                    if (ThreadLocalRandom.current().nextDouble(0, 1) < 0.6) continue

                    Material material
                    switch (ThreadLocalRandom.current().nextInt(0, 3)) {
                        case 0:
                            material = Material.NETHERRACK
                            break
                        case 1:
                            material = Material.SOUL_SAND
                            break
                        case 2:
                            material = Material.MAGMA_BLOCK
                            break
                    }

                    def loc = startingLoc.clone().add(x, 10, z)
                    def block = new TravellerFallingBlockEntity(world, loc, material)
                    block.track()

                    blocks.add(block)
                }
            }
        }
    }

    class TravellerFallingBlockEntity extends FloatingBlock {

        TravellerFallingBlockEntity(World world, Location loc, Material material) {
            super(world, loc, material)
        }

        @Override
        net.minecraft.world.entity.Entity createEntity() {
            FallingBlockEntity entityFallingBlock = new FallingBlockEntity((world as CraftWorld).getHandle(), origin.getX(), origin.getY(), origin.getZ(), CraftMagicNumbers.getBlock(material).defaultBlockState())
            entityFallingBlock.setHurtsEntities(1, 7)
            entityFallingBlock.setNoGravity(false)
            entityFallingBlock.setInvulnerable(true)

            return entityFallingBlock
        }
    }
}

