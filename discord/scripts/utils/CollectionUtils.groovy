package scripts.utils

import java.util.function.Function
import java.util.function.Predicate

class CollectionUtils {
    static Number sum(Collection<? extends Number> collection) {
        Number sum = 0

        for (Number number : collection) {
            sum += number
        }
        return sum
    }

    static <T> List<T> listOf(T element, int size) {
        List<T> list = new ArrayList<>(size)

        for (int i = 0; i < size; ++i) {
            list.add(element)
        }
        return list
    }

    static <S, T> List<T> toList(S[] arr, Function<S, T> map) {
        List<T> list = new ArrayList<>(arr.length + 1)

        for (int i = 0; i < arr.length; ++i) {
            list.add(map.apply(arr[i]))
        }
        return list
    }

    static <S, T> T[] toArray(List<S> list, Function<S, T> map) {
        T[] arr = new Object[list.size()] as T[]

        for (int i = 0; i < list.size(); ++i) {
            arr[i] = map.apply(list.get(i))
        }
        return arr
    }

    static <S, T> T[] map(S[] arr, Function<S, T> map) {
        T[] mapped = new Object[arr.length] as T[]

        for (int i = 0; i < arr.length; ++i) {
            mapped[i] = map.apply(arr[i])
        }
        return mapped
    }

    static <T> T[] getAll(T[] arr, Predicate<T> predicate) {
        T[] cpy = new Object[arr.length] as T[]
        int filter = 0

        for (int i = 0; i < cpy.length; ++i) {
            if (predicate.test(arr[i])) {
                cpy[filter++] = arr[i]
            }
        }
        arr = new Object[filter] as T[]
        System.arraycopy(cpy, 0, arr, 0, filter)
        return arr
    }

    static <T> boolean contains(T[] arr, T value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                return true
            }
        }
        return false
    }
}