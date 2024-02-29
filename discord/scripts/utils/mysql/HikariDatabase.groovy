package scripts.utils.mysql

import com.zaxxer.hikari.HikariDataSource

import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class HikariDatabase extends AbstractDatabase {
    private HikariDataSource dataSource

    HikariDatabase(DatabaseInfo info, Executor executor, int poolSize) {
        super(info, executor)
        this.dataSource = new HikariDataSource()
        this.dataSource.setDriverClassName("org.mariadb.jdbc.Driver")
        this.dataSource.setJdbcUrl(info.getURL())
        this.dataSource.setUsername(info.getUsername())
        this.dataSource.setPassword(info.getPassword())
        this.dataSource.setMaximumPoolSize(poolSize)
        this.dataSource.setMinimumIdle(Math.min(poolSize, 10))
        this.dataSource.setMaxLifetime(TimeUnit.MINUTES.toMillis(1L))
        this.dataSource.setIdleTimeout(TimeUnit.MINUTES.toMillis(1L))
        this.dataSource.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(60L))
        this.dataSource.setConnectionTimeout(TimeUnit.SECONDS.toMillis(55L))
    }

    Connection getConnection() throws SQLException {
        return this.dataSource.getConnection()
    }

    void close() {
        this.dataSource.close()
    }
}