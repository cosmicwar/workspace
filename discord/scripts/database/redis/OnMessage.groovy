package scripts.database.redis

interface OnMessage {
    void message(String channel, String message);
}


