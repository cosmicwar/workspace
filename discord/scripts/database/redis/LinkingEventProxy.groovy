package scripts.database.redis

class LinkingEventProxy implements OnMessage {

    private OnMessage wrapped
    private OnMessage next

    LinkingEventProxy(OnMessage wrapped, OnMessage next) {
        this.wrapped = wrapped
        this.next = next
    }

    @Override
    void message(String channel, String message) {
        wrapped.message(channel, message)
        next.message(channel, message)
    }
}

