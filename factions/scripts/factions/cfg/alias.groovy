package scripts.factions.cfg

import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.event.filter.EventFilters
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerCommandPreprocessEvent

Events.subscribe(PlayerCommandPreprocessEvent.class, EventPriority.LOWEST)
        .filter(EventFilters.ignoreCancelled())
        .handler { e ->
    def msg = e.message.toLowerCase()
    if (msg.startsWith("/f strike")) {
        e.message = msg.replace("/f strike", "/fstrike")
    } else if (msg.startsWith("/f target")) {
        e.message = msg.replace("/f target", "/ftarget")
    } else if (msg.startsWith("/f location")) {
        e.message = msg.replace("/f location", "/flocation")
    } else if (msg.startsWith("/f delalt")) {
        e.message = msg.replace("/f delalt", "/fdelalt")
    } else if (msg.startsWith("/f addalt")) {
        e.message = msg.replace("/f addalt", "/faddalt")
    } else if (msg.startsWith("/f altlist")) {
        e.message = msg.replace("/f altlist", "/faltlist")
    } else if (msg.startsWith("/f sendcoupon")) {
        e.message = msg.replace("/f sendcoupon", "/fsendcoupon")
    } else if (msg.startsWith("/f coupons")) {
        e.message = msg.replace("/f coupons", "/fcoupons")
    } else if (msg == ("/f di")) {
        e.player.sendMessage("§cDue to people running this command without knowing what it does, we made an explanation on what it does: https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        e.setCancelled(true)
    } else if (msg.startsWith("/f mute")) {
        e.message = msg.replace("/f mute", "/fmute")
    } else if (msg.startsWith("/f target")) {
        e.message = msg.replace("/f target", "/ftarget")
    } else if (msg.startsWith("/f ignore")) {
        e.message = msg.replace("/f ignore", "/fignore")
    } else if (msg.startsWith("/f raidclaim")) {
        e.message = msg.replace("/f raidclaim", "/fraidclaim")
    } else if (msg.startsWith("/f check")) {
        e.message = msg.replace("/f check", "/fcheck")
    } else if (msg.startsWith("/f alert")) {
        e.message = msg.replace("/f alert", "/falert")
    } else if (msg.startsWith("/f clear")) {
        e.message = msg.replace("/f clear", "/fclear")
    } else if (msg.startsWith("/f stealth")) {
        e.message = msg.replace("/f stealth", "/fstealth")
    } else if (msg.startsWith("/f power")) {
        e.message = msg.replace("/f power", "/fpower")
    } else if (msg.startsWith("/f list")) {
        e.message = msg.replace("/f list", "/flist")
    } else if (msg.startsWith("/f sandbank")) {
        e.message = msg.replace("/f sandbank", "/fsandbank")
    } else if (msg.startsWith("/f tntbank")) {
        e.message = msg.replace("/f tntbank", "/ftntbank")
    } else if (msg.startsWith("/falts add")) {
        e.message = msg.replace("/falts add", "/faddalt")
    } else if (msg.startsWith("/falt add")) {
        e.message = msg.replace("/falt add", "/faddalt")
    } else if (msg.startsWith("/falts join")) {
        e.message = msg.replace("/falts join", "/f join")
    } else if (msg.startsWith("/falt join")) {
        e.message = msg.replace("/falt join", "/f join")
    } else if (msg.startsWith("/falts kick")) {
        e.message = msg.replace("/falts kick", "/fdelalt")
    } else if (msg.startsWith("/falt kick")) {
        e.message = msg.replace("/falt kick", "/fdelalt")
    } else if (msg.startsWith("/fcf")) {
        e.message = msg.replace("/fcf", "/f c f")
    } else if (msg.startsWith("/fcp")) {
        e.message = msg.replace("/fcp", "/f c p")
    }
}