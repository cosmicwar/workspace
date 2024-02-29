package scripts.utils

class LangUtil {
    static void close(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch(Exception exception) {
                    exception.printStackTrace()
                }
            }
        }
    }

    static void close(Iterable<? extends AutoCloseable> closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch(Exception exception) {
                    exception.printStackTrace()
                }
            }
        }
    }

    static void closeSilently(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch(Exception ignore) {
                    // ignore
                }
            }
        }
    }

    static void closeSilently(Iterable<? extends AutoCloseable> closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch(Exception ignore) {

                }
            }
        }
    }
}