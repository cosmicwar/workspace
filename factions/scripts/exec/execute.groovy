package scripts.exec

import scripts.shared.utils.Temple

import static scripts.execute.ArkRepo.*
import static scripts.execute.ArkExecute.*

println("${getPrefix()} : Starting Starcade - ${Temple.temple}")

addArgs("-XX:+UseG1GC", "-XX:+ParallelRefProcEnabled", "-XX:MaxGCPauseMillis=200", "-XX:+UnlockExperimentalVMOptions", "-XX:+UnlockDiagnosticVMOptions", "-XX:+DisableExplicitGC", "-XX:+AlwaysPreTouch")
addArgs("-XX:+UseStringDeduplication", "-XX:+UseFastUnorderedTimeStamps", "-XX:+UseLoopPredicate", "-XX:+RangeCheckElimination", "-XX:+EliminateLocks", "-XX:+DoEscapeAnalysis", "-XX:+UseCodeCacheFlushing")
addArgs("-XX:+SegmentedCodeCache", "-XX:+UseFastJNIAccessors", "-XX:+OptimizeStringConcat", "-XX:+UseCompressedOops", "-XX:+UseThreadPriorities", "-XX:+OmitStackTraceInFastThrow", "-XX:+TrustFinalNonStaticFields")
addArgs("-XX:+UseInlineCaches", "-XX:+RewriteBytecodes", "-XX:+RewriteFrequentPairs", "-XX:+UseNUMA", "-XX:-DontCompileHugeMethods", "-XX:+UseFPUForSpilling", "-XX:+UseFastStosb")
addArgs("-XX:+UseNewLongLShift", "-XX:+UseVectorCmov", "-XX:+UseXMMForArrayCopy", "-Dfile.encoding=UTF-8", "-Dterminal.jline=false", "-Dterminal.ansi=true"/*, "--add-modules", "jdk.incubator.vector"*/)
addArgs("-XX:G1NewSizePercent=40", "-XX:G1MaxNewSizePercent=50", "-XX:G1HeapRegionSize=16M", "-XX:G1ReservePercent=15", "-XX:G1HeapWastePercent=5", "-XX:G1MixedGCCountTarget=4", "-XX:InitiatingHeapOccupancyPercent=20")
addArgs("-XX:G1MixedGCLiveThresholdPercent=90", "-XX:G1RSetUpdatingPauseTimePercent=5", "-XX:SurvivorRatio=32", "-XX:+PerfDisableSharedMem", "-XX:MaxTenuringThreshold=1")

println "${getPrefix()} : Running production environment"

loadConfig("global/bStats", "bStats")

// To avoid the imports
def Sgse = Class.forName("com.ngxdev.grunner.Sgse")
def watch = Sgse.getMethod("watch", String[].class)
if (Temple.templebase == "hub" || Temple.templebase == "nexus" || Temple.templebase == "proxy" || Temple.templebase == "gproxy") {
    watch.invoke(null, new Object[]{new String[]{"scripts/exec/${Temple.templebase}/_execute.groovy"}})
} else {
    watch.invoke(null, new Object[]{new String[]{"scripts/exec/${Temple.templeId.replace("_local", "")}/_execute.groovy"}})
}
