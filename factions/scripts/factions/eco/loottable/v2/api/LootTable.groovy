package scripts.factions.eco.loottable.v2.api

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material
import org.starcade.starlight.helper.random.RandomSelector
import scripts.factions.eco.loottable.v2.impl.ExpShopReward
import scripts.shared.data.uuid.UUIDDataManager
import scripts.shared.data.uuid.UUIDDataObject
import scripts.factions.eco.loottable.v2.impl.CommandReward
import scripts.factions.eco.loottable.v2.impl.ItemReward

@CompileStatic(TypeCheckingMode.SKIP)
class LootTable extends UUIDDataObject {

    String name = "default"

    Material icon = Material.BEACON

    List<ItemReward> itemRewards = new ArrayList<>()
    List<CommandReward> commandRewards = new ArrayList<>()
    List<ExpShopReward> expShopRewards = new ArrayList<>()

//    Set<LootTableImage> images = new HashSet<>()

    UUID parentCategoryId = null

    LootTable() {
    }

    LootTable(UUID id) {
        super(id)
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }

    @BsonIgnore
    void organize(List<Reward> rewards) {
        this.itemRewards = rewards.findAll { it instanceof ItemReward }.collect({ it as ItemReward })
        this.commandRewards = rewards.findAll { it instanceof CommandReward }.collect({ it as CommandReward })
    }

    @BsonIgnore
    Reward getRandomReward() {
        def loot = new HashMap<Reward, Double>()

        rewards.each { reward ->
            if (!reward.enabled) return

            if (reward.isTracking() && reward.timesPulled >= reward.maxPulls && reward.maxPulls != 0) return

            loot.put(reward, reward.weight)
        }

        return RandomSelector.weighted(loot.entrySet(), { entry -> entry.getValue() }).pick().getKey()
    }

    @BsonIgnore
    List<Reward> getRewards() {
        List<Reward> rewards = []
        rewards.addAll(this.itemRewards)
        rewards.addAll(this.commandRewards)
        rewards.addAll(this.expShopRewards)
        return rewards
    }

    @BsonIgnore
    void removeReward(Reward reward) {
        if (reward instanceof ItemReward) {
            itemRewards.remove(reward)
        } else if (reward instanceof CommandReward) {
            commandRewards.remove(reward)
        } else if (reward instanceof ExpShopReward) {
            expShopRewards.remove(reward)
        }
    }

    @BsonIgnore
    void addReward(Reward reward) {
        if (reward instanceof ItemReward) {
            itemRewards.add(reward)
        } else if (reward instanceof CommandReward) {
            commandRewards.add(reward)
        } else if (reward instanceof ExpShopReward) {
            expShopRewards.add(reward)
        }
    }

    @BsonIgnore
    LootTableCategory getParentCategory() {
        return UUIDDataManager.getData(parentCategoryId, LootTableCategory, false)
    }

    @BsonIgnore
    List<Reward> getSortedRewards() {
        return getRewards()?.isEmpty() ? [] : getRewards().sort({ a, b -> a.weight <=> b.weight })
    }
}
