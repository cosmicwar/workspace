package scripts.factions.features.enchant.enchants.ultimate


import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.scheduler.Task
import scripts.factions.features.enchant.struct.CustomEnchantment
import scripts.factions.features.enchant.struct.EnchantmentTier
import scripts.factions.features.enchant.struct.EnchantmentType
import scripts.factions.features.enchant.struct.TickingEnchant
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.shared.utils.ItemType

class Angelic extends CustomEnchantment {

    Angelic() {
        super(
                "angelic",
                EnchantmentTier.ULTIMATE,
                EnchantmentType.PROC,
                "Angelic",
                ["Heals you over a short span of time"],
                [ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET],
                5,
                false
        )

        setCoolDown(20)
        setProcChance(0.03D)
    }

    @Override
    void onDamaged(Player player, ItemStack itemStack, int enchantLevel, Entity attacker, EntityDamageByEntityEvent event) {
        if (!proc(player, enchantLevel)) return
        Task healTask
        int repetitions = 0
        player.sendMessage("§e ** Angelic ** ")
        healTask = Schedulers.async().runRepeating({
            EnchantUtils.heal(player, 0.5D * enchantLevel)
            if (repetitions >= 4) healTask.stop()
            repetitions++
        }, 0, 20)
    }


}