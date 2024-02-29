package scripts.utils.mysql

import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.Executor

interface Database extends AutoCloseable, DatabaseInfo {
	AsyncDatabase sync()

	AsyncDatabase async()
	
	AsyncDatabase async(Executor executor)
	
	Executor asyncExecutor()

	Connection getConnection() throws SQLException
}