package scripts.utils.mysql

interface AsyncDatabaseSuccessor<T> {
	void handle(T result)
}