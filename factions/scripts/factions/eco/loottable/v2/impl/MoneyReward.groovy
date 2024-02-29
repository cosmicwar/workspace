package scripts.factions.eco.loottable.v2.impl

import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import scripts.factions.eco.loottable.v2.api.Reward
import scripts.shared.legacy.CurrencyUtils
import scripts.shared.legacy.utils.FastInventoryUtils
import scripts.shared.legacy.utils.FastItemUtils

class MoneyReward implements Reward {

    UUID id = UUID.randomUUID()

    double amount
    String withdrawName
    String currency

    String message = null

    boolean tracking = false

    double weight
    boolean enabled, antiDupe, finalReward
    int maxPulls, timesPulled

    MoneyReward(){}

    MoneyReward(double amount, String withdrawName = "reward", double weight = 1, boolean enabled = true, boolean antiDupe = false, int maxPulls = 0, int timesPulled = 0, boolean finalReward = false) {
        this.amount = amount
        this.withdrawName = withdrawName
        this.weight = weight
        this.enabled = enabled
        this.antiDupe = antiDupe
        this.maxPulls = maxPulls
        this.timesPulled = timesPulled
        this.finalReward = finalReward
    }

    @Override
    boolean isTracking() {
        return tracking
    }

    @Override
    void setTracking(boolean tracking) {
        this.tracking = tracking
    }

    @BsonIgnore @Override
    ItemStack getItemStack() {
        def note = CurrencyUtils.getBankNote()
        FastItemUtils.ensureUnique(note)
        FastInventoryUtils.addOrBox(player.getUniqueId(), player, player, note, "${prefix} §fWithdrew ${mapper.apply(amount)} §ffrom your account!")

        UUID id = FastItemUtils.getId(note)
        if (id != null) {
            Starlight.log.info("${player.getName()} withdrew a banknote worth ${amount.longValue()} ${table} with the UUID: ${id.toString()}")
        }
        return FastItemUtils.convertStringToItemStack(this.itemBase64)
    }

    @BsonIgnore @Override
    void giveReward(UUID uuid) {
        FastInventoryUtils.addOrBox(uuid, null, null, getItemStack(), null)
        timesPulled++
    }

    @BsonIgnore @Override
    void giveReward(Player player, String message) {
        FastInventoryUtils.addOrBox(player.getUniqueId(), player, null, getItemStack(), message)
        timesPulled++
    }

}
