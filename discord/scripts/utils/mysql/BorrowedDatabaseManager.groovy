package scripts.utils.mysql

class BorrowedDatabaseManager extends DelegateDatabaseManager {
	BorrowedDatabaseManager(DatabaseManager databaseManager) {
		super(databaseManager)
	}

	@Override
	void close() {
		// we do not own this object, thus we can not close it.
	}
}