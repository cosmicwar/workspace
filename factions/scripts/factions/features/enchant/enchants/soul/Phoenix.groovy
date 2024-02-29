package scripts.factions.features.enchant.enchants.soul


import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType


class Phoenix extends CustomEnchantment {

    Phoenix() {
        super(
                "phoenix",
                EnchantmentTier.SOUL,
                EnchantmentType.PROC,
                "Phoenix",
                ["Chance to revive you", "when you die"],
                [ItemType.BOOTS],
                3
        )

        setProcChance(1D)
        setCoolDown(180)
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (!(attacker instanceof Player) || player.getHealth() - event.getFinalDamage() > 0D) return
        if (!proc(player, enchantLevel)) return
        if (!consumeSouls(player, true)) return
        attacker.sendMessage("\n§6§l*** ${player.name} has phoenixed! ***\n")
        event.setDamage(0D);
        EnchantUtils.heal(player, 200D);

        player.sendMessage("")
        player.sendMessage("§6§l*** PHOENIX SOUL ***")
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1F, 1.25F);
    }
}
