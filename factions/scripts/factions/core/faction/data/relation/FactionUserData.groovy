package scripts.factions.core.faction.data.relation

import org.bukkit.Location
import org.bukkit.entity.EnderPearl

import java.lang.ref.WeakReference

class FactionUserData {
    long looterMessageCooldown
    long enderpearlCooldown
    long enderpearlDoorCooldown
    long superAppleCooldown
    long regularAppleCooldown
    boolean teleported
    Location validLocation
    WeakReference<EnderPearl> pearl

    boolean isThrowingPearl() {
        EnderPearl pearl
        return !(this.pearl == null || (pearl = this.pearl.get()) == null || pearl.isDead())
    }

}

