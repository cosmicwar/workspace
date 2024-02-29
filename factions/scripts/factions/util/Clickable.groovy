package scripts.factions.util

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Clickable {
    private String[] args
    private ComponentBuilder message

    Clickable(String prefix) {
        this.message = new ComponentBuilder(prefix)
    }

    static void clickableCommand(Player player, String message, String command) {
        TextComponent component = new TextComponent(message)
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
        player.spigot().sendMessage(component)
    }

    static TextComponent clickableURLComponent(String message, String url) {
        TextComponent component = new TextComponent(message)
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
        return component
    }

    Clickable addClickEvent(String text, ClickEvent.Action actionType, String action) {
        message.append(text)
        //message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("")));
        message.event(new ClickEvent(actionType, action))
        return this
    }

    Clickable addClickHoverEvent(String text, ClickEvent.Action actionType, String action, HoverEvent.Action hoverAction, String hoverText) {
        message.append(text)
        message.event(new ClickEvent(actionType, action))
        message.event(new HoverEvent(hoverAction, TextComponent.fromLegacyText(hoverText)))
        message.append("")
        return this
    }

    Clickable addClickHoverEvent(String text, ClickEvent.Action actionType, String action, HoverEvent.Action hoverAction, List<String> hoverText) {
        message.append(text)
        message.event(new ClickEvent(actionType, action))
        message.event(new HoverEvent(hoverAction, TextComponent.fromLegacyText(hoverText.join("\n"))))
        message.append("")
        return this
    }

    Clickable addClickHoverEvent(String text, ClickEvent.Action actionType, String action, HoverEvent.Action hoverAction, String hoverText, ChatColor hoverChatColor) {
        message.append(text)
        message.event(new ClickEvent(actionType, action))
        message.event(new HoverEvent(hoverAction, TextComponent.fromLegacyText(hoverText, hoverChatColor)))
        message.append("")
        return this
    }

    Clickable addText(String text) {
        message.append(text)
        message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("")))
        message.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
        message.append("")
        return this
    }

    Clickable addHoverEvent(String text, HoverEvent.Action action, String hoverText) {
        message.append(text)
        message.event(new HoverEvent(action, TextComponent.fromLegacyText(hoverText)))
        message.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
        message.append("")
        return this
    }

    Clickable addHoverEvent(String text, HoverEvent.Action action, List<String> hoverText) {
        message.append(text)
        message.event(new HoverEvent(action, TextComponent.fromLegacyText(hoverText.join("\n"))))
        message.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
        message.append("")
        return this
    }

    Clickable join(Clickable clickable) {
        message.append(clickable.build())
        return this
    }

    BaseComponent[] build() {
        return message.create()
    }


}