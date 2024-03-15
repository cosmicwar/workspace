package scripts.exec

import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Schedulers
import net.minecraft.server.MinecraftServer

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
        "scripts/shared/systems/_loottable.groovy",
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
        "scripts/shared/legacy/command/_subcommandbuilder.groovy",
        "scripts/shared/legacy/toggles.groovy",
        "scripts/shared/legacy/cooldowns.groovy",
        "scripts/shared/legacy/utils/_combatutils.groovy",
        //"scripts/shared/legacy/weather.groovy",
        "scripts/shared/legacy/immovables.groovy",
        "scripts/shared/legacy/dropables.groovy",
        "scripts/shared/legacy/xp.groovy",
        "scripts/shared/legacy/chat.groovy",
//        "scripts/shared/legacy/tablist.groovy",
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
        "scripts/shared3/commands/CustomLore.groovy",
        "scripts/shared3/commands/RandomNick.groovy",
        "scripts/shared/utils/events/litebans/LiteBans.groovy",
        "scripts/shared/content/commands/worlds.groovy",

        "scripts/shared/content/Commands.groovy",

        // Data Storage \\
        "scripts/shared/data/uuid/UUIDDataManager.groovy",
        "scripts/shared/data/string/StringDataManager.groovy",
        "scripts/shared/core/cfg/DBConfig.groovy",

        // Profiles \\
        "scripts/shared/core/profile/Profiles.groovy",

        // Scoreboard \\
        "scripts/shared/content/scoreboard/tab/TabHandler.groovy",
        "scripts/shared/content/scoreboard/tab/tablist.groovy",

        // Essentials \\
        "scripts/shared/core/ess/Essentials.groovy",
)

Schedulers.sync().runLater({
    MinecraftServer.getServer().getPlayerList().maxPlayers = 1000
}, 1)
