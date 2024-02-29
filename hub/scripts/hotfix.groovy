package scripts

import org.starcade.starlight.Starlight
import org.starcade.starlight.helper.Commands
import org.starcade.starlight.helper.Events
import org.starcade.starlight.helper.Schedulers
import io.papermc.paper.configuration.WorldConfiguration
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.GameRule
import org.bukkit.World
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.world.WorldLoadEvent
import scripts.shared.legacy.utils.TitleUtils
import scripts.shared.systems.Bedrock
import scripts.shared.utils.HastebinUtils
import scripts.shared.utils.PluginUtils

Commands.create().assertOp().assertUsage("<player>").handler({ command ->
    org.bukkit.entity.Player player = Bukkit.getPlayerExact(command.rawArg(0))
    if (player != null) {
//        player.world.execute {
            player.kickPlayer("Disconnecting.")
//        }
    }

    ServerPlayer serverPlayer = MinecraftServer.getServer().getPlayerList().players.find { ((Player) it).getGameProfile().getName().equalsIgnoreCase(command.rawArg(0)) }
    if (serverPlayer == null) {
        command.reply("Could not find a stuck player matching that name.")
        return
    }

//    serverPlayer.level.execute {
        MinecraftServer.getServer().getPlayerList().remove(serverPlayer)
//    }

    command.reply("Player has been force disconnected")
}).register("forcedc")

Commands.create().assertOp().assertPlayer().handler({
    it.reply("${Bedrock.isBedrockPlayer(it.sender())}")
}).register("dev/bedrock")

Commands.create().assertOp().handler({
    Bukkit.getOnlinePlayers().forEach({
        org.bukkit.entity.Player player = it
        String title = "My name is ${player.name}"

        TitleUtils.show(player, title, "${player.name}", 0, 10, 1)
    })
}).register("dev/shownametime")

Commands.create().assertPermission("commands.logs").assertUsage("<player>").handler { command ->
    // export NAME=Markoo; zgrep -a $NAME *.gz > $NAME.txt; cat latest.log | grep $NAME >> $NAME.txt
    String name = command.rawArg(0)

    String defaultWebsite = "dylan"

    Map<String, String> websites = [
            hastebin: "https://hastebin.com",
            dylan   : "https://paste.dylankeir.uk"
    ]
    String website = websites.get(defaultWebsite)

    if (command.args().size() > 1) {
        website = websites.get(command.rawArg(1).toLowerCase())

        if (website == null) {
            website = websites.get(defaultWebsite)
            command.reply("§eUnknown website, defaulting to ${website}...")
        }
    }
    command.reply("§aUsing website ${website}...")

    Schedulers.async().run {
        File logs = new File(Starlight.plugin.getDataFolder().getParentFile().getAbsoluteFile().getParentFile(), "logs")

        command.reply("§aStarting program...")

        Process proc = new ProcessBuilder("/bin/bash", "-c", "zgrep -a ${name} *.gz; cat latest.log | grep ${name}; echo \"CHAT START\"; cat ../plugins/ChatControl/logs/chat.txt | grep ${name}").directory(logs).redirectErrorStream(true).start()

        command.reply("§aReading logs...")

        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))
        StringBuilder builder = new StringBuilder()

        String line

        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n")
        }
        proc.waitFor()
        reader.close()

        command.reply("§aUploading...")
        try {
            String url = HastebinUtils.createRequest(builder, HastebinUtils.URIS.get("dylan"))
            command.reply("§aUploaded! §e${url}")
        } catch (Exception exc) {
            command.reply("§cFailed upload! Message:\n§f${exc.message}")
        }
    }
}.register("logs")

MinecraftServer.getServer().getPlayerList().setViewDistance(5)
MinecraftServer.getServer().getPlayerList().setSimulationDistance(4)
Schedulers.sync().runLater({
    MinecraftServer.getServer().getPlayerList().setViewDistance(5)
    MinecraftServer.getServer().getPlayerList().setSimulationDistance(4)
}, 20) // IDK!

Events.subscribe(WorldLoadEvent.class).handler { e -> configWorld(e.getWorld()) }

