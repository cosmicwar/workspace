package scripts.utils.mysql


import java.util.concurrent.Executor

abstract class AbstractDatabase implements Database, DatabaseInfo {
    private DatabaseInfo info
    private AsyncDatabase asyncDatabase
    private Executor asyncExecutor

    AbstractDatabase(DatabaseInfo info, Executor executor) {
        this.info = info
        this.asyncDatabase = new SimpleAsyncDatabase(this, this.asyncExecutor = executor)
    }

    AsyncDatabase sync() {
        return this.async(new CurrentThreadExecutor())
    }

    AsyncDatabase async() {
        return this.asyncDatabase
    }

    AsyncDatabase async(Executor executor) {
        return new SimpleAsyncDatabase(this, executor)
    }

    Executor asyncExecutor() {
        return this.asyncExecutor
    }

    URI getURI() {
        try {
            return new URI(this.getURL().replace("jdbc:", ""))
        } catch (URISyntaxException ignore) {
            return null
        }
    }

    String getDatabase() {
        String path = this.getURI().getPath().substring(1)
        int argsIndex = path.indexOf('?')
        if (argsIndex != -1) {
            path = path.substring(0, argsIndex)
        }
        return path
    }

    String getURL() {
        return this.info.getURL()
    }

    String getUsername() {
        return this.info.getUsername()
    }

    String getPassword() {
        return this.info.getPassword()
    }
}
