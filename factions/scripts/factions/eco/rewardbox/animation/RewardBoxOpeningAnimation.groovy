package scripts.factions.eco.rewardbox.animation

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.ItemEntity
import org.bukkit.*
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.random.RandomSelector
import org.starcade.starlight.helper.utils.Players
import scripts.factions.eco.lootboxes.data.LootBox
import scripts.factions.eco.lootboxes.data.LootBoxReward
import scripts.factions.eco.loottable.api.Reward
import scripts.shared.visuals.floating.FloatingEntity

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@CompileStatic(TypeCheckingMode.SKIP)
abstract class RewardBoxOpeningAnimation {

    Player player
    World world
    LootBox lootBox
    ItemStack itemStack
    int durationTicks

    final AtomicInteger currentTick = new AtomicInteger(0)
    final AtomicBoolean finished = new AtomicBoolean(false)

    final List<Reward> rolledRewards = new ArrayList<>()

    RewardBoxOpeningAnimation() {
    }

    RewardBoxOpeningAnimation(Player player, World world, LootBox lootBox, ItemStack itemStack, int durationTicks) {
        this.player = player
        this.world = world
        this.lootBox = lootBox
        this.itemStack = itemStack?.clone()
        this.durationTicks = durationTicks
    }

    abstract void start()

    abstract void onTick(int tick)

    abstract void cleanup()

    boolean canFinish(int currentTick) {
        return currentTick > durationTicks
    }

    void tick() {
        int currentTick = currentTick.getAndAdd(1)
        if (currentTick == 0) {
            start()
        }

        if (finished.get()) return

        if (!player.isOnline() || player.getWorld() != world || canFinish(currentTick) || currentTick > durationTicks + 100) {
            // after 100 ticks over, just give up
            finish()
            return
        }

        if (currentTick < durationTicks) {
            onTick(currentTick)
        }
    }

    boolean isFinalTick(int tick) {
        return tick == durationTicks - 1
    }

    void rollRewards() {
        if (!rolledRewards.isEmpty()) return

        List<LootBoxReward> allRewards = lootBox.getRewardGroups().findResults { it.getRewards() }.flatten() as List<LootBoxReward>
        RandomSelector<LootBoxReward> randomSelector = RandomSelector.weighted(allRewards, reward -> reward.getWeight())
        int minRewards = lootBox.getMinRewards()
        int maxRewards = lootBox.getMaxRewards()
        (minRewards == maxRewards ? minRewards : ThreadLocalRandom.current().nextInt(minRewards, maxRewards + 1)).times {
            rolledRewards.add(randomSelector.pick())
        }
    }

    private void giveRewards() {
        if (rolledRewards.isEmpty()) {
            rollRewards()
        }
        //redo
        rolledRewards.each { lootBoxReward ->
            lootBoxReward.getCommands().each {
                String command = it.replace("{player}", player.getName())
                // temp fix
                if (command.startsWith("skins give")) {
                    String[] split = command.split("\\s+")
                    command = "giveskin ${split[2]} ${split[3]}"
                }

                Starlight.log.info("[LootBoxes] Dispatching command: ${command}")
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
            }
        }

        Map<String, List<LootBoxReward>> rewardsByName = rolledRewards.groupBy { it.displayName }.toSorted { -it.value.size() }
        StringBuilder rewardString = new StringBuilder()
        rewardsByName.eachWithIndex { Map.Entry<String, List<LootBoxReward>> entry, int index ->
            if (index > 0) {
                if (index == rewardsByName.size() - 1) {
                    rewardString.append("§f and ")
                } else {
                    rewardString.append("§f, ")
                }
            }

            rewardString.append("§f§l${entry.getValue().size()} x ")
            rewardString.append(entry.getKey())
        }

        Players.msg(player, "${lootBox.getDisplayName()} §> §f§lYou received: ${rewardString.toString()}§f§l!")
    }


    void finish() {
        if (finished.getAndSet(true)) return // already finished

        cleanup()
        giveRewards()
    }

    static FloatingEntity makeFloatingItem(World world, Location location, ItemStack itemStack, ChatColor glowColor, String displayName) {
        FloatingEntity floatingEntity = new FloatingEntity(world, location, {
            ItemEntity entityItem = new ItemEntity(EntityType.ITEM, (world as CraftWorld).getHandle())
            entityItem.setItem(CraftItemStack.asNMSCopy(itemStack))
            if (displayName != null) {
                entityItem.setCustomName(CraftChatMessage.fromStringOrNull(displayName))
                entityItem.setCustomNameVisible(true)
            }

            return entityItem
        })
//        if (glowColor != null) {
//            floatingEntity.setGlowColor(glowColor)
//        }
//        floatingEntity.attachToArmorStand = true
        return floatingEntity
    }

    static Color convertChatColorToColor(ChatColor chatColor) {
        java.awt.Color color = chatColor.asBungee().getColor()
        return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue())
    }

}