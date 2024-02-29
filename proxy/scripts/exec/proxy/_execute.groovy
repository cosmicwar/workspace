package scripts.exec.proxy


import scripts.shared.utils.Temple

import static scripts.execute.ArkExecute.*
import static scripts.execute.ArkRepo.*

setWorkspaceName("proxy")
setVersion("v1_20_R1")

configureProduction()

if (Temple.templeEnv == "dev") addArgs("-DdevMode=true")
if (Temple.templeId.endsWith("_scp")) addArgs("-Dnetwork=starcade")

if (Temple.templeId == "proxy_bedrock") addArgs("-Xms2G", "-Xmx24G", "-XX:-OmitStackTraceInFastThrow") // bedrock is more memory intensive
else if (Temple.templeId == "proxy_mh") addArgs("-Xms2G", "-Xmx12G", "-XX:-OmitStackTraceInFastThrow", "-Dmojang.sessionserver=https://api.minehut.com/mitm/session/minecraft/hasJoined")
else addArgs("-Xms2G", "-Xmx16G", "-XX:-OmitStackTraceInFastThrow")

setJar("velocity-3.2.jar")

execute {
    clear()

    if (Temple.templeId == "proxy_mh") {
        loadFromConfig("configs/proxy/proxy_mh.toml", "velocity.toml")
    } else {
        loadFromConfig("configs/proxy/proxy.toml", "velocity.toml")

        loadPlugin("BotSentry")
        loadFromConfig("configs/proxy/botsentry", "plugins/botsentry")
    }

    if (Temple.templeId.contains("bedrock")) {
        loadPlugin("Geyser-Velocity")
        loadPlugin("floodgate-velocity")

        loadFromConfig("configs/proxy/Geyser-Velocity", "plugins/Geyser-Velocity")

        loadFromConfig("configs/proxy/proxy_bedrock-geyser.yml", "plugins/Geyser-Velocity/config.yml")
        loadFromConfig("configs/global/floodgate/key.pem", "plugins/floodgate/key.pem")
        loadFromConfig("configs/proxy/floodgate.yml", "plugins/floodgate/config.yml")
    }

    loadPlugin("scproxy")

    loadFromConfig("configs/proxy/starcade.toml", "plugins/starcade.toml")
    loadFromConfig("configs/proxy/icons", "icons")
}