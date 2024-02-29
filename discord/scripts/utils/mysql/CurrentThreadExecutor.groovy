package scripts.utils.mysql

import java.util.concurrent.Executor

class CurrentThreadExecutor implements Executor {
    void execute(Runnable r) {
        r.run()
    }
}