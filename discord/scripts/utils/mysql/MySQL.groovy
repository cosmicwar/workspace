package scripts.utils.mysql

class MySQL {

    static List<Map<String, Object>> creds = [
            [
                    "name": "global",
                    "url": "jdbc:mariadb://51.81.107.166:3306/global",
                    "username": "starcade",
                    "password": "3040ctIcFCCQ2S2zmBogPVkCFinkL68V8xXSC8mBPq11Niyb0i",
                    "aliases": []
            ]
    ];

    private static final String GLOBAL_DATABASE = "global"

    private static DatabaseManager manager
    private static void init() {
        if (manager == null) {
            manager = new SimpleDatabaseManager(new GroovyDatabaseInfoService(creds), new HikariDatabaseFactory(20), "Starlight Database Executor - %d", 2, 20)

//            Starlight.plugin.bind({
//                Starlight.plugin.logger.info("Shutting down SQL...")
//                manager?.close()
//            })
        }
    }

    static DatabaseManager getManager() {
        init()
        return manager
    }

    static Database getDatabase(String name = "default") {
        init()
        return manager.getDatabase(manager.infoService().byName(name))
    }

    static AsyncDatabase getSyncDatabase(String name = "default") {
        return getDatabase(name).sync()
    }

    static AsyncDatabase getAsyncDatabase(String name = "default") {
        return getDatabase(name).async()
    }

    static Database getGlobalDatabase() {
        return getDatabase(GLOBAL_DATABASE)
    }

    static AsyncDatabase getGlobalSyncDatabase() {
        return getDatabase(GLOBAL_DATABASE).sync()
    }

    static AsyncDatabase getGlobalAsyncDatabase() {
        return getDatabase(GLOBAL_DATABASE).async()
    }

}
