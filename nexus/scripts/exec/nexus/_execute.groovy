package scripts.exec.nexus

import static scripts.execute.ArkExecute.*
import static scripts.execute.ArkRepo.*
import scripts.shared.utils.Temple

setVersion("v1_20_R1")
addArgs("-Xms2G", "-Xmx8G", "-DPaper.ignoreWorldDataVersion=true", "--add-modules=jdk.incubator.vector")
addProgramArgs("--nogui")
setJar("entropy-bundler-1.20.1-R0.9-reobf.jar")

if (Temple.templeEnv == "dev") addArgs("-DdevMode=true")

execute {

    clear()
    loadPlugin("starlight8")

    loadPlugin("psql-lib")
    loadPlugin("floodgate-spigot")
    loadPlugin("PlugMan")
    loadPlugin("ProtocolLib")
    loadPlugin("spark")
    loadPlugin("Vault")
    loadPlugin("LiteBans")
    loadPlugin("EssentialsX-2.18.2.0")
    loadPlugin("BuycraftX")
    loadPlugin("PlayerLands")
    loadPlugin("nuvotifier-2.5.3")
    loadPlugin("VanishNoPacket")

    loadPlugin("PlaceholderAPI")
    loadPlugin("MVdWPlaceholderAPI")

    loadConfig("Starlight", "Starlight")
    loadFromConfig("configs/proxy/floodgate.yml", "plugins/floodgate/config.yml")
    loadConfig("global/ChatControlDefaults", "ChatControl")
    loadConfig("global/LiteBans${Temple.templeEnv == "dev" ? "-dev" : ""}", "LiteBans")

    loadConfig("nexus/Votifier", "Votifier")
}
