package scripts.exec.nova1

import scripts.shared.utils.Temple

import static scripts.execute.ArkExecute.*
import static scripts.execute.ArkRepo.*

setVersion("v1_20_R1")
setWorkspaceName("factions")

addArgs("-Xms25G", "-Xmx64G", "-DPaper.ignoreWorldDataVersion=true", "--add-modules=jdk.incubator.vector")

addProgramArgs("--nogui")

setJar("entropy-bundler-1.20.1-R0.9-reobf.jar")

if (Temple.templeEnv == "dev") addArgs("-DdevMode=true")

execute {

    clear()
    loadPlugin("starlight8")

    // -------------------------------------------- //
    // STATIC PLUGINS
    // -------------------------------------------- //
    loadPlugin("CoreProtect-22.2")
    loadPlugin("Multiverse-Core-4.3.1")
    loadPlugin("MVdWPlaceholderAPI-3.1.1")
    loadPlugin("PlaceholderAPI-2.10.9")
    loadPlugin("PlugManX-2.3.3")
    loadPlugin("Spark-1.10.17-Bukkit")
    loadPlugin("Vault-1.7.3")
    loadPlugin("LiteBans")
    loadPlugin("VanishNoPacket")

    loadPlugin("DecentHolograms-2.8.5")

    // -------------------------------------------- //
    // PROTOCOL PLUGINS
    // -------------------------------------------- //
    loadPlugin("ViaBackwards")
    loadPlugin("ViaRewind")
    loadPlugin("ViaVersion")
    loadPlugin("psql-lib")
    loadPlugin("floodgate-spigot")
    loadPlugin("ProtocolLib")

    // -------------------------------------------- //
    // WORLD EDITING
    // -------------------------------------------- //
    loadPlugin("FastAsyncWorldEdit-Bukkit-2.8.3-SNAPSHOT-606")
    loadPlugin("WorldGuard-7.0.9")

    // -------------------------------------------- //
    // CONFIGS
    // -------------------------------------------- //

    loadConfig("global/starlight", "starlight")

    loadFromConfig("configs/proxy/floodgate.yml", "plugins/floodgate/config.yml")
    loadFromConfig("configs/global/floodgate/key.pem", "plugins/floodgate/key.pem")

    loadFromConfig("configs/factions/bukkit.yml", "bukkit.yml")
    loadFromConfig("configs/factions/entropy.yml", "entropy.yml")
    loadFromConfig("configs/factions/eula.txt", "eula.txt")
    loadFromConfig("configs/factions/pufferfish.yml", "pufferfish.yml")
    loadFromConfig("configs/factions/spigot.yml", "spigot.yml")

    loadFromConfig("configs/factions/paper-global.yml", "config/paper-global.yml")
    loadFromConfig("configs/factions/paper-world-defaults.yml", "config/paper-world-defaults.yml")

    loadConfig("global/LiteBans${Temple.templeEnv == "dev" ? "-dev" : ""}", "LiteBans")
    loadConfig("global/ViaRewind", "ViaRewind")
    loadConfig("global/ViaVersion", "ViaVersion")
    loadConfig("global/ViaBackwards", "ViaBackwards")
}