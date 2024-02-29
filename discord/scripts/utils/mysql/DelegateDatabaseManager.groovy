package scripts.utils.mysql

import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.Executor

class DelegateDatabaseManager implements DatabaseManager {
	private DatabaseManager delegate

	DelegateDatabaseManager(DatabaseManager delegate) {
		this.delegate = delegate
	}

    DatabaseInfo info(String url, String username, String password) {
		return this.delegate.info(url, username, password)
	}

    DatabaseInfoService infoService() {
		return this.delegate.infoService()
	}

	void execute(Runnable runnable) {
		this.executor().execute(runnable)
	}

	Executor executor() {
		return this.delegate.executor()
	}

    Database getDatabase(DatabaseInfo info) {
		return this.delegate.getDatabase(info)
	}

	Connection getConnection(DatabaseInfo info) throws SQLException {
		return this.delegate.getConnection(info)
	}

	DatabaseManager scope() {
		return new BorrowedDatabaseManager(this)
	}

	void close() throws Exception {
		this.delegate.close()
	}
}