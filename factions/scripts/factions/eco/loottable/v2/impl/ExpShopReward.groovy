package scripts.factions.eco.loottable.v2.impl

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import scripts.factions.eco.loottable.v2.RewardType
import scripts.factions.eco.loottable.v2.api.Reward
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.wrappers.Console

@CompileStatic
class ExpShopReward implements Reward {

    UUID id = UUID.randomUUID()

    String rewardSerialized
    String message = null

    Integer price = 0

    RewardType rewardType

    boolean tracking = false

    double weight
    boolean enabled, antiDupe, finalReward
    int maxPulls, timesPulled

    ExpShopReward(){}

    ExpShopReward(ItemStack stack, double weight = 1, boolean enabled = true, boolean antiDupe = false, int maxPulls = 0, int timesPulled = 0, boolean finalReward = false) {
        this.rewardSerialized = FastItemUtils.convertItemStackToString(stack)
        this.rewardType = RewardType.ITEM
        this.weight = weight
        this.enabled = enabled
        this.antiDupe = antiDupe
        this.maxPulls = maxPulls
        this.timesPulled = timesPulled
        this.finalReward = finalReward
    }

    ExpShopReward(String command, double weight = 1, boolean enabled = true, boolean antiDupe = false, int maxPulls = 0, int timesPulled = 0, boolean finalReward = false) {
        this.rewardSerialized = command
        this.rewardType = RewardType.COMMAND
        this.weight = weight
        this.enabled = enabled
        this.antiDupe = antiDupe
        this.maxPulls = maxPulls
        this.timesPulled = timesPulled
        this.finalReward = finalReward
    }

    @BsonIgnore @Override
    ItemStack getItemStack() {
        return FastItemUtils.createItem(Material.BARRIER, "exp shop item", [])
    }

    @BsonIgnore @Override
    void giveReward(UUID uuid) {
        FastInventoryUtils.addOrBox(uuid, null, null, getItemStack(), null)
        if (isTracking()) timesPulled++
    }

    @Override
    ExpShopReward clone() {
        def reward = new ExpShopReward()

        reward.rewardSerialized = this.rewardSerialized
        reward.rewardType = this.rewardType
        reward.message = this.message
        reward.tracking = this.tracking
        reward.weight = this.weight
        reward.enabled = this.enabled
        reward.antiDupe = this.antiDupe
        reward.finalReward = this.finalReward
        reward.maxPulls = this.maxPulls
        reward.timesPulled = this.timesPulled

        return reward
    }

    @BsonIgnore @Override
    void giveReward(Player player, String message) {
        if (rewardType == RewardType.COMMAND) {
            Console.dispatchCommand("rewards", rewardSerialized)
        } else if (rewardType == RewardType.ITEM) {
            FastInventoryUtils.addOrBox(player.getUniqueId(), player, null, getItemStack(), message)
        }

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

