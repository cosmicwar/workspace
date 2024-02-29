package scripts.utils.mysql

import org.intellij.lang.annotations.Language

import java.sql.ResultSet

interface AsyncDatabase {
	void execute(String sql, Object[] data)

	void execute(String sql)

	void execute(@Language("SQL") String sql, AsyncDatabasePreparer preparer)

	void createTable(String table, LinkedHashMap<String, String> columns, List<String> primaryKeys)

	void insert(String table, Object[] data)

	void insert(String table, Object[] data, String onDuplicate)
	
	void executeUpdate(String sql, AsyncDatabasePreparer preparer, AsyncDatabaseSuccessor<Integer> successor)

	void executeBatch(String sql, AsyncDatabasePreparer preparer)

	void executeGeneratedKeys(String sql, AsyncDatabasePreparer preparer, AsyncDatabaseSuccessor<ResultSet> successor)
	
	void executeQuery(String sql, AsyncDatabasePreparer preparer, AsyncDatabaseSuccessor<ResultSet> successor)

	String getDatabaseName()
}