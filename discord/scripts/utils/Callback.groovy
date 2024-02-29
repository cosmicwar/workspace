package scripts.utils

interface Callback<T> {
    void exec(T t);
}