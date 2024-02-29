package scripts.utils.mysql

interface DatabaseInfo {
	String getURL()

	URI getURI()

	String getDatabase()

	String getUsername()

	String getPassword()
}