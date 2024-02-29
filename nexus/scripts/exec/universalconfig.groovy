package scripts.exec

import org.starcade.starlight.Starlight
import org.spigotmc.AsyncCatcher
import scripts.shared.utils.preprocessors.VersionProcessor

AsyncCatcher.enabled = false
//TickThread.STRICT_THREAD_CHECKS = false
VersionProcessor.init()

Starlight.watch(
        "scripts/shared/legacy/utils/FastItemUtils_impl.groovy",
        "scripts/shared/utils/PAPI.groovy"
)

