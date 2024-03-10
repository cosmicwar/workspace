package scripts

import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Schedulers
import net.minecraft.server.MinecraftServer

Starlight.watch(
        "~/shared/systems/bedrock_impl.groovy",
        "~/shared/systems/login.groovy",
        "~/shared/patches/TpsPatch.groovy",
        "~/shared/content/features/PlayerActivity.groovy",
        "~/shared/systems/heartbeat.groovy",
        "~/shared/systems/_servercache.groovy",
        "~/shared/systems/_bungeecache.groovy",
        "~/shared/systems/queue.groovy",
        "~/shared/systems/_menubuilder.groovy",
        "~/shared/content/commands/transport.groovy",
        "~/shared/systems/security.groovy",
        "~/shared/systems/control.groovy",
        "~/shared/content/commands/glist.groovy",
        "~/shared/systems/variables.groovy",
        "~/shared/content/features/chatfilter.groovy",
        "~/shared/content/features/antispam.groovy",
        "~/shared/content/commands/alert.groovy",
        "~/shared/systems/notify.groovy",
        "~/shared/content/systems/reboot.groovy",
        "~/shared/content/commands/cgive.groovy",
        "~/shared/legacy/statistics/Statistics.groovy",
        "~/shared/legacy/AntiDupeUtils.groovy",
        "~/shared/legacy/systems/AntiFraud.groovy",
        "~/shared/legacy/awl.groovy",
        "~/shared/legacy/reboot.groovy",
        "~/shared/staff/Staff.groovy",
        "~/shared/legacy/command/_subcommandbuilder.groovy",
        "~/shared/legacy/toggles.groovy",
        "~/shared/legacy/cooldowns.groovy",
        "~/shared/legacy/utils/_combatutils.groovy",
        //"~/shared/legacy/weather.groovy",
        "~/shared/legacy/immovables.groovy",
        "~/shared/legacy/dropables.groovy",
        "~/shared/legacy/xp.groovy",
        "~/shared/legacy/chat.groovy",
//        "~/shared/legacy/tablist.groovy",
        "~/shared/legacy/playertracker.groovy",
        "~/shared/legacy/StaffMode.groovy",
        "~/shared/legacy/StaffUtils.groovy",
        "~/shared/legacy/TrialSystem.groovy",
        "~/shared/legacy/Reports.groovy",
        "~/shared/legacy/disablecommands.groovy",
        //"~/shared/legacy/event/eventlistener.groovy",
        "~/shared/legacy/itemholograms.groovy",
        "~/shared/legacy/bossbar.groovy",
        "~/shared/legacy/tips.groovy",
        "~/shared/legacy/skincache.groovy",
        "~/shared/legacy/emotes.groovy",
        "~/shared/legacy/viaprotcolremappings.groovy",
        "~/shared/legacy/StaffStats.groovy",
        "~/shared/legacy/utils/npc/NPCRegistry.groovy",
        "~/shared/features/holograms/HologramRegistry.groovy",
        "~/shared/legacy/itemutils.groovy",
        "~/shared/legacy/utils/BlockedWordUtils.groovy",
        "~/shared3/commands/CustomLore.groovy",
        "~/shared3/commands/RandomNick.groovy",
        "~/shared/utils/events/litebans/LiteBans.groovy",
        "~/shared/content/commands/worlds.groovy",
        "scripts/shared3/ArkExt.groovy",

        // new
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
