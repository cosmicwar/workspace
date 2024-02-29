package scripts.utils.mysql

import scripts.utils.LangUtil
import scripts.utils.ManagedThreadFactory

import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class SimpleDatabaseManager implements DatabaseManager {
	static {
		try {
			Class.forName("org.mariadb.jdbc.Driver")
		} catch(Exception exception) {
			exception.printStackTrace()
		}
	}

	private Map<DatabaseInfo, Database> databases = new HashMap<>()
	private DatabaseInfoService infoService
	private DatabaseFactory factory
	private Executor executor
	private ThreadPoolExecutor ownedExecutor

	AtomicBoolean closed = new AtomicBoolean(false)

	SimpleDatabaseManager(DatabaseInfoService infoService, DatabaseFactory factory, String poolNameFormat, int poolCoreSize, int poolMaxSize) {
		this(infoService, factory, null)
		this.ownedExecutor = new ThreadPoolExecutor(
				poolCoreSize,
				poolMaxSize,
				30, TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(),
				new ManagedThreadFactory(poolNameFormat),
				{ runnable, executor -> runnable.run() }
		)
		this.executor = this.ownedExecutor
	}

	SimpleDatabaseManager(DatabaseInfoService infoService, DatabaseFactory factory, Executor executor) {
		this.infoService = infoService
		this.factory = factory
		this.executor = executor
	}

    DatabaseInfo info(String url, String username, String password) {
		return new SimpleDatabaseInfo(url, username, password)
	}

	void execute(Runnable runnable) {
		this.executor().execute(runnable)
	}

    Database getDatabase(DatabaseInfo info) {
		if (closed.get()) {
			throw new Exception("Connection attempted after database closed!")
		}

		if (info == null) {
			throw new IllegalArgumentException()
		}
        Database database = this.databases.get(info)

		if (database == null) {
			database = this.factory.make(info, this.executor)
			this.databases.put(info, database)
		}
		return database
	}
	
	Connection getConnection(DatabaseInfo info) throws SQLException {
		return this.getDatabase(info).getConnection()
	}

	void close() throws Exception {
		if (closed.getAndSet(true)) return

		try {
			this.ownedExecutor?.shutdown()
			this.ownedExecutor?.awaitTermination(1L, TimeUnit.MINUTES)
		} catch (InterruptedException e) {
			e.printStackTrace()
		} finally {
			LangUtil.closeSilently(this.databases.values())
			this.databases.clear()
		}
	}

    DatabaseInfoService infoService() {
		return this.infoService
	}

	Executor executor() {
		return this.executor
	}

	DatabaseManager scope() {
		return new BorrowedDatabaseManager(this)
	}
}