package scripts.factions.features.enchant.enchants.legendary

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.utils.Players
import scripts.factions.features.enchant.indicators.Indicator
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier

import scripts.factions.features.enchant.EnchantListener
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.shared.utils.ItemType

class Silence extends CustomEnchantment {

    Closure<Boolean> hasSolitude = Exports.ptr("ench:solitude:contains") as Closure<Boolean>
    Closure<Integer> getSolitude = Exports.ptr("ench:solitude:get") as Closure<Integer>
    ScilenceIndicator indicator = new ScilenceIndicator()

    Silence() {
        super(
                "silence",
                EnchantmentTier.LEGENDARY,
                EnchantmentType.PROC,
                "Silence",
                ["Chance to silence your target"],
                [ItemType.SWORD],
                4
        )

        setStackable(false)
        setProcChance(0.045D)
        setCoolDown(25) // min of 10 sec cooldown, tbh should be longer :P
    }

    @Override
    void onAttack(Player player, ItemStack itemStack, int enchantLevel, LivingEntity target, EntityDamageByEntityEvent event) {
        if (target !instanceof Player) return
        if (!proc(player, enchantLevel)) return
        indicator.spawn("scilence_${player.getUniqueId().toString()}_${target.getUniqueId()}", player, target)
        Long soliduteMultiplier = 0
        if (hasSolitude.call(player)) {
            soliduteMultiplier = getSolitude.call(player) * 750
        }

        Players.msg(target, "§5§l* §5§lSILENCED §7§o({duration} Seconds) §5§l*".replace("{duration}", ((enchantLevel * 2) + getSolitude.call(player) as Integer).toString()))

        if (EnchantListener.addSilencedPlayer(target, (enchantLevel * 2 * 1000) + soliduteMultiplier)) {
            Players.msg(target, "§5§l* §5§lSILENCED §7§o({duration} Seconds) §5§l*".replace("{duration}", ((enchantLevel * 2) + getSolitude.call(player) as Integer).toString()))
        }
    }

    @Override
    double getRandomBypassChance(int enchantLevel) {
        return 0D
    }
}

class ScilenceIndicator implements Indicator {

    @Override
    List<String> build(Object data) {
        return [" §5§l* SILENCED * "]
    }
}