Schedulers.sync().runLater({
    Bukkit.getServer().getWorlds().forEach({ w ->
        configWorld(w)
    })
}, 5)

static def configWorld(World w) {
    w.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0)
    w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
    w.setGameRule(GameRule.DO_INSOMNIA, false)
    w.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
    ServerLevel nmsWorld = (w as CraftWorld).getHandle()
    nmsWorld.spigotConfig.playerTrackingRange = 32
    nmsWorld.spigotConfig.animalTrackingRange = 32
    nmsWorld.spigotConfig.monsterTrackingRange = 32
    nmsWorld.spigotConfig.otherTrackingRange = 32
    nmsWorld.spigotConfig.miscTrackingRange = 32

    nmsWorld.spigotConfig.monsterActivationRange = 32
    nmsWorld.spigotConfig.raiderActivationRange = 32
    nmsWorld.spigotConfig.villagerActivationRange = 32
    nmsWorld.spigotConfig.waterActivationRange = 32
    nmsWorld.spigotConfig.animalActivationRange = 32
    nmsWorld.spigotConfig.miscActivationRange = 32
    nmsWorld.spigotConfig.flyingMonsterActivationRange = 32
    nmsWorld.spigotConfig.itemMerge = 8
    nmsWorld.spigotConfig.expMerge = 8
    nmsWorld.spigotConfig.itemDespawnRate = 20 * 5 * 60
    nmsWorld.spigotConfig.arrowDespawnRate = 20 * 10
    nmsWorld.spigotConfig.hopperTransfer = 10
    nmsWorld.spigotConfig.hopperCheck = 10
    nmsWorld.spigotConfig.hopperAmount = 64

    nmsWorld.paperConfig().tickRates.mobSpawner = 2
    nmsWorld.paperConfig().tickRates.containerUpdate = 2
    nmsWorld.paperConfig().collisions.maxEntityCollisions = 0
    nmsWorld.paperConfig().collisions.onlyPlayersCollide = true
    nmsWorld.paperConfig().collisions.allowPlayerCrammingDamage = false
    nmsWorld.paperConfig().environment.disableIceAndSnow = true
    nmsWorld.paperConfig().entities.behavior.disableChestCatDetection = true
    nmsWorld.paperConfig().entities.armorStands.doCollisionEntityLookups = false
    nmsWorld.paperConfig().entities.armorStands.tick = false
    nmsWorld.paperConfig().lootables.autoReplenish = false
    nmsWorld.paperConfig().misc.redstoneImplementation = WorldConfiguration.Misc.RedstoneImplementation.EIGENCRAFT

    nmsWorld.paperConfig().anticheat.antiXray.enabled = false
    nmsWorld.spigotConfig.monsterActivationRange = 0
    nmsWorld.spigotConfig.animalActivationRange = 0
    nmsWorld.spigotConfig.miscActivationRange = 0
    nmsWorld.spigotConfig.flyingMonsterActivationRange = 0
    nmsWorld.spigotConfig.waterActivationRange = 0
    nmsWorld.spigotConfig.raiderActivationRange = 0
    nmsWorld.spigotConfig.villagerActivationRange = 0
    w.setDifficulty(Difficulty.PEACEFUL)

    w.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
    w.setGameRule(GameRule.DISABLE_RAIDS, true)
    w.setGameRule(GameRule.DO_ENTITY_DROPS, false)
    w.setGameRule(GameRule.DO_TILE_DROPS, false)
    w.setGameRule(GameRule.DO_MOB_SPAWNING, false)
    w.setGameRule(GameRule.DO_MOB_LOOT, false)
    w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
    w.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
    w.setGameRule(GameRule.DO_PATROL_SPAWNING, false)
    w.setTime(Globals.worldTimeFunction.call(w) as Integer ?: 6000)
}

Schedulers.sync().runLater({
    PlayerMoveEvent.getHandlerList().unregister(PluginUtils.get("Essentials"))
    BlockBreakEvent.getHandlerList().unregister(PluginUtils.get("Essentials"))
}, 2L)

Commands.create().assertPlayer().assertOp().handler { c ->
    def player = c.sender()
    c.reply("view distance = ${player.getViewDistance()}")

}.register("dev/thiimo")