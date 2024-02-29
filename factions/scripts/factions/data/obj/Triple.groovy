package scripts.factions.data.obj

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

@CompileStatic(TypeCheckingMode.SKIP)
class Triple <K, V, T> {
    private K key
    private V value
    private T third

    Triple(K key, V value, T third) {
        this.key = key
        this.value = value
        this.third = third
    }

    static <K, V, T> Triple<K, V, T> of(K key, V value, T third) {
        return new Triple(key, value, third)
    }

    K getLeft() {
        return (K) this.key
    }

    V getMiddle() {
        return (V) this.value
    }

    T getRight() {
        return (T) this.third
    }

    void setValue(V value) {
        this.value = value
    }
}
