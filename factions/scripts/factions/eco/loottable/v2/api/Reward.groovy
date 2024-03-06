package scripts.factions.eco.loottable.v2.api

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.helper.utils.Players

@CompileStatic
interface Reward {

    @BsonIgnore UUID getId()
    @BsonIgnore void setId(UUID uuid)

    @BsonIgnore String getMessage()

    @BsonIgnore double getWeight()
    @BsonIgnore void setWeight(double weight)

    @BsonIgnore boolean isEnabled()
    @BsonIgnore void setEnabled(boolean enabled)

    @BsonIgnore boolean isTracking()
    @BsonIgnore void setTracking(boolean tracking)

    @BsonIgnore boolean isAntiDupe()
    @BsonIgnore void setAntiDupe(boolean antiDupe)

    @BsonIgnore int getMaxPulls()
    @BsonIgnore void setMaxPulls(int maxPulls)

    @BsonIgnore int getTimesPulled()
    @BsonIgnore void setTimesPulled(int timesPulled)

    @BsonIgnore ItemStack getItemStack()

    @BsonIgnore default void giveReward(Player player) {
        giveReward(player, null)
    }

    @BsonIgnore void giveReward(Player player, String message)

    @BsonIgnore default void giveReward(Player player, int amount)
    {
        giveReward(player, amount, null)
    }

    @BsonIgnore default void giveReward(Player player, int amount, String message)
    {
        for (int i = 0; i < amount; i++) {
            giveReward(player)
        }
        Players.msg(player, message)
    }

    @BsonIgnore void giveReward(UUID uuid)

    @BsonIgnore Reward clone()

}

