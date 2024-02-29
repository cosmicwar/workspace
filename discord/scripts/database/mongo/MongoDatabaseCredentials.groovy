package scripts.database.mongo


/**
 * Represents the credentials for a remote database.
 */
final class MongoDatabaseCredentials {

    static MongoDatabaseCredentials of(String address, int port, String database, String username, String password) {
        return new MongoDatabaseCredentials(address, port, database, username, password)
    }

    private final String address
    private final int port
    private final String database
    private final String username
    private final String password

    private MongoDatabaseCredentials(String address, int port, String database, String username, String password) {
        this.address = Objects.requireNonNull(address)
        this.port = port
        this.database = Objects.requireNonNull(database)
        this.username = Objects.requireNonNull(username)
        this.password = Objects.requireNonNull(password)
    }


    String getAddress() {
        return this.address
    }

    int getPort() {
        return this.port
    }


    String getDatabase() {
        return this.database
    }


    String getUsername() {
        return this.username
    }


    String getPassword() {
        return this.password
    }

    @Override
    boolean equals(Object o) {
        if (o == this) return true
        if (!(o instanceof MongoDatabaseCredentials)) return false
        final MongoDatabaseCredentials other = (MongoDatabaseCredentials) o

        return this.getAddress() == other.getAddress() &&
                this.getPort() == other.getPort() &&
                this.getDatabase() == other.getDatabase() &&
                this.getUsername() == other.getUsername() &&
                this.getPassword() == other.getPassword()
    }

    @Override
    int hashCode() {
        final int PRIME = 59
        int result = 1
        result = result * PRIME + this.getPort()
        result = result * PRIME + this.getAddress().hashCode()
        result = result * PRIME + this.getDatabase().hashCode()
        result = result * PRIME + this.getUsername().hashCode()
        result = result * PRIME + this.getPassword().hashCode()
        return result
    }

    @Override
    String toString() {
        return "MongoDatabaseCredentials(" +
                "address=" + this.getAddress() + ", " +
                "port=" + this.getPort() + ", " +
                "database=" + this.getDatabase() + ", " +
                "username=" + this.getUsername() + ", " +
                "password=" + this.getPassword() + ")"
    }
}


