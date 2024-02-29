package scripts.factions.eco.loottable.v2.impl

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scripts.factions.eco.loottable.v2.api.Reward
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils

@CompileStatic
class ItemReward implements Reward {

    UUID id = UUID.randomUUID()

    String itemBase64

    String message = null

    boolean tracking = false

    double weight
    boolean enabled, antiDupe, finalReward
    int maxPulls, timesPulled

    ItemReward(){}

    ItemReward(ItemStack stack, double weight = 1, boolean enabled = true, boolean antiDupe = false, int maxPulls = 0, int timesPulled = 0, boolean finalReward = false) {
        this.itemBase64 = FastItemUtils.convertItemStackToString(stack)
        this.weight = weight
        this.enabled = enabled
        this.antiDupe = antiDupe
        this.maxPulls = maxPulls
        this.timesPulled = timesPulled
        this.finalReward = finalReward
    }

    @BsonIgnore @Override
    ItemStack getItemStack() {
        return FastItemUtils.convertStringToItemStack(this.itemBase64)
    }

    @BsonIgnore @Override
    void giveReward(UUID uuid) {
        FastInventoryUtils.addOrBox(uuid, null, null, getItemStack(), null)
        if (isTracking()) timesPulled++
    }

    @BsonIgnore @Override
    void giveReward(Player player, String message) {
        FastInventoryUtils.addOrBox(player.getUniqueId(), player, null, getItemStack(), message)
        if (isTracking()) timesPulled++
    }

    @Override
    boolean isTracking() {
        return tracking
    }

    @Override
    void setTracking(boolean tracking) {
        this.tracking = tracking
    }

}

