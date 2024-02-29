package scripts.factions.features.enchant.enchants.soul

import com.google.common.collect.Sets
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.starcade.starlight.helper.Schedulers
import scripts.factions.content.dbconfig.entries.DoubleEntry
import scripts.factions.content.dbconfig.entries.IntEntry
import scripts.factions.content.dbconfig.entries.LongEntry
import scripts.factions.features.enchant.Enchantments
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.factions.features.enchant.indicators.Indicator
import scripts.shared.legacy.objects.Pair
import scripts.shared.utils.ItemType
import scripts.factions.features.enchant.utils.SoulUtils
import java.util.concurrent.ConcurrentHashMap


class DivineImmolation extends CustomEnchantment {

    int enchantLvl

    DivineIndicator indicator = new DivineIndicator()

    private final Map<UUID, Pair<Long, Integer>> immolatedPlayers = new ConcurrentHashMap<>()

    private Set<UUID> soulTaskUsers = Sets.newConcurrentHashSet()

    DivineImmolation() {
        super(
                "divineimmolation",
                EnchantmentTier.SOUL,
                EnchantmentType.PROC,
                "Divine Immolation",
                ["Chance to set your target on fire", "and deal extra damage"],
                [ItemType.SWORD],
                5
        )

        setProcChance(1.0D)

        getConfig().addDefault([
                new IntEntry("radiusPerLvL", 1, "Radius per level", "Ex. lvl = 2: 2*3 = 6 block radius"),
                new DoubleEntry("dmgPerLvL", 1.35D, "Damage per level"),
                new LongEntry("durationPerLvL", 4000L, "Duration per level")
        ])

        Enchantments.enchantConfig.queueSave()

        registerEvent()

        Schedulers.async().runRepeating({
            soulTaskUsers.each {
                Player player = Bukkit.getPlayer(it)
                if (player == null) {
                    soulTaskUsers.add(it) //was soulTaskToRemove
                    return
                }
                if (!consumeSouls(player)) {
                    soulTaskUsers.remove(it)
                    return
                }
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 0.5)
            }
        }, 0, 10)
    }

    @Override
    void onEquip(Player player, ItemStack itemStack, int enchantLevel) {
        if (ItemType.getTypeOf(itemStack) != ItemType.SWORD) return

        soulTaskUsers.add(player.getUniqueId())
    }

    @Override
    void onUnequip(Player player, ItemStack itemStack, int enchantLevel) {
        soulTaskUsers.remove(player.getUniqueId())
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (!(target instanceof Player)) return;
        if (!SoulUtils.isSoulMode(player)) return
        target.damage(2D)

        double damage = Math.floor((double)enchantLevel * (double)getConfig().getDoubleEntry("dmgPerLvL").value);
        int newRadius = getConfig().getIntEntry("radiusPerLvL").value * enchantLvl
        for (Player entity : EnchantUtils.getNearbyEnemyPlayers(player, target.getLocation(), newRadius, false)) {
            player.spawnParticle(Particle.FLAME, entity.getEyeLocation(), 30)
            player.spawnParticle(Particle.LAVA, entity.getEyeLocation(), 20)
//            indicator.spawn("divine_${player.getUniqueId().toString()}_${target.getUniqueId()}", player, target)
            addPotionWithDuration(entity, PotionEffectType.WITHER, 1, (int) (damage * 20D))

            entity.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1F, 0.3F);
            entity.playSound(player.getLocation(), Sound.ENTITY_ZOMBIFIED_PIGLIN_ANGRY, 1F, 0.3F);
            this.enchantLvl = enchantLevel
            immolatedPlayers.computeIfAbsent(player.getUniqueId(), k -> Pair.of(System.currentTimeMillis() + (getConfig().getLongEntry("durationPerLvL").value * enchantLevel), enchantLvl))
        }
    }

    void registerEvent() {
        Events.subscribe(EntityDamageEvent.class).filter(EventFilters.<EntityDamageEvent> ignoreCancelled()).handler({ event ->
            if (event.getCause() != EntityDamageEvent.DamageCause.WITHER) return
            if (!(event.getEntity() instanceof Player)) return

            Player player = (Player) event.getEntity();
            UUID uuid = player.getUniqueId()

            if (!immolatedPlayers.containsKey(uuid)) return
            event.setCancelled(true)
            Pair<Long, Integer> nestedMap = immolatedPlayers.get(uuid)
            if (nestedMap.getLeft() < System.currentTimeMillis()) {
                immolatedPlayers.remove(uuid)
                return
            }

            player.damage(Math.min(7D, 4D + enchantLvl));
            if (new Random().nextDouble() < 0.3) player.damage(9D)

            player.spawnParticle(Particle.FLAME, player.getEyeLocation(), 20)
            player.spawnParticle(Particle.LAVA, player.getEyeLocation(), 20)

            player.sendMessage("§c§l** DIVINE IMMOLATION **")
            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIFIED_PIGLIN_ANGRY, 0.6F, 0.8F);
        })
    }

    class DivineIndicator implements Indicator {

        @Override
        List<String> build(Object data) {
            return ["§4*§c§lDIVINE§4*"]
        }

    }
}
