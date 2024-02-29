package scripts.utils

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

@CompileStatic(TypeCheckingMode.SKIP)
class TimeUtils {
    static String formatTime(long difference) {
        long days = (long) (difference / 86400000)
        long hours = (long) (difference / 3600000) % 24
        long minutes = (long) (difference / 60000) % 60
        long seconds = (long) (difference / 1000) % 60
        String time = ''
        if (days > 0) {
            time += "${days}d "
        }
        if (hours > 0) {
            time += "${hours}h "
        }
        if (minutes > 0) {
            time += "${minutes}m "
        }
        if (seconds > 0) {
            time += "${seconds}s "
        }
        return time
    }
}


