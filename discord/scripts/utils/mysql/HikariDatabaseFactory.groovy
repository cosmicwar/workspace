package scripts.utils.mysql


import java.util.concurrent.Executor

class HikariDatabaseFactory implements DatabaseFactory {

    private int poolSize

    HikariDatabaseFactory(int poolSize) {
        this.poolSize = poolSize
    }

    Database make(DatabaseInfo info, Executor executor) {
        return new HikariDatabase(info, executor, this.poolSize)
    }
}