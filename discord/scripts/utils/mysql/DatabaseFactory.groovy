package scripts.utils.mysql


import java.util.concurrent.Executor

interface DatabaseFactory {
    Database make(DatabaseInfo info, Executor executor)
}