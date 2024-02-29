package scripts.utils.mysql

import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.Executor

interface DatabaseManager extends AutoCloseable {
	DatabaseInfo info(String url, String username, String password)

    DatabaseInfoService infoService()

	void execute(Runnable runnable)

	Executor executor()

    Database getDatabase(DatabaseInfo info)
	
	Connection getConnection(DatabaseInfo info) throws SQLException

    DatabaseManager scope()
}