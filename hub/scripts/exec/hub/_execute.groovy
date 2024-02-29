package scripts.exec.hub

import scripts.shared.utils.Temple

import static scripts.execute.ArkExecute.*
import static scripts.execute.ArkRepo.*

setVersion("v1_20_R1")
setWorkspaceName("hub")

configureProduction()
addArgs("-Xms2G", "-Xmx8G", "-DPaper.WorkerThreadCount=4", "-DPaper.ignoreWorldDataVersion=true")
setJar("entropy-bundler-1.20.1-R0.9-reobf.jar")

if (Temple.templeEnv == "dev") addArgs("-DdevMode=true")

execute {

    loadFromConfig("configs/global/bukkit.yml", "bukkit.yml")
    loadFromConfig("configs/global/entropy.yml", "entropy.yml")
    loadFromConfig("configs/global/pufferfish.yml", "pufferfish.yml")
    loadFromConfig("configs/global/spigot.yml", "spigot.yml")

    loadFromConfig("configs/global/paper-global.yml", "config/paper-global.yml")
    loadFromConfig("configs/global/paper-world-defaults.yml", "config/paper-world-defaults.yml")

//    loadFromConfig("configs/worlds/starcade_hub_v2", "world", false)
    clear()

    loadPlugin("starlight8")
    loadPlugin("psql-lib")
    loadPlugin("floodgate-spigot")
    loadPlugin("EssentialsX-2.19.5-dev+30-b135fcf")
    loadPlugin("PlugMan")
    loadPlugin("ProtocolLib")
    loadPlugin("spark")
    loadPlugin("Vault")
    loadPlugin("ViaBackwards")
    loadPlugin("ViaRewind")
    loadPlugin("ViaVersion")
    loadPlugin("LiteBans")

    loadPlugin("NoteBlockAPI")
    loadPlugin("PlaceholderAPI")

    loadPlugin("VanishNoPacket")

    loadConfig("starlight", "starlight")
    loadFromConfig("configs/proxy/floodgate.yml", "plugins/floodgate/config.yml")
    loadFromConfig("configs/global/floodgate/key.pem", "plugins/floodgate/key.pem")

    loadConfig("global/Essentials", "Essentials")
    loadConfig("global/ViaRewind", "ViaRewind")
    loadConfig("global/ViaVersion", "ViaVersion")
    loadConfig("global/ViaBackwards", "ViaBackwards")

    loadConfig("LiteBans${Temple.templeEnv == "dev" ? "-dev" : ""}", "LiteBans")
//    loadConfig("AnimatedScoreboard", "AnimatedScoreboard")
//    loadConfig("HolographicDisplays", "HolographicDisplays")
}
