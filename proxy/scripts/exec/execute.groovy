package scripts.exec

import scripts.execute.ArkExecute
import scripts.execute.ArkRepo
import scripts.shared.utils.Temple

println("${ArkExecute.getPrefix()} : Starting Starcade - ${Temple.temple}")

ArkExecute.addArgs(
        "-Djline.terminal=jline.UnsupportedTerminal",
        "-DuseEpoll=true",
        "--illegal-access=permit",
        "-XX:+UseG1GC",
        "-XX:+ParallelRefProcEnabled",
        "-XX:MaxGCPauseMillis=25",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+DisableExplicitGC",
        "-XX:+AlwaysPreTouch",
        "-XX:G1NewSizePercent=50",
        "-XX:G1MaxNewSizePercent=60",
        "-XX:G1HeapRegionSize=32M",
        "-XX:G1ReservePercent=10",
        "-XX:G1HeapWastePercent=5",
        "-XX:G1MixedGCCountTarget=4",
        "-XX:InitiatingHeapOccupancyPercent=30",
        "-XX:G1MixedGCLiveThresholdPercent=90",
        "-XX:G1RSetUpdatingPauseTimePercent=5",
        "-XX:SurvivorRatio=32",
        "-XX:+PerfDisableSharedMem",
        "-XX:MaxTenuringThreshold=1"
)

println "${ArkExecute.getPrefix()} : Running production environment"

ArkRepo.loadConfig("global/bStats", "bStats")

// To avoid the imports
def Sgse = Class.forName("com.ngxdev.grunner.Sgse")
def watch = Sgse.getMethod("watch", String[].class)
if (Temple.templebase == "hub" || Temple.templebase == "nexus" || Temple.templebase == "proxy" || Temple.templebase == "build" || Temple.templebase == "playground") {  // singleton servers
    watch.invoke(null, new Object[]{new String[]{"scripts/exec/${Temple.templebase}/_execute.groovy"}})
} else {
    watch.invoke(null, new Object[]{new String[]{"scripts/exec/${Temple.templebase}/${Temple.templeId.replace("_local", "")}/_execute.groovy"}})
}
