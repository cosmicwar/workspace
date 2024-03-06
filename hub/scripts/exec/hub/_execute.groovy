package scripts.exec.hub

import scripts.shared.utils.Temple

import static scripts.execute.ArkExecute.*
import static scripts.execute.ArkRepo.*

setVersion("v1_20_R1")
setWorkspaceName("hub")

configureProduction()
addArgs("-Xms2G", "-Xmx8G", "-DPaper.WorkerThreadCount=4", "-DPaper.ignoreWorldDataVersion=true")

addProgramArgs("--nogui")

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
    loadPlugin("EssentialsX-2.21.0")
    loadPlugin("PlugManX-2.3.3")
    loadPlugin("ProtocolLib")
    loadPlugin("Spark-1.10.17-Bukkit")
    loadPlugin("Vault-1.7.3")
    loadPlugin("ViaBackwards")
    loadPlugin("ViaRewind")
    loadPlugin("ViaVersion")

    loadPlugin("LiteBans")
    loadConfig("global/LiteBans", "LiteBans")

    loadPlugin("MVdWPlaceholderAPI-3.1.1")
    loadPlugin("PlaceholderAPI-2.10.9")

    loadPlugin("VanishNoPacket")

    // -------------------------------------------- //
    // WORLD EDITING
    // -------------------------------------------- //
    loadPlugin("FastAsyncWorldEdit-Bukkit-2.8.3-SNAPSHOT-606")
    loadPlugin("WorldGuard-7.0.9")

    loadConfig("hub/starlight", "starlight")
    loadFromConfig("configs/proxy/floodgate.yml", "plugins/floodgate/config.yml")
    loadFromConfig("configs/global/floodgate/key.pem", "plugins/floodgate/key.pem")

    loadConfig("global/Essentials", "Essentials")
    loadConfig("global/ViaRewind", "ViaRewind")
    loadConfig("global/ViaVersion", "ViaVersion")
    loadConfig("global/ViaBackwards", "ViaBackwards")
}
