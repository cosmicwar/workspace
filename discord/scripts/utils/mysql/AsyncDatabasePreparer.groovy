package scripts.utils.mysql

import java.sql.PreparedStatement

interface AsyncDatabasePreparer {
	void handle(PreparedStatement preparedStatement)
}