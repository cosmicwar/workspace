package scripts.factions.eco.loottable.v2.impl

import groovy.transform.CompileStatic
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.helper.utils.Players
import scripts.factions.eco.loottable.v2.api.Reward
import scripts.shared.legacy.utils.FastItemUtils
import scripts.shared.legacy.wrappers.Console

@CompileStatic
class CommandReward implements Reward {

    UUID id = UUID.randomUUID()

    List<String> commands

    String title = "§aCommand Reward"
    List<String> lore = []
    Material icon = Material.GOLD_INGOT

    boolean tracking = false

    String message = null

    double weight
    boolean enabled, antiDupe, finalReward
    int maxPulls, timesPulled

    CommandReward(){}

    CommandReward(String command, double weight = 1, boolean enabled = true, boolean antiDupe = false, int maxPulls = 0, int timesPulled = 0, boolean finalReward = false) {
        this.commands = [command]
        this.weight = weight
        this.enabled = enabled
        this.antiDupe = antiDupe
        this.maxPulls = maxPulls
        this.timesPulled = timesPulled
        this.finalReward = finalReward
    }

    @BsonIgnore @Override
    ItemStack getItemStack() {
        return FastItemUtils.createItem(this.icon, this.title, this.lore)
    }

    @BsonIgnore @Override
    void giveReward(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid)
        if (player) {
            giveReward(player)
        } else {
            for (String command : this.commands) {//{uuid} §a| §d{player}
                Console.dispatchCommand(command.replace("{uuid}", uuid.toString()).replace("{player}", Bukkit.getOfflinePlayer(uuid).getName()))
            }
        }
        if (isTracking()) timesPulled++
    }

    @BsonIgnore @Override
    void giveReward(Player player, String message) {
        for (String command : this.commands) {//{uuid} §a| §d{player}
            Console.dispatchCommand(command.replace("{uuid}", player.getUniqueId().toString()).replace("{player}", player.getName()))
        }
        if (message) Players.msg(player, message)
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

