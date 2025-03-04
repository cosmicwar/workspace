package scripts.exec

import net.minecraft.server.MinecraftServer
import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Schedulers
import scripts.shared.legacy.utils.ReflectionUtil

Starlight.watch(
        "scripts/shared/systems/bedrock_impl.groovy",
        "scripts/shared/systems/login.groovy",
        "scripts/shared/patches/TpsPatch.groovy",
        "scripts/shared/content/features/PlayerActivity.groovy",
        "scripts/shared/systems/heartbeat.groovy",
        "scripts/shared/systems/_servercache.groovy",
        "scripts/shared/systems/_bungeecache.groovy",
        "scripts/shared/systems/queue.groovy",
        "scripts/shared/systems/_menubuilder.groovy",
        "scripts/shared/systems/gui/GuiHandler.groovy",
        "scripts/shared/content/commands/transport.groovy",
        "scripts/shared/systems/security.groovy",
        "scripts/shared/systems/control.groovy",
        "scripts/shared/content/commands/glist.groovy",
        "scripts/shared/systems/variables.groovy",
        "scripts/shared/content/features/chatfilter.groovy",
        "scripts/shared/content/features/antispam.groovy",
        "scripts/shared/content/commands/alert.groovy",
        "scripts/shared/systems/notify.groovy",
        "scripts/shared/content/systems/reboot.groovy",
        "scripts/shared/content/commands/cgive.groovy",
        "scripts/shared/legacy/statistics/Statistics.groovy",
        "scripts/shared/legacy/AntiDupeUtils.groovy",
        "scripts/shared/legacy/systems/AntiFraud.groovy",
        "scripts/shared/legacy/awl.groovy",
        "scripts/shared/legacy/reboot.groovy",
        "scripts/shared/staff/Staff.groovy",
//        "scripts/shared/staff/Staff2.groovy",
        "scripts/shared/legacy/command/_subcommandbuilder.groovy",
        "scripts/shared/legacy/toggles.groovy",
        "scripts/shared/legacy/cooldowns.groovy",
        "scripts/shared/legacy/utils/_combatutils.groovy",
        "scripts/shared/legacy/weather.groovy",
        "scripts/shared/legacy/immovables.groovy",
        "scripts/shared/legacy/dropables.groovy",
        "scripts/shared/legacy/xp.groovy",
        "scripts/shared/legacy/chat.groovy",
        "scripts/shared/legacy/tablist.groovy",
        "scripts/shared/legacy/playertracker.groovy",
        "scripts/shared/legacy/StaffMode.groovy",
        "scripts/shared/legacy/StaffUtils.groovy",
        "scripts/shared/legacy/TrialSystem.groovy",
        "scripts/shared/legacy/Reports.groovy",
        "scripts/shared/legacy/disablecommands.groovy",
        //"scripts/shared/legacy/event/eventlistener.groovy",
        "scripts/shared/legacy/itemholograms.groovy",
        "scripts/shared/legacy/bossbar.groovy",
        "scripts/shared/legacy/tips.groovy",
        "scripts/shared/legacy/skincache.groovy",
        "scripts/shared/legacy/emotes.groovy",
        "scripts/shared/legacy/viaprotcolremappings.groovy",
        "scripts/shared/legacy/StaffStats.groovy",
        "scripts/shared/legacy/utils/npc/NPCRegistry.groovy",
        "scripts/shared/features/holograms/HologramRegistry.groovy",
        "scripts/shared/legacy/itemutils.groovy",
        "scripts/shared/legacy/utils/BlockedWordUtils.groovy",
//        "~/global/ArkGuildUtils.groovy",
//        "~/global/ArkGuilds.groovy",
        "scripts/shared3/commands/RandomNick.groovy",
        "scripts/shared/utils/events/litebans/LiteBans.groovy"
)

Schedulers.sync().runLater({
    MinecraftServer.getServer().playerList.maxPlayers = 2000
}, 1)
