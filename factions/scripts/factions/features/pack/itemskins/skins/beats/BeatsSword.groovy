package scripts.factions.features.pack.itemskins.skins.beats

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import scripts.factions.features.enchant.utils.EnchantUtils
import scripts.factions.features.pack.itemskins.ItemSkin

class BeatsSword extends ItemSkin {

    BeatsSword() {
        super("beatssword")
    }

    

    @Override
    void onAttack(Player player, ItemStack itemStack, LivingEntity target, EntityDamageByEntityEvent event) {
        player.sendMessage("beats skin attack")
        EnchantUtils.scaleDamage(event, 5D)
    }
}