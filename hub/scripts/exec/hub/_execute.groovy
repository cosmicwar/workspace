package scripts.exec.hub

import scripts.shared.utils.Temple

import static scripts.execute.ArkExecute.*
import static scripts.execute.ArkRepo.*

setVersion("v1_19_R1")
setWorkspaceName("hub")

configureProduction()
addArgs("-Xms2G", "-Xmx8G", "-DPaper.WorkerThreadCount=4", "-DPaper.ignoreWorldDataVersion=true")
setJar("AsyncPaper-bundler-1.19.2-R0.1-SNAPSHOT-reobf.jar")

if (Temple.templeEnv == "dev") addArgs("-DdevMode=true")

execute {
    loadFromConfig("configs/global/bukkit.yml", "bukkit.yml")
    if (Temple.templeEnv != "dev") {
        loadFromConfig("configs/global/spigot.yml", "spigot.yml")
        loadFromConfig("configs/global/paper.yml", "paper.yml")
    }

    loadFromConfig("configs/worlds/starcade_hub_v2", "world", false)
    clear()

//    loadPlugin("arkgroovy7")
    loadPlugin("starlight8")
    loadPlugin("psql-lib")
    loadPlugin("floodgate-spigot")
    loadPlugin("ChatControl")
    loadPlugin("EssentialsX-2.19.5-dev+30-b135fcf")
    loadPlugin("PlugMan")
    loadPlugin("ProtocolLib")
    loadPlugin("spark")
    loadPlugin("Vault")
    loadPlugin("ViaBackwards")
    loadPlugin("ViaRewind")
    loadPlugin("ViaVersion")
    loadPlugin("LiteBans")
    loadPlugin("HolographicDisplays")

    loadPlugin("NoteBlockAPI")
    loadPlugin("PlaceholderAPI")

    loadPlugin("AnimatedScoreboard")
    loadPlugin("VanishNoPacket")

//    loadConfig("Starlight", "Starlight")
    loadConfig("starlight", "starlight")
    loadFromConfig("configs/proxy/floodgate.yml", "plugins/floodgate/config.yml")
    loadFromConfig("configs/global/floodgate/key.pem", "plugins/floodgate/key.pem")
    loadConfig("global/Essentials", "Essentials")
    loadConfig("global/ChatControlDefaults", "ChatControl")
    loadConfig("global/ViaRewind", "ViaRewind")
    loadConfig("global/ViaVersion", "ViaVersion")
    loadConfig("global/ViaBackwards", "ViaBackwards")
    loadConfig("global/ChatControl", "ChatControl")

    loadConfig("LiteBans${Temple.templeEnv == "dev" ? "-dev" : ""}", "LiteBans")
    loadConfig("AnimatedScoreboard", "AnimatedScoreboard")
    loadConfig("HolographicDisplays", "HolographicDisplays")
}
