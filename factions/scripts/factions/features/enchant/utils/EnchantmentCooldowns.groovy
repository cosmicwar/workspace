package scripts.factions.features.enchant.utils

import org.starcade.starlight.enviorment.Exports
import org.bukkit.entity.Player
import scripts.shared.utils.Persistent

EnchantmentCooldownUtils.init()

Exports.ptr("enchantments:getCooldown", { UUID uuid, String key, long needed -> return EnchantmentCooldownUtils.get(uuid, key, needed) })
Exports.ptr("enchantments:setCooldown", { UUID uuid, String key -> return EnchantmentCooldownUtils.set(uuid, key) })

class EnchantmentCooldownUtils {
    static Map<String, Map<UUID, Long>> COOLDOWNS

    static void init() {
        COOLDOWNS = Persistent.of("enchant_cooldowns", new HashMap<String, Map<UUID, Long>>()).get()
    }

    static void set(UUID uuid, String key, long time = System.currentTimeMillis()) {
        COOLDOWNS.computeIfAbsent(key, { k -> new HashMap<>() }).put(uuid, time)
    }

    static void set(Player player, String key, long time = System.currentTimeMillis()) {
        set(player.getUniqueId(), key, time)
    }

    static Long getLast(UUID uuid, String key) {
        return COOLDOWNS.get(key)?.get(uuid)
    }

    static long get(UUID uuid, String key, long needed) {
        Map<UUID, Long> cooldowns = COOLDOWNS.get(key)

        if (cooldowns == null) {
            return 0
        }
        Long lastUsed = cooldowns.get(uuid)

        if (lastUsed == null) {
            return 0
        }
        long passed = System.currentTimeMillis() - lastUsed
        return passed >= needed ? 0 : needed - passed
    }

    static long get(Player player, String key, long needed) {
        return get(player.getUniqueId(), key, needed)
    }

    static boolean has(Player player, String key) {
        return has(player.uniqueId, key)
    }

    static boolean has(UUID uuid, String key) {
        return COOLDOWNS.get(key)?.containsKey(uuid)
    }
}