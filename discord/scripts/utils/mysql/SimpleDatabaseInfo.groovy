package scripts.utils.mysql

class SimpleDatabaseInfo implements DatabaseInfo {
	private String url
	private String username
	private String password

	SimpleDatabaseInfo(String url, String username, String password) {
		this.url = url
		this.username = username
		this.password = password
	}

	URI getURI() {
		try {
			return new URI(this.getURL().replace("jdbc:", ""))
		} catch (URISyntaxException ignore) {
			return null
		}
	}

	String getDatabase() {
		String path = this.getURI().getPath().substring(1)
		int argsIndex = path.indexOf('?')
		if (argsIndex != -1) {
			path = path.substring(0, argsIndex)
		}
		return path
	}

	String getURL() {
		return this.url
	}

	String getUsername() {
		return this.username
	}

	String getPassword() {
		return this.password
	}

	@Override
	int hashCode() {
		final int prime = 31
		int result = 1
		result = prime * result + (this.url == null ? 0 : this.url.hashCode())
		result = prime * result + (this.username == null ? 0 : this.username.hashCode())
		result = prime * result + (this.password == null ? 0 : this.password.hashCode())
		return result
	}

	@Override
	boolean equals(Object object) {
		if (object == null) {
			return false
		}
		if (!(object instanceof DatabaseInfo)) {
			return false
		}
		DatabaseInfo other = (DatabaseInfo) object
		if (this.url == null) {
			if (other.getURL() != null) {
				return false
			}
		} else if (!this.url.equals(other.getURL())) {
			return false
		}
		if (this.username == null) {
			if (other.getUsername() != null) {
				return false
			}
		} else if (!this.username.equals(other.getUsername())) {
			return false
		}
		if (this.password == null) {
			if (other.getPassword() != null) {
				return false
			}
		} else if (!this.password.equals(other.getPassword())) {
			return false
		}
		return true
	}
}