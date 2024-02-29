package scripts.factions.anticheat

import com.google.common.collect.Maps
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.utils.Players
import scripts.shared.utils.Persistent

import java.util.concurrent.CopyOnWriteArrayList

class AutoClickerAlerts {
    Map<UUID, List<Long>> CLICKS_PER_SECOND

    AutoClickerAlerts() {
        CLICKS_PER_SECOND = Persistent.of("autoclicker:clicks_per_second", Maps.<UUID, CopyOnWriteArrayList<Long>> newConcurrentMap()).get()

        Schedulers.async().runRepeating({
            CLICKS_PER_SECOND.entrySet().findAll { it.value.size() > 80 }.each {
                Player player = Bukkit.getPlayer(it.key)
                if (player == null) return

                List<Long> clicks = it.value
                List<Integer> formattedMS = new ArrayList<>()
                long max = 0
                long min = 500
                for (Long time : clicks) {
                    long initialTime = time
                    int index = clicks.indexOf(initialTime)
                    if (index > 0) {
                        long previousTime = clicks.get(index - 1)
                        if (initialTime - previousTime > max) {
                            if (initialTime - previousTime < 500) {
                                max = initialTime - previousTime
                            }
                        }
                        if (initialTime - previousTime < min) {
                            min = initialTime - previousTime
                        }
                        formattedMS.add(initialTime - previousTime as Integer)
                    }
                }

                TextComponent message = new TextComponent(Players.messageRemapper.remap("§c§lAC §> §e$player.name §fmay be using §eautoclicker§f! §7(§f${it.value.size() / 5} > 16§7) Variance (${min} - ${max}ms)"))
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, [new TextComponent("Time Between Clicks: ${formattedMS}")] as BaseComponent[]))

                for (Player staff : Bukkit.getOnlinePlayers()) {
                    if (staff.hasPermission("group.staff.srmod")) {
//                        if (ToggleUtils.hasToggled(staff, "anticheat_autoclicker_notification")) continue
                        staff.sendMessage(message)
                    }
                }
            }
            CLICKS_PER_SECOND.clear()
        }, 100, 100)

        registerEvents()
    }

    void registerEvents() {
        Events.subscribe(PlayerInteractEvent, EventPriority.MONITOR).handler { e ->
            World world = e.getPlayer().getWorld()

            if (world == null) return

            if (e.getHand() == EquipmentSlot.HAND) {
                CLICKS_PER_SECOND.computeIfAbsent(e.player.uniqueId, v -> new CopyOnWriteArrayList<Long>()).add(System.currentTimeMillis())
            }
        }

        Events.subscribe(PlayerQuitEvent).handler { e ->
            CLICKS_PER_SECOND.remove(e.player.uniqueId)
        }
    }
}
