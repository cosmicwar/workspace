package scripts.factions.features.enchant.potion

import com.google.common.collect.Sets
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.enviorment.GroovyScript
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import scripts.shared.legacy.utils.BroadcastUtils

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

class PotionEffects {
    Map<UUID, Set<PotionEffect>> equippedPotionEffects = new ConcurrentHashMap<>()

    PotionEffects() {

        GroovyScript.addUnloadHook {
            Bukkit.getOnlinePlayers().each {
                removeAllEffects(it.getUniqueId())
            }
        }

        registerEvents()
        Exports.ptr("potionEffects:getEquipedEffects", { Player player ->
            return getEquippedPotionEffects(player)
        })
        Exports.ptr("potionEffects:addEquippedEffect", { Player player, PotionEffectType type, int amplifier ->
            addEquippedPotionEffect(player, type, amplifier)
        })
        Exports.ptr("potionEffects:addPotionWithDuration", { Player player, PotionEffectType type, int amplifier, int duration ->
            addPotionWithDuration(player, type, amplifier, duration)
        })
        Exports.ptr("potionEffects:removeEquippedEffect", { Player player, PotionEffectType type, int amplifier ->
            removeEquippedPotionEffect(player, type, amplifier)
        })
        Exports.ptr("potionEffects:removeMatchingEffect", { Player player, PotionEffectType type ->
            removeMatchingEffect(player, type)
        })
        Exports.ptr("potionEffects:removeAllEffects", { Player player ->
            removeAllEffects(player.getUniqueId())
        })
    }

    void registerEvents() {
        Events.subscribe(EntityPotionEffectEvent.class).filter(EventFilters.<EntityPotionEffectEvent> ignoreCancelled()).handler({
            if (!(it.getEntity() instanceof Player)) return

            Player player = it.getEntity() as Player
            Set<PotionEffect> equippedEffects = getEquippedPotionEffects(player)
            if (equippedEffects.isEmpty()) return

            switch (it.getAction()) {
                case EntityPotionEffectEvent.Action.ADDED:
                    PotionEffect effect = it.getNewEffect()
                    // same or worse effect
                    if (equippedEffects.any { it.getType() == effect.getType() && it.getAmplifier() == effect.getAmplifier() && effect.getDuration() < it.getDuration() }) {
                        it.setCancelled(true)
                    }
                    break
                case EntityPotionEffectEvent.Action.CLEARED:
                case EntityPotionEffectEvent.Action.REMOVED:
                    if (it.getAction() == EntityPotionEffectEvent.Action.CLEARED && it.getCause() == EntityPotionEffectEvent.Cause.DEATH) break

                    PotionEffect effect = it.getOldEffect()
                    PotionEffect bestEquipped = equippedEffects.findAll { it.getType() == effect.getType() }?.max { it.getAmplifier() }
                    if (bestEquipped == null || effect.getAmplifier() > bestEquipped.getAmplifier()) break

                    if (it.getOldEffect() != null && it.getOldEffect().getDuration() == 0) break
                    if (it.getNewEffect() != null && it.getNewEffect().getDuration() == 0) break

                    it.setCancelled(true)
                    break
                case EntityPotionEffectEvent.Action.CHANGED:
                    PotionEffect newEffect = it.getNewEffect()
                    PotionEffect bestEquipped = equippedEffects.findAll { it.getType() == newEffect.getType() }?.max { it.getAmplifier() }

                    if (bestEquipped?.getAmplifier() > newEffect.getAmplifier()) {
                        it.setCancelled(true)
                    }
                    break
            }
        })
    }

    Set<PotionEffect> getEquippedPotionEffects(Player player) {
        return equippedPotionEffects.getOrDefault(player.uniqueId, Collections.<PotionEffect> emptySet())
    }

    void addEquippedPotionEffect(Player player, PotionEffectType potionEffectType, int amplifier) {
        Set<PotionEffect> effects = equippedPotionEffects.computeIfAbsent(player.getUniqueId(), v -> Sets.newConcurrentHashSet())
        effects.removeIf({ PotionEffect effect -> effect.getType() == potionEffectType } as Predicate<PotionEffect>)

        PotionEffect potionEffect = new PotionEffect(potionEffectType, PotionEffect.INFINITE_DURATION, amplifier, false, false)
        effects.add(potionEffect)
        player.addPotionEffect(potionEffect)
    }

    void addPotionWithDuration(Player player, PotionEffectType potionEffectType, int amplifier, int duration) {
        Set<PotionEffect> effects = equippedPotionEffects.computeIfAbsent(player.getUniqueId(), v -> Sets.newConcurrentHashSet())
        effects.removeIf({ PotionEffect effect -> effect.getType() == potionEffectType } as Predicate<PotionEffect>)

        PotionEffect potionEffect = new PotionEffect(potionEffectType, duration, amplifier, false, false)
        effects.add(potionEffect)
        player.addPotionEffect(potionEffect)
    }

    void removeMatchingEffect(Player player, PotionEffectType type) {
        boolean removed = false
        equippedPotionEffects.getOrDefault(player.uniqueId, Collections.<PotionEffect> emptySet()).removeIf({ PotionEffect effect ->
            if (effect.getType() == type) {
                removed = true
                return true
            }

            return false
        } as Predicate<PotionEffect>)

        if (!removed) return

        // vanilla now "hides" unexpired effects within other ones of the same type to reapply later
        List<PotionEffect> effects = player.getActivePotionEffects().toList()
        if (effects.isEmpty()) return

        PotionEffect existingEffect = effects.stream().filter({ PotionEffect effect -> effect.getType() == type }).findFirst().orElse(null)
        if (existingEffect == null) return

        player.removePotionEffect(type)
    }

    void removeEquippedPotionEffect(Player player, PotionEffectType type, int amplifier) {
        boolean removed = false
        equippedPotionEffects.getOrDefault(player.uniqueId, Collections.<PotionEffect> emptySet()).removeIf({ PotionEffect effect ->
            if (effect.getType() == type && effect.getAmplifier() == amplifier) {
                removed = true
                return true
            }

            return false
        } as Predicate<PotionEffect>)

        if (!removed) return

        // vanilla now "hides" unexpired effects within other ones of the same type to reapply later
        List<PotionEffect> effects = player.getActivePotionEffects().toList()
        if (effects.isEmpty()) return

        PotionEffect existingEffect = effects.stream().filter({ PotionEffect effect -> effect.getType() == type }).findFirst().orElse(null)
        if (existingEffect == null) return

        if (existingEffect.getAmplifier() == amplifier) {
            player.removePotionEffect(type)
        }
    }

    void removeAllEffects(UUID uuid) {
        equippedPotionEffects.remove(uuid)
    }
}