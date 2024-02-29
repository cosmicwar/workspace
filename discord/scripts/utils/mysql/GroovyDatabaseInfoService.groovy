package scripts.utils.mysql

class GroovyDatabaseInfoService implements DatabaseInfoService {
	private static final String DEFAULT_NAME = "default"
	
	private Map<String, DatabaseInfo> infoCache = new HashMap<>()

	GroovyDatabaseInfoService(List<Map<String, Object>> databases) {
		for (Map<String, Object> database : databases) {

			DatabaseInfo info = new SimpleDatabaseInfo(
					database["url"] as String,
					database["username"] as String,
					database["password"] as String
			)
			for (String alias : database["aliases"] as List<String>) {
				this.infoCache.put(alias, info)
			}
			this.infoCache.put(database["name"] as String, info)
		}
	}

    DatabaseInfo byName(String name) {
		if (this.infoCache.containsKey(name)) {
			return this.infoCache.get(name)
		} else {
			return this.infoCache.get(DEFAULT_NAME)
		}
	}
}