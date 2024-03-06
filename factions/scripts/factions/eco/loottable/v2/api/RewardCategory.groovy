package scripts.factions.eco.loottable.v2.api

import com.google.common.collect.Sets
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Material
import scripts.shared.data.uuid.UUIDDataObject
import scripts.factions.eco.loottable.v2.impl.CommandReward
import scripts.factions.eco.loottable.v2.impl.ItemReward

class RewardCategory extends UUIDDataObject {

    String name
    Material icon

    Set<CommandReward> commandRewards = Sets.newConcurrentHashSet()
    Set<ItemReward> itemRewards = Sets.newConcurrentHashSet()

    RewardCategory() {}
    RewardCategory(UUID uuid) {
        super(uuid)
    }

    RewardCategory(String name, Material icon = Material.STONE, Set<ItemReward> itemRewards = [], Set<CommandReward> commandRewards = []) {
        this.name = name
        this.icon = icon

        this.itemRewards = itemRewards
        this.commandRewards = commandRewards
    }

    @BsonIgnore
    Set<Reward> getRewards() {
        return Sets.union(this.itemRewards, this.commandRewards)
    }

    @BsonIgnore
    Reward getReward(UUID uuid) {
        return this.getRewards().find { it.id == uuid }
    }

    @BsonIgnore @Override
    boolean isEmpty() {
        return false
    }

}
