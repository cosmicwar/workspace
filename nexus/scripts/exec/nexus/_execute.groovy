package scripts.exec.nexus

import static scripts.execute.ArkExecute.*
import static scripts.execute.ArkRepo.*
import scripts.shared.utils.Temple

setVersion("v1_20_R1")
setWorkspaceName("nexus")

addArgs("-Xms2G", "-Xmx8G", "-DPaper.ignoreWorldDataVersion=true", "--add-modules=jdk.incubator.vector")
addProgramArgs("--nogui")

setJar("entropy-bundler-1.20.1-R0.9-reobf.jar")

if (Temple.templeEnv == "dev") addArgs("-DdevMode=true")

execute {

    clear()

    loadPlugin("starlight8")
    loadPlugin("psql-lib")
    loadPlugin("floodgate-spigot")
    loadPlugin("PlugManX-2.3.3")
    loadPlugin("ProtocolLib")
    loadPlugin("Spark-1.10.17-Bukkit")
    loadPlugin("Vault-1.7.3")
    loadPlugin("LiteBans")
    loadPlugin("EssentialsX-2.21.0")
    loadPlugin("tebex-bukkit")
    loadPlugin("nuvotifier")
    loadPlugin("VanishNoPacket")

    loadPlugin("MVdWPlaceholderAPI-3.1.1")
    loadPlugin("PlaceholderAPI-2.10.9")

    loadConfig("nexus/starlight", "starlight")
    loadFromConfig("configs/proxy/floodgate.yml", "plugins/floodgate/config.yml")
    loadConfig("global/LiteBans${Temple.templeEnv == "dev" ? "-dev" : ""}", "LiteBans")

    loadConfig("nexus/Votifier", "Votifier")
}
