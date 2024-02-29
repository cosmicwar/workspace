package scripts.factions.util

import groovy.transform.CompileStatic
import net.jodah.expiringmap.ExpirationListener
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.starcade.starlight.enviorment.Exports
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.utils.Players
import scripts.shared3.utils.Callback

import java.util.concurrent.TimeUnit

@CompileStatic
class ChatPrompt {

    static Map<UUID, Callback<String>> prompts

    static ExpirationListener<UUID, Callback<String>> expiration(){
        return new ExpirationListener<UUID, Callback<String>>() {
            @Override
            void expired(UUID uuid, Callback<String> ignored) {
                def player = Bukkit.getPlayer(uuid)
                Players.msg(player, "§cPrompt expired.")
            }
        }
    }

    static Callback<String> empty = { }

    ChatPrompt() {
        prompts = ExpiringMap.builder()
                .expiration(30, TimeUnit.SECONDS)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .expirationListener(expiration())
                .build()

        Events.subscribe(AsyncPlayerChatEvent.class).handler { event ->
            def player = event.getPlayer()

            if (prompts.containsKey(player.getUniqueId())) {
                def prompt = prompts.findAll { it.key == player.getUniqueId() }.collect { it.value }.first()
                if (prompt == null) {
                    return
                }
                if (event.getMessage().startsWith("/")) {
                    return
                }

                if (event.getMessage().equalsIgnoreCase("cancel")) {
                    event.player.sendMessage("§aPrompt canceled.")
                    prompts.remove(event.getPlayer().getUniqueId())
                    event.setCancelled(true)
                    return
                }

                prompt.exec(event.getMessage())
                prompts.remove(event.getPlayer().getUniqueId())

                event.setCancelled(true)
            }
        }

        Exports.ptr("prompt:prompt", { Player player, String message = null, Callback<String> callback = empty ->
            prompts.put(player.getUniqueId(), callback)

            player.closeInventory()

            if (message != null) Players.msg(player, message)
        })
    }

}

@CompileStatic
class PromptUtils {
    static def prompt(Player player, String message, Callback<String> callback) {
        (Exports.ptr("prompt:prompt") as Closure).call(player, message, callback)
    }
}

