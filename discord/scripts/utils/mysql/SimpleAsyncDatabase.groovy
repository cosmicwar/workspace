package scripts.utils.mysql

import scripts.utils.CollectionUtils
import scripts.utils.LangUtil
import scripts.utils.StringUtils

import java.sql.*
import java.util.concurrent.Executor

class SimpleAsyncDatabase implements AsyncDatabase {
    private Database database
    private Executor executor

    SimpleAsyncDatabase(Database database, Executor executor) {
        this.database = database
        this.executor = executor
    }

    void execute(String sql, Object[] data) {
        execute(sql, { statement ->
            for (int i = 1; i <= data.length; ++i) {
                Object object = data[i - 1]

                if (object instanceof Boolean) {
                    statement.setBoolean(i, object as Boolean)
                } else if (object instanceof Byte) {
                    statement.setByte(i, object as Byte)
                } else if (object instanceof String) {
                    statement.setString(i, object as String)
                } else if (object instanceof Short) {
                    statement.setShort(i, object as Short)
                } else if (object instanceof Integer) {
                    statement.setInt(i, object as Integer)
                } else if (object instanceof Long) {
                    statement.setLong(i, object as Long)
                } else if (object instanceof Float) {
                    statement.setFloat(i, object as Float)
                } else if (object instanceof Double) {
                    statement.setDouble(i, object as Double)
                } else if (object instanceof Timestamp) {
                    statement.setTimestamp(i, object as Timestamp)
                }
            }
        })
    }

    void execute(String sql, AsyncDatabasePreparer preparer = null) {
        this.executor.execute {
            Connection connection = null
            PreparedStatement preparedStatement = null

            try {
                connection = this.database.getConnection()
                preparedStatement = connection.prepareStatement(sql)

                if (preparer != null) {
                    preparer.handle(preparedStatement)
                }
                preparedStatement.execute()
            } catch (SQLException exception) {
                throw new RuntimeException("execute(" + sql + ")", exception)
            } catch (Throwable t) {
                t.printStackTrace()
            } finally {
                LangUtil.closeSilently(connection, preparedStatement)
            }
        }
    }

    private Map<String, List<String>> tables = new HashMap<>()

    void createTable(String table, LinkedHashMap<String, String> columns, List<String> primaryKeys) {
        int size = columns.size()
        List<String> columnNames = new ArrayList<>(size)
        List<String> columnEntries = new ArrayList<>(size)

        for (Map.Entry<String, String> entry : columns.entrySet()) {
            String columnName = entry.getKey()
            columnNames.add(columnName)
            columnEntries.add(columnName + " " + entry.getValue())
        }
        this.tables.put(table, columnNames)

        execute("CREATE TABLE IF NOT EXISTS ${table} (${StringUtils.asString(columnEntries, ", ")}, PRIMARY KEY(${StringUtils.asString(primaryKeys, ", ")}))")
    }

    void insert(String table, Object[] data, String onDuplicate = null) {
        if (onDuplicate == null) {
            onDuplicate = ""
        } else {
            onDuplicate = " ON DUPLICATE KEY UPDATE " + onDuplicate
        }
        List<String> columnNames = this.tables.get(table)
        String columns = StringUtils.asString(columnNames, ", ")
        String values = StringUtils.asString(CollectionUtils.listOf("?", columnNames.size()), ", ")
        execute("INSERT INTO ${table} (${columns}) VALUES (${values})${onDuplicate}", data)
    }

    void executeUpdate(String sql, AsyncDatabasePreparer preparer, AsyncDatabaseSuccessor<Integer> successor) {
        this.executor.execute {
            Connection connection = null
            PreparedStatement preparedStatement = null

            try {
                connection = this.database.getConnection()
                preparedStatement = connection.prepareStatement(sql)

                if (preparer != null) {
                    preparer.handle(preparedStatement)
                }
                int updated = preparedStatement.executeUpdate()

                if (successor != null) {
                    successor.handle(updated)
                }
            } catch (SQLException exception) {
                if (exception.getMessage().contains("Duplicate entry")) {
                    if (successor != null) {
                        successor.handle(0)
                    }
                } else {
                    throw new RuntimeException("executeUpdate(" + sql + ")", exception)
                }
            } catch (Throwable t) {
                t.printStackTrace()
            } finally {
                LangUtil.closeSilently(connection, preparedStatement)
            }
        }
    }

    void executeBatch(String sql, AsyncDatabasePreparer preparer) {
        this.executor.execute {
            Connection connection = null
            PreparedStatement preparedStatement = null

            try {
                connection = this.database.getConnection()
                connection.setAutoCommit(false)
                preparedStatement = connection.prepareStatement(sql)
                if (preparer != null) {
                    preparer.handle(preparedStatement)
                }
                preparedStatement.executeBatch()
                connection.commit()
            } catch (SQLException exception) {
                exception.printStackTrace()
            } catch (Throwable t) {
                t.printStackTrace()
            } finally {
                connection.setAutoCommit(true)
                LangUtil.closeSilently(connection, preparedStatement)
            }
        }
    }

    void executeGeneratedKeys(String sql, AsyncDatabasePreparer preparer, AsyncDatabaseSuccessor<ResultSet> successor) {
        this.executor.execute {
            Connection connection = null
            PreparedStatement preparedStatement = null

            try {
                connection = this.database.getConnection()
                preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

                if (preparer != null) {
                    preparer.handle(preparedStatement)
                }
                preparedStatement.execute()

                if (successor != null) {
                    successor.handle(preparedStatement.getGeneratedKeys())
                }
            } catch (SQLException exception) {
                throw new RuntimeException("executeGeneratedKeys(" + sql + ")", exception)
            } catch (Throwable t) {
                t.printStackTrace()
            } finally {
                LangUtil.closeSilently(connection, preparedStatement)
            }
        }
    }

    void executeQuery(String sql, AsyncDatabasePreparer preparer, AsyncDatabaseSuccessor<ResultSet> successor) {
        this.executor.execute {
            Connection connection = null
            PreparedStatement preparedStatement = null
            ResultSet resultSet = null

            try {
                connection = this.database.getConnection()
                preparedStatement = connection.prepareStatement(sql)

                if (preparer != null) {
                    preparer.handle(preparedStatement)
                }
                resultSet = preparedStatement.executeQuery()

                if (successor != null) {
                    successor.handle(resultSet)
                }
            } catch (SQLException exception) {
                throw new RuntimeException("executeQuery(" + sql + ")", exception)
            } catch (Throwable t) {
                t.printStackTrace()
            } finally {
                LangUtil.closeSilently(connection, preparedStatement, resultSet)
            }
        }
    }

    String getDatabaseName() {
        return this.database.getURL().split("/").last()
    }
}