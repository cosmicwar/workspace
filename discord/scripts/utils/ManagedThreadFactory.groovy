package scripts.utils

import net.jodah.expiringmap.ExpiringMap

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class ManagedThreadFactory implements ThreadFactory {

    static {
        init()
    }

    @SuppressWarnings("deprecation")
    private static final void init() {
        ExpiringMap.setThreadFactory(new ManagedThreadFactory("DiscordBot ExpiringMap - %d"))
    }

    private ThreadGroup group
    private String nameFormat
    private AtomicInteger idNext = new AtomicInteger()

    ManagedThreadFactory(String nameFormat) {
        this.group = Thread.currentThread().getThreadGroup()
        this.nameFormat = nameFormat
    }

    Thread newThread(Runnable runnable) {
        Thread thread = new Thread(this.group, runnable, String.format(this.nameFormat, this.idNext.getAndIncrement()))
        thread.setDaemon(false)
        thread.setPriority(Thread.NORM_PRIORITY)
        return thread
    }
}