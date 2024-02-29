package scripts.factions.data.obj

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

@CompileStatic(TypeCheckingMode.SKIP)
class Pair<K, V> implements Map.Entry<K, V> {
    private K key
    private V value

    Pair() {}

    Pair(K key, V value) {
        this.key = key
        this.value = value
    }

    static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair(key, value)
    }

    K getKey() {
        return (K) this.getLeft()
    }

    V getValue() {
        return (V) this.getRight()
    }

    K getLeft() {
        return this.key
    }

    V getRight() {
        return this.value
    }

    V setValue(V value) {
        this.value = value
        return this.value
    }
}
